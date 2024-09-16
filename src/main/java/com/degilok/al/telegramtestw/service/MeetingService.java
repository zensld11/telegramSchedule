package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.entity.Meeting;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MeetingService {


    // void saveMeeting(Long chatId, LocalDate selectedDate, LocalTime selectedTime, String person, String project, String topic);

    boolean isTimeSlotAvailable(LocalDate date, LocalTime startTime, LocalTime endTime);

    Meeting saveMeeting(Meeting meeting);
}