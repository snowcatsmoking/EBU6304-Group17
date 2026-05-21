package ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

public class StyleUtils {

    private StyleUtils() {}

    public static final String COLOR_PRIMARY = "#6366f1";
    public static final String COLOR_PRIMARY_HOVER = "#4f46e5";
    public static final String COLOR_SUCCESS = "#10b981";
    public static final String COLOR_WARNING = "#f59e0b";
    public static final String COLOR_DANGER = "#ef4444";
    public static final String COLOR_BACKGROUND = "#f8fafc";
    public static final String COLOR_SURFACE = "#ffffff";
    public static final String COLOR_BORDER = "#e2e8f0";
    public static final String TEXT_PRIMARY = "#1e293b";
    public static final String TEXT_SECONDARY = "#64748b";
    public static final String TEXT_MUTED = "#94a3b8";

    public static void applyPrimaryButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + COLOR_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: " + COLOR_PRIMARY_HOVER + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 8px;" +
                "-fx-font-weight: 500;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;"
            )
        );
        button.setOnMouseExited(e -> applyPrimaryButton(button));
    }

    public static void applySecondaryButton(Button button) {
        button.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: " + COLOR_BACKGROUND + ";" +
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-radius: 8px;" +
                "-fx-font-weight: 500;" +
                "-fx-padding: 10px 20px;" +
                "-fx-cursor: hand;"
            )
        );
        button.setOnMouseExited(e -> applySecondaryButton(button));
    }

    public static void applyDangerButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + COLOR_DANGER + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
    }

    public static void applySuccessButton(Button button) {
        button.setStyle(
            "-fx-background-color: " + COLOR_SUCCESS + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: 500;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
    }

    public static void applyTextField(TextField field) {
        field.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 10px 14px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 0, 0, 0, 3);"
                );
            } else {
                applyTextField(field);
            }
        });
    }

    public static void applyPasswordField(PasswordField field) {
        field.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: " + COLOR_PRIMARY + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 10px 14px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 0, 0, 0, 3);"
                );
            } else {
                applyPasswordField(field);
            }
        });
    }

    public static void applyCard(VBox box) {
        box.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 10, 0, 0, 2);"
        );
    }

    public static void applyStatCard(VBox box) {
        applyCard(box);
        box.setAlignment(javafx.geometry.Pos.CENTER);
    }

    public static void applyTitleLabel(Label label) {
        label.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
    }

    public static void applySectionLabel(Label label) {
        label.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
    }

    public static void applySubtitleLabel(Label label) {
        label.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + TEXT_SECONDARY + ";"
        );
    }

    public static void applyMutedLabel(Label label) {
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + TEXT_MUTED + ";"
        );
    }

    public static void applySidebarItem(Label label, boolean active) {
        if (active) {
            label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: " + COLOR_PRIMARY + ";" +
                "-fx-background-color: #e0e7ff;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 12px 16px;" +
                "-fx-font-weight: 600;" +
                "-fx-cursor: hand;"
            );
        } else {
            label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 12px 16px;" +
                "-fx-cursor: hand;"
            );
            label.setOnMouseEntered(e -> 
                label.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                    "-fx-background-color: " + COLOR_BACKGROUND + ";" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 12px 16px;" +
                    "-fx-cursor: hand;"
                )
            );
            label.setOnMouseExited(e -> applySidebarItem(label, false));
        }
    }

    public static void applyBadge(Label label, String type) {
        String bgColor, textColor;
        switch (type) {
            case "success":
                bgColor = "#d1fae5";
                textColor = "#059669";
                break;
            case "warning":
                bgColor = "#fef3c7";
                textColor = "#b45309";
                break;
            case "danger":
                bgColor = "#fee2e2";
                textColor = "#dc2626";
                break;
            case "primary":
                bgColor = "#e0e7ff";
                textColor = "#4338ca";
                break;
            default:
                bgColor = "#f1f5f9";
                textColor = "#475569";
        }
        label.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 4px 10px;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 500;"
        );
    }

    public static void applySidebar(VBox sidebar) {
        sidebar.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + COLOR_BORDER + ";" +
            "-fx-border-width: 0 1px 0 0;"
        );
    }

    public static void applyBackground(Region region) {
        region.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
    }

    public static void applySurface(Region region) {
        region.setStyle("-fx-background-color: white;");
    }
}
