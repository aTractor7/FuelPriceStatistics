package com.example.FuelStatisticsBot.service;

import com.example.FuelStatisticsBot.client.FuelClient;
import com.example.FuelStatisticsBot.model.Fuel;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class FuelStatisticsService {

    private final FuelClient fuelClient;
    private final FuelStatisticsFileEditor fileEditor;
    @Autowired
    public FuelStatisticsService(FuelClient fuelClient, FuelStatisticsFileEditor fileEditor) {
        this.fuelClient = fuelClient;
        this.fileEditor = fileEditor;
    }

    public File getStatisticsInDocsFile(List<LocalDate> dates, List<FuelType> requiredFuel) throws IOException {
        Map<LocalDate, List<Fuel>> fuelDateMap = fuelClient.getFuelPriceData(dates);

        List<List<Double>> fuelsPercents = getFuelPercents(fuelDateMap, requiredFuel);

        return fileEditor.getFuelStatisticsFile(fuelDateMap, requiredFuel, fuelsPercents);
    }

    public File getStatisticsInDocsFile(LocalDate start, LocalDate end, List<FuelType> requiredFuel) throws IOException {
        Map<LocalDate, List<Fuel>> fuelDateMap = fuelClient.getFuelPriceData(start, end);

        trimDate(start, end, fuelDateMap);

        List<List<Double>> fuelsPercents = getFuelPercents(fuelDateMap, requiredFuel);

        return fileEditor.getFuelStatisticsFile(fuelDateMap, requiredFuel, fuelsPercents);
    }

    private List<List<Double>> getFuelPercents(Map<LocalDate, List<Fuel>> fuelDateMap, List<FuelType> requiredFuel) {
        List<List<Double>> fuelsPercents = new ArrayList<>();

        for (FuelType type: requiredFuel) {
            fuelsPercents.add(getGrowthStatisticsInPercent(fuelDateMap, type));
        }

        return fuelsPercents;
    }

    private List<Double> getGrowthStatisticsInPercent(Map<LocalDate, List<Fuel>> fuelDateMap, FuelType fuelType) {
        List<Fuel> fuelList = fuelDateMap.keySet().stream()
                .map(fuelDateMap::get)
                .flatMap(Collection::stream)
                .filter(f -> f.getFuelType() == fuelType).toList();

        double lastPrice = fuelList.get(fuelList.size() - 1).getPrice();
        List<Double> result = new ArrayList<>();

        for (Fuel fuel : fuelList) {
            double percent = lastPrice / fuel.getPrice() * 100 - 100;
            percent = Math.round(percent * 100) / 100.0;
            result.add(percent);
        }
        return result;
    }

    private void trimDate(LocalDate start, LocalDate end, Map<LocalDate, List<Fuel>> fuelDateMap) {
        fuelDateMap.entrySet().removeIf(e -> start.isAfter(e.getKey()) || end.isBefore(e.getKey()));
    }
}
