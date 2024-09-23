package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.models.Meeting;
import com.degilok.al.telegramtestw.models.enums.TimeSlot;
import com.degilok.al.telegramtestw.models.enums.UserState;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
@Setter
@Slf4j
public class BotService extends SpringWebhookBot {

    @Value(value = "${telegram.bot.username}")
    private String botUserName;

    @Value(value = "${telegram.bot.token}")
    private String botToken;

    @Value(value = "${telegram.bot.webhook-path}")
    private String webHookPath;

    private final MeetingService meetingService;
    private final MeetingRepository meetingRepository;


    public BotService(SetWebhook setWebhook, MeetingService meetingService, MeetingRepository meetingRepository) {
        super(setWebhook);
        this.meetingService = meetingService;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
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
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                Meeting newMeeting = new Meeting();
                newMeeting.setChatId(chatId);
                newMeeting.setUserSession(UserState.AWAITING_TOPIC);
                meetingRepository.save(newMeeting);
                sendTextMessage(chatId, "Добро пожаловать! Напишите тему встречи");
            } else {
                korocheZdecMyBeremDannyeKotoryePiwetUserIZabiraemEgoSoobwenieIEgoChatId(update.getMessage());
            }
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update.getCallbackQuery());
        }
        return null;
    }


    public void handleInput(Long chatId, String input) {
        Meeting meeting = meetingRepository.findByChatId(chatId);
        //UserState currentState = userStates.getOrDefault(chatId, UserState.AWAITING_TOPIC);//todo: добавить логику обработки сообщений после брони окошка
        if (meeting == null) {
            sendTextMessage(chatId, "Встреча не найдена. Пожалуйста, начните заново с /start.");
        } else {
            switch (meeting.getUserSession()) {
                case AWAITING_TOPIC:
                    meetingRepository.updateTopic(UserState.AWAITING_PROJECT.name(), input, chatId, UserState.AWAITING_TOPIC.name());
                    sendTextMessage(chatId, "Тема встречи сохранена. Напишите название проекта");
                    break;
                case AWAITING_PROJECT:
                    meetingRepository.updateProject(UserState.AWAITING_DATE.name(), input, chatId, UserState.AWAITING_PROJECT.name());
                    //sendTextMessage(chatId, "Проект сохранен. Выберите дату");
                    sendDateOptions(chatId);
                    break;
                case AWAITING_DATE:
                    DayOfWeek selectedDayOfWeek = DayOfWeek.valueOf(input.split("_")[1].toUpperCase());
                    LocalDate selectedDate = getNextOrSameDayOfWeek(selectedDayOfWeek);
                    meetingRepository.updateDate(UserState.AWAITING_TIME.name(), selectedDate, chatId, UserState.AWAITING_DATE.name());
                    //sendTextMessage(chatId, "Дата сохранена. Выберите время");
                    sendTimeOptions(chatId, selectedDayOfWeek.name());
                    break;
                case AWAITING_TIME:
                    String timeSlot = input.split("_")[1];
                    TimeSlot selectedTimeTimeSlot = TimeSlot.findByIndex(Integer.valueOf(timeSlot));
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(input.split("_")[3].toUpperCase());
                    LocalDate date = getNextOrSameDayOfWeek(dayOfWeek);
                    if (meetingRepository.checkSlot(selectedTimeTimeSlot.ordinal(), date) == null) {
                        meetingRepository.updateSlot(UserState.AWAITING_NAME.name(), selectedTimeTimeSlot.ordinal(), chatId, UserState.AWAITING_TIME.name());
                        sendTextMessage(chatId, "Вы выбрали время: " + selectedTimeTimeSlot.getDisplayName());
                        sendTextMessage(chatId, "Время сохранено. Укажите имя ответственного.");
                    } else {
                        sendTextMessage(chatId, "Время забронировано: " + input);
                    }
                    break;
                case AWAITING_NAME:
                    meetingRepository.updateResponsiblePerson(UserState.FINISHED.name(), input, chatId, UserState.AWAITING_NAME.name());
                    sendTextMessage(chatId, "Бронированный титан готов");
                    break;
            }
        }
    }


    public InlineKeyboardMarkup sendDaysKeyboard(Long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<DayOfWeek> workDays = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                if (day.getValue() >= today.getValue()) {
                    workDays.add(day);
                }
            }
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                if (day.getValue() < today.getValue()) {
                    workDays.add(day);
                }
            }
        }
        for (DayOfWeek day : workDays) {
            InlineKeyboardButton button = new InlineKeyboardButton(day.name());
            button.setCallbackData("DATE_" + day.name());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        markup.setKeyboard(rows);
        return markup;
    }


    public InlineKeyboardMarkup sendTimeSlotButtons(Long chatId, String dayOfWeekName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (TimeSlot timeSlot : TimeSlot.values()) {

            String bookedBy = getWhoBooked(timeSlot, dayOfWeekName);
            String buttonText = timeSlot.getDisplayName();

            if (bookedBy != null) {
                buttonText += " (Забронировано: " + bookedBy + ")";
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            //button.setText(timeSlot.getDisplayName());
            button.setText(buttonText);
            button.setCallbackData("TIME_" + timeSlot.ordinal() + "_DATE_" + dayOfWeekName);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private String getWhoBooked(TimeSlot timeSlot, String dayWeek) {

        LocalDate date = getNextOrSameDayOfWeek(DayOfWeek.valueOf(dayWeek.toUpperCase()));
        Meeting meeting = meetingRepository.findBySlotAndDate(timeSlot, date);
        if (meeting != null) {
            return meeting.getResponsiblePerson();
        }
        return null;
    }

    public LocalDate getNextOrSameDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek().equals(dayOfWeek)) {
            return today;
        }
        while (!today.getDayOfWeek().equals(dayOfWeek)) {
            today = today.plusDays(1);
        }
        return today;
    }


    @Operation(summary = "отправляет юзеру кнопки с доступными временными слотами для бронирования встречи")
    public void sendDateOptions(Long chatId) {
        InlineKeyboardMarkup dayOfWeek = sendDaysKeyboard(chatId);
        SendMessage message = new SendMessage(chatId.toString(), "Проект сохранен. Выберите день недели для встречи: ");
        message.setReplyMarkup(dayOfWeek);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendTimeOptions(Long chatId, String dayOfWeekName) {
        InlineKeyboardMarkup timesOfDay = sendTimeSlotButtons(chatId, dayOfWeekName);
        SendMessage message = new SendMessage(chatId.toString(), "Выберите время");
        message.setReplyMarkup(timesOfDay);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    @Operation(summary = "обрабатывает запросы, когда юзер нажимает на кнопку")
    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        String callBackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        if (callBackData.startsWith("TIME_")) {
            handleInput(chatId, callBackData);
        } else if (callBackData.startsWith("DATE_")) {
            handleInput(chatId, callBackData);
        }
    }


    public void korocheZdecMyBeremDannyeKotoryePiwetUserIZabiraemEgoSoobwenieIEgoChatId(Message message) {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        log.info("message: " + messageText);
        handleInput(chatId, messageText);
    }


    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            log.info("Отправка сообщения в чат" + chatId, text);
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
            throw new RuntimeException(e);
        }
    }
}