package com.jamesdegroot.teacher;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import com.jamesdegroot.calendar.Duty;

/**
 * Teacher.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    Teacher class, used to create a teacher object
 *      
 *  Methods:
 * 	  - Teacher, constructor, creates a new Teacher object
 *    - addScheduleItem, void, adds a schedule item to the teacher's schedule
 *    - calculateTimeAllocation, void, calculates the teacher's time allocation
 *    - calculateScheduleStatus, void, calculates the teacher's schedule status
 *    - determineTeacherType, TeacherTypeEnum, determines the teacher's type
 *    - calculateMaxDutiesPerSemester, void, calculates the teacher's max duties per semester
 *    - toString, String, returns a string representation of the teacher's schedule
 *    - getName, String, gets the teacher's name
 *    - getTimeAllocation, double, gets the teacher's time allocation
 *    - getClassScheduleStatus, TeacherScheduleStatusEnum, gets the teacher's schedule status
 *    - getFilledPeriods, int, gets the number of filled periods in the teacher's schedule
 *    - getJobType, TeacherTypeEnum, gets the teacher's job type
 *    - setJobType, void, sets the teacher's job type
 *    - getMaxDutiesPerSemester, int, gets the teacher's max duties per semester
 *    - getDutiesThisSemester, int, gets the number of duties assigned to the teacher this semester
 *    - incrementDutiesThisSemester, void, increments the number of duties assigned to the teacher this semester
 *    - resetDutiesThisSemester, void, resets the number of duties assigned to the teacher this semester
 *    - getSchedule, List<String>, gets the teacher's schedule
 *    - incrementDutiesForTerm, void, increments the number of duties for a specific term
 *    - getDutiesForTerm, int, gets the number of duties for a specific term
 *    - hasDutyInSemester, boolean, checks if the teacher has any duties in a specific semester
 *    - hasClassInSemester, boolean, checks if the teacher has any classes in a specific semester
 *    - assignDuty, void, assigns a duty to the teacher
 *    - hasDutyAssigned, boolean, checks if the teacher has a specific duty assigned
 *    - resetDutiesForNewSemester, void, resets the number of duties for a new semester
 *    - canDoDuty, boolean, checks if the teacher can perform a specific duty
*/


public class Teacher {
    // Schedule constants
    private static final int TOTAL_PERIODS = 10;
    private static final int PERIOD_5_INDEX = 4;
    private static final int PERIOD_10_INDEX = 9;
    private static final int DEFAULT_TOTAL_PERIODS = 8;
    private static final int BASE_TEACHING_PERIODS = 6;
    
    // Course code constants
    private static final String COOP_CODE = "1CO";
    private static final String GYM_CODE = "PPL";
    private static final String CREDIT_RECOVERY_CODE = "RCR";
    private static final String CREDIT_RECOVERY_ALT_CODE = "1RC";
    private static final String GUIDANCE_CODE = "2GU";
    private static final String GUIDANCE_ALT_CODE = "GLE";
    private static final String LIBRARY_CODE = "2LI";
    
    // Status thresholds
    private static final int OVER_FULL_TIME_MIN = 7;
    private static final int FULL_TIME = 6;
    private static final int FIVE_SIXTHS = 5;
    private static final int FOUR_SIXTHS = 4;
    private static final int THREE_SIXTHS = 3;
    private static final int TWO_SIXTHS = 2;
    private static final int ONE_SIXTH = 1;
    
    // Duty allocation constants
    private static final int GUIDANCE_MAX_DUTIES = 25;
    private static final int HEAD_MAX_DUTIES = 10;
    private static final int FULL_TIME_MAX_DUTIES = 14;
    private static final int FIVE_SIXTHS_MAX_DUTIES = 11;
    private static final int FOUR_SIXTHS_MAX_DUTIES = 9;
    private static final int THREE_SIXTHS_MAX_DUTIES = 7;
    private static final int TWO_SIXTHS_MAX_DUTIES = 6;
    private static final int NO_DUTIES = 0;
    
    // Display format constants
    private static final String FREE_PERIOD = "FREE";
    private static final String SPECIAL_PERIOD_FORMAT = "Period %d*";
    private static final String NORMAL_PERIOD_FORMAT = "Period %d ";
    private static final String PERIOD_FORMAT = "  %-10s: %s\n";
    private static final String TEACHER_FORMAT = "Teacher: %s\n";
    private static final String TYPE_FORMAT = "Type: %s\n";
    private static final String STATUS_FORMAT = "Status: %s (%.2f load)\n";
    private static final String DUTIES_FORMAT = "Duties: %d/%d\n";
    private static final String SPECIAL_PERIODS_NOTE = "\n* Periods 5 and 10 are optional periods\n";
    
