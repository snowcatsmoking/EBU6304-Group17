package TA.java.component;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SidebarComponent {

    public interface Navigable {
        void onNavigate(String viewName);
    }

    public interface LogoutListener {
        void onLogout();
    }

    private Navigable navigable;
    private LogoutListener logoutListener;
    private Runnable languageChangeListener;

    private Label navItem1;
    private Label navItem2;
    private Label navItem3;
    private Label navItem4;
    private Label navItem5;
    private Label navItem6;

    private Region navIndicator;
    private TranslateTransition indicatorAnim;

    public SidebarComponent() {}

    public void setNavigable(Navigable navigable) {
        this.navigable = navigable;
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    public void setLanguageChangeListener(Runnable listener) {
        this.languageChangeListener = listener;
    }

    public VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(220);
        sidebar.setMaxWidth(220);
        sidebar.setMaxHeight(Double.MAX_VALUE);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setSpacing(0);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("TA System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        titleLabel.setPadding(new Insets(0, 0, 20, 16));

        // Sliding indicator + nav items in a StackPane (like login tabs)
        StackPane navStack = new StackPane();
        navStack.setAlignment(Pos.TOP_LEFT);

        navIndicator = new Region();
        navIndicator.setPrefWidth(220);
        navIndicator.setMaxWidth(220);
        navIndicator.setPrefHeight(40);
        navIndicator.setMaxHeight(40);
        navIndicator.setStyle(
            "-fx-background-color: #f1f5f9;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-color: #6366f1;"
        );
        navIndicator.setTranslateY(0);

        indicatorAnim = new TranslateTransition(Duration.millis(300), navIndicator);

        VBox navBox = new VBox();
        navBox.setSpacing(0);
        navBox.setAlignment(Pos.TOP_LEFT);

        navItem1 = createNavItem("Dashboard", "dashboard");
        navItem2 = createNavItem("Positions", "positions");
        navItem3 = createNavItem("My Applications", "applications");
        navItem5 = createNavItem("My Favorites", "favorites");
        navItem6 = createNavItem("AI Assistant", "ai");
        navItem4 = createNavItem("Profile", "profile");

        navBox.getChildren().addAll(navItem1, navItem2, navItem3, navItem5, navItem6, navItem4);

        navStack.getChildren().addAll(navIndicator, navBox);

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutButton = new Button("Log Out");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: transparent;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        logoutButton.setOnMouseEntered(e ->
            logoutButton.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
                "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;")
        );
        logoutButton.setOnMouseExited(e ->
            logoutButton.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: transparent;" +
                "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;")
        );
        logoutButton.setOnAction(e -> {
            if (logoutListener != null) {
                logoutListener.onLogout();
            }
        });

        VBox logoutBox = new VBox(logoutButton);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        VBox languageBox = new VBox(core.LanguageSwitcher.create(() -> {
            if (languageChangeListener != null) {
                languageChangeListener.run();
            }
        }));
        languageBox.setPadding(new Insets(0, 16, 10, 16));

        sidebar.getChildren().addAll(titleLabel, navStack, spacer, languageBox, logoutBox);

        core.UiText.localize(sidebar);

        return sidebar;
    }

    public void setActiveNavItem(String viewName) {
        int targetIndex = 0;
        switch (viewName) {
            case "dashboard":    targetIndex = 0; break;
            case "positions":    targetIndex = 1; break;
            case "applications": targetIndex = 2; break;
            case "favorites":    targetIndex = 3; break;
            case "ai":           targetIndex = 4; break;
            case "profile":      targetIndex = 5; break;
        }

        double targetY = targetIndex * 40;

        if (indicatorAnim != null) {
            indicatorAnim.stop();
            indicatorAnim.setFromY(navIndicator.getTranslateY());
            indicatorAnim.setToY(targetY);
            indicatorAnim.play();
        } else {
            navIndicator.setTranslateY(targetY);
        }

        resetNavItems();
        switch (viewName) {
            case "dashboard":    setNavActiveStyle(navItem1); break;
            case "positions":    setNavActiveStyle(navItem2); break;
            case "applications": setNavActiveStyle(navItem3); break;
            case "favorites":    setNavActiveStyle(navItem5); break;
            case "ai":           setNavActiveStyle(navItem6); break;
            case "profile":      setNavActiveStyle(navItem4); break;
        }
    }

    private Label createNavItem(String text, String viewName) {
        Label item = new Label(text);
        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        item.setPrefWidth(220);
        item.setPrefHeight(40);
        item.setMinHeight(40);
        item.setMaxHeight(40);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setOnMouseClicked(e -> {
            if (navigable != null) {
                navigable.onNavigate(viewName);
            }
        });
        return item;
    }

    private void resetNavItems() {
        setNavDefaultStyle(navItem1);
        setNavDefaultStyle(navItem2);
        setNavDefaultStyle(navItem3);
        setNavDefaultStyle(navItem4);
        setNavDefaultStyle(navItem5);
        setNavDefaultStyle(navItem6);
    }

    private void setNavDefaultStyle(Label label) {
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }

    private void setNavActiveStyle(Label label) {
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #6366f1; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }
}
