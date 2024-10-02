package com.degilok.al.telegramtestw.models;

import com.degilok.al.telegramtestw.models.enums.SlotStatus;
import com.degilok.al.telegramtestw.models.enums.TimeSlot;
import com.degilok.al.telegramtestw.models.enums.UserState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long chatId;
    String responsiblePerson;
    String project;
    String topic;
    LocalDate date;

    @Column(name = "creation_time")
    LocalDateTime creationTime;


    @Enumerated(EnumType.ORDINAL)
    TimeSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_session")
    private UserState userSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_status")
    private SlotStatus slotStatus;

    @PrePersist
    protected void onCreate(){
        this.creationTime = LocalDateTime.now();
    }

    @Column(name = "notified")
    private boolean notified = false;
}