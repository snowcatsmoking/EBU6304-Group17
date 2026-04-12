package data;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LocalStorageManager {

    private static final String STORAGE_FILE = DataConfig.BASE_DIR + "session.json";
    private final ObjectMapper objectMapper;

    public LocalStorageManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(DataConfig.BASE_DIR);
    }

    public void saveLastLogin(String account, String role) {
        Map<String, String> payload = new HashMap<>();
        payload.put("account", account == null ? "" : account);
        payload.put("role", role == null ? "" : role);
        payload.put("lastLoginTime", LocalDateTime.now().toString());
        try {
            objectMapper.writeValue(new File(STORAGE_FILE), payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> readLastLogin() {
        try {
            File file = new File(STORAGE_FILE);
            if (!file.exists()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(file, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
