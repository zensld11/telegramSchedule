package com.degilok.al.telegramtestw.service;

import com.degilok.al.telegramtestw.models.Meeting;
import com.degilok.al.telegramtestw.models.enums.UserState;
import com.degilok.al.telegramtestw.repository.MeetingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CancelUserWhereCreationTimeFalseDbService {

    private final MeetingRepository meetingRepository;

    public CancelUserWhereCreationTimeFalseDbService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

//    @Scheduled(fixedRate = 30000)
//    public void notifyUserAboutMeetingEnd() {
//        LocalDateTime threeMinutesAgo = LocalDateTime.now().minus(3, ChronoUnit.MINUTES);
//
//        List<Meeting> expiredMeetings = meetingRepository.findByCreationTimeBeforeAndUserSessionNotFinished(threeMinutesAgo, UserState.FINISHED);
//
//        for (Meeting meeting : expiredMeetings) {
//            .notifyUser(meeting.getChatId(), "Ваша встреча будет удалена через 1 минуту, если вы ее не завершите");
//        }
//    }

//    @Scheduled(cron = "0 * * * * *")
//    public void deleteExpiredMeetings() {
//        LocalDateTime fourMinutesAgo = LocalDateTime.now().minus(2, ChronoUnit.MINUTES);
//
//        List<Meeting> meetingsToDelete = meetingRepository.findByCreationTimeBeforeAndUserSessionNotFinished(fourMinutesAgo, UserState.FINISHED);
//
//        if (!meetingsToDelete.isEmpty()) {
//            meetingRepository.deleteAll(meetingsToDelete);
//            System.out.println("Удалено " + meetingsToDelete.size() + " встреч");
//        }
//    }
}