package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.FuelStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
import java.util.Collections;
import java.util.List;

import static com.example.FuelStatisticsBot.util.DatesValidator.validateDates;
import static com.example.FuelStatisticsBot.util.TelegramUtil.*;

@Component
@Scope("prototype")
public class FuelStatisticsHandler implements Handler {

    private static final String ACCEPT_DATES = "/accept_dates";
    private static final String CANSEL_DATES = "/cancel_dates";


    private final DateTimeFormatter dateTimeFormatter;
    private final FuelStatisticsService fuelStatisticsService;

    private LocalDate start;
    private LocalDate end;

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
        if(start == null || end == null) return Collections.emptyList();

        File fuelStatisticsFile = fuelStatisticsService.getStatisticsInDocsFile(start, end,
                List.of(FuelType.A95_PLUS, FuelType.A95, FuelType.A92, FuelType.DT, FuelType.GAS));

        SendDocument fuelStatisticsDocument = createDocumentTemplate(user);
        fuelStatisticsDocument.setDocument(new InputFile(fuelStatisticsFile));

        return List.of(fuelStatisticsDocument);
    }

    private List<PartialBotApiMethod<? extends Serializable>> canselDates(User user) {
        start = null;
        end = null;

        SendMessage canselMessage = createMessageTemplate(user);
        canselMessage.setText("Введені вами дати були стерті\nЩоб ввести заново використовуйте /getStatistics");

        user.setState(State.NONE);

        return List.of(canselMessage);
    }

    private List<PartialBotApiMethod<? extends Serializable>> checkDate(User user, String message) {

        SendMessage sendMessage = createMessageTemplate(user);

        try {
            LocalDate date = parseStringToDateAndValidate(message);

            if(start == null){
                start = date;
                sendMessage.setText("Введіть другу дату");
            }
            else {
                end = date;
                validateDates(start, end);

                sendMessage.setText(String.format("Починаємо збір інформації за цими датами?\n %s - %s",
                        start.format(dateTimeFormatter), end.format(dateTimeFormatter)));
                sendMessage.setReplyMarkup(createKeyboardMarkupForCheckDates());
            }

        }catch (DateTimeParseException | IllegalArgumentException e) {
            sendMessage.setText(e.getMessage() + "\nВведіть заново");
        }
        return List.of(sendMessage);
    }

    private InlineKeyboardMarkup createKeyboardMarkupForCheckDates() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtons = List.of(
                createInlineKeyBoardButton("Почати", ACCEPT_DATES),
                createInlineKeyBoardButton("Відміна", CANSEL_DATES)
        );

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtons));

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
