package ZiqianCao.java;

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

    public void setApplicationListener(ApplicationListener listener) {
        this.applicationListener = listener;
    }

    public void setDialogCloseListener(DialogCloseListener listener) {
        this.dialogCloseListener = listener;
    }

    private void loadUserData(String studentId) {
        try {
            String filePath = "resources/Data/TAData/" + studentId + ".json";
            File file = new File(filePath);
            if (file.exists()) {
                currentUser = objectMapper.readValue(file, TAApplication.class);
            } else {
                currentUser = new TAApplication("未知用户", studentId, "未知专业", "", "", "", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentUser = new TAApplication("未知用户", studentId, "未知专业", "", "", "", "");
        }
    }

    public void showDialog(Stage ownerStage) {
        dialogStage = new Stage();
        dialogStage.setTitle("申请岗位");
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

        Label titleLabel = new Label("申请岗位");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        headerBox.getChildren().addAll(backButton, titleLabel);

        return headerBox;
    }

    private VBox createJobInfoBox() {
        VBox jobBox = new VBox();
        jobBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        jobBox.setPadding(new Insets(16, 16, 16, 16));
        jobBox.setSpacing(16);

        Label sectionTitle = new Label("岗位信息");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox titleRow = new HBox();
        titleRow.setSpacing(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("岗位名称：");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label titleValue = new Label(currentJob.getPositionName());
        titleValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        titleRow.getChildren().addAll(titleLabel, titleValue);

        HBox courseRow = new HBox();
        courseRow.setSpacing(12);
        courseRow.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("所属课程/活动：");
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label courseValue = new Label(currentJob.getCourseName());
        courseValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        courseRow.getChildren().addAll(courseLabel, courseValue);

        HBox countRow = new HBox();
        countRow.setSpacing(12);
        countRow.setAlignment(Pos.CENTER_LEFT);

        Label countLabel = new Label("招聘人数：");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label countValue = new Label(currentJob.getRecruitmentCount() + "人");
        countValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        countRow.getChildren().addAll(countLabel, countValue);

        HBox requirementRow = new HBox();
        requirementRow.setSpacing(12);
        requirementRow.setAlignment(Pos.CENTER_LEFT);

        Label requirementLabel = new Label("任职要求：");
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label requirementValue = new Label(currentJob.getRequirements());
        requirementValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        requirementRow.getChildren().addAll(requirementLabel, requirementValue);

        HBox deadlineRow = new HBox();
        deadlineRow.setSpacing(12);
        deadlineRow.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("申请截止时间：");
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label deadlineValue = new Label(currentJob.getDeadline());
        deadlineValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        deadlineRow.getChildren().addAll(deadlineLabel, deadlineValue);

        HBox publisherRow = new HBox();
        publisherRow.setSpacing(12);
        publisherRow.setAlignment(Pos.CENTER_LEFT);

        Label publisherLabel = new Label("发布人：");
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

        Label sectionTitle = new Label("个人信息");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox detailsBox = new VBox();
        detailsBox.setSpacing(12);

        HBox nameRow = new HBox();
        nameRow.setSpacing(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("姓名：");
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label nameValue = new Label(currentUser.getName());
        nameValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        nameRow.getChildren().addAll(nameLabel, nameValue);

        HBox idRow = new HBox();
        idRow.setSpacing(12);
        idRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("学号：");
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label idValue = new Label(currentUser.getTAId());
        idValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        idRow.getChildren().addAll(idLabel, idValue);

        HBox majorRow = new HBox();
        majorRow.setSpacing(12);
        majorRow.setAlignment(Pos.CENTER_LEFT);

        Label majorLabel = new Label("专业：");
        majorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label majorValue = new Label(currentUser.getMajor());
        majorValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        majorRow.getChildren().addAll(majorLabel, majorValue);

        HBox emailRow = new HBox();
        emailRow.setSpacing(12);
        emailRow.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("邮箱：");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label emailValue = new Label(currentUser.getEmail());
        emailValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        emailRow.getChildren().addAll(emailLabel, emailValue);

        HBox phoneRow = new HBox();
        phoneRow.setSpacing(12);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        Label phoneLabel = new Label("电话：");
        phoneLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label phoneValue = new Label(currentUser.getPhone());
        phoneValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        phoneRow.getChildren().addAll(phoneLabel, phoneValue);

        HBox skillsRow = new HBox();
        skillsRow.setSpacing(12);
        skillsRow.setAlignment(Pos.CENTER_LEFT);

        Label skillsLabel = new Label("专业技能：");
        skillsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Label skillsValue = new Label(currentUser.getSkill());
        skillsValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        skillsRow.getChildren().addAll(skillsLabel, skillsValue);

        HBox timeRow = new HBox();
        timeRow.setSpacing(12);
        timeRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("可用时间：");
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

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            if (dialogStage != null) {
                dialogStage.close();
                if (dialogCloseListener != null) {
                    dialogCloseListener.onDialogClosed();
                }
            }
        });

        Button submitButton = new Button("提交申请");
        submitButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        submitButton.setOnAction(e -> submitApplication());

        actionBox.getChildren().addAll(cancelButton, submitButton);

        return actionBox;
    }

    private void submitApplication() {
        System.out.println("=== 开始申请流程 ===");
        System.out.println("学生ID: " + currentUser.getTAId());
        System.out.println("岗位ID: " + currentJob.getJobId());

        if (recordManager.hasDuplicateApplication(currentUser.getTAId(), currentJob.getJobId())) {
            System.out.println("检测到重复申请，已阻止");
            showAlert("重复申请", "您已经申请过该岗位，请勿重复申请！");
            return;
        }

        TAApplicationRecord record = new TAApplicationRecord(
            currentUser.getTAId(),
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
        
        showSuccess("申请成功", "您的申请已提交成功，状态为「审核中」！");

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
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
