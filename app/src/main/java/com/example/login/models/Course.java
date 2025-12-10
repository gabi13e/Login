package com.example.login.models;

public class Course {
    private String courseId;
    private String courseName;
    private String courseCode;
    private String instructor;
    private int credits;
    private String description;
    private String schedule;
    private String room;
    private boolean enrolled;  // Add this field

    // Default constructor required for Firebase
    public Course() {
    }

    public Course(String courseId, String courseName, String courseCode,
                  String instructor, int credits, String description,
                  String schedule, String room) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.instructor = instructor;
        this.credits = credits;
        this.description = description;
        this.schedule = schedule;
        this.room = room;
        this.enrolled = false;  // Default to not enrolled
    }

    // Getters
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getInstructor() {
        return instructor;
    }

    public int getCredits() {
        return credits;
    }

    public String getDescription() {
        return description;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getRoom() {
        return room;
    }

    // Add this getter for enrolled status
    public boolean isEnrolled() {
        return enrolled;
    }

    // Setters
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    // Add this setter for enrolled status
    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    public String getCourseDescription() {
        return description;
    }
}