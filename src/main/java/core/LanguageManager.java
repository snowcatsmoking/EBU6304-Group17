package core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;
    private String currentLanguage;
    private Map<String, String> uiTextTranslations = Collections.emptyMap();

    private LanguageManager() {
        setLanguage("en");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setLanguage(String lang) {
        if ("en".equals(lang)) {
            currentLanguage = "en";
            currentLocale = Locale.forLanguageTag("en-US");
        } else {
            currentLanguage = "zh";
            currentLocale = Locale.forLanguageTag("zh-CN");
        }
        bundle = ResourceBundle.getBundle("messages", currentLocale);
        uiTextTranslations = loadUiTextTranslations(currentLanguage);
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "[" + key + "]";
        }
    }

    public String get(String key, Object... params) {
        String text = get(key);
        for (int i = 0; i < params.length; i++) {
            text = text.replace("{" + i + "}", String.valueOf(params[i]));
        }
        return text;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isEnglish() {
        return "en".equals(currentLanguage);
    }

    public void toggleLanguage() {
        setLanguage(isEnglish() ? "zh" : "en");
    }

    public String text(String englishText) {
        if (englishText == null || englishText.isEmpty()) {
            return englishText;
        }
        String translated = uiTextTranslations.get(englishText);
        if (translated != null) {
            return translated;
        }
        for (Map.Entry<String, String> entry : uiTextTranslations.entrySet()) {
            String key = entry.getKey();
            if (key.length() >= 3 && englishText.startsWith(key) && isPrefixKey(key)) {
                return entry.getValue() + englishText.substring(key.length());
            }
        }
        return englishText;
    }

    private boolean isPrefixKey(String key) {
        return key.endsWith(":")
            || key.endsWith(": ")
            || key.endsWith(", ")
            || key.endsWith(" - ")
            || key.endsWith(" has been ")
            || key.endsWith("\uff1a")
            || key.endsWith("\uff1a ")
            || key.endsWith("：")
            || key.endsWith("： ");
    }

    private Map<String, String> loadUiTextTranslations(String language) {
        Map<String, String> translations = new HashMap<>();
        String resourceName = "zh".equals(language) ? "/ui_zh_CN.tsv" : "/ui_en_US.tsv";
        try (InputStream stream = LanguageManager.class.getResourceAsStream(resourceName)) {
            if (stream == null) {
                return translations;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int tabIndex = line.indexOf('\t');
                    if (tabIndex <= 0) {
                        continue;
                    }
                    String key = unescape(line.substring(0, tabIndex));
                    String value = unescape(line.substring(tabIndex + 1));
                    translations.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return translations;
    }

    private String unescape(String text) {
        return text.replace("\\n", "\n").replace("\\t", "\t");
    }
}
