package com.degilok.al.telegramtestw.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
public class AppWebHookConfig {

    private final BotConfig botConfigImpl;

    public AppWebHookConfig(BotConfig botConfigImpl) {
        this.botConfigImpl = botConfigImpl;
    }

    @Bean
    public SetWebhook setWebhook() {
        return SetWebhook.builder().url(botConfigImpl.getWebHookPath()).build();
    }
}