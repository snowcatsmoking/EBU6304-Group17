package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorkloadConfigManager {

    private static final String CONFIG_FILE = DataConfig.ADMIN_DIR + "workload_config.json";
    private static final int DEFAULT_THRESHOLD = 3;

    private final ObjectMapper objectMapper;

    public WorkloadConfigManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(DataConfig.ADMIN_DIR);
    }

    public int getThreshold() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return DEFAULT_THRESHOLD;
        try {
            Map<?, ?> map = objectMapper.readValue(file, Map.class);
            Object val = map.get("maxPositionsPerSemester");
            if (val instanceof Integer) return (Integer) val;
            if (val instanceof Number)  return ((Number) val).intValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DEFAULT_THRESHOLD;
    }

    public void saveThreshold(int threshold) {
        Map<String, Integer> payload = new HashMap<>();
        payload.put("maxPositionsPerSemester", threshold);
        try {
            objectMapper.writeValue(new File(CONFIG_FILE), payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
