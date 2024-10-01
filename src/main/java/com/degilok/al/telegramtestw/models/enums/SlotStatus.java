package com.degilok.al.telegramtestw.models.enums;

public enum SlotStatus {
    AVAILABLE("Доступен"),
    IN_PROGRESS("В процессе бронирования"),
    BOOKED("Забронирован");

    private final String status;

    SlotStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