    private String name;
    private List<String> schedule;
    private double timeAllocation; // percentage of time allocated to teaching
    private TeacherScheduleStatusEnum classScheduleStatus; // status of the teacher's schedule
    private TeacherTypeEnum jobType; // type of the teacher's job
    private int[] dutiesPerTerm = new int[4];  // One counter for each term
    private int dutiesThisSemester = 0; // counter for duties assigned this semester
    private int maxDutiesPerSemester;   // maximum number of duties per semester
    private Set<Duty> assignedDuties;  // Track which duties are assigned
    
    /**
     * Creates a new Teacher with the given name and initializes their schedule.
     * @param name The full name of the teacher
     */
    public Teacher(String name) {
        this.name = name;
        this.schedule = new ArrayList<>();
        this.classScheduleStatus = TeacherScheduleStatusEnum.NO_LOAD;
        this.jobType = TeacherTypeEnum.REGULAR;
        this.assignedDuties = new HashSet<>();
        
        // Initialize all periods as empty
        for (int periodIndex = 0; periodIndex < TOTAL_PERIODS; periodIndex++) {
            schedule.add("");
        }
    }

    /**
     * Adds a class period to the teacher's schedule and updates their type.
     * @param item The class/period details to add
     * @param periodIndex The period number (0-9)
     */
    public void addScheduleItem(String item, int periodIndex) {
        if (periodIndex >= 0 && periodIndex < TOTAL_PERIODS) {
            schedule.set(periodIndex, item);
            this.jobType = determineTeacherType(); // Update type when schedule changes
        }
    }

    /**
     * Calculates the teacher's time allocation based on their schedule.
     * Considers periods 5 and 10 only if they contain classes.
     * Updates both timeAllocation and status fields.
     */
    public void calculateTimeAllocation() {
        int filledPeriods = 0;
        int totalPeriods = DEFAULT_TOTAL_PERIODS;
        
        // Check if teacher has classes in periods 5 and 10
        if (schedule.size() > PERIOD_5_INDEX && !schedule.get(PERIOD_5_INDEX).trim().isEmpty()) {
            totalPeriods++;
        }
        if (schedule.size() > PERIOD_10_INDEX && !schedule.get(PERIOD_10_INDEX).trim().isEmpty()) {
            totalPeriods++;
        }
        
        // Count filled periods (skipping periods 5 and 10 if empty)
        for (int periodIndex = 0; periodIndex < schedule.size(); periodIndex++) {
            if (periodIndex != PERIOD_5_INDEX && periodIndex != PERIOD_10_INDEX) {  // Regular periods
                if (!schedule.get(periodIndex).trim().isEmpty()) {
                    filledPeriods++;
                }
            } else if (!schedule.get(periodIndex).trim().isEmpty()) {  // Extra periods (5 and 10)
                filledPeriods++;
            }
        }
        
        this.timeAllocation = (double) filledPeriods / totalPeriods;
        calculateScheduleStatus();
    }

    /**
     * Updates the teacher's status based on their time allocation.
     * Maps exact number of periods to status.
     */
    private void calculateScheduleStatus() {
        int filledPeriods = 0;
        // Count non-empty periods (excluding periods 5 and 10)
        for (int periodIndex = 0; periodIndex < schedule.size(); periodIndex++) {
            if (periodIndex != PERIOD_5_INDEX && periodIndex != PERIOD_10_INDEX && !schedule.get(periodIndex).trim().isEmpty()) {
                filledPeriods++;
            }
        }
        
        // Calculate time allocation based on base teaching periods
        this.timeAllocation = (double) filledPeriods / BASE_TEACHING_PERIODS;
        
        // Map exact number of periods to status
        if (filledPeriods >= OVER_FULL_TIME_MIN) {
            classScheduleStatus = TeacherScheduleStatusEnum.OVER_FULL_TIME;
        } else if (filledPeriods == FULL_TIME) {
            classScheduleStatus = TeacherScheduleStatusEnum.FULL_TIME;
        } else if (filledPeriods == FIVE_SIXTHS) {
            classScheduleStatus = TeacherScheduleStatusEnum.FIVE_SIXTHS;
        } else if (filledPeriods == FOUR_SIXTHS) {
            classScheduleStatus = TeacherScheduleStatusEnum.FOUR_SIXTHS;
        } else if (filledPeriods == THREE_SIXTHS) {
            classScheduleStatus = TeacherScheduleStatusEnum.THREE_SIXTHS;
        } else if (filledPeriods == TWO_SIXTHS) {
            classScheduleStatus = TeacherScheduleStatusEnum.TWO_SIXTHS;
        } else if (filledPeriods == ONE_SIXTH) {
            classScheduleStatus = TeacherScheduleStatusEnum.ONE_SIXTH;
        } else {
            classScheduleStatus = TeacherScheduleStatusEnum.NO_LOAD;
        }
    }

