package com.jamesdegroot.calendar;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * Day.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    Day class, used to store the day and the duties that are assigned to the day
 *      
 *  Methods:
 * 	  - addDuty, void, adds a duty to the day
 *    - getDuties, Duty[], gets the duties for a specific time slot
 *    - getDuties, List<Duty>, gets all duties for the day
 *    - getDutySchedule, Duty[][], gets the entire duty schedule
 *    - getDate, LocalDate, gets the date of the day
 *    - setSchoolDay, void, sets the school day status of the day
 *    - setHoliday, void, sets the holiday status of the day
 *    - isSchoolDay, boolean, checks if the day is a school day
 *    - isHoliday, boolean, checks if the day is a holiday
 *    - isDay1, boolean, checks if the day is a Day 1 in the schedule rotation
 *    - getTermNumber, int, gets the term number for the day
 *    - toString, String, returns a string representation of the day
*/

public class Day {
    public static final int TIME_SLOTS = 11;  // Number of duty time slots (maybe 11)
    public static final int DUTIES_PER_SLOT = 1;  // Number of duties per time slot - easily changeable
    
    private LocalDate date;
    private Duty[][] dutySchedule; // 2D array [timeSlot][dutyPosition]
    private boolean isSchoolDay;
    private boolean isHoliday;

    /**
     * Constructs a new Day object.
     * @param date the date of the day
     */
    public Day(LocalDate date) {
        this.date = date;
        this.isSchoolDay = true;
        this.isHoliday = false;
        this.dutySchedule = new Duty[TIME_SLOTS][DUTIES_PER_SLOT];
    }

    /**
     * Adds a duty to a specific time slot and position.
     * @param timeSlot the time slot index (0-3)
     * @param position the position in the time slot (0-1)
     * @param duty the duty to add
     */
    public void addDuty(int timeSlot, int position, Duty duty) {
        if (timeSlot >= 0 && timeSlot < TIME_SLOTS && 
            position >= 0 && position < DUTIES_PER_SLOT) {
            dutySchedule[timeSlot][position] = duty;
        }
    }

    /**
     * Gets all duties for a specific time slot.
     * @param timeSlot the time slot index (0-3)
     * @return Array of duties for that time slot
     */
    public Duty[] getDuties(int timeSlot) {
        if (timeSlot >= 0 && timeSlot < TIME_SLOTS) {
            return dutySchedule[timeSlot];
        }
        return new Duty[DUTIES_PER_SLOT];
    }

    /**
     * Gets all duties for this day across all time slots
     * @return List of all duties
     */
    public List<Duty> getDuties() {
        List<Duty> allDuties = new ArrayList<>();
        for (Duty[] timeSlot : dutySchedule) {
            for (Duty duty : timeSlot) {
                if (duty != null) {
                    allDuties.add(duty);
                }
            }
        }
        return allDuties;
    }

    /**
     * Gets the entire duty schedule.
     * @return 2D array of duties
     */
    public Duty[][] getDutySchedule() {
        return dutySchedule;
    }

    /**
     * Gets the date of the day.
     * @return the date of the day
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the school day status of the day.
     * @param isSchoolDay the new school day status
     */
    public void setSchoolDay(boolean isSchoolDay) {
        this.isSchoolDay = isSchoolDay;
    }

    /**
     * Sets the holiday status of the day.
     * @param isHoliday the new holiday status
     */
    public void setHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    /**
     * Checks if the day is a school day.
     * @return true if the day is a school day, false otherwise
     */
    public boolean isSchoolDay() {
        return isSchoolDay;
    }

    /**
     * Checks if the day is a holiday.
     * @return true if the day is a holiday, false otherwise
     */
    public boolean isHoliday() {
        return isHoliday;
    }

    /**
     * Checks if this is a Day 1 in the schedule rotation
     * @return true if Day 1, false if Day 2
     */
    public boolean isDay1() {
        return com.jamesdegroot.duty_assigment.DutyAssignmentRules.isDay1(date);
    }

    /**
     * Gets the term number (0-3) for this day
     * @return term number (0 for Term 1, 1 for Term 2, etc)
     */
    public int getTermNumber() {
        LocalDate term1Start = LocalDate.of(2024, Month.SEPTEMBER, 3);
        LocalDate term2Start = LocalDate.of(2024, Month.NOVEMBER, 7);
        LocalDate term3Start = LocalDate.of(2025, Month.FEBRUARY, 1);
        LocalDate term4Start = LocalDate.of(2025, Month.APRIL, 8);
        LocalDate term4End = LocalDate.of(2025, Month.JUNE, 28);
        
        if (date.isBefore(term1Start) || date.isAfter(term4End)) {
            return 3; // Default to Term 4 if outside school year
        }
        
        if (date.isBefore(term2Start)) return 0;
        if (date.isBefore(term3Start)) return 1;
        if (date.isBefore(term4Start)) return 2;
        return 3;
    }

    /**
     * Returns a string representation of the day.
     * @return a string representation of the day
     */
    @Override
    public String toString() {
        return date.toString() + (isSchoolDay ? " (School Day)" : " (No School)") +
               (isHoliday ? " - Holiday" : "");
    }
}
