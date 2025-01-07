package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.time.DayOfWeek;
import java.util.Collections;

public class DutyAssigner {
    // Calendar and teacher references
    private final Calendar calendar;
    private final List<Teacher> teachers;
    private final DutyScheduleTemplate[] termTemplates;
    
    // Random number generator for teacher selection
    private final Random random = new Random();
    
    public DutyAssigner(Calendar calendar, List<Teacher> teachers) {
        this.calendar = calendar;
        this.teachers = teachers;
        this.termTemplates = new DutyScheduleTemplate[4];
        initializeTemplates();
    }
    
    private void initializeTemplates() {
        for (int i = 0; i < 4; i++) {
            termTemplates[i] = new DutyScheduleTemplate(i);
        }
    }
    
    /**
     * Main method to assign duties for the entire semester
     */
    public void assignDuties() {
        List<Day> schoolDays = getSchoolDays();
        System.out.println("Total school days: " + schoolDays.size());
        
        // First, create templates for all terms
        for (int termIndex = 0; termIndex < 4; termIndex++) {
            final int term = termIndex;
            List<Day> termDays = schoolDays.stream()
                .filter(day -> getTermNumber(day.getDate()) == term)
                .toList();
            
            System.out.println("Term " + (term + 1) + " has " + termDays.size() + " school days");
            
            if (!termDays.isEmpty()) {
                createTemplateForTerm(termDays, term);
            }
        }
        
        // Process each term separately
        for (int termIndex = 0; termIndex < 4; termIndex++) {
            final int currentTerm = termIndex;
            System.out.println("\nProcessing Term " + (currentTerm + 1) + "...");
            
            // Reset all duty counts for this term
            for (Teacher teacher : teachers) {
                teacher.resetDutiesThisSemester();
            }
            
            List<Day> termDays = schoolDays.stream()
                .filter(day -> getTermNumber(day.getDate()) == currentTerm)
                .toList();
                
            if (termDays.isEmpty()) {
                continue;
            }
            
            boolean termProgress;
            int termPass = 0;
            int maxTermPasses = 50;
            int noProgressCount = 0;
            int maxNoProgressAllowed = 10;
            
            do {
                termProgress = false;
                termPass++;
                
                // Process days in sequential order first, then random order
                List<Day> orderedDays = new ArrayList<>(termDays);
                if (termPass > 4) {
                    Collections.shuffle(orderedDays);
                }
                
                // First pass: non-Hall duties
                for (Day day : orderedDays) {
                    boolean dayProgress = assignDutiesForDay(day, currentTerm);
                    termProgress = termProgress || dayProgress;
                }
                
                if (!termProgress) {
                    noProgressCount++;
                } else {
                    noProgressCount = 0;
                }
                
                // Print progress every 5 passes
                if (termPass % 5 == 0) {
                    System.out.println("Term " + (currentTerm + 1) + " - Pass " + termPass + 
                        (termProgress ? " (made progress)" : " (no progress)"));
                    printTeacherSummary();
                }
                
            } while (termPass < maxTermPasses && noProgressCount < maxNoProgressAllowed && !allDutiesFilled(termDays));
            
            System.out.println("Completed Term " + (currentTerm + 1));
        }
        
        System.out.println("\nDuty assignment completed!");
    }
    
    private void printTeacherSummary() {
        int totalTeachers = 0;
        int teachersAtMax = 0;
        int totalAssignedDuties = 0;
        int totalRequiredDuties = 0;
        
        for (Teacher teacher : teachers) {
            if (teacher.getMaxDutiesPerSemester() > 0) {
                totalTeachers++;
                totalAssignedDuties += teacher.getDutiesThisSemester();
                totalRequiredDuties += teacher.getMaxDutiesPerSemester();
                if (teacher.getDutiesThisSemester() >= teacher.getMaxDutiesPerSemester()) {
                    teachersAtMax++;
                }
            }
        }
        
        System.out.println(String.format("Progress: %d/%d teachers at max, %d/%d total duties assigned (%.1f%%)",
            teachersAtMax, totalTeachers, totalAssignedDuties, totalRequiredDuties,
            (totalAssignedDuties * 100.0) / totalRequiredDuties));
    }
    
    private boolean allTeachersAtMaxDuties() {
        return teachers.stream().allMatch(t -> 
            (t.getDutiesThisSemester() * 2) >= t.getMaxDutiesPerSemester() || 
            t.getMaxDutiesPerSemester() == 0
        );
    }
    
    /**
     * Creates duty schedule template for a specific term
     */
    private void createTemplateForTerm(List<Day> termDays, final int term) {
        // Initialize template for this term if needed
        if (termTemplates[term] == null) {
            termTemplates[term] = new DutyScheduleTemplate(term);
        }
        
        // Create templates for each weekday
        for (int dayOfWeek = 1; dayOfWeek <= 5; dayOfWeek++) {
            final int currentDayOfWeek = dayOfWeek;
            List<Day> daysForThisWeekday = termDays.stream()
                .filter(d -> d.getDate().getDayOfWeek().getValue() == currentDayOfWeek)
                .toList();
                
            if (!daysForThisWeekday.isEmpty()) {
                Day sampleDay = daysForThisWeekday.get(0);
                createDayTemplates(sampleDay, term, currentDayOfWeek);
            }
        }
    }
    
