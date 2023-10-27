package com.example.FuelStatisticsBot.model;

import java.time.LocalDate;

public class User {

    private long chatId;

    private String name;

    private State state;

    private LocalDate startDate;

    private LocalDate endDate;

    public User(long chatId, String name, State state) {
        this.chatId = chatId;
        this.name = name;
        this.state = state;
    }

    public User(long chatId, String name) {
        this.chatId = chatId;
        this.name = name;
        state = State.NONE;
    }

    public User() {}

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
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
}
