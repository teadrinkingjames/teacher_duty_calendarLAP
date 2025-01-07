package com.jamesdegroot.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Holiday;

public class ReadCalendarFromDisk {
    // ICS file event markers
    private static final String EVENT_START = "BEGIN:VEVENT";
    private static final String EVENT_END = "END:VEVENT";
    
    // ICS file field prefixes
    private static final String SUMMARY_PREFIX = "SUMMARY:";
    private static final String START_DATE_PREFIX = "DTSTART;VALUE=DATE:";
    private static final String END_DATE_PREFIX = "DTEND;VALUE=DATE:";
    private static final String DESCRIPTION_PREFIX = "DESCRIPTION:";
    
    // Field lengths for substring operations
    private static final int SUMMARY_PREFIX_LENGTH = 8;
    private static final int START_DATE_PREFIX_LENGTH = 17;
    private static final int END_DATE_PREFIX_LENGTH = 15;
    private static final int DESCRIPTION_PREFIX_LENGTH = 12;
    
    // Date format pattern
    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd";
    
    /**
     * Loads calendar events from an ICS file into a Calendar object.
     * @param calendar The Calendar object to populate
     * @param file The ICS file to read
     * @throws IOException if file reading fails (caught internally)
     */
    public static void loadCalendarFromICS(Calendar calendar, File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Holiday currentHoliday = null;
            String summary = null;
            LocalDate startDate = null;
            LocalDate endDate = null;
            String description = "";
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(EVENT_START)) {
                    summary = null;
                    startDate = null;
                    endDate = null;
                    description = "";
                } else if (line.startsWith(SUMMARY_PREFIX)) {
                    summary = line.substring(SUMMARY_PREFIX_LENGTH);
                } else if (line.startsWith(START_DATE_PREFIX)) {
                    startDate = parseDate(line.substring(START_DATE_PREFIX_LENGTH));
                } else if (line.startsWith(END_DATE_PREFIX)) {
                    endDate = parseDate(line.substring(END_DATE_PREFIX_LENGTH));
                } else if (line.startsWith(DESCRIPTION_PREFIX)) {
                    description = line.substring(DESCRIPTION_PREFIX_LENGTH);
                } else if (line.startsWith(EVENT_END) && summary != null && startDate != null && endDate != null) {
                    currentHoliday = new Holiday(summary, startDate, endDate, description);
                    calendar.addHoliday(currentHoliday);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading ICS file: " + e.getMessage());
        }
    }

    /**
     * Parses a date string from ICS format to LocalDate.
     * @param dateStr The date string in yyyyMMdd format
     * @return LocalDate object, or null if parsing fails
     * @throws Exception if parsing fails
     */
    private static LocalDate parseDate(String dateStr) {
        dateStr = dateStr.replaceAll("[^0-9]", "");
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return null;
        }
    }
}
