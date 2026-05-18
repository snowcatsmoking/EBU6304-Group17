package Admin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminDashboard extends Application {

    private String adminId;
    private BorderPane root;
    private Label activeNavLabel;

    private static final String NAV_DEFAULT =
        "-fx-font-size: 14px; -fx-text-fill: #555555; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 0 3;";
    private static final String NAV_ACTIVE =
        "-fx-font-size: 14px; -fx-text-fill: #000000; -fx-font-weight: 600; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: #f0f0f0; -fx-border-color: #000000; -fx-border-width: 0 0 0 3;";

    public AdminDashboard() {
        this.adminId = "admin";
    }

    public AdminDashboard(String adminId) {
        this.adminId = adminId;
    }

    /** Called from LoginView after login — uses the shared Stage via AppNavigator. */
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
        root.setStyle("-fx-background-color: #fafafa;");
        root.setLeft(buildSidebar());
        showDashboard();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #ffffff;");

        // Top padding spacer
        Label topSpacer = new Label();
        topSpacer.setPrefHeight(24);

        Label navDashboard  = buildNavItem("Dashboard");
        Label navUsers      = buildNavItem("User Management");
        Label navPositions  = buildNavItem("Global Positions");
        Label navLogs       = buildNavItem("Operation Logs");
        Label navStats      = buildNavItem("Recruitment Stats");
        Label navWorkload   = buildNavItem("Workload Alert");
        Label navExport     = buildNavItem("Data Export");
        Label navBackup     = buildNavItem("Backup / Restore");

        navDashboard.setStyle(NAV_ACTIVE);
        activeNavLabel = navDashboard;

        navDashboard.setOnMouseClicked(e ->  { setActive(navDashboard);  showDashboard(); });
        navUsers.setOnMouseClicked(e ->      { setActive(navUsers);      showUserManagement(); });
        navPositions.setOnMouseClicked(e ->  { setActive(navPositions);  showGlobalPositions(); });
        navLogs.setOnMouseClicked(e ->       { setActive(navLogs);       showOperationLog(); });
        navStats.setOnMouseClicked(e ->      { setActive(navStats);      showRecruitmentStats(); });
        navWorkload.setOnMouseClicked(e ->   { setActive(navWorkload);   showWorkload(); });
        navExport.setOnMouseClicked(e ->     { setActive(navExport);     showDataExport(); });
        navBackup.setOnMouseClicked(e ->     { setActive(navBackup);     showBackupRestore(); });

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        javafx.scene.control.Button logoutButton = new javafx.scene.control.Button("Log out");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-background-color: transparent;" +
            "-fx-border-color: #cc0000; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            try {
                LoginScreen.LoginView loginView = new LoginScreen.LoginView();
                core.AppNavigator.getInstance().navigateTo(loginView.buildLoginScene(), "TA Recruitment System - Login");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        javafx.scene.layout.VBox logoutBox = new javafx.scene.layout.VBox(logoutButton);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        sidebar.getChildren().addAll(
            topSpacer,
            navDashboard, navUsers, navPositions, navLogs, navStats, navWorkload, navExport, navBackup,
            spacer, logoutBox
        );
        return sidebar;
    }

    private Label buildNavItem(String text) {
        Label lbl = new Label(text);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setStyle(NAV_DEFAULT);
        return lbl;
    }

    private void setActive(Label target) {
        if (activeNavLabel != null) activeNavLabel.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavLabel = target;
    }

    private void showDashboard() {
        root.setCenter(new DashboardView().build());
    }

    private void showUserManagement() {
        root.setCenter(new UserManagementView(adminId).build());
    }

    private void showGlobalPositions() {
        root.setCenter(new GlobalPositionsView().build());
    }

    private void showRecruitmentStats() {
        root.setCenter(new RecruitmentStatsView().build());
    }

    private void showWorkload() {
        root.setCenter(new WorkloadView().build());
    }

    private void showDataExport() {
        root.setCenter(new DataExportView().build());
    }

    private void showBackupRestore() {
        root.setCenter(new BackupRestoreView().build());
    }

    private void showOperationLog() {
        root.setCenter(new OperationLogView().build());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
