package TA.java;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Compatibility wrapper for older TA code paths.
 * The active assistant UI lives in ai.ui.AIChatView.
 */
public class AIChatComponent {
    private final ai.ui.AIChatView chatView = new ai.ui.AIChatView();

    public void setPrimaryStage(Stage stage) {
        chatView.setPrimaryStage(stage);
    }

    public VBox createChatView() {
        return chatView.createChatView();
    }
}
