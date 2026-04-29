package TA.java;

import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.FadeTransition;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.JobDataManager;

import java.io.File;
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
    private FavoriteManager favoriteManager;
    private String currentStudentId = "2024999";
    private int currentPage = 1;
    private static final int PAGE_SIZE = 3;
    private VBox positionListBox;
    private Label navItem5;

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    private Stage primaryStage;
    private VBox overlay;

    /** Called from LoginView after login — uses the shared Stage via AppNavigator. */
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
        data.DataConfig.initAllDirs();
        rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #f5f5f5;");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        recordManager = new TAApplicationRecordManager();
        favoriteManager = new FavoriteManager();
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

        // 检查并显示申请状态变更通知 - 延迟显示，让控制台先显示
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(this::checkAndShowNotifications);
        });
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
        navItem5 = createNavItem("My Favorites", "favorites");
        navItem4 = createNavItem("Profile", "profile");

        navBox.getChildren().addAll(navItem1, navItem2, navItem3, navItem5, navItem4);

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
        navItem5.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
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
            case "favorites":
                root.setCenter(createFavoritesView());
                navItem5.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                root.setCenter(profileView.getView());
                navItem4.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
        }
    }

    private javafx.scene.control.TextField courseNameField;
    private javafx.scene.control.TextField availableTimeField;
    private javafx.scene.control.TextField recruitmentCountField;
    private List<TAJob> filteredJobList;

    private VBox createPositionListView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course Name:");
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        courseNameField = new javafx.scene.control.TextField();
        courseNameField.setPromptText("Enter course name");
        courseNameField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        courseNameField.setPrefWidth(150);

        Label timeLabel = new Label("Available Time:");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        availableTimeField = new javafx.scene.control.TextField();
        availableTimeField.setPromptText("YYYY-MM-DD");
        availableTimeField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        availableTimeField.setPrefWidth(150);

        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        recruitmentCountField = new javafx.scene.control.TextField();
        recruitmentCountField.setPromptText("Enter number");
        recruitmentCountField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        recruitmentCountField.setPrefWidth(100);

        Button filterButton = new Button("Filter");
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        filterButton.setOnAction(e -> applyFilters());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        resetButton.setOnAction(e -> resetFilters());

        filterBox.getChildren().addAll(courseLabel, courseNameField, timeLabel, availableTimeField, countLabel, recruitmentCountField, filterButton, resetButton);

        positionListBox = new VBox();
        positionListBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        positionListBox.setSpacing(0);

        filteredJobList = new ArrayList<>(jobList);

        HBox paginationBox = createPaginationBox();

        refreshPositionList();

        content.getChildren().addAll(filterBox, positionListBox, paginationBox);

        return content;
    }

    private void applyFilters() {
        String courseName = courseNameField.getText().trim();
        String availableTime = availableTimeField.getText().trim();
        String recruitmentCount = recruitmentCountField.getText().trim();

        filteredJobList = new ArrayList<>();
        for (TAJob job : jobList) {
            boolean match = true;

            if (!courseName.isEmpty() && !job.getCourseName().toLowerCase().contains(courseName.toLowerCase())) {
                match = false;
            }

            if (!availableTime.isEmpty()) {
                try {
                    // 解析用户输入的日期
                    java.time.LocalDate inputDate = java.time.LocalDate.parse(availableTime, 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    // 解析岗位的截止日期
                    java.time.LocalDate deadlineDate = java.time.LocalDate.parse(job.getDeadline(), 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    // 只保留截止日期在输入日期及之前的岗位
                    if (deadlineDate.isAfter(inputDate)) {
                        match = false;
                    }
                } catch (Exception e) {
                    // 输入格式不正确，不匹配
                    match = false;
                }
            }

            if (!recruitmentCount.isEmpty()) {
                try {
                    int count = Integer.parseInt(recruitmentCount);
                    if (job.getRecruitmentCount() != count) {
                        match = false;
                    }
                } catch (NumberFormatException e) {
                    // 输入不是数字，不匹配
                    match = false;
                }
            }

            if (match) {
                filteredJobList.add(job);
            }
        }

        currentPage = 1;
        refreshPositionList();
    }

    private void resetFilters() {
        courseNameField.clear();
        availableTimeField.clear();
        recruitmentCountField.clear();
        filteredJobList = new ArrayList<>(jobList);
        currentPage = 1;
        refreshPositionList();
    }

    private void refreshPositionList() {
        positionListBox.getChildren().clear();
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredJobList.size());

        for (int i = start; i < end; i++) {
            TAJob job = filteredJobList.get(i);
            VBox positionBox = createPositionBox(job);
            positionListBox.getChildren().add(positionBox);
        }
    }

    private HBox createPaginationBox() {
        HBox paginationBox = new HBox();
        paginationBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 1;");
        paginationBox.setPadding(new Insets(16, 20, 16, 20));
        paginationBox.setSpacing(16);
        paginationBox.setAlignment(Pos.CENTER);

        int totalPages = (int) Math.ceil((double) filteredJobList.size() / PAGE_SIZE);

        Button prevButton = new Button("Previous");
        prevButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        prevButton.setDisable(currentPage == 1);
        if (currentPage == 1) {
            prevButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #cccccc; -fx-background-color: #f5f5f5; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: not-allowed;");
        }
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshPositionList();
                switchToView("positions");
            }
        });

        Label pageInfo = new Label("Page " + currentPage + " of " + totalPages + "  (" + filteredJobList.size() + " positions)");
        pageInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Button nextButton = new Button("Next");
        nextButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        nextButton.setDisable(currentPage == totalPages);
        if (currentPage == totalPages) {
            nextButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #cccccc; -fx-background-color: #f5f5f5; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: not-allowed;");
        }
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshPositionList();
                switchToView("positions");
            }
        });

        paginationBox.getChildren().addAll(prevButton, pageInfo, nextButton);

        return paginationBox;
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
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        boolean isFav = favoriteManager.isFavorite(currentStudentId, job.getJobId());
        javafx.scene.control.Button favButton = new javafx.scene.control.Button(isFav ? "★" : "☆");
        favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (isFav ? "#ffd700" : "#cccccc") + "; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
        favButton.setOnAction(e -> {
            boolean isCurrentlyFav = favoriteManager.isFavorite(currentStudentId, job.getJobId());
            
            // 缩放动画：先放大再缩小
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), favButton);
            scaleUp.setToX(1.5);
            scaleUp.setToY(1.5);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), favButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            
            SequentialTransition anim = new SequentialTransition(scaleUp, scaleDown);
            
            if (isCurrentlyFav) {
                // 取消收藏
                favoriteManager.removeFavorite(currentStudentId, job.getJobId());
                scaleUp.setOnFinished(ev -> {
                    favButton.setText("☆");
                    favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #cccccc; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
                });
            } else {
                // 添加收藏
                favoriteManager.addFavorite(currentStudentId, job.getJobId());
                scaleUp.setOnFinished(ev -> {
                    favButton.setText("★");
                    favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffd700; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
                });
            }
            anim.play();
        });

        boolean manuallyClosed = job.isActive();
        boolean expired = isDeadlineExpired(job);

        if (manuallyClosed) {
            Label badge = new Label("Closed");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, favButton);
        } else if (expired) {
            Label badge = new Label("Expired");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, favButton);
        } else {
            titleBox.getChildren().addAll(titleLabel, favButton);
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
            boolean profileComplete = checkProfileComplete();

            if (hasApplied) {
                Button appliedButton = new Button("Applied");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #1890ff; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else if (!profileComplete) {
                Button incompleteButton = new Button("Complete Profile");
                incompleteButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                incompleteButton.setOnAction(e -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Incomplete Profile");
                    alert.setHeaderText("Please complete your profile before applying");
                    alert.setContentText("Please go to the \"Profile\" page and fill in:\n• Name\n• Major\n• Phone\n• Available Time\n• Skills");
                    alert.showAndWait();
                    switchToView("profile");
                });
                actionBox.getChildren().add(incompleteButton);
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

    private boolean checkProfileComplete() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(data.DataConfig.TA_DIR + currentStudentId + ".json");
            if (file.exists()) {
                TAApplication user = mapper.readValue(file, TAApplication.class);
                return user.getName() != null && !user.getName().trim().isEmpty()
                    && user.getMajor() != null && !user.getMajor().trim().isEmpty()
                    && user.getPhone() != null && !user.getPhone().trim().isEmpty()
                    && user.getAvailableTime() != null && !user.getAvailableTime().trim().isEmpty()
                    && user.getSkill() != null && !user.getSkill().trim().isEmpty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openApplicationForm(TAJob job) {
        if (!checkProfileComplete()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Application Rejected");
            alert.setHeaderText("Incomplete Profile");
            alert.setContentText("Please complete your profile before applying for a TA position.\n\nRequired fields: Name, Major, Phone, Available Time, Skills\n\nGo to the \"Profile\" page to complete your details.");
            alert.showAndWait();
            return;
        }

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

    private boolean isDeadlineExpired(TAJob job) {
        if (job.getDeadline() == null || job.getDeadline().trim().isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDate deadline = java.time.LocalDate.parse(job.getDeadline(),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return deadline.isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private VBox createFavoritesView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        Label titleLabel = new Label("My Favorite Positions");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox favoritesList = new VBox();
        favoritesList.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        favoritesList.setSpacing(0);

        List<Favorite> favorites = favoriteManager.getFavoritesByTA(currentStudentId);
        if (favorites.isEmpty()) {
            Label emptyLabel = new Label("No favorites yet. Click the star icon on positions to add them here.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999;");
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setPadding(new Insets(40, 20, 40, 20));
            favoritesList.getChildren().add(emptyLabel);
        } else {
            data.JobDataManager jobDataManager = new data.JobDataManager();
            for (Favorite fav : favorites) {
                TAJob job = jobDataManager.getJobById(fav.getJobId());
                if (job != null) {
                    VBox favBox = createFavoritePositionBox(job);
                    favoritesList.getChildren().add(favBox);
                }
            }
        }

        content.getChildren().addAll(titleLabel, favoritesList);
        return content;
    }

    private VBox createFavoritePositionBox(TAJob job) {
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
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.Button unfavButton = new javafx.scene.control.Button("★");
        unfavButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffd700; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
        unfavButton.setOnAction(e -> {
            // 缩放动画
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), unfavButton);
            scaleUp.setToX(1.5);
            scaleUp.setToY(1.5);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), unfavButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            
            SequentialTransition anim = new SequentialTransition(scaleUp, scaleDown);
            
            scaleUp.setOnFinished(ev -> {
                favoriteManager.removeFavorite(currentStudentId, job.getJobId());
                switchToView("favorites");
            });
            
            anim.play();
        });

        boolean manuallyClosed = job.isActive();
        boolean expired = isDeadlineExpired(job);

        if (manuallyClosed) {
            Label badge = new Label("Closed");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, unfavButton);
        } else if (expired) {
            Label badge = new Label("Expired");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, unfavButton);
        } else {
            titleBox.getChildren().addAll(titleLabel, unfavButton);
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
            boolean profileComplete = checkProfileComplete();

            if (hasApplied) {
                Button appliedButton = new Button("Applied");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #1890ff; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else if (!profileComplete) {
                Button incompleteButton = new Button("Complete Profile");
                incompleteButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                incompleteButton.setOnAction(e -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Incomplete Profile");
                    alert.setHeaderText("Please complete your profile before applying");
                    alert.setContentText("Please go to the \"Profile\" page and fill in:\n• Name\n• Major\n• Phone\n• Available Time\n• Skills");
                    alert.showAndWait();
                    switchToView("profile");
                });
                actionBox.getChildren().add(incompleteButton);
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

    private void checkAndShowNotifications() {
        List<TAApplicationRecord> unnotified = recordManager.getUnnotifiedApplications(currentStudentId);
        if (unnotified.isEmpty()) {
            return;
        }

        // 如果有多个，显示第一个
        TAApplicationRecord app = unnotified.get(0);
        showNotificationDialog(app);
    }

    private void showNotificationDialog(TAApplicationRecord application) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Application Status Update");
        dialogStage.initModality(javafx.stage.Modality.NONE);
        dialogStage.setAlwaysOnTop(false);

        VBox dialogVBox = new VBox();
        dialogVBox.setPadding(new Insets(30));
        dialogVBox.setSpacing(20);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: #ffffff;");

        // 图标区域
        String status = application.getStatus();
        String iconText = "";
        String iconColor = "";
        String titleText = "";
        String statusText = "";

        if (TAApplicationRecord.STATUS_APPROVED.equals(status)) {
            iconText = "🎉";
            iconColor = "#10b981";
            titleText = "Congratulations!";
            statusText = "Your application has been APPROVED";
        } else if (TAApplicationRecord.STATUS_REJECTED.equals(status)) {
            iconText = "📝";
            iconColor = "#ef4444";
            titleText = "Application Status";
            statusText = "Your application has been REJECTED";
        }

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + iconColor + ";");

        VBox contentBox = new VBox();
        contentBox.setSpacing(12);
        contentBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #333333;");

        Label positionLabel = new Label("Position: " + application.getPositionName());
        positionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        Label courseLabel = new Label("Course: " + application.getCourseName());
        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        contentBox.getChildren().addAll(statusLabel, positionLabel, courseLabel);

        // 如果有评论，显示评论
        if (application.getReviewComment() != null && !application.getReviewComment().trim().isEmpty()) {
            VBox commentBox = new VBox();
            commentBox.setSpacing(8);
            commentBox.setPadding(new Insets(15));
            commentBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            commentBox.setAlignment(Pos.CENTER_LEFT);

            Label commentTitle = new Label("Review Comment:");
            commentTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333333;");

            Label commentText = new Label(application.getReviewComment());
            commentText.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-wrap-text: true;");
            commentText.setMaxWidth(350);

            commentBox.getChildren().addAll(commentTitle, commentText);
            contentBox.getChildren().add(commentBox);
        }

        // 关闭按钮
        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");
        closeButton.setOnAction(e -> {
            recordManager.markAsNotified(application.getApplicationId());
            dialogStage.close();

            // 如果还有其他未通知的，继续显示
            checkAndShowNotifications();
        });

        dialogVBox.getChildren().addAll(iconLabel, titleLabel, contentBox, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 450, 500);
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
