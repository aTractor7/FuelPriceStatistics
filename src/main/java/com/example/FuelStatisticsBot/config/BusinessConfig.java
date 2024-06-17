package com.example.FuelStatisticsBot.config;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.TextHandler;
import com.example.FuelStatisticsBot.handler.impl.*;
import com.example.FuelStatisticsBot.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BusinessConfig {

    @Bean
    @Autowired
    public List<TextHandler> handlerList(StartHandler start, HelpHandler help, FuelStatisticsHandler fuel,
                                         FuelStatisticsInitHandler fuelInit) {
        return List.of(start, help, fuel, fuelInit);
    }

    @Bean
    public Map<String, State> messageToStateMap() {
        Map<String, State> messageToStateMap = new HashMap<>();

        messageToStateMap.put("/start", State.START);
        messageToStateMap.put("/help", State.HELP);
        messageToStateMap.put("/get_statistics", State.FUEL_STATISTICS_INIT);

        return messageToStateMap;
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter(@Value("${date.format}") String format) {
        return DateTimeFormatter.ofPattern(format);
    }
}
