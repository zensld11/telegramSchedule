package com.degilok.al.telegramtestw.controller;

import com.degilok.al.telegramtestw.service.BotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class MeetingController {


    private final BotService botService;

    public MeetingController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/webhook")/*"/webhook"*/
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return botService.onWebhookUpdateReceived(update);
    }
}