package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import com.jamesdegroot.GenerateDutyCalendar;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

public class DutyAssigner {
    // Calendar and teacher references
    private final Calendar calendar;
    private final List<Teacher> teachers;
    //private final DutyScheduleTemplate[] termTemplates;
    private boolean debugOutput = false;  // Flag to control console output
    
    // Random number generator for teacher selection
    private final Random random = new Random();
    
    // Add new class-level data structure
    private Map<DayPattern, List<Day>> dayGroups;

    // Add enum for day patterns
    private enum DayPattern {
        MONDAY_DAY1,    MONDAY_DAY2,
        TUESDAY_DAY1,   TUESDAY_DAY2,
        WEDNESDAY_DAY1, WEDNESDAY_DAY2,
        THURSDAY_DAY1,  THURSDAY_DAY2,
        FRIDAY_DAY1,    FRIDAY_DAY2
    }

    public DutyAssigner(Calendar calendar, List<Teacher> teachers) {
        this.calendar = calendar;
        this.teachers = teachers;
        //this.termTemplates = new DutyScheduleTemplate[4];
        this.dayGroups = new EnumMap<>(DayPattern.class);
        // Initialize all groups with empty lists
        for (DayPattern pattern : DayPattern.values()) {
            dayGroups.put(pattern, new ArrayList<>());
        }
    }
    
    public void setDebugOutput(boolean enabled) {
        this.debugOutput = enabled;
    }
    
    
    /**
     * Main method to assign duties for the entire semester
     */
    public void assignDuties() {
        List<Day> schoolDays = getSchoolDays();
        System.out.println("Starting duty assignment...");
        
        // Create a map for each term
        Map<Integer, Map<DayPattern, List<Day>>> termPatternGroups = new HashMap<>();
        for (int term = 0; term < 4; term++) {
            termPatternGroups.put(term, new EnumMap<>(DayPattern.class));
            for (DayPattern pattern : DayPattern.values()) {
                termPatternGroups.get(term).put(pattern, new ArrayList<>());
            }
        }
        
        // Group the days by term and pattern
        for (Day day : schoolDays) {
            LocalDate date = day.getDate();
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean isDay1 = day.isDay1();
            int term = day.getTermNumber();
            
            DayPattern pattern = getDayPattern(dayOfWeek, isDay1);
            if (pattern != null) {
                termPatternGroups.get(term).get(pattern).add(day);
            }
        }

        // Print the number of days for each pattern in each term
        for (int term = 0; term < 4; term++) {
            System.out.println("\nTerm " + term + " pattern counts:");
            for (DayPattern pattern : DayPattern.values()) {
                int count = termPatternGroups.get(term).get(pattern).size();
                System.out.println(pattern + ": " + count);
            }
        }

        /*  Third, give a value to every duty in one of each type of day (Mondays day 1s (odd weekday), Mondays day 2s (even weekday), Tuesdays day 1s (odd weekday), etc.) 
         *  based on how many times they are repeated in the term
         *  Remember each duty is repeated every 2 weeks because of the day 1/2 rotation
         *  This is need to check is this duty is cancelled due to an holiday
        */
        for (DayPattern pattern : DayPattern.values()) {
            List<Day> days = dayGroups.get(pattern);
            for (Day day : days) {
                System.out.println("Day: " + day.getDate() + " - Pattern: " + pattern + " - Term: " + day.getTermNumber());
            }
        } // this means the every duty in the given type of day is repeated that amount of times in the year
          // this should be changed to the number of the given type of day in the term
        // lets create a new list of days for each term, and fill it with the days of the term
        
        List<List<Day>> termDaysForValues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            termDaysForValues.add(new ArrayList<>());
        }
        for (Day day : schoolDays) {
            int termNumber = day.getTermNumber();
            termDaysForValues.get(termNumber).add(day);
        }

