package com.example.FuelStatisticsBot.model;

public class User {

    private long charId;

    private State state;

    public User(long charId, State state) {
        this.charId = charId;
        this.state = state;
    }

    public User(long charId) {
        this.charId = charId;
        state = State.NONE;
    }

    public User() {}

    public long getCharId() {
        return charId;
    }

    public void setCharId(long charId) {
        this.charId = charId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
