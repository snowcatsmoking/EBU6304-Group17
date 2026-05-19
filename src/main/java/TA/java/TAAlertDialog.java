package TA.java;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class TAAlertDialog {

    public enum Variant {
        SUCCESS,
        ERROR,
        WARNING,
        EXPORT
    }

    private TAAlertDialog() {
    }

    public static void showSuccess(Stage owner, String title, String message) {
        show(owner, Variant.SUCCESS, title, "Saved", message, "Done", null);
    }

    public static void showError(Stage owner, String title, String header, String message) {
        show(owner, Variant.ERROR, title, header, message, "Got It", null);
    }

    public static void showWarning(Stage owner, String title, String header, String message, Runnable onClose) {
        showChoice(owner, Variant.WARNING, title, header, message, "Complete Profile", "Cancel", onClose);
    }

    public static void showExportSuccess(Stage owner, String title, String message) {
        show(owner, Variant.EXPORT, title, "Export ready", message, "Done", null);
    }

    public static void showExportError(Stage owner, String title, String message) {
        show(owner, Variant.ERROR, title, "Export failed", message, "Try Again", null);
    }

    private static void show(Stage owner, Variant variant, String title, String header,
                             String message, String buttonText, Runnable onClose) {
        showChoice(owner, variant, title, header, message, buttonText, null, onClose);
    }

    private static void showChoice(Stage owner, Variant variant, String title, String header,
                                   String message, String primaryText, String secondaryText,
                                   Runnable primaryAction) {
        Stage dialog = new Stage();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);

        Palette palette = Palette.forVariant(variant);

        StackPane shell = new StackPane();
        shell.setPadding(new Insets(20));
        shell.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(15);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(470);
        card.setPadding(new Insets(20, 24, 22, 24));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + palette.border + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;"
        );

        final boolean[] primaryChosen = {false};

        card.getChildren().addAll(
            createTopDots(palette),
            createIconBadge(palette),
            createTitle(title),
            createMessageArea(header, message, palette, variant),
            createButtonRow(primaryText, secondaryText, palette.accent, dialog, primaryChosen)
        );

        shell.getChildren().add(card);

        Scene scene = new Scene(shell, 530, chooseHeight(variant, header, message));
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });

        dialog.setScene(scene);
        dialog.setOnHidden(e -> {
            if (primaryChosen[0] && primaryAction != null) {
                primaryAction.run();
            }
        });

        card.setOpacity(0);
        card.setScaleX(0.96);
        card.setScaleY(0.96);
        FadeTransition fade = new FadeTransition(Duration.millis(170), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(210), card);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1);
        scale.setToY(1);
        dialog.setOnShown(e -> new ParallelTransition(fade, scale).play());

        dialog.showAndWait();
    }

    private static HBox createTopDots(Palette palette) {
        HBox dots = new HBox(7);
        dots.setAlignment(Pos.CENTER);

        String[] colors = {"#ef4444", "#f59e0b", "#6366f1"};
        for (String color : colors) {
            Circle dot = new Circle(color.equals(palette.accent) ? 5.5 : 4.5);
            dot.setFill(Color.web(color));
            dot.setOpacity(color.equals(palette.accent) ? 1 : 0.48);
            dots.getChildren().add(dot);
        }
        return dots;
    }

    private static StackPane createIconBadge(Palette palette) {
        StackPane badge = new StackPane();
        badge.setMinSize(54, 54);
        badge.setPrefSize(54, 54);
        badge.setMaxSize(54, 54);
        badge.setStyle(
            "-fx-background-color: " + palette.soft + ";" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: " + palette.border + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 18;"
        );

        Label icon = new Label(palette.icon);
        icon.setTextFill(Color.web(palette.accent));
        icon.setStyle("-fx-font-size: 22px; -fx-font-weight: 900;");
        badge.getChildren().add(icon);
        return badge;
    }

    private static Label createTitle(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(400);
        titleLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        return titleLabel;
    }

    private static VBox createMessageArea(String header, String message, Palette palette, Variant variant) {
        VBox area = new VBox(10);
        area.setAlignment(Pos.CENTER);
        area.setMaxWidth(410);

        if (header != null && !header.isBlank()) {
            Label headerLabel = new Label(header);
            headerLabel.setWrapText(true);
            headerLabel.setAlignment(Pos.CENTER);
            headerLabel.setMaxWidth(390);
            headerLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: " + palette.accent + ";");
            area.getChildren().add(headerLabel);
        }

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(390);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-line-spacing: 2;");

        if (variant == Variant.EXPORT) {
            VBox pathBox = new VBox(messageLabel);
            pathBox.setAlignment(Pos.CENTER);
            pathBox.setPadding(new Insets(11, 14, 11, 14));
            pathBox.setStyle(
                "-fx-background-color: " + palette.soft + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + palette.border + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;"
            );
            area.getChildren().add(pathBox);
        } else {
            area.getChildren().add(messageLabel);
        }

        return area;
    }

    private static HBox createButtonRow(String primaryText, String secondaryText, String accent,
                                        Stage dialog, boolean[] primaryChosen) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        if (secondaryText != null && !secondaryText.isBlank()) {
            row.getChildren().add(createSecondaryButton(secondaryText, dialog));
        }
        row.getChildren().add(createPrimaryButton(primaryText, accent, dialog, primaryChosen));
        return row;
    }

    private static Button createPrimaryButton(String text, String accent, Stage dialog, boolean[] primaryChosen) {
        Button button = new Button(text);
        button.setDefaultButton(true);
        button.setMinWidth(150);
        button.setStyle(buttonStyle(accent));
        button.setOnMouseEntered(e -> button.setOpacity(0.88));
        button.setOnMouseExited(e -> button.setOpacity(1));
        button.setOnAction(e -> {
            primaryChosen[0] = true;
            dialog.close();
        });
        return button;
    }

    private static Button createSecondaryButton(String text, Stage dialog) {
        Button button = new Button(text);
        button.setCancelButton(true);
        button.setMinWidth(120);
        button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: #64748b;" +
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #dbe4f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 10 24;" +
            "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: #475569;" +
            "-fx-background-color: #f8fafc;" +
            "-fx-border-color: #cbd5e1;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 10 24;" +
            "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: #64748b;" +
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #dbe4f0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999;" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 10 24;" +
            "-fx-cursor: hand;"
        ));
        button.setOnAction(e -> dialog.close());
        return button;
    }

    private static String buttonStyle(String accent) {
        return "-fx-font-size: 14px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: " + accent + ";" +
            "-fx-background-radius: 999;" +
            "-fx-padding: 10 28;" +
            "-fx-cursor: hand;";
    }

    private static int chooseHeight(Variant variant, String header, String message) {
        int height = variant == Variant.EXPORT ? 350 : 320;
        int lineCount = message == null ? 0 : message.split("\\n", -1).length;
        if (lineCount > 3) {
            height += Math.min(90, (lineCount - 3) * 18);
        }
        if (header != null && !header.isBlank()) {
            height += 18;
        }
        return height;
    }

    private static final class Palette {
        final String accent;
        final String soft;
        final String border;
        final String icon;

        private Palette(String accent, String soft, String border, String icon) {
            this.accent = accent;
            this.soft = soft;
            this.border = border;
            this.icon = icon;
        }

        static Palette forVariant(Variant variant) {
            switch (variant) {
                case SUCCESS:
                    return new Palette("#16a34a", "#f0fdf4", "#bbf7d0", "OK");
                case ERROR:
                    return new Palette("#ef4444", "#fef2f2", "#fecaca", "!");
                case WARNING:
                    return new Palette("#6366f1", "#eef2ff", "#c7d2fe", "i");
                case EXPORT:
                default:
                    return new Palette("#2563eb", "#eff6ff", "#bfdbfe", "EX");
            }
        }
    }
}
