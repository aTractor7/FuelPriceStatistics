package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.client.TelegramFileLoader;
import com.example.FuelStatisticsBot.handler.DocumentHandler;
import com.example.FuelStatisticsBot.handler.TextHandler;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.DateParserFromFile;
import com.example.FuelStatisticsBot.service.FuelStatisticsService;
import com.example.FuelStatisticsBot.service.UserService;
import com.example.FuelStatisticsBot.util.exception.WrongFileExtensionException;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.*;

@Component
public class ImportDateFromDocxHandler implements DocumentHandler, TextHandler {

    private final UserService userService;
    private final TelegramFileLoader fileLoader;
    private final DateParserFromFile dateParserFromFile;
    private final FuelStatisticsService fuelStatisticsService;
    private static final String FILE_EXTENSION = ".docx";
    private static final String CANSEL_FILE_LOAD = "/cancel_file_load";

    @Autowired
    public ImportDateFromDocxHandler(UserService userService, TelegramFileLoader fileLoader,
                                     DateParserFromFile dateParserFromFile, FuelStatisticsService fuelStatisticsService) {
        this.userService = userService;
        this.fileLoader = fileLoader;
        this.dateParserFromFile = dateParserFromFile;
        this.fuelStatisticsService = fuelStatisticsService;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        SendMessage canselMessage = createMessageTemplate(user);
        canselMessage.setText("Завантаження файлу відмінено");

        user.setState(State.NONE);
        userService.update(user.getChatId(), user);

        return List.of(canselMessage);
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, Document document) {
        if(document == null) return Collections.emptyList();

        try{
            checkFileExtension(document.getFileName());

            File file = fileLoader.loadFile(document.getFileId());
            List<LocalDate> dates = dateParserFromFile.getDatesFromFile(file);

            File fuelStatisticsFile = fuelStatisticsService.getStatisticsInDocsFile(dates,
                    List.of(FuelType.A95_PLUS, FuelType.A95, FuelType.A92, FuelType.DT, FuelType.GAS));

            SendDocument fuelStatisticsDocument = createDocumentTemplate(user);
            fuelStatisticsDocument.setDocument(new InputFile(fuelStatisticsFile));

            return List.of(fuelStatisticsDocument);
        }catch (WrongFileExtensionException e) {
            SendMessage exceptionMessage = createMessageTemplate(user);
            exceptionMessage.setText("Не правильний формат файлу. Використовуйте .docx");
            exceptionMessage.setReplyMarkup(
                    createOneRowSizeKeyboardMarkup(List.of(
                            new Pair<>("Відмінити", CANSEL_FILE_LOAD))));
            return List.of(exceptionMessage);
        } catch (IOException e) {
            SendMessage exceptionMessage = createMessageTemplate(user);
            exceptionMessage.setText("Упс. У нас виникла проблема( Спробуйте пізніше.");
            return List.of(exceptionMessage);
        } finally {
            user.setState(State.NONE);
            userService.update(user.getChatId(), user);
        }
    }

    private void checkFileExtension(String fileName) {
        String extension = fileName.substring(fileName.length() - 5);
        if(!extension.equals(FILE_EXTENSION)) {
            throw new WrongFileExtensionException("Wrong file type. Use .docx");
        }
    }

    @Override
    public State operatedState() {
        return State.SEND_FILE;
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(CANSEL_FILE_LOAD);
    }
}
