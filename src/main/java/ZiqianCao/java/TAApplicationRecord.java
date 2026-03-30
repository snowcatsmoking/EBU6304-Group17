package ZiqianCao.java;

import java.util.Date;
import java.util.UUID;

public class TAApplicationRecord {
    private String applicationId;
    private String studentId;
    private String jobId;
    private String positionName;
    private String courseName;
    private String status;
    private Date applicationDate;
    private String studentName;
    private String major;
    private String phone;
    private String email;
    private String availableTime;
    private String skills;

    public TAApplicationRecord() {
        this.applicationId = UUID.randomUUID().toString();
        this.applicationDate = new Date();
        this.status = "审核中";
    }

    public TAApplicationRecord(String studentId, String jobId, String positionName, String courseName,
                              String studentName, String major, String phone, String email,
                              String availableTime, String skills) {
        this();
        this.studentId = studentId;
        this.jobId = jobId;
        this.positionName = positionName;
        this.courseName = courseName;
        this.studentName = studentName;
        this.major = major;
        this.phone = phone;
        this.email = email;
        this.availableTime = availableTime;
        this.skills = skills;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}
