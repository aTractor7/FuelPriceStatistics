package com.example.FuelStatisticsBot;

import com.example.FuelStatisticsBot.handler.Handler;
import com.example.FuelStatisticsBot.handler.impl.StartHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class FuelStatisticsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FuelStatisticsBotApplication.class, args);
	}

	@Bean
	@Autowired
	public List<Handler> handlerList(StartHandler start) {
		return List.of(start);
	}
}
