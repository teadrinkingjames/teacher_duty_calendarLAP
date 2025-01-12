package com.jamesdegroot.duty_assigment;

import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import com.jamesdegroot.GenerateDutyCalendar;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;

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

    public DutyAssigner(Calendar calendar, List<Teacher> teachers) {
        this.calendar = calendar;
        this.teachers = teachers;
        this.dayGroups = new EnumMap<>(DayPattern.class);
        // Initialize all groups with empty lists
        for (DayPattern pattern : DayPattern.values()) {
            dayGroups.put(pattern, new ArrayList<>());
        }
    }

    // ===== Main Public Methods =====

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
     * 
     * QoL Note: To modify duty priorities or assignment rules:
     * - Check tryAssignDutyToTeacher() for duty type filtering
     * - Adjust assignDutiesForTeacher() for semester/term balancing
     */
    public void assignDuties() {
        System.out.println("Starting duty assignment...\n");
        
        List<Day> schoolDays = getSchoolDays();
        initializeTermPatternGroups(schoolDays);
        printPatternCounts();
        
        // Create a copy of the teachers list and shuffle it
        List<Teacher> shuffledTeachers = new ArrayList<>(teachers);
        Collections.shuffle(shuffledTeachers);
        
        for (Teacher teacher : shuffledTeachers) {
            assignDutiesForTeacher(teacher);
        }
        
        System.out.println("Duty assignment completed!");
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
     * 
     * QoL Note: To modify the schedule format:
     * - Adjust column widths in the printf statements
     * - Modify separator lengths (currently 135 chars)
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
     * Loads test data automatically from resource files
     */
    public static void loadTestData(GenerateDutyCalendar appLogic) {
        try {
            // Get resource URLs
            java.net.URL calendarUrl = DutyAssigner.class.getClassLoader()
                .getResource("ICalendarHandler.ics");
            java.net.URL teacherUrl = DutyAssigner.class.getClassLoader()
                .getResource("Copy Teacher Linear from 2023-2024 - Sheet1.csv");
            
            if (calendarUrl == null || teacherUrl == null) {
                System.err.println("Could not find resource files");
                return;
            }
            
            // Create file objects from URLs
            java.io.File calendarFile = new java.io.File(calendarUrl.toURI());
            java.io.File teacherFile = new java.io.File("C:\\Users\\teadr\\git\\JAVA\\Gr12\\teacher_duty_calendarPC-master\\src\\main\\resources\\Copy Teacher Linear from 2023-2024 - Sheet1.csv");
            
            // Load the files using the app logic
            appLogic.loadCalendar(calendarFile);
            appLogic.processFile(teacherFile);
            
            System.out.println("Test data loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Core Duty Assignment Methods =====

    /**
     * Assigns duties to a single teacher across both semesters.
     * 
     * Process per semester:
     * 1. Reset duty counts for new semester
     * 2. Skip if teacher has no classes this semester
     * 3. First pass: Try to assign non-Hall duties across both terms
     * 4. Second pass: If needed, assign Hall duties to meet quota
     * 
     * QoL Note: 
     * - Modify numberOfDutiesNeeded calculation to adjust workload
     * - Adjust the order of term processing to change distribution
     * 
     * @param teacher The teacher to assign duties to
     */
    private void assignDutiesForTeacher(Teacher teacher) {
        if (teacher.getMaxDutiesPerSemester() == 0) {
            System.out.println("Teacher " + teacher.getName() + " has no duties");
            return;
        }
        
        System.out.println("Teacher: " + teacher.getName());
        List<String> classSchedule = teacher.getSchedule();
        
        for (int semester = 0; semester < 2; semester++) {
            teacher.resetDutiesForNewSemester();
            
            // Skip if teacher has no classes this semester
            List<String> semesterClasses = semester == 0 ? 
                classSchedule.subList(0, 5) : classSchedule.subList(5, 10);
            if (semesterClasses.equals(Arrays.asList("", "", "", "", ""))) {
                System.out.println("Teacher " + teacher.getName() + " has no classes in semester " + (semester+1));
                continue;
            }
            
            System.out.println("Teacher " + teacher.getName() + " has classes in semester " + (semester+1));
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
     * QoL Note:
     * - Modify the order of timeSlot/pos iteration to prioritize certain periods
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
     * QoL Note:
     * - Modify the isHall check to change what counts as a Hall duty
     * - Adjust the logging format for better debugging
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
            
            String dutyType = isHallDuty ? "hall duty" : "duty";
            System.out.println("Assigned " + dutyType + ": " + duty.getName() + " to teacher: " + teacher.getName() +
                " (worth " + patternCount + " duties, total now: " + teacher.getDutiesThisSemester() + 
                ", Day " + (day.isDay1() ? "1" : "2") + ")");
            
            return true;
        }
        
        return false;
    }

    // ===== Pattern and Term Management Methods =====

    /**
     * Initializes the term pattern groups data structure.
     * Groups school days by term and pattern for efficient access.
     * 
     * Structure:
     * termPatternGroups[term][pattern] = List of days matching that pattern
     * 
     * QoL Note:
     * - Modify the grouping logic here to change how days are organized
     * - Add additional filtering criteria if needed
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
     * QoL Note:
     * - Modify to change how terms are grouped or ordered
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

    // ===== Utility Methods =====

    /**
     * Gets all school days from the calendar.
     * Filters out weekends and holidays.
     * 
     * QoL Note:
     * - Add additional filtering here if needed
     * - Modify to change what counts as a school day
     */
    private List<Day> getSchoolDays() {
        return calendar.getDaysOfYear().stream()
            .filter(Day::isSchoolDay)
            .toList();
    }

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
     * QoL Note:
     * - Add new patterns here if schedule structure changes
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
     * 
     * QoL Note:
     * - Modify output format for different debugging needs
     * - Add additional statistics as needed
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
} 