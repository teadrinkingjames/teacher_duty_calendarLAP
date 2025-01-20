package com.jamesdegroot.io;

import com.jamesdegroot.GenerateDutyCalendar;

/**
 * TestDataLoader.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * Purpose: 
 *    Utility class for loading test data files (calendar and teacher data)
 *    from the resources directory for testing and development purposes.
 *  
 * Methods:
 * 	  - loadTestData, void, loads the test data
 */
public class TestDataLoader {
    
    /**
     * Loads test data automatically from resource files
     */
    public static void loadTestData(GenerateDutyCalendar appLogic) {
        try {
            // Get resource URLs from within the resources folder
            java.net.URL calendarUrl = TestDataLoader.class.getClassLoader()
                .getResource("ICalendarHandler.ics");
            java.net.URL teacherUrl = TestDataLoader.class.getClassLoader()
                .getResource("Copy Teacher Linear from 2023-2024 - Sheet1.csv");
            
            if (calendarUrl == null || teacherUrl == null) {
                System.err.println("Could not find resource files");
                return;
            }
            
            // Create file objects from URLs
            java.io.File calendarFile = new java.io.File(calendarUrl.toURI());
            java.io.File teacherFile = new java.io.File("src/main/resources/Copy Teacher Linear from 2023-2024 - Sheet1.csv");
            
            // Load the files using the app logic
            appLogic.loadCalendar(calendarFile);
            appLogic.processFile(teacherFile);
            
            System.out.println("Test data loaded successfully");
            
        } catch (Exception e) {
            System.err.println("Error loading test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 