package com.example.FuelStatisticsBot.config;

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
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BusinessConfig {

    @Bean
    @Autowired
    public List<Handler> handlerList(StartHandler start, HelpHandler help, FuelStatisticsHandler fuel,
                                     FuelStatisticsInitHandler fuelInit) {
        return List.of(start, help, fuel, fuelInit);
    }

    @Bean
    public Map<String, State> messageToStateMap() {
        Map<String, State> messageToStateMap = new HashMap<>();

        messageToStateMap.put("/start", State.START);
        messageToStateMap.put("/help", State.HELP);
        messageToStateMap.put("/getStatistics", State.FUEL_STATISTICS_INIT);

        return messageToStateMap;
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter(@Value("${date.format}") String format) {
        return DateTimeFormatter.ofPattern(format);
    }
}
