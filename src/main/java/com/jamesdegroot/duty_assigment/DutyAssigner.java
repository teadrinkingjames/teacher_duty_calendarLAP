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
        
        // Create templates for each term
        createTemplates(schoolDays);
        
        // Apply templates to all school days
        for (Day day : schoolDays) {
            applyTemplateToDay(day);
        }
    }
    
    /**
     * Creates duty schedule templates for each term
     */
    private void createTemplates(List<Day> schoolDays) {
        // Process each term
        for (int term = 0; term < 4; term++) {
            final int currentTerm = term;  // Make term effectively final for lambda
            
            // Reset duty counts for teachers at the start of each term
            for (Teacher teacher : teachers) {
                teacher.resetDutiesThisSemester();
            }
            
            // Get a sample week of days for this term
            List<Day> termDays = schoolDays.stream()
                .filter(day -> getTermNumber(day.getDate()) == currentTerm)
                .toList();
            
            if (!termDays.isEmpty()) {
                // Create templates for each weekday
                for (int dayOfWeek = 1; dayOfWeek <= 5; dayOfWeek++) {
                    final int currentDayOfWeek = dayOfWeek;
                    List<Day> daysForThisWeekday = termDays.stream()
                        .filter(day -> day.getDate().getDayOfWeek().getValue() == currentDayOfWeek)
                        .toList();
                    
                    if (!daysForThisWeekday.isEmpty()) {
                        Day sampleDay = daysForThisWeekday.get(0);
                        
                        // Create and assign duties for Day 1
                        Day day1Template = new Day(sampleDay.getDate());
                        Duty[][] originalDuties = sampleDay.getDutySchedule();
                        for (int timeSlot = 0; timeSlot < originalDuties.length; timeSlot++) {
                            for (int pos = 0; pos < originalDuties[timeSlot].length; pos++) {
                                if (originalDuties[timeSlot][pos] != null) {
                                    Duty templateDuty = new Duty(
                                        originalDuties[timeSlot][pos].getName(),
                                        originalDuties[timeSlot][pos].getDescription(),
                                        originalDuties[timeSlot][pos].getRoom(),
                                        originalDuties[timeSlot][pos].getTimeSlot()
                                    );
                                    day1Template.addDuty(timeSlot, pos, templateDuty);
                                }
                            }
                        }
                        assignDutiesForDay(day1Template, true);

                        // Create and assign duties for Day 2
                        Day day2Template = new Day(sampleDay.getDate());
                        for (int timeSlot = 0; timeSlot < originalDuties.length; timeSlot++) {
                            for (int pos = 0; pos < originalDuties[timeSlot].length; pos++) {
                                if (originalDuties[timeSlot][pos] != null) {
                                    Duty templateDuty = new Duty(
                                        originalDuties[timeSlot][pos].getName(),
                                        originalDuties[timeSlot][pos].getDescription(),
                                        originalDuties[timeSlot][pos].getRoom(),
                                        originalDuties[timeSlot][pos].getTimeSlot()
                                    );
                                    day2Template.addDuty(timeSlot, pos, templateDuty);
                                }
                            }
                        }
                        assignDutiesForDay(day2Template, false);

                        // Store both templates for this term and weekday
                        termTemplates[currentTerm].setDayTemplate(
                            sampleDay.getDate().getDayOfWeek(),
                            new Day[]{day1Template, day2Template}
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Applies the appropriate template to a given day
     */
    private void applyTemplateToDay(Day day) {
        int termNumber = getTermNumber(day.getDate());
        
        // Get the template for this day from the correct term
        Day[] templates = termTemplates[termNumber].getDayTemplate(day.getDate().getDayOfWeek());
        if (templates != null && templates.length > 0) {
            Day template = templates[0]; // Use the first template since it has both Day 1 and Day 2 assignments
            
            // Copy duties from template to target day
            Duty[][] templateDuties = template.getDutySchedule();
            for (int timeSlot = 0; timeSlot < templateDuties.length; timeSlot++) {
                for (int pos = 0; pos < templateDuties[timeSlot].length; pos++) {
                    if (templateDuties[timeSlot][pos] != null) {
                        Duty originalDuty = templateDuties[timeSlot][pos];
                        Duty newDuty = new Duty(
                            originalDuty.getName(),
                            originalDuty.getDescription(),
                            originalDuty.getRoom(),
                            originalDuty.getTimeSlot()
                        );
                        
                        // Copy both Day 1 and Day 2 teacher assignments from the template
                        for (String teacher : originalDuty.getDay1Teachers()) {
                            newDuty.addDay1Teacher(teacher);
                        }
                        for (String teacher : originalDuty.getDay2Teachers()) {
                            newDuty.addDay2Teacher(teacher);
                        }
                        
                        day.addDuty(timeSlot, pos, newDuty);
                    }
                }
            }
        }
    }
    
    /**
     * Determines which term a date falls into
     */
    private int getTermNumber(LocalDate date) {
        int month = date.getMonthValue();
        
        if (month >= Month.SEPTEMBER.getValue() && month <= Month.JANUARY.getValue()) {
            // Fall semester
            return month < Month.NOVEMBER.getValue() ? 
                DutyScheduleTemplate.TERM_1 : DutyScheduleTemplate.TERM_2;
        } else if (month >= Month.FEBRUARY.getValue() && month <= Month.JUNE.getValue()) {
            // Spring semester
            return month < Month.APRIL.getValue() ? 
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
               (month == Month.FEBRUARY && dayOfMonth <= 7) ||   // Term 3
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
     * Assigns duties for a specific day
     */
    private void assignDutiesForDay(Day day, boolean isDay1) {
        LocalDate date = day.getDate();
        Duty[][] dutySchedule = day.getDutySchedule();
        
        // For each time slot
        for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
            // For each duty position in the time slot
            for (int position = 0; position < dutySchedule[timeSlot].length; position++) {
                Duty duty = dutySchedule[timeSlot][position];
                if (duty != null) {
                    // Assign teachers for both Day 1 and Day 2
                    assignTeacherToDuty(duty, timeSlot, date, true);  // Day 1
                    assignTeacherToDuty(duty, timeSlot, date, false); // Day 2
                }
            }
        }
    }
    
    /**
     * Assigns a teacher to a specific duty
     */
    private void assignTeacherToDuty(Duty duty, int timeSlot, LocalDate date, boolean isDay1) {
        List<Teacher> availableTeachers = findAvailableTeachers(timeSlot, date, isDay1);
        
        if (!availableTeachers.isEmpty()) {
            // Get number of teachers needed for this duty
            int teachersNeeded = 1;  // One teacher per duty
            
            // Sort teachers by number of duties (ascending)
            availableTeachers.sort((t1, t2) -> 
                Integer.compare(t1.getDutiesThisSemester(), t2.getDutiesThisSemester()));
            
            // Assign up to teachersNeeded teachers, prioritizing those with fewer duties
            for (int i = 0; i < Math.min(teachersNeeded, availableTeachers.size()); i++) {
                Teacher selectedTeacher = availableTeachers.get(i);
                
                // Assign the teacher to the duty
                if (isDay1) {
                    duty.addDay1Teacher(selectedTeacher.getName());
                } else {
                    duty.addDay2Teacher(selectedTeacher.getName());
                }
                
                // Increment the teacher's duty count
                selectedTeacher.incrementDutiesThisSemester();
            }
        }
    }
    
    /**
     * Finds available teachers for a duty
     */
    private List<Teacher> findAvailableTeachers(int timeSlot, LocalDate date, boolean isDay1) {
        List<Teacher> availableTeachers = new ArrayList<>();
        
        for (Teacher teacher : teachers) {
            if (DutyAssignmentRules.canAssignDuty(teacher, timeSlot, date, isDay1)) {
                availableTeachers.add(teacher);
            }
        }
        
        return availableTeachers;
    }
} 