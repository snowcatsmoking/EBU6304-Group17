package TA.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NotificationService {

    private TAApplicationRecordManager recordManager;
    private String currentStudentId;
    private Stage primaryStage;

    public NotificationService(TAApplicationRecordManager recordManager, String currentStudentId, Stage primaryStage) {
        this.recordManager = recordManager;
        this.currentStudentId = currentStudentId;
        this.primaryStage = primaryStage;
    }

    public void checkAndShowNotifications() {
        java.util.List<TAApplicationRecord> unnotified = recordManager.getUnnotifiedApplications(currentStudentId);
        if (unnotified.isEmpty()) {
            return;
        }
        TAApplicationRecord app = unnotified.get(0);
        showNotificationDialog(app);
    }

    private void showNotificationDialog(TAApplicationRecord application) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Application Status Update");
        dialogStage.initModality(Modality.NONE);
        dialogStage.setAlwaysOnTop(false);

        VBox dialogVBox = new VBox();
        dialogVBox.setPadding(new Insets(30));
        dialogVBox.setSpacing(20);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: #ffffff;");

        String status = application.getStatus();
        String iconText = "";
        String titleText = "";

        if (TAApplicationRecord.STATUS_APPROVED.equals(status)) {
            iconText = "🎉";
            titleText = "Congratulations!";
        } else if (TAApplicationRecord.STATUS_REJECTED.equals(status)) {
            iconText = "📝";
            titleText = "Application Status";
        }

        Label iconLabel = new Label(iconText);
        iconLabel.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        VBox contentBox = new VBox();
        contentBox.setSpacing(12);
        contentBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("Your application has been " + status + ".");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #333333;");

        Label positionLabel = new Label("Position: " + application.getPositionName());
        positionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label courseLabel = new Label("Course: " + application.getCourseName());
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        contentBox.getChildren().addAll(statusLabel, positionLabel, courseLabel);

        if (application.getReviewComment() != null && !application.getReviewComment().trim().isEmpty()) {
            VBox commentBox = new VBox();
            commentBox.setSpacing(8);
            commentBox.setPadding(new Insets(15));
            commentBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            commentBox.setAlignment(Pos.CENTER_LEFT);

            Label commentTitle = new Label("Review Comment:");
            commentTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #333333;");

            Label commentText = new Label(application.getReviewComment());
            commentText.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-wrap-text: true;");
            commentText.setMaxWidth(350);

            commentBox.getChildren().addAll(commentTitle, commentText);
            contentBox.getChildren().add(commentBox);
        }

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 10 30; -fx-cursor: hand;");
        closeButton.setOnAction(e -> {
            recordManager.markAsNotified(application.getApplicationId());
            dialogStage.close();
            checkAndShowNotifications();
        });

        dialogVBox.getChildren().addAll(iconLabel, titleLabel, contentBox, closeButton);

        Scene dialogScene = new Scene(dialogVBox, 400, 450);
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.show();
    }
}
