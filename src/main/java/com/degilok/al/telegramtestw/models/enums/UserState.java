package com.degilok.al.telegramtestw.models.enums;

public enum UserState {
    AWAITING_TOPIC("ожидание записи темы"),
    AWAITING_PROJECT("ожидание записи проекта"),
    AWAITING_DATE("ожидание записи даты"),
    AWAITING_TIME("ожидание записи времени"),
    AWAITING_NAME("ожидание записи имени ответственного"),
    FINISHED("встреча забронирована");

    private final String state;

    UserState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}