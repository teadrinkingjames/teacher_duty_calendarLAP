package com.jamesdegroot.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Holiday;

/**
 * ReadCalendarFromDisk.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * Purpose: 
 *    Reads and parses an ICS calendar file (standard calendar format) to extract school holidays
 *    and special events. This information is used to determine school days and duty scheduling.
 *
 *  Methods:
 * 	  - loadCalendarFromICS, void, loads the calendar from an ICS file
 *    - parseDate, LocalDate, parses a date string from ICS format to LocalDate
 * 
*/

public class ReadCalendarFromDisk {
    // Standard ICS file markers and field identifiers
    private static final String EVENT_START = "BEGIN:VEVENT";
    private static final String EVENT_END = "END:VEVENT";
    private static final String SUMMARY_PREFIX = "SUMMARY:";
    private static final String START_DATE_PREFIX = "DTSTART;VALUE=DATE:";
    private static final String END_DATE_PREFIX = "DTEND;VALUE=DATE:";
    private static final String DESCRIPTION_PREFIX = "DESCRIPTION:";
    
    // Prefix lengths for efficient string parsing
    private static final int SUMMARY_PREFIX_LENGTH = 8;
    private static final int START_DATE_PREFIX_LENGTH = 17;
    private static final int END_DATE_PREFIX_LENGTH = 15;
    private static final int DESCRIPTION_PREFIX_LENGTH = 12;
    
    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd";
    
    /**
     * Processes an ICS file line by line, collecting event details until a complete
     * holiday event is found (marked by EVENT_END). Each complete event is added
     * to the calendar as a Holiday object.
     * 
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
                    // Reset fields for new event
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
                    // Create and add holiday only if all required fields are present
                    currentHoliday = new Holiday(summary, startDate, endDate, description);
                    calendar.addHoliday(currentHoliday);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading ICS file: " + e.getMessage());
        }
    }

    /**
     * Handles ICS date format conversion. ICS dates may contain additional characters,
     * so we clean the string before parsing (e.g., "20240115T000000Z" -> "20240115").
     * 
     * @param dateStr The date string in yyyyMMdd format
     * @return LocalDate object, or null if parsing fails
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
