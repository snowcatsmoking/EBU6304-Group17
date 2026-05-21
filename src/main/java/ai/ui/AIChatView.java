package ai.ui;

import ai.model.AIResponse;
import ai.model.UploadedFile;
import ai.service.AIService;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class AIChatView {
    private static final double BUBBLE_WIDTH = 520;
    private static final String PRIMARY = "#6366f1";
    private static final String PRIMARY_DARK = "#4f46e5";
    private static final String TEXT = "#1e293b";
    private static final String MUTED = "#64748b";

    private final AIService aiService;
    private VBox chatContainer;
    private ScrollPane scrollPane;
    private UploadedFile pendingFile;
    private Label fileStatusLabel;
    private Stage primaryStage;

    public AIChatView() {
        this.aiService = new AIService();
    }

    public AIChatView(String apiKey) {
        this.aiService = new AIService(apiKey);
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public VBox createChatView() {
        VBox mainContainer = new VBox(18);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc, #eef2ff);");

        VBox header = createHeader();

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-width: 0;"
        );

        chatContainer = new VBox(14);
        chatContainer.setPadding(new Insets(18));
        chatContainer.setStyle(
                "-fx-background-color: rgba(255,255,255,0.72);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 18;"
        );
        addWelcomeMessage();

        scrollPane.setContent(chatContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox inputContainer = createInputArea();
        mainContainer.getChildren().addAll(header, scrollPane, inputContainer);
        return mainContainer;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setMinSize(42, 42);
        iconBadge.setPrefSize(42, 42);
        iconBadge.setMaxSize(42, 42);
        iconBadge.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #818cf8, #4f46e5);" +
                        "-fx-background-radius: 14;"
        );
        Label icon = new Label("AI");
        icon.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: white;");
        iconBadge.getChildren().add(icon);

        VBox titleText = new VBox(2);
        Label title = new Label("AI Assistant");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: " + TEXT + ";");
        Label subtitle = new Label("Ask about TA applications, requirements, matching, and uploaded documents.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED + ";");
        titleText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scopeBadge = new Label("TA guidance only");
        scopeBadge.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #4338ca;" +
                        "-fx-background-color: #e0e7ff;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 7 12;"
        );

        titleRow.getChildren().addAll(iconBadge, titleText, spacer, scopeBadge);
        header.getChildren().add(titleRow);
        return header;
    }

    private VBox createInputArea() {
        VBox inputContainer = new VBox(10);
        inputContainer.setPadding(new Insets(14));
        inputContainer.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 18;"
        );

        TextArea messageField = new TextArea();
        messageField.setPromptText("Ask a TA application question...");
        messageField.setWrapText(true);
        messageField.setPrefHeight(78);
        messageField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-prompt-text-fill: #94a3b8;" +
                        "-fx-padding: 10 12;" +
                        "-fx-background-color: #f8fafc;" +
                        "-fx-control-inner-background: #f8fafc;" +
                        "-fx-border-color: #dbe4f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        fileStatusLabel = new Label("No file attached");
        fileStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        Button uploadButton = createSecondaryButton("Attach file");
        uploadButton.setOnAction(e -> openFileChooser());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button sendButton = createPrimaryButton("Send");

        Runnable sendMessageAction = () -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty() || pendingFile != null) {
                sendMessage(message, pendingFile);
                messageField.clear();
                pendingFile = null;
                updateFileStatus(null);
            }
        };

        sendButton.setOnAction(e -> sendMessageAction.run());
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                sendMessageAction.run();
                event.consume();
            }
        });

        actionRow.getChildren().addAll(uploadButton, fileStatusLabel, spacer, sendButton);
        inputContainer.getChildren().addAll(messageField, actionRow);
        return inputContainer;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(104);
        button.setStyle(primaryButtonStyle(PRIMARY));
        button.setOnMouseEntered(e -> button.setStyle(primaryButtonStyle(PRIMARY_DARK)));
        button.setOnMouseExited(e -> button.setStyle(primaryButtonStyle(PRIMARY)));
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(secondaryButtonStyle("#ffffff"));
        button.setOnMouseEntered(e -> button.setStyle(secondaryButtonStyle("#f8fafc")));
        button.setOnMouseExited(e -> button.setStyle(secondaryButtonStyle("#ffffff")));
        return button;
    }

    private String primaryButtonStyle(String color) {
        return "-fx-font-size: 14px;" +
                "-fx-font-weight: 700;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 10 24;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle(String background) {
        return "-fx-font-size: 13px;" +
                "-fx-font-weight: 700;" +
                "-fx-text-fill: #475569;" +
                "-fx-background-color: " + background + ";" +
                "-fx-border-color: #dbe4f0;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 999;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: 9 16;" +
                "-fx-cursor: hand;";
    }

    private void openFileChooser() {
        if (primaryStage == null) {
            addMessageWithAnimation(createAIMessageBox("File upload is unavailable because the main window is not ready."));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to upload");
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
        VBox uploadingMsg = createAIMessageBox("Uploading " + file.getName() + " (" + formatFileSize(file.length()) + ")...");
        addMessageWithAnimation(uploadingMsg);
        updateFileStatus("Uploading " + file.getName() + "...");

        new Thread(() -> {
            try {
                UploadedFile uploadedFile = aiService.uploadFile(file);

                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(uploadingMsg);
                    pendingFile = uploadedFile;
                    updateFileStatus("Attached: " + file.getName());
                    addMessageWithAnimation(createAIMessageBox("File attached: " + file.getName() + "\nSend a message and I will analyze it with your question."));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatContainer.getChildren().remove(uploadingMsg);
                    pendingFile = null;
                    updateFileStatus(null);
                    addMessageWithAnimation(createAIMessageBox("File upload failed: " + e.getMessage()));
                });
            }
        }).start();
    }

    private void updateFileStatus(String text) {
        if (fileStatusLabel == null) {
            return;
        }
        if (text == null || text.isBlank()) {
            fileStatusLabel.setText("No file attached");
            fileStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        } else {
            fileStatusLabel.setText(text);
            fileStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4f46e5; -fx-font-weight: 700;");
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void addWelcomeMessage() {
        VBox aiMessage = createAIMessageBox(
                "Hello! I can help with TA applications, position requirements, profile preparation, and uploaded files.\n\n" +
                        "Attach a document or ask a question to get started."
        );
        chatContainer.getChildren().add(aiMessage);
        Platform.runLater(this::scrollToBottom);
    }

    private void sendMessage(String message, UploadedFile file) {
        if (file != null) {
            String fileMessage = message.isEmpty()
                    ? "[Attached file: " + file.filename + "]"
                    : message + "\n\n[Attached file: " + file.filename + "]";
            addMessageWithAnimation(createUserMessageBox(fileMessage));
        } else {
            addMessageWithAnimation(createUserMessageBox(message));
        }

        VBox aiMessage = createStreamingMessageBox();
        addMessageWithAnimation(aiMessage);

        TextArea reasoningArea = (TextArea) aiMessage.lookup("#reasoningArea");
        TextArea answerArea = (TextArea) aiMessage.lookup("#answerArea");

        new Thread(() -> {
            try {
                if (file != null) {
                    aiService.sendMessageWithFileStream(message, file,
                            reasoningDelta -> updateArea(reasoningArea, reasoningDelta),
                            answerDelta -> updateArea(answerArea, answerDelta),
                            () -> Platform.runLater(this::scrollToBottom)
                    );
                } else {
                    aiService.sendMessageStream(message,
                            reasoningDelta -> updateArea(reasoningArea, reasoningDelta),
                            answerDelta -> updateArea(answerArea, answerDelta),
                            () -> Platform.runLater(this::scrollToBottom)
                    );
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (answerArea != null) {
                        String errorMessage = "Sorry, something went wrong: " + e.getMessage();
                        String exceptionString = e.toString();
                        if (exceptionString.contains("401") ||
                                exceptionString.contains("AuthenticationError") ||
                                exceptionString.contains("Unauthorized") ||
                                exceptionString.contains("API key")) {
                            errorMessage = "API key error. Please check that your API key is correct.";
                        }
                        answerArea.setText(core.UiText.tr(errorMessage));
                        fitTextAreaHeight(answerArea, BUBBLE_WIDTH - 32);
                    }
                });
            }
        }).start();
    }

    private void updateArea(TextArea area, String text) {
        Platform.runLater(() -> {
            if (area != null) {
                area.setText(text);
                fitTextAreaHeight(area, BUBBLE_WIDTH - 32);
            }
        });
    }

    private VBox createStreamingMessageBox() {
        VBox messageContainer = createMessageContainer(Pos.CENTER_LEFT, "AI Assistant");

        VBox contentContainer = new VBox(8);
        contentContainer.getChildren().add(createReasoningBlock(null));
        contentContainer.getChildren().add(createAnswerBlock(""));

        messageContainer.getChildren().add(contentContainer);
        return messageContainer;
    }

    private VBox createReasoningBlock(String text) {
        VBox reasoningContainer = new VBox(6);

        HBox toggleBox = new HBox(6);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setStyle("-fx-cursor: hand;");

        Label toggleArrow = new Label(">");
        toggleArrow.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: 800;");

        Label reasoningLabel = new Label("Reasoning");
        reasoningLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 800;");
        toggleBox.getChildren().addAll(toggleArrow, reasoningLabel);

        VBox reasoningContent = new VBox();
        reasoningContent.setVisible(false);
        reasoningContent.setManaged(false);

        VBox reasoningBubble = createBubble("#f8fafc", "#e2e8f0", BUBBLE_WIDTH);
        TextArea reasoningArea = createReadOnlyArea(text == null ? "" : text, "#64748b", BUBBLE_WIDTH - 32, "transparent");
        reasoningArea.setId("reasoningArea");
        reasoningBubble.getChildren().add(reasoningArea);
        reasoningContent.getChildren().add(reasoningBubble);

        toggleBox.setOnMouseClicked(e -> {
            boolean isVisible = reasoningContent.isVisible();
            reasoningContent.setVisible(!isVisible);
            reasoningContent.setManaged(!isVisible);
            toggleArrow.setText(isVisible ? ">" : "v");
        });

        reasoningContainer.getChildren().addAll(toggleBox, reasoningContent);
        return reasoningContainer;
    }

    private VBox createAnswerBlock(String text) {
        VBox answerContainer = new VBox(6);
        Label answerLabel = new Label("Answer");
        answerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155; -fx-font-weight: 800;");

        VBox answerBubble = createBubble("#ffffff", "#e2e8f0", BUBBLE_WIDTH);
        TextArea answerArea = createReadOnlyArea(text, TEXT, BUBBLE_WIDTH - 32, "#ffffff");
        answerArea.setId("answerArea");
        answerBubble.getChildren().add(answerArea);
        answerContainer.getChildren().addAll(answerLabel, answerBubble);
        return answerContainer;
    }

    private void addMessageWithAnimation(VBox messageBox) {
        messageBox.setOpacity(0);
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), messageBox);
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

    private VBox createUserMessageBox(String message) {
        VBox messageContainer = createMessageContainer(Pos.CENTER_RIGHT, "You");

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        VBox messageBubble = createBubble(PRIMARY, PRIMARY, BUBBLE_WIDTH);
        TextArea messageArea = createReadOnlyArea(message, "#ffffff", BUBBLE_WIDTH - 32, PRIMARY);
        messageBubble.getChildren().add(messageArea);
        row.getChildren().add(messageBubble);

        messageContainer.getChildren().add(row);
        Platform.runLater(() -> fitTextAreaHeight(messageArea, BUBBLE_WIDTH - 32));
        return messageContainer;
    }

    private VBox createAIMessageBox(String message) {
        return createAIMessageBox(new AIResponse(null, message));
    }

    private VBox createAIMessageBox(AIResponse response) {
        VBox messageContainer = createMessageContainer(Pos.CENTER_LEFT, "AI Assistant");
        VBox contentContainer = new VBox(8);

        if (response.reasoning != null && !response.reasoning.isEmpty()) {
            contentContainer.getChildren().add(createReasoningBlock(response.reasoning));
        }

        String answerText = response.answer != null ? response.answer : "No answer was returned.";
        VBox answerBlock = createAnswerBlock(answerText);
        contentContainer.getChildren().add(answerBlock);

        messageContainer.getChildren().add(contentContainer);
        Platform.runLater(() -> {
            TextArea answerArea = (TextArea) answerBlock.lookup("#answerArea");
            fitTextAreaHeight(answerArea, BUBBLE_WIDTH - 32);
        });
        return messageContainer;
    }

    private VBox createMessageContainer(Pos alignment, String sender) {
        VBox messageContainer = new VBox(5);
        messageContainer.setAlignment(alignment);

        Label nameLabel = new Label(sender);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 700;");
        messageContainer.getChildren().add(nameLabel);
        return messageContainer;
    }

    private VBox createBubble(String background, String border, double width) {
        VBox bubble = new VBox();
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(width);
        bubble.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;"
        );
        return bubble;
    }

    private TextArea createReadOnlyArea(String text, String textColor, double width, String innerBackground) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(width);
        area.setPrefRowCount(1);
        area.setMinHeight(30);
        area.setMaxHeight(Double.MAX_VALUE);
        area.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: " + innerBackground + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-highlight-fill: #c7d2fe;" +
                        "-fx-text-box-border: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;" +
                        "-fx-hbar-policy: never;" +
                        "-fx-vbar-policy: never;"
        );
        area.textProperty().addListener((obs, oldText, newText) -> fitTextAreaHeight(area, width));
        Platform.runLater(() -> fitTextAreaHeight(area, width));
        return area;
    }

    private void fitTextAreaHeight(TextArea textArea, double wrappingWidth) {
        if (textArea == null) {
            return;
        }
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
        helper.setWrappingWidth(wrappingWidth);
        double textHeight = helper.getLayoutBounds().getHeight();

        double prefHeight = Math.max(30, textHeight + 20);
        textArea.setPrefHeight(prefHeight);
        textArea.setMinHeight(prefHeight);
        textArea.setMaxHeight(prefHeight);
    }
}
