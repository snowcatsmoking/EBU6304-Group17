package ZiqianCao.java;

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

import java.text.SimpleDateFormat;

public class ApplicationDetailView {

    private TAApplicationRecord record;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ApplicationDetailView(TAApplicationRecord record) {
        this.record = record;
    }

    public void showDialog(Stage primaryStage) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setTitle("申请详情");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(30, 30, 30, 30));
        content.setSpacing(20);

        Label titleLabel = new Label("申请详情");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox detailBox = new VBox();
        detailBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 20;");
        detailBox.setSpacing(15);

        // 岗位信息
        Label positionTitle = new Label("岗位信息");
        positionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        HBox positionNameBox = new HBox();
        positionNameBox.setSpacing(10);
        Label positionNameLabel = new Label("岗位名称:");
        positionNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label positionNameValue = new Label(record.getPositionName());
        positionNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        positionNameBox.getChildren().addAll(positionNameLabel, positionNameValue);

        HBox courseNameBox = new HBox();
        courseNameBox.setSpacing(10);
        Label courseNameLabel = new Label("所属课程:");
        courseNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label courseNameValue = new Label(record.getCourseName());
        courseNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        courseNameBox.getChildren().addAll(courseNameLabel, courseNameValue);

        HBox moduleCodeBox = new HBox();
        moduleCodeBox.setSpacing(10);
        Label moduleCodeLabel = new Label("课程代码:");
        moduleCodeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label moduleCodeValue = new Label(record.getModuleCode());
        moduleCodeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        moduleCodeBox.getChildren().addAll(moduleCodeLabel, moduleCodeValue);

        // 申请信息
        Label applicationTitle = new Label("申请信息");
        applicationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-margin: 20 0 0 0;");

        HBox applicationDateBox = new HBox();
        applicationDateBox.setSpacing(10);
        Label applicationDateLabel = new Label("申请时间:");
        applicationDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label applicationDateValue = new Label(dateFormat.format(record.getApplicationDate()));
        applicationDateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        applicationDateBox.getChildren().addAll(applicationDateLabel, applicationDateValue);

        HBox statusBox = new HBox();
        statusBox.setSpacing(10);
        Label statusLabel = new Label("申请状态:");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label statusValue = new Label(getStatusDisplay(record.getStatus()));
        statusValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        // 审核信息
        Label reviewTitle = new Label("审核信息");
        reviewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-margin: 20 0 0 0;");

        HBox reviewerBox = new HBox();
        reviewerBox.setSpacing(10);
        Label reviewerLabel = new Label("审核人:");
        reviewerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label reviewerValue = new Label(record.getReviewer() != null ? record.getReviewer() : "无");
        reviewerValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        reviewerBox.getChildren().addAll(reviewerLabel, reviewerValue);

        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        Label commentLabel = new Label("审核意见:");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        Label commentValue = new Label(record.getReviewComment() != null ? record.getReviewComment() : "无");
        commentValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-wrap-text: true;");
        commentBox.getChildren().addAll(commentLabel, commentValue);

        // 个人信息
        Label personalTitle = new Label("个人信息");
        personalTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-margin: 20 0 0 0;");

        HBox studentNameBox = new HBox();
        studentNameBox.setSpacing(10);
        Label studentNameLabel = new Label("姓名:");
        studentNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label studentNameValue = new Label(record.getStudentName());
        studentNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        studentNameBox.getChildren().addAll(studentNameLabel, studentNameValue);

        HBox majorBox = new HBox();
        majorBox.setSpacing(10);
        Label majorLabel = new Label("专业:");
        majorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label majorValue = new Label(record.getMajor());
        majorValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        majorBox.getChildren().addAll(majorLabel, majorValue);

        HBox phoneBox = new HBox();
        phoneBox.setSpacing(10);
        Label phoneLabel = new Label("联系电话:");
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label phoneValue = new Label(record.getPhone());
        phoneValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        phoneBox.getChildren().addAll(phoneLabel, phoneValue);

        HBox emailBox = new HBox();
        emailBox.setSpacing(10);
        Label emailLabel = new Label("邮箱:");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label emailValue = new Label(record.getEmail());
        emailValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        emailBox.getChildren().addAll(emailLabel, emailValue);

        HBox availableTimeBox = new HBox();
        availableTimeBox.setSpacing(10);
        Label availableTimeLabel = new Label("可任职时间:");
        availableTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label availableTimeValue = new Label(record.getAvailableTime());
        availableTimeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        availableTimeBox.getChildren().addAll(availableTimeLabel, availableTimeValue);

        HBox skillsBox = new HBox();
        skillsBox.setSpacing(10);
        Label skillsLabel = new Label("专业技能:");
        skillsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-min-width: 100px;");
        Label skillsValue = new Label(record.getSkills());
        skillsValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        skillsBox.getChildren().addAll(skillsLabel, skillsValue);

        detailBox.getChildren().addAll(
                positionTitle, positionNameBox, courseNameBox, moduleCodeBox,
                applicationTitle, applicationDateBox, statusBox,
                reviewTitle, reviewerBox, commentBox,
                personalTitle, studentNameBox, majorBox, phoneBox, emailBox, availableTimeBox, skillsBox
        );

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        Button closeButton = new Button("关闭");
        closeButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        closeButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().add(closeButton);

        content.getChildren().addAll(titleLabel, detailBox, buttonBox);

        root.setCenter(content);

        Scene scene = new Scene(root, 600, 700);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "审核中";
            case TAApplicationRecord.STATUS_APPROVED:
                return "已通过";
            case TAApplicationRecord.STATUS_REJECTED:
                return "已拒绝";
            case TAApplicationRecord.STATUS_WITHDRAWN:
                return "已撤回";
            default:
                return status;
        }
    }
}