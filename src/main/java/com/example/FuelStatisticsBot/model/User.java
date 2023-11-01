package com.example.FuelStatisticsBot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "id")
    private long chatId;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    private State state;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
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

    /**
     * This method also set owner to statistics data you set
     * */
    public void setStatisticsData(StatisticsData statisticsData) {
        statisticsData.setOwner(this);
        this.statisticsData = statisticsData;
    }
}
