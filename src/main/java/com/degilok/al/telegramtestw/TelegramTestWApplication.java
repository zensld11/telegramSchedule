package com.degilok.al.telegramtestw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@SpringBootApplication
@EnableScheduling
public class TelegramTestWApplication {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        SpringApplication.run(TelegramTestWApplication.class, args);
    }
}
