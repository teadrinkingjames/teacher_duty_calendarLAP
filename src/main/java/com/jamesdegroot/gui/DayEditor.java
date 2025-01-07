package com.jamesdegroot.gui;

import com.jamesdegroot.GenerateDutyCalendar;
import com.jamesdegroot.calendar.Day;
import com.jamesdegroot.calendar.Duty;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DayEditor extends JDialog {
    // Window constants
    private static final String WINDOW_TITLE = "Day Schedule Editor";
    private static final int DIALOG_PADDING = 10;
    
    // Search panel constants
    private static final int SEARCH_PANEL_PADDING = 5;
    private static final int SEARCH_PANEL_BORDER = 10;
    private static final String SEARCH_LABEL_TEXT = "Search Date:";
    private static final int SEARCH_COMBOBOX_WIDTH = 250;
    private static final int SEARCH_COMBOBOX_HEIGHT = 25;
    
    // Form panel constants
    private static final int FORM_PANEL_PADDING = 10;
    private static final String SCHOOL_DAY_CHECKBOX_TEXT = "School Day";
    private static final String DAY_TYPE_LABEL = "Day Type:";
    private static final String DUTIES_PANEL_TITLE = "Duties";
    
    // Field constants
    private static final int DUTY_NAME_FIELD_WIDTH = 20;
    private static final int ROOM_FIELD_WIDTH = 10;
    private static final int TEACHER_FIELD_WIDTH = 20;
    private static final String NAME_LABEL = "Name:";
    private static final String ROOM_LABEL = "Room:";
    private static final String TEACHER_LABEL = "Teacher:";
    
    // Button constants
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;
    
    // Layout constants
    private static final int FLOW_LAYOUT_GAP = 5;
    private static final int FLOW_LAYOUT_V_GAP = 2;
    private static final int LABEL_WIDTH = 120;
    private static final int LABEL_HEIGHT = 20;
    
    // Display format constants
    private static final String DATE_FORMAT_PATTERN = "EEEE, MMMM d, yyyy";
    private static final String DUTY_FORMAT = "%-12s | %-25s | %-30s | %-20s%n";
    private static final String TIME_SLOT_PREFIX = "Slot ";
    private static final String UNASSIGNED_TEXT = "UNASSIGNED";
    
    private Day day;
    private List<Day> allDays;
    private JComboBox<String> daySearch;
    private JPanel formPanel;
    
    public DayEditor(JFrame parent, Day day, List<Day> allDays) {
        super(parent, WINDOW_TITLE, true);
        this.day = day;
        this.allDays = allDays;
        
        setLayout(new BorderLayout(DIALOG_PADDING, DIALOG_PADDING));
        
        // Create search panel at top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SEARCH_PANEL_PADDING, SEARCH_PANEL_PADDING));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(SEARCH_PANEL_PADDING, SEARCH_PANEL_BORDER, SEARCH_PANEL_PADDING, SEARCH_PANEL_BORDER));
        
        searchPanel.add(new JLabel(SEARCH_LABEL_TEXT));
        String[] dates = allDays.stream()
            .map(d -> d.getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)))
            .toArray(String[]::new);
        daySearch = new JComboBox<>(dates);
        daySearch.setSelectedItem(day.getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        daySearch.setPreferredSize(new Dimension(SEARCH_COMBOBOX_WIDTH, SEARCH_COMBOBOX_HEIGHT));
        daySearch.addActionListener(e -> updateDay());
        searchPanel.add(daySearch);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Create form panel
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PANEL_PADDING, FORM_PANEL_PADDING, FORM_PANEL_PADDING, FORM_PANEL_PADDING));
        
        updateForm();
        
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void updateDay() {
        String selectedDate = (String) daySearch.getSelectedItem();
        LocalDate date = LocalDate.parse(selectedDate, 
            DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        day = allDays.stream()
            .filter(d -> d.getDate().equals(date))
            .findFirst()
            .orElse(day);
        updateForm();
        System.out.println("\nDay Schedule:");
        System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
        System.out.println(day.toString());
        pack();
    }
    
    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
    }
    
    private void updateForm() {
        formPanel.removeAll();
        
        // School Day Checkbox
        JCheckBox schoolDayCheck = new JCheckBox(SCHOOL_DAY_CHECKBOX_TEXT, day.isSchoolDay());
        schoolDayCheck.addActionListener(e -> day.setSchoolDay(schoolDayCheck.isSelected()));
        formPanel.add(createRow(DAY_TYPE_LABEL, schoolDayCheck));
        
        // Duties
        JPanel dutiesPanel = new JPanel();
        dutiesPanel.setLayout(new BoxLayout(dutiesPanel, BoxLayout.Y_AXIS));
        dutiesPanel.setBorder(BorderFactory.createTitledBorder(DUTIES_PANEL_TITLE));
        
        Duty[][] duties = day.getDutySchedule();
        for (int timeSlot = 0; timeSlot < Day.TIME_SLOTS; timeSlot++) {
            for (int position = 0; position < Day.DUTIES_PER_SLOT; position++) {
                Duty duty = duties[timeSlot][position];
                if (duty != null) {
                    JPanel dutyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_GAP, FLOW_LAYOUT_V_GAP));
                    
                    // Duty Name
                    JTextField nameField = new JTextField(duty.getName(), DUTY_NAME_FIELD_WIDTH);
                    dutyPanel.add(createLabeledField(NAME_LABEL, nameField));
                    
                    // Room
                    JTextField roomField = new JTextField(duty.getRoom(), ROOM_FIELD_WIDTH);
                    dutyPanel.add(createLabeledField(ROOM_LABEL, roomField));
                    
                    // Teacher
                    JTextField teacherField = new JTextField(
                        duty.getTeacher() != null ? duty.getTeacher() : "", TEACHER_FIELD_WIDTH);
                    dutyPanel.add(createLabeledField(TEACHER_LABEL, teacherField));
                    
                    dutiesPanel.add(dutyPanel);
                }
            }
        }
        
        formPanel.add(dutiesPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, FLOW_LAYOUT_GAP, FLOW_LAYOUT_GAP));
        JButton saveButton = new JButton(SAVE_BUTTON_TEXT);
        JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        
        // Style buttons
        styleButton(saveButton);
        styleButton(cancelButton);
        
        Dimension buttonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        saveButton.setPreferredSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);
        
        saveButton.addActionListener(e -> {
            // Save changes
            System.out.println("\nUpdated Day Schedule:");
            System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
            System.out.println("Date: " + day.getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
            System.out.println("School Day: " + (day.isSchoolDay() ? "Yes" : "No"));
            if (day.isSchoolDay()) {
                System.out.println("\nDuty Schedule:");
                System.out.println("-".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
                System.out.printf(DUTY_FORMAT, "Time Slot", "Duty", "Location", "Teacher");
                System.out.println("-".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
                Duty[][] dutySchedule = day.getDutySchedule();
                for (int timeSlot = 0; timeSlot < Day.TIME_SLOTS; timeSlot++) {
                    for (int position = 0; position < Day.DUTIES_PER_SLOT; position++) {
                        Duty duty = dutySchedule[timeSlot][position];
                        if (duty != null) {
                            System.out.printf(DUTY_FORMAT,
                                TIME_SLOT_PREFIX + (timeSlot + 1),
                                duty.getName(),
                                duty.getRoom(),
                                duty.getTeacher() != null ? duty.getTeacher() : UNASSIGNED_TEXT);
                        }
                    }
                }
            }
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel);
        
        formPanel.revalidate();
        formPanel.repaint();
    }
    
    private JPanel createRow(String label, JComponent component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_GAP, FLOW_LAYOUT_V_GAP));
        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        row.add(jLabel);
        row.add(component);
        return row;
    }
    
    private JPanel createLabeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_V_GAP, 0));
        panel.add(new JLabel(label));
        panel.add(field);
        return panel;
    }
} 