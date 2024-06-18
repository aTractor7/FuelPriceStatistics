package com.example.FuelStatisticsBot.client;

import com.example.FuelStatisticsBot.model.Fuel;
import com.example.FuelStatisticsBot.model.FuelType;
import com.example.FuelStatisticsBot.util.exception.ClientException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
        Map<LocalDate, List<Fuel>> fuelDatePriceMap = new LinkedHashMap<>();

        while (start.getMonth().getValue() <= end.getMonth().getValue() || start.getYear() < end.getYear()) {
            try {
                Document document = Jsoup.connect(getUrlWithDate(start)).get();
                Elements rows = Objects.requireNonNull(document.selectFirst("table")).select("tr");
                fuelDatePriceMap.putAll(getFuelPriceDataPerMonths(rows));
                start = start.plusMonths(1);
            } catch (IOException e) {
                throw new ClientException("Exception due to connecting to url using getMethod: " + url, e);
            }
        }

        return fuelDatePriceMap;
    }

    private Map<LocalDate, List<Fuel>> getFuelPriceDataPerMonths(Elements rows) throws ClientException{
        Map<LocalDate, List<Fuel>> dateFuelMap = new LinkedHashMap<>();
        for(int i = 1; i < rows.size(); i ++) {
            Elements cells = rows.get(i).select("td");

            LocalDate date = null;
            List<Fuel> fuelList = new ArrayList<>();

            for (int j = 0; j < cells.size(); j ++) {
                Element cell = cells.get(j);

                if(cell.text().isEmpty()) continue;

                if(j == 0) {
                    date = parseDate(cell.text());
                }else {
                    FuelType fuelType = determineFuelType(j);

                    String price = cell.text();
                    Fuel fuel = new Fuel(fuelType, parsePrice(price));
                    fuelList.add(fuel);
                }
            }
            dateFuelMap.put(date, fuelList);
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

    private String getUrlWithDate(LocalDate date) {
        StringBuilder dateBuilder = new StringBuilder();
        dateBuilder.append(date.getYear()).append("-");

        int months = date.getMonth().getValue();
        if (months < 10) dateBuilder.append("0");

        String dateStr = dateBuilder.append(months).toString();
        return String.format(url, dateStr);
    }
}