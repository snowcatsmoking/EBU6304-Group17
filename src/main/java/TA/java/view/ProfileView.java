package TA.java.view;
import TA.java.TAApplication;
import TA.java.TAApplicationRecordManager;
import TA.java.TAApplicationRecord;
import TA.java.service.FileUploader;
import TA.java.component.TAAlertDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileView {

    private TAApplication currentUser;
    private ObjectMapper objectMapper = new ObjectMapper();
    private TAApplicationRecordManager recordManager = new TAApplicationRecordManager();
    private String currentStudentId = "2024999";
    private boolean hasActiveApplication = false;
    private final Map<String, Label> fieldErrorLabels = new HashMap<>();

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    public ScrollPane getView() {
        loadUserData(currentStudentId);

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        VBox profileForm = createProfileForm();
        content.getChildren().add(profileForm);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    private void loadUserData(String studentId) {
        try {
            File file = new File(data.DataConfig.TA_DIR + studentId + ".json");
            if (file.exists()) {
                currentUser = objectMapper.readValue(file, TAApplication.class);
            } else {
                currentUser = new TAApplication(core.UiText.tr("Unknown User"), studentId, core.UiText.tr("Unknown Major"), "", "", "", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentUser = new TAApplication(core.UiText.tr("Unknown User"), studentId, core.UiText.tr("Unknown Major"), "", "", "", "");
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
        formBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        formBox.setPadding(new Insets(24, 24, 24, 24));
        formBox.setSpacing(20);

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(16);

        Label titleLabel = new Label(core.UiText.tr("My Profile"));
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        titleBox.getChildren().add(titleLabel);

        HBox statusBox = new HBox();
        statusBox.setPadding(new Insets(12, 16, 12, 16));
        statusBox.setSpacing(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        if (hasActiveApplication) {
            statusBox.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
            Label statusIcon = new Label("⚠️");
            Label statusText = new Label(core.UiText.tr("You have an active TA application under review. Personal information is locked. Only email can still be updated."));
            statusText.setStyle("-fx-font-size: 12px; -fx-text-fill: #b45309;");
            statusBox.getChildren().addAll(statusIcon, statusText);
        } else {
            statusBox.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
            Label statusIcon = new Label("✓");
            Label statusText = new Label(core.UiText.tr("No active applications. You can freely edit all profile information."));
            statusText.setStyle("-fx-font-size: 12px; -fx-text-fill: #15803d;");
            statusBox.getChildren().addAll(statusIcon, statusText);
        }

        VBox field1 = createFormField("Name", currentUser != null ? currentUser.getName() : "", "name");
        VBox field2 = createFormField("User ID", currentUser != null ? currentUser.getTAId() : "", "studentId");
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

        Button saveButton = new Button(core.UiText.tr("Save"));
        saveButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        saveButton.setOnMouseEntered(e ->
                saveButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;")
        );
        saveButton.setOnMouseExited(e ->
                saveButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;")
        );
        saveButton.setOnAction(e -> saveProfile());

        Button cancelButton = new Button(core.UiText.tr("Cancel"));
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> loadUserData(currentStudentId));

        Button exportButton = new Button(core.UiText.tr("Export Profile"));
        exportButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #007bff; -fx-border-color: #007bff; -fx-border-width: 1; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        exportButton.setOnAction(e -> exportProfileToJson());

        buttonBox.getChildren().addAll(saveButton, cancelButton, exportButton);

        formBox.getChildren().addAll(titleBox, statusBox, field1, field2, field3, field4, field5, field6, field7, fileUploadBox, buttonBox);

        return formBox;
    }

    private VBox createFormField(String label, String value, String fieldName) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(4);

        HBox labelBox = new HBox();
        labelBox.setAlignment(Pos.CENTER_LEFT);
        labelBox.setSpacing(8);

        Label labelLabel = new Label(core.UiText.tr(label));
        labelLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: 500;");

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
                    lockHint = core.UiText.tr(" (Under review, cannot be modified)");
                    break;
                case "email":
                    lockHint = core.UiText.tr(" (Can be modified)");
                    break;
            }
        }

        if (!lockHint.isEmpty()) {
            Label lockedLabel = new Label(lockHint);
            lockedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            labelBox.getChildren().addAll(labelLabel, lockedLabel);
        } else {
            labelBox.getChildren().add(labelLabel);
        }

        // Shared inline error label for this field
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc2626;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        if (fieldName.equals("availableTime")) {
            fieldBox.getChildren().addAll(labelBox, createAvailableTimePicker(value, isLocked));
            return fieldBox;
        }

        if (fieldName.equals("skill")) {
            javafx.scene.control.TextArea valueArea = new javafx.scene.control.TextArea(value);
            valueArea.setWrapText(true);
            valueArea.setPrefRowCount(4);
            valueArea.setPrefWidth(400);
            valueArea.setMinHeight(Region.USE_PREF_SIZE);
            valueArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 8px 12px;");
            if (isLocked) {
                valueArea.setDisable(true);
                valueArea.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-background-color: #f8fafc; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 8px 12px;");
            }
            valueArea.textProperty().addListener((observable, oldValue, newValue) -> {
                updateUserField(fieldName, newValue);
            });
            fieldBox.getChildren().addAll(labelBox, valueArea);
            return fieldBox;
        }

        TextField valueField = new TextField(value);
        valueField.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 10px 16px;");
        valueField.setPrefWidth(400);

        if (fieldName.equals("studentId") || isLocked) {
            valueField.setDisable(true);
            valueField.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-background-color: #f8fafc; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 10px 16px;");
            fieldBox.getChildren().addAll(labelBox, valueField);
            return fieldBox;
        }

        // Register error label so saveProfile() can trigger it
        fieldErrorLabels.put(fieldName, errorLabel);

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateUserField(fieldName, newValue);
            String error = validateField(fieldName, newValue);
            errorLabel.setText(error != null ? error : "");
            errorLabel.setVisible(error != null);
            errorLabel.setManaged(error != null);
        });

        fieldBox.getChildren().addAll(labelBox, valueField, errorLabel);
        return fieldBox;
    }

    private VBox createAvailableTimePicker(String value, boolean isLocked) {
        VBox container = new VBox();
        container.setSpacing(4);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(400);
        datePicker.getStyleClass().add("ta-date-picker");

        if (!value.isEmpty()) {
            try {
                datePicker.setValue(LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
            }
        }

        if (isLocked) {
            datePicker.setDisable(true);
            datePicker.getStyleClass().add("ta-date-picker-locked");
        } else {
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });
        }

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUserField("availableTime", newValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        container.getChildren().add(datePicker);
        return container;
    }

    /**
     * Validates a single field's value (format only — empty is allowed for draft saving).
     * Returns null if valid, or a localised error message if invalid.
     */
    private String validateField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) return null; // empty = OK for save
        switch (fieldName) {
            case "name":
                return value.trim().length() < 2
                        ? core.UiText.tr("Name must be at least 2 characters") : null;
            case "phone":
                return value.trim().matches("\\d{11}")
                        ? null : core.UiText.tr("Phone must be exactly 11 digits");
            case "email":
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                return value.matches(emailRegex)
                        ? null : core.UiText.tr("Invalid email format (e.g. user@example.com)");
            default:
                return null;
        }
    }

    /** Returns the current value of a field from the model. */
    private String getFieldValue(String fieldName) {
        if (currentUser == null) return "";
        switch (fieldName) {
            case "name":          return currentUser.getName()          != null ? currentUser.getName()          : "";
            case "phone":         return currentUser.getPhone()         != null ? currentUser.getPhone()         : "";
            case "email":         return currentUser.getEmail()         != null ? currentUser.getEmail()         : "";
            case "major":         return currentUser.getMajor()         != null ? currentUser.getMajor()         : "";
            case "skill":         return currentUser.getSkill()         != null ? currentUser.getSkill()         : "";
            case "availableTime": return currentUser.getAvailableTime() != null ? currentUser.getAvailableTime() : "";
            default:              return "";
        }
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
        // Run format validation on all registered fields and show inline errors
        boolean hasFormatErrors = false;
        for (Map.Entry<String, Label> entry : fieldErrorLabels.entrySet()) {
            String fieldName = entry.getKey();
            String value     = getFieldValue(fieldName);
            String error     = validateField(fieldName, value);
            Label  errLbl    = entry.getValue();
            errLbl.setText(error != null ? error : "");
            errLbl.setVisible(error != null);
            errLbl.setManaged(error != null);
            if (error != null) hasFormatErrors = true;
        }

        if (hasFormatErrors) {
            TAAlertDialog.showError(null,
                    core.UiText.tr("Validation Error"),
                    core.UiText.tr("Please fix the highlighted errors before saving."),
                    core.UiText.tr("Fields marked in red contain invalid values."));
            return;
        }

        // Partial profiles (empty required fields) are allowed to save as drafts.
        // Completeness is enforced at application time via TAApplicationUtils.checkProfileComplete().
        try {
            objectMapper.writeValue(
                    new File(data.DataConfig.TA_DIR + currentUser.getTAId() + ".json"),
                    currentUser);
            TAAlertDialog.showSuccess(null,
                    core.UiText.tr("Save Successful"),
                    core.UiText.tr("Your profile changes have been saved."));
        } catch (IOException e) {
            e.printStackTrace();
            TAAlertDialog.showError(null,
                    core.UiText.tr("Save Failed"),
                    core.UiText.tr("Failed to save profile"),
                    core.UiText.tr("Please try again or check whether the profile file is writable."));
        }
    }

    private void exportProfileToJson() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "profile_export_" + currentUser.getTAId() + "_" + timestamp + ".json";
            String filePath = data.DataConfig.TA_DIR + fileName;

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), currentUser);

            TAAlertDialog.showExportSuccess(null, "Export Successful", "File saved to:\n" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            TAAlertDialog.showExportError(null, "Export Failed", "An error occurred while exporting. Please try again.");
        }
    }
}
