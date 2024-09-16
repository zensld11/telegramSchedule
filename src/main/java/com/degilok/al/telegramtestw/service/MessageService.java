package com.degilok.al.telegramtestw.service;

public interface MessageService {

    String processTextMessage(String messageText, Long chatId);

    void sendTextMessage(Long chatId, String text);
    void saveBookingDetails(Long chatId, String messageText);
}