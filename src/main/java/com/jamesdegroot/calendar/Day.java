package com.jamesdegroot.calendar;

import java.time.LocalDate;

/**
 * Represents a day in the calendar.
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
     * Returns a string representation of the day.
     * @return a string representation of the day
     */
    @Override
    public String toString() {
        return date.toString() + (isSchoolDay ? " (School Day)" : " (No School)") +
               (isHoliday ? " - Holiday" : "");
    }
}
