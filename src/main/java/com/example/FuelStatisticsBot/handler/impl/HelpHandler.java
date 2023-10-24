package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.createMessageTemplate;

@Component
public class HelpHandler implements Handler {

    private final static String HELP_MESSAGE = """
            Для отримання файлу з статистикою використовуйте команду:
            /getStatistics

            Для переліку команд використовуйте:
            /help
            """;
    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage helpMessage = createMessageTemplate(user);
        helpMessage.setText(HELP_MESSAGE);

        user.setState(State.NONE);

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
