    package ZiqianCao.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TAApplicationRecordManager {
    private static final String APPLICATION_DATA_DIR = "resources/Data/ApplicationData/";
    private ObjectMapper objectMapper = new ObjectMapper();

    public TAApplicationRecordManager() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        File dataDir = new File(APPLICATION_DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public boolean hasDuplicateApplication(String studentId, String jobId) {
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getStudentId().equals(studentId) && record.getJobId().equals(jobId) && !"已撤回".equals(record.getStatus())) {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
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

    public List<TAApplicationRecord> getApplicationsByStudentId(String studentId) {
        List<TAApplicationRecord> records = new ArrayList<>();
        File[] files = new File(APPLICATION_DATA_DIR).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    TAApplicationRecord record = objectMapper.readValue(file, TAApplicationRecord.class);
                    if (record.getStudentId().equals(studentId)) {
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
        if (record != null && "审核中".equals(record.getStatus())) {
            record.setStatus("已撤回");
            saveApplication(record);
            return true;
        }
        return false;
    }
}
