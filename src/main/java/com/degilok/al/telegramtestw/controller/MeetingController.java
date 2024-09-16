package com.degilok.al.telegramtestw.controller;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.service.impl.MeetingServiceImpl;
import com.degilok.al.telegramtestw.service.impl.MessageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping("/webhook")
public class MeetingController {


    private final MeetingServiceImpl meetingServiceImpl;
    private final MessageServiceImpl messageServiceImpl;


    public MeetingController(MeetingServiceImpl meetingServiceImpl, MessageServiceImpl messageServiceImpl) {

        this.meetingServiceImpl = meetingServiceImpl;
        this.messageServiceImpl = messageServiceImpl;
    }


    @Operation(summary = "метод для обработки запросов от тг")
    @PostMapping()
    public ResponseEntity<?> handleWebHook(@RequestBody Update update) {
        if (update.hasCallbackQuery()) {
            meetingServiceImpl.processCallBackQuery(update.getCallbackQuery());
            return ResponseEntity.ok("Callback обработан");
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            messageServiceImpl.processTextMessage(messageText, chatId);
            return ResponseEntity.ok("Сообщение обработано");
        }
        return ResponseEntity.badRequest().body("Invalid update received");
    }


    @Operation(summary = "метод для бронирования встреч")
    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleMeeting(@RequestBody Meeting meeting) {
        try {
            meetingServiceImpl.saveMeeting(meeting);
            return ResponseEntity.ok("Встреча успешно забронирована");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}