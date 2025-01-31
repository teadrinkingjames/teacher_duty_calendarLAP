package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;

/**
 * DutyAssigner.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    DutyAssigner class, used to assign duties to teachers based on their schedule and the duties they can do
 *      
 *  Methods:
 * 	  - assignDuties, void, assigns duties to teachers based on their schedule and the duties they can do
 *    - printDutySchedule, void, prints the duty schedule for all terms
 *    - loadTestData, void, loads test data from resource files
 *    - assignDutiesForTeacher, void, assigns duties to a single teacher across both semesters
 *    - assignDutiesInTerm, boolean, attempts to assign a duty to a teacher within a specific term
 *    - tryAssignDutyToTeacher, boolean, attempts to assign a specific duty to a teacher
 *    - initializeTermPatternGroups, void, initializes the term pattern groups data structure
 *    - getTermDaysForSemester, List<List<Day>>, gets the days for each term in a semester
 *    - getDaysForTerm, List<Day>, gets the days for a specific term
 *    - getSchoolDays, List<Day>, gets all school days from the calendar
 *    - addPatternDaysToList, void, adds days to a list based on the pattern and term
 *    - getDayPattern, DayPattern, gets the day pattern for a specific day of the week and day type
 *    - printPatternCounts, void, prints the pattern counts for debugging and verification
 *    - printTeacherDutyCounts, void, prints a summary of teachers who haven't reached their maximum duties
 * 
*/

public class DutyAssigner {
    // Calendar and teacher references
    private final Calendar calendar;
    private final List<Teacher> teachers;
    private Map<DayPattern, List<Day>> dayGroups;
    private List<Map<DayPattern, List<Day>>> termPatternGroups;

    // Add enum for day patterns
    private enum DayPattern {
        MONDAY_DAY1,    MONDAY_DAY2,
        TUESDAY_DAY1,   TUESDAY_DAY2,
        WEDNESDAY_DAY1, WEDNESDAY_DAY2,
        THURSDAY_DAY1,  THURSDAY_DAY2,
        FRIDAY_DAY1,    FRIDAY_DAY2
    }

    // Constructor
    public DutyAssigner(Calendar calendar, List<Teacher> teachers) {
        this.calendar = calendar;
        this.teachers = teachers;
        this.dayGroups = new EnumMap<>(DayPattern.class);
        // Initialize all groups with empty lists
        for (DayPattern pattern : DayPattern.values()) {
            dayGroups.put(pattern, new ArrayList<>());
        }
    }

    /**
     * Main method to assign duties for the entire school year.
     * 
     * Process Overview:
     * 1. Get all school days (excluding holidays and weekends)
     * 2. Group days by term and pattern (e.g., Monday Day 1, Monday Day 2)
     * 3. For each teacher:
     *    - Process one semester at a time
     *    - First assign non-Hall duties (prioritized)
     *    - Then assign Hall duties if needed to meet duty quota
     *    - Ensure balanced distribution between terms
     */
    public void assignDuties() {
        // Initialize pattern groups for each term
        List<Day> schoolDays = getSchoolDays();
        initializeTermPatternGroups(schoolDays);
        
        // First pass: Assign duties to teachers based on their schedule
        for (Teacher teacher : teachers) {
            assignDutiesForTeacher(teacher);
        }
        
        // Second pass: Try to assign remaining duties
        for (Teacher teacher : teachers) {
            assignDutiesForTeacher(teacher);
        }
        
        // Third pass: Try to assign remaining duties
        for (Teacher teacher : teachers) {
            assignDutiesForTeacher(teacher);
        }
        
        // Final pass: Allow two teachers per duty
        for (Teacher teacher : teachers) {
            assignDutiesForTeacherFinalPass(teacher);
        }
        
        // Print pattern counts for debugging
        printPatternCounts();
        
        // Print the complete duty schedule
        //printDutySchedule(); // DEBUGGING, this present in generateDutyCalendar.java
        
        // Write the complete schedule to CSV
        String outputPath = "src/main/resources/duty_schedule.csv";
        com.jamesdegroot.io.WriteScheduleToDisk.writeDutyScheduleToCSV(calendar, outputPath);
    }

