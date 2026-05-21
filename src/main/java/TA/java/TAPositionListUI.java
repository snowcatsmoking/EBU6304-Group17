package TA.java;

import TA.java.TAApplication;
import TA.java.utils.TAApplicationUtils;
import TA.java.view.DashboardView;
import TA.java.view.FavoritesView;
import TA.java.view.ProfileView;
import TA.java.view.MyApplicationsView;
import TA.java.view.TAApplicationFormView;
import TA.java.service.FavoriteManager;
import TA.java.service.MatchingService;
import TA.java.service.NotificationService;
import TA.java.component.PaginationComponent;
import TA.java.component.FilterComponent;
import TA.java.component.PositionListComponent;
import TA.java.component.SidebarComponent;
import TA.java.component.TAAlertDialog;
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
    private String currentViewName = "dashboard";
    private BorderPane contentPane;

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

        contentPane = new BorderPane();
        contentPane.setStyle("-fx-background-color: #f8fafc;");
        contentPane.centerProperty().addListener((obs, oldCenter, newCenter) ->
            Platform.runLater(() -> core.UiText.localize(root)));
        contentPane.setTop(buildTopBar());
        root.setCenter(contentPane);

        dashboardView = new DashboardView();
        dashboardView.setCurrentStudentId(currentStudentId);
        dashboardView.setNavigationListener(() -> switchToView("profile"));
        contentPane.setCenter(dashboardView.getView());
        sidebarComponent.setActiveNavItem("dashboard");

        overlay = new VBox();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setVisible(false);

        rootContainer.getChildren().addAll(root, overlay);

        if (!"dashboard".equals(currentViewName)) {
            switchToView(currentViewName);
        }

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
        jobList = new JobDataManager().getActiveJobs();
    }



    private void switchToView(String viewName) {
        currentViewName = viewName;
        sidebarComponent.setActiveNavItem(viewName);
        switch (viewName) {
            case "dashboard":
                contentPane.setCenter(dashboardView.getView());
                break;
            case "positions":
                contentPane.setCenter(createPositionListView());
                break;
            case "applications":
                contentPane.setCenter(new MyApplicationsView(currentStudentId).getView());
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
                contentPane.setCenter(favoritesView.getView());
                break;
            case "ai":
                ai.config.AIConfig aiConfig = new ai.config.AIConfig();
                ai.ui.AIChatView aiChatView = new ai.ui.AIChatView(aiConfig.getApiKey());
                aiChatView.setPrimaryStage(primaryStage);
                contentPane.setCenter(aiChatView.createChatView());
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                contentPane.setCenter(profileView.getView());
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
                    core.UiText.localize(newPositionListUI);
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

        JobDataManager jobDataManager = new JobDataManager();
        TAJob latestJob = jobDataManager.getJobById(job.getJobId());
        if (!jobDataManager.isJobOpen(latestJob)) {
            TAAlertDialog.showWarning(
                primaryStage,
                "Position Unavailable",
                "This position is no longer open",
                "The deadline has passed or the module organiser has closed this position. Existing applications are not affected.",
                null
            );
            initJobList();
            switchToView("positions");
            return;
        }

        overlay.setVisible(true);
        TAApplicationFormView formView = new TAApplicationFormView(latestJob, currentStudentId);
        formView.setApplicationListener(() -> {
            overlay.setVisible(false);
            switchToView("positions");
        });
        formView.setDialogCloseListener(() -> {
            overlay.setVisible(false);
        });
        formView.showDialog(primaryStage);
    }


    private HBox buildTopBar() {
        String userName = currentStudentId;
        try {
            java.io.File file = new java.io.File(data.DataConfig.TA_DIR + currentStudentId + ".json");
            if (file.exists()) {
                TAApplication app = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(file, TAApplication.class);
                if (app.getName() != null && !app.getName().isBlank()) {
                    userName = app.getName();
                }
            }
        } catch (Exception ignored) {}

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(8, 16, 8, 16));
        topBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label welcomeLabel = new Label(core.UiText.tr("Welcome back, "));
        welcomeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");

        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label roleLabel = new Label(core.UiText.tr("(TA)"));
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: #eef2ff; -fx-padding: 2 8 2 8; -fx-border-radius: 4; -fx-background-radius: 4;");

        HBox langBox = core.LanguageSwitcher.create(() -> {
            buildUI();
            core.AppNavigator.getInstance().navigateTo(createStyledScene(), "TA Application System");
        });
        langBox.setPadding(javafx.geometry.Insets.EMPTY);

        topBar.getChildren().addAll(welcomeLabel, nameLabel, roleLabel, langBox);
        return topBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
