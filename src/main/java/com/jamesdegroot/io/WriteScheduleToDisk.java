package com.jamesdegroot.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println(CSV_HEADER);
            
            // Process each term
            for (int term = 1; term <= 4; term++) {
                // Process each weekday (Monday to Friday)
                for (int dayOfWeek = 1; dayOfWeek <= 5; dayOfWeek++) {
                    final int currentDayOfWeek = dayOfWeek;
                    
                    // Get Day 1 and Day 2 samples for this weekday
                    List<Day> daysForWeekday = calendar.getDaysOfYear().stream()
                        .filter(Day::isSchoolDay)
                        .filter(day -> day.getDate().getDayOfWeek().getValue() == currentDayOfWeek)
                        .toList();
                    
                    if (!daysForWeekday.isEmpty()) {
                        // Get separate Day 1 and Day 2 samples
                        Day day1Sample = daysForWeekday.stream()
                            .filter(Day::isDay1)
                            .findFirst()
                            .orElse(null);
                            
                        Day day2Sample = daysForWeekday.stream()
                            .filter(day -> !day.isDay1())
                            .findFirst()
                            .orElse(null);
                        
                        // Use either day as template (they should have same duty structure)
                        Day templateDay = day1Sample != null ? day1Sample : day2Sample;
                        if (templateDay != null) {
                            String weekdayName = templateDay.getDate().getDayOfWeek().toString();
                            
                            // Write duties for this weekday
                            Duty[][] dutySchedule = templateDay.getDutySchedule();
                            for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                                for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                                    Duty duty1 = day1Sample != null ? day1Sample.getDutySchedule()[timeSlot][pos] : null;
                                    Duty duty2 = day2Sample != null ? day2Sample.getDutySchedule()[timeSlot][pos] : null;
                                    
                                    // Use either duty as template (they should have same name)
                                    Duty templateDuty = duty1 != null ? duty1 : duty2;
                                    if (templateDuty != null) {
                                        writer.printf(CSV_FORMAT,
                                            "Term " + term,
                                            weekdayName,
                                            templateDuty.getName(),
                                            duty1 != null ? String.join(" + ", duty1.getDay1Teachers()) : "", // This cant use commas because in the csv it will be read as different columns
                                            duty2 != null ? String.join(" + ", duty2.getDay2Teachers()) : "");
                                    }
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
