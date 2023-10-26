package com.example.FuelStatisticsBot.bot;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UpdateReceiver {

    private final List<User> userList = new ArrayList<>();

    private final List<Handler> handlerList;

    private final Map<String, State> messageToStateMap;

    @Autowired
    public UpdateReceiver(List<Handler> handlerList, Map<String, State> messageToStateMap) {
        this.handlerList = handlerList;
        this.messageToStateMap = messageToStateMap;
    }


    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        try{
            if(isMassageWithText(update)) {
                final Message message = update.getMessage();
                final long chatId = message.getChatId();
                final String userName = message.getFrom().getUserName();

                final User user = userList.stream().filter(u -> u.getChatId() == chatId).findAny()
                        .orElseGet(() -> new User(chatId, userName, State.START));

                if(!userList.contains(user)) userList.add(user);

                if(user.getState().equals(State.NONE)) setStateByMessage(user, message);
                if(user.getState().equals(State.NONE)) setStateByMessage(user, message);

                return getHandlerByState(user.getState()).handle(user, message.getText());
            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                final long chatId = callbackQuery.getFrom().getId();
                final String userName = callbackQuery.getFrom().getUserName();
                final User user = userList.stream().filter(u -> u.getChatId() == chatId).findAny()
                        .orElseGet(() -> new User(chatId, userName));

                if(!userList.contains(user)) userList.add(user);

                return getHandlerByCallBackQuery(callbackQuery.getData()).handle(user, callbackQuery.getData());
            }

            throw new UnsupportedOperationException();
        }catch (UnsupportedOperationException e){
            //TODO return empty list
            throw new RuntimeException("unsupported command");
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
        if(messageToStateMap.containsKey(message.getText()))
            user.setState(messageToStateMap.get(message.getText()));
        else
            throw new UnsupportedOperationException();
    }

    private boolean isMassageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }


}
