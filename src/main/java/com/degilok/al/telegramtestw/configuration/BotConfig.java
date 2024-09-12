package com.degilok.al.telegramtestw.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BotConfig {

    @Value(value = "${telegram.bot.username}")
    private String botUserName;

    @Value(value = "${telegram.bot.token}")
    private String botToken;

    @Value(value = "${telegram.bot.webhook-path}")
    private String webHookPath;
}