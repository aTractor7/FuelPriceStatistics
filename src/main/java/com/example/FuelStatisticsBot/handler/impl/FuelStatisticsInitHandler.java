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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> params = Arrays.stream(message.split(" "))
                .filter(s -> s.startsWith("-"))
                .toList();

        SendMessage responseMessage;

        if(params.contains("-f")) {
            updateState(user, State.SEND_FILE);
            responseMessage = createMessage(user, "Відправте файл з датами у форматі .docx");
        } else {
            updateState(user, State.ENTER_DATE);
            responseMessage = createMessage(user, "Введіть першу дату в такому форматі: \"дд.мм.рррр\"");
        }

        return List.of(responseMessage);
    }

    private void updateState(User user, State newState) {
        user.setState(newState);
        userService.update(user.getChatId(), user);
    }

    private SendMessage createMessage(User user, String text) {
        SendMessage sendMassage = createMessageTemplate(user);
        sendMassage.setText(text);
        return sendMassage;
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
