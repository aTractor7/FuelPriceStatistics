package com.example.FuelStatisticsBot.config;

import com.example.FuelStatisticsBot.bot.FuelStatisticsTelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.format.DateTimeFormatter;

@Configuration
@PropertySource("application.properties")
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FuelStatisticsTelegramBot fuelStatisticsTelegramBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(fuelStatisticsTelegramBot);
        return api;
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter(@Value("${date.format}") String format) {
        return DateTimeFormatter.ofPattern(format);
    }
}
