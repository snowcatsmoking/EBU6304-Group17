package ZiqianCao.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProfileView {

    private TAApplication currentUser;
    private ObjectMapper objectMapper = new ObjectMapper();
    private TAApplicationRecordManager recordManager = new TAApplicationRecordManager();
    private String currentStudentId = "2024999";
    private boolean hasActiveApplication = false;

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    public BorderPane getView() {
        loadUserData(currentStudentId);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        VBox profileForm = createProfileForm();
        content.getChildren().add(profileForm);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        return root;
    }

    private void loadUserData(String studentId) {
        try {
            File file = new File(data.DataConfig.TA_DIR + studentId + ".json");
            if (file.exists()) {
                currentUser = objectMapper.readValue(file, TAApplication.class);
            } else {
                currentUser = new TAApplication("Unknown User", studentId, "Unknown Major", "", "", "", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentUser = new TAApplication("Unknown User", studentId, "Unknown Major", "", "", "", "");
        }
        checkActiveApplication(studentId);
    }

    private void checkActiveApplication(String studentId) {
        List<TAApplicationRecord> records = recordManager.getApplicationsByTA(studentId);
        hasActiveApplication = false;
        for (TAApplicationRecord record : records) {
            String status = record.getStatus();
            if (TAApplicationRecord.STATUS_PENDING.equals(status) || 
                TAApplicationRecord.STATUS_APPROVED.equals(status)) {
                hasActiveApplication = true;
                break;
            }
        }
    }

    private VBox createProfileForm() {
        VBox formBox = new VBox();
        formBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        formBox.setPadding(new Insets(24, 24, 24, 24));
        formBox.setSpacing(20);

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(16);

        Label titleLabel = new Label("My Profile");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        titleBox.getChildren().add(titleLabel);

        HBox statusBox = new HBox();
        statusBox.setPadding(new Insets(12, 16, 12, 16));
        statusBox.setSpacing(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        if (hasActiveApplication) {
            statusBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
            Label statusIcon = new Label("⚠️");
            Label statusText = new Label("You have an active TA application under review. Personal information is locked. Only email can still be updated.");
            statusText.setStyle("-fx-font-size: 12px; -fx-text-fill: #856404;");
            statusBox.getChildren().addAll(statusIcon, statusText);
        } else {
            statusBox.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
            Label statusIcon = new Label("✓");
            Label statusText = new Label("No active applications. You can freely edit all profile information.");
            statusText.setStyle("-fx-font-size: 12px; -fx-text-fill: #155724;");
            statusBox.getChildren().addAll(statusIcon, statusText);
        }

        VBox field1 = createFormField("Name", currentUser != null ? currentUser.getName() : "", "name");
        VBox field2 = createFormField("Student ID", currentUser != null ? currentUser.getTAId() : "", "studentId");
        VBox field3 = createFormField("Major", currentUser != null ? currentUser.getMajor() : "", "major");
        VBox field4 = createFormField("Phone", currentUser != null ? currentUser.getPhone() : "", "phone");
        VBox field5 = createFormField("Email", currentUser != null ? currentUser.getEmail() : "", "email");
        VBox field6 = createFormField("Available Time", currentUser != null ? currentUser.getAvailableTime() : "", "availableTime");
        VBox field7 = createFormField("Skills", currentUser != null ? currentUser.getSkill() : "", "skill");

        FileUploader fileUploader = new FileUploader(currentUser.getTAId());
        VBox fileUploadBox = fileUploader.getUploadComponent();

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(12);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        saveButton.setOnAction(e -> saveProfile());

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> loadUserData(currentStudentId));

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        formBox.getChildren().addAll(titleBox, statusBox, field1, field2, field3, field4, field5, field6, field7, fileUploadBox, buttonBox);

        return formBox;
    }

    private VBox createFormField(String label, String value, String fieldName) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(4);

        HBox labelBox = new HBox();
        labelBox.setAlignment(Pos.CENTER_LEFT);
        labelBox.setSpacing(8);

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-font-weight: 500;");

        boolean isLocked = false;
        String lockHint = "";
        if (hasActiveApplication) {
            switch (fieldName) {
                case "name":
                case "major":
                case "availableTime":
                case "skill":
                case "phone":
                    isLocked = true;
                    lockHint = " (Under review, cannot be modified)";
                    break;
                case "email":
                    lockHint = " (Can be modified)";
                    break;
            }
        }

        if (!lockHint.isEmpty()) {
            Label lockedLabel = new Label(lockHint);
            lockedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");
            labelBox.getChildren().addAll(labelLabel, lockedLabel);
        } else {
            labelBox.getChildren().add(labelLabel);
        }

        TextField valueField = new TextField(value);
        valueField.setStyle("-fx-font-size: 13px; -fx-text-fill: #111111; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 8 12 8 12;");
        valueField.setPrefWidth(400);

        // Add date format hint for available time field
        if (fieldName.equals("availableTime")) {
            valueField.setPromptText("YYYY-MM-DD");
        }

        if (fieldName.equals("studentId") || isLocked) {
            valueField.setDisable(true);
            valueField.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 8 12 8 12;");
        }

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateUserField(fieldName, newValue);
        });

        fieldBox.getChildren().addAll(labelBox, valueField);
        return fieldBox;
    }

    private void updateUserField(String fieldName, String value) {
        if (currentUser == null) {
            currentUser = new TAApplication("", "2024004", "", "", "", "", "");
        }
        switch (fieldName) {
            case "name":
                currentUser.setName(value);
                break;
            case "studentId":
                break;
            case "major":
                currentUser.setMajor(value);
                break;
            case "phone":
                currentUser.setPhone(value);
                break;
            case "email":
                currentUser.setEmail(value);
                break;
            case "availableTime":
                currentUser.setAvailableTime(value);
                break;
            case "skill":
                currentUser.setSkill(value);
                break;
        }
    }

    private void saveProfile() {
        try {
            String fileName = data.DataConfig.TA_DIR + currentUser.getTAId() + ".json";
            objectMapper.writeValue(new File(fileName), currentUser);
            System.out.println("个人档案已保存！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
