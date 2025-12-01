package ru.alimovdev.datar.service;

public enum TimeZone {
    UTC_2 ("UTC+2  Калининград", 1),
    UTC_3 ("UTC+3  Москва", 0),
    UTC_4 ("UTC+4  Самара", -1),
    UTC_5 ("UTC+5  Екатеринбург", -2),
    UTC_6 ("UTC+6  Омск", -3),
    UTC_7 ("UTC+7  Новосибирск", -4),
    UTC_8 ("UTC+8  Иркутск", -5),
    UTC_9 ("UTC+9  Чита", -6),
    UTC_10 ("UTC+10  Хабаровск", -7),
    UTC_11 ("UTC+11  Магадан", -8),
    UTC_12 ("UTC+12  Камчатка", -9);

    public final String label;
    public final int timeShift;

    TimeZone(String label, int timeShift){
        this.label = label;
        this.timeShift = timeShift;
    }
}
