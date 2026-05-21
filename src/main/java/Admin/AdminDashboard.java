package Admin;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminDashboard extends Application {

    private String adminId;
    private BorderPane root;
    private BorderPane contentPane;

    private Label activeNavLabel;
    private Region navIndicator;
    private TranslateTransition indicatorAnim;
    private static final double NAV_H = 40.0;

    private static final String NAV_DEFAULT =
        "-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-background-color: transparent;";
    private static final String NAV_ACTIVE =
        "-fx-font-size: 14px; -fx-text-fill: #6366f1; -fx-font-weight: 600; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-background-color: transparent;";

    public AdminDashboard() { this.adminId = "admin"; }
    public AdminDashboard(String adminId) { this.adminId = adminId; }

    public void navigateTo() {
        buildUI();
        core.AppNavigator.getInstance().navigateTo(new Scene(root), "TA Recruitment System - Admin Console");
    }

    @Override
    public void start(Stage stage) {
        core.AppNavigator.getInstance().init(stage);
        buildUI();
        core.AppNavigator.getInstance().navigateTo(new Scene(root), "TA Recruitment System - Admin Console");
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");
        root.setLeft(buildSidebar());

        contentPane = new BorderPane();
        contentPane.setStyle("-fx-background-color: #f8fafc;");
        contentPane.centerProperty().addListener((obs, oldCenter, newCenter) ->
            javafx.application.Platform.runLater(() -> core.UiText.localize(root)));
        contentPane.setTop(buildTopBar());
        root.setCenter(contentPane);

        showWithFade(new DashboardView().build());
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(8, 16, 8, 16));
        topBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label welcomeLabel = new Label(core.UiText.tr("Welcome back, "));
        welcomeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");

        Label nameLabel = new Label(adminId);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label roleLabel = new Label(core.UiText.tr("(Admin)"));
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: #eef2ff; -fx-padding: 2 8 2 8; -fx-border-radius: 4; -fx-background-radius: 4;");

        HBox langBox = core.LanguageSwitcher.create(() -> {
            buildUI();
            core.AppNavigator.getInstance().navigateTo(new Scene(root), "TA Recruitment System - Admin Console");
        });
        langBox.setPadding(Insets.EMPTY);

        topBar.getChildren().addAll(welcomeLabel, nameLabel, roleLabel, langBox);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setMaxHeight(Double.MAX_VALUE);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");

        Label titleLabel = new Label("Admin System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        titleLabel.setPadding(new Insets(20, 0, 20, 16));

        Label navDashboard = buildNavItem("Dashboard");
        Label navUsers     = buildNavItem("User Management");
        Label navPositions = buildNavItem("Global Positions");
        Label navLogs      = buildNavItem("Operation Logs");
        Label navStats     = buildNavItem("Recruitment Stats");
        Label navWorkload  = buildNavItem("Workload Alert");
        Label navExport    = buildNavItem("Data Export");
        Label navBackup    = buildNavItem("Backup / Restore");

        // Sliding indicator
        navIndicator = new Region();
        navIndicator.setPrefWidth(240);
        navIndicator.setMaxWidth(240);
        navIndicator.setPrefHeight(NAV_H);
        navIndicator.setMaxHeight(NAV_H);
        navIndicator.setStyle(
            "-fx-background-color: #eef2ff;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-color: #6366f1;");
        navIndicator.setTranslateY(0);
        navIndicator.setMouseTransparent(true);

        indicatorAnim = new TranslateTransition(Duration.millis(280), navIndicator);
        indicatorAnim.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        VBox navBox = new VBox();
        navBox.setAlignment(Pos.TOP_LEFT);
        navBox.getChildren().addAll(
            navDashboard, navUsers, navPositions, navLogs,
            navStats, navWorkload, navExport, navBackup);

        StackPane navStack = new StackPane();
        navStack.setAlignment(Pos.TOP_LEFT);
        navStack.getChildren().addAll(navIndicator, navBox);

        // Click handlers
        navDashboard.setOnMouseClicked(e -> { setActive(navDashboard, 0); showWithFade(new DashboardView().build()); });
        navUsers    .setOnMouseClicked(e -> { setActive(navUsers,     1); showWithFade(new UserManagementView(adminId).build()); });
        navPositions.setOnMouseClicked(e -> { setActive(navPositions, 2); showWithFade(new GlobalPositionsView().build()); });
        navLogs     .setOnMouseClicked(e -> { setActive(navLogs,      3); showWithFade(new OperationLogView().build()); });
        navStats    .setOnMouseClicked(e -> { setActive(navStats,     4); showWithFade(new RecruitmentStatsView().build()); });
        navWorkload .setOnMouseClicked(e -> { setActive(navWorkload,  5); showWithFade(new WorkloadView().build()); });
        navExport   .setOnMouseClicked(e -> { setActive(navExport,   6); showWithFade(new DataExportView().build()); });
        navBackup   .setOnMouseClicked(e -> { setActive(navBackup,   7); showWithFade(new BackupRestoreView().build()); });

        navDashboard.setStyle(NAV_ACTIVE);
        activeNavLabel = navDashboard;

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        javafx.scene.control.Button logoutBtn = new javafx.scene.control.Button("Log Out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        String logoutOff = "-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: transparent;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;";
        String logoutOn  = "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;";
        logoutBtn.setStyle(logoutOff);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutOn));
        logoutBtn.setOnMouseExited(e ->  logoutBtn.setStyle(logoutOff));
        logoutBtn.setOnAction(e -> {
            try {
                core.AppNavigator.getInstance().navigateTo(
                    new LoginScreen.LoginView().buildLoginScene(), "TA Recruitment System - Login");
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        VBox logoutBox = new VBox(logoutBtn);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        sidebar.getChildren().addAll(titleLabel, navStack, spacer, logoutBox);
        return sidebar;
    }

    private Label buildNavItem(String text) {
        Label lbl = new Label(text);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setPrefHeight(NAV_H);
        lbl.setMinHeight(NAV_H);
        lbl.setMaxHeight(NAV_H);
        lbl.setAlignment(Pos.CENTER_LEFT);
        lbl.setStyle(NAV_DEFAULT);
        return lbl;
    }

    private void setActive(Label target, int index) {
        if (activeNavLabel != null) activeNavLabel.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavLabel = target;

        double toY = index * NAV_H;
        indicatorAnim.stop();
        indicatorAnim.setFromY(navIndicator.getTranslateY());
        indicatorAnim.setToY(toY);
        indicatorAnim.play();
    }

    private void showWithFade(Node content) {
        content.setOpacity(0);
        contentPane.setCenter(content);
        core.UiText.localize(root);
        FadeTransition ft = new FadeTransition(Duration.millis(180), content);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public static void main(String[] args) { launch(args); }
}
