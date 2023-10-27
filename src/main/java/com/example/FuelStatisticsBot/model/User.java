package com.example.FuelStatisticsBot.model;

import java.time.LocalDate;

public class User {

    private long chatId;
    private String name;
    private State state;

    private StatisticsData statisticsData;

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

    public StatisticsData getStatisticsData() {
        return statisticsData;
    }

    public void setStatisticsData(StatisticsData statisticsData) {
        this.statisticsData = statisticsData;
    }
}
