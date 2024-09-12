package com.degilok.al.telegramtestw.controller;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping("/Schedule")
    public ResponseEntity<String> scheduleMeeting(@RequestBody Meeting meeting){
        try {
            meetingService.saveMeeting(meeting);
            return ResponseEntity.ok("Встреча успешно забронирована");
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}