    /**
     * Determines the teacher type based on their course schedule.
     * @return TeacherTypeEnum representing the teacher's primary teaching area
     */
    public TeacherTypeEnum determineTeacherType() {
        // Initialize maxDutiesPerSemester to default value
        maxDutiesPerSemester = -1;
        
        // Count occurrences of different course types
        int coopCourseCount = 0;
        int gymCourseCount = 0;
        int guidanceCourseCount = 0;
        int creditRecoveryCourseCount = 0;
        
        // Check all course codes
        for (String timeSlot : schedule) {
            if (timeSlot == null || timeSlot.trim().isEmpty()) {
                continue;
            }
            
            // Split multiple courses in the same time slot
            String[] coursesInSlot = timeSlot.split(",");
            for (String courseEntry : coursesInSlot) {
                String courseCode = courseEntry.toUpperCase()
                    .replaceAll("\"", "")         // Remove quotes
                    .split("[ ,-]")[0];           // Split on space, comma, or dash and take first part
                
                // Check for excluded course codes
                if (courseCode.contains(GYM_CODE) || 
                    courseCode.contains(COOP_CODE) ||
                    courseCode.contains(CREDIT_RECOVERY_ALT_CODE) ||
                    courseCode.contains(CREDIT_RECOVERY_CODE) ||
                    courseCode.contains(GUIDANCE_CODE) ||
                    courseCode.contains(GUIDANCE_ALT_CODE) ||
                    courseCode.contains(LIBRARY_CODE)) {
                    maxDutiesPerSemester = NO_DUTIES;
                }
                
                // Count course types for teacher type determination
                if (courseCode.contains(COOP_CODE)) {
                    coopCourseCount++;
                    return TeacherTypeEnum.COOP;
                } else if (courseCode.contains(GYM_CODE)) {
                    gymCourseCount++;
                    return TeacherTypeEnum.GYM;
                } else if (courseCode.contains(GUIDANCE_CODE) || courseCode.contains(GUIDANCE_ALT_CODE)) {
                    guidanceCourseCount++;
                    return TeacherTypeEnum.GUIDANCE;
                } else if (courseCode.contains(CREDIT_RECOVERY_ALT_CODE) || courseCode.contains(CREDIT_RECOVERY_CODE)) {
                    creditRecoveryCourseCount++;
                    return TeacherTypeEnum.CREDIT_RECOVERY;
                }
            }
        }
        
        // Determine primary role based on most frequent course type
        if (coopCourseCount > 2) return TeacherTypeEnum.COOP;
        if (gymCourseCount > 2) return TeacherTypeEnum.GYM;
        if (guidanceCourseCount > 2) return TeacherTypeEnum.GUIDANCE;
        if (creditRecoveryCourseCount > 2) return TeacherTypeEnum.CREDIT_RECOVERY;
        
        return TeacherTypeEnum.REGULAR;
    }

    /**
     * Calculates the maximum number of duties per semester based on type and allocation
     */
    public void calculateMaxDutiesPerSemester() {
        // Skip calculation if already set to 0 by excluded course codes
        if (maxDutiesPerSemester == NO_DUTIES) {
            return;
        }
        
        // Calculate based on teacher type and status
        if (jobType == TeacherTypeEnum.GUIDANCE) {
            maxDutiesPerSemester = GUIDANCE_MAX_DUTIES;
        } else if (jobType == TeacherTypeEnum.HEAD) {
            maxDutiesPerSemester = HEAD_MAX_DUTIES;
        } else {
            // Map duties based on schedule status
            switch (classScheduleStatus) {
                case FULL_TIME: maxDutiesPerSemester = FULL_TIME_MAX_DUTIES; break;
                case FIVE_SIXTHS: maxDutiesPerSemester = FIVE_SIXTHS_MAX_DUTIES; break;
                case FOUR_SIXTHS: maxDutiesPerSemester = FOUR_SIXTHS_MAX_DUTIES; break;
                case THREE_SIXTHS: maxDutiesPerSemester = THREE_SIXTHS_MAX_DUTIES; break;
                case TWO_SIXTHS: maxDutiesPerSemester = TWO_SIXTHS_MAX_DUTIES; break;
                default: maxDutiesPerSemester = NO_DUTIES; break;
            }
        }
    }

