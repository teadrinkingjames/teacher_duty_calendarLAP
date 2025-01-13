package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DutyAssignmentRules.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    DutyAssignmentRules class, used to store the rules for assigning duties to teachers
 *      
 *  Methods:
 * 	  - canAssignDuty, boolean, checks if a teacher can be assigned a duty based on their schedule
 *    - canDoAdjacentPeriodDuty, boolean, checks if a teacher can do a duty based on their schedule in adjacent periods
 *    - hasClassDuringTimeSlot, boolean, checks if a teacher has a class during the specified time slot
 *    - getTimeSlot, int, converts a duty time slot string to a schedule index
 *    - isDay1, boolean, checks if a given date is a Day 1 in the schedule rotation
 *    - getDayRotation, String, gets the day rotation identifier for a given date
*/

public class DutyAssignmentRules {
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
        0,  //
        1,  //
        1,  //
        2,  //
        2,  //
        3,  //
        4,  //
        5,  //
        6,  //
        7,  //
        8,  
        9   
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
        // print schedule
        // for (String slot : schedule) {
        //     System.out.println(slot);
        // }

        // System.out.println("timeSlot: " + timeSlot);
        
        // Check if the time slot is valid
        if (timeSlot < 0 || timeSlot >= PERIOD_TO_SCHEDULE_MAP.length) {
            System.out.printf("ERROR: Invalid time slot %d for teacher %s%n", timeSlot, teacher.getName());
            return false;
        }
        
        int scheduleIndex = PERIOD_TO_SCHEDULE_MAP[timeSlot];
        System.out.println("scheduleIndex: " + scheduleIndex + !schedule.get(scheduleIndex).trim().equals(""));
        if (scheduleIndex >= 0 && scheduleIndex < schedule.size()) {
            return !schedule.get(scheduleIndex).trim().equals("");
        }
        
        // If we get here, the schedule index was out of bounds
        System.out.printf("ERROR: Schedule index %d out of bounds for teacher %s (schedule size: %d)%n",
            scheduleIndex, teacher.getName(), schedule.size());
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
} 