    /**
     * Prints a formatted duty schedule for all terms.
     * 
     * Format:
     * Term X Duty Schedule:
     * ===============================
     * Day      | Duty              | Day 1 Teachers        | Day 2 Teachers
     * ------------------------------------------------------------------------
     * MONDAY   | Cafeteria        | Smith, Jones         | Brown, Wilson
     */
    public void printDutySchedule() {
        // For each term
        for (int term = 0; term < 4; term++) {
            System.out.println("\nTerm " + (term + 1) + " Duty Schedule:");
            System.out.println("=".repeat(135));
            
            // Print header
            System.out.printf("%-15s | %-25s | %-40s | %-40s%n", 
                "Day", "Duty", "Day 1 Teachers", "Day 2 Teachers");
            System.out.println("-".repeat(135));
            
            // Get the pattern groups for this term
            Map<DayPattern, List<Day>> patterns = termPatternGroups.get(term);
            if (patterns == null || patterns.isEmpty()) {
                System.out.println("No school days in this term");
                continue;
            }
            
            // Print each weekday's duties
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) continue;
                
                // Get Day1 and Day2 patterns for this weekday
                DayPattern day1Pattern = getDayPattern(dayOfWeek, true);
                DayPattern day2Pattern = getDayPattern(dayOfWeek, false);
                
                // Get both pattern days
                Day day1 = patterns.get(day1Pattern) != null && !patterns.get(day1Pattern).isEmpty() 
                    ? patterns.get(day1Pattern).get(0) : null;
                Day day2 = patterns.get(day2Pattern) != null && !patterns.get(day2Pattern).isEmpty() 
                    ? patterns.get(day2Pattern).get(0) : null;
                
                // Use either day as template (they should have same duty structure)
                Day templateDay = day1 != null ? day1 : day2;
                if (templateDay == null) continue;
                
                Duty[][] dutySchedule = templateDay.getDutySchedule();
                for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                    for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                        Duty duty1 = day1 != null ? day1.getDutySchedule()[timeSlot][pos] : null;
                        Duty duty2 = day2 != null ? day2.getDutySchedule()[timeSlot][pos] : null;
                        
                        // Use either duty as template (they should have same name)
                        Duty templateDuty = duty1 != null ? duty1 : duty2;
                        if (templateDuty != null) {
                            System.out.printf("%-15s | %-25s | %-40s | %-40s%n",
                                dayOfWeek,
                                templateDuty.getName(),
                                duty1 != null ? String.join(", ", duty1.getDay1Teachers()) : "",
                                duty2 != null ? String.join(", ", duty2.getDay2Teachers()) : "");
                        }
                    }
                }
                System.out.println("-".repeat(135));
            }
        }
    }

    /**
     * Assigns duties to a single teacher across both semesters.
     * 
     * Process per semester:
     * 1. Reset duty counts for new semester
     * 2. Skip if teacher has no classes this semester
     * 3. First pass: Try to assign non-Hall duties across both terms
     * 4. Second pass: If needed, assign Hall duties to meet quota
     * 
     * @param teacher The teacher to assign duties to
     */
    private void assignDutiesForTeacher(Teacher teacher) {
        if (teacher.getMaxDutiesPerSemester() == 0) {
            // DEBUGGING, prints the teacher name, if they have no classes in the semester
            //System.out.println("Teacher " + teacher.getName() + " has no duties");
            return;
        }
        
        // DEBUGGING, prints the teacher name, if they are being assigned duties
        //System.out.println("Teacher: " + teacher.getName());
        List<String> classSchedule = teacher.getSchedule();
        
        for (int semester = 0; semester < 2; semester++) {
            teacher.resetDutiesForNewSemester();
            
            // Skip if teacher has no classes this semester
            List<String> semesterClasses = semester == 0 ? 
                classSchedule.subList(0, 5) : classSchedule.subList(5, 10);
            if (semesterClasses.equals(Arrays.asList("", "", "", "", ""))) {
                
                // DEBUGGING, prints the teacher name, if they have no classes in the semester
                //System.out.println("Teacher " + teacher.getName() + " has no classes in semester " + (semester+1));
                continue;
            }
            
            // DEBUGGING, prints the teacher name, if they have classes in the semester 
            //System.out.println("Teacher " + teacher.getName() + " has classes in semester " + (semester+1));
            int numberOfDutiesNeeded = teacher.getMaxDutiesPerSemester();
            
            List<List<Day>> termDays = getTermDaysForSemester(semester);
            
            // First pass: non-hall duties
            boolean continueAssigning = true;
            while (continueAssigning && teacher.getDutiesThisSemester() < numberOfDutiesNeeded) {
                continueAssigning = false;
                for (int termIndex = 0; termIndex < termDays.size(); termIndex++) {
                    if (assignDutiesInTerm(teacher, termDays.get(termIndex), semester * 2 + termIndex, false, numberOfDutiesNeeded)) {
                        continueAssigning = true;
                        break;
                    }
                }
            }
            
            // Second pass: hall duties if needed
            if (teacher.getDutiesThisSemester() < numberOfDutiesNeeded) {
                continueAssigning = true;
                while (continueAssigning && teacher.getDutiesThisSemester() < numberOfDutiesNeeded) {
                    continueAssigning = false;
                    for (int termIndex = 0; termIndex < termDays.size(); termIndex++) {
                        if (assignDutiesInTerm(teacher, termDays.get(termIndex), semester * 2 + termIndex, true, numberOfDutiesNeeded)) {
                            continueAssigning = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts to assign a duty to a teacher within a specific term.
     * Returns true if a duty was assigned, false otherwise.
     * 
     * Process:
     * 1. Iterate through each day in the term
     * 2. For each day, check all duty slots
     * 3. Try to assign each duty if it matches criteria
     * 
     * @param teacher The teacher to assign the duty to
     * @param daysInTerm List of days in the current term
     * @param term Current term number
     * @param isHallDuty Whether to assign hall duties or non-hall duties
     * @param numberOfDutiesNeeded Maximum duties for this teacher
     * @return boolean indicating if a duty was assigned
     */
    private boolean assignDutiesInTerm(Teacher teacher, List<Day> daysInTerm, int term, boolean isHallDuty, int numberOfDutiesNeeded) {
        for (Day day : daysInTerm) {
            if (teacher.getDutiesThisSemester() >= numberOfDutiesNeeded) return false;
            
            Duty[][] dutySchedule = day.getDutySchedule();
            for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                    if (tryAssignDutyToTeacher(teacher, day, dutySchedule[timeSlot][pos], term, isHallDuty, numberOfDutiesNeeded)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Attempts to assign a specific duty to a teacher.
     * 
     * Checks performed:
     * 1. Duty exists and hasn't been assigned to this teacher
     * 2. Duty type matches what we're trying to assign (Hall vs non-Hall)
     * 3. No teacher already assigned for this day type
     * 4. Assignment won't exceed teacher's semester limit
     * 
     * @param teacher The teacher to assign the duty to
     * @param day The day containing the duty
     * @param duty The specific duty to assign
     * @param term Current term number
     * @param isHallDuty Whether we're assigning hall duties
     * @param numberOfDutiesNeeded Maximum duties for this teacher
     * @return boolean indicating if the duty was assigned
     */
    private boolean tryAssignDutyToTeacher(Teacher teacher, Day day, Duty duty, int term, boolean isHallDuty, int numberOfDutiesNeeded) {
        if (duty == null || teacher.hasDutyAssigned(duty)) return false;
        
        boolean isHall = duty.getName().contains("Hall");
        if ((isHallDuty && !isHall) || (!isHallDuty && isHall)) return false;

        // If this is a hall duty, check if there are any unfilled non-hall duties in the same time slot
        if (isHall) {
            Duty[][] dutySchedule = day.getDutySchedule();
            int currentTimeSlot = DutyAssignmentRules.getTimeSlot(duty.getTimeSlot());
            
            // Check all duties in the same time slot
            for (Duty otherDuty : dutySchedule[currentTimeSlot]) {
                if (otherDuty != null && !otherDuty.getName().contains("Hall")) {
                    // If there's an unfilled non-hall duty, don't assign this hall duty
                    boolean hasDay1Teacher = !otherDuty.getDay1Teachers().isEmpty();
                    boolean hasDay2Teacher = !otherDuty.getDay2Teachers().isEmpty();
                    if ((day.isDay1() && !hasDay1Teacher) || (!day.isDay1() && !hasDay2Teacher)) {
                        return false;
                    }
                }
            }
        }

        // Skip if duty already has a teacher assigned for this day type
        if ((day.isDay1() && !duty.getDay1Teachers().isEmpty()) ||
            (!day.isDay1() && !duty.getDay2Teachers().isEmpty())) {
            return false;
        }

        // Skip if teacher cannot do this duty
        int timeSlot = DutyAssignmentRules.getTimeSlot(duty.getTimeSlot());
        if (!DutyAssignmentRules.canAssignDuty(teacher, timeSlot)) return false;

        DayPattern pattern = getDayPattern(day.getDate().getDayOfWeek(), day.isDay1());
        int patternCount = termPatternGroups.get(term).get(pattern).size();

        // Check if assigning this duty would exceed the semester limit
        if (teacher.getDutiesThisSemester() + patternCount <= numberOfDutiesNeeded) {
            teacher.assignDuty(duty, patternCount);
            
            // Add teacher to the pattern's duty
            if (day.isDay1()) {
                duty.addDay1Teacher(teacher.getName());
            } else {
                duty.addDay2Teacher(teacher.getName());
            }
            // DEBUGGING, prints the duty assignment
            // String dutyType = isHallDuty ? "hall duty" : "duty";
            // System.out.println("Assigned " + dutyType + ": " + duty.getName() + " to teacher: " + teacher.getName() +
            //     " (worth " + patternCount + " duties, total now: " + teacher.getDutiesThisSemester() + 
            //     ", Day " + (day.isDay1() ? "1" : "2") + ")");
            
            return true;
        }
        
        return false;
    }

    /**
     * Initializes the term pattern groups data structure.
     * Groups school days by term and pattern for efficient access.
     * 
     * Structure:
     * termPatternGroups[term][pattern] = List of days matching that pattern
     * 
     * @param schoolDays List of all school days
     */
    private void initializeTermPatternGroups(List<Day> schoolDays) {
        termPatternGroups = new ArrayList<>();
        for (int term = 0; term < 4; term++) {
            int finalTerm = term;
            Map<DayPattern, List<Day>> patternGroups = schoolDays.stream()
                .filter(day -> day.getTermNumber() == finalTerm)
                .collect(Collectors.groupingBy(day -> getDayPattern(day.getDate().getDayOfWeek(), day.isDay1())));
            termPatternGroups.add(patternGroups);
        }
    }

    /**
     * Gets a list of days for each term in a semester.
     * 
     * Structure:
     * - Index 0: First term of semester
     * - Index 1: Second term of semester
     * 
     * @param semester The semester to get the days for
     * @return List of days for each term in the semester
     */
    private List<List<Day>> getTermDaysForSemester(int semester) {
        List<List<Day>> termDays = new ArrayList<>();
        for (int term = semester * 2; term < (semester * 2 + 2); term++) {
            termDays.add(getDaysForTerm(term));
        }
        return termDays;
    }

    private List<Day> getDaysForTerm(int term) {
        List<Day> daysInTerm = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) continue;
            
            addPatternDaysToList(term, dayOfWeek, true, daysInTerm);
            addPatternDaysToList(term, dayOfWeek, false, daysInTerm);
        }
        return daysInTerm;
    }

    /**
     * Gets all school days from the calendar.
     * Filters out weekends and holidays.
     * 
     * @return List of all school days
     */
    private List<Day> getSchoolDays() {
        return calendar.getDaysOfYear().stream()
            .filter(Day::isSchoolDay)
            .toList();
    }

    /**
     * Adds days to a list based on the pattern and term
     * 
     * @param term The term to get the days for
     * @param dayOfWeek The day of the week
     * @param isDay1 Whether the day is Day 1
     * @param daysInTerm The list of days to add to
     */
    private void addPatternDaysToList(int term, DayOfWeek dayOfWeek, boolean isDay1, List<Day> daysInTerm) {
        DayPattern pattern = getDayPattern(dayOfWeek, isDay1);
        List<Day> days = termPatternGroups.get(term).get(pattern);
        if (days != null && !days.isEmpty()) {
            daysInTerm.add(days.get(0));
        }
    }

    /**
     * Maps a day of the week and Day1/Day2 status to a DayPattern.
     * Used for consistent pattern matching across the system.
     * 
     * @param dayOfWeek The day of the week
     * @param isDay1 Whether the day is Day 1
     * @return The DayPattern for the given day and Day1/Day2 status
     */
    private DayPattern getDayPattern(DayOfWeek dayOfWeek, boolean isDay1) {
        return switch (dayOfWeek) {
            case MONDAY -> isDay1 ? DayPattern.MONDAY_DAY1 : DayPattern.MONDAY_DAY2;
            case TUESDAY -> isDay1 ? DayPattern.TUESDAY_DAY1 : DayPattern.TUESDAY_DAY2;
            case WEDNESDAY -> isDay1 ? DayPattern.WEDNESDAY_DAY1 : DayPattern.WEDNESDAY_DAY2;
            case THURSDAY -> isDay1 ? DayPattern.THURSDAY_DAY1 : DayPattern.THURSDAY_DAY2;
            case FRIDAY -> isDay1 ? DayPattern.FRIDAY_DAY1 : DayPattern.FRIDAY_DAY2;
            default -> null;
        };
    }

    /**
     * Prints pattern counts for debugging and verification.
     * Shows how many instances of each pattern exist in each term.
     */
    private void printPatternCounts() {
        for (int term = 0; term < 4; term++) {
            System.out.println("\nTerm " + term + " pattern counts:");
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    DayPattern day1Pattern = getDayPattern(day, true);
                    DayPattern day2Pattern = getDayPattern(day, false);
                    System.out.printf("%s: %d%n", day1Pattern, 
                        termPatternGroups.get(term).getOrDefault(day1Pattern, Collections.emptyList()).size());
                    System.out.printf("%s: %d%n", day2Pattern,
                        termPatternGroups.get(term).getOrDefault(day2Pattern, Collections.emptyList()).size());
                }
            }
        }
    }

    // /**
    //  * Prints a summary of teachers who haven't reached their maximum duties
    //  * 
    //  * Note:
    //  * - This likely is printing out inaccurate information, as the teachers are per semester is being reset before this may be called
    //  */
    // private void printTeacherDutyCounts() {
    //     List<Teacher> teachersUnderMax = teachers.stream()
    //         .filter(t -> t.getDutiesThisSemester() < t.getMaxDutiesPerSemester())
    //         .collect(Collectors.toList());
            
    //     if (!teachersUnderMax.isEmpty()) {
    //         System.out.println("\nTeachers Under Maximum Duties:");
    //         System.out.println("=".repeat(50));
    //         System.out.printf("%-30s | %s/%s%n", "Teacher Name", "Current", "Max");
    //         System.out.println("-".repeat(50));
            
    //         for (Teacher teacher : teachersUnderMax) {
    //             System.out.printf("%-30s | %d/%d%n", 
    //                 teacher.getName(),
    //                 teacher.getDutiesThisSemester(),
    //                 teacher.getMaxDutiesPerSemester());
    //         }
    //         System.out.println("=".repeat(50));
    //     }
    // }

    /**
     * Final pass assignment method that allows two teachers per duty
     * 
     * @param teacher The teacher to assign the duties to
     */
    private void assignDutiesForTeacherFinalPass(Teacher teacher) {
        if (teacher.getMaxDutiesPerSemester() == 0) {
            
            // DEBUGGING, prints the teacher name, if they have no duties
            //System.out.println("Teacher " + teacher.getName() + " has no duties");
            return;
        }
        // DEBUGGING, prints the teacher name, if they are being assigned duties in the final pass
        //System.out.println("Final pass for teacher: " + teacher.getName());
        List<String> classSchedule = teacher.getSchedule();
        
        for (int semester = 0; semester < 2; semester++) {
            // Skip if teacher has no classes this semester
            List<String> semesterClasses = semester == 0 ? 
                classSchedule.subList(0, 5) : classSchedule.subList(5, 10);
            if (semesterClasses.equals(Arrays.asList("", "", "", "", ""))) {
                continue;
            }
            
            int numberOfDutiesNeeded = teacher.getMaxDutiesPerSemester();
            List<List<Day>> termDays = getTermDaysForSemester(semester);
            
            // Try to assign duties in each term, loosening 1 teacher per duty rule to 2
            // TODO: this may be where the incorrect assignments are happening, due to the way the terms are being processed, resetting the max number of duties per semester
            for (int termIndex = 0; termIndex < termDays.size(); termIndex++) {
                if (teacher.getDutiesThisSemester() >= numberOfDutiesNeeded) break;
                assignDutiesInTermFinalPass(teacher, termDays.get(termIndex), semester * 2 + termIndex, numberOfDutiesNeeded);
            }
        }
    }

    /**
     * Attempts to assign duties to a teacher in a specific term
     * 
     * @param teacher The teacher to assign the duties to
     * @param daysInTerm The days in the term to assign the duties to
     * @param term The term to assign the duties to
     * @param numberOfDutiesNeeded The maximum number of duties needed
     * @return boolean indicating if any duties were assigned
     */
    private boolean assignDutiesInTermFinalPass(Teacher teacher, List<Day> daysInTerm, int term, int numberOfDutiesNeeded) {
        for (Day day : daysInTerm) {
            if (teacher.getDutiesThisSemester() >= numberOfDutiesNeeded) return false;
            
            Duty[][] dutySchedule = day.getDutySchedule();
            for (int timeSlot = 0; timeSlot < dutySchedule.length; timeSlot++) {
                for (int pos = 0; pos < dutySchedule[timeSlot].length; pos++) {
                    if (tryAssignDutyToTeacherFinalPass(teacher, day, dutySchedule[timeSlot][pos], term, numberOfDutiesNeeded)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Attempts to assign a specific duty to a teacher in a specific term
     * 
     * @param teacher The teacher to assign the duty to
     * @param day The day containing the duty
     * @param duty The specific duty to assign
     * @param term The term to assign the duty to
     * @param numberOfDutiesNeeded The maximum number of duties needed
     * @return boolean indicating if the duty was assigned
     * 
     * Note: this can be consolidated with tryAssignDutyToTeacher, but I'm keeping it separate
     */
    private boolean tryAssignDutyToTeacherFinalPass(Teacher teacher, Day day, Duty duty, int term, int numberOfDutiesNeeded) {
        if (duty == null || teacher.hasDutyAssigned(duty)) return false;
        
        // Skip if duty already has two teachers assigned for this day type
        if (day.isDay1() && duty.getDay1Teachers().size() >= 2) return false;
        if (!day.isDay1() && duty.getDay2Teachers().size() >= 2) return false;
        
        // Skip if teacher cannot do this duty
        int timeSlot = DutyAssignmentRules.getTimeSlot(duty.getTimeSlot());
        if (!DutyAssignmentRules.canAssignDuty(teacher, timeSlot)) return false;
        
        DayPattern pattern = getDayPattern(day.getDate().getDayOfWeek(), day.isDay1());
        int patternCount = termPatternGroups.get(term).get(pattern).size();
        
        // Check if assigning this duty would exceed the semester limit
        if (teacher.getDutiesThisSemester() + patternCount <= numberOfDutiesNeeded) {
            teacher.assignDuty(duty, patternCount);
            
            // Add teacher to the pattern's duty
            if (day.isDay1()) {
                duty.addDay1Teacher(teacher.getName());
            } else {
                duty.addDay2Teacher(teacher.getName());
            }
            // DEBUGGING, prints the duty assignment
            // System.out.printf("Final pass assigned duty: %s to teacher: %s (worth %d duties, total now: %d, Day %s)%n",
            //     duty.getName(), teacher.getName(), patternCount, teacher.getDutiesThisSemester(),
            //     day.isDay1() ? "1" : "2");
            
            return true;
        }
        
        return false;
    }
} 