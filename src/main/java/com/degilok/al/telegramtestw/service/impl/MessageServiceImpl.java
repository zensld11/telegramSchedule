package com.degilok.al.telegramtestw.service.impl;

import com.degilok.al.telegramtestw.configuration.BotConfigWithImpl;
import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import com.degilok.al.telegramtestw.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Tag(name = "сервис отвечающий за обработку сообщений и взаимодействие с пользователем")
public class MessageServiceImpl implements MessageService {


    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final BotConfigWithImpl botConfigWith;
    private final MeetingRepository meetingRepository;

    public MessageServiceImpl(BotConfigWithImpl botConfigWith, MeetingRepository meetingRepository) {
        this.botConfigWith = botConfigWith;
        this.meetingRepository = meetingRepository;
    }

    @Operation(summary = "мой метод, который обрабатывает текстовые сообщения пользователя",
            description = "если сообщение юзера равно /start, то вызывается handleStartCommand, который предлагает выбрать дату и время встречи," +
                    "в другом же случае просто сохраняет тему встречи(topic)")
    @Override
    public String processTextMessage(String messageText, Long chatId) {
        if (messageText.equals("/start")) {
            handleStartCommand(chatId);
        } else {
            handleMeetingDetails(chatId, messageText);
        }
        return "Message processed";
    }

    @Operation(summary = "отвечает на команду /start и вызывает sendDateOptions, который отображает кнопочки для времени встречи")
    private void handleStartCommand(Long chatId) {
        sendTextMessage(chatId, "Привет! Выберите дату и время для встречи");
        botConfigWith.sendDateOptions(chatId);
    }

    @Operation(summary = "сохраняет тему встречи")
    private void handleMeetingDetails(Long chatId, String messageText) {
        saveBookingDetails(chatId, messageText);
        sendTextMessage(chatId, "Тема встречи сохранена. Выберите дату");
    }


    @Operation(summary = "метод для отправки сообщений юзеру")
    @Override
    public void sendTextMessage(Long chatId, String text) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            log.info("Отправка сообщения в чат" + chatId, text);
            botConfigWith.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Operation(summary = "это мой метод, который сохраняет детали бронирования, тему(topic)")
    @Override
    public void saveBookingDetails(Long chatId, String meetingTopic) {
        Meeting meeting = new Meeting();
        meeting.setId(chatId);
        meeting.setTopic(meetingTopic);
        meetingRepository.save(meeting);
    }
}