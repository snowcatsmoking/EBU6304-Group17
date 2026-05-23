package MO;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import TA.java.SkillUtils;
import TA.java.SkillMatchResult;
import TA.java.SkillMatcher;
import TA.java.TAApplicationRecord;
import TA.java.TAApplicationRecordManager;
import TA.java.TAJob;
import data.DataConfig;
import data.JobDataManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MODashboard extends javafx.application.Application {

    private static final int POSITION_PAGE_SIZE = 5;
    private static final int APPLICANT_PAGE_SIZE = 5;
    private static final int APPLICATION_RECORD_PAGE_SIZE = 5;
    private static final String SORT_MATCH = "Match Score";
    private static final String SORT_DATE = "Application Date";
    private static final String SORT_STATUS = "Review Status";
    private static final String SORT_NAME = "Name / Student ID";
    private static final Path TA_UPLOAD_BASE_DIR = Path.of(DataConfig.UPLOAD_DIR);

    private static final String NAV_DEFAULT =
        "-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: transparent;";

    private static final String NAV_ACTIVE =
        "-fx-font-size: 14px; -fx-text-fill: #6366f1; -fx-font-weight: 600; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: transparent;";

    private static final double NAV_H = 40.0;

    private final String moStaffId;
    private BorderPane root;
    private BorderPane contentPane;
    private Node activeNavNode;
    private Region navIndicator;
    private TranslateTransition indicatorAnim;
    private int activeNavIndex = 0;
    private final JobDataManager jobManager = new JobDataManager();
    private final TAApplicationRecordManager recordManager = new TAApplicationRecordManager();
    private Stage stage;

    public MODashboard() {
        this.moStaffId = "MO0001";
    }

    public MODashboard(String moStaffId) {
        this.moStaffId = moStaffId == null || moStaffId.trim().isEmpty() ? "MO0001" : moStaffId;
    }

    /** Called from LoginView after login — uses the shared Stage via AppNavigator. */
    public void navigateTo() {
        this.stage = core.AppNavigator.getInstance().getStage();
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");
        root.setLeft(buildSidebar());
        contentPane = new BorderPane();
        contentPane.setStyle("-fx-background-color: #f8fafc;");
        contentPane.centerProperty().addListener((obs, oldCenter, newCenter) ->
            javafx.application.Platform.runLater(() -> core.UiText.localize(root)));
        contentPane.setTop(buildTopBar());
        contentPane.setCenter(buildDashboardView());
        root.setCenter(contentPane);
        core.AppNavigator.getInstance().navigateTo(
            new Scene(root, 1180, 720), "TA Recruitment System - Module Organiser Console");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        core.AppNavigator.getInstance().init(stage);
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");
        root.setLeft(buildSidebar());
        contentPane = new BorderPane();
        contentPane.setStyle("-fx-background-color: #f8fafc;");
        contentPane.centerProperty().addListener((obs, oldCenter, newCenter) ->
            javafx.application.Platform.runLater(() -> core.UiText.localize(root)));
        contentPane.setTop(buildTopBar());
        contentPane.setCenter(buildDashboardView());
        root.setCenter(contentPane);
        core.AppNavigator.getInstance().navigateTo(
            new Scene(root, 1180, 720), "TA Recruitment System - Module Organiser Console");
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(8, 16, 8, 16));
        topBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Label welcomeLabel = new Label(core.UiText.tr("Welcome back, "));
        welcomeLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1e293b;");

        Label nameLabel = new Label(moStaffId);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        Label roleLabel = new Label(core.UiText.tr("(MO)"));
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: #eef2ff; -fx-padding: 2 8 2 8; -fx-border-radius: 4; -fx-background-radius: 4;");

        HBox langBox = core.LanguageSwitcher.create(this::navigateTo);
        langBox.setPadding(Insets.EMPTY);

        topBar.getChildren().addAll(welcomeLabel, nameLabel, roleLabel, langBox);
        return topBar;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setMaxHeight(Double.MAX_VALUE);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setSpacing(0);

        Label titleLabel = new Label("MO System");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        titleLabel.setPadding(new Insets(0, 0, 20, 20));

        Label navDashboard    = buildNavItem("Dashboard");
        Label navPublish      = buildNavItem("Post Position");
        HBox  navMyPositions  = buildNavItemWithBadge("My Positions");
        Label navStatistics   = buildNavItem("Position Statistics");
        Label navReviews      = buildNavItem("Application Review");

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
        indicatorAnim.setInterpolator(Interpolator.EASE_BOTH);

        VBox navBox = new VBox();
        navBox.setAlignment(Pos.TOP_LEFT);
        navBox.getChildren().addAll(navDashboard, navPublish, navMyPositions, navStatistics, navReviews);

        StackPane navStack = new StackPane();
        navStack.setAlignment(Pos.TOP_LEFT);
        navStack.getChildren().addAll(navIndicator, navBox);

        // Wire click handlers
        navDashboard  .setOnMouseClicked(e -> { setActive(navDashboard,   0); showWithFade(buildDashboardView()); });
        navPublish    .setOnMouseClicked(e -> { setActive(navPublish,     1); showWithFade(buildPostPositionView()); });
        navMyPositions.setOnMouseClicked(e -> { setActive(navMyPositions, 2); showWithFade(buildMyPositionsView()); });
        navStatistics .setOnMouseClicked(e -> { setActive(navStatistics,  3); showWithFade(buildPositionStatisticsView()); });
        navReviews    .setOnMouseClicked(e -> { setActive(navReviews,     4); showWithFade(buildApplicantReviewView()); });

        // Initial active
        setActive(navDashboard, 0);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Log Out");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        String logoutOff = "-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-background-color: transparent;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;";
        String logoutOn = "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;";
        logoutButton.setStyle(logoutOff);
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle(logoutOn));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle(logoutOff));
        logoutButton.setOnAction(e -> {
            try {
                core.AppNavigator.getInstance().navigateTo(
                    new LoginScreen.LoginView().buildLoginScene(), "TA Recruitment System - Login");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox logoutBox = new VBox(logoutButton);
        logoutBox.setPadding(new Insets(0, 16, 16, 16));

        sidebar.getChildren().addAll(titleLabel, navStack, spacer, logoutBox);
        return sidebar;
    }

    private Label buildNavItem(String text) {
        Label item = new Label(core.UiText.tr(text));
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPrefHeight(NAV_H);
        item.setMinHeight(NAV_H);
        item.setMaxHeight(NAV_H);
        item.setStyle(NAV_DEFAULT);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private ScrollPane wrapScrollablePage(VBox page) {
        page.setFillWidth(true);
        page.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return scrollPane;
    }

    /** My Positions with a live pending-count badge */
    private HBox buildNavItemWithBadge(String text) {
        Label item = new Label(core.UiText.tr(text));
        item.setStyle(NAV_DEFAULT);
        item.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(item, Priority.ALWAYS);

        long pending = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count();

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setPrefHeight(NAV_H);
        box.setMinHeight(NAV_H);
        box.setMaxHeight(NAV_H);
        box.setStyle(NAV_DEFAULT);
        box.getChildren().add(item);

        if (pending > 0) {
            Label badge = new Label(String.valueOf(pending));
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
                " -fx-padding: 1 6 1 6; -fx-background-radius: 8;");
            badge.setPadding(new Insets(0, 16, 0, 0));
            box.getChildren().add(badge);
        }
        return box;
    }

    private void setActive(Label target, int index) {
        if (activeNavNode != null) activeNavNode.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavNode = target;
        animateIndicator(index);
    }

    private void setActive(HBox target, int index) {
        if (activeNavNode != null) activeNavNode.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavNode = target;
        animateIndicator(index);
    }

    private void animateIndicator(int index) {
        if (indicatorAnim == null) return;
        double toY = index * NAV_H;
        indicatorAnim.stop();
        indicatorAnim.setFromY(navIndicator.getTranslateY());
        indicatorAnim.setToY(toY);
        indicatorAnim.play();
        activeNavIndex = index;
    }

    private void showContent(Node content) {
        if (content == null) {
            return;
        }
        if (contentPane != null) {
            contentPane.setCenter(content);
        } else if (root != null) {
            root.setCenter(content);
        }
        if (root != null) {
            core.UiText.localize(root);
        }
    }

    private void showWithFade(Node content) {
        content.setOpacity(0);
        showContent(content);
        FadeTransition ft = new FadeTransition(Duration.millis(180), content);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private Node buildDashboardView() {
        VBox box = new VBox();
        box.setPadding(new Insets(24));
        box.setSpacing(24);

        HBox statRow = new HBox();
        statRow.setSpacing(16);

        List<TAJob> myJobs = jobManager.getJobsByMo(moStaffId);
        List<TAApplicationRecord> myApplications = recordManager.getApplicationsByMoStaffId(moStaffId);

        statRow.getChildren().addAll(
            buildStatCard(myJobs.size(), "My Positions"),
            buildStatCard((int) myJobs.stream().filter(this::isJobOpen).count(), "Open Positions"),
            buildStatCard((int) myApplications.stream().filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count(), "Pending Reviews"),
            buildStatCard((int) myApplications.stream().filter(r -> TAApplicationRecord.STATUS_APPROVED.equals(r.getStatus())).count(), "Approved")
        );

        box.getChildren().addAll(statRow, buildSectionCard("Quick Access", buildDashboardTips()));
        return box;
    }

    private Node buildSectionCard(String title, Node content) {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 18;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        Label label = new Label(core.UiText.tr(title));
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        section.getChildren().addAll(label, content);
        return section;
    }

    private Node buildAccessDeniedPage(String message) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(16);

        Label title = new Label("Access Denied");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label body = new Label(message);
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc2626;");

        Button backButton = new Button(core.UiText.tr("Back to My Positions"));
        backButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        backButton.setOnAction(e -> showContent(buildMyPositionsView()));

        page.getChildren().addAll(title, body, backButton);
        return wrapScrollablePage(page);
    }

    private Node buildDashboardTips() {
        VBox box = new VBox();
        box.setSpacing(12);
        Label tip1 = new Label("• Click \"Post Position\" to fill in position details and set the application deadline.");
        Label tip2 = new Label("• Published positions appear automatically in the public listing and close after the deadline.");
        Label tip3 = new Label("• In \"My Positions\", view position details and applicant profiles for each posting.");
        Label tip4 = new Label("• In \"Application Review\", approve or reject individual applications; status syncs to applicants instantly.");
        box.getChildren().addAll(tip1, tip2, tip3, tip4);
        return box;
    }

    private VBox buildStatCard(int value, String title) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 18; -fx-spacing: 8;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        card.setPrefWidth(240);

        Label num = new Label(String.valueOf(value));
        num.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");

        Label desc = new Label(core.UiText.tr(title));
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        card.getChildren().addAll(num, desc);
        return card;
    }

    private Node buildPostPositionView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label("Post New Position");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox form = new VBox();
        form.setSpacing(14);

        TextField positionNameField = createTextField("Position Name");
        TextField courseNameField = createTextField("Course Name");
        TextField courseCodeField = createTextField("Course Code");
        TextField recruitmentCountField = createTextField("Number of Openings");
        TextArea requirementsArea = new TextArea();
        requirementsArea.setPromptText("Enter requirements, e.g. skills, experience, working hours.");
        requirementsArea.setPrefRowCount(4);
        requirementsArea.setWrapText(true);
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-background-color: #ffffff;");
        SkillInputControl skillInput = createSkillInputControl(null);

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Application Deadline");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        Button submitButton = new Button("Publish Position");
        submitButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        submitButton.setOnAction(e -> {
            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();
            LocalDate deadline = deadlinePicker.getValue();
            String skillError = SkillUtils.validateSkills(skillInput.getRawSkills());

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText) || deadline == null) {
                showMessage(messageLabel, core.UiText.tr("Please fill in all required fields and select a deadline."), false);
                return;
            }
            if (isBlank(requirements)) {
                showMessage(messageLabel, core.UiText.tr("Please enter the job requirements."), false);
                return;
            }
            if (skillError != null) {
                showMessage(messageLabel, skillError, false);
                return;
            }
            int count;
            try {
                count = Integer.parseInt(recruitText.trim());
                if (count <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showMessage(messageLabel, core.UiText.tr("Number of openings must be a positive integer."), false);
                return;
            }

            if (!deadline.isAfter(LocalDate.now())) {
                showMessage(messageLabel, core.UiText.tr("Deadline must be at least tomorrow."), false);
                return;
            }

            TAJob job = new TAJob();
            job.setJobId(moStaffId + "-" + System.currentTimeMillis());
            job.setPositionName(positionName.trim());
            job.setCourseName(courseName.trim());
            job.setCourseCode(courseCode.trim());
            job.setRecruitmentCount(count);
            job.setRequirements(requirements == null ? "" : requirements.trim());
            job.setRequiredSkills(skillInput.getSkills());
            job.setDeadline(deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            job.setPublisher("MO:" + moStaffId);
            job.setMoStaffId(moStaffId);
            job.setActive(false);

            try {
                jobManager.saveJob(job);
                showMessage(messageLabel, core.UiText.tr("Position published and added to the public listing."), true);
                positionNameField.clear();
                courseNameField.clear();
                courseCodeField.clear();
                recruitmentCountField.clear();
                requirementsArea.clear();
                skillInput.clear();
                deadlinePicker.setValue(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, core.UiText.tr("Publishing failed, please try again."), false);
            }
        });

        HBox submitRow = new HBox(12);
        submitRow.setAlignment(Pos.CENTER_RIGHT);
        submitRow.getChildren().addAll(messageLabel, submitButton);

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("Requirements"), requirementsArea,
            new Label("Skill Requirements"), skillInput.getView(),
            new Label("Application Deadline"), deadlinePicker,
            submitRow
        );
        page.getChildren().addAll(title, form);
        return wrapScrollablePage(page);
    }

    private Node buildEditPositionView(TAJob job) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Button backButton = new Button(core.UiText.tr("← Back to My Positions"));
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> showContent(buildMyPositionsView()));

        Label title = new Label("Edit Published Position");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label tip = new Label("Only positions with no applicants and a valid deadline can be edited.");
        tip.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        if (!canEditJob(job)) {
            showMessage(messageLabel, core.UiText.tr(getEditBlockedReason(job)), false);
            page.getChildren().addAll(backButton, title, tip, messageLabel);
            return page;
        }

        VBox form = new VBox();
        form.setSpacing(14);

        TextField positionNameField = createTextField("Position Name");
        positionNameField.setText(job.getPositionName());

        TextField courseNameField = createTextField("Course Name");
        courseNameField.setText(job.getCourseName());

        TextField courseCodeField = createTextField("Course Code");
        courseCodeField.setText(job.getCourseCode());

        TextField recruitmentCountField = createTextField("Number of Openings");
        recruitmentCountField.setText(String.valueOf(job.getRecruitmentCount()));

        TextArea requirementsArea = new TextArea();
        requirementsArea.setPromptText("Enter requirements, e.g. skills, experience, working hours.");
        requirementsArea.setPrefRowCount(4);
        requirementsArea.setWrapText(true);
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-background-color: #ffffff;");
        requirementsArea.setText(job.getRequirements());
        SkillInputControl skillInput = createSkillInputControl(job.getRequiredSkills());

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Application Deadline");
        if (!isBlank(job.getDeadline())) {
            try {
                deadlinePicker.setValue(LocalDate.parse(job.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException ignored) {
            }
        }

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        saveButton.setOnAction(e -> {
            TAJob latestJob = jobManager.getJobById(job.getJobId());
            if (latestJob == null) {
                showMessage(messageLabel, core.UiText.tr("This position no longer exists."), false);
                return;
            }
            if (!canEditJob(latestJob)) {
                showMessage(messageLabel, core.UiText.tr(getEditBlockedReason(latestJob)), false);
                return;
            }

            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();
            LocalDate deadline = deadlinePicker.getValue();
            String skillError = SkillUtils.validateSkills(skillInput.getRawSkills());

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText) || deadline == null) {
                showMessage(messageLabel, core.UiText.tr("Please fill in all required fields and select a deadline."), false);
                return;
            }
            if (isBlank(requirements)) {
                showMessage(messageLabel, core.UiText.tr("Please enter the job requirements."), false);
                return;
            }
            if (skillError != null) {
                showMessage(messageLabel, skillError, false);
                return;
            }

            int count;
            try {
                count = Integer.parseInt(recruitText.trim());
                if (count <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showMessage(messageLabel, core.UiText.tr("Number of openings must be a positive integer."), false);
                return;
            }

            if (deadline.isBefore(LocalDate.now())) {
                showMessage(messageLabel, core.UiText.tr("Deadline cannot be earlier than today."), false);
                return;
            }

            latestJob.setPositionName(positionName.trim());
            latestJob.setCourseName(courseName.trim());
            latestJob.setCourseCode(courseCode.trim());
            latestJob.setRecruitmentCount(count);
            latestJob.setRequirements(requirements == null ? "" : requirements.trim());
            latestJob.setRequiredSkills(skillInput.getSkills());
            latestJob.setDeadline(deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            try {
                if (jobManager.saveJobForMo(latestJob, moStaffId)) {
                    showContent(buildMyPositionsView());
                } else {
                    showMessage(messageLabel, core.UiText.tr("You do not have permission to edit this position."), false);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, core.UiText.tr("Failed to save changes. Please try again."), false);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 10 20 10 20; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        cancelButton.setOnAction(e -> showContent(buildMyPositionsView()));

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("Requirements"), requirementsArea,
            new Label("Skill Requirements"), skillInput.getView(),
            new Label("Application Deadline"), deadlinePicker,
            buttonBox,
            messageLabel
        );

        page.getChildren().addAll(backButton, title, tip, form);
        return page;
    }

    private Node buildMyPositionsView() {
        return buildMyPositionsView(1);
    }

    private Node buildMyPositionsView(int initialPage) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label(core.UiText.tr("My Positions"));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        List<TAJob> jobs = jobManager.getJobsByMo(moStaffId);

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        list.setPadding(new Insets(16));

        HBox paginationBox = createPaginationBox();
        int[] currentPage = { initialPage };

        refreshMyPositionsList(list, paginationBox, jobs, currentPage);

        page.getChildren().addAll(title, list, paginationBox);
        return wrapScrollablePage(page);
    }

    private void refreshMyPositionsList(VBox list, HBox paginationBox, List<TAJob> jobs, int[] currentPage) {
        list.getChildren().clear();

        if (jobs.isEmpty()) {
            Label empty = new Label(core.UiText.tr("You have not published any positions yet. Click \"Post Position\" on the left to get started."));
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            list.getChildren().add(empty);
            refreshPaginationBox(paginationBox, 1, 1, 0, POSITION_PAGE_SIZE, null, null);
            return;
        }

        int totalPages = getTotalPages(jobs.size(), POSITION_PAGE_SIZE);
        currentPage[0] = clampPage(currentPage[0], totalPages);
        List<TAJob> pageJobs = getPageItems(jobs, currentPage[0], POSITION_PAGE_SIZE);

        for (TAJob job : pageJobs) {
            HBox row = new HBox();
            row.setSpacing(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 14 0 14 0; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

            VBox textBox = new VBox();
            textBox.setSpacing(6);

            HBox titleRow = new HBox();
            titleRow.setSpacing(10);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            Label rowTitle = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
            rowTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            Label statusBadge = buildStatusBadge(job);
            titleRow.getChildren().addAll(rowTitle, statusBadge);

            Label rowMeta = new Label(core.UiText.tr("Openings: ") + job.getRecruitmentCount()
                + "  " + core.UiText.tr("Deadline: ") + (isBlank(job.getDeadline()) ? core.UiText.tr("Not set") : job.getDeadline()));
            rowMeta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            Label skillsMeta = new Label(core.UiText.tr("Skill Requirements: ")
                + SkillUtils.toDisplayText(job.getRequiredSkills(), core.UiText.tr("No specific skill requirements")));
            skillsMeta.setWrapText(true);
            skillsMeta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            List<TAApplicationRecord> jobRecords = recordManager.getApplicationsByMoStaffId(moStaffId)
                .stream().filter(r -> r.getJobId().equals(job.getJobId())).collect(Collectors.toList());
            long pendingCount = jobRecords.stream()
                .filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count();

            HBox applicantRow = new HBox();
            applicantRow.setSpacing(8);
            applicantRow.setAlignment(Pos.CENTER_LEFT);
            Label applicants = new Label(core.UiText.tr("Applicants: ") + jobRecords.size());
            applicants.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            applicantRow.getChildren().add(applicants);
            if (pendingCount > 0) {
                Label pendingBadge = new Label(pendingCount + core.UiText.tr(" pending review"));
                pendingBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc2626;"
                    + " -fx-background-color: #fee2e2; -fx-border-color: #fecaca;"
                    + " -fx-border-width: 1; -fx-padding: 1 8 1 8; -fx-border-radius: 4; -fx-background-radius: 4;");
                applicantRow.getChildren().add(pendingBadge);
            }
            textBox.getChildren().addAll(titleRow, rowMeta, skillsMeta, applicantRow);

            HBox actionBox = new HBox();
            actionBox.setSpacing(8);
            actionBox.setAlignment(Pos.CENTER_LEFT);

            Button editButton = new Button(core.UiText.tr("Edit Position"));
            editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            editButton.setDisable(!canEditJob(job));
            editButton.setOnAction(e -> showContent(buildEditPositionView(job)));
            actionBox.getChildren().add(editButton);

            Button detailButton = new Button(core.UiText.tr("View Details"));
            detailButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            detailButton.setOnAction(e -> showContent(buildJobDetailView(job, 1)));
            actionBox.getChildren().add(detailButton);

            boolean expired = isJobExpired(job);
            if (expired) {
                Label expiredNote = new Label(core.UiText.tr("Deadline has passed"));
                expiredNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-padding: 8 0 8 0;");
                actionBox.getChildren().add(expiredNote);
            } else if (job.isActive()) {
                Button reopenButton = new Button(core.UiText.tr("Reopen Position"));
                reopenButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #16a34a; -fx-border-color: #86efac; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                    "-fx-border-radius: 8; -fx-background-radius: 8;");
                reopenButton.setOnAction(e -> {
                    TAJob latestJob = jobManager.getJobById(job.getJobId());
                    if (latestJob == null || !jobManager.canManageJob(latestJob.getJobId(), moStaffId)) {
                        return;
                    }
                    latestJob.setActive(false);
                    try {
                        jobManager.saveJobForMo(latestJob, moStaffId);
                        showContent(buildMyPositionsView(currentPage[0]));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                actionBox.getChildren().add(reopenButton);
            } else {
                Button closeButton = new Button(core.UiText.tr("Close Position"));
                closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #dc2626; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                    "-fx-border-radius: 8; -fx-background-radius: 8;");
                closeButton.setOnAction(e -> {
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirm.setTitle(core.UiText.tr("Close Position"));
                    confirm.setHeaderText(core.LanguageManager.getInstance().isEnglish()
                        ? "Close \"" + job.getPositionName() + "\"?"
                        : "\u5173\u95ed \"" + job.getPositionName() + "\"?");
                    confirm.setContentText(core.UiText.tr("Closing this position will remove it from the public listing.\nApplicants will no longer be able to apply.\nYou can reopen it at any time."));
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == javafx.scene.control.ButtonType.OK) {
                            try {
                                jobManager.deactivateJobForMo(job.getJobId(), moStaffId);
                                showContent(buildMyPositionsView(currentPage[0]));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });
                actionBox.getChildren().add(closeButton);
            }

            row.getChildren().addAll(textBox, actionBox);
            HBox.setHgrow(textBox, Priority.ALWAYS);
            list.getChildren().add(row);

            if (!canEditJob(job)) {
                Label editHint = new Label(core.UiText.tr("Edit locked: ") + core.UiText.tr(getEditBlockedReason(job)));
                editHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
                textBox.getChildren().add(editHint);
            }
        }

        refreshPaginationBox(
            paginationBox,
            currentPage[0],
            totalPages,
            jobs.size(),
            POSITION_PAGE_SIZE,
            () -> {
                currentPage[0]--;
                refreshMyPositionsList(list, paginationBox, jobs, currentPage);
            },
            () -> {
                currentPage[0]++;
                refreshMyPositionsList(list, paginationBox, jobs, currentPage);
            }
        );
    }

    private Node buildPositionStatisticsView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(12);

        VBox titleBox = new VBox();
        titleBox.setSpacing(6);
        Label title = new Label(core.UiText.tr("Position Statistics"));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label subtitle = new Label(core.UiText.tr("Track the status of your positions together with application and approval counts."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button(core.UiText.tr("Refresh"));
        refreshButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        refreshButton.setOnAction(e -> showContent(buildPositionStatisticsView()));

        headerRow.getChildren().addAll(titleBox, spacer, refreshButton);

        List<TAJob> jobs = jobManager.getJobsByMo(moStaffId);
        long openCount = jobs.stream().filter(job -> "Open".equals(getPositionStatisticsStatus(job))).count();
        long closedCount = jobs.stream().filter(job -> "Closed".equals(getPositionStatisticsStatus(job))).count();
        long expiredCount = jobs.stream().filter(job -> "Expired".equals(getPositionStatisticsStatus(job))).count();
        int totalApplications = jobs.stream().mapToInt(job -> countApplicationsForJob(job.getJobId())).sum();
        int totalApproved = jobs.stream().mapToInt(job -> countApprovedApplicationsForJob(job.getJobId())).sum();

        HBox topStatRow = new HBox();
        topStatRow.setSpacing(16);
        topStatRow.getChildren().addAll(
            buildStatCard((int) openCount, "Open Positions"),
            buildStatCard((int) closedCount, "Closed Positions"),
            buildStatCard((int) expiredCount, "Expired Positions")
        );

        HBox bottomStatRow = new HBox();
        bottomStatRow.setSpacing(16);
        bottomStatRow.getChildren().addAll(
            buildStatCard(totalApplications, "Total Applications"),
            buildStatCard(totalApproved, "Total Approved")
        );

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 16;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        if (jobs.isEmpty()) {
            Label empty = new Label(core.UiText.tr("You have not published any positions yet. Publish one to start tracking progress here."));
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            list.getChildren().add(empty);
        } else {
            for (TAJob job : jobs) {
                list.getChildren().add(buildPositionStatisticsItem(job));
            }
        }

        page.getChildren().addAll(headerRow, topStatRow, bottomStatRow, list);
        return wrapScrollablePage(page);
    }

    private VBox buildPositionStatisticsItem(TAJob job) {
        VBox item = new VBox();
        item.setSpacing(8);
        item.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 14;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label status = new Label(core.UiText.tr("Status: ") + core.UiText.tr(getPositionStatisticsStatus(job)));
        status.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        Label meta = new Label(
            core.UiText.tr("Course: ") + showFallback(job.getCourseName())
                + "  " + core.UiText.tr("Deadline: ") + (isBlank(job.getDeadline()) ? core.UiText.tr("Not set") : job.getDeadline())
                + "  " + core.UiText.tr("Openings: ") + job.getRecruitmentCount()
        );
        meta.setWrapText(true);
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        int applicationCount = countApplicationsForJob(job.getJobId());
        int approvedCount = countApprovedApplicationsForJob(job.getJobId());
        Label progress = new Label(core.UiText.tr("Applications: ") + applicationCount + "  " + core.UiText.tr("Approved: ") + approvedCount);
        progress.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label skills = new Label(core.UiText.tr("Skill Requirements: ")
            + SkillUtils.toDisplayText(job.getRequiredSkills(), core.UiText.tr("No specific skill requirements")));
        skills.setWrapText(true);
        skills.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        item.getChildren().addAll(title, status, meta, skills, progress);
        return item;
    }

    private Node buildJobDetailView(TAJob job) {
        return buildJobDetailView(job, 1);
    }

    private Node buildJobDetailView(TAJob job, int initialPage) {
        TAJob latestJob = jobManager.getJobById(job.getJobId());
        if (latestJob == null || !jobManager.canManageJob(latestJob.getJobId(), moStaffId)) {
            return buildAccessDeniedPage("You can only view positions published by your own MO account.");
        }
        final TAJob displayJob = latestJob;

        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Button backButton = new Button(core.UiText.tr("← Back to My Positions"));
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> showContent(buildMyPositionsView()));

        Label title = new Label(displayJob.getPositionName() + " (" + displayJob.getCourseCode() + ")");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label meta = new Label(core.UiText.tr("Openings: ") + displayJob.getRecruitmentCount() + "  " + core.UiText.tr("Deadline: ") + (isBlank(displayJob.getDeadline()) ? core.UiText.tr("Not set") : displayJob.getDeadline()) + "  " + core.UiText.tr("Status: ") + core.UiText.tr(getJobStatus(displayJob)));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label desc = new Label(core.UiText.tr("Description: ") + (isBlank(displayJob.getRequirements()) ? core.UiText.tr("None") : displayJob.getRequirements()));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        Label skills = new Label(core.UiText.tr("Skill Requirements: ")
            + SkillUtils.toDisplayText(displayJob.getRequiredSkills(), core.UiText.tr("No specific skill requirements")));
        skills.setWrapText(true);
        skills.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        HBox topActionBox = new HBox();
        topActionBox.setSpacing(10);

        Button editButton = new Button(core.UiText.tr("Edit Position"));
        editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        editButton.setDisable(!canEditJob(displayJob));
        editButton.setOnAction(e -> showContent(buildEditPositionView(displayJob)));

        Label editHint = new Label(canEditJob(displayJob)
            ? core.UiText.tr("This position can still be edited.")
            : core.UiText.tr("Edit locked: ") + core.UiText.tr(getEditBlockedReason(displayJob)));
        editHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        topActionBox.getChildren().addAll(editButton, editHint);

        VBox recordBox = new VBox();
        recordBox.setSpacing(12);
        recordBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 16;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(r -> displayJob.getJobId().equals(r.getJobId()))
            .collect(Collectors.toList());

        Label applicantTitle = new Label(core.UiText.tr("Applicants for this Position (showing current MO's positions only)"));
        applicantTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox recordList = new VBox();
        recordList.setSpacing(12);

        HBox paginationBox = createPaginationBox();
        int[] currentPage = { initialPage };

        recordBox.getChildren().addAll(applicantTitle, recordList, paginationBox);
        refreshJobDetailRecords(recordList, paginationBox, records, displayJob, currentPage);

        page.getChildren().addAll(backButton, title, meta, desc, skills, topActionBox, recordBox);
        return page;
    }

    private void refreshJobDetailRecords(
        VBox recordList,
        HBox paginationBox,
        List<TAApplicationRecord> records,
        TAJob job,
        int[] currentPage
    ) {
        recordList.getChildren().clear();

        if (records.isEmpty()) {
            Label empty = new Label(core.UiText.tr("No applications for this position yet. Check back later or view all applications in Application Review."));
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            recordList.getChildren().add(empty);
            refreshPaginationBox(paginationBox, 1, 1, 0, APPLICATION_RECORD_PAGE_SIZE, null, null);
            return;
        }

        int totalPages = getTotalPages(records.size(), APPLICATION_RECORD_PAGE_SIZE);
        currentPage[0] = clampPage(currentPage[0], totalPages);
        List<TAApplicationRecord> pageRecords = getPageItems(records, currentPage[0], APPLICATION_RECORD_PAGE_SIZE);

        for (TAApplicationRecord record : pageRecords) {
            HBox row = new HBox();
            row.setSpacing(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 12;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

            VBox textBox = new VBox();
            textBox.setSpacing(5);
            Label name = new Label(record.getStudentName() + " (" + record.getTaStudentId() + ")");
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            Label applicantMeta = new Label(core.UiText.tr("Major: ") + record.getMajor() + "  ·  " + core.UiText.tr("Applied: ") + fmtDate(record.getApplicationDate()));
            applicantMeta.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            Label matchMeta = new Label(formatMatchSummary(SkillMatcher.calculate(job, record)));
            matchMeta.setWrapText(true);
            matchMeta.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            textBox.getChildren().addAll(name, applicantMeta, matchMeta);

            Label statusLabel = new Label(core.UiText.tr(formatStatus(record.getStatus())));
            statusLabel.setStyle(statusBadgeStyle(record.getStatus()));
            statusLabel.setPrefWidth(100);

            Button viewBtn = new Button(core.UiText.tr("View Application"));
            viewBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-padding: 7 14 7 14; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            viewBtn.setOnAction(e -> showContent(
                buildApplicationDetailView(record, () -> showContent(buildJobDetailView(job, currentPage[0])))));

            HBox.setHgrow(textBox, Priority.ALWAYS);
            row.getChildren().addAll(textBox, statusLabel, viewBtn);
            recordList.getChildren().add(row);
        }

        refreshPaginationBox(
            paginationBox,
            currentPage[0],
            totalPages,
            records.size(),
            APPLICATION_RECORD_PAGE_SIZE,
            () -> {
                currentPage[0]--;
                refreshJobDetailRecords(recordList, paginationBox, records, job, currentPage);
            },
            () -> {
                currentPage[0]++;
                refreshJobDetailRecords(recordList, paginationBox, records, job, currentPage);
            }
        );
    }

    private Node buildApplicantReviewView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label(core.UiText.tr("Application Review"));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label(core.UiText.tr("Review all applications submitted for the positions published by the current module organiser."));
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        HBox filterBox = new HBox();
        filterBox.setSpacing(12);

        TextField majorFilterField = createTextField("Filter by major");
        TextField availableTimeFilterField = createTextField("Filter by available time");
        TextField skillsFilterField = createTextField("Filter by skills or keywords");
        HBox.setHgrow(majorFilterField, Priority.ALWAYS);
        HBox.setHgrow(availableTimeFilterField, Priority.ALWAYS);
        HBox.setHgrow(skillsFilterField, Priority.ALWAYS);

        ComboBox<String> sortBox = new ComboBox<>();
        sortBox.getItems().addAll(
            core.UiText.tr(SORT_MATCH),
            core.UiText.tr(SORT_DATE),
            core.UiText.tr(SORT_STATUS),
            core.UiText.tr(SORT_NAME)
        );
        sortBox.setValue(core.UiText.tr(SORT_MATCH));
        sortBox.setPrefWidth(180);
        sortBox.setStyle("-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        Button clearButton = new Button(core.UiText.tr("Clear Filters"));
        clearButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        filterBox.getChildren().addAll(majorFilterField, availableTimeFilterField, skillsFilterField, sortBox, clearButton);

        HBox batchActionBox = new HBox();
        batchActionBox.setSpacing(12);

        Button selectAllButton = new Button(core.UiText.tr("Select Pending On Page"));
        selectAllButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        Button clearSelectionButton = new Button(core.UiText.tr("Clear Selection"));
        clearSelectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        Button batchApproveButton = new Button(core.UiText.tr("Batch Approve"));
        batchApproveButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        Button batchRejectButton = new Button(core.UiText.tr("Batch Reject"));
        batchRejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #dc2626; -fx-border-color: #fecaca; -fx-padding: 10 18 10 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        batchActionBox.getChildren().addAll(selectAllButton, clearSelectionButton, batchApproveButton, batchRejectButton);

        Label summary = new Label();
        summary.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label selectionLabel = new Label();
        selectionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 13px;");

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 16;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        HBox paginationBox = createPaginationBox();
        Set<String> selectedApplicationIds = new HashSet<>();
        int[] currentPage = { 1 };
        Runnable refreshList = () -> refreshApplicantReviewList(
            list,
            paginationBox,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            currentPage,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField,
            sortBox
        );

        majorFilterField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage[0] = 1;
            refreshList.run();
        });
        availableTimeFilterField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage[0] = 1;
            refreshList.run();
        });
        skillsFilterField.textProperty().addListener((obs, oldValue, newValue) -> {
            currentPage[0] = 1;
            refreshList.run();
        });
        sortBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            currentPage[0] = 1;
            refreshList.run();
        });
        clearButton.setOnAction(e -> {
            majorFilterField.clear();
            availableTimeFilterField.clear();
            skillsFilterField.clear();
            sortBox.setValue(core.UiText.tr(SORT_MATCH));
            currentPage[0] = 1;
            refreshList.run();
        });
        selectAllButton.setOnAction(e -> {
            List<TAApplicationRecord> filteredPendingRecords = getPageItems(
                getFilteredApplicantRecords(
                    majorFilterField.getText(),
                    availableTimeFilterField.getText(),
                    skillsFilterField.getText(),
                    sortBox.getValue()
                ),
                currentPage[0],
                APPLICANT_PAGE_SIZE
            ).stream()
                .filter(this::isPendingApplication)
                .collect(Collectors.toList());

            if (filteredPendingRecords.isEmpty()) {
                showMessage(feedbackLabel, core.UiText.tr("No pending applications match the current filters."), false);
                refreshList.run();
                return;
            }

            for (TAApplicationRecord record : filteredPendingRecords) {
                selectedApplicationIds.add(record.getApplicationId());
            }
            showMessage(feedbackLabel, core.UiText.tr("Selected: ") + filteredPendingRecords.size() + core.UiText.tr(" pending applications."), true);
            refreshList.run();
        });
        clearSelectionButton.setOnAction(e -> {
            selectedApplicationIds.clear();
            showMessage(feedbackLabel, core.UiText.tr("Selection cleared."), true);
            refreshList.run();
        });
        batchApproveButton.setOnAction(e -> handleBatchReviewAction(
            true,
            selectedApplicationIds,
            feedbackLabel,
            list,
            paginationBox,
            summary,
            selectionLabel,
            currentPage,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField,
            sortBox
        ));
        batchRejectButton.setOnAction(e -> handleBatchReviewAction(
            false,
            selectedApplicationIds,
            feedbackLabel,
            list,
            paginationBox,
            summary,
            selectionLabel,
            currentPage,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField,
            sortBox
        ));

        refreshList.run();

        page.getChildren().addAll(title, subtitle, filterBox, batchActionBox, summary, selectionLabel, feedbackLabel, list, paginationBox);
        return wrapScrollablePage(page);
    }

    private void refreshApplicantReviewList(
        VBox list,
        HBox paginationBox,
        Label summary,
        Label selectionLabel,
        Label feedbackLabel,
        Set<String> selectedApplicationIds,
        int[] currentPage,
        TextField majorFilterField,
        TextField availableTimeFilterField,
        TextField skillsFilterField,
        ComboBox<String> sortBox
    ) {
        list.getChildren().clear();

        Set<String> pendingApplicationIds = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(this::isPendingApplication)
            .map(TAApplicationRecord::getApplicationId)
            .collect(Collectors.toSet());
        selectedApplicationIds.retainAll(pendingApplicationIds);

        List<TAApplicationRecord> filteredRecords = getFilteredApplicantRecords(
            majorFilterField.getText(),
            availableTimeFilterField.getText(),
            skillsFilterField.getText(),
            sortBox.getValue()
        );
        long pendingCount = filteredRecords.stream()
            .filter(record -> TAApplicationRecord.STATUS_PENDING.equals(record.getStatus()))
            .count();

        summary.setText(core.UiText.tr("Matching applications: ") + filteredRecords.size() + core.UiText.tr("  Pending review: ") + pendingCount);
        updateSelectionLabel(selectionLabel, selectedApplicationIds);

        if (filteredRecords.isEmpty()) {
            Label empty = new Label(core.UiText.tr("No applications match the current filters."));
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            list.getChildren().add(empty);
            refreshPaginationBox(paginationBox, 1, 1, 0, APPLICANT_PAGE_SIZE, null, null);
            return;
        }

        int totalPages = getTotalPages(filteredRecords.size(), APPLICANT_PAGE_SIZE);
        currentPage[0] = clampPage(currentPage[0], totalPages);
        List<TAApplicationRecord> pageRecords = getPageItems(filteredRecords, currentPage[0], APPLICANT_PAGE_SIZE);

        Runnable refreshAction = () -> refreshApplicantReviewList(
            list,
            paginationBox,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            currentPage,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField,
            sortBox
        );

        for (TAApplicationRecord record : pageRecords) {
            list.getChildren().add(buildApplicantReviewItem(record, selectedApplicationIds, selectionLabel, refreshAction));
        }

        refreshPaginationBox(
            paginationBox,
            currentPage[0],
            totalPages,
            filteredRecords.size(),
            APPLICANT_PAGE_SIZE,
            () -> {
                currentPage[0]--;
                refreshApplicantReviewList(
                    list,
                    paginationBox,
                    summary,
                    selectionLabel,
                    feedbackLabel,
                    selectedApplicationIds,
                    currentPage,
                    majorFilterField,
                    availableTimeFilterField,
                    skillsFilterField,
                    sortBox
                );
            },
            () -> {
                currentPage[0]++;
                refreshApplicantReviewList(
                    list,
                    paginationBox,
                    summary,
                    selectionLabel,
                    feedbackLabel,
                    selectedApplicationIds,
                    currentPage,
                    majorFilterField,
                    availableTimeFilterField,
                    skillsFilterField,
                    sortBox
                );
            }
        );
    }

    private VBox buildApplicantReviewItem(
        TAApplicationRecord record,
        Set<String> selectedApplicationIds,
        Label selectionLabel,
        Runnable refreshAction
    ) {
        VBox item = new VBox();
        item.setSpacing(8);
        item.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 14;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        HBox summaryRow = new HBox();
        summaryRow.setSpacing(10);
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox selectBox = new CheckBox();
        selectBox.setStyle("-fx-cursor: hand;");
        selectBox.setSelected(selectedApplicationIds.contains(record.getApplicationId()));
        selectBox.setDisable(!isPendingApplication(record));
        selectBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                selectedApplicationIds.add(record.getApplicationId());
            } else {
                selectedApplicationIds.remove(record.getApplicationId());
            }
            updateSelectionLabel(selectionLabel, selectedApplicationIds);
        });

        Label summary = new Label(record.getPositionName() + " - " + record.getStudentName() + " (" + record.getTaStudentId() + ")");
        summary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        summaryRow.getChildren().addAll(selectBox, summary);

        Label profile = new Label(
            core.UiText.tr("Major: ") + showFallback(record.getMajor())
                + "  " + core.UiText.tr("Available Time: ") + showFallback(record.getAvailableTime())
                + "  " + core.UiText.tr("Skills: ") + showFallback(record.getSkills())
        );
        profile.setWrapText(true);
        profile.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label meta = new Label(core.UiText.tr("Applied: ") + fmtDate(record.getApplicationDate()) + "  " + core.UiText.tr("Status: ") + core.UiText.tr(formatStatus(record.getStatus())));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label matchLabel = new Label(formatMatchSummary(getSkillMatchResult(record)));
        matchLabel.setWrapText(true);
        matchLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-background-color: #eef2ff; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-padding: 6 8 6 8;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;");

        Button openButton = new Button(core.UiText.tr("Open Review"));
        openButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        openButton.setOnAction(e -> showContent(
            buildApplicationDetailView(record, () -> showContent(buildApplicantReviewView()))));

        HBox actionBox = new HBox();
        actionBox.setSpacing(8);
        actionBox.getChildren().add(openButton);
        if (isPendingApplication(record)) {
            Button approveButton = new Button(core.UiText.tr("Approve"));
            approveButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            approveButton.setOnAction(e -> {
                recordManager.approveApplicationForMo(record.getApplicationId(), moStaffId);
                refreshAction.run();
            });

            Button rejectButton = new Button(core.UiText.tr("Reject"));
            rejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #dc2626; -fx-border-color: #fecaca; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            rejectButton.setOnAction(e -> {
                recordManager.rejectApplicationForMo(record.getApplicationId(), moStaffId);
                refreshAction.run();
            });
            actionBox.getChildren().addAll(approveButton, rejectButton);
        }

        item.getChildren().addAll(summaryRow, profile, meta, matchLabel, actionBox);
        return item;
    }

    private Node buildApplicationDetailView(TAApplicationRecord record, Runnable onBack) {
        TAApplicationRecord latestRecord = recordManager.getApplicationByIdForMo(record.getApplicationId(), moStaffId);
        if (latestRecord == null) {
            return buildAccessDeniedPage("You can only review applications for positions published by your own MO account.");
        }
        record = latestRecord;

        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(20);

        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-padding: 6 12 6 0; -fx-cursor: hand;");
        backButton.setOnAction(e -> onBack.run());

        // 标题区
        HBox titleRow = new HBox();
        titleRow.setSpacing(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(record.getPositionName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label statusBadge = new Label(formatStatus(record.getStatus()));
        statusBadge.setStyle(statusBadgeStyle(record.getStatus()));
        titleRow.getChildren().addAll(title, statusBadge);

        Label courseInfo = new Label(core.UiText.tr("Course: ") + record.getCourseName() + "  ·  " + core.UiText.tr("Applied: ") + fmtDate(record.getApplicationDate()));
        courseInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // 申请人信息卡片
        VBox profileCard = buildDetailCard("Applicant Profile", buildProfileGrid(record));
        VBox attachmentCard = buildDetailCard("Resume Attachments", buildAttachmentList(record));

        VBox matchCard = buildDetailCard("Skill Match", buildMatchDetail(record));
        VBox keywordCard = buildKeywordReviewCard(record, onBack);

        // 审核操作区
        VBox reviewCard = buildReviewCard(record, onBack);

        page.getChildren().addAll(backButton, titleRow, courseInfo, profileCard, attachmentCard, matchCard, keywordCard, reviewCard);
        return wrapScrollablePage(page);
    }

    private VBox buildDetailCard(String cardTitle, Node content) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 20;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        Label label = new Label(core.UiText.tr(cardTitle));
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        card.getChildren().addAll(label, content);
        return card;
    }

    private Node buildAttachmentList(TAApplicationRecord record) {
        VBox box = new VBox();
        box.setSpacing(10);

        List<Path> attachments = getUploadedResumeAttachments(record);
        if (attachments.isEmpty()) {
            Label empty = new Label(core.UiText.tr("No resume attachments uploaded"));
            empty.setWrapText(true);
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 4 0 4 0;");
            box.getChildren().add(empty);
            return box;
        }

        Label feedback = new Label();
        feedback.setStyle("-fx-font-size: 13px;");

        for (Path attachment : attachments) {
            HBox row = new HBox();
            row.setSpacing(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

            Label name = new Label(attachment.getFileName().toString());
            name.setWrapText(true);
            name.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
            HBox.setHgrow(name, Priority.ALWAYS);

            Button openButton = new Button(core.UiText.tr("Open Attachment"));
            openButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
                "-fx-background-radius: 8; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
            openButton.setOnAction(e -> openAttachment(attachment, feedback));

            row.getChildren().addAll(name, openButton);
            box.getChildren().add(row);
        }

        box.getChildren().add(feedback);
        return box;
    }

    private List<Path> getUploadedResumeAttachments(TAApplicationRecord record) {
        if (record == null || isBlank(record.getTaStudentId())) {
            return List.of();
        }

        Path baseDir = TA_UPLOAD_BASE_DIR.toAbsolutePath().normalize();
        Path studentDir = baseDir.resolve(record.getTaStudentId().trim()).normalize();
        if (!studentDir.startsWith(baseDir) || !Files.isDirectory(studentDir)) {
            return List.of();
        }

        try (var paths = Files.list(studentDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(this::isSupportedResumeAttachment)
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private boolean isSupportedResumeAttachment(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".pdf") || fileName.endsWith(".doc") || fileName.endsWith(".docx");
    }

    private void openAttachment(Path attachment, Label feedback) {
        try {
            Path normalized = attachment.toAbsolutePath().normalize();
            if (!Files.isRegularFile(normalized)) {
                showMessage(feedback, core.UiText.tr("Attachment file is no longer available."), false);
                return;
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                showMessage(feedback, core.UiText.tr("This device cannot open attachments automatically."), false);
                return;
            }
            Desktop.getDesktop().open(normalized.toFile());
            showMessage(feedback, core.UiText.tr("Attachment opened."), true);
        } catch (IOException | RuntimeException e) {
            showMessage(feedback, core.UiText.tr("Unable to open attachment."), false);
        }
    }

    private Node buildProfileGrid(TAApplicationRecord record) {
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(24);
        grid.setVgap(10);

        String[][] fields = {
            {"Name",           record.getStudentName()},
            {"Student ID",     record.getTaStudentId()},
            {"Major",          record.getMajor()},
            {"Phone",          record.getPhone()},
            {"Email",          record.getEmail()},
            {"Available Time", record.getAvailableTime()},
            {"Skills",         record.getSkills()},
            {"Keywords",       SkillUtils.toDisplayText(record.getEffectiveKeywords(), "No keywords extracted")},
            {"Resume Text",    showFallback(record.getResumeText())},
        };

        for (int i = 0; i < fields.length; i++) {
            Label key = new Label(fields[i][0]);
            key.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
            key.setPrefWidth(120);

            Label val = new Label(fields[i][1] == null || fields[i][1].isEmpty() ? "—" : fields[i][1]);
            val.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
            val.setWrapText(true);

            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }
        return grid;
    }

    private Node buildMatchDetail(TAApplicationRecord record) {
        SkillMatchResult result = getSkillMatchResult(record);
        VBox box = new VBox();
        box.setSpacing(8);

        Label score = new Label(result.hasRequirements()
            ? core.UiText.tr("Match Score: ") + result.getScore() + "%"
            : core.UiText.tr("Match Score: ") + core.UiText.tr("Not available because this position has no skill requirements."));
        score.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label explanation = new Label(result.getExplanation());
        explanation.setWrapText(true);
        explanation.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label matched = new Label(core.UiText.tr("Matched Skills: ")
            + SkillUtils.toDisplayText(result.getMatchedSkills(), core.UiText.tr("None")));
        matched.setWrapText(true);
        matched.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a;");

        Label missing = new Label(core.UiText.tr("Missing Skills: ")
            + SkillUtils.toDisplayText(result.getMissingSkills(), core.UiText.tr("None")));
        missing.setWrapText(true);
        missing.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");

        Label evidence = new Label(core.UiText.tr("Applicant Keywords Used: ")
            + SkillUtils.toDisplayText(result.getCandidateKeywords(), core.UiText.tr("No skills or keywords available")));
        evidence.setWrapText(true);
        evidence.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        box.getChildren().addAll(score, explanation, matched, missing, evidence);
        return box;
    }

    private VBox buildKeywordReviewCard(TAApplicationRecord record, Runnable onBack) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 20;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        Label title = new Label("Resume Keywords");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label hint = new Label(record.isKeywordsVerified()
            ? "These keywords have been manually verified. Re-extraction will update the automatic keywords but keep this verified list."
            : "Automatic keywords are shown below. You can confirm, remove incorrect keywords, or add missing ones.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label autoKeywords = new Label(core.UiText.tr("Automatically Extracted: ")
            + SkillUtils.toDisplayText(record.getResumeKeywords(), core.UiText.tr("No keywords extracted")));
        autoKeywords.setWrapText(true);
        autoKeywords.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        TextArea keywordArea = new TextArea(SkillUtils.toEditableText(record.getEffectiveKeywords()));
        keywordArea.setPromptText("Comma-separated keywords, max 20 labels");
        keywordArea.setPrefRowCount(3);
        keywordArea.setWrapText(true);
        keywordArea.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

        Label feedback = new Label();
        feedback.setStyle("-fx-font-size: 13px;");

        Button saveButton = new Button("Save Verified Keywords");
        saveButton.setStyle("-fx-background-color: #6366f1; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        saveButton.setOnAction(e -> {
            List<String> keywords = SkillUtils.parseKeywords(keywordArea.getText());
            if (recordManager.saveVerifiedKeywordsForMo(record.getApplicationId(), moStaffId, keywords)) {
                showMessage(feedback, core.UiText.tr("Verified keywords saved."), true);
                showContent(buildApplicationDetailView(record, onBack));
            } else {
                showMessage(feedback, core.UiText.tr("You do not have permission to update these keywords."), false);
            }
        });

        Button reextractButton = new Button("Re-extract Automatically");
        reextractButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        reextractButton.setOnAction(e -> {
            if (recordManager.reextractKeywordsForMo(record.getApplicationId(), moStaffId)) {
                showMessage(feedback, core.UiText.tr("Automatic keywords refreshed. Verified keywords were not overwritten."), true);
                showContent(buildApplicationDetailView(record, onBack));
            } else {
                showMessage(feedback, core.UiText.tr("You do not have permission to refresh these keywords."), false);
            }
        });

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.getChildren().addAll(saveButton, reextractButton);

        card.getChildren().addAll(title, hint, autoKeywords, new Label("Verified Keywords"), keywordArea, buttons, feedback);
        return card;
    }

    private VBox buildReviewCard(TAApplicationRecord record, Runnable onBack) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 20;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        Label cardTitle = new Label("Review Decision");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        card.getChildren().add(cardTitle);

        if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            Label hint = new Label("This application is awaiting your decision.");
            hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

            javafx.scene.control.TextArea commentField = new javafx.scene.control.TextArea();
            commentField.setPromptText("Optional: add a review comment for your records...");
            commentField.setPrefRowCount(3);
            commentField.setWrapText(true);
            commentField.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

            HBox buttons = new HBox();
            buttons.setSpacing(12);

            Button approveBtn = new Button("✓  Approve");
            approveBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            approveBtn.setOnAction(e -> {
                recordManager.approveApplicationForMo(record.getApplicationId(), moStaffId, commentField.getText().trim());
                onBack.run();
            });

            Button rejectBtn = new Button("✗  Reject");
            rejectBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #dc2626; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            rejectBtn.setOnAction(e -> {
                recordManager.rejectApplicationForMo(record.getApplicationId(), moStaffId, commentField.getText().trim());
                onBack.run();
            });

            buttons.getChildren().addAll(approveBtn, rejectBtn);
            card.getChildren().addAll(hint, new Label("Review Comment (optional):"), commentField, buttons);
        } else {
            Label decided = new Label(core.UiText.tr("Decision: ") + core.UiText.tr(formatStatus(record.getStatus())));
            decided.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");
            card.getChildren().add(decided);

            if (record.getReviewComment() != null && !record.getReviewComment().isEmpty()) {
                Label comment = new Label(core.UiText.tr("Comment: ") + record.getReviewComment());
                comment.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
                comment.setWrapText(true);
                card.getChildren().add(comment);
            }

            // Only allow undo for APPROVED / REJECTED (not WITHDRAWN — that's the TA's action)
            if (TAApplicationRecord.STATUS_APPROVED.equals(record.getStatus()) ||
                    TAApplicationRecord.STATUS_REJECTED.equals(record.getStatus())) {
                Label undoHint = new Label("You can undo this decision to reopen the application for review.");
                undoHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

                Button undoBtn = new Button("↩  Undo Decision");
                undoBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b;" +
                    " -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
                    " -fx-font-size: 13px; -fx-padding: 7 16 7 16; -fx-cursor: hand;" +
                    " -fx-border-radius: 8; -fx-background-radius: 8;");
                undoBtn.setOnAction(e -> {
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Undo Decision");
                    confirm.setHeaderText("Reset this application back to \"Under Review\"?");
                    confirm.setContentText("The applicant's status will return to Pending and you can re-review it.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == javafx.scene.control.ButtonType.OK) {
                            recordManager.resetToPendingForMo(record.getApplicationId(), moStaffId);
                            onBack.run();
                        }
                    });
                });
                card.getChildren().addAll(undoHint, undoBtn);
            }
        }
        return card;
    }

    private String statusBadgeStyle(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "-fx-font-size: 12px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
            case TAApplicationRecord.STATUS_APPROVED:
                return "-fx-font-size: 12px; -fx-text-fill: #16a34a; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
            case TAApplicationRecord.STATUS_REJECTED:
                return "-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-background-color: #fee2e2; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
            default:
                return "-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
        }
    }

    private String fmtDate(java.util.Date date) {
        if (date == null) return "—";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private String formatStatus(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "Under Review";
            case TAApplicationRecord.STATUS_APPROVED:
                return "Approved";
            case TAApplicationRecord.STATUS_REJECTED:
                return "Rejected";
            case TAApplicationRecord.STATUS_WITHDRAWN:
                return "Withdrawn";
            default:
                return status;
        }
    }

    private Label buildStatusBadge(TAJob job) {
        String text;
        String style;
        if (job.isActive()) {
            text = "Closed";
            style = "-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        } else if (isJobExpired(job)) {
            text = "Expired";
            style = "-fx-font-size: 11px; -fx-text-fill: #d97706; -fx-background-color: #fffbeb; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        } else {
            text = "Open";
            style = "-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        }
        Label badge = new Label(core.UiText.tr(text));
        badge.setStyle(style);
        return badge;
    }

    private String getJobStatus(TAJob job) {
        if (job.isActive()) {
            return "Closed";
        }
        if (isJobExpired(job)) {
            return "Expired";
        }
        return "Open";
    }

    private String getPositionStatisticsStatus(TAJob job) {
        if (isJobExpired(job)) {
            return "Expired";
        }
        if (job.isActive()) {
            return "Closed";
        }
        return "Open";
    }

    private int countApplicationsForJob(String jobId) {
        return (int) recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(record -> jobId.equals(record.getJobId()))
            .count();
    }

    private int countApprovedApplicationsForJob(String jobId) {
        return (int) recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(record -> jobId.equals(record.getJobId()))
            .filter(record -> TAApplicationRecord.STATUS_APPROVED.equals(record.getStatus()))
            .count();
    }

    private List<TAApplicationRecord> getFilteredApplicantRecords(
        String majorFilter,
        String availableTimeFilter,
        String skillsFilter,
        String sortMode
    ) {
        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(record -> matchesApplicantFilters(record, majorFilter, availableTimeFilter, skillsFilter))
            .collect(Collectors.toList());
        records.sort(getApplicantComparator(normalizeSortMode(sortMode)));
        return records;
    }

    private String normalizeSortMode(String sortMode) {
        if (SORT_DATE.equals(sortMode) || core.UiText.tr(SORT_DATE).equals(sortMode)) {
            return SORT_DATE;
        }
        if (SORT_STATUS.equals(sortMode) || core.UiText.tr(SORT_STATUS).equals(sortMode)) {
            return SORT_STATUS;
        }
        if (SORT_NAME.equals(sortMode) || core.UiText.tr(SORT_NAME).equals(sortMode)) {
            return SORT_NAME;
        }
        return SORT_MATCH;
    }

    private boolean matchesApplicantFilters(
        TAApplicationRecord record,
        String majorFilter,
        String availableTimeFilter,
        String skillsFilter
    ) {
        return matchesKeywordFilter(record.getMajor(), majorFilter)
            && matchesKeywordFilter(record.getAvailableTime(), availableTimeFilter)
            && matchesKeywordFilter(
                record.getSkills() + " " + SkillUtils.toDisplayText(record.getEffectiveKeywords(), ""),
                skillsFilter
            );
    }

    private boolean matchesKeywordFilter(String value, String filterText) {
        if (isBlank(filterText)) {
            return true;
        }

        String normalizedValue = value == null ? "" : value.trim().toLowerCase();
        String[] keywords = filterText.trim().toLowerCase().split("[,，\\s]+");
        for (String keyword : keywords) {
            if (!keyword.isEmpty() && !normalizedValue.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private Comparator<TAApplicationRecord> getApplicantComparator(String sortMode) {
        if (SORT_DATE.equals(sortMode)) {
            return Comparator.comparing(TAApplicationRecord::getApplicationDate,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();
        }
        if (SORT_STATUS.equals(sortMode)) {
            return Comparator.comparingInt((TAApplicationRecord record) -> statusRank(record.getStatus()))
                .thenComparing(TAApplicationRecord::getApplicationDate,
                    Comparator.nullsLast(Comparator.reverseOrder()));
        }
        if (SORT_NAME.equals(sortMode)) {
            return Comparator.comparing((TAApplicationRecord record) -> safeLower(record.getStudentName()))
                .thenComparing(record -> safeLower(record.getTaStudentId()));
        }
        return Comparator.comparingInt((TAApplicationRecord record) -> getSkillMatchResult(record).getScore())
            .reversed()
            .thenComparing(TAApplicationRecord::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int statusRank(String status) {
        if (TAApplicationRecord.STATUS_PENDING.equals(status)) {
            return 0;
        }
        if (TAApplicationRecord.STATUS_APPROVED.equals(status)) {
            return 1;
        }
        if (TAApplicationRecord.STATUS_REJECTED.equals(status)) {
            return 2;
        }
        if (TAApplicationRecord.STATUS_WITHDRAWN.equals(status)) {
            return 3;
        }
        return 4;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private SkillMatchResult getSkillMatchResult(TAApplicationRecord record) {
        TAJob job = jobManager.getJobById(record.getJobId());
        return SkillMatcher.calculate(job, record);
    }

    private String formatMatchSummary(SkillMatchResult result) {
        if (result == null || !result.hasRequirements()) {
            return core.UiText.tr("Skill Match: No position skill requirements set.");
        }
        return core.UiText.tr("Skill Match: ") + result.getScore() + "%  " + core.UiText.tr("Matched: ")
            + SkillUtils.toDisplayText(result.getMatchedSkills(), core.UiText.tr("None"))
            + "  " + core.UiText.tr("Missing: ")
            + SkillUtils.toDisplayText(result.getMissingSkills(), core.UiText.tr("None"));
    }

    private void handleBatchReviewAction(
        boolean approve,
        Set<String> selectedApplicationIds,
        Label feedbackLabel,
        VBox list,
        HBox paginationBox,
        Label summary,
        Label selectionLabel,
        int[] currentPage,
        TextField majorFilterField,
        TextField availableTimeFilterField,
        TextField skillsFilterField,
        ComboBox<String> sortBox
    ) {
        if (selectedApplicationIds.isEmpty()) {
            showMessage(feedbackLabel, core.UiText.tr("Select at least one pending application first."), false);
            return;
        }

        int selectedCount = selectedApplicationIds.size();
        int updatedCount = 0;
        for (String applicationId : new HashSet<>(selectedApplicationIds)) {
            boolean updated = approve
                ? recordManager.approveApplicationForMo(applicationId, moStaffId)
                : recordManager.rejectApplicationForMo(applicationId, moStaffId);
            if (updated) {
                updatedCount++;
                selectedApplicationIds.remove(applicationId);
            }
        }

        if (updatedCount == 0) {
            showMessage(feedbackLabel, core.UiText.tr("None of the selected applications could be updated."), false);
        } else if (updatedCount == selectedCount) {
            showMessage(feedbackLabel, core.UiText.tr(approve ? "Approved: " : "Rejected: ") + updatedCount + core.UiText.tr(" applications."), true);
        } else {
            showMessage(
                feedbackLabel,
                core.UiText.tr("Updated: ") + updatedCount + core.UiText.tr(" applications. ")
                    + (selectedCount - updatedCount) + core.UiText.tr(" were skipped because their status changed."),
                true
            );
        }

        refreshApplicantReviewList(
            list,
            paginationBox,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            currentPage,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField,
            sortBox
        );
    }

    private HBox createPaginationBox() {
        HBox box = new HBox();
        box.setSpacing(12);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void refreshPaginationBox(
        HBox paginationBox,
        int currentPage,
        int totalPages,
        int totalItems,
        int pageSize,
        Runnable previousAction,
        Runnable nextAction
    ) {
        paginationBox.getChildren().clear();

        Button previousButton = new Button(core.UiText.tr("Previous"));
        previousButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        previousButton.setDisable(totalItems == 0 || currentPage <= 1);
        if (previousAction != null) {
            previousButton.setOnAction(e -> previousAction.run());
        }

        Button nextButton = new Button(core.UiText.tr("Next"));
        nextButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-padding: 8 16 8 16; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        nextButton.setDisable(totalItems == 0 || currentPage >= totalPages);
        if (nextAction != null) {
            nextButton.setOnAction(e -> nextAction.run());
        }

        Label pageLabel = new Label(core.UiText.tr("Page ") + currentPage + " / " + totalPages);
        pageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        int startIndex = totalItems == 0 ? 0 : (currentPage - 1) * pageSize + 1;
        int endIndex = totalItems == 0 ? 0 : Math.min(currentPage * pageSize, totalItems);
        Label countLabel = new Label(core.UiText.tr("Showing ") + startIndex + "-" + endIndex + core.UiText.tr(" of ") + totalItems);
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        paginationBox.getChildren().addAll(previousButton, nextButton, pageLabel, countLabel);
    }

    private int getTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) {
            return 1;
        }
        return (totalItems + pageSize - 1) / pageSize;
    }

    private int clampPage(int page, int totalPages) {
        return Math.max(1, Math.min(page, totalPages));
    }

    private <T> List<T> getPageItems(List<T> items, int page, int pageSize) {
        if (items.isEmpty()) {
            return items;
        }

        int safePage = clampPage(page, getTotalPages(items.size(), pageSize));
        int fromIndex = (safePage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    private boolean canEditJob(TAJob job) {
        return job != null
            && jobManager.canManageJob(job.getJobId(), moStaffId)
            && !isJobExpired(job)
            && countApplicationsForJob(job.getJobId()) == 0;
    }

    private String getEditBlockedReason(TAJob job) {
        if (job == null || !jobManager.canManageJob(job.getJobId(), moStaffId)) {
            return "you do not have permission to edit this position";
        }
        if (isJobExpired(job)) {
            return "deadline has already passed";
        }
        if (countApplicationsForJob(job.getJobId()) > 0) {
            return "the position already has applicants";
        }
        return "this position is not eligible for editing";
    }

    private boolean isJobOpen(TAJob job) {
        return !job.isActive() && !isJobExpired(job);
    }

    private boolean isJobExpired(TAJob job) {
        if (isBlank(job.getDeadline())) {
            return false;
        }
        try {
            LocalDate deadline = LocalDate.parse(job.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return deadline.isBefore(LocalDate.now());
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(core.UiText.tr(prompt));
        field.setStyle("-fx-font-size: 14px; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-background-color: #ffffff; -fx-padding: 10 12 10 12;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");
        return field;
    }

    private SkillInputControl createSkillInputControl(List<String> initialSkills) {
        return new SkillInputControl(initialSkills);
    }

    private void showMessage(Label label, String message, boolean success) {
        label.setText(message);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#16a34a" : "#ef4444") + ";");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String showFallback(String text) {
        return isBlank(text) ? core.UiText.tr("Not provided") : text.trim();
    }

    private boolean isPendingApplication(TAApplicationRecord record) {
        return TAApplicationRecord.STATUS_PENDING.equals(record.getStatus());
    }

    private void updateSelectionLabel(Label selectionLabel, Set<String> selectedApplicationIds) {
        selectionLabel.setText(core.UiText.tr("Selected pending applications: ") + selectedApplicationIds.size());
    }

    private static class SkillInputControl {
        private final VBox view = new VBox();
        private final List<CheckBox> presetBoxes = new ArrayList<>();
        private final TextField customField = new TextField();

        SkillInputControl(List<String> initialSkills) {
            List<String> normalizedInitial = SkillUtils.normalizeSkills(initialSkills);
            List<String> defaultSkills = SkillUtils.getDefaultSkills();

            view.setSpacing(10);
            view.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 12;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

            Label hint = new Label("Select preset skills or enter custom skills separated by commas. Max 10 skills, 20 characters each.");
            hint.setWrapText(true);
            hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

            FlowPane presetPane = new FlowPane();
            presetPane.setHgap(10);
            presetPane.setVgap(8);
            for (String skill : defaultSkills) {
                CheckBox box = new CheckBox(skill);
                box.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
                box.setSelected(normalizedInitial.stream().anyMatch(existing -> SkillUtils.looselyMatches(skill, existing)));
                presetBoxes.add(box);
                presetPane.getChildren().add(box);
            }

            customField.setPromptText("Custom skills, e.g. Data Mining, Mentoring");
            customField.setStyle("-fx-font-size: 13px; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-background-color: #ffffff; -fx-padding: 8 10 8 10;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
            customField.setText(toCustomSkillText(normalizedInitial, defaultSkills));

            view.getChildren().addAll(hint, presetPane, customField);
        }

        Node getView() {
            return view;
        }

        List<String> getRawSkills() {
            List<String> values = new ArrayList<>();
            for (CheckBox box : presetBoxes) {
                if (box.isSelected()) {
                    values.add(box.getText());
                }
            }
            values.addAll(splitCustomSkills(customField.getText()));
            return values;
        }

        List<String> getSkills() {
            return SkillUtils.normalizeSkills(getRawSkills());
        }

        void clear() {
            for (CheckBox box : presetBoxes) {
                box.setSelected(false);
            }
            customField.clear();
        }

        private String toCustomSkillText(List<String> initialSkills, List<String> defaultSkills) {
            List<String> customSkills = initialSkills.stream()
                .filter(skill -> defaultSkills.stream().noneMatch(defaultSkill -> SkillUtils.looselyMatches(defaultSkill, skill)))
                .collect(Collectors.toList());
            return String.join(", ", customSkills);
        }

        private List<String> splitCustomSkills(String rawText) {
            if (rawText == null || rawText.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.stream(rawText.split("[,，;；\\n\\r\\t]+"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
