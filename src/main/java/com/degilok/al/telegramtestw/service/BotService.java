package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.configuration.BotConfig;
import com.degilok.al.telegramtestw.models.Meeting;
import com.degilok.al.telegramtestw.models.enums.SlotStatus;
import com.degilok.al.telegramtestw.models.enums.TimeSlot;
import com.degilok.al.telegramtestw.models.enums.UserState;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
@Slf4j
public class BotService extends SpringWebhookBot {

    private static final Long GROUP_ID = -1002459479576l;
    private final BotConfig botConfig;
    private final MeetingRepository meetingRepository;


    public BotService(SetWebhook setWebhook, BotConfig botConfig, MeetingRepository meetingRepository) {
        super(setWebhook);
        this.botConfig = botConfig;
        this.meetingRepository = meetingRepository;
    }

    @Override
    public String getBotPath() {
        return botConfig.getWebHookPath();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();

            if (!isUserInGroup(userId)) {
                sendTextMessage(chatId, "У вас нет доступа к этому боту");
                return null;
            }

            String messageText = update.getMessage().getText();
            if (messageText.equals("/start")) {
                Meeting activeMeeting = meetingRepository.findActiveMeetingByChatId(chatId, UserState.FINISHED);
                if (activeMeeting != null) {
                    sendTextMessage(chatId, "Вы уже забронировали встречу. Завершите или отмените текущую встречу, чтобы создать новую");
                }
                if (activeMeeting == null) {
                    Meeting newMeeting = new Meeting();
                    newMeeting.setChatId(chatId);
                    newMeeting.setUserSession(UserState.AWAITING_DATE);
                    meetingRepository.save(newMeeting);
                    sendTextMessageToShow(chatId, "Добро пожаловать!", createBottomButton());
                    sendDateOptions(chatId);
                }
            } else if (messageText.equals("Просмотр встреч")) {
                showView(chatId);
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

        if (meeting == null) {
            sendTextMessage(chatId, "Встреча не найдена. Пожалуйста, начните заново с /start.");
        } else {
            log.info("Текущий статус встречи: {}", meeting.getUserSession());

            switch (meeting.getUserSession()) {
                case AWAITING_DATE:
                    try {
                        String[] inputParts = input.split("_");
                        log.info("Разделенные входные данные: {}", Arrays.toString(inputParts));

                        if (inputParts.length >= 2 && inputParts[0].equals("DATE")) {//было два, было не >, а <
                            String dayOfWeekString = inputParts[1].toUpperCase();
                            log.info("Выбранный день недели: {}", dayOfWeekString);

                            DayOfWeek selectedDayOfWeek;

                            try {
                                selectedDayOfWeek = DayOfWeek.valueOf(dayOfWeekString);
                                log.info("Корректный день недели: {}", selectedDayOfWeek);
                            } catch (IllegalArgumentException e) {
                                log.error("Некорректный день недели: {}", dayOfWeekString);
                                sendTextMessage(chatId, "Ошибка: некорректный день недели. Попробуйте снова.");
                                return;
                            }

                            LocalDate selectedDate = getNextOrSameDayOfWeek(selectedDayOfWeek);
                            log.info("Полученная дата: {}", selectedDate);
                            if (selectedDate == null) {
                                log.error("Получена нулевая дата для дня недели: {}", selectedDayOfWeek);
                                sendTextMessage(chatId, "Ошибка: не удалось получить дату. Пожалуйста, попробуйте снова.");
                                return;
                            }

                            log.info("Выбранная дата: {}", selectedDate);
                            meetingRepository.updateDate(UserState.AWAITING_TIME.name(), selectedDate, chatId, UserState.AWAITING_DATE.name());
                            sendTimeOptions(chatId, selectedDayOfWeek.name());
                        } else {
                            log.warn("Некорректный формат ввода: {}", input);
                            sendTextMessage(chatId, "Некорректный формат даты. Пожалуйста, выберите дату снова");
                        }
                    } catch (NullPointerException e) {
                        log.error("NullPointerException: {}", e.getMessage());
                        sendTextMessage(chatId, "Произошла ошибка. Пожалуйста, повторите попытку.");

                    } catch (Exception e) {
                        log.error("Ошибка при обработке даты: {}", e.getMessage());
                        sendTextMessage(chatId, "Произошла ошибка при выборе даты. Пожалуйста, выберите дату снова.");
                    }

                    break;
                case AWAITING_TIME:
                    //мои данные инпута: TIME_0_DATE_WEDNESDAY
                    try {
                        log.info("Полученный input: {}", input);

                        String[] timeInputParts = input.split("_");
                        if (timeInputParts.length != 4 || !timeInputParts[0].equals("TIME")) {
                            sendTextMessage(chatId, "Некорректный формат времени. Пожалуйста, выберите время снова.");
                            return;
                        }
                        String timeSlot = timeInputParts[1];
                        int timeSlotIndex = Integer.parseInt(timeSlot);

                        log.info("Индекс выбранного временного слота: {}", timeSlotIndex);

                        TimeSlot selectedTimeTimeSlot = TimeSlot.findByIndex(Integer.valueOf(timeSlotIndex));
                        DayOfWeek dayOfWeek = DayOfWeek.valueOf(input.split("_")[3].toUpperCase());
                        LocalDate date = getNextOrSameDayOfWeek(dayOfWeek);

                        log.info("Выбранный день недели: {}, Дата: {}", dayOfWeek, date);

                        if (meetingRepository.checkSlot(selectedTimeTimeSlot.ordinal(), date) != null) {
                            sendTextMessage(chatId, "Этот слот занят или в процессе бронирования. Пожалуйста, выберите другой слот");
                            log.warn("Слот {} на дату {} уже занят.", selectedTimeTimeSlot.getDisplayName(), date);

                        } else if (selectedTimeTimeSlot.getStatus() == SlotStatus.AVAILABLE) {
                            log.info("Слот {} доступен. Резервируем.", selectedTimeTimeSlot.getDisplayName());

                            meeting.setSlot(selectedTimeTimeSlot);
                            reserveSlot(meeting);
                            meeting.setSlotStatus(SlotStatus.BOOKED);
                            meetingRepository.updateSlot(UserState.AWAITING_TOPIC.name(), selectedTimeTimeSlot.ordinal(), chatId, UserState.AWAITING_TIME.name());
                            meetingRepository.save(meeting);
                            sendTextMessage(chatId, "Вы выбрали время: " + selectedTimeTimeSlot.getDisplayName());
                            sendTextMessage(chatId, "Время сохранено. Напишите название темы встречи:");
                        } else {
                            sendTextMessage(chatId, "Этот слот уже занят или в процессе бронирования");
                            log.warn("Слот {} недоступен для бронирования. Статус: {}", selectedTimeTimeSlot.getDisplayName(), selectedTimeTimeSlot.getStatus());
                        }

                    } catch (
                            NumberFormatException e) {
                        log.error("Ошибка преобразования временного слота: {}", e.getMessage());
                        sendTextMessage(chatId, "Произошла ошибка при выборе времени. Пожалуйста, повторите попытку.");
                    } catch (
                            IllegalArgumentException e) {
                        log.error("Некорректный день недели блин", e.getMessage());
                        sendTextMessage(chatId, "думаю ввелся некорректный день");
                    }
                    break;
                case AWAITING_TOPIC:
                    if (input != null && !input.trim().

                            isEmpty()) {
                        meetingRepository.updateTopic(UserState.AWAITING_PROJECT.name(), input, chatId, UserState.AWAITING_TOPIC.name());
                        sendTextMessage(chatId, "Тема встречи сохранена. Напишите название проекта");
                    } else {
                        sendTextMessage(chatId, "Пожалуйста, введите тему встречи.");
                    }
                    break;
                case AWAITING_PROJECT:
                    meetingRepository.updateProject(UserState.AWAITING_NAME.name(), input, chatId, UserState.AWAITING_PROJECT.name());

                    sendTextMessage(chatId, "Проект сохранен. Напишите имя ответственного");
                    break;
                case AWAITING_NAME:
                    meetingRepository.updateResponsiblePerson(UserState.FINISHED.name(), input, chatId, UserState.AWAITING_NAME.name());

                    confirmBooking(meeting);

                    sendTextMessage(chatId, "Встреча забронирована");
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

        Map<DayOfWeek, String> dayNamesInRussia = new HashMap<>();
        dayNamesInRussia.put(DayOfWeek.MONDAY, "ПОНЕДЕЛЬНИК");
        dayNamesInRussia.put(DayOfWeek.TUESDAY, "ВТОРНИК");
        dayNamesInRussia.put(DayOfWeek.WEDNESDAY, "СРЕДА");
        dayNamesInRussia.put(DayOfWeek.THURSDAY, "ЧЕТВЕРГ");
        dayNamesInRussia.put(DayOfWeek.FRIDAY, "ПЯТНИЦА");

        LocalDate currentDate = LocalDate.now();

        for (DayOfWeek day : workDays) {
            LocalDate date = currentDate.with(day);
            if (date.isBefore(currentDate)) {
                date = date.plusWeeks(1);
            }

            InlineKeyboardButton button = new InlineKeyboardButton(
                    dayNamesInRussia.get(day) + " - "
                            + date.format(DateTimeFormatter.ofPattern("dd.MM"))
            );
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
        message.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<TimeSlot> availableSlots = Arrays.stream(TimeSlot.values())
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (availableSlots.isEmpty()) {
            message.setText("Нет доступных временных слотов.");
            message.setReplyMarkup(inlineKeyboardMarkup);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return inlineKeyboardMarkup;
        }

        for (TimeSlot timeSlot : availableSlots) {
            String bookedBy = getWhoBooked(timeSlot, dayOfWeekName);
            String buttonText = timeSlot.getDisplayName();

            if (bookedBy != null) {
                buttonText += " (Забронировано: " + bookedBy + ")";
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("TIME_" + timeSlot.ordinal() + "_DATE_" + dayOfWeekName);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отмена");
        cancelButton.setCallbackData("CANCEL_MEETING");

        List<InlineKeyboardButton> cancelRow = new ArrayList<>();
        cancelRow.add(cancelButton);
        rows.add(cancelRow);

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
        SendMessage message = new SendMessage(chatId.toString(), "Выберите день недели для встречи: ");
        message.setReplyMarkup(dayOfWeek);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendTimeOptions(Long chatId, String dayOfWeekName) {
        InlineKeyboardMarkup timesOfDay = sendTimeSlotButtons(chatId, dayOfWeekName);

        if (timesOfDay.getKeyboard().isEmpty()) {
            sendTextMessage(chatId, "Нет доступных временных слотов на выбранный день");
            return;
        }

        SendMessage message = new SendMessage(chatId.toString(), "Выберите время");
        message.setReplyMarkup(timesOfDay);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения с временными слотами: " + e.getMessage(), e);
        }
    }


    private Map<Long, LocalDate> userContextMap = new HashMap<>();

    @Operation(summary = "обрабатывает запросы, когда юзер нажимает на кнопку")
    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        String callBackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        if (callBackData.startsWith("TIME_")) {
            handleInput(chatId, callBackData);
        } else if (callBackData.startsWith("DATE_")) {
            handleInput(chatId, callBackData);
        } else if (callBackData.equals("CANCEL_MEETING")) {
            cancelMeeting(chatId);
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


    @Operation(summary = "метод, который проверяет встречи, бронь которых превышает одну минуту")
    @Scheduled(fixedRate = 30000)
    public void notifyUserAboutMeetingEnd() {
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);//3

        List<Meeting> expiredMeetings = meetingRepository.findByCreationTimeBeforeAndUserSessionNotFinished(threeMinutesAgo, UserState.FINISHED);

        for (Meeting meeting : expiredMeetings) {
            if (!meeting.isNotified()) {
                sendTextMessage(meeting.getChatId(), "Ваша встреча будет удалена через 1 минуту, если вы ее не завершите");
                meeting.setNotified(true);
                meetingRepository.save(meeting);
            }
        }
    }

    @Operation(summary = "метод удаления встреч которые длятся более двух минут")
    @Scheduled(cron = "0 * * * * *")
    public void deleteExpiredMeetings() {
        LocalDateTime fourMinutesAgo = LocalDateTime.now().minus(2, ChronoUnit.MINUTES);

        List<Meeting> meetingsToDelete = meetingRepository.findByCreationTimeBeforeAndUserSessionNotFinished(fourMinutesAgo, UserState.FINISHED);

        if (!meetingsToDelete.isEmpty()) {
            for (Meeting meeting : meetingsToDelete) {
                meeting.setNotified(false);
                meetingRepository.save(meeting);
            }
            meetingRepository.deleteAll(meetingsToDelete);
            log.info("Удалено " + meetingsToDelete.size() + " встреч");
        }
        for (Meeting meeting : meetingsToDelete) {
            sendTextMessage(meeting.getChatId(), "Ваша встреча была удалена из-за неактивности");
        }
    }

    private void sendTextMessageToShow(Long chatId, String text, ReplyKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private boolean isUserInGroup(Long userId) {
        try {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(GROUP_ID);
            getChatMember.setUserId(userId);

            ChatMember chatMember = execute(getChatMember);
            return chatMember.getStatus().equals("member") ||
                    chatMember.getStatus().equals("administrator") ||
                    chatMember.getStatus().equals("creator");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }


    public ReplyKeyboardMarkup createBottomButton() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButton showButton = new KeyboardButton("Просмотр встреч");

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(showButton);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(keyboardRow);
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }


    private void showView(Long chatId) {
        List<Meeting> meetings = meetingRepository.findAll();
        if (meetings.isEmpty()) {
            sendTextMessage(chatId, "Встреч не запланировано");
        } else {
            StringBuilder messageText = new StringBuilder("Запланированные встречи");
            for (Meeting meeting : meetings) {
                messageText.append("\nВремя: ");
                if (meeting.getSlot() != null) {
                    messageText.append(meeting.getSlot().getDisplayName());
                } else {
                    messageText.append("Слот не задан");
                }
                messageText.append(" ⬎ ");
                if (meeting.getTopic() != null) {
                    messageText.append("\nТема: ").append(meeting.getTopic());
                } else {
                    messageText.append("\nТема: Не задана");
                }

                if (meeting.getDate() != null) {
                    messageText.append("\nДата: ").append(meeting.getDate())
                            .append(" - ").append(meeting.getDate().getDayOfWeek());
                } else {
                    messageText.append("\nДата: Не задана");
                }

                if (meeting.getResponsiblePerson() != null) {
                    messageText.append("\nОтветственный: ").append(meeting.getResponsiblePerson());
                } else {
                    messageText.append("\nОтветственный: Не задан");
                }

                messageText.append("\n\n");
            }
            sendTextMessage(chatId, messageText.toString());
        }
    }

    public void reserveSlot(Meeting meeting) {
        TimeSlot selectedSlot = meeting.getSlot();
        if (selectedSlot.getStatus() == SlotStatus.AVAILABLE) {
            selectedSlot.setStatus(SlotStatus.IN_PROGRESS);
            meetingRepository.save(meeting);
        } else {
            throw new IllegalStateException("Этот слот уже занят или в процессе бронирования");
        }
    }

    public void confirmBooking(Meeting meeting) {
        TimeSlot selectedSlot = meeting.getSlot();

        if (selectedSlot == null) {
            throw new IllegalArgumentException("Слот не может быть null ");
        }

        if (selectedSlot.getStatus() == SlotStatus.IN_PROGRESS) {
            selectedSlot.setStatus(SlotStatus.BOOKED);
            meetingRepository.save(meeting);
        } else {
            throw new IllegalStateException("Слот не в процессе бронирования, не может быть подтвержден");
        }
    }

    public void cancelMeeting(Long chatId) {
        Meeting meeting = meetingRepository.findByChatId(chatId);

        if (meeting == null) {
            sendTextMessage(chatId, "Встреча не найдена.");
            return;
        }

        if (meeting.getSlot() != null && meeting.getSlotStatus() == SlotStatus.BOOKED) {
            meeting.getSlot().setStatus(SlotStatus.AVAILABLE);
            log.info("Слот {} освобожден.", meeting.getSlot().getDisplayName());
        } else {
            log.warn("Попытка отменить встречу, но слот либо не задан, либо не был забронирован");
        }
        meetingRepository.delete(meeting);
        log.info("Встреча с id {} отменена.", meeting.getId());
        sendTextMessage(chatId, "Встреча отменена и слот времени освобожден.");
    }
}