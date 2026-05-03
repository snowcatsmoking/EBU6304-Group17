package TA.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SidebarComponent {

    public interface Navigable {
        void onNavigate(String viewName);
    }

    public interface LogoutListener {
        void onLogout();
    }

    private Navigable navigable;
    private LogoutListener logoutListener;

    private Label navItem1;
    private Label navItem2;
    private Label navItem3;
    private Label navItem4;
    private Label navItem5;
    private Label navItem6;

    public SidebarComponent() {}

    public void setNavigable(Navigable navigable) {
        this.navigable = navigable;
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    public VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setSpacing(0);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("TA System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        titleLabel.setPadding(new Insets(0, 0, 20, 16));

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

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button logoutButton = new Button("Log Out");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-background-color: transparent;" +
            "-fx-border-color: #cc0000; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            if (logoutListener != null) {
                logoutListener.onLogout();
            }
        });

        VBox logoutBox = new VBox(logoutButton);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        sidebar.getChildren().addAll(titleLabel, navBox, spacer, logoutBox);

        return sidebar;
    }

    public void setActiveNavItem(String viewName) {
        resetNavItems();
        switch (viewName) {
            case "dashboard":
                setNavActiveStyle(navItem1);
                break;
            case "positions":
                setNavActiveStyle(navItem2);
                break;
            case "applications":
                setNavActiveStyle(navItem3);
                break;
            case "favorites":
                setNavActiveStyle(navItem5);
                break;
            case "ai":
                setNavActiveStyle(navItem6);
                break;
            case "profile":
                setNavActiveStyle(navItem4);
                break;
        }
    }

    private Label createNavItem(String text, String viewName) {
        Label item = new Label(text);
        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
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
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }

    private void setNavActiveStyle(Label label) {
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
    }
}
