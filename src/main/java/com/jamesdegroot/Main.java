package com.jamesdegroot;

import com.jamesdegroot.gui.AppWindow;

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
        window.show();
    }
} 