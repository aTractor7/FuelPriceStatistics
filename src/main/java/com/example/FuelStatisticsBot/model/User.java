package com.example.FuelStatisticsBot.model;

import java.time.LocalDate;

public class User {

    private long chatId;

    private String userName;

    private State state;

    private LocalDate startDate;

    private LocalDate endDate;

    public User(long chatId, String userName, State state) {
        this.chatId = chatId;
        this.userName = userName;
        this.state = state;
    }

    public User(long chatId, String userName) {
        this.chatId = chatId;
        this.userName = userName;
        state = State.NONE;
    }

    public User() {}

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
