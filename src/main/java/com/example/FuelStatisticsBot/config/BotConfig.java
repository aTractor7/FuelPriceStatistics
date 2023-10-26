package com.example.FuelStatisticsBot.config;

import com.example.FuelStatisticsBot.bot.FuelStatisticsTelegramBot;
import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.impl.FuelStatisticsHandler;
import com.example.FuelStatisticsBot.handler.impl.FuelStatisticsInitHandler;
import com.example.FuelStatisticsBot.handler.impl.HelpHandler;
import com.example.FuelStatisticsBot.handler.impl.StartHandler;
import com.example.FuelStatisticsBot.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("application.properties")
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FuelStatisticsTelegramBot fuelStatisticsTelegramBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(fuelStatisticsTelegramBot);
        return api;
    }
}
