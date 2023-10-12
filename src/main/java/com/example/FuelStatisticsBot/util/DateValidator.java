package com.example.FuelStatisticsBot.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateValidator {

    public void validateDates(LocalDate start, LocalDate end) {
        if(start.equals(end))
            throw new IllegalArgumentException("Дати не можуть бути однаковими");

        if(start.isAfter(end))
            throw new IllegalArgumentException("Дата початку має бути раніше за кінцеву");

        if(start.isAfter(LocalDate.now()) || end.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Дата не може бути в майбутньому");
    }

}
