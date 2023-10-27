package com.example.FuelStatisticsBot.model;

import java.time.LocalDate;
import java.util.List;

public class StatisticsData {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<FuelType> fuelTypes;

    public StatisticsData(LocalDate startDate, LocalDate endDate, List<FuelType> fuelTypes) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.fuelTypes = fuelTypes;
    }

    public StatisticsData(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public StatisticsData() {}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<FuelType> getFuelTypes() {
        return fuelTypes;
    }

    public void setFuelTypes(List<FuelType> fuelTypes) {
        this.fuelTypes = fuelTypes;
    }
}
