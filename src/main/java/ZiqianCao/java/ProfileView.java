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

public class ProfileView {

    private TAApplication currentUser;
    private ObjectMapper objectMapper = new ObjectMapper();

    public BorderPane getView() {
        loadUserData("2024999");
        
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
            File file = new File("resources/Data/TAData/" + studentId + ".json");
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

    private VBox createProfileForm() {
        VBox formBox = new VBox();
        formBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        formBox.setPadding(new Insets(24, 24, 24, 24));
        formBox.setSpacing(20);

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(16);

        Label titleLabel = new Label("个人档案");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        titleBox.getChildren().add(titleLabel);

        VBox field1 = createFormField("姓名", currentUser != null ? currentUser.getName() : "", "name");
        VBox field2 = createFormField("学号", currentUser != null ? currentUser.getTAId() : "", "studentId");
        VBox field3 = createFormField("专业", currentUser != null ? currentUser.getMajor() : "", "major");
        VBox field4 = createFormField("联系电话", currentUser != null ? currentUser.getPhone() : "", "phone");
        VBox field5 = createFormField("邮箱", currentUser != null ? currentUser.getEmail() : "", "email");
        VBox field6 = createFormField("可任职时间", currentUser != null ? currentUser.getAvailableTime() : "", "availableTime");
        VBox field7 = createFormField("技能", currentUser != null ? currentUser.getSkill() : "", "skill");

        FileUploader fileUploader = new FileUploader(currentUser.getTAId());
        VBox fileUploadBox = fileUploader.getUploadComponent();

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(12);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));

        Button saveButton = new Button("保存");
        saveButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        saveButton.setOnAction(e -> saveProfile());

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> loadUserData("2024999"));

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        formBox.getChildren().addAll(titleBox, field1, field2, field3, field4, field5, field6, field7, fileUploadBox, buttonBox);

        return formBox;
    }

    private VBox createFormField(String label, String value, String fieldName) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(4);

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-font-weight: 500;");

        TextField valueField = new TextField(value);
        valueField.setStyle("-fx-font-size: 13px; -fx-text-fill: #111111; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 8 12 8 12;");
        valueField.setPrefWidth(400);

        if (fieldName.equals("studentId")) {
            valueField.setDisable(true);
            valueField.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 8 12 8 12;");
        }

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateUserField(fieldName, newValue);
        });

        fieldBox.getChildren().addAll(labelLabel, valueField);
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
            String fileName = "resources/Data/TAData/" + currentUser.getTAId() + ".json";
            objectMapper.writeValue(new File(fileName), currentUser);
            System.out.println("个人档案已保存！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
