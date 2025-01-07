package com.jamesdegroot.gui;

import com.jamesdegroot.GenerateDutyCalendar;
import com.jamesdegroot.teacher.Teacher;
import com.jamesdegroot.teacher.TeacherTypeEnum;
import com.jamesdegroot.teacher.TeacherScheduleStatusEnum;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TeacherProfileEditor extends JDialog {
    // Window constants
    private static final String WINDOW_TITLE = "Teacher Profile Editor";
    private static final int DIALOG_PADDING = 10;
    
    // Search panel constants
    private static final int SEARCH_PANEL_PADDING = 5;
    private static final int SEARCH_PANEL_BORDER = 10;
    private static final String SEARCH_LABEL_TEXT = "Search Teacher:";
    private static final int SEARCH_COMBOBOX_WIDTH = 200;
    private static final int SEARCH_COMBOBOX_HEIGHT = 25;
    
    // Form panel constants
    private static final int FORM_PANEL_PADDING = 10;
    
    // Field labels
    private static final String NAME_LABEL = "Name:";
    private static final String TYPE_LABEL = "Type:";
    private static final String STATUS_LABEL = "Status:";
    private static final String TIME_ALLOCATION_LABEL = "Time Allocation:";
    private static final String DUTIES_LABEL = "Duties This Semester:";
    private static final String MAX_DUTIES_LABEL = "Max Duties:";
    private static final String SCHEDULE_LABEL = "Schedule:";
    
    // Schedule panel constants
    private static final String PERIOD_LABEL_PREFIX = "Period ";
    private static final int PERIOD_LABEL_WIDTH = 70;
    private static final int PERIOD_LABEL_HEIGHT = 20;
    private static final int SCHEDULE_FIELD_WIDTH = 250;
    private static final int SCHEDULE_FIELD_HEIGHT = 25;
    private static final int SCHEDULE_PANEL_WIDTH = 350;
    private static final int SCHEDULE_PANEL_HEIGHT = 200;
    private static final int NUM_PERIODS = 10;
    
    // Field dimensions
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 25;
    private static final int LABEL_WIDTH = 120;
    private static final int LABEL_HEIGHT = 20;
    
    // Button constants
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String CANCEL_BUTTON_TEXT = "Cancel";
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 25;
    
    // Layout constants
    private static final int FLOW_LAYOUT_GAP = 5;
    private static final int FLOW_LAYOUT_V_GAP = 2;
    
    private Teacher teacher;
    private List<Teacher> allTeachers;
    private JComboBox<String> teacherSearch;
    private JPanel formPanel;
    
    public TeacherProfileEditor(JFrame parent, Teacher teacher, List<Teacher> allTeachers) {
        super(parent, WINDOW_TITLE, true);
        this.teacher = teacher;
        this.allTeachers = allTeachers;
        
        setLayout(new BorderLayout(DIALOG_PADDING, DIALOG_PADDING));
        
        // Create search panel at top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SEARCH_PANEL_PADDING, SEARCH_PANEL_PADDING));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(SEARCH_PANEL_PADDING, SEARCH_PANEL_BORDER, SEARCH_PANEL_PADDING, SEARCH_PANEL_BORDER));
        
        searchPanel.add(new JLabel(SEARCH_LABEL_TEXT));
        String[] teacherNames = allTeachers.stream()
            .map(Teacher::getName)
            .toArray(String[]::new);
        teacherSearch = new JComboBox<>(teacherNames);
        teacherSearch.setSelectedItem(teacher.getName());
        teacherSearch.setPreferredSize(new Dimension(SEARCH_COMBOBOX_WIDTH, SEARCH_COMBOBOX_HEIGHT));
        teacherSearch.addActionListener(e -> updateTeacher());
        searchPanel.add(teacherSearch);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Create form panel using BoxLayout for better height control
        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(FORM_PANEL_PADDING, FORM_PANEL_PADDING, FORM_PANEL_PADDING, FORM_PANEL_PADDING));
        
        updateForm();
        
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void updateTeacher() {
        String selectedName = (String) teacherSearch.getSelectedItem();
        teacher = allTeachers.stream()
            .filter(t -> t.getName().equals(selectedName))
            .findFirst()
            .orElse(teacher);
        updateForm();
        System.out.println("\nTeacher Profile:");
        System.out.println(teacher.toString());
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
        
        // Name
        JTextField nameField = new JTextField(teacher.getName());
        formPanel.add(createRow(NAME_LABEL, nameField));
        
        // Type
        JComboBox<TeacherTypeEnum> typeCombo = new JComboBox<>(TeacherTypeEnum.values());
        typeCombo.setSelectedItem(teacher.getJobType());
        formPanel.add(createRow(TYPE_LABEL, typeCombo));
        
        // Status
        JComboBox<TeacherScheduleStatusEnum> statusCombo = new JComboBox<>(TeacherScheduleStatusEnum.values());
        statusCombo.setSelectedItem(teacher.getClassScheduleStatus());
        formPanel.add(createRow(STATUS_LABEL, statusCombo));
        
        // Time Allocation
        JTextField allocationField = new JTextField(String.format("%.2f", teacher.getTimeAllocation()));
        formPanel.add(createRow(TIME_ALLOCATION_LABEL, allocationField));
        
        // Duties This Semester
        JTextField dutiesField = new JTextField(String.valueOf(teacher.getDutiesThisSemester()));
        formPanel.add(createRow(DUTIES_LABEL, dutiesField));
        
        // Max Duties
        JTextField maxDutiesField = new JTextField(String.valueOf(teacher.getMaxDutiesPerSemester()));
        formPanel.add(createRow(MAX_DUTIES_LABEL, maxDutiesField));
        
        // Schedule
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        for (int i = 0; i < NUM_PERIODS; i++) {
            JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_GAP, FLOW_LAYOUT_V_GAP));
            JLabel periodLabel = new JLabel(PERIOD_LABEL_PREFIX + (i + 1) + ":");
            periodLabel.setPreferredSize(new Dimension(PERIOD_LABEL_WIDTH, PERIOD_LABEL_HEIGHT));
            periodPanel.add(periodLabel);
            
            JTextField scheduleField = new JTextField(teacher.getSchedule().get(i));
            scheduleField.setPreferredSize(new Dimension(SCHEDULE_FIELD_WIDTH, SCHEDULE_FIELD_HEIGHT));
            periodPanel.add(scheduleField);
            schedulePanel.add(periodPanel);
        }
        
        JPanel scheduleContainer = new JPanel(new BorderLayout());
        scheduleContainer.add(new JLabel(SCHEDULE_LABEL), BorderLayout.NORTH);
        JScrollPane scheduleScroll = new JScrollPane(schedulePanel);
        scheduleScroll.setPreferredSize(new Dimension(SCHEDULE_PANEL_WIDTH, SCHEDULE_PANEL_HEIGHT));
        scheduleContainer.add(scheduleScroll, BorderLayout.CENTER);
        formPanel.add(scheduleContainer);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, FLOW_LAYOUT_GAP, FLOW_LAYOUT_GAP));
        
        JButton saveButton = new JButton(SAVE_BUTTON_TEXT);
        JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        
        // Style buttons
        styleButton(saveButton);
        styleButton(cancelButton);
        
        // Set fixed button size
        Dimension buttonSize = new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT);
        saveButton.setMaximumSize(buttonSize);
        saveButton.setPreferredSize(buttonSize);
        cancelButton.setMaximumSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);
        
        saveButton.addActionListener(e -> {
            // Save changes
            teacher.setJobType((TeacherTypeEnum) typeCombo.getSelectedItem());
            // Add other save logic here
            System.out.println("\nUpdated Teacher Summary:");
            System.out.println("=".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
            for (Teacher t : allTeachers) {
                System.out.println(t.toString());
                System.out.println("-".repeat(GenerateDutyCalendar.NUM_OF_SEPERATORS_CHAR));
            }
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        formPanel.revalidate();
        formPanel.repaint();
    }
    
    private JPanel createRow(String label, JComponent component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_LAYOUT_GAP, FLOW_LAYOUT_V_GAP));
        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
        row.add(jLabel);
        component.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        row.add(component);
        return row;
    }
} 