package ai.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class AIConfig {
    private static final String DEFAULT_MODEL = "doubao-seed-2-0-lite-260215";
    private static final String DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";
    private static final String CONFIG_FILE = "config.properties";

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
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
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

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        save();
    }

    private void save() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        } catch (IOException ignored) {
        }

        props.setProperty("ark.api.key", this.apiKey);
        if (this.model != null && !this.model.isEmpty()) {
            props.setProperty("ark.model", this.model);
        }

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "AI Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

