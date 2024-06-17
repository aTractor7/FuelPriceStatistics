package com.example.FuelStatisticsBot.handler.impl;

import com.example.FuelStatisticsBot.client.TelegramFileLoader;
import com.example.FuelStatisticsBot.handler.DocumentHandler;
import com.example.FuelStatisticsBot.model.State;
import com.example.FuelStatisticsBot.model.User;
import com.example.FuelStatisticsBot.service.DateParserFromFile;
import com.example.FuelStatisticsBot.service.UserService;
import com.example.FuelStatisticsBot.util.exception.WrongFileExtensionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import static com.example.FuelStatisticsBot.util.TelegramUtil.createMessageTemplate;

@Component
public class ImportDateFromDocxHandler implements DocumentHandler {

    private final UserService userService;
    private final TelegramFileLoader fileLoader;
    private final DateParserFromFile dateParserFromFile;
    private static final String FILE_EXTENSION = ".docx";

    @Autowired
    public ImportDateFromDocxHandler(UserService userService, TelegramFileLoader fileLoader, DateParserFromFile dateParserFromFile) {
        this.userService = userService;
        this.fileLoader = fileLoader;
        this.dateParserFromFile = dateParserFromFile;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, Document document) {
        try{
            checkFileExtension(document.getFileName());

            File file = fileLoader.loadFile(document.getFileId());
            List<LocalDate> dates = dateParserFromFile.getDatesFromFile(file);

            return null;
        }catch (WrongFileExtensionException e) {
            SendMessage exceptionMessage = createMessageTemplate(user);
            exceptionMessage.setText("Не правильний формат файлу. Використовуйте .docx");
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
        return null;
    }
}
