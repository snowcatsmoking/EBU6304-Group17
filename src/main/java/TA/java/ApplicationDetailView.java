package TA.java;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
        dialogStage.setTitle("Application Details");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);

        VBox content = new VBox();
        content.setPadding(new Insets(30, 30, 30, 30));
        content.setSpacing(20);

        Label titleLabel = new Label("Application Details");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        VBox detailBox = new VBox();
        detailBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4); -fx-padding: 20;");
        detailBox.setSpacing(15);

        // Position Information
        Label positionTitle = new Label("Position Information");
        positionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        HBox positionNameBox = new HBox();
        positionNameBox.setSpacing(10);
        Label positionNameLabel = new Label("Position Name:");
        positionNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label positionNameValue = new Label(record.getPositionName());
        positionNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        positionNameBox.getChildren().addAll(positionNameLabel, positionNameValue);

        HBox courseNameBox = new HBox();
        courseNameBox.setSpacing(10);
        Label courseNameLabel = new Label("Course:");
        courseNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label courseNameValue = new Label(record.getCourseName());
        courseNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        courseNameBox.getChildren().addAll(courseNameLabel, courseNameValue);

        HBox moduleCodeBox = new HBox();
        moduleCodeBox.setSpacing(10);
        Label moduleCodeLabel = new Label("Module Code:");
        moduleCodeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label moduleCodeValue = new Label(record.getModuleCode());
        moduleCodeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        moduleCodeBox.getChildren().addAll(moduleCodeLabel, moduleCodeValue);

        // Application Information
        Label applicationTitle = new Label("Application Information");
        applicationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b; -fx-margin: 20 0 0 0;");

        HBox applicationDateBox = new HBox();
        applicationDateBox.setSpacing(10);
        Label applicationDateLabel = new Label("Application Time:");
        applicationDateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label applicationDateValue = new Label(dateFormat.format(record.getApplicationDate()));
        applicationDateValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        applicationDateBox.getChildren().addAll(applicationDateLabel, applicationDateValue);

        HBox statusBox = new HBox();
        statusBox.setSpacing(10);
        Label statusLabel = new Label("Application Status:");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label statusValue = new Label(getStatusDisplay(record.getStatus()));
        statusValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        // Review Information
        Label reviewTitle = new Label("Review Information");
        reviewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b; -fx-margin: 20 0 0 0;");

        HBox reviewerBox = new HBox();
        reviewerBox.setSpacing(10);
        Label reviewerLabel = new Label("Reviewer:");
        reviewerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label reviewerValue = new Label(record.getReviewer() != null ? record.getReviewer() : "None");
        reviewerValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        reviewerBox.getChildren().addAll(reviewerLabel, reviewerValue);

        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        Label commentLabel = new Label("Review Comments:");
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        Label commentValue = new Label(record.getReviewComment() != null ? record.getReviewComment() : "None");
        commentValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        commentBox.getChildren().addAll(commentLabel, commentValue);

        // Personal Information
        Label personalTitle = new Label("Personal Information");
        personalTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #1e293b; -fx-margin: 20 0 0 0;");

        HBox studentNameBox = new HBox();
        studentNameBox.setSpacing(10);
        Label studentNameLabel = new Label("Name:");
        studentNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label studentNameValue = new Label(record.getStudentName());
        studentNameValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        studentNameBox.getChildren().addAll(studentNameLabel, studentNameValue);

        HBox majorBox = new HBox();
        majorBox.setSpacing(10);
        Label majorLabel = new Label("Major:");
        majorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label majorValue = new Label(record.getMajor());
        majorValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        majorBox.getChildren().addAll(majorLabel, majorValue);

        HBox phoneBox = new HBox();
        phoneBox.setSpacing(10);
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label phoneValue = new Label(record.getPhone());
        phoneValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        phoneBox.getChildren().addAll(phoneLabel, phoneValue);

        HBox emailBox = new HBox();
        emailBox.setSpacing(10);
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label emailValue = new Label(record.getEmail());
        emailValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        emailBox.getChildren().addAll(emailLabel, emailValue);

        HBox availableTimeBox = new HBox();
        availableTimeBox.setSpacing(10);
        Label availableTimeLabel = new Label("Available Time:");
        availableTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label availableTimeValue = new Label(record.getAvailableTime());
        availableTimeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        availableTimeBox.getChildren().addAll(availableTimeLabel, availableTimeValue);

        HBox skillsBox = new HBox();
        skillsBox.setSpacing(10);
        Label skillsLabel = new Label("Skills:");
        skillsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label skillsValue = new Label(record.getSkills());
        skillsValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");
        skillsBox.getChildren().addAll(skillsLabel, skillsValue);

        HBox keywordsBox = new HBox();
        keywordsBox.setSpacing(10);
        Label keywordsLabel = new Label("Keywords:");
        keywordsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-min-width: 100px;");
        Label keywordsValue = new Label(SkillUtils.toDisplayText(record.getEffectiveKeywords(), "No keywords extracted"));
        keywordsValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        keywordsValue.setWrapText(true);
        keywordsBox.getChildren().addAll(keywordsLabel, keywordsValue);

        VBox resumeBox = new VBox();
        resumeBox.setSpacing(5);
        Label resumeLabel = new Label("Resume Text:");
        resumeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        Label resumeValue = new Label(record.getResumeText() == null || record.getResumeText().trim().isEmpty()
            ? "No resume text provided"
            : record.getResumeText().trim());
        resumeValue.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        resumeValue.setWrapText(true);
        resumeBox.getChildren().addAll(resumeLabel, resumeValue);

        detailBox.getChildren().addAll(
                positionTitle, positionNameBox, courseNameBox, moduleCodeBox,
                applicationTitle, applicationDateBox, statusBox,
                reviewTitle, reviewerBox, commentBox,
                personalTitle, studentNameBox, majorBox, phoneBox, emailBox, availableTimeBox, skillsBox, keywordsBox, resumeBox
        );

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        closeButton.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().add(closeButton);

        content.getChildren().addAll(titleLabel, detailBox, buttonBox);

        scrollPane.setContent(content);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 600, 700);
        dialogStage.setScene(scene);
        dialogStage.setOnShown(e -> Platform.runLater(() -> scrollPane.setVvalue(0)));
        dialogStage.showAndWait();
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "Pending";
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
}
