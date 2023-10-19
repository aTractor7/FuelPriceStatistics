package com.example.FuelStatisticsBot.bot;

import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.service.FuelStatisticsService;
import com.example.FuelStatisticsBot.util.DateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Component
@PropertySource("application.properties")
public class FuelStatisticsTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(FuelStatisticsTelegramBot.class);

    public static final String START = "/start";
    public static final String FUEL_STATISTICS = "/get_statistics";
    public static final String HELP = "/help";

    public static final String HELP_MESSAGE = """
            Для отримання файлу з статистикою використовуйте команду:
            /get_statistics
            Після неї потрібно ввести дві дати через пробіл.
            У такому форматі дд.мм.рррр
            
            Для переліку команд використовуйте:
            /help
            """;

    public static final String REG_DATE_VALIDATOR = "\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}\\.\\d{2}\\.\\d{4}";

    private final String botUsername;
    @Value("${fuel.file.pass}")
    private String filePass;

    private final FuelStatisticsService fuelStatisticsService;
    private final DateValidator dateValidator;
    private final DateTimeFormatter dateTimeFormatter;

    @Autowired
    public FuelStatisticsTelegramBot(@Value("${bot.token}") String token,
                                     @Value("${bot.name}")String botUsername,
                                     FuelStatisticsService fuelStatisticsService, DateValidator dateValidator, DateTimeFormatter dateTimeFormatter) {
        super(token);

        this.botUsername = botUsername;
        this.fuelStatisticsService = fuelStatisticsService;
        this.dateValidator = dateValidator;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() && !update.getMessage().hasText()) return;

        String message = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if(message.equals(START)) startCommand(chatId, update.getMessage().getFrom().getUserName());
        else if(message.equals(HELP)) helpCommand(chatId);
        else if(message.startsWith(FUEL_STATISTICS)) {
            if(message.length() < 16) {
                sendMessage(chatId, "Вкажіть дати. Щоб дазнатись детальніше використовуйте команду /help");
            }else{
                fuelStatistics(chatId, message.substring(16));
            }
        }
        else unknownCommand(chatId);

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    private void sendMessage(long chatId, String text) {
        String chatIdStr = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdStr, text);
        try{
            execute(sendMessage);
        }catch (TelegramApiException e) {
            LOG.error("Massage send error", e);
        }
    }

    private void sendFile(long chatId, InputFile file) {
        String chatIdStr = String.valueOf(chatId);
        SendDocument sendDocument = new SendDocument(chatIdStr, file);
        try{
            execute(sendDocument);
        }catch (TelegramApiException e) {
            LOG.error("Document send error", e);
        }
    }


    private void startCommand(long chatId, String username) {
        String text = """
                Привіт %s!
                Я %s.
                Ось мій функціонал
                
                """;
        String formattedText = String.format(text, username, botUsername);
        sendMessage(chatId, formattedText.concat(HELP_MESSAGE));
    }

    private void helpCommand(long chatId) {
        sendMessage(chatId, HELP_MESSAGE);
    }

    private void fuelStatistics(long chatId, String message) {

        String[] dates = message.split(" ");

        try {
            LocalDate start = LocalDate.parse(dates[0], dateTimeFormatter);
            LocalDate end = LocalDate.parse(dates[1], dateTimeFormatter);
            dateValidator.validateDates(start, end);

            sendMessage(chatId, "Іде збір та форматуваня інформації...");

            fuelStatisticsService.fillStatisticsInDocsFile(start, end,
                    List.of(FuelType.A95_PLUS, FuelType.A95, FuelType.A92, FuelType.DT, FuelType.GAS));

            File fuelFile = new File(filePass);
            sendFile(chatId, new InputFile(fuelFile));

        }catch (IllegalArgumentException e) {
            sendMessage(chatId, e.getMessage());
        }catch (DateTimeParseException e) {
            sendMessage(chatId, "Такої дати не існує. Або вона введене в невірному форматі.\n" +
                    "Щоб пеглянути формат вокличіть команду /help");
        }



    }

    private void unknownCommand(long chatId) {
        String message = "Доу! Не вдалося розпізнати команду. \nЩоб отримати список команд використайте /help";
        sendMessage(chatId, message);
    }
}
