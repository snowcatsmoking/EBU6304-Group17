package TA.java.model;

public class MatchingResult {
    private String studentId;
    private String jobId;
    private int percentage;
    private String reason;
    private String analysis;
    private String improvements;
    private long timestamp;

    public MatchingResult() {}

    public MatchingResult(String studentId, String jobId, int percentage, String reason) {
        this.studentId = studentId;
        this.jobId = jobId;
        this.percentage = percentage;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }

    public MatchingResult(String studentId, String jobId, int percentage, String reason, String analysis, String improvements) {
        this.studentId = studentId;
        this.jobId = jobId;
        this.percentage = percentage;
        this.reason = reason;
        this.analysis = analysis;
        this.improvements = improvements;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
