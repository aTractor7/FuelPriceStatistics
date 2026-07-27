package com.example.FuelStatisticsBot.client;

import com.example.FuelStatisticsBot.model.Fuel;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.util.exception.ClientException;
import org.apache.commons.collections4.map.LinkedMap;
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
import java.util.function.BiConsumer;
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

    public Map<LocalDate, List<Fuel>> getFuelPriceData(Map<FuelType, List<LocalDate>> fuelDateMap) {
        Map<LocalDate, List<Fuel>> fuelDatePriceMap;

        LocalDate[] startEndDates = getStartEndDates(fuelDateMap);;

        fuelDatePriceMap = getFuelPriceDataInternal(startEndDates[0], startEndDates[1],
                (cells, fuelList) -> {
                    for (FuelType fuelType : fuelDateMap.keySet()) {
                        if(cells.get(fuelType.ordinal() + 1) != null && !cells.get(fuelType.ordinal() + 1).text().isBlank()) {
                            String priceText = cells.get(fuelType.ordinal() + 1).text();
                            fuelList.add(new Fuel(fuelType, parsePrice(priceText)));
                        }
                    }
                });

        return removeUnnecessaryDates(fuelDateMap, fuelDatePriceMap);
    }

    public Map<LocalDate, List<Fuel>> getFuelPriceData(LocalDate start, LocalDate end) {
        return getFuelPriceDataInternal(start, end, (cells, fuelList) -> {
            for (int j = 1; j < cells.size(); j++) {
                if (cells.get(j) == null || cells.get(j).text().isEmpty()) continue;
                String priceText = cells.get(j).text();

                if(FuelType.values().length <= j - 1)
                    throw new ClientException("Exception with fuelType determination. FuelType index: " + j);

                FuelType fuelType = FuelType.values()[j - 1];
                fuelList.add(new Fuel(fuelType, parsePrice(priceText)));
            }
        });
    }

    private LocalDate[] getStartEndDates(Map<FuelType, List<LocalDate>> fuelDateMap) {
        LocalDate start = null;
        LocalDate end = null;
        for(FuelType key : fuelDateMap.keySet()) {
            List<LocalDate> dates = fuelDateMap.get(key);
            if(dates == null || dates.isEmpty()) continue;

            if(start == null && end == null) {
                start = dates.get(0);
                end = dates.get(dates.size() - 1);
            }
            if(dates.get(0).isBefore(start)) {
                start = dates.get(0);
            }
            if(dates.get(dates.size() - 1).isAfter(end)) {
                end = dates.get(dates.size() - 1);
            }
        }

        return new LocalDate[]{start, end};
    }

    private Map<LocalDate, List<Fuel>> getFuelPriceDataInternal(LocalDate start, LocalDate end, BiConsumer<Elements, List<Fuel>> fuelProcessor) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<CompletableFuture<Map<LocalDate, List<Fuel>>>> futures = new ArrayList<>();

        while (start.isBefore(end) || start.getMonth() == end.getMonth()) {
            LocalDate immutableStart = start;
            futures.add(CompletableFuture.supplyAsync(
                    () -> getFuelPriceDataPerMonths(immutableStart, fuelProcessor), executor));
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

    private Map<LocalDate, List<Fuel>> removeUnnecessaryDates(Map<FuelType, List<LocalDate>> fuelDateMap,
                                                              Map<LocalDate, List<Fuel>> fuelDatePriceMap) {
        addEmptyDates(fuelDateMap, fuelDatePriceMap);

        TreeMap<LocalDate, List<Fuel>> sortedFuelDatePriceMap = new TreeMap<>(fuelDatePriceMap);
        removeUnusedDates(fuelDateMap, sortedFuelDatePriceMap);


        for (FuelType fuelType : fuelDateMap.keySet()) {
            List<LocalDate> requiredDates = fuelDateMap.get(fuelType);

            for (LocalDate key : sortedFuelDatePriceMap.keySet()) {
                LocalDate previousKey = sortedFuelDatePriceMap.lowerKey(key);
                if(requiredDates.contains(key) || previousKey != null && sortedFuelDatePriceMap.get(previousKey).stream()
                        .anyMatch(fuel -> fuel.getPrice() == -1 && fuel.getFuelType() == fuelType)) continue;

                fuelDatePriceMap.get(key).removeIf(fuel -> fuel.getFuelType() == fuelType);
            }
        }
        return sortedFuelDatePriceMap;
    }

    private void removeUnusedDates(Map<FuelType, List<LocalDate>> fuelDateMap,
                                   TreeMap<LocalDate, List<Fuel>> fuelDatePriceMap) {
        Set<LocalDate> allDates = fuelDateMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Set<LocalDate> toRemove = new HashSet<>();
        for(LocalDate date : fuelDatePriceMap.keySet()) {
            if(!allDates.contains(date)) {
                LocalDate previousKey = fuelDatePriceMap.lowerKey(date);
                if(previousKey != null && fuelDatePriceMap.get(previousKey).stream()
                        .anyMatch(fuel -> fuel.getPrice() == -1)) {
                        continue;
                    }
                toRemove.add(date);
            }
        }

        toRemove.forEach(fuelDatePriceMap::remove);
    }

    private void addEmptyDates(Map<FuelType, List<LocalDate>> fuelDateMap,
                               Map<LocalDate, List<Fuel>> fuelDatePriceMap) {
        Map<LocalDate, List<Fuel>> emptyDatesFuelDatePriceMap = new HashMap<>();

        for(FuelType fuelType : fuelDateMap.keySet()) {
            List<LocalDate> emptyDates = fuelDateMap.get(fuelType).stream()
                    .filter(date -> !fuelDatePriceMap.containsKey(date))
                    .toList();

            for(LocalDate emptyDate: emptyDates) {
                if(emptyDatesFuelDatePriceMap.containsKey(emptyDate)) {
                    emptyDatesFuelDatePriceMap.get(emptyDate)
                            .add(new Fuel(fuelType, -1));
                }
                else {
                    ArrayList<Fuel> emptyFuels = new ArrayList<>();
                    emptyFuels.add(new Fuel(fuelType, -1));

                    emptyDatesFuelDatePriceMap.put(emptyDate, emptyFuels);
                }
            }
        }
        fuelDatePriceMap.putAll(emptyDatesFuelDatePriceMap);
    }

    private Map<LocalDate, List<Fuel>> getFuelPriceDataPerMonths(LocalDate date, BiConsumer<Elements, List<Fuel>> fuelProcessor) {
        try {
            Document document = Jsoup.connect(getUrlWithDate(date)).get();
            Elements rows = Objects.requireNonNull(document.selectFirst("table")).select("tr");

            return buildFuelPriceMap(rows, fuelProcessor);
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

    private Map<LocalDate, List<Fuel>> buildFuelPriceMap(Elements rows, BiConsumer<Elements, List<Fuel>> fuelProcessor) throws ClientException {
        Map<LocalDate, List<Fuel>> dateFuelMap = new LinkedHashMap<>();
        for (int i = 1; i < rows.size(); i++) {
            Elements cells = rows.get(i).select("td");
            if (cells.isEmpty() || cells.get(0).text().isEmpty()) continue;

            LocalDate date = parseDate(cells.get(0).text());
            List<Fuel> fuelList = dateFuelMap.computeIfAbsent(date, k -> new ArrayList<>());

            fuelProcessor.accept(cells, fuelList);
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
}