    /**
     * Assigns duties for a specific day
     * @return true if any assignments were made
     */
    private boolean assignDutiesForDay(Day day, final int term) {
        LocalDate date = day.getDate();
        Duty[][] dutySchedule = day.getDutySchedule();
        boolean madeAssignment = false;
        
        // First pass: Assign non-Hall duties
        for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
            for (int position = 0; position < dutySchedule[timeSlot].length; position++) {
                Duty duty = dutySchedule[timeSlot][position];
                if (duty != null && !duty.getName().contains("Hall")) {
                    if (duty.getDay1Teachers().isEmpty()) {
                        boolean day1Assigned = assignTeacherToDuty(duty, timeSlot, date, true, term);
                        madeAssignment = madeAssignment || day1Assigned;
                    }
                    if (duty.getDay2Teachers().isEmpty()) {
                        boolean day2Assigned = assignTeacherToDuty(duty, timeSlot, date, false, term);
                        madeAssignment = madeAssignment || day2Assigned;
                    }
                }
            }
        }
        
        // Second pass: Assign Hall duties
        for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
            for (int position = 0; position < dutySchedule[timeSlot].length; position++) {
                Duty duty = dutySchedule[timeSlot][position];
                if (duty != null && duty.getName().contains("Hall")) {
                    if (duty.getDay1Teachers().isEmpty()) {
                        boolean day1Assigned = assignTeacherToDuty(duty, timeSlot, date, true, term);
                        madeAssignment = madeAssignment || day1Assigned;
                    }
                    if (duty.getDay2Teachers().isEmpty()) {
                        boolean day2Assigned = assignTeacherToDuty(duty, timeSlot, date, false, term);
                        madeAssignment = madeAssignment || day2Assigned;
                    }
                }
            }
        }
        
        return madeAssignment;
    }
    
    /**
     * Assigns a teacher to a specific duty
     * @return true if an assignment was made
     */
    private boolean assignTeacherToDuty(Duty duty, int timeSlot, LocalDate date, boolean isDay1, int term) {
        List<Teacher> availableTeachers = findAvailableTeachers(timeSlot, date, isDay1);
        
        if (!availableTeachers.isEmpty()) {
            // Sort teachers by percentage of max duties completed (ascending)
            availableTeachers.sort((t1, t2) -> {
                // Handle null cases first
                if (t1 == null) return -1;
                if (t2 == null) return 1;
                
                // Calculate duty percentages
                double t1Percent = t1.getMaxDutiesPerSemester() == 0 ? 1.0 : 
                    (double)t1.getDutiesThisSemester() / t1.getMaxDutiesPerSemester();
                double t2Percent = t2.getMaxDutiesPerSemester() == 0 ? 1.0 : 
                    (double)t2.getDutiesThisSemester() / t2.getMaxDutiesPerSemester();
                
                // First compare by duty percentage
                int percentComparison = Double.compare(t1Percent, t2Percent);
                if (Math.abs(t1Percent - t2Percent) > 0.2) { // Only use percentage if significant difference
                    return percentComparison;
                }
                
                // If percentages are similar, prioritize teachers with more remaining duties
                int remainingDuties1 = t1.getMaxDutiesPerSemester() - t1.getDutiesThisSemester();
                int remainingDuties2 = t2.getMaxDutiesPerSemester() - t2.getDutiesThisSemester();
                int dutiesComparison = Integer.compare(remainingDuties2, remainingDuties1);
                if (dutiesComparison != 0) {
                    return dutiesComparison;
                }
                
                // If still equal, use teacher names to ensure consistent ordering
                return t1.getName().compareTo(t2.getName());
            });
            
            // Get the teacher with the lowest percentage of completed duties
            Teacher selectedTeacher = availableTeachers.stream()
                .filter(t -> t.getDutiesThisSemester() < t.getMaxDutiesPerSemester())
                .findFirst()
                .orElse(null);
                
            if (selectedTeacher != null) {
                // Check if the teacher already has this duty assigned
                boolean alreadyAssigned = false;
                if (isDay1 && duty.getDay1Teachers().contains(selectedTeacher.getName())) {
                    alreadyAssigned = true;
                } else if (!isDay1 && duty.getDay2Teachers().contains(selectedTeacher.getName())) {
                    alreadyAssigned = true;
                }
                
                if (!alreadyAssigned) {
                    // Assign the teacher to the duty
                    if (isDay1) {
                        duty.addDay1Teacher(selectedTeacher.getName());
                    } else {
                        duty.addDay2Teacher(selectedTeacher.getName());
                    }
                    
                    // Increment both term and semester duty counts
                    selectedTeacher.incrementDutiesForTerm(term);
                    selectedTeacher.incrementDutiesThisSemester();
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Creates Day 1 and Day 2 templates for a specific day
     */
    private void createDayTemplates(Day sampleDay, int term, int dayOfWeek) {
        // Create and assign duties for Day 1
        Day day1Template = new Day(sampleDay.getDate());
        Duty[][] originalDuties = sampleDay.getDutySchedule();
        copyDutiesToTemplate(originalDuties, day1Template);
        
        // Create and assign duties for Day 2
        Day day2Template = new Day(sampleDay.getDate());
        copyDutiesToTemplate(originalDuties, day2Template);
        
        // Store both templates
        termTemplates[term].setDayTemplate(
            sampleDay.getDate().getDayOfWeek(),
            new Day[]{day1Template, day2Template}
        );
    }
    
    /**
     * Helper method to copy duties to a template
     */
    private void copyDutiesToTemplate(Duty[][] originalDuties, Day template) {
        for (int timeSlot = 0; timeSlot < originalDuties.length; timeSlot++) {
            for (int pos = 0; pos < originalDuties[timeSlot].length; pos++) {
                if (originalDuties[timeSlot][pos] != null) {
                    Duty templateDuty = new Duty(
                        originalDuties[timeSlot][pos].getName(),
                        originalDuties[timeSlot][pos].getDescription(),
                        originalDuties[timeSlot][pos].getRoom(),
                        originalDuties[timeSlot][pos].getTimeSlot()
                    );
                    template.addDuty(timeSlot, pos, templateDuty);
                }
            }
        }
    }
    
    /**
     * Determines which term a date falls into
     */
    private int getTermNumber(LocalDate date) {
        int month = date.getMonthValue();
        
        if (month >= Month.SEPTEMBER.getValue() && month <= Month.DECEMBER.getValue()) {
            // Fall semester
            return month <= Month.OCTOBER.getValue() ? 
                DutyScheduleTemplate.TERM_1 : DutyScheduleTemplate.TERM_2;
        } else if (month >= Month.JANUARY.getValue() && month <= Month.JUNE.getValue()) {
            // Spring semester
            return month <= Month.MARCH.getValue() ? 
                DutyScheduleTemplate.TERM_3 : DutyScheduleTemplate.TERM_4;
        } else {
            // Summer months - default to last term
            return DutyScheduleTemplate.TERM_4;
        }
    }
    
    /**
     * Checks if a date is in the first week of its term
     */
    private boolean isFirstWeekOfTerm(LocalDate date) {
        int dayOfMonth = date.getDayOfMonth();
        Month month = date.getMonth();
        
        return (month == Month.SEPTEMBER && dayOfMonth <= 7) ||  // Term 1
               (month == Month.NOVEMBER && dayOfMonth <= 7) ||   // Term 2
               (month == Month.JANUARY && dayOfMonth <= 7) ||    // Term 3
               (month == Month.APRIL && dayOfMonth <= 7);        // Term 4
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
     * Finds available teachers for a duty
     */
    private List<Teacher> findAvailableTeachers(int timeSlot, LocalDate date, boolean isDay1) {
        List<Teacher> availableTeachers = new ArrayList<>();
        
        // Find the Day object for this date
        Day day = calendar.getDaysOfYear().stream()
            .filter(d -> d.getDate().equals(date))
            .findFirst()
            .orElse(null);
            
        if (day != null) {
            // Get all days in the same week
            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = date.with(DayOfWeek.FRIDAY);
            List<Day> daysInWeek = calendar.getDaysOfYear().stream()
                .filter(d -> !d.getDate().isBefore(weekStart) && !d.getDate().isAfter(weekEnd))
                .toList();
            
            for (Teacher teacher : teachers) {
                if (DutyAssignmentRules.canAssignDuty(teacher, timeSlot, day, isDay1, daysInWeek)) {
                    availableTeachers.add(teacher);
                }
            }
        }
        
        return availableTeachers;
    }
    
    private boolean allDutiesFilled(List<Day> days) {
        for (Day day : days) {
            Duty[][] duties = day.getDutySchedule();
            for (Duty[] timeSlot : duties) {
                for (Duty duty : timeSlot) {
                    if (duty != null) {
                        if (duty.getDay1Teachers().isEmpty() || duty.getDay2Teachers().isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private boolean allTermsHaveDutiesFilled(List<Day> allDays) {
        for (int term = 0; term < 4; term++) {
            final int currentTerm = term;
            List<Day> termDays = allDays.stream()
                .filter(day -> getTermNumber(day.getDate()) == currentTerm)
                .toList();
            
            if (!termDays.isEmpty() && !allDutiesFilled(termDays)) {
                return false;
            }
        }
        return true;
    }
} 