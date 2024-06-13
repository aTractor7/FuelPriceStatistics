package com.example.FuelStatisticsBot.bot;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.*;

@Component
public class UpdateReceiver {

    private final UserService userService;

    private final List<Handler> handlerList;

    private final Map<String, State> messageToStateMap;

    @Autowired
    public UpdateReceiver(UserService userService, List<Handler> handlerList, Map<String, State> messageToStateMap) {
        this.userService = userService;
        this.handlerList = handlerList;
        this.messageToStateMap = messageToStateMap;
    }


    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        try{
            if(isMassageWithText(update)) {
                final Message message = update.getMessage();
                final long chatId = message.getChatId();
                final String name = message.getFrom().getFirstName();

                final User user = userService.findOne(chatId)
                        .orElseGet(() -> userService.save(new User(chatId, name, State.START)));

                if(user.getState().equals(State.NONE)) setStateByMessage(user, message);

                return getHandlerByState(user.getState()).handle(user, message.getText());
            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                final long chatId = callbackQuery.getFrom().getId();
                final String name = callbackQuery.getFrom().getUserName();

                final User user = userService.findOne(chatId)
                        .orElseGet(() -> userService.save(new User(chatId, name)));

                return getHandlerByCallBackQuery(callbackQuery.getData()).handle(user, callbackQuery.getData());
            }

            throw new UnsupportedOperationException();
        }catch (UnsupportedOperationException e){
            //TODO add log
            return Collections.emptyList();
        }
    }

    private Handler getHandlerByState(State state) throws UnsupportedOperationException{
        return handlerList.stream()
                .filter(h -> h.operatedState() != null)
                .filter(h -> h.operatedState().equals(state))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private Handler getHandlerByCallBackQuery(String queryData) throws UnsupportedOperationException{
        return handlerList.stream()
                .filter(h -> h.operatedCallBackQuery().stream()
                        .anyMatch(queryData::startsWith))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }


    private void setStateByMessage(User user, Message message) throws UnsupportedOperationException{
        String command = message.getText().split(" ")[0];

        if(messageToStateMap.containsKey(command))
            user.setState(messageToStateMap.get(command));
        else
            throw new UnsupportedOperationException();
    }

    private boolean isMassageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }


}
