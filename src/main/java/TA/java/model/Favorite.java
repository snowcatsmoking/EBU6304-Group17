package TA.java.model;

public class Favorite {
    private String favoriteId;
    private String taStudentId;
    private String jobId;
    private long favoriteTime;

    public Favorite() {}

    public Favorite(String favoriteId, String taStudentId, String jobId, long favoriteTime) {
        this.favoriteId = favoriteId;
        this.taStudentId = taStudentId;
        this.jobId = jobId;
        this.favoriteTime = favoriteTime;
    }

    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getTaStudentId() {
        return taStudentId;
    }

    public void setTaStudentId(String taStudentId) {
        this.taStudentId = taStudentId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public long getFavoriteTime() {
        return favoriteTime;
    }

    public void setFavoriteTime(long favoriteTime) {
        this.favoriteTime = favoriteTime;
    }
}
