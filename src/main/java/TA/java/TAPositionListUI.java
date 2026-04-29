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
    private DashboardView dashboardView;
    private List<TAJob> jobList;
    private TAApplicationRecordManager recordManager;
    private FavoriteManager favoriteManager;
    private NotificationService notificationService;
    private String currentStudentId = "2024999";
    private static final int PAGE_SIZE = 3;
    private VBox positionListUI;
    private PaginationComponent paginationComponent;
    private FilterComponent filterComponent;
    private PositionListComponent positionListComponent;
    private SidebarComponent sidebarComponent;

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

        sidebarComponent = new SidebarComponent();
        sidebarComponent.setNavigable(viewName -> switchToView(viewName));
        sidebarComponent.setLogoutListener(() -> {
            try {
                LoginScreen.LoginView loginView = new LoginScreen.LoginView();
                core.AppNavigator.getInstance().navigateTo(loginView.buildLoginScene(), "TA Recruitment System - Login");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        VBox sidebar = sidebarComponent.createSidebar();
        root.setLeft(sidebar);

        dashboardView = new DashboardView();
        dashboardView.setCurrentStudentId(currentStudentId);
        dashboardView.setNavigationListener(() -> switchToView("profile"));
        root.setCenter(dashboardView.getView());
        sidebarComponent.setActiveNavItem("dashboard");

        overlay = new VBox();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setVisible(false);

        rootContainer.getChildren().addAll(root, overlay);

        // 初始化通知服务
        notificationService = new NotificationService(recordManager, currentStudentId, primaryStage);

        // 检查并显示申请状态变更通知 - 延迟显示，让控制台先显示
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(notificationService::checkAndShowNotifications);
        });
    }

    private void initJobList() {
        jobList = new JobDataManager().getAllJobs();
    }



    private void switchToView(String viewName) {
        sidebarComponent.setActiveNavItem(viewName);
        switch (viewName) {
            case "dashboard":
                root.setCenter(dashboardView.getView());
                break;
            case "positions":
                root.setCenter(createPositionListView());
                break;
            case "applications":
                root.setCenter(new MyApplicationsView(currentStudentId).getView());
                break;
            case "favorites":
                root.setCenter(createFavoritesView());
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                root.setCenter(profileView.getView());
                break;
        }
    }

    private List<TAJob> filteredJobList;

    private VBox createPositionListView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        filterComponent = new FilterComponent();
        filterComponent.setFilterListener(new FilterComponent.FilterListener() {
            @Override
            public void onFilter(String courseName, String availableTime, String recruitmentCount) {
                applyFilters(courseName, availableTime, recruitmentCount);
            }

            @Override
            public void onReset() {
                resetFilters();
            }
        });

        HBox filterUI = filterComponent.createComponent();

        filteredJobList = new ArrayList<>(jobList);

        positionListComponent = new PositionListComponent(favoriteManager, recordManager, currentStudentId);
        positionListComponent.setPositionActionListener(new PositionListComponent.PositionActionListener() {
            @Override
            public void onApply(TAJob job) {
                openApplicationForm(job);
            }

            @Override
            public void onCompleteProfile() {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Incomplete Profile");
                alert.setHeaderText("Please complete your profile before applying");
                alert.setContentText("Please go to the \"Profile\" page and fill in:\n• Name\n• Major\n• Phone\n• Available Time\n• Skills");
                alert.showAndWait();
                switchToView("profile");
            }
        });

        paginationComponent = new PaginationComponent(PAGE_SIZE);
        paginationComponent.setPageChangeListener(newPage -> {
            refreshPositionList();
        });

        positionListUI = positionListComponent.createPositionList(filteredJobList, 1, PAGE_SIZE);
        
        HBox paginationUI = paginationComponent.createComponent();
        paginationComponent.updateData(filteredJobList.size());

        content.getChildren().addAll(filterUI, positionListUI, paginationUI);

        return content;
    }

    private void applyFilters(String courseName, String availableTime, String recruitmentCount) {

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

        paginationComponent.setCurrentPage(1);
        paginationComponent.updateData(filteredJobList.size());
        refreshPositionList();
    }

    private void resetFilters() {
        filteredJobList = new ArrayList<>(jobList);
        paginationComponent.setCurrentPage(1);
        paginationComponent.updateData(filteredJobList.size());
        refreshPositionList();
    }

    private void refreshPositionList() {
        // 当筛选或分页变化时，刷新职位列表
        VBox newPositionListUI = positionListComponent.createPositionList(filteredJobList, paginationComponent.getCurrentPage(), PAGE_SIZE);
        
        // 替换当前的职位列表
        if (positionListUI != null) {
            javafx.scene.Parent parent = positionListUI.getParent();
            if (parent instanceof VBox) {
                VBox parentVBox = (VBox) parent;
                int index = parentVBox.getChildren().indexOf(positionListUI);
                if (index != -1) {
                    parentVBox.getChildren().set(index, newPositionListUI);
                    positionListUI = newPositionListUI;
                }
            }
        }
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

    public static void main(String[] args) {
        launch(args);
    }
}
