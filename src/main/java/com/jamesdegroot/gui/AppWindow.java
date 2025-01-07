package com.jamesdegroot.gui;

import com.jamesdegroot.GenerateDutyCalendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.teacher.Teacher;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Font;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * This class contains the main window for the application.
 * 
 */
public class AppWindow {
    // Window dimensions and positioning
    private static final int WINDOW_WIDTH = 1025;
    private static final int WINDOW_HEIGHT = 1100;
    private static final int WINDOW_X = 100;
    private static final int WINDOW_Y = 100;
    
    // UI component dimensions
    private static final int INPUT_FIELD_COLUMNS = 40;
    private static final int CALENDAR_FIELD_COLUMNS = 35;
    private static final int CONSOLE_WIDTH = 800;
    private static final int CONSOLE_HEIGHT = 600;
    
    // Font settings
    private static final String DEFAULT_FONT_FAMILY = "Arial";
    private static final String CONSOLE_FONT_FAMILY = "Monospaced";
    private static final int DEFAULT_FONT_SIZE = 14;
    private static final int CONSOLE_FONT_SIZE = 12;
    
    // UI text constants
    private static final String WINDOW_TITLE = "Teacher Duty Scheduler";
    private static final String TEACHER_INPUT_PLACEHOLDER = "Click Browse and enter path to teacher schedule file";
    private static final String CALENDAR_INPUT_PLACEHOLDER = "Click Browse and enter path to calendar file";
    private static final String CSV_FILTER_DESCRIPTION = "CSV Files (*.csv)";
    private static final String ICS_FILTER_DESCRIPTION = "Calendar Files (*.ics)";
    private static final String CSV_EXTENSION = "csv";
    private static final String ICS_EXTENSION = "ics";
    
    // Button text
    private static final String BROWSE_BUTTON_TEXT = "Browse";
    private static final String HELP_BUTTON_TEXT = "Help";
    private static final String EDIT_TEACHER_BUTTON_TEXT = "Edit Teacher";
    private static final String EDIT_DAY_BUTTON_TEXT = "Edit Day";
    private static final String SHOW_CONSOLE_TEXT = "Show Console";
    private static final String HIDE_CONSOLE_TEXT = "Hide Console";
    private static final String ASSIGN_DUTIES_TEXT = "Assign Duties";
    
    // Layout constants
    private static final int LAYOUT_GAP = 5;
    private static final int BORDER_PADDING = 25;
    private static final int TOP_PADDING = 10;
    private static final int BOTTOM_PADDING = 10;
    
    // Panel names for CardLayout
    private static final String CONSOLE_PANEL = "console";
    private static final String EMPTY_PANEL = "empty";

    private JFrame frame;
    private JPanel mainPanel;
    private JFileChooser fileChooser;
    private JTextField inputField;
    private JTextArea consoleOutput;
    private String userInput;
    private GenerateDutyCalendar appLogic;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private FileNameExtensionFilter csvFilter;
    private FileNameExtensionFilter icsFilter;

    /**
     * Constructor for the AppWindow class.
     * @param appLogic the logic for the application
     */
    public AppWindow(GenerateDutyCalendar appLogic) {
        this.appLogic = appLogic;
        initialize();
        redirectSystemOut();
    }

