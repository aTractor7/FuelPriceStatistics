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
import java.util.Collections;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.createMessageTemplate;

@Component
public class HelpHandler implements Handler {

    private final UserService userService;

    private final static String HELP_MESSAGE = """
            Для отримання файлу з статистикою використовуйте команду:
            /get\\_statistics

            Для переліку команд використовуйте:
            /help
            """;

    @Autowired
    public HelpHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage helpMessage = createMessageTemplate(user);
        helpMessage.setText(HELP_MESSAGE);

        user.setState(State.NONE);
        userService.update(user.getChatId(), user);

        return List.of(helpMessage);
    }

    @Override
    public State operatedState() {
        return State.HELP;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
