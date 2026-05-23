package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LogManager {

    private static final String LOG_DIR = DataConfig.LOG_DIR;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;

    public LogManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(LOG_DIR);
    }

    public static class LogEntry {
        public String logId;
        public String timestamp;
        public String adminId;
        public String action;
        public String target;
        public String detail;

        public LogEntry() {}

        public LogEntry(String adminId, String action, String target, String detail) {
            this.timestamp = LocalDateTime.now().format(FORMATTER);
            this.logId = this.timestamp.replace(":", "-").replace(" ", "_") + "_" + adminId;
            this.adminId = adminId;
            this.action = action;
            this.target = target;
            this.detail = detail;
        }

        public String getLogId()    { return logId; }
        public String getTimestamp(){ return timestamp; }
        public String getAdminId()  { return adminId; }
        public String getAction()   { return action; }
        public String getTarget()   { return target; }
        public String getDetail()   { return detail; }

        public void setLogId(String logId)       { this.logId = logId; }
        public void setTimestamp(String ts)      { this.timestamp = ts; }
        public void setAdminId(String adminId)   { this.adminId = adminId; }
        public void setAction(String action)     { this.action = action; }
        public void setTarget(String target)     { this.target = target; }
        public void setDetail(String detail)     { this.detail = detail; }
    }

    public void log(String adminId, String action, String target, String detail) {
        LogEntry entry = new LogEntry(adminId, action, target, detail);
        String fileName = LOG_DIR + entry.logId + ".json";
        try {
            objectMapper.writeValue(new File(fileName), entry);
        } catch (IOException e) {
            System.err.println("Failed to write admin log " + fileName + ": " + e.getMessage());
        }
    }

    public List<LogEntry> getAllLogs() {
        List<LogEntry> result = new ArrayList<>();
        File dir = new File(LOG_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return result;
        Arrays.sort(files, Comparator.comparing(File::getName).reversed());
        for (File file : files) {
            try {
                LogEntry entry = objectMapper.readValue(file, LogEntry.class);
                result.add(entry);
            } catch (IOException e) {
                System.err.println("Skipping unreadable log file " + file.getName() + ": " + e.getMessage());
            }
        }
        return result;
    }
}
