package TA.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox headerBox = createHeaderBox();
        VBox jobInfoBox = createJobInfoBox();
        VBox personalInfoBox = createPersonalInfoBox();
        HBox actionBox = createActionBox();

        content.getChildren().addAll(headerBox, jobInfoBox, personalInfoBox, actionBox);
        root.setCenter(content);

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
        backButton.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        backButton.setOnAction(e -> {
            if (dialogStage != null) {
                dialogStage.close();
                if (dialogCloseListener != null) {
                    dialogCloseListener.onDialogClosed();
                }
            }
        });

        Label titleLabel = new Label("Apply for Position");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        headerBox.getChildren().addAll(backButton, titleLabel);

        return headerBox;
    }

    private VBox createJobInfoBox() {
        VBox jobBox = new VBox();
        jobBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        jobBox.setPadding(new Insets(16, 16, 16, 16));
        jobBox.setSpacing(16);

        Label sectionTitle = new Label("Position Details");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox titleRow = new HBox();
        titleRow.setSpacing(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Position:");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label titleValue = new Label(currentJob.getPositionName());
        titleValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        titleRow.getChildren().addAll(titleLabel, titleValue);

        HBox courseRow = new HBox();
        courseRow.setSpacing(12);
        courseRow.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course/Activity:");
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label courseValue = new Label(currentJob.getCourseName());
        courseValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        courseRow.getChildren().addAll(courseLabel, courseValue);

        HBox countRow = new HBox();
        countRow.setSpacing(12);
        countRow.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label countValue = new Label(String.valueOf(currentJob.getRecruitmentCount()));
        countValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        countRow.getChildren().addAll(countLabel, countValue);

        HBox requirementRow = new HBox();
        requirementRow.setSpacing(12);
        requirementRow.setAlignment(Pos.CENTER_LEFT);

        Label requirementLabel = new Label("Requirements:");
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label requirementValue = new Label(currentJob.getRequirements());
        requirementValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        requirementRow.getChildren().addAll(requirementLabel, requirementValue);

        HBox deadlineRow = new HBox();
        deadlineRow.setSpacing(12);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("Deadline:");
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label deadlineValue = new Label(currentJob.getDeadline());
        deadlineValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        deadlineRow.getChildren().addAll(deadlineLabel, deadlineValue);

        HBox publisherRow = new HBox();
        publisherRow.setSpacing(12);
        publisherRow.setAlignment(Pos.CENTER_LEFT);

        Label publisherLabel = new Label("Posted By:");
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label publisherValue = new Label(currentJob.getPublisher());
        publisherValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        publisherRow.getChildren().addAll(publisherLabel, publisherValue);

        detailsBox.getChildren().addAll(titleRow, courseRow, countRow, requirementRow, deadlineRow, publisherRow);
        jobBox.getChildren().addAll(sectionTitle, detailsBox);

        return jobBox;
    }

    private VBox createPersonalInfoBox() {
        VBox personalBox = new VBox();
        personalBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        personalBox.setPadding(new Insets(16, 16, 16, 16));
        personalBox.setSpacing(16);

        Label sectionTitle = new Label("Personal Details");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox nameRow = new HBox();
        nameRow.setSpacing(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label nameValue = new Label(currentUser.getName());
        nameValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        nameRow.getChildren().addAll(nameLabel, nameValue);

        HBox idRow = new HBox();
        idRow.setSpacing(12);
        idRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Student ID:");
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label idValue = new Label(currentUser.getTAId());
        idValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        idRow.getChildren().addAll(idLabel, idValue);

        HBox majorRow = new HBox();
        majorRow.setSpacing(12);
        majorRow.setAlignment(Pos.CENTER_LEFT);

        Label majorLabel = new Label("Major:");
        majorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label majorValue = new Label(currentUser.getMajor());
        majorValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        majorRow.getChildren().addAll(majorLabel, majorValue);

        HBox emailRow = new HBox();
        emailRow.setSpacing(12);
        emailRow.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label emailValue = new Label(currentUser.getEmail());
        emailValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        emailRow.getChildren().addAll(emailLabel, emailValue);

        HBox phoneRow = new HBox();
        phoneRow.setSpacing(12);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label phoneValue = new Label(currentUser.getPhone());
        phoneValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        phoneRow.getChildren().addAll(phoneLabel, phoneValue);

        HBox skillsRow = new HBox();
        skillsRow.setSpacing(12);
        skillsRow.setAlignment(Pos.CENTER_LEFT);

        Label skillsLabel = new Label("Skills:");
        skillsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label skillsValue = new Label(currentUser.getSkill());
        skillsValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        skillsRow.getChildren().addAll(skillsLabel, skillsValue);

        HBox timeRow = new HBox();
        timeRow.setSpacing(12);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("Availability:");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label timeValue = new Label(currentUser.getAvailableTime());
        timeValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        timeRow.getChildren().addAll(timeLabel, timeValue);

        detailsBox.getChildren().addAll(nameRow, idRow, majorRow, emailRow, phoneRow, skillsRow, timeRow);
        personalBox.getChildren().addAll(sectionTitle, detailsBox);

        return personalBox;
    }

    private HBox createActionBox() {
        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setSpacing(12);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            if (dialogStage != null) {
                dialogStage.close();
                if (dialogCloseListener != null) {
                    dialogCloseListener.onDialogClosed();
                }
            }
        });

        Button submitButton = new Button("Submit Application");
        submitButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        submitButton.setOnAction(e -> submitApplication());

        actionBox.getChildren().addAll(cancelButton, submitButton);

        return actionBox;
    }

    private void submitApplication() {
        System.out.println("=== 开始申请流程 ===");
        System.out.println("学生ID: " + currentUser.getTAId());
        System.out.println("岗位ID: " + currentJob.getJobId());

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

        System.out.println("申请ID: " + record.getApplicationId());
        System.out.println("申请状态: " + record.getStatus());
        
        recordManager.saveApplication(record);
        System.out.println("申请记录已保存！");
        
        showSuccess("Application Submitted", "Your application has been submitted successfully. Status: Under Review.");

        if (dialogStage != null) {
            dialogStage.close();
            if (dialogCloseListener != null) {
                dialogCloseListener.onDialogClosed();
            }
        }

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
        alertBox.setStyle("-fx-background-color: #ffffff;");
        alertBox.setPadding(new Insets(30, 40, 30, 40));
        alertBox.setSpacing(20);
        alertBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("⚠");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #333333;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-wrap-text: true;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 10 40; -fx-cursor: hand;");
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
        alertBox.setStyle("-fx-background-color: #ffffff;");
        alertBox.setPadding(new Insets(30, 40, 30, 40));
        alertBox.setSpacing(20);
        alertBox.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("✓");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #333333;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-wrap-text: true;");
        messageLabel.setMaxWidth(350);
        messageLabel.setAlignment(Pos.CENTER);

        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 10 40; -fx-cursor: hand;");
        closeButton.setOnAction(e -> alertStage.close());

        alertBox.getChildren().addAll(iconLabel, titleLabel, messageLabel, closeButton);

        Scene alertScene = new Scene(alertBox, 400, 300);
        alertStage.setScene(alertScene);
        alertStage.setResizable(false);
        alertStage.showAndWait();
    }
}
