package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.entity.Meeting;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public void saveMeeting(Long chatId, LocalDate selectedDate, LocalTime selectedTime, String person, String project, String topic) {
        Meeting meeting = new Meeting();

        meeting.setDate(selectedDate);
        meeting.setStartTime(selectedTime);
        meeting.setResponsiblePerson(person);
        meeting.setProject(project);
        meeting.setTopic(topic);
        meetingRepository.save(meeting);
    }


    public boolean isTimeSlotAvailable(LocalTime startTime, LocalTime endTime){
        List<Meeting> meetings = meetingRepository.findByStartTimeBetween(startTime, endTime);
        return meetings.isEmpty();
    }


    public Meeting saveMeeting(Meeting meeting){
        if (isTimeSlotAvailable(meeting.getStartTime(), meeting.getEndTime())){
            return meetingRepository.save(meeting);
        }else {
            throw new IllegalArgumentException("Время занято");
        }
    }


}