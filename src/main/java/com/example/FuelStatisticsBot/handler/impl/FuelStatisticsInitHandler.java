package com.example.FuelStatisticsBot.handler.impl;


import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.createMessageTemplate;

@Component
public class FuelStatisticsInitHandler implements Handler {

    private final UserService userService;

    @Autowired
    public FuelStatisticsInitHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage enterFirstDateMessage = createMessageTemplate(user);
        enterFirstDateMessage.setText("Введіть першу дату в такому форматі: \"дд.мм.рррр\"");

        user.setState(State.ENTER_DATE);
        userService.update(user.getChatId(), user);

        return List.of(enterFirstDateMessage);
    }

    @Override
    public State operatedState() {
        return State.FUEL_STATISTICS_INIT;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return null;
    }
}
