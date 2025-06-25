package com.example.FuelStatisticsBot.client;

import com.example.FuelStatisticsBot.model.Fuel;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.util.exception.ClientException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@PropertySource("application.properties")
public class FuelClient {

    @Value("${fuel.inf.url}")
    private String url;

    private final DateTimeFormatter dateTimeFormatter;

    @Autowired
    public FuelClient(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public Map<LocalDate, List<Fuel>> getFuelPriceData(List<LocalDate> dates) {
        LocalDate start = dates.get(0);
        LocalDate end = dates.get(dates.size() - 1);

        Map<LocalDate, List<Fuel>> fuelDatePriceMap = getFuelPriceData(start, end);

        return fuelDatePriceMap.entrySet().stream()
                .filter(entry -> dates.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<LocalDate, List<Fuel>> getFuelPriceData(LocalDate start, LocalDate end) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<CompletableFuture<Map<LocalDate, List<Fuel>>>> futures = new ArrayList<>();

        while (start.isBefore(end) || start.getMonth() == end.getMonth()) {
            LocalDate immutableStart = start;
            futures.add(CompletableFuture.supplyAsync(
                    () -> getFuelPriceDataPerMonths(immutableStart), executor));
            start = start.plusMonths(1);
        }

        Map<LocalDate, List<Fuel>> result = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        executor.shutdown();
        return result;
    }

    private Map<LocalDate, List<Fuel>> getFuelPriceDataPerMonths(LocalDate date) {
        try {
            Document document = Jsoup.connect(getUrlWithDate(date)).get();
            Elements rows = Objects.requireNonNull(document.selectFirst("table")).select("tr");

            return getFuelPriceMapPerMonths(rows);
        } catch (IOException e) {
            throw new ClientException("Exception due to connecting to url using getMethod: " + url, e);
        }
    }

    private String getUrlWithDate(LocalDate date) {
        StringBuilder dateBuilder = new StringBuilder();
        dateBuilder.append(date.getYear()).append("-");

        int months = date.getMonth().getValue();
        if (months < 10) dateBuilder.append("0");

        String dateStr = dateBuilder.append(months).toString();
        return String.format(url, dateStr);
    }

    private Map<LocalDate, List<Fuel>> getFuelPriceMapPerMonths(Elements rows) throws ClientException{
        Map<LocalDate, List<Fuel>> dateFuelMap = new LinkedHashMap<>();
        for(int i = 1; i < rows.size(); i ++) {
            Elements cells = rows.get(i).select("td");

            if (cells.isEmpty() || cells.get(0).text().isEmpty()) continue;

            LocalDate date = parseDate(cells.get(0).text());
            List<Fuel> fuelList = dateFuelMap.computeIfAbsent(date, k -> new ArrayList<>());

            for (int j = 1; j < cells.size(); j++) {
                String priceText = cells.get(j).text();
                if (priceText.isEmpty()) continue;

                FuelType fuelType = determineFuelType(j);
                fuelList.add(new Fuel(fuelType, parsePrice(priceText)));
            }
        }
        return dateFuelMap;
    }

    private int parsePrice(String cellPrice) {
        StringBuilder fuelPrice = new StringBuilder(cellPrice);
        fuelPrice.deleteCharAt(2);
        return Integer.parseInt(fuelPrice.toString());
    }

    private LocalDate parseDate(String cellDate) {
        String stringDate = cellDate.substring(0, 10);
        return LocalDate.parse(stringDate, dateTimeFormatter);
    }

    private FuelType determineFuelType(int index) {
        FuelType fuelType;

        switch (index) {
            case 1 -> fuelType = FuelType.A95_PLUS;
            case 2 -> fuelType = FuelType.A95;
            case 3 -> fuelType = FuelType.A92;
            case 4 -> fuelType = FuelType.DT;
            case 5 -> fuelType = FuelType.DT_PLUS;
            case 6 -> fuelType = FuelType.GAS;
            default -> throw
                    new ClientException("Exception with fuelType determination. FuelType index: " + index);
        }
        return fuelType;
    }
}