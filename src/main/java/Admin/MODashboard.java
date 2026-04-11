package Admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        setActive(navDashboard);
        navDashboard.setOnMouseClicked(e -> { setActive(navDashboard); root.setCenter(buildDashboardView()); });
        navPublish.setOnMouseClicked(e -> { setActive(navPublish); root.setCenter(buildPostPositionView()); });
        navMyPositions.setOnMouseClicked(e -> { setActive(navMyPositions); root.setCenter(buildMyPositionsView()); });

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

        sidebar.getChildren().addAll(titleLabel, navDashboard, navPublish, navMyPositions, spacer, logoutLabel);
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

        VBox tips = new VBox();
        tips.setSpacing(12);
        tips.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 18;");
        Label tipsTitle = new Label("Quick Tips");
        tipsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        Label tip1 = new Label("• Click \"Post Position\" to fill in position details and set the application deadline.");
        Label tip2 = new Label("• Published positions appear automatically in the public listing and close after the deadline.");
        Label tip3 = new Label("• In \"My Positions\", view position details and applicant profiles for each posting.");
        Label tip4 = new Label("• In \"Application Review\", approve or reject individual applications; status syncs to applicants instantly.");
        tips.getChildren().addAll(tipsTitle, tip1, tip2, tip3, tip4);

        box.getChildren().addAll(header, tips);
        return box;
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

                HBox titleRow = new HBox();
                titleRow.setSpacing(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                Label rowTitle = new Label(job.getPositionName() + " (" + job.getCourseCode() + ")");
                rowTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                titleRow.getChildren().add(rowTitle);

                Label rowMeta = new Label("Openings: " + job.getRecruitmentCount()
                    + "  Deadline: " + (isBlank(job.getDeadline()) ? "Not set" : job.getDeadline()));
                rowMeta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

                List<TAApplicationRecord> jobRecords = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
                    .filter(r -> r.getJobId().equals(job.getJobId())).collect(Collectors.toList());
                Label applicants = new Label("Applicants: " + jobRecords.size());
                applicants.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

                textBox.getChildren().addAll(titleRow, rowMeta, applicants);

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                actionBox.setAlignment(Pos.CENTER_LEFT);

                Button detailButton = new Button("View Details");
                detailButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                detailButton.setOnAction(e -> root.setCenter(buildJobDetailView(job)));
                actionBox.getChildren().add(detailButton);

                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                editButton.setOnAction(e -> root.setCenter(buildEditPositionView(job)));
                actionBox.getChildren().add(editButton);

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

        Label meta = new Label("Openings: " + job.getRecruitmentCount() + "  Deadline: " + (isBlank(job.getDeadline()) ? "Not set" : job.getDeadline()));
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

        Label applicantTitle = new Label("Applicants for this Position");
        applicantTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        if (records.isEmpty()) {
            Label empty = new Label("No applications for this position yet.");
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

        page.getChildren().addAll(backButton, title, meta, desc, recordBox);
        return page;
    }

    private Node buildApplicationDetailView(TAApplicationRecord record, Runnable onBack) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(20);

        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 0; -fx-cursor: hand;");
        backButton.setOnAction(e -> onBack.run());

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

        VBox profileCard = new VBox();
        profileCard.setSpacing(14);
        profileCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20;");
        Label profileTitle = new Label("Applicant Profile");
        profileTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        javafx.scene.layout.GridPane profileGrid = new javafx.scene.layout.GridPane();
        profileGrid.setHgap(24);
        profileGrid.setVgap(10);

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

            profileGrid.add(key, 0, i);
            profileGrid.add(val, 1, i);
        }
        profileCard.getChildren().addAll(profileTitle, profileGrid);

        VBox reviewCard = new VBox();
        reviewCard.setSpacing(14);
        reviewCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20;");

        Label reviewTitle = new Label("Review Decision");
        reviewTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        reviewCard.getChildren().add(reviewTitle);

        if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
            Label hint = new Label("This application is awaiting your decision.");
            hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

            HBox buttons = new HBox();
            buttons.setSpacing(12);

            Button approveBtn = new Button("✓  Approve");
            approveBtn.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;");
            approveBtn.setOnAction(e -> {
                recordManager.approveApplication(record.getApplicationId());
                onBack.run();
            });

            Button rejectBtn = new Button("✗  Reject");
            rejectBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #cc0000; -fx-border-color: #cc0000; -fx-border-width: 1; -fx-font-size: 13px; -fx-padding: 9 20 9 20; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> {
                recordManager.rejectApplication(record.getApplicationId());
                onBack.run();
            });

            buttons.getChildren().addAll(approveBtn, rejectBtn);
            reviewCard.getChildren().addAll(hint, buttons);
        } else {
            Label decided = new Label("Decision: " + formatStatus(record.getStatus()));
            decided.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
            reviewCard.getChildren().add(decided);
        }

        page.getChildren().addAll(backButton, titleRow, courseInfo, profileCard, reviewCard);
        return page;
    }

    private Node buildEditPositionView(TAJob job) {
        VBox page = new VBox();
        page.setPadding(new Insets(24));
        page.setSpacing(18);

        Button backButton = new Button("← Back to My Positions");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> root.setCenter(buildMyPositionsView()));

        Label title = new Label("Edit Position");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

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
        requirementsArea.setText(job.getRequirements());
        requirementsArea.setPromptText("Enter requirements, e.g. skills, experience, working hours.");
        requirementsArea.setPrefRowCount(4);
        requirementsArea.setWrapText(true);
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        saveButton.setOnAction(e -> {
            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText)) {
                showMessage(messageLabel, "Please fill in all required fields.", false);
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

            job.setPositionName(positionName.trim());
            job.setCourseName(courseName.trim());
            job.setCourseCode(courseCode.trim());
            job.setRecruitmentCount(count);
            job.setRequirements(requirements == null ? "" : requirements.trim());

            try {
                jobManager.saveJob(job);
                showMessage(messageLabel, "Position updated successfully.", true);
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, "Update failed, please try again.", false);
            }
        });

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("Requirements"), requirementsArea,
            saveButton,
            messageLabel
        );
        page.getChildren().addAll(backButton, title, form);
        return page;
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
