package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.models.Meeting;
import com.degilok.al.telegramtestw.models.enums.TimeSlot;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    private Meeting meeting;

    public void startNewMeeting(Long chatId) {
        meeting = new Meeting();
        meeting.setChatId(chatId);
    }

    public void addDate(LocalDate date) {
        meeting.setDate(date);
    }

    public void addSlot(TimeSlot slot) {
        meeting.setSlot(slot);
    }

    public void addResponsiblePerson(String responsiblePerson) {
        meeting.setResponsiblePerson(responsiblePerson);
    }

  /*  public void completeMeeting(UserSessionEntity userSession) {
        Meeting meeting = new Meeting();

        meeting.setChatId(userSession.getChatId());
        meeting.setDate(userSession.getDate());
        meeting.setSlot(userSession.getSlot());
        meeting.setProject(userSession.getProject());
        meeting.setTopic(userSession.getTopic());
        meeting.setResponsiblePerson(userSession.getResponsiblePerson());

        meeting.setUserSession(userSession);
       meetingRepository.save(meeting);
    }*/

    public void addProject(String project) {
        meeting.setProject(project);
    }

    public void addTopic(String topic) {
        meeting.setTopic(topic);
    }
}