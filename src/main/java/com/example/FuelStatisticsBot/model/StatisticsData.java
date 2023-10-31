package com.example.FuelStatisticsBot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "statistics_data")
public class StatisticsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User owner;

    @Transient
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

    public boolean isEmpty() {
        return startDate == null && endDate == null;
    }

    public void clear() {
        startDate = null;
        endDate = null;
        fuelTypes = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
