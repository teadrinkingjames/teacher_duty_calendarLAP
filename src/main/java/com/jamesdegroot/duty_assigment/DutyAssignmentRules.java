package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.time.Month;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import java.util.Map;
import java.util.HashMap;

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
    
    // Map duty time slots to schedule indices
    private static final Map<String, Integer> TIME_SLOT_MAP = new HashMap<>() {{
        put("Slot 1", 0);  // Period 1
        put("Slot 2", 1);  // Period 2
        put("Slot 3", 2);  // Lunch A
        put("Slot 4", 2);  // Lunch A
        put("Slot 5", 3);  // Lunch B
        put("Slot 6", 3);  // Lunch B
        put("Slot 7", 4);  // Period 4
        put("Slot 8", 4);  // Period 4
        put("Slot 9", 5);  // Period 5
        put("Slot 10", 5); // Period 5
        put("Slot 11", 6); // After school
    }};
    
    /**
     * Determines if it's a Day 1 or Day 2 based on the slot number
     * @param date The date to check
     * @return true if it's Day 1 (odd slot), false if Day 2 (even slot)
     */
    public static boolean isDay1(LocalDate date) {
        // Extract slot number from date
        int slotNumber = date.getDayOfMonth();
        // Odd slots are Day 1, even slots are Day 2
        return slotNumber % 2 == 1;
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
    public static boolean canAssignDuty(Teacher teacher, int timeSlot) {
        
        if (timeSlot == -1){ System.out.println("ERROR: Invalid time slot " + timeSlot); return false; }
        // Check if teacher has classes during the duty time slot
        if (hasClassDuringTimeSlot(teacher, timeSlot)) return false;
        
        // Check adjacent period rules
        if (!canDoAdjacentPeriodDuty(teacher, timeSlot)) return false;
        
        
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

        // For Lunch A duties (can't have class in Period 2)
        if (timeSlot == LUNCH_A_SLOT) {
            return schedule.get(PERIOD_2_SLOT).trim().equals("");
        }
        
        // For Lunch B duties (can't have class in Period 3)
        if (timeSlot == LUNCH_B_SLOT) {
            return schedule.get(PERIOD_3_SLOT).trim().equals("");
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
     * Converts a duty time slot string to a schedule index
     * @param timeSlot The time slot string from the duty
     * @return The corresponding schedule index, or -1 if invalid
     */
    public static int getTimeSlot(String timeSlot) {
        if (timeSlot == null) {
            System.out.println("ERROR: Null time slot");
            return -1;
        }
        return TIME_SLOT_MAP.getOrDefault(timeSlot, -1);
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