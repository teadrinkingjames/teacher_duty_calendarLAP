package com.jamesdegroot;

import com.jamesdegroot.gui.AppWindow;
import com.jamesdegroot.duty_assigment.DutyAssigner;

/**
 * Main entry point for the Teacher Duty Calendar application.
 */
public class Main {
    /**
     * Initializes and starts the application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        GenerateDutyCalendar appLogic = new GenerateDutyCalendar();
        AppWindow window = new AppWindow(appLogic);
        
        // Load test data automatically
        DutyAssigner.loadTestData(appLogic);
        
        // Disable debug output for better performance
        appLogic.setDebugOutput(false);
        
        window.show();
    }
} 