        // now we have a list of days for each term, with the days of the term
        // print the number of days in each term
        for (int i = 0; i < 4; i++) {
            System.out.println("Term " + i + ": " + termDaysForValues.get(i).size());
        }

        
        /*  Fourth, assign duties to teachers
         *  This is done by checking the duty schedule of each teacher
         *  and giving them the largest value duty that is not already assigned
         *  this may have to consider using two smaller duties to add up to the value needed
         *  PRIORITIZE the duties that are more important, DDC, LUNCH, Library, etc. 
         *  Halls are used as filler duties if a teacher needs more to fill their schedule
         * 
         *  This is best done iterating through the teachers, not the days -> dutys
        */
        // lets iterate through the teachers
        for (Teacher teacher : teachers) {
            if (teacher.getMaxDutiesPerSemester() == 0) {
                System.out.println("Teacher " + teacher.getName() + " has no duties");
                continue;
            }
            System.out.println("Teacher: " + teacher.getName());
            // check if the teacher should be assigned a duty
            
            // get the schedule of the teacher
            List<String> schedule = teacher.getSchedule();
            // iterate through the schedule
            for (String classPeriod : schedule) {
                System.out.println("Class Period: " + classPeriod);
            }
            
            // begin assigning duties
            // check if the teacher needs more duties
            for (int numberOfDutiesAssigned = 0; numberOfDutiesAssigned < teacher.getMaxDutiesPerSemester(); numberOfDutiesAssigned++) {
                // iterate through the terms
                for (int term = 0; term < 4; term++) {
                    //if the teacher has no duties in this term, skip to the next term
                    if (!teacher.hasClassInSemester(term)) {
                        System.out.println("Teacher " + teacher.getName() + " has no classes in term " + term);
                        continue;
                    }
                    // iterate through the days
                    for (Day day : termDaysForValues.get(term)) {
                        // check if the duty is already assigned
                    }
                }
            }
        }
        
        System.out.println("Duty assignment completed!");
    }
    
    /**
     * Gets a list of all school days from the calendar
     */
    private List<Day> getSchoolDays() {
        return calendar.getDaysOfYear().stream()
            .filter(Day::isSchoolDay)
            .toList();
    }
    
    /**
     * Loads test data automatically from resource files
     */
    public static void loadTestData(GenerateDutyCalendar appLogic) {
        try {
            // Get resource URLs
            java.net.URL calendarUrl = DutyAssigner.class.getClassLoader()
                .getResource("ICalendarHandler.ics");
            java.net.URL teacherUrl = DutyAssigner.class.getClassLoader()
                .getResource("Copy Teacher Linear from 2023-2024 - Sheet1.csv");
            
            if (calendarUrl == null || teacherUrl == null) {
                System.err.println("Could not find resource files");
                return;
            }
            
            // Create file objects from URLs
            java.io.File calendarFile = new java.io.File(calendarUrl.toURI());
            java.io.File teacherFile = new java.io.File(teacherUrl.toURI());
            
            // Load the files using the app logic
            appLogic.loadCalendar(calendarFile);
            appLogic.processFile(teacherFile);
            
            System.out.println("Test data loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DayPattern getDayPattern(DayOfWeek dayOfWeek, boolean isDay1) {
        return switch (dayOfWeek) {
            case MONDAY -> isDay1 ? DayPattern.MONDAY_DAY1 : DayPattern.MONDAY_DAY2;
            case TUESDAY -> isDay1 ? DayPattern.TUESDAY_DAY1 : DayPattern.TUESDAY_DAY2;
            case WEDNESDAY -> isDay1 ? DayPattern.WEDNESDAY_DAY1 : DayPattern.WEDNESDAY_DAY2;
            case THURSDAY -> isDay1 ? DayPattern.THURSDAY_DAY1 : DayPattern.THURSDAY_DAY2;
            case FRIDAY -> isDay1 ? DayPattern.FRIDAY_DAY1 : DayPattern.FRIDAY_DAY2;
            default -> null;
        };
    }
} 