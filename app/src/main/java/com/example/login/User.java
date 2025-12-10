package com.example.login;

public class User {
    private String userId;
    private String schoolId;
    private String firstname;
    private String lastname;
    private String middlename;
    private String email;
    private String role;
    private String yearLevel;
    private String department;
    private String course;

    // Empty constructor required for Firebase
    public User() {}

    public User(String schoolId, String firstname, String lastname, String middlename,
                String email, String role, String yearLevel, String department, String course) {
        this.schoolId = schoolId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.middlename = middlename;
        this.email = email;
        this.role = role;
        this.yearLevel = yearLevel;
        this.department = department;
        this.course = course;
    }

    // Helper method to get full name
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstname != null && !firstname.isEmpty()) {
            name.append(firstname);
        }
        if (middlename != null && !middlename.isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(middlename);
        }
        if (lastname != null && !lastname.isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(lastname);
        }
        return name.length() > 0 ? name.toString() : "N/A";
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}