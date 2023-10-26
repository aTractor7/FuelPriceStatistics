package com.example.FuelStatisticsBot.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

public class DatesValidator {

    public static void validateDates(LocalDate start, LocalDate end) {
        if(start.equals(end))
            throw new IllegalArgumentException("Дати не можуть бути однаковими");

        if(start.isAfter(end))
            throw new IllegalArgumentException("Дата початку має бути раніше за кінцеву");

    }

}
