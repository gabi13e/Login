package com.example.login;

public class EnrolledStudent {
    private String userId;
    private String fullName;
    private String schoolId;
    private String yearLevel;
    private double prelimGrade;
    private double midtermGrade;
    private double finalsGrade;
    private long enrolledAt;

    public EnrolledStudent() {
    }

    public EnrolledStudent(String userId, String fullName, String schoolId, String yearLevel,
                           double prelimGrade, double midtermGrade, double finalsGrade, long enrolledAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.schoolId = schoolId;
        this.yearLevel = yearLevel;
        this.prelimGrade = prelimGrade;
        this.midtermGrade = midtermGrade;
        this.finalsGrade = finalsGrade;
        this.enrolledAt = enrolledAt;
    }

    public double getAverageGrade() {
        return (prelimGrade + midtermGrade + finalsGrade) / 3.0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public double getPrelimGrade() {
        return prelimGrade;
    }

    public void setPrelimGrade(double prelimGrade) {
        this.prelimGrade = prelimGrade;
    }

    public double getMidtermGrade() {
        return midtermGrade;
    }

    public void setMidtermGrade(double midtermGrade) {
        this.midtermGrade = midtermGrade;
    }

    public double getFinalsGrade() {
        return finalsGrade;
    }

    public void setFinalsGrade(double finalsGrade) {
        this.finalsGrade = finalsGrade;
    }

    public long getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(long enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}