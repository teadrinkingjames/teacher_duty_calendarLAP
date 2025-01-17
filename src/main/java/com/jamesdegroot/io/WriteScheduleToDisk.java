package com.jamesdegroot.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;

/**
 * WriteScheduleToDisk.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    WriteScheduleToDisk class, used to write the duty schedule to a CSV file
 *      
 *  Methods:
 * 	  - writeDutyScheduleToCSV, void, writes the duty schedule to a CSV file
 * 
*/

public class WriteScheduleToDisk {
    // CSV file constants
    private static final String CSV_HEADER = "Term,Day,Duty,Day 1 Teachers,Day 2 Teachers";
    private static final String CSV_FORMAT = "%s,%s,%s,%s,%s%n";
    
    /**
     * Writes the duty schedule to a CSV file.
     * @param calendar The calendar containing the duty schedule
     * @param outputPath The path to write the CSV file to
     */
    public static void writeDutyScheduleToCSV(Calendar calendar, String outputPath) {
        // Define term dates
        LocalDate[][] termDates = {
            {LocalDate.of(2024, 9, 3), LocalDate.of(2024, 11, 7)},    // Term 1
            {LocalDate.of(2024, 11, 8), LocalDate.of(2025, 2, 1)},    // Term 2
            {LocalDate.of(2025, 2, 2), LocalDate.of(2025, 4, 4)},     // Term 3
            {LocalDate.of(2025, 4, 5), LocalDate.of(2025, 6, 28)}     // Term 4
        };
        
        String[] termNames = {
            "Term 1",
            "Term 2",
            "Term 3",
            "Term 4"
        };
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            // Write header
            writer.println(CSV_HEADER);
            
            // For each term
            for (int term = 0; term < 4; term++) {
                final int currentTerm = term;
                
                // For each day of week (Monday to Friday)
                for (int dayOfWeek = 1; dayOfWeek <= 5; dayOfWeek++) {
                    final int currentDayOfWeek = dayOfWeek;
                    List<Day> daysForThisWeekday = calendar.getDaysOfYear().stream()
                        .filter(day -> day.getDate().getDayOfWeek().getValue() == currentDayOfWeek)
                        .filter(Day::isSchoolDay)
                        .filter(day -> {
                            LocalDate date = day.getDate();
                            return !date.isBefore(termDates[currentTerm][0]) && !date.isAfter(termDates[currentTerm][1]);
                        })
                        .toList();
                    
                    if (!daysForThisWeekday.isEmpty()) {
                        Day sampleDay = daysForThisWeekday.get(0);
                        String weekdayName = sampleDay.getDate().getDayOfWeek().toString();
                        
                        // Print duties for this day
                        Duty[][] dutySchedule = sampleDay.getDutySchedule();
                        for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                            for (Duty duty : dutySchedule[timeSlot]) {
                                if (duty != null) {
                                    writer.printf(CSV_FORMAT,
                                        termNames[currentTerm],
                                        weekdayName,
                                        duty.getName(),
                                        String.join(", ", duty.getDay1Teachers()),
                                        String.join(", ", duty.getDay2Teachers()));
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("Duty schedule has been written to: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
}
