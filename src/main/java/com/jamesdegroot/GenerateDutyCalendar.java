package com.jamesdegroot;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import com.jamesdegroot.calendar.Holiday;
import com.jamesdegroot.duty_assigment.DutyAssigner;
import com.jamesdegroot.io.ReadCalendarFromDisk;
import com.jamesdegroot.io.ReadTeachersFromDisk;
import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.teacher.TeacherScheduleStatusEnum;
import com.jamesdegroot.teacher.TeacherTypeEnum;

/**
 * GenerateDutyCalendar.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    GenerateDutyCalendar class, used to generate a duty calendar for a school year
 *      
 *  Methods:
 * 	  - processFile, void, processes a teacher schedule file and updates the teacher list
 *    - loadCalendar, void, loads and processes an ICS calendar file
 *    - printSummary, void, prints a summary of the teacher schedule statuses
 *    - getTeachers, List<Teacher>, gets the list of teachers
 *    - isSchoolDay, boolean, checks if a given date is a school day
 *    - printSchedule, void, prints the complete duty schedule
 *    - printTeacherDutyCounts, void, prints a summary of teachers who haven't reached their maximum duties
 * 
*/

public class GenerateDutyCalendar {
    public static final int NUM_OF_SEPERATORS_CHAR = 135; // maximum number of duties per semester
    private List<Teacher> teachers;
    private Calendar calendar;
    private List<String> warnings = new ArrayList<>();
    private DutyAssigner dutyAssigner;
    
    /**
     * Creates a new GenerateDutyCalendar instance.
     * Initializes empty lists for teachers and warnings.
     */
    public GenerateDutyCalendar() {
        teachers = new ArrayList<>();
        calendar = new Calendar();
    }
    
    /**
     * Processes a teacher schedule file and updates the teacher list.
     * @param file The CSV file containing teacher schedules
     */
    public void processFile(File file) {
        teachers = ReadTeachersFromDisk.readTeachersNames(file.getAbsolutePath());
    }
    
    /**
     * Loads and processes an ICS calendar file.
     * Finds the start and end dates of the school year.
     * @param file The ICS file to process
     */
    public void loadCalendar(File file) {
        ReadCalendarFromDisk.loadCalendarFromICS(calendar, file);
        System.out.println("Calendar loaded with " + calendar.getEvents().size() + " events");
        
        // Debug output for holidays
        System.out.println("\nHolidays loaded:");
        calendar.getEvents().forEach(event -> {
            System.out.println(String.format("%s: %s to %s", 
                event.getSummary(), 
                event.getStartDate(), 
                event.getEndDate()));
        });
        System.out.println();
        
        // Set start date to September 3rd
        LocalDate startDate = LocalDate.of(2024, 9, 3);
        
        // Find the "Last Day of School" event
        LocalDate endDate = calendar.getEvents().stream()
            .filter(event -> event.getSummary().contains("Last Day of School"))
            .map(Holiday::getStartDate)
            .findFirst()
            .orElse(LocalDate.of(2025, 6, 28));
        
        System.out.println("School Year Start Date: " + startDate);
        System.out.println("Last Day of School: " + endDate);
        
        // Initialize the calendar days before printing
        calendar.initializeDaysOfYear();
    }
    
