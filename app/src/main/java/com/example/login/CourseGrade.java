package com.example.login;

public class CourseGrade {
    private String courseId;
    private String courseName;
    private double prelimGrade;
    private double midtermGrade;
    private double finalsGrade;
    private double averageGrade;

    public CourseGrade() {
        // Default constructor for Firebase
    }

    public CourseGrade(String courseId, String courseName, double prelimGrade, double midtermGrade, double finalsGrade) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.prelimGrade = prelimGrade;
        this.midtermGrade = midtermGrade;
        this.finalsGrade = finalsGrade;
        this.averageGrade = calculateAverage();
    }

    private double calculateAverage() {
        return (prelimGrade + midtermGrade + finalsGrade) / 3.0;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getPrelimGrade() {
        return prelimGrade;
    }

    public void setPrelimGrade(double prelimGrade) {
        this.prelimGrade = prelimGrade;
        this.averageGrade = calculateAverage();
    }

    public double getMidtermGrade() {
        return midtermGrade;
    }

    public void setMidtermGrade(double midtermGrade) {
        this.midtermGrade = midtermGrade;
        this.averageGrade = calculateAverage();
    }

    public double getFinalsGrade() {
        return finalsGrade;
    }

    public void setFinalsGrade(double finalsGrade) {
        this.finalsGrade = finalsGrade;
        this.averageGrade = calculateAverage();
    }

    public double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(double averageGrade) {
        this.averageGrade = averageGrade;
    }
}