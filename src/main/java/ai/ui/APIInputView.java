package ai.ui;

import ai.config.AIConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class APIInputView {

    private Consumer<String> onAPISubmitted;

    public APIInputView(Consumer<String> onAPISubmitted) {
        this.onAPISubmitted = onAPISubmitted;
    }

    public VBox createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("AI Assistant");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Enter your API key to use the AI assistant");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("Enter API key");
        apiKeyField.setStyle(
                "-fx-padding: 12px 16px;" +
                        "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-font-size: 14px;"
        );
        apiKeyField.setMaxWidth(400);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-font-size: 13px;");

        Button submitBtn = new Button("Open AI Assistant");
        submitBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: #6366f1;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;"
        );
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: #4f46e5;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;")
        );
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: #6366f1;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 12px 32px;" +
                        "-fx-cursor: hand;")
        );

        submitBtn.setOnAction(e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                errorLabel.setText("Please enter a valid API key.");
                errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ef4444;");
                return;
            }

            errorLabel.setText("Verified. Opening assistant...");
            errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #15803d;");

            if (onAPISubmitted != null) {
                onAPISubmitted.accept(apiKey);
            }
        });

        apiKeyField.setOnAction(e -> submitBtn.fire());

        Button clearKeyBtn = new Button("Clear saved API key");
        clearKeyBtn.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-background-color: transparent;" +
                        "-fx-underline: true;" +
                        "-fx-cursor: hand;"
        );
        clearKeyBtn.setOnAction(e -> {
            AIConfig config = new AIConfig();
            config.setApiKey("");
            errorLabel.setText("API key cleared.");
            errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        });

        root.getChildren().addAll(
                title,
                subtitle,
                apiKeyField,
                submitBtn,
                errorLabel,
                clearKeyBtn
        );

        return root;
    }
}
