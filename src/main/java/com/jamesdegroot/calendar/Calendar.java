package com.jamesdegroot.calendar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jamesdegroot.GenerateDutyCalendar;

/**
 * Calendar.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    Manages the school year calendar, including:
 *    - School days and holidays tracking
 *    - Duty schedules for each school day
 *    - Day 1/Day 2 rotation system
 *    - Teacher duty assignments
 *    
 *    The calendar spans from September 2024 to June 2025, handling both
 *    regular school days and special events/holidays loaded from ICS files.
 *      
 *  Methods:
 * 	  - loadFromICS, void, loads the calendar from an ICS file
 *    - parseDate, LocalDate, parses a date string into a LocalDate object
 *    - isSchoolDay, boolean, checks if a given date is a school day
 *    - getEvents, List<Holiday>, returns the list of events
 *    - printSchoolDays, void, prints the school days between two dates
 *    - initializeDaysOfYear, void, initializes the days of the year for 2024
 *    - getDaysOfYear, List<Day>, returns the list of days in the year
 *    - addHoliday, void, adds a holiday to the calendar
*/
public class Calendar {
    // School year boundary definitions
    private static final int DEFAULT_DAYS_IN_YEAR = 365;
    private static final int SCHOOL_YEAR_START_YEAR = 2024;
    private static final int SCHOOL_YEAR_START_MONTH = 9;
    private static final int SCHOOL_YEAR_START_DAY = 3;
    private static final int SCHOOL_YEAR_END_YEAR = 2025;
    private static final int SCHOOL_YEAR_END_MONTH = 6;
    private static final int SCHOOL_YEAR_END_DAY = 28;
    
    // ICS file parsing markers (used for reading calendar events)
    private static final String EVENT_START = "BEGIN:VEVENT";
    private static final String EVENT_END = "END:VEVENT";
    private static final String SUMMARY_PREFIX = "SUMMARY:";
    private static final String START_DATE_PREFIX = "DTSTART;VALUE=DATE:";
    private static final String END_DATE_PREFIX = "DTEND;VALUE=DATE:";
    private static final String DESCRIPTION_PREFIX = "DESCRIPTION:";
    
    private static final String DATE_FORMAT_PATTERN = "yyyyMMdd";
    private static final String DISPLAY_DATE_FORMAT = "EEEE, MMMM d, yyyy";
    
    // Display formatting for duty schedules
    private static final String DUTY_FORMAT = "%-12s | %-30s | %-20s\n";
    private static final String UNASSIGNED_TEXT = "UNASSIGNED";
    private static final String TIME_SLOT_PREFIX = "Slot ";
    
    // Core data structures: holidays/events and daily schedules
    private List<Holiday> events;
    private List<Day> daysOfYear = new ArrayList<>(DEFAULT_DAYS_IN_YEAR); 

    public Calendar() {
        this.events = new ArrayList<>();
    }

