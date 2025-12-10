package com.example.login.models;

public class StudentEnrollment {
    private String userId;
    private String studentName;
    private String studentId;
    private String email;
    private String courseId;
    private double prelimGrade;
    private double midtermGrade;
    private double finalsGrade;

    public StudentEnrollment() {
        // Required for Firebase
    }

    public StudentEnrollment(String userId, String studentName, String studentId,
                             String email, String courseId, double prelimGrade,
                             double midtermGrade, double finalsGrade) {
        this.userId = userId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.email = email;
        this.courseId = courseId;
        this.prelimGrade = prelimGrade;
        this.midtermGrade = midtermGrade;
        this.finalsGrade = finalsGrade;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }

    public String getCourseId() {
        return courseId;
    }

    public double getPrelimGrade() {
        return prelimGrade;
    }

    public double getMidtermGrade() {
        return midtermGrade;
    }

    public double getFinalsGrade() {
        return finalsGrade;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setPrelimGrade(double prelimGrade) {
        this.prelimGrade = prelimGrade;
    }

    public void setMidtermGrade(double midtermGrade) {
        this.midtermGrade = midtermGrade;
    }

    public void setFinalsGrade(double finalsGrade) {
        this.finalsGrade = finalsGrade;
    }

    public double getAverageGrade() {
        return (prelimGrade + midtermGrade + finalsGrade) / 3;
    }
}