package com.degilok.al.telegramtestw.configuration;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import com.degilok.al.telegramtestw.service.impl.MeetingServiceImpl;
import com.degilok.al.telegramtestw.service.impl.MessageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
@Slf4j
public class BotConfigWithImpl extends SpringWebhookBot {

    private String webHootPath;
    private String botUserName;
    private String botToken;

    @Autowired
    @Lazy
    private MeetingServiceImpl meetingServiceImpl;

    @Autowired
    @Lazy
    private MessageServiceImpl messageServiceImpl;

    @Autowired
    @Lazy
    private MeetingRepository meetingRepository;


    public BotConfigWithImpl(SetWebhook setWebhook) {
        super(setWebhook);
        this.webHootPath = setWebhook.getUrl();
    }

    @Override
    public String getBotPath() {
        return webHootPath;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Operation(summary = "метод, который получает все виды обновления от тг")
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update.getCallbackQuery());
        }
        return null;
    }

    @Operation(summary = "обрабатывает сообщения и передает их на обработку в messageServiceImpl")
    private void handleTextMessage(Message message) {
        String messageText = message.getText();
        Long chatId = message.getChatId();

        log.info("Received message: " + messageText);
        messageServiceImpl.processTextMessage(messageText, chatId);
    }

    @Operation(summary = "обрабатывает запросы, когда юзер нажимает на кнопку")
    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        meetingServiceImpl.processCallBackQuery(callbackQuery);
    }


    @Operation(summary = "отправляет юзеру кнопки с доступными временными слотами для бронирования встречи")
    public void sendDateOptions(Long chatId) {
        LocalDate selectedDate = LocalDate.now();
        List<LocalTime> bookedTimes = getBookedTimes(selectedDate);

        InlineKeyboardMarkup markup = createInlineKeyboard(bookedTimes);

        SendMessage message = new SendMessage(chatId.toString(), "Выберите доступное время");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Operation(summary = "список уже забронированных временных слотов для исключения из списка доступных")
    private List<LocalTime> getBookedTimes(LocalDate localDate) {
        List<Meeting> meetings = meetingRepository.findByDate(localDate);
        return meetings.stream()
                .map(Meeting::getStartTime)
                .collect(Collectors.toList());
    }


    @Operation(summary = "создает инлайн(InlineKeyboardMarkup) с кнопками времени, и исключает те, что уже забронированы")
    private InlineKeyboardMarkup createInlineKeyboard(List<LocalTime> bookedTimes) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int hour = 8; hour <= 18; hour++) {
            LocalTime slot = LocalTime.of(hour, 0);
            if (!bookedTimes.contains(slot)) {
                InlineKeyboardButton button = new InlineKeyboardButton(slot.toString());
                button.setCallbackData("Забронирован_" + slot.toString());
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(button);
                rows.add(row);
            }
        }
        markup.setKeyboard(rows);
        return markup;
    }
}