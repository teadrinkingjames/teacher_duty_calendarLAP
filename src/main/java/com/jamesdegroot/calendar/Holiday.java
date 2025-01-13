package com.jamesdegroot.calendar;

import java.time.LocalDate;

/**
 * Holiday.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    Holiday class, used to store the holiday and the start and end dates of the holiday
 *      
 *  Methods:
 * 	  - Holiday, constructor, creates a holiday object with the given summary, start date, end date, and description
 *    - getSummary, String, gets the summary of the holiday
 *    - getStartDate, LocalDate, gets the start date of the holiday
 *    - getEndDate, LocalDate, gets the end date of the holiday
 *    - getDescription, String, gets the description of the holiday
 *    - isSchoolDay, boolean, checks if the holiday is a school day
*/

public class Holiday {
    private String summary;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    /**
     * Constructs a new Holiday object.
     * @param summary the summary of the holiday
     * @param startDate the start date of the holiday
     * @param endDate the end date of the holiday
     * @param description the description of the holiday
     */
    public Holiday(String summary, LocalDate startDate, LocalDate endDate, String description) {
        this.summary = summary;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    /**
     * Gets the summary of the holiday.
     * @return the summary of the holiday
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Gets the start date of the holiday.
     * @return the start date of the holiday
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Gets the end date of the holiday.
     * @return the end date of the holiday
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Gets the description of the holiday.
     * @return the description of the holiday
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if the holiday is a school day.
     * @return true if the holiday is a school day, false otherwise
     */
    public boolean isSchoolDay() {
        String summaryUpper = summary.toUpperCase();
        return !summaryUpper.contains("PA DAY") && 
               !summaryUpper.contains("P.A. DAY") &&
               !summaryUpper.contains("HOLIDAY") && 
               !summaryUpper.contains("EXAM") &&
               !summaryUpper.contains("BREAK") &&
               !summaryUpper.contains("SUMMER") &&
               !summaryUpper.contains("WINTER") &&
               !summaryUpper.contains("MARCH") &&
               !summaryUpper.contains("CHRISTMAS") &&
               !summaryUpper.contains("THANKSGIVING");
    }

    /**
     * Returns a string representation of the holiday.
     * @return a string representation of the holiday
     */
    @Override
    public String toString() {
        return String.format("%s: %s to %s", summary, startDate, endDate);
    }
}
