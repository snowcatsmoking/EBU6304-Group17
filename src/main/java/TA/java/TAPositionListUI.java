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
import javafx.scene.control.ScrollPane;
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
    private MatchingService matchingService;
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

    /** Called from LoginView after login 鈥?uses the shared Stage via AppNavigator. */
    public void navigateTo() {
        this.primaryStage = core.AppNavigator.getInstance().getStage();
        buildUI();
        core.AppNavigator.getInstance().navigateTo(createStyledScene(), "TA Application System");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        core.AppNavigator.getInstance().init(primaryStage);
        buildUI();
        core.AppNavigator.getInstance().navigateTo(createStyledScene(), "TA Application System");
    }

    private Scene createStyledScene() {
        Scene scene = new Scene(rootContainer);
        scene.getStylesheets().add(TAPositionListUI.class.getResource("/styles/ta-theme.css").toExternalForm());
        return scene;
    }

    private void buildUI() {
        data.DataConfig.initAllDirs();

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // 璁?BorderPane 濮嬬粓濉弧 StackPane锛岄槻姝㈠唴瀹圭煭鏃朵晶杈规爮鏀剁缉
        rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #f8fafc;");
        root.minWidthProperty().bind(rootContainer.widthProperty());
        root.minHeightProperty().bind(rootContainer.heightProperty());
        root.prefWidthProperty().bind(rootContainer.widthProperty());
        root.prefHeightProperty().bind(rootContainer.heightProperty());
        root.maxWidthProperty().bind(rootContainer.widthProperty());
        root.maxHeightProperty().bind(rootContainer.heightProperty());

        recordManager = new TAApplicationRecordManager();
        favoriteManager = new FavoriteManager();
        matchingService = new MatchingService();
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

        // 鍒濆鍖栭€氱煡鏈嶅姟
        notificationService = new NotificationService(recordManager, currentStudentId, primaryStage);

        // 妫€鏌ュ苟鏄剧ず鐢宠鐘舵€佸彉鏇撮€氱煡 - 寤惰繜鏄剧ず锛岃鎺у埗鍙板厛鏄剧ず
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
                favoritesView.setMatchingService(matchingService);
                favoritesView.setNavigationListener(v -> switchToView(v));
                favoritesView.setApplicationListener(new FavoritesView.ApplicationListener() {
                    @Override
                    public void onApplyForPosition(TAJob job) {
                        openApplicationForm(job);
                    }

                    @Override
                    public void onCompleteProfile() {
                        showIncompleteProfileDialog(() -> switchToView("profile"));
                    }
                });
                root.setCenter(favoritesView.getView());
                break;
            case "ai":
                ai.config.AIConfig aiConfig = new ai.config.AIConfig();
                ai.ui.AIChatView aiChatView = new ai.ui.AIChatView(aiConfig.getApiKey());
                aiChatView.setPrimaryStage(primaryStage);
                root.setCenter(aiChatView.createChatView());
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                root.setCenter(profileView.getView());
                break;
        }
    }

    private List<TAJob> filteredJobList;

    private ScrollPane createPositionListView() {
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
        positionListComponent.setMatchingService(matchingService);
        positionListComponent.setPositionActionListener(new PositionListComponent.PositionActionListener() {
            @Override
            public void onApply(TAJob job) {
                openApplicationForm(job);
            }

            @Override
            public void onCompleteProfile() {
                showIncompleteProfileDialog(() -> switchToView("profile"));
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

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
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
                    // 瑙ｆ瀽鐢ㄦ埛杈撳叆鐨勬棩鏈?
                    java.time.LocalDate inputDate = java.time.LocalDate.parse(availableTime,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    // 瑙ｆ瀽宀椾綅鐨勬埅姝㈡棩鏈?
                    java.time.LocalDate deadlineDate = java.time.LocalDate.parse(job.getDeadline(),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    // 鍙繚鐣欐埅姝㈡棩鏈熷湪杈撳叆鏃ユ湡鍙婁箣鍓嶇殑宀椾綅
                    if (deadlineDate.isAfter(inputDate)) {
                        match = false;
                    }
                } catch (Exception e) {
                    // 杈撳叆鏍煎紡涓嶆纭紝涓嶅尮閰?
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
                    // 杈撳叆涓嶆槸鏁板瓧锛屼笉鍖归厤
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
        // 褰撶瓫閫夋垨鍒嗛〉鍙樺寲鏃讹紝鍒锋柊鑱屼綅鍒楄〃
        VBox newPositionListUI = positionListComponent.createPositionList(filteredJobList, paginationComponent.getCurrentPage(), PAGE_SIZE);

        // 鏇挎崲褰撳墠鐨勮亴浣嶅垪琛?
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

    private void showIncompleteProfileDialog(Runnable onClose) {
        TAAlertDialog.showWarning(
            primaryStage,
            "Incomplete Profile",
            "Please complete your profile before applying",
            "Required fields:\n- Name\n- Major\n- Phone\n- Available Time\n- Skills\n\nOpen the Profile page to complete your details.",
            onClose
        );
    }

    private void openApplicationForm(TAJob job) {
        if (!checkProfileComplete()) {
            showIncompleteProfileDialog(null);
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
