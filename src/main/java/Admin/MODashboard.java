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

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardView());

        Scene scene = new Scene(root, 1180, 720);
        stage.setTitle("TA 招聘管理系统 - 课程组织者控制台");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(24, 0, 24, 0));
        sidebar.setSpacing(0);

        Label titleLabel = new Label("课程组织者控制台");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222222;");
        titleLabel.setWrapText(true);
        titleLabel.setPadding(new Insets(0, 0, 24, 20));

        Label navDashboard = buildNavItem("仪表盘");
        Label navPublish = buildNavItem("岗位发布");
        Label navMyPositions = buildNavItem("我的岗位");
        Label navReviews = buildNavItem("申请审核");

        setActive(navDashboard);
        navDashboard.setOnMouseClicked(e -> { setActive(navDashboard); root.setCenter(buildDashboardView()); });
        navPublish.setOnMouseClicked(e -> { setActive(navPublish); root.setCenter(buildPostPositionView()); });
        navMyPositions.setOnMouseClicked(e -> { setActive(navMyPositions); root.setCenter(buildMyPositionsView()); });
        navReviews.setOnMouseClicked(e -> { setActive(navReviews); root.setCenter(buildApplicantReviewView()); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label logoutLabel = new Label("退出登录");
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

        Label header = new Label("欢迎，" + moStaffId + "");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        HBox statRow = new HBox();
        statRow.setSpacing(16);

        List<TAJob> myJobs = jobManager.getJobsByMo(moStaffId);
        List<TAApplicationRecord> myApplications = recordManager.getApplicationsByMoStaffId(moStaffId);

        statRow.getChildren().addAll(
            buildStatCard(myJobs.size(), "我发布的岗位"),
            buildStatCard((int) myJobs.stream().filter(this::isJobOpen).count(), "当前可申请岗位"),
            buildStatCard((int) myApplications.stream().filter(r -> TAApplicationRecord.STATUS_PENDING.equals(r.getStatus())).count(), "待审核申请"),
            buildStatCard((int) myApplications.stream().filter(r -> TAApplicationRecord.STATUS_APPROVED.equals(r.getStatus())).count(), "已通过申请")
        );

        box.getChildren().addAll(header, statRow, buildSectionCard("快速入口", buildDashboardTips()));
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
        Label tip1 = new Label("• 点击“岗位发布”填写基础岗位信息并设置申请截止时间。");
        Label tip2 = new Label("• 已发布岗位自动展示在公开岗位列表，截止后不可申请。");
        Label tip3 = new Label("• 在“我的岗位”中查看岗位详情和本岗位的申请人档案。");
        Label tip4 = new Label("• 在“申请审核”中审批单条申请，状态会同步至申请人记录页面。");
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

        Label title = new Label("发布新岗位");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        VBox form = new VBox();
        form.setSpacing(14);

        TextField positionNameField = createTextField("岗位名称");
        TextField courseNameField = createTextField("课程名称");
        TextField courseCodeField = createTextField("课程代码");
        TextField recruitmentCountField = createTextField("招聘人数");
        TextArea requirementsArea = new TextArea();
        requirementsArea.setPromptText("请输入岗位要求，例如技能、经验、工作时间等。");
        requirementsArea.setPrefRowCount(4);
        requirementsArea.setWrapText(true);
        requirementsArea.setStyle("-fx-font-size: 13px; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff;");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("申请截止日期");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 13px;");

        Button submitButton = new Button("发布岗位");
        submitButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        submitButton.setOnAction(e -> {
            String positionName = positionNameField.getText();
            String courseName = courseNameField.getText();
            String courseCode = courseCodeField.getText();
            String recruitText = recruitmentCountField.getText();
            String requirements = requirementsArea.getText();
            LocalDate deadline = deadlinePicker.getValue();

            if (isBlank(positionName) || isBlank(courseName) || isBlank(courseCode) || isBlank(recruitText) || deadline == null) {
                showMessage(messageLabel, "请填写所有必填字段，并选择申请截止日期。", false);
                return;
            }
            int count;
            try {
                count = Integer.parseInt(recruitText.trim());
                if (count <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showMessage(messageLabel, "招聘人数必须为大于0的整数。", false);
                return;
            }

            if (deadline.isBefore(LocalDate.now())) {
                showMessage(messageLabel, "截止日期必须是今天或之后的日期。", false);
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
                showMessage(messageLabel, "岗位已发布，并已同步到公开岗位列表。", true);
                positionNameField.clear();
                courseNameField.clear();
                courseCodeField.clear();
                recruitmentCountField.clear();
                requirementsArea.clear();
                deadlinePicker.setValue(null);
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage(messageLabel, "发布失败，请稍后重试。", false);
            }
        });

        form.getChildren().addAll(
            positionNameField,
            courseNameField,
            courseCodeField,
            recruitmentCountField,
            new Label("岗位要求"), requirementsArea,
            new Label("申请截止日期"), deadlinePicker,
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

        Label title = new Label("我的岗位");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        List<TAJob> jobs = jobManager.getJobsByMo(moStaffId);

        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        list.setPadding(new Insets(16));

        if (jobs.isEmpty()) {
            Label empty = new Label("您尚未发布任何岗位，点击左侧“岗位发布”立即创建。");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
        } else {
            for (TAJob job : jobs) {
                HBox row = new HBox();
                row.setSpacing(14);
                row.setStyle("-fx-padding: 14 0 14 0; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

                VBox textBox = new VBox();
                textBox.setSpacing(6);
                Label rowTitle = new Label(job.getPositionName() + "（" + job.getCourseCode() + "）");
                rowTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label rowMeta = new Label("招聘人数：" + job.getRecruitmentCount() + "，截止日期：" + (isBlank(job.getDeadline()) ? "未设置" : job.getDeadline()) + "，状态：" + getJobStatus(job));
                rowMeta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
                Label applicants = new Label("申请人数：" + recordManager.getApplicationsByMoStaffId(moStaffId).stream().filter(r -> r.getJobId().equals(job.getJobId())).count());
                applicants.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");
                textBox.getChildren().addAll(rowTitle, rowMeta, applicants);

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                Button detailButton = new Button("查看详情");
                detailButton.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                detailButton.setOnAction(e -> root.setCenter(buildJobDetailView(job)));
                Button closeButton = new Button("结束发布");
                closeButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
                closeButton.setOnAction(e -> {
                    job.setActive(true);
                    try {
                        jobManager.saveJob(job);
                        root.setCenter(buildMyPositionsView());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                actionBox.getChildren().addAll(detailButton, closeButton);
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

        Button backButton = new Button("← 返回我的岗位");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        backButton.setOnAction(e -> root.setCenter(buildMyPositionsView()));

        Label title = new Label(job.getPositionName() + "（" + job.getCourseCode() + "）");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label meta = new Label("招聘人数：" + job.getRecruitmentCount() + "，截止日期：" + (isBlank(job.getDeadline()) ? "未设置" : job.getDeadline()) + "，状态：" + getJobStatus(job));
        meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label desc = new Label("岗位说明：" + (isBlank(job.getRequirements()) ? "无" : job.getRequirements()));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");

        VBox recordBox = new VBox();
        recordBox.setSpacing(12);
        recordBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId).stream()
            .filter(r -> job.getJobId().equals(r.getJobId()))
            .collect(Collectors.toList());

        Label applicantTitle = new Label("本岗位申请人（仅展示当前登录 MO 发布岗位）");
        applicantTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        if (records.isEmpty()) {
            Label empty = new Label("当前岗位尚无申请。请耐心等待，或在申请审核页查看所有申请。");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            recordBox.getChildren().addAll(applicantTitle, empty);
        } else {
            recordBox.getChildren().add(applicantTitle);
            for (TAApplicationRecord record : records) {
                VBox item = new VBox();
                item.setSpacing(8);
                item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 12;");

                Label name = new Label("姓名：" + record.getStudentName() + "（" + record.getTaStudentId() + "）");
                name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label info = new Label("专业：" + record.getMajor() + "，电话：" + record.getPhone() + "，邮箱：" + record.getEmail());
                info.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label applicationTime = new Label("申请时间：" + record.getApplicationDate());
                applicationTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label status = new Label("当前状态：" + formatStatus(record.getStatus()));
                status.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
                    Button approveButton = new Button("通过");
                    approveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    approveButton.setOnAction(e -> {
                        recordManager.approveApplication(record.getApplicationId());
                        root.setCenter(buildJobDetailView(job));
                    });
                    Button rejectButton = new Button("未通过");
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

        Label title = new Label("申请审核");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        List<TAApplicationRecord> records = recordManager.getApplicationsByMoStaffId(moStaffId);
        VBox list = new VBox();
        list.setSpacing(12);
        list.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 16;");

        if (records.isEmpty()) {
            Label empty = new Label("当前尚无本岗位申请记录。发布岗位后，可在此审核对应申请。\n提示：请确认申请人已提交资料。" );
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
            list.getChildren().add(empty);
        } else {
            for (TAApplicationRecord record : records) {
                VBox item = new VBox();
                item.setSpacing(8);
                item.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ededed; -fx-border-width: 1; -fx-padding: 14;");

                Label summary = new Label(record.getPositionName() + " - " + record.getStudentName() + "（" + record.getTaStudentId() + "）");
                summary.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222222;");
                Label applicantInfo = new Label("专业：" + record.getMajor() + "，电话：" + record.getPhone() + "，邮箱：" + record.getEmail());
                applicantInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label applicationTime = new Label("申请时间：" + record.getApplicationDate());
                applicationTime.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
                Label status = new Label("当前状态：" + formatStatus(record.getStatus()));
                status.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

                HBox actionBox = new HBox();
                actionBox.setSpacing(8);
                if (TAApplicationRecord.STATUS_PENDING.equals(record.getStatus())) {
                    Button approveButton = new Button("通过");
                    approveButton.setStyle("-fx-background-color: #008800; -fx-text-fill: #ffffff; -fx-padding: 6 14 6 14; -fx-cursor: hand;");
                    approveButton.setOnAction(e -> {
                        recordManager.approveApplication(record.getApplicationId());
                        root.setCenter(buildApplicantReviewView());
                    });
                    Button rejectButton = new Button("未通过");
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
                return "审核中";
            case TAApplicationRecord.STATUS_APPROVED:
                return "已通过";
            case TAApplicationRecord.STATUS_REJECTED:
                return "未通过";
            case TAApplicationRecord.STATUS_WITHDRAWN:
                return "已撤回";
            default:
                return status;
        }
    }

    private String getJobStatus(TAJob job) {
        if (job.isActive()) {
            return "已截止";
        }
        if (isJobExpired(job)) {
            return "已到期";
        }
        return "开放中";
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
