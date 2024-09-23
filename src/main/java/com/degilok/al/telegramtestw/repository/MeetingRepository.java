package com.degilok.al.telegramtestw.repository;

import com.degilok.al.telegramtestw.models.Meeting;
import com.degilok.al.telegramtestw.models.enums.TimeSlot;
import com.degilok.al.telegramtestw.models.enums.UserState;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    //List<Meeting> findByChatIdAndSlot(Long chatId, TimeSlot slot);


    //отображать занятые слоты
    @Query(value = "select * from meetings where date = :date", nativeQuery = true)
    List<Meeting> findTimeSlotsByDate(LocalDate date);


    @Query(value = "select * from meetings where chat_id = :chatId order by id desc limit 1 ", nativeQuery = true)
    Meeting findByChatId(Long chatId);

    @Modifying
    @Transactional
    @Query(value = "update meetings set user_session = :newUserSession, topic = :topic where chat_id = :chatId and user_session = :oldUserSession", nativeQuery = true)
    void updateTopic(String newUserSession, String topic, Long chatId, String oldUserSession);

    @Modifying
    @Transactional
    @Query(value = "update meetings set user_session = :newUserSession, project = :project where chat_id = :chatId and user_session = :oldUserSession ", nativeQuery = true)
    void updateProject(String newUserSession, String project, Long chatId, String oldUserSession);

    @Modifying
    @Transactional
    @Query(value = "update meetings set user_session = :newUserSession, date = :date where chat_id = :chatId and user_session = :oldUserSession", nativeQuery = true)
    void updateDate(String newUserSession, LocalDate date, Long chatId, String oldUserSession);

    @Modifying
    @Transactional
    @Query(value = "update meetings set user_session = :newUserSession, slot = :slot where chat_id = :chatId and user_session = :oldUserSession", nativeQuery = true)
    void updateSlot(String newUserSession, Integer slot, Long chatId, String oldUserSession);

    @Modifying
    @Transactional
    @Query(value = "update meetings set user_session = :newUserSession, responsible_person = :responsiblePerson where chat_id = :chatId and user_session = :oldUserSession", nativeQuery = true)
    void updateResponsiblePerson(String newUserSession, String responsiblePerson, Long chatId, String oldUserSession);

    @Query(value = "select * from meetings m where m.slot = :slot and m.date = :date", nativeQuery = true)
    Meeting checkSlot(Integer slot, LocalDate date);
}