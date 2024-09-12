package com.degilok.al.telegramtestw.configuration;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import com.degilok.al.telegramtestw.service.MessageService;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BotConfigWithImpl extends SpringWebhookBot implements MessageService {

    String webHootPath;
    String botUserName;
    String botToken;


    private MessageService messageService;
    private DefaultBotOptions defaultBotOptions;
    private MeetingRepository meetingRepository;


    public BotConfigWithImpl(MessageService messageService, SetWebhook setWebhook) {
        super(setWebhook);
        this.messageService = messageService;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            Long chatId = update.getMessage().getChatId();

            processTextMessage(messageText, chatId);
        }
        return null;
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


    @Override
    public void processTextMessage(String messageText, Long chatId) {
        if (messageText.equals("/start")) {
            sendTextMessage(chatId, "Привет! Выберите дату и время для встречи");
            sendDateOptions(chatId);
        } else {
            saveBookingDetails(chatId, messageText);
            sendTextMessage(chatId, "Тема встречи сохранена. Выберите дату");
        }
    }


    @Override
    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processCalBackQuery(CallbackQuery callbackQuery) {
        String callBackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        if (callBackData.startsWith("Забронирован_")) {
            String timeSlot = callBackData.substring(12);
            LocalTime selectedTime = LocalTime.parse(timeSlot);
            LocalDate selectedDate = LocalDate.now();

            String responsiblePerson = "Ответственный"; // Здесь можно взять информацию из запроса или базы данных
            String project = "Проект"; // Здесь можно взять информацию из запроса или базы данных
            String topic = "Тема"; // Здесь можно взять информацию из запроса или базы данных

            saveMeeting(chatId, selectedDate, selectedTime, responsiblePerson, project, topic);
            //chatId, selectedDate, selectedTime, responsiblePerson, project, topic);

            sendTextMessage(chatId, "Встреча забронирована на " + selectedTime.toString());
        }
    }


    @Override
    public void saveMeeting(Long chatId, LocalDate date, LocalTime time, String selectedDate, String selectedTime, String topic) {

        LocalDate localDate = LocalDate.parse(selectedDate);
        LocalTime localTime = LocalTime.parse(selectedTime);

        Meeting meeting = new Meeting();
        meeting.setDate(localDate);
        meeting.setStartTime(localTime);
        meetingRepository.save(meeting);

        sendTextMessage(chatId, "Ваша встреча успешно забронирована на " + selectedDate + " " + selectedTime);
    }


    @Override
    public void sendDateOptions(Long chatId) {

        LocalDate selectedDate = LocalDate.now();

        List<Meeting> meetings = meetingRepository.findByDate(selectedDate);
        List<LocalTime> bookedTimes = new ArrayList<>();
        for (Meeting meeting : meetings) {
            bookedTimes.add(meeting.getStartTime());
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        for (int hour = 8; hour <= 18; hour++) {
            LocalTime slot = LocalTime.of(hour, 0);

            if (!bookedTimes.contains(slot)) {
                List<InlineKeyboardButton> rowInLine = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(slot.toString());
                button.setCallbackData("Забронирован_" + slot.toString());
                rowInLine.add(button);
                rowsInLine.add(rowInLine);
            }
        }
        markup.setKeyboard(rowsInLine);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите доступное время:");
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveBookingDetails(Long chatId, String messageText) {

    }
}
