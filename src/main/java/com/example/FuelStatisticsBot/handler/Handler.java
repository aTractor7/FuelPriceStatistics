package com.example.FuelStatisticsBot.handler;

import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.Serializable;
import java.util.List;

public interface Handler {

    State operatedState();

    List<String> operatedCallBackQuery();
}
