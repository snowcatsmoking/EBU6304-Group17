package ZiqianCao.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;

public class DashboardView {

    public interface NavigationListener {
        void onNavigateToProfile();
    }

    public interface LogoutListener {
        void onLogout();
    }

    private TAApplication currentUser;
    private TAApplicationRecordManager recordManager;
    private ObjectMapper objectMapper = new ObjectMapper();
    private NavigationListener navigationListener;
    private LogoutListener logoutListener;
    private String currentStudentId = "2024999";

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    public BorderPane getView() {
        loadUserData(currentStudentId);
        recordManager = new TAApplicationRecordManager();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox headerBox = createHeaderBox();
        HBox statsBox = createStatsBox();
        VBox profileBox = createProfileBox();
        VBox applicationsBox = createApplicationsBox();

        content.getChildren().addAll(headerBox, statsBox, profileBox, applicationsBox);
        root.setCenter(content);

        return root;
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
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
    }

    private HBox createHeaderBox() {
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Label welcomeLabel = new Label("Welcome back, ");
        welcomeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label nameLabel = new Label(currentUser != null ? currentUser.getName() : "Unknown User");
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label roleLabel = new Label("(TA)");
        roleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-background-color: #f0f0f0; -fx-padding: 2 8 2 8; -fx-border-radius: 2; -fx-background-radius: 2;");

        Label idLabel = new Label(currentUser != null ? currentUser.getTAId() : "");
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");

        HBox userInfoBox = new HBox();
        userInfoBox.setSpacing(8);
        userInfoBox.setAlignment(Pos.CENTER);
        userInfoBox.getChildren().addAll(welcomeLabel, nameLabel, roleLabel, idLabel);

        headerBox.getChildren().add(userInfoBox);

        return headerBox;
    }

    private HBox createStatsBox() {
        HBox statsBox = new HBox();
        statsBox.setSpacing(16);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox stat1 = createStatBox("3", "Applications Submitted");
        VBox stat2 = createStatBox("1", "Approved");
        VBox stat3 = createStatBox("1", "Under Review");
        VBox stat4 = createStatBox("5", "Available Positions");

        statsBox.getChildren().addAll(stat1, stat2, stat3, stat4);

        return statsBox;
    }

    private VBox createStatBox(String number, String description) {
        VBox statBox = new VBox();
        statBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        statBox.setPadding(new Insets(16, 16, 16, 16));
        statBox.setSpacing(4);
        statBox.setAlignment(Pos.CENTER);
        statBox.setPrefWidth(180);

        Label numLabel = new Label(number);
        numLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        statBox.getChildren().addAll(numLabel, descLabel);

        return statBox;
    }

    private VBox createProfileBox() {
        VBox profileBox = new VBox();
        profileBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        profileBox.setPadding(new Insets(24, 24, 24, 24));
        profileBox.setSpacing(16);

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(16);

        Label titleLabel = new Label("Profile Summary");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Button editButton = new Button("Edit Profile");
        editButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-underline: true; -fx-background-color: transparent; -fx-border-width: 0; -fx-cursor: hand;");
        editButton.setOnAction(e -> {
            if (navigationListener != null) {
                navigationListener.onNavigateToProfile();
            }
        });

        titleBox.getChildren().addAll(titleLabel, editButton);

        VBox singleColumn = new VBox();
        singleColumn.setSpacing(12);

        addProfileItem(singleColumn, "Name", currentUser != null ? currentUser.getName() : "");
        addProfileItem(singleColumn, "Student ID", currentUser != null ? currentUser.getTAId() : "");
        addProfileItem(singleColumn, "Major", currentUser != null ? currentUser.getMajor() : "");
        addProfileItem(singleColumn, "Phone", currentUser != null ? currentUser.getPhone() : "");
        addProfileItem(singleColumn, "Email", currentUser != null ? currentUser.getEmail() : "");
        addProfileItem(singleColumn, "Available Time", currentUser != null ? currentUser.getAvailableTime() : "");
        addProfileItem(singleColumn, "Skills", currentUser != null ? currentUser.getSkill() : "");

        profileBox.getChildren().addAll(titleBox, singleColumn);

        return profileBox;
    }

    private void addProfileItem(VBox column, String label, String value) {
        HBox itemBox = new HBox();
        itemBox.setSpacing(20);
        itemBox.setAlignment(Pos.CENTER_LEFT);

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");
        labelLabel.setPrefWidth(100);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #111111;");

        itemBox.getChildren().addAll(labelLabel, valueLabel);
        column.getChildren().add(itemBox);
    }

