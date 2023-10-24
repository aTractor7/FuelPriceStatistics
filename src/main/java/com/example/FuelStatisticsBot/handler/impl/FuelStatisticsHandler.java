package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

@Component
public class FuelStatisticsHandler implements Handler {
    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        return null;
    }

    @Override
    public State operatedState() {
        return State.ENTER_FIRST_DATE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return null;
    }
}
