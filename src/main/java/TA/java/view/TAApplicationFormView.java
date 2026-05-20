package TA.java.view;
import TA.java.TAJob;
import TA.java.TAApplication;
import TA.java.TAApplicationRecordManager;
import TA.java.TAApplicationRecord;
import TA.java.ResumeKeywordExtractor;
import TA.java.SkillUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class TAApplicationFormView {

    public interface ApplicationListener {
        void onApplicationSubmitted();
    }

    public interface DialogCloseListener {
        void onDialogClosed();
    }

    private TAJob currentJob;
    private TAApplication currentUser;
    private TAApplicationRecordManager recordManager;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ApplicationListener applicationListener;
    private DialogCloseListener dialogCloseListener;
    private Stage dialogStage;
    private javafx.scene.control.TextArea resumeTextArea;

    public TAApplicationFormView(TAJob job) {
        this.currentJob = job;
        this.recordManager = new TAApplicationRecordManager();
        loadUserData("2024999");
    }

    public TAApplicationFormView(TAJob job, String studentId) {
        this.currentJob = job;
        this.recordManager = new TAApplicationRecordManager();
        loadUserData(studentId);
    }

    public void setApplicationListener(ApplicationListener listener) {
        this.applicationListener = listener;
    }

    public void setDialogCloseListener(DialogCloseListener listener) {
        this.dialogCloseListener = listener;
    }

    private void loadUserData(String studentId) {
        try {
            String filePath = data.DataConfig.TA_DIR + studentId + ".json";
            File file = new File(filePath);
            if (file.exists()) {
                currentUser = objectMapper.readValue(file, TAApplication.class);
            } else {
                currentUser = new TAApplication("Unknown User", studentId, "Unknown Major", "", "", "", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentUser = new TAApplication("Unknown User", studentId, "Unknown Major", "", "", "", "");
        }
    }

    public void showDialog(Stage ownerStage) {
        dialogStage = new Stage();
        dialogStage.setTitle("Apply for Position");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(ownerStage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox headerBox = createHeaderBox();
        VBox jobInfoBox = createJobInfoBox();
        VBox personalInfoBox = createPersonalInfoBox();
        VBox resumeBox = createResumeBox();
        HBox actionBox = createActionBox();

        content.getChildren().addAll(headerBox, jobInfoBox, personalInfoBox, resumeBox, actionBox);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 700, 750);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.setOnCloseRequest(e -> {
            if (dialogCloseListener != null) {
                dialogCloseListener.onDialogClosed();
            }
        });
        dialogStage.showAndWait();
    }

    private HBox createHeaderBox() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(12);

        Button backButton = new Button("←");
        backButton.setStyle("-fx-font-size: 16px; -fx-text-fill: #6366f1; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        backButton.setOnAction(e -> {
            if (dialogStage != null) {
                dialogStage.close();
                if (dialogCloseListener != null) {
                    dialogCloseListener.onDialogClosed();
                }
            }
        });

        Label titleLabel = new Label("Apply for Position");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        headerBox.getChildren().addAll(backButton, titleLabel);

        return headerBox;
    }

    private VBox createJobInfoBox() {
        VBox jobBox = new VBox();
        jobBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        jobBox.setPadding(new Insets(16, 16, 16, 16));
        jobBox.setSpacing(16);

        Label sectionTitle = new Label("Position Details");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox titleRow = new HBox();
        titleRow.setSpacing(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Position:");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label titleValue = new Label(currentJob.getPositionName());
        titleValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        titleRow.getChildren().addAll(titleLabel, titleValue);

        HBox courseRow = new HBox();
        courseRow.setSpacing(12);
        courseRow.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course/Activity:");
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label courseValue = new Label(currentJob.getCourseName());
        courseValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        courseRow.getChildren().addAll(courseLabel, courseValue);

        HBox countRow = new HBox();
        countRow.setSpacing(12);
        countRow.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label countValue = new Label(String.valueOf(currentJob.getRecruitmentCount()));
        countValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        countRow.getChildren().addAll(countLabel, countValue);

        HBox requirementRow = new HBox();
        requirementRow.setSpacing(12);
        requirementRow.setAlignment(Pos.CENTER_LEFT);

        Label requirementLabel = new Label("Requirements:");
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label requirementValue = new Label(currentJob.getRequirements());
        requirementValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        requirementRow.getChildren().addAll(requirementLabel, requirementValue);

        HBox requiredSkillsRow = new HBox();
        requiredSkillsRow.setSpacing(12);
        requiredSkillsRow.setAlignment(Pos.CENTER_LEFT);

        Label requiredSkillsLabel = new Label("Required Skills:");
        requiredSkillsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label requiredSkillsValue = new Label(
            SkillUtils.toDisplayText(currentJob.getRequiredSkills(), "No specific skill requirements")
        );
        requiredSkillsValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        requiredSkillsValue.setWrapText(true);

        requiredSkillsRow.getChildren().addAll(requiredSkillsLabel, requiredSkillsValue);

        HBox deadlineRow = new HBox();
        deadlineRow.setSpacing(12);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("Deadline:");
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label deadlineValue = new Label(currentJob.getDeadline());
        deadlineValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        deadlineRow.getChildren().addAll(deadlineLabel, deadlineValue);

        HBox publisherRow = new HBox();
        publisherRow.setSpacing(12);
        publisherRow.setAlignment(Pos.CENTER_LEFT);

        Label publisherLabel = new Label("Posted By:");
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label publisherValue = new Label(currentJob.getPublisher());
        publisherValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        publisherRow.getChildren().addAll(publisherLabel, publisherValue);

        detailsBox.getChildren().addAll(titleRow, courseRow, countRow, requirementRow, requiredSkillsRow, deadlineRow, publisherRow);
        jobBox.getChildren().addAll(sectionTitle, detailsBox);

        return jobBox;
    }

    private VBox createPersonalInfoBox() {
        VBox personalBox = new VBox();
        personalBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        personalBox.setPadding(new Insets(16, 16, 16, 16));
        personalBox.setSpacing(16);

        Label sectionTitle = new Label("Personal Details");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox nameRow = new HBox();
        nameRow.setSpacing(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label nameValue = new Label(currentUser.getName());
        nameValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        nameRow.getChildren().addAll(nameLabel, nameValue);

        HBox idRow = new HBox();
        idRow.setSpacing(12);
        idRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Student ID:");
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label idValue = new Label(currentUser.getTAId());
        idValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        idRow.getChildren().addAll(idLabel, idValue);

        HBox majorRow = new HBox();
        majorRow.setSpacing(12);
        majorRow.setAlignment(Pos.CENTER_LEFT);

        Label majorLabel = new Label("Major:");
        majorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label majorValue = new Label(currentUser.getMajor());
        majorValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        majorRow.getChildren().addAll(majorLabel, majorValue);

        HBox emailRow = new HBox();
        emailRow.setSpacing(12);
        emailRow.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label emailValue = new Label(currentUser.getEmail());
        emailValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        emailRow.getChildren().addAll(emailLabel, emailValue);

        HBox phoneRow = new HBox();
        phoneRow.setSpacing(12);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label phoneValue = new Label(currentUser.getPhone());
        phoneValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        phoneRow.getChildren().addAll(phoneLabel, phoneValue);

        HBox skillsRow = new HBox();
        skillsRow.setSpacing(12);
        skillsRow.setAlignment(Pos.CENTER_LEFT);

        Label skillsLabel = new Label("Skills:");
        skillsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label skillsValue = new Label(currentUser.getSkill());
        skillsValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        skillsRow.getChildren().addAll(skillsLabel, skillsValue);

        HBox timeRow = new HBox();
        timeRow.setSpacing(12);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("Availability:");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Label timeValue = new Label(currentUser.getAvailableTime());
        timeValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        timeRow.getChildren().addAll(timeLabel, timeValue);

        detailsBox.getChildren().addAll(nameRow, idRow, majorRow, emailRow, phoneRow, skillsRow, timeRow);
        personalBox.getChildren().addAll(sectionTitle, detailsBox);

        return personalBox;
    }

    private VBox createResumeBox() {
        VBox resumeBox = new VBox();
        resumeBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        resumeBox.setPadding(new Insets(16, 16, 16, 16));
        resumeBox.setSpacing(12);

        Label sectionTitle = new Label("Resume / Supporting Statement");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label hint = new Label("Paste key resume content, TA experience, projects, courses, or skills here. The system will extract keywords automatically for the organiser.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        resumeTextArea = new javafx.scene.control.TextArea();
        resumeTextArea.setPromptText("Example: Python project experience, machine learning coursework, previous TA work, lab support, grading experience...");
        resumeTextArea.setPrefRowCount(5);
        resumeTextArea.setWrapText(true);
        resumeTextArea.setStyle("-fx-font-size: 13px; -fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-background-color: #ffffff;");

        Label emptyHint = new Label("Optional: leaving this empty will not block your application.");
        emptyHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        resumeBox.getChildren().addAll(sectionTitle, hint, resumeTextArea, emptyHint);
        return resumeBox;
    }

    private HBox createActionBox() {
        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setSpacing(12);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            if (dialogStage != null) {
                dialogStage.close();
                if (dialogCloseListener != null) {
                    dialogCloseListener.onDialogClosed();
                }
            }
        });

        Button submitButton = new Button("Submit Application");
        submitButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        submitButton.setOnMouseEntered(e ->
            submitButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;")
        );
        submitButton.setOnMouseExited(e ->
            submitButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;")
        );
        submitButton.setOnAction(e -> submitApplication());

        actionBox.getChildren().addAll(cancelButton, submitButton);

        return actionBox;
    }

    private void submitApplication() {
        System.out.println("=== 开始申请流程 ===");
        System.out.println("学生ID: " + currentUser.getTAId());
        System.out.println("岗位ID: " + currentJob.getJobId());

        data.JobDataManager jobDataManager = new data.JobDataManager();
        TAJob latestJob = jobDataManager.getJobById(currentJob.getJobId());
        if (!jobDataManager.isJobOpen(latestJob)) {
            System.out.println("岗位已截止或已下架，已阻止申请");
            showAlert("Position Closed", "This position is no longer open for new applications.\n\nExisting applications can still be reviewed by the module organiser.");
            return;
        }
        currentJob = latestJob;

        if (currentUser.getName() == null || currentUser.getName().trim().isEmpty()
            || currentUser.getMajor() == null || currentUser.getMajor().trim().isEmpty()
            || currentUser.getPhone() == null || currentUser.getPhone().trim().isEmpty()
            || currentUser.getAvailableTime() == null || currentUser.getAvailableTime().trim().isEmpty()
            || currentUser.getSkill() == null || currentUser.getSkill().trim().isEmpty()) {
            System.out.println("检测到档案不完整，已阻止");
            showAlert("Application Rejected", "Your profile is incomplete.\n\nPlease fill in your Name, Major, Phone, Available Time, and Skills before applying.");
            return;
        }

        if (recordManager.hasDuplicateApplication(currentUser.getTAId(), currentJob.getJobId())) {
            System.out.println("检测到重复申请，已阻止");
            showAlert("Duplicate Application", "You have already applied for this position.");
            return;
        }

        TAApplicationRecord record = new TAApplicationRecord(
            currentUser.getTAId(),
            currentJob.getMoStaffId() != null ? currentJob.getMoStaffId() : "",
            currentJob.getCourseCode(),
            currentJob.getJobId(),
            currentJob.getPositionName(),
            currentJob.getCourseName(),
            currentUser.getName(),
            currentUser.getMajor(),
            currentUser.getPhone(),
            currentUser.getEmail(),
            currentUser.getAvailableTime(),
            currentUser.getSkill()
        );
        String resumeText = resumeTextArea == null ? "" : resumeTextArea.getText().trim();
        record.setResumeText(resumeText);
        record.setResumeKeywords(ResumeKeywordExtractor.extractKeywords(resumeText, currentUser.getSkill(), currentJob));

        System.out.println("申请ID: " + record.getApplicationId());
        System.out.println("申请状态: " + record.getStatus());
        System.out.println("提取关键词: " + SkillUtils.toDisplayText(record.getResumeKeywords(), "None"));
        
        recordManager.saveApplication(record);
        System.out.println("申请记录已保存！");

        if (dialogStage != null) {
            dialogStage.close();
            if (dialogCloseListener != null) {
                dialogCloseListener.onDialogClosed();
            }
        }

         SkillGapAnalysisView.showSkillGapAnalysis(currentJob, currentUser); 

        if (applicationListener != null) {
            applicationListener.onApplicationSubmitted();
        }
    }

    private void showAlert(String title, String message) {
        System.out.println("显示警告弹窗: " + title + " - " + message);

        Stage alertStage = new Stage();
        alertStage.setTitle("Notice");
        alertStage.initModality(Modality.WINDOW_MODAL);
        if (dialogStage != null) {
            alertStage.initOwner(dialogStage);
        }

        VBox alertBox = new VBox();
        alertBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;");
        alertBox.setPadding(new Insets(30, 40, 30, 40));
        alertBox.setSpacing(20);
        alertBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("⚠");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #b45309;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 10 40; -fx-cursor: hand;");
        closeButton.setOnAction(e -> alertStage.close());

        alertBox.getChildren().addAll(iconLabel, titleLabel, messageLabel, closeButton);

        Scene alertScene = new Scene(alertBox, 400, 300);
        alertStage.setScene(alertScene);
        alertStage.setResizable(false);
        alertStage.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Stage alertStage = new Stage();
        alertStage.setTitle("Success");
        alertStage.initModality(Modality.WINDOW_MODAL);
        if (dialogStage != null) {
            alertStage.initOwner(dialogStage);
        }

        VBox alertBox = new VBox();
        alertBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 16; -fx-background-radius: 16;");
        alertBox.setPadding(new Insets(30, 40, 30, 40));
        alertBox.setSpacing(20);
        alertBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("✓");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #15803d;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 10 40; -fx-cursor: hand;");
        closeButton.setOnAction(e -> alertStage.close());

        alertBox.getChildren().addAll(iconLabel, titleLabel, messageLabel, closeButton);

        Scene alertScene = new Scene(alertBox, 400, 300);
        alertStage.setScene(alertScene);
        alertStage.setResizable(false);
        alertStage.showAndWait();
    }
}
