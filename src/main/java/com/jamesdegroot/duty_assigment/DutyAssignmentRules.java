package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.time.Month;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;

public class DutyAssignmentRules {
    // Constants for duty limits
    private static final int MAX_DUTIES_PER_WEEK = 3;
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
    
    // Period mapping constants - maps duty slots to teacher schedule indices
    private static final int[] PERIOD_TO_SCHEDULE_MAP = {
        0,  // Period 1 -> Schedule index 0
        1,  // Period 2 -> Schedule index 1
        1,  // Lunch A -> Maps to Period 2 (same time slot)
        2,  // Lunch B -> Maps to Period 3 (same time slot)
        2,  // Period 3 -> Schedule index 2
        3,  // Period 4 -> Schedule index 3
        4,  // Period 5 -> Schedule index 4
        5,  // Period 6 -> Schedule index 5
        6,  // Period 7 -> Schedule index 6
        7,  // Period 8 -> Schedule index 7
        8,  // Period 9 -> Schedule index 8
        9   // Period 10 -> Schedule index 9
    };
    
    /**
     * Determines if it's a Day 1 or Day 2 based on the date
     * @param date The date to check
     * @return true if it's Day 1, false if Day 2
     */
    public static boolean isDay1(LocalDate date) {
        // Calculate the number of school days since the start of the term
        Month month = date.getMonth();
        LocalDate termStart;
        
        if (month.getValue() >= Month.SEPTEMBER.getValue() && month.getValue() <= Month.JANUARY.getValue()) {
            // Fall semester
            if (month.getValue() < Month.NOVEMBER.getValue()) {
                // Term 1
                termStart = LocalDate.of(date.getYear(), Month.SEPTEMBER, 1);
            } else {
                // Term 2
                termStart = LocalDate.of(date.getYear(), Month.NOVEMBER, 1);
            }
        } else {
            // Spring semester
            if (month.getValue() < Month.APRIL.getValue()) {
                // Term 3
                termStart = LocalDate.of(date.getYear(), Month.FEBRUARY, 1);
            } else {
                // Term 4
                termStart = LocalDate.of(date.getYear(), Month.APRIL, 1);
            }
        }
        
        // Calculate days since term start
        long daysSinceTermStart = date.toEpochDay() - termStart.toEpochDay();
        
        // Every other school day should be Day 1
        return daysSinceTermStart % 2 == 0;
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
     * @param day The Day object containing school day information
     * @param isDay1Duty Whether this is a Day 1 duty assignment
     * @param daysInWeek The list of days in the week
     * @return true if the teacher can be assigned the duty
     */
    public static boolean canAssignDuty(Teacher teacher, int timeSlot, Day day, boolean isDay1Duty, List<Day> daysInWeek) {
        // Skip non-school days
        if (!day.isSchoolDay()) {
            return false;
        }
        
        // Check if teacher has classes during the duty time slot
        if (hasClassDuringTimeSlot(teacher, timeSlot)) {
            if (day.getDate().getMonthValue() == 1) { // Only debug January dates
                System.out.printf("Teacher %s has class during time slot %d%n", 
                    teacher.getName(), timeSlot + 1);
            }
            return false;
        }
        
        // Check if teacher has any classes in this term
        if (!hasClassesInTerm(teacher, day.getDate())) {
            if (day.getDate().getMonthValue() == 1) { // Only debug January dates
                System.out.printf("Teacher %s has no classes in term for date %s%n", 
                    teacher.getName(), day.getDate());
            }
            return false;
        }

        // Check adjacent period rules
        if (!canDoAdjacentPeriodDuty(teacher, timeSlot)) {
            if (day.getDate().getMonthValue() == 1) { // Only debug January dates
                System.out.printf("Teacher %s cannot do adjacent period duty for time slot %d%n", 
                    teacher.getName(), timeSlot + 1);
            }
            return false;
        }
        
        // Check weekly duty limit
        if (exceedsWeeklyDutyLimit(teacher, day.getDate(), daysInWeek)) {
            if (day.getDate().getMonthValue() == 1) { // Only debug January dates
                System.out.printf("Teacher %s exceeds weekly duty limit for date %s%n", 
                    teacher.getName(), day.getDate());
            }
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a teacher can do a duty based on their schedule in adjacent periods
     * @param teacher The teacher to check
     * @param timeSlot The time slot for the duty
     * @return true if the teacher can do the duty based on adjacent period rules
     */
    private static boolean canDoAdjacentPeriodDuty(Teacher teacher, int timeSlot) {
        List<String> schedule = teacher.getSchedule();
        int scheduleIndex = PERIOD_TO_SCHEDULE_MAP[timeSlot];

        // For Lunch A duties (can't have class in Period 2)
        if (timeSlot == LUNCH_A_SLOT) {
            return schedule.get(PERIOD_2_SLOT).trim().isEmpty();
        }
        
        // For Lunch B duties (can't have class in Period 3)
        if (timeSlot == LUNCH_B_SLOT) {
            return schedule.get(PERIOD_3_SLOT).trim().isEmpty();
        }

        // For other periods, check the period before
        if (scheduleIndex > 0 && !schedule.get(scheduleIndex - 1).trim().isEmpty()) {
            return false;
        }

        // For other periods, check the period after
        if (scheduleIndex < schedule.size() - 1 && !schedule.get(scheduleIndex + 1).trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if teacher has a class during the specified time slot
     */
    private static boolean hasClassDuringTimeSlot(Teacher teacher, int timeSlot) {
        List<String> schedule = teacher.getSchedule();
        
        // Check if the time slot is valid
        if (timeSlot < 0 || timeSlot >= PERIOD_TO_SCHEDULE_MAP.length) {
            System.out.printf("ERROR: Invalid time slot %d for teacher %s%n", timeSlot, teacher.getName());
            return false;
        }
        
        int scheduleIndex = PERIOD_TO_SCHEDULE_MAP[timeSlot];
        
        if (scheduleIndex >= 0 && scheduleIndex < schedule.size()) {
            return !schedule.get(scheduleIndex).trim().isEmpty();
        }
        
        // If we get here, the schedule index was out of bounds
        System.out.printf("ERROR: Schedule index %d out of bounds for teacher %s (schedule size: %d)%n",
            scheduleIndex, teacher.getName(), schedule.size());
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
        
        // Term boundaries
        LocalDate term1Start = LocalDate.of(2024, Month.SEPTEMBER, 3);
        LocalDate term2Start = LocalDate.of(2024, Month.NOVEMBER, 1);
        LocalDate term3Start = LocalDate.of(2025, Month.FEBRUARY, 1);
        LocalDate term4Start = LocalDate.of(2025, Month.APRIL, 1);
        LocalDate term4End = LocalDate.of(2025, Month.JUNE, 28);
        
        // Check which term this date falls into
        if (date.isBefore(term1Start) || date.isAfter(term4End)) {
            return false;
        }
        
        // All terms are valid for teachers with classes
        return true;
    }

    /**
     * Checks if a teacher has exceeded their weekly duty limit
     * @param teacher The teacher to check
     * @param date The date of the duty
     * @return true if the teacher has exceeded their weekly limit
     */
    private static boolean exceedsWeeklyDutyLimit(Teacher teacher, LocalDate date, List<Day> daysInWeek) {
        // If teacher is under their semester limit, be more flexible with weekly limits
        if (teacher.getDutiesThisSemester() < teacher.getMaxDutiesPerSemester() * 0.8) {
            return false;
        }
        
        int weeklyDuties = 0;
        
        // Count duties in the same week
        for (Day day : daysInWeek) {
            Duty[][] duties = day.getDutySchedule();
            for (Duty[] timeSlot : duties) {
                for (Duty duty : timeSlot) {
                    if (duty != null) {
                        // Check both Day 1 and Day 2 assignments
                        if (duty.getDay1Teachers().contains(teacher.getName())) {
                            weeklyDuties++;
                        }
                        if (duty.getDay2Teachers().contains(teacher.getName())) {
                            weeklyDuties++;
                        }
                    }
                }
            }
        }
        
        // Allow more duties per week if teacher is under their semester limit
        int maxWeeklyDuties = MAX_DUTIES_PER_WEEK;
        if (teacher.getDutiesThisSemester() < teacher.getMaxDutiesPerSemester() * 0.5) {
            maxWeeklyDuties += 1;  // Allow one extra duty per week for teachers under 50% of their limit
        }
        
        return weeklyDuties >= maxWeeklyDuties;
    }
} 