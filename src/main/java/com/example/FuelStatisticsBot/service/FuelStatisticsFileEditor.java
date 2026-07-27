package com.example.FuelStatisticsBot.service;

import com.example.FuelStatisticsBot.model.Fuel;
import com.example.FuelStatisticsBot.model.FuelType;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
@PropertySource("application.properties")
public class FuelStatisticsFileEditor {

    private static final String PRICE_MEASUREMENT = "грн/л";

    @Value("${fuel.file.pass}")
    private String filePass;

    private final DateTimeFormatter dateTimeFormatter;

    @Autowired
    public FuelStatisticsFileEditor(DateTimeFormatter formatter) {
        this.dateTimeFormatter = formatter;
    }

    public File getFuelStatisticsFile(long chatId, Map<LocalDate, List<Fuel>> fuelDateMap, Collection<FuelType> requiredFuel,
                                      List<List<Double>> percentsList) throws IOException {
        File fuelFile = new File(String.format(filePass, chatId));

        try(FileOutputStream output = new FileOutputStream(fuelFile)) {
            XWPFDocument document = new XWPFDocument();

            createDatesList(document.createParagraph().createRun(), fuelDateMap.keySet());

            createDocumentTableStructure(document, fuelDateMap, requiredFuel);


            List<XWPFTable> tables = document.getTables();

            createDatePriceTable(fuelDateMap, requiredFuel, tables.get(0));
            createPriceGrowTable(percentsList, fuelDateMap, requiredFuel, tables.get(1));

            document.write(output);
            document.close();
        }
        return fuelFile;
    }


    private void createDocumentTableStructure(XWPFDocument document, Map<LocalDate, List<Fuel>> fuelDateMap,
                                                    Collection<FuelType> requiredFuel) {
        addNewLine(document);

        document.createTable(
                fuelDateMap.size() + 1,
                requiredFuel.size() + 1);

        addNewLine(document);

        document.createTable(requiredFuel.size() + 1, 2);
    }

    private void createDatesList(XWPFRun run, Set<LocalDate> fuelDateSet) {
        StringBuilder dates = new StringBuilder();
        fuelDateSet.forEach(localDate -> {
                dates.append(localDate.format(dateTimeFormatter)).append(", ");});
        run.setText(dates.substring(0, dates.length() - 2));
    }

    private void createDatePriceTable(Map<LocalDate, List<Fuel>> fuelDateMap, Collection<FuelType> requiredFuel,
                                      XWPFTable table) {
        Iterator<XWPFTableRow> rowIterator = table.getRows().iterator();

        setDatePriceTableHead(rowIterator.next().getTableCells(), requiredFuel);

        for(LocalDate key: fuelDateMap.keySet()) {
            XWPFTableRow row = rowIterator.next();
            Iterator<XWPFTableCell> cellIterator = row.getTableCells().iterator();

            if(fuelDateMap.get(key).stream().anyMatch(fuel -> fuel.getPrice() == -1)) {
                cellIterator.next().setText(key.format(dateTimeFormatter) + "*");
            }else {
                cellIterator.next().setText(key.format(dateTimeFormatter));
            }

            List<Fuel> fuels = new ArrayList<>(fuelDateMap.get(key));

            if (fuels.size() < requiredFuel.size()) {
                requiredFuel.stream()
                        .filter(fuelType -> fuels.stream()
                                .noneMatch(fuel -> fuel.getFuelType() == fuelType))
                        .forEach(fuelType -> fuels.add(new Fuel(fuelType, -1)));
            }

            fuels.sort(Comparator.comparingInt(f -> f.getFuelType().ordinal()));

            for(Fuel fuel: fuels) {
                if(fuel.getPrice() == -1){
                    cellIterator.next().setText("-");
                    continue;
                }
                String price = parsePrice(fuel.getPrice());
                cellIterator.next().setText(price);
            }
        }
    }

    private void setDatePriceTableHead(List<XWPFTableCell> cells, Collection<FuelType> requiredFuel) {
        Iterator<XWPFTableCell> cellIterator = cells.iterator();
        cellIterator.next().setText("Дата");
        for(FuelType fuelType: requiredFuel) {
            cellIterator.next().setText(fuelType.getFullName() + ", " + PRICE_MEASUREMENT);
        }
    }

    private void createPriceGrowTable(List<List<Double>> percentsList, Map<LocalDate, List<Fuel>> fuelDateMap,
                                      Collection<FuelType> requiredFuel, XWPFTable table) {
        Iterator<XWPFTableRow> rowIterator = table.getRows().iterator();

        setPriceGrowTableHead(rowIterator.next().getTableCells());

        Iterator<List<Double>> percentsIterator = percentsList.iterator();

        for(FuelType fuelType: requiredFuel) {
            List<LocalDate> dates = fuelDateMap.entrySet().stream()
                    .filter(entry -> entry.getValue().stream()
                            .anyMatch(fuel -> fuel.getFuelType().equals(fuelType)))
                    .map(Map.Entry::getKey)
                    .toList();

            XWPFTableRow row = rowIterator.next();
            Iterator<XWPFTableCell> cellIterator = row.getTableCells().iterator();

            cellIterator.next().setText(fuelType.getFullName());
            cellIterator.next().setText(parseStatistics(percentsIterator.next(), dates));
        }
    }

    private void setPriceGrowTableHead(List<XWPFTableCell> cells) {
        cells.get(0).setText("Вид пального");
        cells.get(1).setText("Відсоток збільшення роздрібних цін в період ");
    }

    private String parseStatistics(List<Double> percents, List<LocalDate> dates) {
        StringBuilder builder = new StringBuilder();

        if(dates.isEmpty()) return "";

        LocalDate lastElement = dates.get(dates.size() - 1);

        Iterator<Double> percentsIterator = percents.iterator();

        for(LocalDate date: dates) {
            if(dates.indexOf(date) == dates.size() - 1) break;

            String percent = parsePercent(percentsIterator.next());
            builder
                    .append("з ")
                    .append(date.format(dateTimeFormatter))
                    .append("р по ")
                    .append(lastElement.format(dateTimeFormatter))
                    .append("р.                     ")
                    .append(percent)
                    .append("%             ");
        }
        return builder.toString();
    }

    private String parsePercent(Double percent) {
        if(percent.isNaN()) return "----";

        String stringPercent = Double.toString(percent);
        StringBuilder parsedPercent = new StringBuilder();

        String[] num = stringPercent.split("\\.");

        parsedPercent.append(num[0])
                .append(",")
                .append(num[1]);

        if(num[1].length() < 2) parsedPercent.append("0");

        return parsedPercent.toString();
    }

    private String parsePrice(int price) {
        StringBuilder builder = new StringBuilder(price + "");
        builder.insert(builder.length() - 2, ",");
        return builder.toString();
    }

    private void addNewLine(XWPFDocument document) {
        var paragraph = document.createParagraph();
        var run = paragraph.createRun();
        run.addBreak();
    }

}
