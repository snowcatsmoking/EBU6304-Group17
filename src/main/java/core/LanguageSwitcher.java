package core;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public final class LanguageSwitcher {

    private LanguageSwitcher() {}

    public static HBox create(Runnable onLanguageChanged) {
        Button button = new Button(LanguageManager.getInstance().isEnglish() ? "中文" : "English");
        button.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #475569; -fx-background-color: #ffffff;" +
            "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-padding: 6 12 6 12; -fx-cursor: hand;"
        );
        button.setOnAction(e -> {
            LanguageManager.getInstance().toggleLanguage();
            if (onLanguageChanged != null) {
                onLanguageChanged.run();
            }
        });

        HBox box = new HBox(button);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(0, 0, 12, 0));
        return box;
    }
}