    /**
     * Loads the calendar from an ICS file.
     * @param filename the name of the ICS file to load
     * @throws IOException if there is an error reading the ICS file    
     */
    public void loadFromICS(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            Holiday currentEvent = null;
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
                    summary = line.substring(SUMMARY_PREFIX.length());
                } else if (line.startsWith(START_DATE_PREFIX)) {
                    startDate = parseDate(line.substring(START_DATE_PREFIX.length()));
                } else if (line.startsWith(END_DATE_PREFIX)) {
                    endDate = parseDate(line.substring(END_DATE_PREFIX.length()));
                } else if (line.startsWith(DESCRIPTION_PREFIX)) {
                    description = line.substring(DESCRIPTION_PREFIX.length());
                } else if (line.startsWith(EVENT_END) && summary != null && startDate != null && endDate != null) {
                    currentEvent = new Holiday(summary, startDate, endDate, description);
                    events.add(currentEvent);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading ICS file: " + e.getMessage());
        }
    }

    /**
     * Parses a date string into a LocalDate object.
     * @param dateStr the date string to parse
     * @return the parsed LocalDate object
     */
    private LocalDate parseDate(String dateStr) {
        // Clean up the date string by removing any non-digit characters
        dateStr = dateStr.replaceAll("[^0-9]", "");
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr);
            return null;
        }
    }

    /**
     * Checks if a given date is a school day.
     * @param date the date to check
     * @return true if the date is a school day, false otherwise
     */
    public boolean isSchoolDay(LocalDate date) {
        return daysOfYear.stream()
            .filter(d -> d.getDate().equals(date))
            .findFirst()
            .map(Day::isSchoolDay)
            .orElse(false);
    }

    /**
     * Returns the list of events.
     * @return the list of events
     */
    public List<Holiday> getEvents() {
        return events;
    }

    /**
     * Prints the school days between two dates.
     * @param startDate the start date
     * @param endDate the end date
     */
    public void printSchoolDays(LocalDate startDate, LocalDate endDate) {
        System.out.println("\nDuty Schedule:");
        System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate date = current;
            Optional<Day> dayOpt = daysOfYear.stream()
                .filter(d -> d.getDate().equals(date))
                .findFirst();
                
            if (dayOpt.isPresent()) {
                Day day = dayOpt.get();
                if (day.isSchoolDay()) {
                    System.out.println("\n" + day.getDate().format(
                        DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT)));
                    System.out.println("-".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
                    System.out.printf(DUTY_FORMAT, "Time Slot", "Duty", "Teacher");
                    System.out.println("-".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
                    
                    Duty[][] duties = day.getDutySchedule();
                    for (int timeSlot = 0; timeSlot < duties.length; timeSlot++) {
                        for (int pos = 0; pos < duties[timeSlot].length; pos++) {
                            Duty duty = duties[timeSlot][pos];
                            if (duty != null) {
                                System.out.printf(DUTY_FORMAT,
                                    String.format(TIME_SLOT_PREFIX + "%d", timeSlot + 1),
                                    duty.getName(),
                                    duty.getTeacher() != null ? duty.getTeacher() : UNASSIGNED_TEXT
                                );
                            }
                        }
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    /**
     * Sets up the entire school year calendar by:
     * 1. Creating Day objects for each day from Sept 2024 to June 2025
     * 2. Marking weekends and holidays as non-school days
     * 3. Initializing duty slots for each school day
     * 4. Tracking school days per month for scheduling purposes
     */
    public void initializeDaysOfYear() {
        LocalDate startDate = LocalDate.of(SCHOOL_YEAR_START_YEAR, SCHOOL_YEAR_START_MONTH, SCHOOL_YEAR_START_DAY);
        LocalDate endDate = LocalDate.of(SCHOOL_YEAR_END_YEAR, SCHOOL_YEAR_END_MONTH, SCHOOL_YEAR_END_DAY);
        
        // Track school days vs total days per month for scheduling
        int[] schoolDaysByMonth = new int[12];  // Index 0 = January, 11 = December
        int[] totalDaysByMonth = new int[12];   
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Day day = new Day(currentDate);
            
            // Count total days
            totalDaysByMonth[currentDate.getMonthValue() - 1]++;
            
            // Determine if it's a school day (not weekend or holiday)
            boolean isSchoolDay = true;
            if (currentDate.getDayOfWeek().getValue() >= 6) {
                isSchoolDay = false;
            }
            for (Holiday event : events) {
                // Note: End date in ICS is exclusive
                if (currentDate.equals(event.getStartDate()) || 
                    (currentDate.isAfter(event.getStartDate()) && 
                     currentDate.isBefore(event.getEndDate()))) {
                    isSchoolDay = false;
                    break;
                }
            }
            day.setSchoolDay(isSchoolDay);
            
            // Count school days
            if (isSchoolDay) {
                schoolDaysByMonth[currentDate.getMonthValue() - 1]++;
            }
            
            // Initialize duties if it's a school day
            if (isSchoolDay) {
                for (int timeSlot = 0; timeSlot < Day.TIME_SLOTS; timeSlot++) {
                    for (int position = 0; position < Day.DUTIES_PER_SLOT; position++) {
                        int dutyIndex = (timeSlot * Day.DUTIES_PER_SLOT) + position;
                        String dutyName = dutyIndex < Duty.DUTY_NAMES.length ? 
                            Duty.DUTY_NAMES[dutyIndex] : "Duty " + (dutyIndex + 1);
                        
                        Duty duty = new Duty(
                            dutyName,
                            "",  // No description needed
                            "Various",  // room
                            String.format(TIME_SLOT_PREFIX + "%d", timeSlot + 1)
                        );
                        day.addDuty(timeSlot, position, duty);
                    }
                }
            }
            
            daysOfYear.add(day);
            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * Gets the list of days in the year
     * @return List of Day objects
     */
    public List<Day> getDaysOfYear() {
        return daysOfYear;
    }

    /**
     * Adds a holiday to the calendar.
     * @param event the holiday to add
     */
    public void addHoliday(Holiday event) {
        if (event != null) {
            events.add(event);
        }
    }
}
