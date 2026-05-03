package ai.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AIConfig {
    private static final String DEFAULT_MODEL = "doubao-seed-2-0-lite-260215";
    private static final String DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";

    private String apiKey;
    private String model;
    private String baseUrl;

    public AIConfig() {
        load();
    }

    private void load() {
        this.apiKey = System.getenv("ARK_API_KEY");

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            Properties props = new Properties();
            try (InputStream input = new FileInputStream("config.properties")) {
                props.load(input);
                this.apiKey = props.getProperty("ark.api.key");
                if (props.containsKey("ark.model")) {
                    this.model = props.getProperty("ark.model");
                }
            } catch (IOException ignored) {
            }
        }

        if (this.model == null || this.model.isEmpty()) {
            this.model = DEFAULT_MODEL;
        }

        this.baseUrl = DEFAULT_BASE_URL;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isValid() {
        return apiKey != null && !apiKey.isEmpty();
    }
}

