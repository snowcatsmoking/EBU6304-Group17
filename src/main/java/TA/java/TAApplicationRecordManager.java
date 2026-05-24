package TA.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import data.DataConfig;
import data.JobDataManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationRecordManager {
    private static final String APPLICATION_DATA_DIR = DataConfig.APPLICATION_DIR;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final JobDataManager jobDataManager = new JobDataManager();

    public TAApplicationRecordManager() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(APPLICATION_DATA_DIR);
    }

    public boolean hasDuplicateApplication(String taStudentId, String jobId) {
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getTaStudentId().equals(taStudentId) && record.getJobId().equals(jobId) 
                        && !TAApplicationRecord.STATUS_WITHDRAWN.equals(record.getStatus())) {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public List<TAApplicationRecord> getApplicationsByTA(String taStudentId) {
        List<TAApplicationRecord> result = new ArrayList<>();
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getTaStudentId().equals(taStudentId)) {
                        result.add(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public void saveApplication(TAApplicationRecord record) {
        try {
            String fileName = APPLICATION_DATA_DIR + record.getApplicationId() + ".json";
            objectMapper.writeValue(new File(fileName), record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<TAApplicationRecord> getAllApplications() {
        List<TAApplicationRecord> records = new ArrayList<>();
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    records.add(record);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return records;
    }

    /**
     * Auto-reject any PENDING applications whose job has expired or been closed.
     * Called before displaying a TA's application list so the TA never sees
     * a permanently-hanging "PENDING" entry for a job that is no longer open.
     */
    private void autoRejectPendingApplicationsForExpiredJobs() {
        for (TAApplicationRecord record : getAllApplications()) {
            if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
                TAJob job = jobDataManager.getJobById(record.getJobId());
                // isActive == true means the job is closed/expired (inverted flag)
                if (job != null && job.isActive()) {
                    record.setStatus(TAApplicationRecord.STATUS_REJECTED);
                    record.setStatusChangeDate(new java.util.Date());
                    record.setReviewComment("Job deadline has passed");
                    record.setNotified(false);
                    saveApplication(record);
                }
            }
        }
    }

    public List<TAApplicationRecord> getApplicationsByStudentId(String taStudentId) {
        autoRejectPendingApplicationsForExpiredJobs();
        List<TAApplicationRecord> records = new ArrayList<>();
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getTaStudentId().equals(taStudentId)) {
                        records.add(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return records;
    }

    public List<TAApplicationRecord> getApplicationsByMoStaffId(String moStaffId) {
        List<TAApplicationRecord> records = new ArrayList<>();
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (canMoAccessApplication(record, moStaffId)) {
                        records.add(record);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return records;
    }

    public TAApplicationRecord getApplicationById(String applicationId) {
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getApplicationId().equals(applicationId)) {
                        return record;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public TAApplicationRecord getApplicationByIdForMo(String applicationId, String moStaffId) {
        TAApplicationRecord record = getApplicationById(applicationId);
        return canMoAccessApplication(record, moStaffId) ? record : null;
    }

    public boolean canMoAccessApplication(String applicationId, String moStaffId) {
        return canMoAccessApplication(getApplicationById(applicationId), moStaffId);
    }

    public boolean canMoAccessApplication(TAApplicationRecord record, String moStaffId) {
        if (record == null || moStaffId == null || moStaffId.trim().isEmpty()) {
            return false;
        }
        if (moStaffId.equals(record.getMoStaffId())) {
            return true;
        }
        return record.getJobId() != null && jobDataManager.canManageJob(record.getJobId(), moStaffId);
    }

    public boolean withdrawApplication(String applicationId) {
        TAApplicationRecord record = getApplicationById(applicationId);
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            record.setStatus(TAApplicationRecord.STATUS_WITHDRAWN);
            record.setNotified(true);
            saveApplication(record);
            return true;
        }
        return false;
    }

    public boolean approveApplication(String applicationId) {
        return approveApplication(applicationId, null);
    }

    public boolean approveApplication(String applicationId, String reviewComment) {
        TAApplicationRecord record = getApplicationById(applicationId);
        return approveLoadedApplication(record, reviewComment, null);
    }

    public boolean approveApplicationForMo(String applicationId, String moStaffId) {
        return approveApplicationForMo(applicationId, moStaffId, null);
    }

    public boolean approveApplicationForMo(String applicationId, String moStaffId, String reviewComment) {
        TAApplicationRecord record = getApplicationByIdForMo(applicationId, moStaffId);
        return approveLoadedApplication(record, reviewComment, moStaffId);
    }

    private boolean approveLoadedApplication(TAApplicationRecord record, String reviewComment, String reviewer) {
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            // Check recruitment cap: do not allow more approvals than the job's recruitment count
            TAJob job = jobDataManager.getJobById(record.getJobId());
            if (job != null) {
                long approvedCount = getAllApplications().stream()
                        .filter(r -> record.getJobId().equals(r.getJobId())
                                && TAApplicationRecord.STATUS_APPROVED.equals(r.getStatus()))
                        .count();
                if (approvedCount >= job.getRecruitmentCount()) {
                    return false;
                }
            }
            record.setStatus(TAApplicationRecord.STATUS_APPROVED);
            record.setStatusChangeDate(new java.util.Date());
            record.setNotified(false);
            if (reviewer != null && !reviewer.trim().isEmpty()) {
                record.setReviewer(reviewer.trim());
            }
            if (reviewComment != null && !reviewComment.trim().isEmpty()) {
                record.setReviewComment(reviewComment.trim());
            }
            saveApplication(record);
            return true;
        }
        return false;
    }

    public boolean rejectApplication(String applicationId) {
        return rejectApplication(applicationId, null);
    }

    public boolean rejectApplication(String applicationId, String reviewComment) {
        TAApplicationRecord record = getApplicationById(applicationId);
        return rejectLoadedApplication(record, reviewComment, null);
    }

    public boolean rejectApplicationForMo(String applicationId, String moStaffId) {
        return rejectApplicationForMo(applicationId, moStaffId, null);
    }

    public boolean rejectApplicationForMo(String applicationId, String moStaffId, String reviewComment) {
        TAApplicationRecord record = getApplicationByIdForMo(applicationId, moStaffId);
        return rejectLoadedApplication(record, reviewComment, moStaffId);
    }

    private boolean rejectLoadedApplication(TAApplicationRecord record, String reviewComment, String reviewer) {
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            record.setStatus(TAApplicationRecord.STATUS_REJECTED);
            record.setStatusChangeDate(new java.util.Date());
            record.setNotified(false);
            if (reviewer != null && !reviewer.trim().isEmpty()) {
                record.setReviewer(reviewer.trim());
            }
            if (reviewComment != null && !reviewComment.trim().isEmpty()) {
                record.setReviewComment(reviewComment.trim());
            }
            saveApplication(record);
            return true;
        }
        return false;
    }

    /** Revert an APPROVED or REJECTED decision back to PENDING. */
    public boolean resetToPending(String applicationId) {
        TAApplicationRecord record = getApplicationById(applicationId);
        return resetLoadedApplicationToPending(record);
    }

    public boolean resetToPendingForMo(String applicationId, String moStaffId) {
        TAApplicationRecord record = getApplicationByIdForMo(applicationId, moStaffId);
        return resetLoadedApplicationToPending(record);
    }

    private boolean resetLoadedApplicationToPending(TAApplicationRecord record) {
        if (record != null && (
                TAApplicationRecord.STATUS_APPROVED.equals(record.getStatus()) ||
                TAApplicationRecord.STATUS_REJECTED.equals(record.getStatus()))) {
            record.setStatus(TAApplicationRecord.STATUS_PENDING);
            record.setReviewer(null);
            record.setReviewComment(null);
            record.setStatusChangeDate(null);
            record.setNotified(false);
            saveApplication(record);
            return true;
        }
        return false;
    }

    public boolean saveVerifiedKeywordsForMo(String applicationId, String moStaffId, List<String> keywords) {
        TAApplicationRecord record = getApplicationByIdForMo(applicationId, moStaffId);
        if (record == null) {
            return false;
        }
        record.setVerifiedKeywords(keywords);
        record.setKeywordsVerified(true);
        saveApplication(record);
        return true;
    }

    public boolean reextractKeywordsForMo(String applicationId, String moStaffId) {
        TAApplicationRecord record = getApplicationByIdForMo(applicationId, moStaffId);
        if (record == null) {
            return false;
        }
        TAJob job = jobDataManager.getJobById(record.getJobId());
        record.setResumeKeywords(ResumeKeywordExtractor.extractKeywords(record.getResumeText(), record.getSkills(), job));
        saveApplication(record);
        return true;
    }

    /** Get applications that have status changed and not yet notified */
    public List<TAApplicationRecord> getUnnotifiedApplications(String taStudentId) {
        List<TAApplicationRecord> result = new ArrayList<>();
        List<TAApplicationRecord> allApps = getApplicationsByTA(taStudentId);
        for (TAApplicationRecord app : allApps) {
            if (!TAApplicationRecord.STATUS_PENDING.equals(app.getStatus()) && !app.isNotified()) {
                result.add(app);
            }
        }
        return result;
    }

    /** Mark an application as notified */
    public void markAsNotified(String applicationId) {
        TAApplicationRecord record = getApplicationById(applicationId);
        if (record != null) {
            record.setNotified(true);
            saveApplication(record);
        }
    }
}
