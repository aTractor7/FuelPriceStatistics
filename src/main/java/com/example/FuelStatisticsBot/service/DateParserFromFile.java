package com.example.FuelStatisticsBot.service;

import com.example.FuelStatisticsBot.model.FuelType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DateParserFromFile {

    private final DateTimeFormatter dateTimeFormatter;

    private static final String DATE_REGEX = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[0,1,2])\\.(19|20)\\d{2}";

    public DateParserFromFile(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public Map<FuelType, List<LocalDate>> getDatesForFuelTypes(File file) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            XWPFDocument document = new XWPFDocument(input);

            XWPFTable table = document.getTables().get(0);

            Map<FuelType, List<LocalDate>> fuelTypeDates = getDatesFromTable(table);

            document.close();
            return fuelTypeDates.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Enum::ordinal)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
    }

    private Map<FuelType, List<LocalDate>> getDatesFromTable(XWPFTable table) {
        Map<FuelType, List<LocalDate>> fuelTypeDates = new HashMap<>();

        for(XWPFTableRow row: table.getRows()){
            if(row == null || row.getTableCells() == null || row.getTableCells().isEmpty()) continue;

            String fuelType = row.getCell(0).getText();
            if(fuelType == null || fuelType.isBlank() ||
                    !Arrays.stream(FuelType.values()).map(Enum::toString).toList().contains(fuelType))
                continue;

            FuelType fuelTypeEnum = FuelType.valueOf(fuelType);

            List<LocalDate> dates = getDatesFromCell(row.getCell(1));
            dates.sort(LocalDate::compareTo);


            fuelTypeDates.put(fuelTypeEnum, dates);
        }

        return fuelTypeDates;
    }

    private List<LocalDate> getDatesFromCell(XWPFTableCell cell) {
        int dateLength = 10;
        String text = cell.getText();

        List<LocalDate> dates = new ArrayList<>();

        for(int i = 0; i < text.length() - dateLength; i++) {
            String dateCandidate = text.substring(i, i + dateLength);
            if(dateCandidate.matches(DATE_REGEX)) {
                i += dateLength - 1;

                LocalDate date = LocalDate.parse(dateCandidate, dateTimeFormatter);

                if(dates.contains(date)) continue;

                dates.add(date);
            }
        }

        return dates;
    }
}
