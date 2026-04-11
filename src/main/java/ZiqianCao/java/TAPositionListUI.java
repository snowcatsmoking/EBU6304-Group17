package ZiqianCao.java;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import data.JobDataManager;

import java.util.ArrayList;
import java.util.List;

public class TAPositionListUI extends Application {

    private BorderPane root;
    private StackPane rootContainer;
    private Label navItem1;
    private Label navItem2;
    private Label navItem3;
    private Label navItem4;
    private DashboardView dashboardView;
    private List<TAJob> jobList;
    private TAApplicationRecordManager recordManager;
    private String currentStudentId = "2024999";

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    private Stage primaryStage;
    private VBox overlay;

    public void navigateTo() {
        this.primaryStage = core.AppNavigator.getInstance().getStage();
        buildUI();
        core.AppNavigator.getInstance().navigateTo(new Scene(rootContainer), "TA Application System");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        core.AppNavigator.getInstance().init(primaryStage);
        buildUI();
        core.AppNavigator.getInstance().navigateTo(new Scene(rootContainer), "TA Application System");
    }

    private void buildUI() {
        rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #f5f5f5;");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        recordManager = new TAApplicationRecordManager();
        initJobList();

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        dashboardView = new DashboardView();
        dashboardView.setCurrentStudentId(currentStudentId);
        dashboardView.setNavigationListener(() -> switchToView("profile"));
        root.setCenter(dashboardView.getView());
        navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");

        overlay = new VBox();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setVisible(false);

        rootContainer.getChildren().addAll(root, overlay);
    }

    private void initJobList() {
        jobList = new JobDataManager().getAllJobs();
    }

    private VBox createSidebar() {
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
        navItem4 = createNavItem("Profile", "profile");

        navBox.getChildren().addAll(navItem1, navItem2, navItem3, navItem4);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button logoutButton = new javafx.scene.control.Button("Log Out");
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

        VBox logoutBox = new VBox(logoutButton);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        sidebar.getChildren().addAll(titleLabel, navBox, spacer, logoutBox);

        return sidebar;
    }

    private Label createNavItem(String text, String viewName) {
        Label item = new Label(text);
        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        item.setPrefWidth(220);
        item.setPrefHeight(40);
        item.setMinHeight(40);
        item.setMaxHeight(40);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setOnMouseClicked(e -> switchToView(viewName));
        return item;
    }

    private void resetNavItems() {
        navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem3.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem4.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }

    private void switchToView(String viewName) {
        resetNavItems();
        switch (viewName) {
            case "dashboard":
                root.setCenter(dashboardView.getView());
                navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "positions":
                root.setCenter(createPositionListView());
                navItem2.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "applications":
                root.setCenter(new MyApplicationsView(currentStudentId).getView());
                navItem3.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                root.setCenter(profileView.getView());
                navItem4.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
        }
    }

    private VBox createPositionListView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        filterBox.setPadding(new Insets(16, 16, 16, 16));

        Label filterLabel = new Label("All Positions");
        filterLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        filterBox.getChildren().add(filterLabel);

        VBox positionListBox = new VBox();
        positionListBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 1;");
        positionListBox.setSpacing(0);

        for (TAJob job : jobList) {
            VBox positionBox = createPositionBox(job);
            positionListBox.getChildren().add(positionBox);
        }

        content.getChildren().addAll(filterBox, positionListBox);

        return content;
    }

    private VBox createPositionBox(TAJob job) {
        VBox positionBox = new VBox();
        positionBox.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        positionBox.setPadding(new Insets(16, 16, 16, 16));
        positionBox.setSpacing(8);
        positionBox.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox();
        titleBox.setSpacing(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(job.getPositionName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        boolean manuallyClosed = job.isActive();
        boolean expired = isDeadlineExpired(job);

        if (manuallyClosed) {
            Label badge = new Label("Closed");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge);
        } else if (expired) {
            Label badge = new Label("Expired");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge);
        } else {
            titleBox.getChildren().add(titleLabel);
        }

        HBox infoBox = new HBox();
        infoBox.setSpacing(24);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course/Activity: " + job.getCourseName());
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label countLabel = new Label("Openings: " + job.getRecruitmentCount());
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label requirementLabel = new Label("Requirements: " + job.getRequirements());
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox deadlineBox = new HBox();
        deadlineBox.setSpacing(24);
        deadlineBox.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("Deadline: " + job.getDeadline());
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label publisherLabel = new Label("Posted By: " + job.getPublisher());
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        if (manuallyClosed) {
            Button closedButton = new Button("Closed by Organiser");
            closedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #999999; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            closedButton.setDisable(true);
            actionBox.getChildren().add(closedButton);
        } else if (expired) {
            Button expiredButton = new Button("Deadline Passed");
            expiredButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #999999; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            expiredButton.setDisable(true);
            actionBox.getChildren().add(expiredButton);
        } else {
            boolean hasApplied = recordManager.hasDuplicateApplication(currentStudentId, job.getJobId());
            if (hasApplied) {
                Button appliedButton = new Button("Applied");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #1890ff; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else {
                Button applyButton = new Button("Apply");
                applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                applyButton.setOnAction(e -> openApplicationForm(job));
                actionBox.getChildren().add(applyButton);
            }
        }

        infoBox.getChildren().addAll(courseLabel, countLabel, requirementLabel);
        deadlineBox.getChildren().addAll(deadlineLabel, publisherLabel);
        positionBox.getChildren().addAll(titleBox, infoBox, deadlineBox, actionBox);

        return positionBox;
    }

    private boolean isDeadlineExpired(TAJob job) {
        if (job.isActive()) {
            return true;
        }
        String deadline = job.getDeadline();
        if (deadline == null || deadline.trim().isEmpty() || deadline.equals("已截止")) {
            return true;
        }
        return false;
    }

    private void openApplicationForm(TAJob job) {
        overlay.setVisible(true);
        TAApplicationFormView formView = new TAApplicationFormView(job, currentStudentId);
        formView.setApplicationListener(() -> {
            overlay.setVisible(false);
            switchToView("positions");
        });
        formView.setDialogCloseListener(() -> {
            overlay.setVisible(false);
        });
        formView.showDialog(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
