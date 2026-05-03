package TA.java;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class AIChatComponent {

    private AiService aiService;
    private VBox chatContainer;
    private VBox currentLoadingMessage;
    private ScrollPane scrollPane;
    private AiService.UploadedFile pendingFile;
    private Stage primaryStage;

    public AIChatComponent() {
        this.aiService = new AiService();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public VBox createChatView() {
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #ffffff;");
        mainContainer.setPadding(new Insets(20, 20, 20, 20));
        mainContainer.setSpacing(20);

        Label titleLabel = new Label("AI Assistant");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        chatContainer = new VBox();
        chatContainer.setStyle("-fx-background-color: transparent;");
        chatContainer.setSpacing(12);
        chatContainer.setPadding(new Insets(16, 16, 16, 16));

        addWelcomeMessage();

        scrollPane.setContent(chatContainer);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        VBox inputContainer = new VBox();
        inputContainer.setSpacing(12);

        TextArea messageField = new TextArea();
        messageField.setPromptText("Type your message here... (Enter to send, Shift+Enter for new line)");
        messageField.setStyle("-fx-font-size: 14px; -fx-padding: 12 16 12 16; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-color: #ffffff;");
        messageField.setPrefHeight(80);

        HBox buttonContainer = new HBox();
        buttonContainer.setSpacing(12);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        Button uploadButton = new Button("📎 Upload File");
        uploadButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-background-color: #f0f0f0; -fx-padding: 12 24 12 24; -fx-cursor: hand; -fx-border-width: 1; -fx-border-color: #cccccc; -fx-border-radius: 4;");
        uploadButton.setOnAction(e -> openFileChooser());

        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 12 32 12 32; -fx-cursor: hand;");

        Runnable sendMessageAction = () -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() || pendingFile != null) {
                sendMessage(message, pendingFile);
                messageField.clear();
                pendingFile = null;
            }
        };

        sendButton.setOnAction(e -> sendMessageAction.run());

        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                sendMessageAction.run();
                event.consume();
            }
        });

        buttonContainer.getChildren().addAll(uploadButton, sendButton);
        inputContainer.getChildren().addAll(messageField, buttonContainer);

        mainContainer.getChildren().addAll(titleLabel, scrollPane, inputContainer);

        return mainContainer;
    }

    private void openFileChooser() {
        if (primaryStage == null) {
            VBox errorMsg = createAIMessageBox("请先设置 primaryStage 才能使用文件上传功能。");
            addMessageWithAnimation(errorMsg);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件上传");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Word", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Text", "*.txt"),
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mov")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            uploadFile(file);
        }
    }

    private void uploadFile(File file) {
        VBox uploadingMsg = createAIMessageBox("正在上传文件: " + file.getName() + " (" + formatFileSize(file.length()) + ")...");
        addMessageWithAnimation(uploadingMsg);

        new Thread(() -> {
            try {
                AiService.UploadedFile uploadedFile = aiService.uploadFile(file);
                
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(uploadingMsg);
                    pendingFile = uploadedFile;
                    
                    VBox successMsg = createAIMessageBox("文件上传成功: " + file.getName() + "\n现在可以发送消息，AI 将分析该文件。");
                    addMessageWithAnimation(successMsg);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(uploadingMsg);
                    VBox errorMsg = createAIMessageBox("文件上传失败: " + e.getMessage());
                    addMessageWithAnimation(errorMsg);
                });
            }
        }).start();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    private void addWelcomeMessage() {
        VBox aiMessage = createAIMessageBox(
            "Hello! I'm your AI assistant. How can I help you today with your TA applications?\n\n" +
            "你也可以点击 \"📎 Upload File\" 上传文件，然后发送消息让我分析文件内容。"
        );
        chatContainer.getChildren().add(aiMessage);
        Platform.runLater(() -> scrollToBottom());
    }

    private void sendMessage(String message, AiService.UploadedFile file) {
        if (file != null) {
            VBox userMessage;
            if (!message.isEmpty()) {
                userMessage = createUserMessageBox(message + "\n\n[附件: " + file.filename + "]");
            } else {
                userMessage = createUserMessageBox("[附件: " + file.filename + "]");
            }
            addMessageWithAnimation(userMessage);
        } else {
            VBox userMessage = createUserMessageBox(message);
            addMessageWithAnimation(userMessage);
        }

        currentLoadingMessage = createLoadingMessageBox();
        addMessageWithAnimation(currentLoadingMessage);

        new Thread(() -> {
            try {
                String response;
                if (file != null) {
                    response = aiService.sendMessageWithFile(message, file);
                } else {
                    response = aiService.sendMessage(message);
                }
                
                Platform.runLater(() -> {
                    if (currentLoadingMessage != null) {
                        chatContainer.getChildren().remove(currentLoadingMessage);
                    }
                    
                    VBox aiMessage = createAIMessageBox(response);
                    addMessageWithAnimation(aiMessage);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (currentLoadingMessage != null) {
                        chatContainer.getChildren().remove(currentLoadingMessage);
                    }
                    
                    VBox errorMessage = createAIMessageBox(
                        "抱歉，发生了错误：" + e.getMessage()
                    );
                    addMessageWithAnimation(errorMessage);
                });
            }
        }).start();
    }

    private void addMessageWithAnimation(VBox messageBox) {
        messageBox.setOpacity(0);
        chatContainer.getChildren().add(messageBox);
        
        scrollToBottom();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), messageBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> scrollToBottom());
        fadeIn.play();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }

    private VBox createLoadingMessageBox() {
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(4);
        messageContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("AI Assistant");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);

        VBox messageBubble = new VBox();
        messageBubble.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12 16 12 16; -fx-background-radius: 8;");
        messageBubble.setMaxWidth(400);

        Label messageLabel = new Label("正在思考中...");
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999;");

        messageBubble.getChildren().add(messageLabel);
        messageBox.getChildren().add(messageBubble);

        messageContainer.getChildren().addAll(nameLabel, messageBox);
        return messageContainer;
    }

    private VBox createUserMessageBox(String message) {
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(4);
        messageContainer.setAlignment(Pos.CENTER_RIGHT);

        Label nameLabel = new Label("You");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        VBox messageBubble = new VBox();
        messageBubble.setStyle("-fx-background-color: #333333; -fx-padding: 12 16 12 16; -fx-background-radius: 8;");
        messageBubble.setMaxWidth(400);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-wrap-text: true;");

        messageBubble.getChildren().add(messageLabel);
        messageBox.getChildren().add(messageBubble);

        messageContainer.getChildren().addAll(nameLabel, messageBox);
        return messageContainer;
    }

    private VBox createAIMessageBox(String message) {
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(4);
        messageContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("AI Assistant");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);

        VBox messageBubble = new VBox();
        messageBubble.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12 16 12 16; -fx-background-radius: 8;");
        messageBubble.setMaxWidth(400);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true;");

        messageBubble.getChildren().add(messageLabel);
        messageBox.getChildren().add(messageBubble);

        messageContainer.getChildren().addAll(nameLabel, messageBox);
        return messageContainer;
    }
}
