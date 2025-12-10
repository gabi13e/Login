package com.yourpackage.models;

import java.util.Date;

public class Enrollment {
    private String id;
    private String courseId;
    private String studentId;
    private Date enrollmentDate;

    // Constructors
    public Enrollment() {}

    public Enrollment(String courseId, String studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrollmentDate = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Date getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(Date enrollmentDate) { this.enrollmentDate = enrollmentDate; }
}