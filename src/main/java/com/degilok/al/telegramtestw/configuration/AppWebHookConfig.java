package com.degilok.al.telegramtestw.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Component
public class AppWebHookConfig {


    private final BotConfig botConfig;

    public AppWebHookConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public SetWebhook setWebhook() {
        return SetWebhook.builder().url(botConfig.getWebHookPath()).build();
    }
}