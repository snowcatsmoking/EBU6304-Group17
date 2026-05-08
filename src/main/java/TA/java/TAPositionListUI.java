package TA.java;

import TA.java.utils.TAApplicationUtils;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
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

import data.JobDataManager;

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
                FavoritesView favoritesView = new FavoritesView(currentStudentId, favoriteManager, recordManager);
                favoritesView.setNavigationListener(v -> switchToView(v));
                favoritesView.setApplicationListener(new FavoritesView.ApplicationListener() {
                    @Override
                    public void onApplyForPosition(TAJob job) {
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
                root.setCenter(favoritesView.getView());
                break;
            case "ai":
                ai.ui.APIInputView apiInputView = new ai.ui.APIInputView(
                        (apiKey) -> {
                            Platform.runLater(() -> {
                                ai.ui.AIChatView aiChatView = new ai.ui.AIChatView(apiKey);
                                aiChatView.setPrimaryStage(primaryStage);
                                root.setCenter(aiChatView.createChatView());
                            });
                        }
                );
                root.setCenter(apiInputView.createView());
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
        return TAApplicationUtils.checkProfileComplete(currentStudentId);
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


    public static void main(String[] args) {
        launch(args);
    }
}
