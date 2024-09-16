package com.degilok.al.telegramtestw.repository;

import com.degilok.al.telegramtestw.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {


    List<Meeting> findByDate(LocalDate selectedDate);

    List<Meeting> findByDateAndStartTimeBetween(LocalDate date, LocalTime startTime, LocalTime endTime);

   // List<Meeting> findByStartTimeBetween(LocalTime start, LocalTime end);
}
//boolean existsByDateAndTimeFromAndTimeTo(LocalDate date, LocalTime timeFrom, LocalTime timeTo);