package com.degilok.al.telegramtestw.service.impl;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import com.degilok.al.telegramtestw.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class MeetingServiceImpl implements MeetingService {


    private final MeetingRepository meetingRepository;
    private final MessageServiceImpl messageServiceImpl;


    public MeetingServiceImpl(
            MeetingRepository meetingRepository,
            MessageServiceImpl messageServiceImpl) {
        this.meetingRepository = meetingRepository;
        this.messageServiceImpl = messageServiceImpl;
    }

    @Operation(summary = "мой метод, который проверяет доступно ли время для бронирования")
    @Override
    public boolean isTimeSlotAvailable(LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Meeting> meetings = meetingRepository.findByDateAndStartTimeBetween(date, startTime, endTime);
        return meetings.isEmpty();
    }


    @Operation(summary = "метод, который сохраняет встречу в бд, если время доступно")
    @Override
    public Meeting saveMeeting(Meeting meeting) {
        if (isTimeSlotAvailable(meeting.getDate(), meeting.getStartTime(), meeting.getEndTime())) {
            return meetingRepository.save(meeting);
        } else {
            throw new IllegalArgumentException("Время занято");
        }
    }

    @Operation(summary = "обработка нажатия на кнопку выбора времени")
    public void processCallBackQuery(CallbackQuery callbackQuery) {
        String callBackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        if (callBackData.startsWith("Забронирован_")) {
            saveMeetingFromCallback(callBackData, chatId);
        }
    }

    @Operation(summary = "отрабатывает данные из CallBackData, время, идентификатор чата")
    private void saveMeetingFromCallback(String callBackData, Long chatId) {
        Meeting meeting = new Meeting();
        String timeSlot = callBackData.substring(12);

        LocalTime parsedTime;
        try {
            parsedTime = LocalTime.parse(timeSlot);
            meeting.setStartTime(parsedTime);
        } catch (DateTimeException e) {
            messageServiceImpl.sendTextMessage(chatId, "Неверный формат времени: " + timeSlot);
            return;
        }

        LocalDate selectedDate = LocalDate.now();
        meeting.setDate(selectedDate);

        meeting.setChatId(chatId);

        if (isTimeSlotAvailable(selectedDate, parsedTime, parsedTime.plusHours(1))) {
            meetingRepository.save(meeting);
            messageServiceImpl.sendTextMessage(chatId, "Встреча забронирована на " + parsedTime.toString());
        } else {
            messageServiceImpl.sendTextMessage(chatId, "Это время уже занято. Выберите другое.");
        }
    }
}