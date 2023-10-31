package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.StatisticsData;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.FuelStatisticsService;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.example.FuelStatisticsBot.util.DatesValidator.validateDates;
import static com.example.FuelStatisticsBot.util.TelegramUtil.*;

@Component
public class FuelStatisticsHandler implements Handler {

    private static final String ACCEPT_DATES = "/accept_dates";
    private static final String CANSEL_DATES = "/cancel_dates";

    private final DateTimeFormatter dateTimeFormatter;
    private final FuelStatisticsService fuelStatisticsService;


    @Autowired
    public FuelStatisticsHandler(DateTimeFormatter dateTimeFormatter, FuelStatisticsService fuelStatisticsService) {
        this.dateTimeFormatter = dateTimeFormatter;
        this.fuelStatisticsService = fuelStatisticsService;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        if(message.equals(ACCEPT_DATES)) {
            return getStatistics(user);
        } else if (message.equals(CANSEL_DATES)) {
            return canselDates(user);
        }

        return checkDate(user, message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> getStatistics(User user) {
        if(user.getStatisticsData() == null) return Collections.emptyList();
        StatisticsData statisticsData = user.getStatisticsData();

        if(statisticsData.getStartDate() == null || statisticsData.getEndDate() == null) return Collections.emptyList();

        File fuelStatisticsFile = fuelStatisticsService.getStatisticsInDocsFile(
                statisticsData.getStartDate(), statisticsData.getEndDate(),
                List.of(FuelType.A95_PLUS, FuelType.A95, FuelType.A92, FuelType.DT, FuelType.GAS));

        SendDocument fuelStatisticsDocument = createDocumentTemplate(user);
        fuelStatisticsDocument.setDocument(new InputFile(fuelStatisticsFile));

        user.setStatisticsData(null);
        user.setState(State.NONE);

        return List.of(fuelStatisticsDocument);
    }

    private List<PartialBotApiMethod<? extends Serializable>> canselDates(User user) {
        user.setStatisticsData(null);

        SendMessage canselMessage = createMessageTemplate(user);
        canselMessage.setText("Введені вами дати були стерті\nЩоб ввести заново використовуйте /get\\_statistics");

        user.setState(State.NONE);

        return List.of(canselMessage);
    }

    private List<PartialBotApiMethod<? extends Serializable>> checkDate(User user, String message) {

        SendMessage sendMessage = createMessageTemplate(user);

        try {
            LocalDate date = parseStringToDateAndValidate(message);

            StatisticsData statisticsData = user.getStatisticsData();
            if(statisticsData == null){
                statisticsData = new StatisticsData();
                statisticsData.setStartDate(date);
                user.setStatisticsData(statisticsData);
                sendMessage.setText("Введіть другу дату");
            }
            else {
                statisticsData.setEndDate(date);
                validateDates(statisticsData.getStartDate(), statisticsData.getEndDate());

                sendMessage.setText(String.format("Починаємо збір інформації за цими датами?\n %s - %s",
                        statisticsData.getStartDate().format(dateTimeFormatter),
                        statisticsData.getEndDate().format(dateTimeFormatter)));
                sendMessage.setReplyMarkup(
                        createOneRowSizeKeyboardMarkup(List.of(
                                new Pair<> ("Почати", ACCEPT_DATES),
                                new Pair<> ("Відмінити", CANSEL_DATES))));
            }

        }catch (DateTimeParseException | IllegalArgumentException e) {
            sendMessage.setText(e.getMessage() + "\nВведіть заново.");
            sendMessage.setReplyMarkup(
                    createOneRowSizeKeyboardMarkup(List.of(
                            new Pair<>("Відміна", CANSEL_DATES))));
        }
        return List.of(sendMessage);
    }


    private InlineKeyboardMarkup createOneRowSizeKeyboardMarkup(List<Pair<String, String>> buttonList) {
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

    private LocalDate parseStringToDateAndValidate(String text) {
        try{
            LocalDate date = LocalDate.parse(text, dateTimeFormatter);
            if(date.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("Дата не може бути в майбутьньому.");

            return date;
        }catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Формат введення дати хибний.");
        }
    }

    @Override
    public State operatedState() {
        return State.ENTER_DATE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(ACCEPT_DATES, CANSEL_DATES);
    }
}
