package com.degilok.al.telegramtestw.models.enums;

public enum TimeSlot {

    SLOT_08_09("08:00 - 09:00", SlotStatus.AVAILABLE),
    SLOT_09_10("09:00 - 10:00", SlotStatus.AVAILABLE),
    SLOT_10_11("10:00 - 11:00", SlotStatus.AVAILABLE),
    SLOT_11_12("11:00 - 12:00", SlotStatus.AVAILABLE),
    SLOT_12_13("12:00 - 13:00", SlotStatus.AVAILABLE),
    SLOT_13_14("13:00 - 14:00", SlotStatus.AVAILABLE),
    SLOT_14_15("14:00 - 15:00", SlotStatus.AVAILABLE),
    SLOT_15_16("15:00 - 16:00", SlotStatus.AVAILABLE),
    SLOT_16_17("16:00 - 17:00", SlotStatus.AVAILABLE),
    SLOT_17_18("17:00 - 18:00", SlotStatus.AVAILABLE);

    private final String displayName;
    private SlotStatus status;

    TimeSlot(String timePeriod, SlotStatus initialStatus) {
        this.displayName = timePeriod;
        this.status = initialStatus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
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