    /**
     * Returns a string representation of the teacher's schedule.
     * @return A string containing the teacher's name, status, and schedule
     */ 
    @Override
    public String toString() {
        StringBuilder scheduleBuilder = new StringBuilder();
        scheduleBuilder.append(String.format(TEACHER_FORMAT, name));
        scheduleBuilder.append(String.format(TYPE_FORMAT, jobType.toString()));
        scheduleBuilder.append(String.format(STATUS_FORMAT, 
            classScheduleStatus.toString().replace("_", " "), 
            timeAllocation));
        scheduleBuilder.append(String.format(DUTIES_FORMAT, dutiesThisSemester, maxDutiesPerSemester));
        scheduleBuilder.append("Schedule:\n");
        
        // Print schedule with period numbers
        for (int periodIndex = 0; periodIndex < schedule.size(); periodIndex++) {
            String periodContent = schedule.get(periodIndex).trim();
            if (periodContent.isEmpty()) {
                periodContent = FREE_PERIOD;
            }
            // Add special notation for periods 5 and 10
            String periodLabel = (periodIndex == PERIOD_5_INDEX || periodIndex == PERIOD_10_INDEX) ? 
                String.format(SPECIAL_PERIOD_FORMAT, periodIndex + 1) : 
                String.format(NORMAL_PERIOD_FORMAT, periodIndex + 1);
            scheduleBuilder.append(String.format(PERIOD_FORMAT, periodLabel, periodContent));
        }
        
        // Add note about special periods if they exist
        if (schedule.size() > PERIOD_5_INDEX) {
            scheduleBuilder.append(SPECIAL_PERIODS_NOTE);
        }
        
        return scheduleBuilder.toString();
    }

    // Getters and setters remain unchanged
    public String getName() {
        return name;
    }

    public double getTimeAllocation() {
        return timeAllocation;
    }

    public TeacherScheduleStatusEnum getClassScheduleStatus() {
        return classScheduleStatus;
    }

    /**
     * Gets the number of filled periods in the teacher's schedule.
     * @return the number of filled periods
     */
    public int getFilledPeriods() {
        int filledPeriodCount = 0;
        for (int periodIndex = 0; periodIndex < schedule.size(); periodIndex++) {
            if (periodIndex != PERIOD_5_INDEX && periodIndex != PERIOD_10_INDEX && !schedule.get(periodIndex).trim().isEmpty()) {
                filledPeriodCount++;
            }
        }
        return filledPeriodCount;
    }

    public TeacherTypeEnum getJobType() {
        return jobType;
    }

    public void setJobType(TeacherTypeEnum jobType) {
        this.jobType = jobType;
    }

    public int getMaxDutiesPerSemester() {
        return maxDutiesPerSemester;
    }

    public int getDutiesThisSemester() {
        return dutiesThisSemester;
    }

    public void incrementDutiesThisSemester() {
        dutiesThisSemester++;
    }

    /**
     * Resets the duties for this semester.
     */
    public void resetDutiesThisSemester() {
        dutiesThisSemester = 0;
        for (int i = 0; i < 4; i++) {
            dutiesPerTerm[i] = 0;
        }
    }

    public List<String> getSchedule() {
        return schedule;
    }

    /**
     * Increments the duty count for a specific term
     * @param termNumber the term number (0-3)
     */
    public void incrementDutiesForTerm(int termNumber) {
        if (termNumber >= 0 && termNumber < 4) {
            dutiesPerTerm[termNumber]++;
        }
    }

    /**
     * Gets the number of duties for a specific term
     * @param termNumber the term number (0-3)
     * @return the number of duties for that term
     */
    public int getDutiesForTerm(int termNumber) {
        if (termNumber >= 0 && termNumber < 4) {
            return dutiesPerTerm[termNumber];
        }
        return 0;
    }

    /**
     * Checks if teacher has any duties in the given term
     * @param term the term number (0-3)
     * @return true if teacher has duties in this term
     */
    public boolean hasDutyInSemester(int term) {
        return getDutiesForTerm(term) > 0;
    }

    /**
     * Checks if teacher has any classes in the given term
     * @param term the term number (0-3)
     * @return true if teacher has classes in this term
     */
    public boolean hasClassInSemester(int term) {
        return !schedule.stream().allMatch(String::isEmpty);
    }

    /**
     * Assigns a duty to this teacher
     * @param duty the duty to assign
     * @param patternCount number of times this type of day occurs
     */
    public void assignDuty(Duty duty, int patternCount) {
        assignedDuties.add(duty);
        dutiesThisSemester += patternCount;
    }

    /**
     * Checks if teacher has this duty assigned
     * @param duty the duty to check
     * @return true if teacher has this duty
     */
    public boolean hasDutyAssigned(Duty duty) {
        return assignedDuties.contains(duty);
    }

    public void resetDutiesForNewSemester() {
        dutiesThisSemester = 0;
        assignedDuties.clear();
    }

    /**
     * Gets the set of duties assigned to this teacher
     * @return Set of duties assigned to this teacher
     */
    public Set<Duty> getAssignedDuties() {
        return assignedDuties;
    }
}