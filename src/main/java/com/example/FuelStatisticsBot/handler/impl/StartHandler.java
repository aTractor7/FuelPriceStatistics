package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.createMessageTemplate;

@Component
@PropertySource("application.properties")
public class StartHandler implements Handler {

    @Value("${bot.name}")
    private String botName;

    private final UserService userService;

    private final static String START_TEXT = """
                                              Привіт %s!
                                              Я %s.
                                              """;
    private final static String HELP_MESSAGE = "Для отримання списку команд викличи /help";

    @Autowired
    public StartHandler(UserService userService) {
        this.userService = userService;
    }


    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage welcomeMessage = createMessageTemplate(user);
        welcomeMessage.setText(String.format(START_TEXT, user.getName(), botName));

        SendMessage helpMessage = createMessageTemplate(user);
        helpMessage.setText(HELP_MESSAGE);

        user.setState(State.NONE);
        userService.update(user.getChatId(), user);

        return List.of(welcomeMessage, helpMessage);
    }

    @Override
    public State operatedState() {
        return State.START;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}
