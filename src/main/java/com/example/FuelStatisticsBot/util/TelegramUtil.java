package com.example.FuelStatisticsBot.util;

import com.example.FuelStatisticsBot.model.User;
import org.apache.commons.math3.util.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TelegramUtil {

    public static SendMessage createMessageTemplate(User user) {
        return createMessageTemplate(String.valueOf(user.getChatId()));
    }

    public static SendMessage createMessageTemplate(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        return sendMessage;
    }

    public static SendDocument createDocumentTemplate(User user) {
        return createDocumentTemplate(String.valueOf(user.getChatId()));
    }

    public static SendDocument createDocumentTemplate(String chatId) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        return sendDocument;
    }


    public static InlineKeyboardButton createInlineKeyBoardButton(String text, String command) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setCallbackData(command);
        return inlineKeyboardButton;
    }

    public static InlineKeyboardMarkup createOneRowSizeKeyboardMarkup(List<Pair<String, String>> buttonList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> rowI = new ArrayList<>();


        for (Pair<String, String> pair : buttonList) {
            rowI.add(createInlineKeyBoardButton(pair.getKey(), pair.getValue()));
        }
        rows.add(rowI);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
