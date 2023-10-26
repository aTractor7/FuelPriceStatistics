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
    private final List<BotCommand> commandList;

    @Autowired
    public FuelStatisticsTelegramBot(@Value("${bot.token}") String token,
                                     UpdateReceiver updateReceiver) {
        super(token);

        this.updateReceiver = updateReceiver;

        commandList = List.of(new BotCommand("/get_statistics", "файл з статистикою"),
                new BotCommand("/help", "туторіал по командам"));
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
    private void executeCommandList() {
        try {
            SetMyCommands myCommands =
                    new SetMyCommands(List.of(new BotCommand(), new BotCommand()), new BotCommandScopeDefault(), null);
            execute(myCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Command list execute error");
        }
    }
//    }
//    private final Logger LOG = LoggerFactory.getLogger(FuelStatisticsTelegramBot.class);
//
//    private static final String START = "/start";
//    private static final String FUEL_STATISTICS = "/get_statistics";
//    private static final String HELP = "/help";
//
//    private final List<BotCommand> commandList;
//
//    private static final String HELP_MESSAGE = """
//            Для отримання файлу з статистикою використовуйте команду:
//            /get_statistics
//            Після неї потрібно ввести дві дати через пробіл.
//            У такому форматі дд.мм.рррр
//
//            Для переліку команд використовуйте:
//            /help
//            """;
//
//    private final String botUsername;
//
//    private final FuelStatisticsService fuelStatisticsService;
//    private final DateValidator dateValidator;
//    private final DateTimeFormatter dateTimeFormatter;
//
//    @Autowired
//    public FuelStatisticsTelegramBot(@Value("${bot.token}") String token,
//                                     @Value("${bot.name}")String botUsername,
//                                     FuelStatisticsService fuelStatisticsService, DateValidator dateValidator,
//                                     DateTimeFormatter dateTimeFormatter) {
//        super(token);
//
//        this.botUsername = botUsername;
//        this.fuelStatisticsService = fuelStatisticsService;
//        this.dateValidator = dateValidator;
//        this.dateTimeFormatter = dateTimeFormatter;
//
//
//        commandList = List.of(new BotCommand(START, "привітання"),
//                new BotCommand(FUEL_STATISTICS, "файл з статистикою"),
//                new BotCommand(HELP, "туторіал по командам"));
//
//        executeCommandList();
//    }
//
//    private void executeCommandList() {
//        try {
//            SetMyCommands myCommands =
//                    new SetMyCommands(commandList, new BotCommandScopeDefault(), null);
//            execute(myCommands);
//        } catch (TelegramApiException e) {
//            LOG.error("Command list execute error", e);
//        }
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        if(!update.hasMessage() && !update.getMessage().hasText()) return;
//
//        String message = update.getMessage().getText();
//        long chatId = update.getMessage().getChatId();
//
//        if(message.equals(START)) startCommand(chatId, update.getMessage().getFrom().getUserName());
//        else if(message.equals(HELP)) helpCommand(chatId);
//        else if(message.startsWith(FUEL_STATISTICS)) {
//            if(message.length() < 16) {
//                sendMessage(chatId, "Вкажіть дати. Щоб дазнатись детальніше використовуйте команду /help");
//            }else{
//                fuelStatistics(chatId, message.substring(16));
//            }
//        }
//        else unknownCommand(chatId);
//
//    }
//
//    @Override
//    public String getBotUsername() {
//        return botUsername;
//    }
//
//    private void sendMessage(long chatId, String text) {
//        String chatIdStr = String.valueOf(chatId);
//        SendMessage sendMessage = new SendMessage(chatIdStr, text);
//        try{
//            execute(sendMessage);
//        }catch (TelegramApiException e) {
//
//        }
//    }
//
//    private void sendFile(long chatId, InputFile file) {
//        String chatIdStr = String.valueOf(chatId);
//        SendDocument sendDocument = new SendDocument(chatIdStr, file);
//        try{
//            execute(sendDocument);
//        }catch (TelegramApiException e) {
//            LOG.error("Document send error", e);
//        }
//    }
//
//
//    private void startCommand(long chatId, String username) {
//        String text = """
//                Привіт %s!
//                Я %s.
//                Ось мій функціонал
//
//                """;
//        String formattedText = String.format(text, username, botUsername);
//        sendMessage(chatId, formattedText.concat(HELP_MESSAGE));
//    }
//
//    private void helpCommand(long chatId) {
//        sendMessage(chatId, HELP_MESSAGE);
//    }
//
//    private void fuelStatistics(long chatId, String message) {
//
//        String[] dates = message.split(" ");
//
//        try {
//            LocalDate start = LocalDate.parse(dates[0], dateTimeFormatter);
//            LocalDate end = LocalDate.parse(dates[1], dateTimeFormatter);
//            dateValidator.validateDates(start, end);
//
//            sendMessage(chatId, "Іде збір та форматуваня інформації...");
//
//            File fuelFile = fuelStatisticsService.fillStatisticsInDocsFile(start, end,
//                    List.of(FuelType.A95_PLUS, FuelType.A95, FuelType.A92, FuelType.DT, FuelType.GAS));
//
//            sendFile(chatId, new InputFile(fuelFile));
//
//        }catch (IllegalArgumentException e) {
//            sendMessage(chatId, e.getMessage());
//        }catch (DateTimeParseException e) {
//            sendMessage(chatId, "Такої дати не існує. Або вона введене в невірному форматі.\n" +
//                    "Щоб пеглянути формат вокличіть команду /help");
//        }
//    }
//
//    private void unknownCommand(long chatId) {
//        String message = "Доу! Не вдалося розпізнати команду. \nЩоб отримати список команд використайте /help";
//        sendMessage(chatId, message);
//    }


}
