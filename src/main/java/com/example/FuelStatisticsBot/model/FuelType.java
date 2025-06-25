package com.example.FuelStatisticsBot.model;

import lombok.Getter;

@Getter
public enum FuelType {
    A95_PLUS("Бензин А-95 (покращеної якості)"), A95 ("Бензин А-95"), A92("Бензин А-92"),
    DT("Дизельне паливо"), DT_PLUS("Дизельне паливо (покращеної якості)"), GAS("Газ");

    private final String fullName;

    FuelType(String fullName) {
        this.fullName = fullName;
    }
}
