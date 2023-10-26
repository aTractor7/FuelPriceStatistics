package com.example.FuelStatisticsBot;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.impl.FuelStatisticsHandler;
import com.example.FuelStatisticsBot.handler.impl.FuelStatisticsInitHandler;
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
}