    /**
     * Prints a summary of the teacher schedule statuses.
     * Debugging method, prints alot of information about the teachers and their schedules
     */
    @SuppressWarnings("unused") // not currently used, commented out as it is not needed
    private void printSummary() {
        System.out.println("\nDetailed Teacher Schedules:");
        System.out.println("=".repeat(NUM_OF_SEPERATORS_CHAR));
        
        // Print individual teacher schedules
        teachers.forEach(teacher -> {
            System.out.println(teacher.toString());
            System.out.println("-".repeat(NUM_OF_SEPERATORS_CHAR));
        });
        
        // Print summary statistics
        System.out.println("\nTeacher Summary Statistics:");
        System.out.println("=".repeat(NUM_OF_SEPERATORS_CHAR));
        System.out.println("Total Teachers: " + teachers.size());
        
        long fullTime = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.OVER_FULL_TIME)
            .peek(t -> {
                if (t.getName() != null && !t.getName().trim().isEmpty()) {
                    warnings.add(t.getName() + " has " + t.getFilledPeriods() + " classes (over full-time load)");
                }
            })
            .count() + 
            teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.FULL_TIME)
            .count();
        long fiveSixths = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.FIVE_SIXTHS)
            .count();
        long fourSixths = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.FOUR_SIXTHS)
            .count();
        long threeSixths = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.THREE_SIXTHS)
            .count();
        long twoSixths = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.TWO_SIXTHS)
            .count();
        long oneSixth = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.ONE_SIXTH)
            .count();
        long noLoad = teachers.stream()
            .filter(t -> t.getClassScheduleStatus() == TeacherScheduleStatusEnum.NO_LOAD)
            .count();
            
        System.out.println("Full Time (6/6): " + fullTime);
        System.out.println("Five Sixths (5/6): " + fiveSixths);
        System.out.println("Four Sixths (4/6): " + fourSixths);
        System.out.println("Three Sixths (3/6): " + threeSixths);
        System.out.println("Two Sixths (2/6): " + twoSixths);
        System.out.println("One Sixth (1/6): " + oneSixth);
        System.out.println("No Load (0/6): " + noLoad);
        
        // Add teacher type breakdown
        System.out.println("\nTeacher Type Breakdown:");
        System.out.println("-".repeat(NUM_OF_SEPERATORS_CHAR));
        
        long regularCount = teachers.stream()
            .filter(t -> t.getJobType() == TeacherTypeEnum.REGULAR)
            .count();
        long coopCount = teachers.stream()
            .filter(t -> t.getJobType() == TeacherTypeEnum.COOP)
            .count();
        long gymCount = teachers.stream()
            .filter(t -> t.getJobType() == TeacherTypeEnum.GYM)
            .count();
        long guidanceCount = teachers.stream()
            .filter(t -> t.getJobType() == TeacherTypeEnum.GUIDANCE)
            .count();
        long creditRecoveryCount = teachers.stream()
            .filter(t -> t.getJobType() == TeacherTypeEnum.CREDIT_RECOVERY)
            .count();
            
        System.out.println("Regular Teachers: " + regularCount);
        System.out.println("Co-op Teachers: " + coopCount);
        System.out.println("Gym Teachers: " + gymCount);
        System.out.println("Guidance: " + guidanceCount);
        System.out.println("Credit Recovery: " + creditRecoveryCount);
        
        // Print warnings only if they exist
        if (!warnings.isEmpty()) {
            System.out.println("\nWarnings:");
            System.out.println("-".repeat(NUM_OF_SEPERATORS_CHAR));
            warnings.forEach(System.out::println);
            warnings.clear();
        }
    }
    
    public List<Teacher> getTeachers() {
        return teachers;
    }
    
    /**
     * Checks if a given date is a school day.
     * @param date the date to check
     * @return true if the date is a school day, false otherwise
     */
    public boolean isSchoolDay(LocalDate date) {
        return calendar.isSchoolDay(date);
    }

    public Calendar getCalendar() {
        return calendar;
    }
    
    /**
     * Assigns duties to teachers across all terms
     */
    public void assignDuties() {
        dutyAssigner = new DutyAssigner(calendar, teachers);
        dutyAssigner.assignDuties();
        dutyAssigner.printDutySchedule();
        // Debugging method, prints a summary of the teachers and their duties
        //printTeacherDutyCounts();
    }
    
    /**
     * Prints the complete duty schedule
     */
    public void printSchedule() {
        // Define term dates
        LocalDate[][] termDates = {
            {LocalDate.of(2024, 9, 3), LocalDate.of(2024, 11, 7)},    // Term 1
            {LocalDate.of(2024, 11, 8), LocalDate.of(2025, 2, 1)},    // Term 2
            {LocalDate.of(2025, 2, 2), LocalDate.of(2025, 4, 4)},     // Term 3
            {LocalDate.of(2025, 4, 5), LocalDate.of(2025, 6, 28)}      // Term 4
        };
        // print the number of days in each term
        System.out.println("Number of days in each term:");
        System.out.println("Term 1: " + (termDates[0][1].getDayOfYear() - termDates[0][0].getDayOfYear()) + 1);
        System.out.println("Term 2: " + (termDates[1][1].getDayOfYear() - termDates[1][0].getDayOfYear()) + 1);
        System.out.println("Term 3: " + (termDates[2][1].getDayOfYear() - termDates[2][0].getDayOfYear()) + 1);
        System.out.println("Term 4: " + (termDates[3][1].getDayOfYear() - termDates[3][0].getDayOfYear()) + 1);
        
        String[] termNames = {
            "Term 1 (Fall Term 1)",
            "Term 2 (Fall Term 2)",
            "Term 3 (Spring Term 1)",
            "Term 4 (Spring Term 2)"
        };
        
        // Print schedule for each term
        for (int term = 0; term < 4; term++) {
            final int currentTerm = term;  // Make term effectively final for lambda
            System.out.println("\n" + termNames[currentTerm] + " Duty Schedule:");
            System.out.println("=".repeat(NUM_OF_SEPERATORS_CHAR));
            
            // Print header
            System.out.printf("%-15s | %-20s | %-30s | %-30s%n", 
                "Day of Week", "Duty", "Day 1 Teachers", "Day 2 Teachers");
            System.out.println("-".repeat(NUM_OF_SEPERATORS_CHAR));
            
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
                                System.out.printf("%-15s | %-20s | %-30s | %-30s%n",
                                    weekdayName,
                                    duty.getName(),
                                    String.join(", ", duty.getDay1Teachers()),
                                    String.join(", ", duty.getDay2Teachers()));
                            }
                        }
                    }
                    
                    System.out.println("-".repeat(NUM_OF_SEPERATORS_CHAR));
                }
            }
        }
    }
    
    /**
     * Prints a summary of teachers who haven't reached their maximum duties
     * Debugging method, prints information about the teachers and their duties/max duties
     * may be inaccurate as the dutiesthissemester variable is reset multiple times, 
     * but it is a good way to see how many duties each teacher has
     */
    @SuppressWarnings("unused") // not currently used, commented out as it is not needed
    private void printTeacherDutyCounts() {
        List<Teacher> teachersUnderMax = teachers.stream()
            .filter(t -> t.getDutiesThisSemester() < t.getMaxDutiesPerSemester())
            .collect(Collectors.toList());
            
        if (!teachersUnderMax.isEmpty()) {
            System.out.println("\nTeachers Under Maximum Duties:");
            System.out.println("=".repeat(50));
            System.out.printf("%-30s | %s/%s%n", "Teacher Name", "Current", "Max");
            System.out.println("-".repeat(50));
            
            for (Teacher teacher : teachersUnderMax) {
                System.out.printf("%-30s | %d/%d%n", 
                    teacher.getName(),
                    teacher.getDutiesThisSemester(),
                    teacher.getMaxDutiesPerSemester());
            }
            System.out.println("=".repeat(50));
        }
    }
}