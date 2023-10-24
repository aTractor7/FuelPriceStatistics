package com.example.FuelStatisticsBot.model;

public class User {

    private long chatId;

    private String userName;

    private State state;

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
}
