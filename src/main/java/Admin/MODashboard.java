package Admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ZiqianCao.java.TAApplicationRecord;
import ZiqianCao.java.TAApplicationRecordManager;
import ZiqianCao.java.TAJob;
import data.JobDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MODashboard extends javafx.application.Application {

    private static final String NAV_DEFAULT =
        "-fx-font-size: 14px; -fx-text-fill: #555555; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 0 3;";

    private static final String NAV_ACTIVE =
        "-fx-font-size: 14px; -fx-text-fill: #000000; -fx-font-weight: 600; -fx-cursor: hand;" +
        "-fx-padding: 10 16 10 16; -fx-alignment: CENTER_LEFT;" +
        "-fx-background-color: #f0f0f0; -fx-border-color: #000000; -fx-border-width: 0 0 0 3;";

    private final String moStaffId;
    private BorderPane root;
    private Node activeNavNode;
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
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardView());
        core.AppNavigator.getInstance().navigateTo(
            new Scene(root, 1180, 720), "TA Recruitment System - Module Organiser Console");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        core.AppNavigator.getInstance().init(stage);
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardView());
        core.AppNavigator.getInstance().navigateTo(
            new Scene(root, 1180, 720), "TA Recruitment System - Module Organiser Console");
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(24, 0, 24, 0));
        sidebar.setSpacing(0);

        Label titleLabel = new Label("Module Organiser Console");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        titleLabel.setWrapText(true);
        titleLabel.setPadding(new Insets(0, 0, 24, 20));

        Label navDashboard = buildNavItem("Dashboard");
        Label navPublish = buildNavItem("Post Position");
        HBox navMyPositions = buildNavItemWithBadge("My Positions");
        Label navStatistics = buildNavItem("Position Statistics");
        Label navReviews = buildNavItem("Application Review");

        setActive(navDashboard);
        navDashboard.setOnMouseClicked(e -> { setActive(navDashboard); root.setCenter(buildDashboardView()); });
        navPublish.setOnMouseClicked(e -> { setActive(navPublish); root.setCenter(buildPostPositionView()); });
        navMyPositions.setOnMouseClicked(e -> { setActive(navMyPositions); root.setCenter(buildMyPositionsView()); });
        navStatistics.setOnMouseClicked(e -> { setActive(navStatistics); root.setCenter(buildPositionStatisticsView()); });
        navReviews.setOnMouseClicked(e -> { setActive(navReviews); root.setCenter(buildApplicantReviewView()); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logoutLabel = new Label("Log Out");
        logoutLabel.setMaxWidth(Double.MAX_VALUE);
        logoutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand; -fx-padding: 10 16 24 16;");
        logoutLabel.setOnMouseClicked(e -> {
            try {
                core.AppNavigator.getInstance().navigateTo(
                    new LoginScreen.LoginView().buildLoginScene(), "TA Recruitment System - Login");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sidebar.getChildren().addAll(titleLabel, navDashboard, navPublish, navMyPositions, navStatistics, navReviews, spacer, logoutLabel);
        return sidebar;
    }

    private Label buildNavItem(String text) {
        Label item = new Label(text);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle(NAV_DEFAULT);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    /** My Positions with a live pending-count badge */
    private HBox buildNavItemWithBadge(String text) {
        Label item = new Label(text);
        item.setStyle(NAV_DEFAULT);
        item.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(item, Priority.ALWAYS);

        long pending = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count();

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(NAV_DEFAULT);
        box.getChildren().add(item);

        if (pending > 0) {
            Label badge = new Label(String.valueOf(pending));
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff; -fx-background-color: #cc0000;" +
                " -fx-padding: 1 6 1 6; -fx-background-radius: 8;");
            badge.setPadding(new Insets(0, 16, 0, 0));
            box.getChildren().add(badge);
        }
        return box;
    }

    private void setActive(Label target) {
        if (activeNavNode != null) activeNavNode.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavNode = target;
    }

    private void setActive(HBox target) {
        if (activeNavNode != null) activeNavNode.setStyle(NAV_DEFAULT);
        target.setStyle(NAV_ACTIVE);
        activeNavNode = target;
    }

    private Node buildDashboardView() {
        VBox box = new VBox();
        box.setPadding(new Insets(24));
        box.setSpacing(24);

        Label header = new Label("Welcome, " + moStaffId);
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222222;");

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

        box.getChildren().addAll(header, statRow, buildSectionCard("Quick Access", buildDashboardTips()));
        return box;
    }

    private Node buildSectionCard(String title, Node content) {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 18;");

        Label label = new Label(title);
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        section.getChildren().addAll(label, content);
        return section;
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
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 18; -fx-spacing: 8;");
        card.setPrefWidth(240);

        Label num = new Label(String.valueOf(value));
        num.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        Label desc = new Label(title);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        card.getChildren().addAll(num, desc);
        return card;
    }

    private Node buildPostPositionView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label("Post New Position");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

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
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Application Deadline");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        Button submitButton = new Button("Publish Position");
        submitButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        submitButton.setOnAction(e -> {
            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();
            LocalDate deadline = deadlinePicker.getValue();

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText) || deadline == null) {
                showMessage(messageLabel, "Please fill in all required fields and select a deadline.", false);
                return;
            }
            int count;
            try {
                count = Integer.parseInt(recruitText.trim());
                if (count <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Number of openings must be a positive integer.", false);
                return;
            }

            if (!deadline.isAfter(LocalDate.now())) {
                showMessage(messageLabel, "Deadline must be at least tomorrow.", false);
                return;
            }

            TAJob job = new TAJob();
            job.setJobId(moStaffId + "-" + System.currentTimeMillis());
            job.setPositionName(positionName.trim());
            job.setCourseName(courseName.trim());
            job.setCourseCode(courseCode.trim());
            job.setRecruitmentCount(count);
            job.setRequirements(requirements == null ? "" : requirements.trim());
            job.setDeadline(deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            job.setPublisher("MO:" + moStaffId);
            job.setMoStaffId(moStaffId);
            job.setActive(false);

            try {
                jobManager.saveJob(job);
                showMessage(messageLabel, "Position published and added to the public listing.", true);
                positionNameField.clear();
                courseNameField.clear();
                courseCodeField.clear();
                recruitmentCountField.clear();
                requirementsArea.clear();
                deadlinePicker.setValue(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, "Publishing failed, please try again.", false);
            }
        });

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("Requirements"), requirementsArea,
            new Label("Application Deadline"), deadlinePicker,
            submitButton,
            messageLabel
        );
        page.getChildren().addAll(title, form);
        return page;
    }

    private Node buildEditPositionView(TAJob job) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Button backButton = new Button("← Back to My Positions");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> root.setCenter(buildMyPositionsView()));

        Label title = new Label("Edit Published Position");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label tip = new Label("Only positions with no applicants and a valid deadline can be edited.");
        tip.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        if (!canEditJob(job)) {
            showMessage(messageLabel, getEditBlockedReason(job), false);
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
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");
        requirementsArea.setText(job.getRequirements());

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Application Deadline");
        if (!isBlank(job.getDeadline())) {
            try {
                deadlinePicker.setValue(LocalDate.parse(job.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (DateTimeParseException ignored) {
            }
        }

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        saveButton.setOnAction(e -> {
            TAJob latestJob = jobManager.getJobById(job.getJobId());
            if (latestJob == null) {
                showMessage(messageLabel, "This position no longer exists.", false);
                return;
            }
            if (!canEditJob(latestJob)) {
                showMessage(messageLabel, getEditBlockedReason(latestJob), false);
                return;
            }

            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();
            LocalDate deadline = deadlinePicker.getValue();

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText) || deadline == null) {
                showMessage(messageLabel, "Please fill in all required fields and select a deadline.", false);
                return;
            }

            int count;
            try {
                count = Integer.parseInt(recruitText.trim());
                if (count <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "Number of openings must be a positive integer.", false);
                return;
            }

            if (deadline.isBefore(LocalDate.now())) {
                showMessage(messageLabel, "Deadline cannot be earlier than today.", false);
                return;
            }

            latestJob.setPositionName(positionName.trim());
            latestJob.setCourseName(courseName.trim());
            latestJob.setCourseCode(courseCode.trim());
            latestJob.setRecruitmentCount(count);
            latestJob.setRequirements(requirements == null ? "" : requirements.trim());
            latestJob.setDeadline(deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            try {
                jobManager.saveJob(latestJob);
                root.setCenter(buildMyPositionsView());
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, "Failed to save changes. Please try again.", false);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> root.setCenter(buildMyPositionsView()));

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("Requirements"), requirementsArea,
            new Label("Application Deadline"), deadlinePicker,
            buttonBox,
            messageLabel
        );

        page.getChildren().addAll(backButton, title, tip, form);
        return page;
    }

    private Node buildMyPositionsView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label("My Positions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        List<TAJob> jobs = jobManager.getJobsByMo(moStaffId);

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        list.setPadding(new Insets(16));

        if (jobs.isEmpty()) {
            Label empty = new Label("You have not published any positions yet. Click \"Post Position\" on the left to get started.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
        } else {
            for (TAJob job : jobs) {
                HBox row = new HBox();
                row.setSpacing(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 14 0 14 0; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

                VBox textBox = new VBox();
                textBox.setSpacing(6);

                // 标题行 + 状态 badge
                HBox titleRow = new HBox();
                titleRow.setSpacing(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                Label rowTitle = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
                rowTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label statusBadge = buildStatusBadge(job);
                titleRow.getChildren().addAll(rowTitle, statusBadge);

                Label rowMeta = new Label("Openings: " + job.getRecruitmentCount()
                    + "  Deadline: " + (isBlank(job.getDeadline()) ? "Not set" : job.getDeadline()));
                rowMeta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
                List<TAApplicationRecord> jobRecords = recordManager.getApplicationsByMoStaffId(moStaffId)
                    .stream().filter(r -> r.getJobId().equals(job.getJobId())).collect(Collectors.toList());
                long pendingCount = jobRecords.stream()
                    .filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count();

                HBox applicantRow = new HBox();
                applicantRow.setSpacing(8);
                applicantRow.setAlignment(Pos.CENTER_LEFT);
                Label applicants = new Label("Applicants: " + jobRecords.size());
                applicants.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
                applicantRow.getChildren().add(applicants);
                if (pendingCount > 0) {
                    Label pendingBadge = new Label(pendingCount + " pending review");
                    pendingBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #cc0000;" +
                        " -fx-background-color: #fff0f0; -fx-border-color: #ffaaaa;" +
                        " -fx-border-width: 1; -fx-padding: 1 8 1 8;");
                    applicantRow.getChildren().add(pendingBadge);
                }
                textBox.getChildren().addAll(titleRow, rowMeta, applicantRow);

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                Button editButton = new Button("Edit Position");
                editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                editButton.setDisable(!canEditJob(job));
                editButton.setOnAction(e -> root.setCenter(buildEditPositionView(job)));
                actionBox.getChildren().add(editButton);

                Button detailButton = new Button("View Details");
                detailButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                detailButton.setOnAction(e -> root.setCenter(buildJobDetailView(job)));
                actionBox.getChildren().add(detailButton);

                boolean expired = isJobExpired(job);
                if (expired) {
                    // 已过期：显示不可操作提示
                    Label expiredNote = new Label("Deadline has passed");
                    expiredNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999; -fx-padding: 8 0 8 0;");
                    actionBox.getChildren().add(expiredNote);
                } else if (job.isActive()) {
                    // 已手动关闭：显示 Reopen 按钮
                    Button reopenButton = new Button("Reopen Position");
                    reopenButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #008800; -fx-border-color: #008800; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                    reopenButton.setOnAction(e -> {
                        job.setActive(false);
                        try {
                            jobManager.saveJob(job);
                            root.setCenter(buildMyPositionsView());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                    actionBox.getChildren().add(reopenButton);
                } else {
                    // 开放中：显示 Close 按钮，点击弹确认框
                    Button closeButton = new Button("Close Position");
                    closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                    closeButton.setOnAction(e -> {
                        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Close Position");
                        confirm.setHeaderText("Close \"" + job.getPositionName() + "\"?");
                        confirm.setContentText("Closing this position will remove it from the public listing.\nApplicants will no longer be able to apply.\nYou can reopen it at any time.");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == javafx.scene.control.ButtonType.OK) {
                                job.setActive(true);
                                try {
                                    jobManager.saveJob(job);
                                    root.setCenter(buildMyPositionsView());
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
                    Label editHint = new Label("Edit locked: " + getEditBlockedReason(job));
                    editHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");
                    textBox.getChildren().add(editHint);
                }
            }
        }

        page.getChildren().addAll(title, list);
        return page;
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
        Label title = new Label("Position Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        Label subtitle = new Label("Track the status of your positions together with application and approval counts.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> root.setCenter(buildPositionStatisticsView()));

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
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        if (jobs.isEmpty()) {
            Label empty = new Label("You have not published any positions yet. Publish one to start tracking progress here.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
        } else {
            for (TAJob job : jobs) {
                list.getChildren().add(buildPositionStatisticsItem(job));
            }
        }

        page.getChildren().addAll(headerRow, topStatRow, bottomStatRow, list);
        return page;
    }

    private VBox buildPositionStatisticsItem(TAJob job) {
        VBox item = new VBox();
        item.setSpacing(8);
        item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 14;");

        Label title = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label status = new Label("Status: " + getPositionStatisticsStatus(job));
        status.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label meta = new Label(
            "Course: " + showFallback(job.getCourseName())
                + "  Deadline: " + (isBlank(job.getDeadline()) ? "Not set" : job.getDeadline())
                + "  Openings: " + job.getRecruitmentCount()
        );
        meta.setWrapText(true);
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        int applicationCount = countApplicationsForJob(job.getJobId());
        int approvedCount = countApprovedApplicationsForJob(job.getJobId());
        Label progress = new Label("Applications: " + applicationCount + "  Approved: " + approvedCount);
        progress.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        item.getChildren().addAll(title, status, meta, progress);
        return item;
    }

    private Node buildJobDetailView(TAJob job) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Button backButton = new Button("← Back to My Positions");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> root.setCenter(buildMyPositionsView()));

        Label title = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label meta = new Label("Openings: " + job.getRecruitmentCount() + "  Deadline: " + (isBlank(job.getDeadline()) ? "Not set" : job.getDeadline()) + "  Status: " + getJobStatus(job));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label desc = new Label("Description: " + (isBlank(job.getRequirements()) ? "None" : job.getRequirements()));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");

        HBox topActionBox = new HBox();
        topActionBox.setSpacing(10);

        Button editButton = new Button("Edit Position");
        editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        editButton.setDisable(!canEditJob(job));
        editButton.setOnAction(e -> root.setCenter(buildEditPositionView(job)));

        Label editHint = new Label(canEditJob(job)
            ? "This position can still be edited."
            : "Edit locked: " + getEditBlockedReason(job));
        editHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");
        topActionBox.getChildren().addAll(editButton, editHint);

        VBox recordBox = new VBox();
        recordBox.setSpacing(12);
        recordBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(r -> job.getJobId().equals(r.getJobId()))
            .collect(Collectors.toList());

        Label applicantTitle = new Label("Applicants for this Position (showing current MO's positions only)");
        applicantTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        if (records.isEmpty()) {
            Label empty = new Label("No applications for this position yet. Check back later or view all applications in Application Review.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            recordBox.getChildren().addAll(applicantTitle, empty);
        } else {
            recordBox.getChildren().add(applicantTitle);
            for (TAApplicationRecord record : records) {
                HBox row = new HBox();
                row.setSpacing(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 12;");

                VBox textBox = new VBox();
                textBox.setSpacing(5);
                Label name = new Label(record.getStudentName() + " (" + record.getTaStudentId() + ")");
                name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label applicantMeta = new Label("Major: " + record.getMajor() + "  ·  Applied: " + fmtDate(record.getApplicationDate()));
                applicantMeta.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");
                textBox.getChildren().addAll(name, applicantMeta);

                Label statusLabel = new Label(formatStatus(record.getStatus()));
                statusLabel.setStyle(statusBadgeStyle(record.getStatus()));
                statusLabel.setPrefWidth(100);

                Button viewBtn = new Button("View Application");
                viewBtn.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-padding: 7 14 7 14; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> root.setCenter(
                    buildApplicationDetailView(record, () -> root.setCenter(buildJobDetailView(job)))));

                HBox.setHgrow(textBox, Priority.ALWAYS);
                row.getChildren().addAll(textBox, statusLabel, viewBtn);
                recordBox.getChildren().add(row);
            }
        }

        page.getChildren().addAll(backButton, title, meta, desc, topActionBox, recordBox);
        return page;
    }

    private Node buildApplicantReviewView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label("Application Review");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label subtitle = new Label("Review all applications submitted for the positions published by the current module organiser.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox filterBox = new HBox();
        filterBox.setSpacing(12);

        TextField majorFilterField = createTextField("Filter by major");
        TextField availableTimeFilterField = createTextField("Filter by available time");
        TextField skillsFilterField = createTextField("Filter by skills");
        HBox.setHgrow(majorFilterField, Priority.ALWAYS);
        HBox.setHgrow(availableTimeFilterField, Priority.ALWAYS);
        HBox.setHgrow(skillsFilterField, Priority.ALWAYS);

        Button clearButton = new Button("Clear Filters");
        clearButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        filterBox.getChildren().addAll(majorFilterField, availableTimeFilterField, skillsFilterField, clearButton);

        HBox batchActionBox = new HBox();
        batchActionBox.setSpacing(12);

        Button selectAllButton = new Button("Select Pending");
        selectAllButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        Button batchApproveButton = new Button("Batch Approve");
        batchApproveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        Button batchRejectButton = new Button("Batch Reject");
        batchRejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        batchActionBox.getChildren().addAll(selectAllButton, clearSelectionButton, batchApproveButton, batchRejectButton);

        Label summary = new Label();
        summary.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        Label selectionLabel = new Label();
        selectionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 13px;");

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        Set<String> selectedApplicationIds = new HashSet<>();
        Runnable refreshList = () -> refreshApplicantReviewList(
            list,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField
        );

        majorFilterField.textProperty().addListener((obs, oldValue, newValue) -> refreshList.run());
        availableTimeFilterField.textProperty().addListener((obs, oldValue, newValue) -> refreshList.run());
        skillsFilterField.textProperty().addListener((obs, oldValue, newValue) -> refreshList.run());
        clearButton.setOnAction(e -> {
            majorFilterField.clear();
            availableTimeFilterField.clear();
            skillsFilterField.clear();
            refreshList.run();
        });
        selectAllButton.setOnAction(e -> {
            List<TAApplicationRecord> filteredPendingRecords = getFilteredApplicantRecords(
                majorFilterField.getText(),
                availableTimeFilterField.getText(),
                skillsFilterField.getText()
            ).stream()
                .filter(this::isPendingApplication)
                .collect(Collectors.toList());

            if (filteredPendingRecords.isEmpty()) {
                showMessage(feedbackLabel, "No pending applications match the current filters.", false);
                refreshList.run();
                return;
            }

            for (TAApplicationRecord record : filteredPendingRecords) {
                selectedApplicationIds.add(record.getApplicationId());
            }
            showMessage(feedbackLabel, "Selected " + filteredPendingRecords.size() + " pending applications.", true);
            refreshList.run();
        });
        clearSelectionButton.setOnAction(e -> {
            selectedApplicationIds.clear();
            showMessage(feedbackLabel, "Selection cleared.", true);
            refreshList.run();
        });
        batchApproveButton.setOnAction(e -> handleBatchReviewAction(
            true,
            selectedApplicationIds,
            feedbackLabel,
            list,
            summary,
            selectionLabel,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField
        ));
        batchRejectButton.setOnAction(e -> handleBatchReviewAction(
            false,
            selectedApplicationIds,
            feedbackLabel,
            list,
            summary,
            selectionLabel,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField
        ));

        refreshList.run();

        page.getChildren().addAll(title, subtitle, filterBox, batchActionBox, summary, selectionLabel, feedbackLabel, list);
        return page;
    }

    private void refreshApplicantReviewList(
        VBox list,
        Label summary,
        Label selectionLabel,
        Label feedbackLabel,
        Set<String> selectedApplicationIds,
        TextField majorFilterField,
        TextField availableTimeFilterField,
        TextField skillsFilterField
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
            skillsFilterField.getText()
        );
        long pendingCount = filteredRecords.stream()
            .filter(record -> TAApplicationRecord.STATUS_PENDING.equals(record.getStatus()))
            .count();

        summary.setText("Matching applications: " + filteredRecords.size() + "  Pending review: " + pendingCount);
        updateSelectionLabel(selectionLabel, selectedApplicationIds);

        if (filteredRecords.isEmpty()) {
            Label empty = new Label("No applications match the current filters.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
            return;
        }

        Runnable refreshAction = () -> refreshApplicantReviewList(
            list,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField
        );

        for (TAApplicationRecord record : filteredRecords) {
            list.getChildren().add(buildApplicantReviewItem(record, selectedApplicationIds, selectionLabel, refreshAction));
        }
    }

    private VBox buildApplicantReviewItem(
        TAApplicationRecord record,
        Set<String> selectedApplicationIds,
        Label selectionLabel,
        Runnable refreshAction
    ) {
        VBox item = new VBox();
        item.setSpacing(8);
        item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 14;");

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
        summary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        summaryRow.getChildren().addAll(selectBox, summary);

        Label profile = new Label(
            "Major: " + showFallback(record.getMajor())
                + "  Available Time: " + showFallback(record.getAvailableTime())
                + "  Skills: " + showFallback(record.getSkills())
        );
        profile.setWrapText(true);
        profile.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        Label meta = new Label("Applied: " + fmtDate(record.getApplicationDate()) + "  Status: " + formatStatus(record.getStatus()));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        Button openButton = new Button("Open Review");
        openButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        openButton.setOnAction(e -> root.setCenter(
            buildApplicationDetailView(record, () -> root.setCenter(buildApplicantReviewView()))));

        HBox actionBox = new HBox();
        actionBox.setSpacing(8);
        actionBox.getChildren().add(openButton);
        if (isPendingApplication(record)) {
            Button approveButton = new Button("Approve");
            approveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            approveButton.setOnAction(e -> {
                recordManager.approveApplication(record.getApplicationId());
                refreshAction.run();
            });

            Button rejectButton = new Button("Reject");
            rejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            rejectButton.setOnAction(e -> {
                recordManager.rejectApplication(record.getApplicationId());
                refreshAction.run();
            });
            actionBox.getChildren().addAll(approveButton, rejectButton);
        }

        item.getChildren().addAll(summaryRow, profile, meta, actionBox);
        return item;
    }

    private Node buildApplicationDetailView(TAApplicationRecord record, Runnable onBack) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(20);

        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 0; -fx-cursor: hand;");
        backButton.setOnAction(e -> onBack.run());

        // 标题区
        HBox titleRow = new HBox();
        titleRow.setSpacing(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(record.getPositionName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        Label statusBadge = new Label(formatStatus(record.getStatus()));
        statusBadge.setStyle(statusBadgeStyle(record.getStatus()));
        titleRow.getChildren().addAll(title, statusBadge);

        Label courseInfo = new Label("Course: " + record.getCourseName() + "  ·  Applied: " + fmtDate(record.getApplicationDate()));
        courseInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        // 申请人信息卡片
        VBox profileCard = buildDetailCard("Applicant Profile", buildProfileGrid(record));

        // 审核操作区
        VBox reviewCard = buildReviewCard(record, onBack);

        page.getChildren().addAll(backButton, titleRow, courseInfo, profileCard, reviewCard);
        return page;
    }

    private VBox buildDetailCard(String cardTitle, Node content) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20;");
        Label label = new Label(cardTitle);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        card.getChildren().addAll(label, content);
        return card;
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
        };

        for (int i = 0; i < fields.length; i++) {
            Label key = new Label(fields[i][0]);
            key.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-weight: 600;");
            key.setPrefWidth(120);

            Label val = new Label(fields[i][1] == null || fields[i][1].isEmpty() ? "—" : fields[i][1]);
            val.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222;");
            val.setWrapText(true);

            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }
        return grid;
    }

    private VBox buildReviewCard(TAApplicationRecord record, Runnable onBack) {
        VBox card = new VBox();
        card.setSpacing(14);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20;");

        Label cardTitle = new Label("Review Decision");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        card.getChildren().add(cardTitle);

        if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            Label hint = new Label("This application is awaiting your decision.");
            hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

            javafx.scene.control.TextArea commentField = new javafx.scene.control.TextArea();
            commentField.setPromptText("Optional: add a review comment for your records...");
            commentField.setPrefRowCount(3);
            commentField.setWrapText(true);
            commentField.setStyle("-fx-font-size: 13px; -fx-border-color: #cccccc; -fx-border-width: 1;");

            HBox buttons = new HBox();
            buttons.setSpacing(12);

            Button approveBtn = new Button("✓  Approve");
            approveBtn.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;");
            approveBtn.setOnAction(e -> {
                record.setReviewComment(commentField.getText().trim());
                recordManager.approveApplication(record.getApplicationId());
                onBack.run();
            });

            Button rejectBtn = new Button("✗  Reject");
            rejectBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-border-width: 1; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> {
                record.setReviewComment(commentField.getText().trim());
                recordManager.rejectApplication(record.getApplicationId());
                onBack.run();
            });

            buttons.getChildren().addAll(approveBtn, rejectBtn);
            card.getChildren().addAll(hint, new Label("Review Comment (optional):"), commentField, buttons);
        } else {
            Label decided = new Label("Decision: " + formatStatus(record.getStatus()));
            decided.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
            card.getChildren().add(decided);

            if (record.getReviewComment() != null && !record.getReviewComment().isEmpty()) {
                Label comment = new Label("Comment: " + record.getReviewComment());
                comment.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                comment.setWrapText(true);
                card.getChildren().add(comment);
            }

            // Only allow undo for APPROVED / REJECTED (not WITHDRAWN — that's the TA's action)
            if (TAApplicationRecord.STATUS_APPROVED.equals(record.getStatus()) ||
                    TAApplicationRecord.STATUS_REJECTED.equals(record.getStatus())) {
                Label undoHint = new Label("You can undo this decision to reopen the application for review.");
                undoHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #999999;");

                Button undoBtn = new Button("↩  Undo Decision");
                undoBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #555555;" +
                    " -fx-border-color: #aaaaaa; -fx-border-width: 1;" +
                    " -fx-font-size: 13px; -fx-padding: 7 16 7 16; -fx-cursor: hand;");
                undoBtn.setOnAction(e -> {
                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Undo Decision");
                    confirm.setHeaderText("Reset this application back to \"Under Review\"?");
                    confirm.setContentText("The applicant's status will return to Pending and you can re-review it.");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == javafx.scene.control.ButtonType.OK) {
                            recordManager.resetToPending(record.getApplicationId());
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
                return "-fx-font-size: 12px; -fx-text-fill: #008800; -fx-background-color: #efffef; -fx-border-color: #aaccaa; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
            case TAApplicationRecord.STATUS_REJECTED:
                return "-fx-font-size: 12px; -fx-text-fill: #cc0000; -fx-background-color: #fff0f0; -fx-border-color: #eeaaaa; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
            default:
                return "-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 3 10 3 10;";
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
            style = "-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        } else if (isJobExpired(job)) {
            text = "Expired";
            style = "-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        } else {
            text = "Open";
            style = "-fx-font-size: 11px; -fx-text-fill: #008800; -fx-background-color: #efffef; -fx-border-color: #aaccaa; -fx-border-width: 1; -fx-padding: 2 8 2 8;";
        }
        Label badge = new Label(text);
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
        String skillsFilter
    ) {
        return recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(record -> matchesApplicantFilters(record, majorFilter, availableTimeFilter, skillsFilter))
            .collect(Collectors.toList());
    }

    private boolean matchesApplicantFilters(
        TAApplicationRecord record,
        String majorFilter,
        String availableTimeFilter,
        String skillsFilter
    ) {
        return matchesKeywordFilter(record.getMajor(), majorFilter)
            && matchesKeywordFilter(record.getAvailableTime(), availableTimeFilter)
            && matchesKeywordFilter(record.getSkills(), skillsFilter);
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

    private void handleBatchReviewAction(
        boolean approve,
        Set<String> selectedApplicationIds,
        Label feedbackLabel,
        VBox list,
        Label summary,
        Label selectionLabel,
        TextField majorFilterField,
        TextField availableTimeFilterField,
        TextField skillsFilterField
    ) {
        if (selectedApplicationIds.isEmpty()) {
            showMessage(feedbackLabel, "Select at least one pending application first.", false);
            return;
        }

        int selectedCount = selectedApplicationIds.size();
        int updatedCount = 0;
        for (String applicationId : new HashSet<>(selectedApplicationIds)) {
            boolean updated = approve
                ? recordManager.approveApplication(applicationId)
                : recordManager.rejectApplication(applicationId);
            if (updated) {
                updatedCount++;
                selectedApplicationIds.remove(applicationId);
            }
        }

        if (updatedCount == 0) {
            showMessage(feedbackLabel, "None of the selected applications could be updated.", false);
        } else if (updatedCount == selectedCount) {
            showMessage(feedbackLabel, (approve ? "Approved " : "Rejected ") + updatedCount + " applications.", true);
        } else {
            showMessage(
                feedbackLabel,
                "Updated " + updatedCount + " applications. "
                    + (selectedCount - updatedCount) + " were skipped because their status changed.",
                true
            );
        }

        refreshApplicantReviewList(
            list,
            summary,
            selectionLabel,
            feedbackLabel,
            selectedApplicationIds,
            majorFilterField,
            availableTimeFilterField,
            skillsFilterField
        );
    }

    private boolean canEditJob(TAJob job) {
        return !isJobExpired(job) && countApplicationsForJob(job.getJobId()) == 0;
    }

    private String getEditBlockedReason(TAJob job) {
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
        field.setPromptText(prompt);
        field.setStyle("-fx-font-size: 14px; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff; -fx-padding: 10 12 10 12;");
        return field;
    }

    private void showMessage(Label label, String message, boolean success) {
        label.setText(message);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (success ? "#008800" : "#cc0000") + ";");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String showFallback(String text) {
        return isBlank(text) ? "Not provided" : text.trim();
    }

    private boolean isPendingApplication(TAApplicationRecord record) {
        return TAApplicationRecord.STATUS_PENDING.equals(record.getStatus());
    }

    private void updateSelectionLabel(Label selectionLabel, Set<String> selectedApplicationIds) {
        selectionLabel.setText("Selected pending applications: " + selectedApplicationIds.size());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
