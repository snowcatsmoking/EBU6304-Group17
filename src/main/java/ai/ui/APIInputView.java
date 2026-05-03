package ai.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
        root.setStyle("-fx-background-color: #f9f9f9;");

        Label title = new Label("AI Assistant");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.rgb(44, 62, 80));

        Label subtitle = new Label("请输入API Key以使用AI助手");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.rgb(127, 140, 141));

        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("在此输入API Key");
        apiKeyField.setStyle(
                "-fx-padding: 15px; " +
                "-fx-background-color: white; " +
                "-fx-border-color: #ddd; " +
                "-fx-border-radius: 8px; " +
                "-fx-font-size: 14px;"
        );
        apiKeyField.setMaxWidth(400);

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.rgb(231, 76, 60));
        errorLabel.setFont(Font.font("Arial", 12));

        Button submitBtn = new Button("进入AI助手");
        submitBtn.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 15px 30px; " +
                "-fx-border-radius: 8px; " +
                "-fx-cursor: hand; " +
                "-fx-border: none;"
        );
        submitBtn.setOnMouseEntered(e -> {
            submitBtn.setStyle(
                    "-fx-background-color: #2980b9; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 15px 30px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-cursor: hand; " +
                    "-fx-border: none;"
            );
        });
        submitBtn.setOnMouseExited(e -> {
            submitBtn.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-padding: 15px 30px; " +
                    "-fx-border-radius: 8px; " +
                    "-fx-cursor: hand; " +
                    "-fx-border: none;"
            );
        });

        submitBtn.setOnAction(e -> {
            String apiKey = apiKeyField.getText().trim();
            if (apiKey.isEmpty()) {
                errorLabel.setText("请输入有效的API Key！");
                return;
            }

            errorLabel.setText("验证成功！正在进入...");
            errorLabel.setTextFill(Color.rgb(46, 204, 113));
            
            if (onAPISubmitted != null) {
                onAPISubmitted.accept(apiKey);
            }
        });

        apiKeyField.setOnAction(e -> submitBtn.fire());

        Label hint = new Label("API Key不会保存到本地，每次使用都需要重新输入");
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.rgb(149, 165, 166));

        root.getChildren().addAll(
                title,
                subtitle,
                apiKeyField,
                submitBtn,
                errorLabel,
                hint
        );

        return root;
    }
}
