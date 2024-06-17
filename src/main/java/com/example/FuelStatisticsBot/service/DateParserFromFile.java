package com.example.FuelStatisticsBot.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
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

    public List<LocalDate> getDatesFromFile(File file) {
        try(FileInputStream input = new FileInputStream(file)) {
            XWPFDocument document = new XWPFDocument(input);

            XWPFTable table = document.getTables().get(0);

            List<LocalDate> dates = new ArrayList<>(getDatesFromTable(table));
            dates.sort(LocalDate::compareTo);

            return dates;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<LocalDate> getDatesFromTable(XWPFTable table) {
        Set<LocalDate> dates = new HashSet<>();

        int rows = table.getNumberOfRows();

        for(int i = 1; i < rows; i++) {
            XWPFTableCell cell = table.getRows().get(i).getCell(1);
            dates.addAll(getDatesFromCell(cell));
        }

        return dates;
    }

    private Set<LocalDate> getDatesFromCell(XWPFTableCell cell) {
        String text = cell.getText();
        String[] units = text.split(" ");

        return Arrays.stream(units)
                .filter(u -> u.matches(DATE_REGEX))
                .map(s -> LocalDate.parse(s, dateTimeFormatter))
                .collect(Collectors.toSet());
    }
}
