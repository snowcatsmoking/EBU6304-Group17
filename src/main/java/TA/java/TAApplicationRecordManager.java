package TA.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import data.DataConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationRecordManager {
    private static final String APPLICATION_DATA_DIR = DataConfig.APPLICATION_DIR;
    private ObjectMapper objectMapper = new ObjectMapper();

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

    public List<TAApplicationRecord> getApplicationsByStudentId(String taStudentId) {
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
                    if (record.getMoStaffId() != null && record.getMoStaffId().equals(moStaffId)) {
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

    public boolean withdrawApplication(String applicationId) {
        TAApplicationRecord record = getApplicationById(applicationId);
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            record.setStatus(TAApplicationRecord.STATUS_WITHDRAWN);
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
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            record.setStatus(TAApplicationRecord.STATUS_APPROVED);
            record.setStatusChangeDate(new java.util.Date());
            record.setNotified(false);
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
        if (record != null && TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            record.setStatus(TAApplicationRecord.STATUS_REJECTED);
            record.setStatusChangeDate(new java.util.Date());
            record.setNotified(false);
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
        if (record != null && (
                TAApplicationRecord.STATUS_APPROVED.equals(record.getStatus()) ||
                TAApplicationRecord.STATUS_REJECTED.equals(record.getStatus()))) {
            record.setStatus(TAApplicationRecord.STATUS_PENDING);
            record.setReviewComment(null);
            record.setStatusChangeDate(null);
            record.setNotified(false);
            saveApplication(record);
            return true;
        }
        return false;
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
