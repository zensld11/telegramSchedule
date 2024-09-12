package com.degilok.al.telegramtestw.service;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MessageService {

    void processTextMessage(String messageText, Long chatId);
    void sendTextMessage(Long chatId, String text);
    void processCalBackQuery(CallbackQuery callbackQuery);
    void saveMeeting(Long chatId, LocalDate date, LocalTime time, String selectedDate, String selectedTime, String topic);
    void sendDateOptions(Long chatId);
    void saveBookingDetails(Long chatId, String messageText);
}