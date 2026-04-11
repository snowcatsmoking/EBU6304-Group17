package ZiqianCao.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyApplicationsView {

    private String currentStudentId = "2024999";
    private TAApplicationRecordManager recordManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MyApplicationsView() {
    }

    public MyApplicationsView(String studentId) {
        this.currentStudentId = studentId;
    }

    public BorderPane getView() {
        recordManager = new TAApplicationRecordManager();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(40, 40, 40, 40));
        content.setSpacing(20);

        Label titleLabel = new Label("My Applications");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        List<TAApplicationRecord> applications = recordManager.getApplicationsByStudentId(currentStudentId);
        
        java.util.Collections.sort(applications, (a1, a2) -> {
            if (a1.getApplicationDate() != null && a2.getApplicationDate() != null) {
                return a2.getApplicationDate().compareTo(a1.getApplicationDate());
            }
            return 0;
        });
        
        if (applications.isEmpty()) {
            Label emptyLabel = new Label("No application records");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
            content.getChildren().addAll(titleLabel, emptyLabel);
        } else {
            HBox tableHeader = createTableHeader();
            VBox tableContent = createTableContent(applications);
            
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(tableContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            
            content.getChildren().addAll(titleLabel, tableHeader, scrollPane);
        }

        root.setCenter(content);

        return root;
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

    private VBox createTableContent(List<TAApplicationRecord> applications) {
        VBox content = new VBox();
        content.setSpacing(0);

        for (TAApplicationRecord record : applications) {
            String statusType = getStatusType(record.getStatus());
            boolean canWithdraw = TAApplicationRecord.STATUS_PENDING.equals(record.getStatus());
            HBox row = createTableRow(
                record.getPositionName(),
                dateFormat.format(record.getApplicationDate()),
                getStatusDisplay(record.getStatus()),
                statusType,
                canWithdraw,
                record.getApplicationId()
            );
            content.getChildren().add(row);
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

    private HBox createTableRow(String position, String date, String status, String statusType, boolean canWithdraw, String applicationId) {
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
            javafx.scene.control.Button withdrawButton = new javafx.scene.control.Button("Withdraw");
            withdrawButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #ffffff; -fx-padding: 4 12 4 12; -fx-cursor: hand;");
            withdrawButton.setOnAction(e -> {
                boolean success = recordManager.withdrawApplication(applicationId);
                if (success) {
                    System.out.println("成功撤回申请: " + applicationId);
                    if (row.getScene() != null && row.getScene().getRoot() != null) {
                        BorderPane newView = getView();
                        javafx.scene.Parent root = row.getScene().getRoot();
                        if (root instanceof javafx.scene.layout.StackPane) {
                            javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) root;
                            for (javafx.scene.Node node : stackPane.getChildren()) {
                                if (node instanceof BorderPane) {
                                    ((BorderPane) node).setCenter(newView.getCenter());
                                    break;
                                }
                            }
                        } else if (root instanceof BorderPane) {
                            ((BorderPane) root).setCenter(newView.getCenter());
                        }
                    }
                } else {
                    System.out.println("撤回失败: " + applicationId);
                }
            });
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
