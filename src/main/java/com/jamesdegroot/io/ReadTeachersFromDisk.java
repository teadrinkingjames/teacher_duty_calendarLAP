package com.jamesdegroot.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jamesdegroot.teacher.Teacher;

public class ReadTeachersFromDisk {
    
    /**
     * Reads teacher data from a CSV file and creates Teacher objects.
     * @param filename Path to the CSV file
     * @return List of Teacher objects with their schedules
     * @throws IOException if file reading fails (caught internally)
     */
    public static List<Teacher> readTeachersNames(String filename) {
        List<Teacher> teachers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length > 0) {
                    String teacherName = parts[0].replaceAll("[\"',]", "").trim();
                    if (!teacherName.isEmpty() && teacherName.matches(".*[a-zA-Z].*")) {
                        Teacher teacher = new Teacher(teacherName);
                        
                        for (int i = 1; i < Math.min(parts.length, 11); i++) {
                            teacher.addScheduleItem(parts[i].trim(), i - 1);
                        }
                        
                        teacher.calculateTimeAllocation();
                        teacher.calculateMaxDutiesPerSemester();
                        teachers.add(teacher);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return teachers;
    }
}
