package com.example.FuelStatisticsBot.bot;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.impl.StartHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


@Component
@PropertySource("application.properties")
public class FuelStatisticsTelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    private final UpdateReceiver updateReceiver;
//    private final List<BotCommand> commandList;

    @Autowired
    public FuelStatisticsTelegramBot(@Value("${bot.token}") String token,
                                     UpdateReceiver updateReceiver) {
        super(token);

        this.updateReceiver = updateReceiver;

//        commandList = List.of(new BotCommand("/get_statistics", "файл з статистикою"),
//                new BotCommand("/help", "туторіал по командам"));
//        executeCommandList();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<PartialBotApiMethod<? extends Serializable>> messageToSend = updateReceiver.handle(update);

        if(messageToSend != null && !messageToSend.isEmpty()) {
            messageToSend.forEach(response -> {
                if(response instanceof SendMessage) {
                    executeWithExceptionCheck((SendMessage) response);
                }
                else if(response instanceof SendDocument) {
                    executeFileWithExceptionCheck((SendDocument) response);
                }
            });
        }
    }

    private void executeWithExceptionCheck(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        }catch (TelegramApiException e) {
            throw new RuntimeException("Message execute error");
        }
    }

    private void executeFileWithExceptionCheck(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        }catch (TelegramApiException e) {
            throw new RuntimeException("File execute error");
        }
    }


    //TODO find why this method always throw exception
//    private void executeCommandList() {
//        try {
//            SetMyCommands myCommands =
//                    new SetMyCommands(commandList, new BotCommandScopeDefault(), null);
//            execute(myCommands);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException("Command list execute error");
//        }
//    }
}
