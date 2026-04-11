package Admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
    private Label activeNavLabel;
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
        Label navMyPositions = buildNavItem("My Positions");
        Label navReviews = buildNavItem("Application Review");

        setActive(navDashboard);
        navDashboard.setOnMouseClicked(e -> { setActive(navDashboard); root.setCenter(buildDashboardView()); });
        navPublish.setOnMouseClicked(e -> { setActive(navPublish); root.setCenter(buildPostPositionView()); });
        navMyPositions.setOnMouseClicked(e -> { setActive(navMyPositions); root.setCenter(buildMyPositionsView()); });
        navReviews.setOnMouseClicked(e -> { setActive(navReviews); root.setCenter(buildApplicantReviewView()); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logoutLabel = new Label("Log Out");
        logoutLabel.setMaxWidth(Double.MAX_VALUE);
        logoutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-cursor: hand; -fx-padding: 10 16 24 16;");
        logoutLabel.setOnMouseClicked(e -> {
            stage.close();
            try {
                new LoginScreen.LoginView().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sidebar.getChildren().addAll(titleLabel, navDashboard, navPublish, navMyPositions, navReviews, spacer, logoutLabel);
        return sidebar;
    }

    private Label buildNavItem(String text) {
        Label item = new Label(text);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle(NAV_DEFAULT);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private void setActive(Label target) {
        if (activeNavLabel != null) {
            activeNavLabel.setStyle(NAV_DEFAULT);
        }
        target.setStyle(NAV_ACTIVE);
        activeNavLabel = target;
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
                Label applicants = new Label("Applicants: " + recordManager.getApplicationsByMoStaffId(moStaffId)
                    .stream().filter(r -> r.getJobId().equals(job.getJobId())).count());
                applicants.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
                textBox.getChildren().addAll(titleRow, rowMeta, applicants);

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                actionBox.setAlignment(Pos.CENTER_LEFT);

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
            }
        }

        page.getChildren().addAll(title, list);
        return page;
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
                VBox item = new VBox();
                item.setSpacing(8);
                item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 12;");

                Label name = new Label("Name: " + record.getStudentName() + " (" + record.getTaStudentId() + ")");
                name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label info = new Label("Major: " + record.getMajor() + "  Phone: " + record.getPhone() + "  Email: " + record.getEmail());
                info.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label applicationTime = new Label("Applied: " + record.getApplicationDate());
                applicationTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label status = new Label("Status: " + formatStatus(record.getStatus()));
                status.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
                    Button approveButton = new Button("Approve");
                    approveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    approveButton.setOnAction(e -> {
                        recordManager.approveApplication(record.getApplicationId());
                        root.setCenter(buildJobDetailView(job));
                    });
                    Button rejectButton = new Button("Reject");
                    rejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    rejectButton.setOnAction(e -> {
                        recordManager.rejectApplication(record.getApplicationId());
                        root.setCenter(buildJobDetailView(job));
                    });
                    actionBox.getChildren().addAll(approveButton, rejectButton);
                }

                item.getChildren().addAll(name, info, applicationTime, status, actionBox);
                recordBox.getChildren().add(item);
            }
        }

        page.getChildren().addAll(backButton, title, meta, desc, recordBox);
        return page;
    }

    private Node buildApplicantReviewView() {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Label title = new Label("Application Review");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId);
        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        if (records.isEmpty()) {
            Label empty = new Label("No applications yet. After publishing a position, you can review applications here.\nNote: Please confirm that applicants have submitted their details.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
        } else {
            for (TAApplicationRecord record : records) {
                VBox item = new VBox();
                item.setSpacing(8);
                item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 14;");

                Label summary = new Label(record.getPositionName() + " - " + record.getStudentName() + " (" + record.getTaStudentId() + ")");
                summary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label applicantInfo = new Label("Major: " + record.getMajor() + "  Phone: " + record.getPhone() + "  Email: " + record.getEmail());
                applicantInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label applicationTime = new Label("Applied: " + record.getApplicationDate());
                applicationTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label status = new Label("Status: " + formatStatus(record.getStatus()));
                status.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
                    Button approveButton = new Button("Approve");
                    approveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    approveButton.setOnAction(e -> {
                        recordManager.approveApplication(record.getApplicationId());
                        root.setCenter(buildApplicantReviewView());
                    });
                    Button rejectButton = new Button("Reject");
                    rejectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    rejectButton.setOnAction(e -> {
                        recordManager.rejectApplication(record.getApplicationId());
                        root.setCenter(buildApplicantReviewView());
                    });
                    actionBox.getChildren().addAll(approveButton, rejectButton);
                }

                item.getChildren().addAll(summary, applicantInfo, applicationTime, status, actionBox);
                list.getChildren().add(item);
            }
        }

        page.getChildren().addAll(title, list);
        return page;
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

    public static void main(String[] args) {
        launch(args);
    }
}
