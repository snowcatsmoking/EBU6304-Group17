package TA.java;

import java.util.ArrayList;
import java.util.List;

public class TAJob {
    private String jobId;
    private String positionName;
    private String courseName;
    private String courseCode;
    private int recruitmentCount;
    private String requirements;
    private String deadline;
    private String publisher;
    private String moStaffId;
    private boolean isActive;
    private List<String> requiredSkills = new ArrayList<>();

    public TAJob() {}

    public TAJob(String jobId, String positionName, String courseName, String courseCode, 
                int recruitmentCount, String requirements, String deadline, 
                String publisher, boolean isActive) {
        this.jobId = jobId;
        this.positionName = positionName;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.recruitmentCount = recruitmentCount;
        this.requirements = requirements;
        this.deadline = deadline;
        this.publisher = publisher;
        this.isActive = isActive;
    }

    public TAJob(String jobId, String positionName, String courseName, String courseCode,
                int recruitmentCount, String requirements, String deadline,
                String publisher, boolean isActive, List<String> requiredSkills) {
        this(jobId, positionName, courseName, courseCode, recruitmentCount, requirements, deadline, publisher, isActive);
        setRequiredSkills(requiredSkills);
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getRecruitmentCount() {
        return recruitmentCount;
    }

    public void setRecruitmentCount(int recruitmentCount) {
        this.recruitmentCount = recruitmentCount;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getMoStaffId() {
        return moStaffId;
    }

    public void setMoStaffId(String moStaffId) {
        this.moStaffId = moStaffId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getRequiredSkills() {
        if (requiredSkills == null) {
            requiredSkills = new ArrayList<>();
        }
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = SkillUtils.normalizeSkills(requiredSkills);
    }
}
