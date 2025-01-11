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
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;

public class DutyAssigner {
    // Calendar and teacher references
    private final Calendar calendar;
    private final List<Teacher> teachers;
    //private final DutyScheduleTemplate[] termTemplates;
    
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
            List<String> classSchedule = teacher.getSchedule();
            // iterate through the schedule
            List<String> classPeriods = new ArrayList<>();
            for (String classPeriod : classSchedule) {
                // Store this in a list of strings
                classPeriods.add(classPeriod);
                //System.out.println("Class Period: " + classPeriod);
            }
            
            // begin assigning duties
            // check if the teacher needs more duties
            // iterate through the semesters
            for (int semester = 0; semester < 2; semester++) {
                // check if the teacher has no classes in this semester
                if ((classPeriods.subList(0, 5).equals(Arrays.asList("", "", "", "", "")) && semester == 0) || 
                    (classPeriods.subList(5, 10).equals(Arrays.asList("", "", "", "", "")) && semester == 1)) {
                    System.out.println("Teacher " + teacher.getName() + " has no classes in semester " + (semester+1));
                    continue; // skips to the next semester
                }
                
                System.out.println("Teacher " + teacher.getName() + " has classes in semester " + (semester+1));
                int numberOfDutiesNeeded = teacher.getMaxDutiesPerSemester();
                
                // Iterate through the terms in this semester
                for (int term = semester * 2; term < (semester * 2 + 2); term++) {
                    System.out.println("Processing term " + term);
                    
                    //get the values of the types of days in the term
                    List<Day> days = termPatternGroups.get(term).values().stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                    
                    // Count and print occurrences of each day pattern
                    Map<DayPattern, Long> patternCounts = days.stream()
                        .collect(Collectors.groupingBy(
                            day -> getDayPattern(day.getDate().getDayOfWeek(), day.isDay1()),
                            Collectors.counting()
                        ));
                    
                    // Print patterns in order by weekday
                    /*
                    System.out.println("Term " + term + " pattern counts:");
                    for (DayOfWeek day : DayOfWeek.values()) {
                        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                            DayPattern day1Pattern = getDayPattern(day, true);
                            DayPattern day2Pattern = getDayPattern(day, false);
                            System.out.printf("%s Day1: %d, Day2: %d%n", 
                                day.toString(), 
                                patternCounts.getOrDefault(day1Pattern, 0L),
                                patternCounts.getOrDefault(day2Pattern, 0L));
                        }
                    }
                    */
                    // now we have the counts of the types of days of the week
                    // we know the value of each duty in each type of day
                    
                    // now we need to assign the duties to the teacher
                    // go through the days of the week, and through every duty in the day
                    
                    // check if the duty is already assigned to the teacher
                    // if it is, skip to the next duty
                    // if it is not, assign the duty to the teacher
                    // if the teacher has no more duties needed, break the loop

                for (Day day : days) {
                    Duty[][] dutySchedule = day.getDutySchedule();
                    
                    // First pass: Try to assign non-Hall duties
                    for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                        for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                            Duty duty = dutySchedule[timeSlot][pos];
                            if (duty != null && !duty.getName().contains("Hall") && !teacher.hasDutyAssigned(duty)) {
                                if (numberOfDutiesNeeded > 0) {
                                    DayPattern pattern = getDayPattern(day.getDate().getDayOfWeek(), day.isDay1());
                                    int patternCount = termPatternGroups.get(term).get(pattern).size();
                                    
                                    // Check if assigning this duty would exceed the teacher's limit
                                    if (teacher.getDutiesThisSemester() + patternCount <= teacher.getMaxDutiesPerSemester()) {
                                        teacher.assignDuty(duty, patternCount);
                                        numberOfDutiesNeeded -= patternCount;
                                        System.out.println("Assigned duty: " + duty.getName() + " to teacher: " + teacher.getName() +
                                            " (worth " + patternCount + " duties, total now: " + teacher.getDutiesThisSemester() + ")");
                                    }
                                }
                            }
                        }
                    }
                    
                    // Second pass: Assign Hall duties if still needed
                    if (numberOfDutiesNeeded > 0) {
                        for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                            for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                                Duty duty = dutySchedule[timeSlot][pos];
                                if (duty != null && duty.getName().contains("Hall") && !teacher.hasDutyAssigned(duty)) {
                                    if (numberOfDutiesNeeded > 0) {
                                        DayPattern pattern = getDayPattern(day.getDate().getDayOfWeek(), day.isDay1());
                                        int patternCount = termPatternGroups.get(term).get(pattern).size();
                                        
                                        // Check if assigning this duty would exceed the teacher's limit
                                        if (teacher.getDutiesThisSemester() + patternCount <= teacher.getMaxDutiesPerSemester()) {
                                            teacher.assignDuty(duty, patternCount);
                                            numberOfDutiesNeeded -= patternCount;
                                            System.out.println("Assigned hall duty: " + duty.getName() + " to teacher: " + teacher.getName() +
                                                " (worth " + patternCount + " duties, total now: " + teacher.getDutiesThisSemester() + ")");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (numberOfDutiesNeeded == 0) {
                        break; // Exit if the teacher has no more duties needed
                    }
                }
                } // end of term loop
            } // end of semester loop
        } // end of teacher loop
        
        System.out.println("Duty assignment completed!");
    } // end of assignDuties
    
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