    private VBox createApplicationsBox() {
        VBox applicationsBox = new VBox();
        applicationsBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        applicationsBox.setPadding(new Insets(24, 24, 24, 24));
        applicationsBox.setSpacing(16);

        Label titleLabel = new Label("Recent Applications");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        HBox tableHeader = createTableHeader();
        VBox tableContent = createTableContent();

        applicationsBox.getChildren().addAll(titleLabel, tableHeader, tableContent);

        return applicationsBox;
    }

    private HBox createTableHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(8, 12, 8, 12));
        header.setSpacing(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label header1 = new Label("Position");
        header1.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        header1.setPrefWidth(300);

        Label header2 = new Label("Applied On");
        header2.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        header2.setPrefWidth(120);

        Label header3 = new Label("Status");
        header3.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        header3.setPrefWidth(100);

        Label header4 = new Label("Actions");
        header4.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        header4.setPrefWidth(100);

        header.getChildren().addAll(header1, header2, header3, header4);

        return header;
    }

    private VBox createTableContent() {
        VBox content = new VBox();
        content.setSpacing(0);

        java.util.List<TAApplicationRecord> applications = recordManager.getApplicationsByStudentId(currentStudentId);

        java.util.Collections.sort(applications, (a1, a2) -> {
            if (a1.getApplicationDate() != null && a2.getApplicationDate() != null) {
                return a2.getApplicationDate().compareTo(a1.getApplicationDate());
            }
            return 0;
        });

        int count = 0;
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        for (TAApplicationRecord record : applications) {
            if (count >= 3) break;

            String status = getStatusDisplay(record.getStatus());
            String statusType = getStatusType(record.getStatus());
            boolean canWithdraw = TAApplicationRecord.STATUS_PENDING.equals(record.getStatus());

            HBox row = createTableRow(
                record.getPositionName(),
                dateFormat.format(record.getApplicationDate()),
                status,
                statusType,
                canWithdraw
            );
            content.getChildren().add(row);
            count++;
        }

        if (content.getChildren().isEmpty()) {
            HBox emptyRow = new HBox();
            emptyRow.setPadding(new Insets(20, 12, 20, 12));
            emptyRow.setAlignment(Pos.CENTER);
            Label emptyLabel = new Label("No application records");
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");
            emptyRow.getChildren().add(emptyLabel);
            content.getChildren().add(emptyRow);
        }

        return content;
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "Under Review";
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

    private String getStatusType(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return "pending";
            case TAApplicationRecord.STATUS_APPROVED:
                return "pass";
            case TAApplicationRecord.STATUS_REJECTED:
                return "fail";
            case TAApplicationRecord.STATUS_WITHDRAWN:
                return "withdrawn";
            default:
                return "default";
        }
    }

    private HBox createTableRow(String position, String date, String status, String statusType, boolean canWithdraw) {
        HBox row = new HBox();
        row.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setSpacing(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label positionLabel = new Label(position);
        positionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        positionLabel.setPrefWidth(300);

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        dateLabel.setPrefWidth(120);

        Label statusLabel = new Label(status);
        statusLabel.setPrefWidth(100);

        switch (statusType) {
            case "pending":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b08800; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 2 10 2 10; -fx-background-color: #fffbe6;");
                break;
            case "pass":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #008800; -fx-border-color: #aaccaa; -fx-border-width: 1; -fx-padding: 2 10 2 10; -fx-background-color: #efffef;");
                break;
            case "fail":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000; -fx-border-color: #eeaaaa; -fx-border-width: 1; -fx-padding: 2 10 2 10; -fx-background-color: #fff0f0;");
                break;
            case "withdrawn":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 2 10 2 10; -fx-background-color: #f5f5f5;");
                break;
            default:
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 2 10 2 10; -fx-background-color: #f5f5f5;");
                break;
        }

        HBox actionBox = new HBox();
        actionBox.setPrefWidth(100);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        if (canWithdraw) {
            Button withdrawButton = new Button("Withdraw");
            withdrawButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff; -fx-padding: 4 12 4 12; -fx-cursor: hand;");
            actionBox.getChildren().add(withdrawButton);
        } else {
            Label noActionLabel = new Label("—");
            noActionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");
            actionBox.getChildren().add(noActionLabel);
        }

        row.getChildren().addAll(positionLabel, dateLabel, statusLabel, actionBox);

        return row;
    }
}
