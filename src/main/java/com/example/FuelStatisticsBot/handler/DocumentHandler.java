package com.example.FuelStatisticsBot.handler;

import com.example.FuelStatisticsBot.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.Serializable;
import java.util.List;

public interface DocumentHandler extends Handler{

    List<PartialBotApiMethod<? extends Serializable>> handle(User user, Document document);
}
