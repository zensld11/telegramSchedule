package com.degilok.al.telegramtestw.models.enums;

public enum TimeSlot {

    SLOT_08_09("08:00 - 09:00"),
    SLOT_09_10("09:00 - 10:00"),
    SLOT_10_11("10:00 - 11:00"),
    SLOT_11_12("11:00 - 12:00"),
    SLOT_12_13("12:00 - 13:00"),
    SLOT_13_14("13:00 - 14:00"),
    SLOT_14_15("14:00 - 15:00"),
    SLOT_15_16("15:00 - 16:00"),
    SLOT_16_17("16:00 - 17:00"),
    SLOT_17_18("17:00 - 18:00");

    private final String displayName;

    TimeSlot(String timePeriod) {
        this.displayName = timePeriod;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TimeSlot findByIndex(int index) {
        TimeSlot[] slots = TimeSlot.values();
        if (index >= 0 && index < slots.length) {
            return slots[index];
        } else {
            throw new IllegalArgumentException("Нет слота с таким индексом: " + index);
        }
    }
}