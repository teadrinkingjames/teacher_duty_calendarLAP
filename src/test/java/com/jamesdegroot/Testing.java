package com.jamesdegroot;

import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.jamesdegroot.calendar.Calendar;
import com.jamesdegroot.calendar.Day;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/*
 * This is the testing class for the application.
 * contain the test cases for the application.  
 */
public class Testing {
    private Calendar calendar;
    
    @Before
    public void setUp() {
        calendar = new Calendar();
        calendar.loadFromICS("src/test/resources/ICalendarHandler.ics");
    }
    
    @Test
    public void testCalendarLoading() {
        // Verify that events were loaded
        assertFalse("Calendar should not be empty", calendar.getEvents().isEmpty());
    }
    
    @Test
    public void testPADay() {
        // Test a known PA Day (September 27, 2024)
        LocalDate paDay = LocalDate.of(2024, 9, 27);
        assertFalse("September 27, 2024 should not be a school day (PA Day)", 
            calendar.isSchoolDay(paDay));
    }
    
    @Test
    public void testHoliday() {
        // Test a known holiday (October 14, 2024 - Thanksgiving)
        LocalDate holiday = LocalDate.of(2024, 10, 14);
        assertFalse("October 14, 2024 should not be a school day (Holiday)", 
            calendar.isSchoolDay(holiday));
    }
    
    @Test
    public void testRegularSchoolDay() {
        // Test a regular school day (October 1, 2024)
        LocalDate schoolDay = LocalDate.of(2024, 10, 1);
        assertTrue("October 1, 2024 should be a school day", 
            calendar.isSchoolDay(schoolDay));
    }
    
    @Test
    public void testWeekend() {
        // Test a weekend (September 28, 2024 - Saturday)
        LocalDate weekend = LocalDate.of(2024, 9, 28);
        assertFalse("September 28, 2024 should not be a school day (Weekend)", 
            calendar.isSchoolDay(weekend));
    }
    
    @Test
    public void testExamPeriod() {
        // Test during exam period (January 24, 2025)
        LocalDate examDay = LocalDate.of(2025, 1, 24);
        assertFalse("January 24, 2025 should not be a regular school day (Exams)", 
            calendar.isSchoolDay(examDay));
    }

    @Test
    public void testDaysOfYear() {
        Calendar calendar = new Calendar();
        calendar.initializeDaysOfYear();
        List<Day> days = calendar.getDaysOfYear();
        
        System.out.println("\n========= Days of 2024 =========");
        days.forEach(day -> {
            System.out.println(day.getDate().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + 
                (day.isSchoolDay() ? " (School Day)" : " (No School)") +
                (day.isHoliday() ? " - Holiday" : ""));
        });
        System.out.println("================================\n");
        
        assertEquals("Should have 366 days in 2024", 366, days.size());
        assertEquals("First day should be Jan 1", 
            LocalDate.of(2024, 1, 1), 
            days.get(0).getDate());
        assertEquals("Last day should be Dec 31", 
            LocalDate.of(2024, 12, 31), 
            days.get(days.size()-1).getDate());
    }
}