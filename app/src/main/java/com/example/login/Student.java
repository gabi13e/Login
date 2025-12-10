package com.yourpackage.models;

public class Student {
    private String id;
    private String name;
    private String email;
    private String studentId;
    private String yearLevel;
    private String department;
    private String role = "student";

    // Constructors
    public Student() {}

    public Student(String name, String email, String studentId, String yearLevel, String department) {
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.yearLevel = yearLevel;
        this.department = department;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}