    /**
     * Initializes the main window for the application.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle(WINDOW_TITLE);
        frame.setResizable(true);
        frame.setUndecorated(false);
        frame.setBounds(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        String downloadsFolderPath = System.getProperty("user.home") + File.separator + "Downloads";
        fileChooser.setCurrentDirectory(new File(downloadsFolderPath));
        
        csvFilter = new FileNameExtensionFilter(CSV_FILTER_DESCRIPTION, CSV_EXTENSION);
        icsFilter = new FileNameExtensionFilter(ICS_FILTER_DESCRIPTION, ICS_EXTENSION);
        
        mainPanel = new JPanel(new BorderLayout(LAYOUT_GAP, LAYOUT_GAP));
        frame.setContentPane(mainPanel);
        
        cardPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) cardPanel.getLayout();
        
        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font(CONSOLE_FONT_FAMILY, Font.PLAIN, CONSOLE_FONT_SIZE));
        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        
        JPanel paddedConsolePanel = new JPanel(new BorderLayout());
        paddedConsolePanel.add(scrollPane);
        paddedConsolePanel.setBorder(BorderFactory.createEmptyBorder(0, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        scrollPane.setPreferredSize(new Dimension(CONSOLE_WIDTH, CONSOLE_HEIGHT));
        
        cardPanel.add(paddedConsolePanel, CONSOLE_PANEL);
        cardPanel.add(new JPanel(), EMPTY_PANEL);
        
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        addInputField();
        
        // Add assign duties button at the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, LAYOUT_GAP, LAYOUT_GAP));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, BOTTOM_PADDING, 0));
        
        JButton assignDutiesButton = new JButton(ASSIGN_DUTIES_TEXT);
        styleButton(assignDutiesButton);
        assignDutiesButton.addActionListener(e -> assignDuties());
        bottomPanel.add(assignDutiesButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Redirects the system output to the console output.
     */
    private void redirectSystemOut() {
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleOutput));
        System.setOut(printStream);
        System.setErr(printStream);
    }

    /**
     * Shows the main window.
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Adds the input field to the main window.
     */
    private void addInputField() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 1, LAYOUT_GAP, LAYOUT_GAP));
        
        // First row - Teacher Schedule File
        JPanel teacherPanel = new JPanel();
        teacherPanel.setLayout(new BorderLayout(LAYOUT_GAP, 0));
        teacherPanel.setBorder(BorderFactory.createEmptyBorder(LAYOUT_GAP, BORDER_PADDING, LAYOUT_GAP, BORDER_PADDING));
        
        inputField = new JTextField(INPUT_FIELD_COLUMNS) {
            @Override
            public boolean isFocusable() {
                return false;
            }
        };
        inputField.setText(TEACHER_INPUT_PLACEHOLDER);
        inputField.setFont(new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, DEFAULT_FONT_SIZE));
        inputField.setForeground(Color.BLACK);
        inputField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                inputField.setFocusable(true);
                inputField.requestFocusInWindow();
            }
        });
        
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (inputField.getText().equals(TEACHER_INPUT_PLACEHOLDER)) {
                    inputField.setText("");
                    inputField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText(TEACHER_INPUT_PLACEHOLDER);
                    inputField.setForeground(Color.GRAY);
                }
                inputField.setFocusable(false);
            }
        });
        
        JButton teacherBrowseButton = new JButton(BROWSE_BUTTON_TEXT);
        styleButton(teacherBrowseButton);
        
        teacherPanel.add(inputField, BorderLayout.CENTER);
        teacherPanel.add(teacherBrowseButton, BorderLayout.EAST);
        
        // Second row - Calendar File
        JPanel calendarPanel = new JPanel();
        calendarPanel.setLayout(new BorderLayout(LAYOUT_GAP, 0));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(LAYOUT_GAP, BORDER_PADDING, LAYOUT_GAP, BORDER_PADDING));
        
        JTextField calendarField = new JTextField(CALENDAR_FIELD_COLUMNS) {
            @Override
            public boolean isFocusable() {
                return false;
            }
        };
        calendarField.setText(CALENDAR_INPUT_PLACEHOLDER);
        calendarField.setFont(new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, DEFAULT_FONT_SIZE));
        calendarField.setForeground(Color.BLACK);
        calendarField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                calendarField.setFocusable(true);
                calendarField.requestFocusInWindow();
            }
        });
        
        calendarField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (calendarField.getText().equals(CALENDAR_INPUT_PLACEHOLDER)) {
                    calendarField.setText("");
                    calendarField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (calendarField.getText().isEmpty()) {
                    calendarField.setText(CALENDAR_INPUT_PLACEHOLDER);
                    calendarField.setForeground(Color.GRAY);
                }
                calendarField.setFocusable(false);
            }
        });
        
        JButton calendarBrowseButton = new JButton(BROWSE_BUTTON_TEXT);
        styleButton(calendarBrowseButton);
        
        calendarPanel.add(calendarField, BorderLayout.CENTER);
        calendarPanel.add(calendarBrowseButton, BorderLayout.EAST);
        
        // Add toggle button panel
        JPanel togglePanel = new JPanel(new BorderLayout(LAYOUT_GAP, 0));
        togglePanel.setBorder(BorderFactory.createEmptyBorder(0, BORDER_PADDING, 0, BORDER_PADDING));
        
        JButton helpButton = new JButton(HELP_BUTTON_TEXT);
        JButton toggleConsoleButton = new JButton(HIDE_CONSOLE_TEXT);
        styleButton(helpButton);
        styleButton(toggleConsoleButton);
        
        // Create help button action
        helpButton.addActionListener(e -> showHelpDialog());
        
        // Add toggle console action
        toggleConsoleButton.addActionListener(e -> {
            boolean isVisible = toggleConsoleButton.getText().equals(HIDE_CONSOLE_TEXT);
            cardLayout.show(cardPanel, isVisible ? EMPTY_PANEL : CONSOLE_PANEL);
            toggleConsoleButton.setText(isVisible ? SHOW_CONSOLE_TEXT : HIDE_CONSOLE_TEXT);
            frame.revalidate();
        });
        
        // Add buttons to toggle panel
        JPanel leftButtonPanel = new JPanel(new BorderLayout(LAYOUT_GAP, 0));
        leftButtonPanel.add(helpButton, BorderLayout.WEST);
        
        JPanel centerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, LAYOUT_GAP, 0));
        JButton editTeacherButton = new JButton(EDIT_TEACHER_BUTTON_TEXT);
        styleButton(editTeacherButton);
        editTeacherButton.addActionListener(e -> showTeacherEditor());
        JButton editDayButton = new JButton(EDIT_DAY_BUTTON_TEXT);
        styleButton(editDayButton);
        editDayButton.addActionListener(e -> showDayEditor());
        centerButtonPanel.add(editTeacherButton);
        centerButtonPanel.add(editDayButton);
        leftButtonPanel.add(centerButtonPanel, BorderLayout.CENTER);
        
        JPanel rightButtonPanel = new JPanel(new BorderLayout());
        rightButtonPanel.add(toggleConsoleButton, BorderLayout.EAST);
        
        togglePanel.add(leftButtonPanel, BorderLayout.WEST);
        togglePanel.add(rightButtonPanel, BorderLayout.EAST);
        
        // Add action listeners
        teacherBrowseButton.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(csvFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.getName().toLowerCase().endsWith(".csv")) {
                    inputField.setText(selectedFile.getAbsolutePath());
                    userInput = selectedFile.getAbsolutePath();
                    appLogic.processFile(selectedFile);
                    System.out.println("Selected teacher file: " + userInput);
                } else {
                    System.err.println("Error: Please select a CSV file for teacher schedules");
                }
            }
        });
        
        calendarBrowseButton.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.setFileFilter(icsFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile.getName().toLowerCase().endsWith(".ics")) {
                    calendarField.setText(selectedFile.getAbsolutePath());
                    appLogic.loadCalendar(selectedFile);
                    System.out.println("Selected calendar file: " + selectedFile.getAbsolutePath());
                } else {
                    System.err.println("Error: Please select an ICS file for calendar");
                }
            }
        });
        
        // Add all panels
        inputPanel.add(teacherPanel);
        inputPanel.add(calendarPanel);
        inputPanel.add(togglePanel);
        
        // Add padding around the entire input section
        JPanel paddedPanel = new JPanel(new BorderLayout());
        paddedPanel.add(inputPanel, BorderLayout.NORTH);
        paddedPanel.setBorder(BorderFactory.createEmptyBorder(TOP_PADDING, 0, TOP_PADDING, 0));
        
        mainPanel.add(paddedPanel, BorderLayout.NORTH);
        
        // Move focus to frame initially
        frame.requestFocusInWindow();
    }

    /**
     * Shows the help dialog with application instructions.
     */
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(frame, "Application Help", true);
        helpDialog.setLayout(new BorderLayout(10, 10));
        
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setFont(new Font("Dialog", Font.PLAIN, 14));
        helpText.setText(
            "Teacher Duty Scheduler - Help Guide\n\n" +
            "1. Loading Teacher Schedule:\n" +
            "   - Click 'Browse' next to 'Teacher Schedule' to select your CSV file\n" +
            "   - The file should contain teacher names and their teaching schedules\n\n" +
            "2. Loading Calendar:\n" +
            "   - Click 'Browse' next to 'Calendar File' to select your ICS calendar file\n" +
            "   - The calendar should contain school events, holidays, and PA days\n\n" +
            "3. Viewing Output:\n" +
            "   - Use the 'Show/Hide Console' button to toggle the console view\n" +
            "   - The console displays teacher schedules and calendar information\n\n" +
            "4. Understanding Results:\n" +
            "   - Teacher schedules show their teaching load and type\n" +
            "   - Calendar view shows school days and non-school days\n" +
            "   - Warnings will appear for teachers with unusual schedules"
        );
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> helpDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        helpDialog.add(scrollPane, BorderLayout.CENTER);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        helpDialog.pack();
        helpDialog.setLocationRelativeTo(frame);
        helpDialog.setVisible(true);
    }

    /**
     * Shows the teacher profile editor dialog.
     */
    private void showTeacherEditor() {
        if (appLogic.getTeachers() == null || appLogic.getTeachers().isEmpty()) {
            JOptionPane.showMessageDialog(frame, 
                "Please load a teacher schedule file first.", 
                "No Teachers Loaded", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Teacher firstTeacher = appLogic.getTeachers().get(0);
        TeacherProfileEditor editor = new TeacherProfileEditor(frame, firstTeacher, appLogic.getTeachers());
        editor.setVisible(true);
    }

    /**
     * Shows the day editor dialog.
     */
    private void showDayEditor() {
        if (appLogic.getCalendar() == null || appLogic.getCalendar().getDaysOfYear().isEmpty()) {
            JOptionPane.showMessageDialog(frame, 
                "Please load a calendar file first.", 
                "No Calendar Loaded", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<Day> days = appLogic.getCalendar().getDaysOfYear();
        DayEditor editor = new DayEditor(frame, days.get(0), days);
        editor.setVisible(true);
    }

    /**
     * Handles the duty assignment process
     */
    private void assignDuties() {
        if (appLogic.getTeachers() == null || appLogic.getTeachers().isEmpty()) {
            JOptionPane.showMessageDialog(frame, 
                "Please load a teacher schedule file first.", 
                "No Teachers Loaded", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (appLogic.getCalendar() == null || appLogic.getCalendar().getDaysOfYear().isEmpty()) {
            JOptionPane.showMessageDialog(frame, 
                "Please load a calendar file first.", 
                "No Calendar Loaded", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show processing message
        System.out.println("\nAssigning duties for all terms...");
        System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
        
        // Perform duty assignment
        appLogic.assignDuties();
        
        // Show completion message
        System.out.println("\nDuty assignment completed!");
        System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
        
        // Print the final schedule
        appLogic.printSchedule();
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
    }
}

/**
 * Custom output stream for redirecting system output to the console output.
 */
class CustomOutputStream extends OutputStream {
    private JTextArea textArea;
    /**
     * Constructor for the CustomOutputStream class.
     * @param textArea the text area to write to
     */
    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    /**
     * Writes a byte to the text area.
     * @param b the byte to write
     */
    @Override
    public void write(int b) {
        textArea.append(String.valueOf((char)b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
