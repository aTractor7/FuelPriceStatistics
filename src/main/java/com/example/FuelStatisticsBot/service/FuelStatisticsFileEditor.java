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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

    public File getFuelStatisticsFile(Map<LocalDate, List<Fuel>> fuelDateMap, List<FuelType> requiredFuel,
                                      List<List<Double>> percentsList) throws IOException {
        File fuelFile = new File(filePass);

        try(FileOutputStream output = new FileOutputStream(fuelFile)) {
            XWPFDocument document = new XWPFDocument();

            createDocumentStructure(document, fuelDateMap, requiredFuel);

            List<XWPFTable> tables = document.getTables();

            createDatePriceTable(fuelDateMap, requiredFuel, tables.get(0));
            createPriceGrowTable(percentsList, fuelDateMap.keySet(), requiredFuel, tables.get(1));

            document.write(output);
            document.close();
        }
        return fuelFile;
    }


    private void createDocumentStructure(XWPFDocument document, Map<LocalDate, List<Fuel>> fuelDateMap,
                                                    List<FuelType> requiredFuel) {
        document.createTable(
                fuelDateMap.keySet().size() + 1,
                requiredFuel.size() + 1);

        addNewLine(document);

        document.createTable(requiredFuel.size() + 1, 2);
    }

    private void createDatePriceTable(Map<LocalDate, List<Fuel>> fuelDateMap, List<FuelType> requiredFuel,
                                      XWPFTable table) {
        Iterator<XWPFTableRow> rowIterator = table.getRows().iterator();

        setDatePriceTableHead(rowIterator.next().getTableCells(), requiredFuel);

        for(LocalDate key: fuelDateMap.keySet()) {
            XWPFTableRow row = rowIterator.next();
            Iterator<XWPFTableCell> cellIterator = row.getTableCells().iterator();

            cellIterator.next().setText(key.format(dateTimeFormatter));

            for(Fuel fuel: fuelDateMap.get(key)) {
                if(requiredFuel.contains(fuel.getFuelType())) {
                    String price = parsePrice(fuel.getPrice());
                    cellIterator.next().setText(price);
                }
            }
        }
    }

    private void setDatePriceTableHead(List<XWPFTableCell> cells, List<FuelType> requiredFuel) {
        Iterator<XWPFTableCell> cellIterator = cells.iterator();
        cellIterator.next().setText("Дата");
        for(FuelType fuelType: requiredFuel) {
            cellIterator.next().setText(parseFuelType(fuelType) + ", " + PRICE_MEASUREMENT);
        }
    }

    private void createPriceGrowTable(List<List<Double>> percentsList, Set<LocalDate> dateSet,
                                      List<FuelType> requiredFuel, XWPFTable table) {
        Iterator<XWPFTableRow> rowIterator = table.getRows().iterator();

        setPriceGrowTableHead(rowIterator.next().getTableCells());

        Iterator<List<Double>> percentsIterator = percentsList.iterator();

        for(FuelType fuelType: requiredFuel) {
            XWPFTableRow row = rowIterator.next();
            Iterator<XWPFTableCell> cellIterator = row.getTableCells().iterator();

            cellIterator.next().setText(parseFuelType(fuelType));
            cellIterator.next().setText(parseStatistics(percentsIterator.next(), dateSet));
        }
    }

    private void setPriceGrowTableHead(List<XWPFTableCell> cells) {
        cells.get(0).setText("Вид пального");
        cells.get(1).setText("Відсоток збільшення роздрібних цін в період ");
    }



    private String parseStatistics(List<Double> percents, Set<LocalDate> dateSet) {
        StringBuilder builder = new StringBuilder();

        LocalDate lastElement = dateSet.stream().reduce((first, second) -> second).get();

        Iterator<Double> percentsIterator = percents.iterator();

        for(LocalDate date: dateSet) {
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

    private String parseFuelType(FuelType fuelType) {
        String result;

        switch (fuelType) {
            case A95_PLUS -> result = "Бензин А-95 (покращеної якості)";
            case A95 -> result = "Бензин А-95";
            case A92 -> result = "Бензин А-92";
            case DT_PLUS -> result = "Дизельне паливо (покращенної якості)";
            case DT -> result = "Дизельне паливо";
            case GAS -> result = "Газ";
            default -> throw new RuntimeException("No such fuel type");
        }
        return result;
    }

    private String parsePercent(double percent) {
        String stringPercent = Double.toString(percent);
        StringBuilder parsedPercent = new StringBuilder();

        String[] num = stringPercent.split("\\.");

        return parsedPercent.append(num[0])
                .append(",")
                .append(num[1])
                .toString();
    }

    private String parsePrice(int price) {
        StringBuilder builder = new StringBuilder(price + "");
        builder.insert(2, ",");
        return builder.toString();
    }

    private void addNewLine(XWPFDocument document) {
        var paragraph = document.createParagraph();
        var run = paragraph.createRun();
        run.addBreak();
    }

}
