package com.example.FuelStatisticsBot.handler;

import com.example.FuelStatisticsBot.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;

public interface TextHandler extends Handler{
    List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message);
}
