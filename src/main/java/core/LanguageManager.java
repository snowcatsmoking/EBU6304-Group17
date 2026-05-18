package core;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;

    private LanguageManager() {
        setLanguage("zh");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void setLanguage(String lang) {
        if ("en".equals(lang)) {
            currentLocale = new Locale("en", "US");
        } else {
            currentLocale = new Locale("zh", "CN");
        }
        bundle = ResourceBundle.getBundle("messages", currentLocale);
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
}
