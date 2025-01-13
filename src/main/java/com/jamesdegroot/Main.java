package com.jamesdegroot;

import com.jamesdegroot.gui.AppWindow;
import com.jamesdegroot.duty_assigment.DutyAssigner;

/**
 * Main.java
 * Name: James de Groot
 * Date: January 13, 2025
 *
 * 	Purpose: 
 *    Main class, used to start the application
 *      
 *  Methods:
 * 	  - main, void, starts the application
 * 
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
        
        window.show();
    }
} 