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
                AiService.AIResponse response;
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
        messageBubble.setStyle("-fx-background-color: #4a90e2; -fx-padding: 12 16 12 16; -fx-background-radius: 8;");
        messageBubble.setMaxWidth(400);

        javafx.scene.control.TextArea messageArea = new javafx.scene.control.TextArea(message);
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-padding: 0; -fx-highlight-fill: #555555; -fx-control-inner-background: #4a90e2; -fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-hbar-policy: never; -fx-vbar-policy: never;");
        
        messageArea.setPrefWidth(400);
        messageArea.setPrefRowCount(1);
        messageArea.setMinHeight(1);
        messageArea.setMaxHeight(Double.MAX_VALUE);
        
        messageArea.textProperty().addListener((obs, oldText, newText) -> {
            fitTextAreaHeight(messageArea);
        });
        
        messageBubble.getChildren().add(messageArea);
        messageBox.getChildren().add(messageBubble);

        messageContainer.getChildren().addAll(nameLabel, messageBox);
        
        Platform.runLater(() -> {
            fitTextAreaHeight(messageArea);
        });
        
        return messageContainer;
    }

    private VBox createAIMessageBox(AiService.AIResponse response) {
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(4);
        messageContainer.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("AI Assistant");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        VBox contentContainer = new VBox();
        contentContainer.setSpacing(8);

        // 如果有思考内容，显示思考部分（可折叠）
        if (response.reasoning != null && !response.reasoning.isEmpty()) {
            VBox reasoningContainer = new VBox();
            reasoningContainer.setSpacing(4);

            // 创建可点击的标题
            HBox toggleBox = new HBox();
            toggleBox.setSpacing(4);
            toggleBox.setAlignment(Pos.CENTER_LEFT);
            toggleBox.setStyle("-fx-cursor: hand;");

            Label toggleArrow = new Label("▶");
            toggleArrow.setStyle("-fx-font-size: 10px; -fx-text-fill: #999999;");

            Label reasoningLabel = new Label("💭 思考过程");
            reasoningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999; -fx-font-weight: bold;");

            toggleBox.getChildren().addAll(toggleArrow, reasoningLabel);

            // 思考内容容器
            VBox reasoningContent = new VBox();
            reasoningContent.setSpacing(4);
            reasoningContent.setVisible(false);
            reasoningContent.setManaged(false);

            VBox reasoningBubble = new VBox();
            reasoningBubble.setStyle("-fx-background-color: #fff4d4; -fx-padding: 10 14 10 14; -fx-background-radius: 6;");
            reasoningBubble.setMaxWidth(400);

            javafx.scene.control.TextArea reasoningArea = new javafx.scene.control.TextArea(response.reasoning);
            reasoningArea.setEditable(false);
            reasoningArea.setWrapText(true);
            reasoningArea.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-background-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-padding: 0; -fx-highlight-fill: #cccccc; -fx-control-inner-background: transparent; -fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-hbar-policy: never; -fx-vbar-policy: never;");
            reasoningArea.setPrefWidth(372);
            reasoningArea.setPrefRowCount(1);
            reasoningArea.setMinHeight(1);
            reasoningArea.setMaxHeight(Double.MAX_VALUE);

            reasoningArea.textProperty().addListener((obs, oldText, newText) -> {
                fitTextAreaHeight(reasoningArea);
            });

            reasoningBubble.getChildren().add(reasoningArea);
            reasoningContent.getChildren().add(reasoningBubble);

            // 点击切换显示/隐藏
            toggleBox.setOnMouseClicked(e -> {
                boolean isVisible = reasoningContent.isVisible();
                reasoningContent.setVisible(!isVisible);
                reasoningContent.setManaged(!isVisible);
                toggleArrow.setText(isVisible ? "▶" : "▼");
            });

            reasoningContainer.getChildren().addAll(toggleBox, reasoningContent);
            contentContainer.getChildren().add(reasoningContainer);

            Platform.runLater(() -> {
                fitTextAreaHeight(reasoningArea);
            });
        }

        // 显示回答部分
        VBox answerContainer = new VBox();
        answerContainer.setSpacing(4);

        Label answerLabel = new Label("✨ 回答");
        answerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333; -fx-font-weight: bold;");

        VBox answerBubble = new VBox();
        answerBubble.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 12 16 12 16; -fx-background-radius: 8;");
        answerBubble.setMaxWidth(400);

        String answerText = response.answer != null ? response.answer : "无法获取回答";
        javafx.scene.control.TextArea answerArea = new javafx.scene.control.TextArea(answerText);
        answerArea.setEditable(false);
        answerArea.setWrapText(true);
        answerArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-background-color: transparent; -fx-border-width: 0; -fx-background-radius: 0; -fx-padding: 0; -fx-highlight-fill: #cccccc; -fx-control-inner-background: transparent; -fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-hbar-policy: never; -fx-vbar-policy: never;");
        answerArea.setPrefWidth(400);
        answerArea.setPrefRowCount(1);
        answerArea.setMinHeight(1);
        answerArea.setMaxHeight(Double.MAX_VALUE);

        answerArea.textProperty().addListener((obs, oldText, newText) -> {
            fitTextAreaHeight(answerArea);
        });

        answerBubble.getChildren().add(answerArea);
        answerContainer.getChildren().addAll(answerLabel, answerBubble);
        contentContainer.getChildren().add(answerContainer);

        Platform.runLater(() -> {
            fitTextAreaHeight(answerArea);
        });

        messageContainer.getChildren().addAll(nameLabel, contentContainer);
        return messageContainer;
    }

    // 保持向后兼容
    private VBox createAIMessageBox(String message) {
        AiService.AIResponse response = new AiService.AIResponse(null, message);
        return createAIMessageBox(response);
    }
    
    private void fitTextAreaHeight(javafx.scene.control.TextArea textArea) {
        textArea.applyCss();
        textArea.layout();
        
        String text = textArea.getText();
        if (text == null || text.isEmpty()) {
            textArea.setPrefHeight(30);
            textArea.setMinHeight(30);
            textArea.setMaxHeight(30);
            return;
        }
        
        javafx.scene.text.Text helper = new javafx.scene.text.Text(text);
        helper.setFont(textArea.getFont());
        helper.setWrappingWidth(368);
        double textHeight = helper.getLayoutBounds().getHeight();
        
        double prefHeight = textHeight + 20;
        prefHeight = Math.max(30, prefHeight);
        
        textArea.setPrefHeight(prefHeight);
        textArea.setMinHeight(prefHeight);
        textArea.setMaxHeight(prefHeight);
    }
}
