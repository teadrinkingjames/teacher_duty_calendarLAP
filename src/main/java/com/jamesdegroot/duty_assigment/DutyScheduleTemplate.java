package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class DutyScheduleTemplate {
    // Constants for terms
    public static final int TERM_1 = 0;  // Fall Term 1
    public static final int TERM_2 = 1;  // Fall Term 2
    public static final int TERM_3 = 2;  // Spring Term 1
    public static final int TERM_4 = 3;  // Spring Term 2
    
    // Store duty schedules for each day of the week
    private final Map<DayOfWeek, Day[]> weeklySchedule;
    private final int termNumber;
    
    public DutyScheduleTemplate(int termNumber) {
        this.termNumber = termNumber;
        this.weeklySchedule = new HashMap<>();
        initializeWeeklySchedule();
    }
    
    private void initializeWeeklySchedule() {
        // Initialize a template for each weekday
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                // Create two days for Day1/Day2 rotation
                Day[] rotationDays = new Day[2];
                rotationDays[0] = new Day(null); // Day1 template
                rotationDays[1] = new Day(null); // Day2 template
                weeklySchedule.put(day, rotationDays);
            }
        }
    }
    
    public Day[] getDayTemplate(DayOfWeek dayOfWeek) {
        return weeklySchedule.get(dayOfWeek);
    }
    
    public void setDayTemplate(DayOfWeek dayOfWeek, Day[] rotationDays) {
        if (rotationDays.length == 2) {
            weeklySchedule.put(dayOfWeek, rotationDays);
        }
    }
    
    public int getTermNumber() {
        return termNumber;
    }
    
    /**
     * Copies duties from a template day to a target day
     */
    public void applyTemplate(Day targetDay, boolean isDay1) {
        DayOfWeek dayOfWeek = targetDay.getDate().getDayOfWeek();
        Day[] template = weeklySchedule.get(dayOfWeek);
        
        if (template != null) {
            Day templateDay = template[isDay1 ? 0 : 1];
            Duty[][] templateDuties = templateDay.getDutySchedule();
            
            // Copy duties from template to target
            for (int timeSlot = 0; timeSlot < templateDuties.length; timeSlot++) {
                for (int pos = 0; pos < templateDuties[timeSlot].length; pos++) {
                    Duty templateDuty = templateDuties[timeSlot][pos];
                    if (templateDuty != null) {
                        // Create a new duty with the same basic info
                        Duty newDuty = new Duty(
                            templateDuty.getName(),
                            templateDuty.getDescription(),
                            templateDuty.getRoom(),
                            templateDuty.getTimeSlot()
                        );
                        
                        // Copy the appropriate teacher assignments based on Day 1/2
                        if (isDay1) {
                            templateDuty.getDay1Teachers().forEach(newDuty::addDay1Teacher);
                        } else {
                            templateDuty.getDay2Teachers().forEach(newDuty::addDay2Teacher);
                        }
                        
                        // Add the duty to the target day
                        targetDay.addDuty(timeSlot, pos, newDuty);
                    }
                }
            }
        }
    }
} 