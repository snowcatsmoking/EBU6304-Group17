package core;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Singleton that holds the single application Stage.
 * All page transitions swap the Scene on this stage instead of
 * opening/closing new Stage instances, so the window position and
 * size stay constant throughout the entire session.
 */
public class AppNavigator {

    private static final int WIDTH  = 1200;
    private static final int HEIGHT = 750;

    private static AppNavigator instance;
    private Stage primaryStage;

    private AppNavigator() {}

    public static AppNavigator getInstance() {
        if (instance == null) {
            instance = new AppNavigator();
        }
        return instance;
    }

    /** Called once at startup by LoginView.start() */
    public void init(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setResizable(false);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
    }

    public Stage getStage() {
        return primaryStage;
    }

    public void navigateTo(Scene scene, String title) {
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
        UiText.localize(scene.getRoot());
        primaryStage.setTitle(UiText.tr(title));
        primaryStage.setScene(scene);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }
}
