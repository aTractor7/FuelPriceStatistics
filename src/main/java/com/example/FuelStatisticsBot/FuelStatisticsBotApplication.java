package com.example.FuelStatisticsBot;

import com.example.FuelStatisticsBot.bot.FuelStatisticsTelegramBot;
import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.impl.FuelStatisticsHandler;
import com.example.FuelStatisticsBot.handler.impl.HelpHandler;
import com.example.FuelStatisticsBot.handler.impl.StartHandler;
import com.example.FuelStatisticsBot.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class FuelStatisticsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FuelStatisticsBotApplication.class, args);
	}

	@Bean
	@Autowired
	public List<Handler> handlerList(StartHandler start, HelpHandler help, FuelStatisticsHandler fuel) {
		return List.of(start, help, fuel);
	}

	@Bean
	public Map<String, State> messageToStateMap() {
		Map<String, State> messageToStateMap = new HashMap<>();

		messageToStateMap.put("/start", State.START);
		messageToStateMap.put("/help", State.HELP);
		messageToStateMap.put("/getStatistics", State.ENTER_FIRST_DATE);

		return messageToStateMap;
	}
}
