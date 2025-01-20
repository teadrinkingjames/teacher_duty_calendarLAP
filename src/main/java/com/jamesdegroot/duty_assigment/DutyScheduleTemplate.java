package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

/**
 * DutyScheduleTemplate.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    DutyScheduleTemplate class, used to store the duty schedule template for a term
 *      
 *  Methods:
 * 	  - DutyScheduleTemplate, constructor, creates a duty schedule template object with the given term number
 *    - getDayTemplate, Day[], gets the day template for a specific day of the week
 *    - setDayTemplate, void, sets the day template for a specific day of the week
 *    - getTermNumber, int, gets the term number for the duty schedule template
 *    - applyTemplate, void, applies the duty schedule template to a target day
*/

public class DutyScheduleTemplate {
    // Store duty schedules for each day of the week
    private final Map<DayOfWeek, Day[]> weeklySchedule;
    private final int termNumber;
    
    public DutyScheduleTemplate(int termNumber) {
        this.termNumber = termNumber;
        this.weeklySchedule = new HashMap<>();
        initializeWeeklySchedule();
    }
    
    /**
     * Initializes the weekly schedule for each day of the week
     */
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
    
    /**
     * Gets the day template for a specific day of the week
     * 
     * @param dayOfWeek The day of the week
     * @return The day template for the given day of the week
     */
    public Day[] getDayTemplate(DayOfWeek dayOfWeek) {
        return weeklySchedule.get(dayOfWeek);
    }
    
    /**
     * Sets the day template for a specific day of the week
     * 
     * @param dayOfWeek The day of the week
     * @param rotationDays The day template for the given day of the week
     */
    public void setDayTemplate(DayOfWeek dayOfWeek, Day[] rotationDays) {
        if (rotationDays.length == 2) {
            weeklySchedule.put(dayOfWeek, rotationDays);
        }
    }
    
    /**
     * Gets the term number for the duty schedule template
     * 
     * @return The term number for the duty schedule template
     */
    public int getTermNumber() {
        return termNumber;
    }
    
    /**
     * Copies duties from a template day to a target day
     * 
     * @param targetDay The target day to copy the duties to
     * @param isDay1 Whether the day is Day 1
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