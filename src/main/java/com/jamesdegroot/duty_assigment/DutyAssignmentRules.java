package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.time.Month;

public class DutyAssignmentRules {
    // Constants for duty limits
    private static final int MAX_DUTIES_PER_WEEK = 1;
    private static final int MAX_CONSECUTIVE_DAYS = 1;
    
    // Day rotation constants
    private static final String DAY_1_IDENTIFIER = "D1";
    private static final String DAY_2_IDENTIFIER = "D2";
    
    // Time slot constants
    private static final int PERIOD_1_SLOT = 0;
    private static final int PERIOD_2_SLOT = 1;
    private static final int LUNCH_A_SLOT = 2;
    private static final int LUNCH_B_SLOT = 3;
    private static final int PERIOD_3_SLOT = 4;
    private static final int PERIOD_4_SLOT = 5;
    
    /**
     * Determines if it's a Day 1 or Day 2 based on the date
     * @param date The date to check
     * @return true if it's Day 1, false if Day 2
     */
    public static boolean isDay1(LocalDate date) {
        return date.getDayOfMonth() % 2 != 0; // Odd days are Day 1
    }
    
    /**
     * Gets the day rotation identifier for a given date
     * @param date The date to check
     * @return "D1" or "D2" depending on the date
     */
    public static String getDayRotation(LocalDate date) {
        return isDay1(date) ? DAY_1_IDENTIFIER : DAY_2_IDENTIFIER;
    }
    
    /**
     * Checks if a teacher can be assigned a duty based on their schedule
     * @param teacher The teacher to check
     * @param timeSlot The time slot for the duty
     * @param date The date of the duty
     * @param isDay1Duty Whether this is a Day 1 duty assignment
     * @return true if the teacher can be assigned the duty
     */
    public static boolean canAssignDuty(Teacher teacher, int timeSlot, LocalDate date, boolean isDay1Duty) {
        // Skip weekends
        if (isWeekend(date)) {
            return false;
        }
        
        // Check if teacher has classes during the duty time slot
        if (hasClassDuringTimeSlot(teacher, timeSlot)) {
            return false;
        }
        
        // Check if teacher has reached their maximum duties
        if (teacher.getDutiesThisSemester() >= teacher.getMaxDutiesPerSemester()) {
            return false;
        }
        
        // Check if teacher has any classes in this term
        if (!hasClassesInTerm(teacher, date)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the given date is a weekend
     */
    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
    
    /**
     * Checks if teacher has a class during the specified time slot
     */
    private static boolean hasClassDuringTimeSlot(Teacher teacher, int timeSlot) {
        List<String> schedule = teacher.getSchedule();
        if (timeSlot >= 0 && timeSlot < schedule.size()) {
            return !schedule.get(timeSlot).trim().isEmpty();
        }
        return false;
    }
    
    /**
     * Checks if teacher has classes in adjacent periods
     */
    private static boolean hasAdjacentClasses(Teacher teacher, int timeSlot) {
        List<String> schedule = teacher.getSchedule();
        
        // Check period before (if not first period)
        if (timeSlot > 0 && !schedule.get(timeSlot - 1).trim().isEmpty()) {
            return true;
        }
        
        // Check period after (if not last period)
        if (timeSlot < schedule.size() - 1 && !schedule.get(timeSlot + 1).trim().isEmpty()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if teacher has exceeded their duty limit for the given date
     */
    private static boolean exceedsDutyLimit(Teacher teacher, LocalDate date) {
        // Check if teacher has reached their maximum duties for the semester
        if (teacher.getDutiesThisSemester() >= teacher.getMaxDutiesPerSemester()) {
            return true;
        }
        
        // Check weekly duty count
        // For now, we'll just use the semester count as a proxy for weekly count
        if (teacher.getDutiesThisSemester() > MAX_DUTIES_PER_WEEK * 2) { // Allow for both Day 1 and Day 2
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if teacher already has a duty on the opposite day rotation
     */
    private static boolean hasOppositeDayDuty(Teacher teacher, LocalDate date, boolean isDay1Duty) {
        // Allow teachers to have duties on both Day 1 and Day 2
        return false;
    }
    
    /**
     * Maps a time period to its corresponding duty slot
     */
    public static int getTimeSlot(String period) {
        switch (period.toUpperCase()) {
            case "PERIOD 1": return PERIOD_1_SLOT;
            case "PERIOD 2": return PERIOD_2_SLOT;
            case "LUNCH A": return LUNCH_A_SLOT;
            case "LUNCH B": return LUNCH_B_SLOT;
            case "PERIOD 3": return PERIOD_3_SLOT;
            case "PERIOD 4": return PERIOD_4_SLOT;
            default: return -1;
        }
    }
    
    /**
     * Checks if a teacher has any classes in the given term
     */
    private static boolean hasClassesInTerm(Teacher teacher, LocalDate date) {
        int month = date.getMonthValue();
        List<String> schedule = teacher.getSchedule();
        
        // Check if teacher has any classes in their schedule
        boolean hasClasses = false;
        for (String slot : schedule) {
            if (!slot.trim().isEmpty()) {
                hasClasses = true;
                break;
            }
        }
        
        if (!hasClasses) {
            return false;
        }
        
        // Check which term this date falls into and if teacher has classes in that term
        if (month >= Month.SEPTEMBER.getValue() && month < Month.NOVEMBER.getValue()) {
            // Term 1 - Fall Term 1 (September-October)
            return true;
        } else if (month >= Month.NOVEMBER.getValue() && month <= Month.JANUARY.getValue()) {
            // Term 2 - Fall Term 2 (November-January)
            return true;
        } else if (month >= Month.FEBRUARY.getValue() && month < Month.APRIL.getValue()) {
            // Term 3 - Spring Term 1 (February-March)
            return true;
        } else if (month >= Month.APRIL.getValue() && month <= Month.JUNE.getValue()) {
            // Term 4 - Spring Term 2 (April-June)
            return true;
        }
        
        return false;
    }
} 