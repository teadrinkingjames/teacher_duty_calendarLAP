package com.jamesdegroot.calendar;

import java.util.ArrayList;
import java.util.List;

public class Duty {
    // Duty rotation constants
    private static final String DAY_1_PREFIX = "D1";
    private static final String DAY_2_PREFIX = "D2";
    
    // Time slot constants
    private static final String PERIOD_1 = "[per 1]";
    private static final String PERIOD_2 = "[per 2]";
    private static final String LUNCH_A = "[Lun A]";
    private static final String LUNCH_B = "[Lun B]";
    private static final String PERIOD_3 = "[per 3]";
    private static final String PERIOD_4 = "[per 4]";
    
    // Location constants
    private static final String HALL = "Hall";
    private static final String CAFETERIA = "Cafeteria";
    private static final String LIBRARY = "Library";
    private static final String DDP_ROOM = "DDP Rm 209";
    
    // Simple array of duty names - organized by time slots
    public static final String[] DUTY_NAMES = {
        // Period 1
        PERIOD_1 + " " + HALL,
        
        // Period 2
        PERIOD_2 + " " + HALL,
        
        // Lunch A
        LUNCH_A + " " + CAFETERIA,
        LUNCH_A + " " + LIBRARY,
        LUNCH_A + " " + DDP_ROOM,
        
        // Lunch B
        LUNCH_B + " " + CAFETERIA,
        LUNCH_B + " " + LIBRARY,
        
        // Period 3
        PERIOD_3 + " " + HALL,
        PERIOD_3 + " " + LIBRARY,
        
        // Period 4
        PERIOD_4 + " " + HALL,
        PERIOD_4 + " " + LIBRARY
    };
    
    private String name;            // Name of the duty (e.g., "[per 1] Hall")
    private String description;     // Additional details about the duty
    private List<String> day1Teachers;  // Teachers assigned for Day 1
    private List<String> day2Teachers;  // Teachers assigned for Day 2
    private String room;           // Location of the duty
    private String timeSlot;       // Time period of the duty
    
    public Duty(String name, String description, String room, String time) {
        this.name = name;
        this.description = description;
        this.day1Teachers = new ArrayList<>();
        this.day2Teachers = new ArrayList<>();
        this.room = room;
        this.timeSlot = time;
    }   

    // Modified getters and setters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getDay1Teachers() {
        return day1Teachers;
    }

    public List<String> getDay2Teachers() {
        return day2Teachers;
    }

    public String getTeacher() {
        // For backward compatibility, returns teachers as comma-separated string
        List<String> teachers = day1Teachers.isEmpty() ? day2Teachers : day1Teachers;
        return teachers.isEmpty() ? "UNASSIGNED" : String.join(", ", teachers);
    }

    public String getRoom() {
        return room;
    }

    public String getTimeSlot() {
        return timeSlot;
    }   

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }   

    public void addDay1Teacher(String teacher) {
        if (teacher != null && !teacher.isEmpty()) {
            day1Teachers.add(teacher);
        }
    }

    public void addDay2Teacher(String teacher) {
        if (teacher != null && !teacher.isEmpty()) {
            day2Teachers.add(teacher);
        }
    }

    public void setTeacher(String teacher) {
        // For backward compatibility
        day1Teachers.clear();
        day2Teachers.clear();
        if (teacher != null && !teacher.isEmpty()) {
            day1Teachers.add(teacher);
            day2Teachers.add(teacher);
        }
    }

    public void setRoom(String room) {
        this.room = room;
    }   

    public void setTimeSlot(String time) {
        this.timeSlot = time;
    }   
}
