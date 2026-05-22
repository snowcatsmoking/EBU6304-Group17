package TA.java.view;
import TA.java.TAApplicationRecordManager;
import TA.java.TAApplicationRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class MyApplicationsView {

    private String currentStudentId = "2024999";
    private TAApplicationRecordManager recordManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private DatePicker availableTimeField;
    private List<TAApplicationRecord> allApplications;
    private List<TAApplicationRecord> currentApplications;
    private VBox tableContentBox;
    private Label emptyLabel;
    private ScrollPane scrollPane;

    public MyApplicationsView() {
    }

    public MyApplicationsView(String studentId) {
        this.currentStudentId = studentId;
    }

    public ScrollPane getView() {
        recordManager = new TAApplicationRecordManager();

        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        Label titleLabel = new Label(core.UiText.tr("My Applications"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        // 添加时间筛选功能
        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label availableTimeLabel = new Label(core.UiText.tr("Available Time:"));
        availableTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        availableTimeField = new DatePicker();
        availableTimeField.setPromptText(core.UiText.tr("Select date"));
        availableTimeField.getStyleClass().add("ta-date-picker");
        availableTimeField.setPrefWidth(190);

        Button filterButton = new Button(core.UiText.tr("Filter"));
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        filterButton.setOnAction(e -> applyDateFilter());

        Button resetButton = new Button(core.UiText.tr("Reset"));
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        resetButton.setOnAction(e -> resetDateFilter());

        filterBox.getChildren().addAll(availableTimeLabel, availableTimeField, filterButton, resetButton);

        loadApplicationsFromStore();

        HBox tableHeader = createTableHeader();

        // 创建表格内容容器
        tableContentBox = new VBox();
        tableContentBox.setSpacing(0);

        // 创建空标签
        emptyLabel = new Label(core.UiText.tr("No application records"));
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        emptyLabel.setPadding(new Insets(40, 0, 40, 0));

        // 创建滚动面板
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 刷新表格内容
        refreshTableContent();

        content.getChildren().addAll(titleLabel, filterBox, tableHeader, scrollPane);

        ScrollPane outerScrollPane = new ScrollPane();
        outerScrollPane.setContent(content);
        outerScrollPane.setFitToWidth(true);
        outerScrollPane.setFitToHeight(true);
        outerScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        outerScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        outerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return outerScrollPane;
    }

    private void loadApplicationsFromStore() {
        allApplications = recordManager.getApplicationsByStudentId(currentStudentId);

        java.util.Collections.sort(allApplications, (a1, a2) -> {
            if (a1.getApplicationDate() != null && a2.getApplicationDate() != null) {
                return a2.getApplicationDate().compareTo(a1.getApplicationDate());
            }
            return 0;
        });

        currentApplications = new java.util.ArrayList<>(allApplications);
    }

    private void refreshAfterWithdrawal() {
        loadApplicationsFromStore();
        if (availableTimeField != null && availableTimeField.getValue() != null) {
            applyDateFilter();
            return;
        }
        emptyLabel.setText(core.UiText.tr("No application records"));
        refreshTableContent();
    }

    private void refreshTableContent() {
        tableContentBox.getChildren().clear();

        if (currentApplications.isEmpty()) {
            // 显示空标签
            scrollPane.setContent(emptyLabel);
        } else {
            // 显示表格内容
            for (TAApplicationRecord record : currentApplications) {
                String statusType = getStatusType(record.getStatus());
                boolean canWithdraw = TAApplicationRecord.STATUS_PENDING.equals(record.getStatus());
                HBox row = createTableRow(
                        record.getPositionName(),
                        dateFormat.format(record.getApplicationDate()),
                        getStatusDisplay(record.getStatus()),
                        statusType,
                        canWithdraw,
                        record.getApplicationId(),
                        record
                );
                tableContentBox.getChildren().add(row);
            }
            scrollPane.setContent(tableContentBox);
        }
    }

    private void applyDateFilter() {
        LocalDate inputDate = availableTimeField.getValue();

        List<TAApplicationRecord> filteredApplications = new java.util.ArrayList<>();

        for (TAApplicationRecord record : allApplications) {
            boolean match = true;

            if (inputDate != null) {
                java.time.LocalDate applicationDate = record.getApplicationDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (applicationDate.isAfter(inputDate)) {
                    match = false;
                }
            }

            if (match) {
                filteredApplications.add(record);
            }
        }

        currentApplications = filteredApplications;

        if (currentApplications.isEmpty()) {
            emptyLabel.setText(core.UiText.tr("No matching applications"));
        }

        refreshTableContent();
    }

    private void resetDateFilter() {
        availableTimeField.setValue(null);
        currentApplications = new java.util.ArrayList<>(allApplications);
        emptyLabel.setText(core.UiText.tr("No application records"));
        refreshTableContent();
    }

    private HBox createTableHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(8, 12, 8, 12));
        header.setSpacing(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label header1 = new Label(core.UiText.tr("Position"));
        header1.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        header1.setPrefWidth(300);

        Label header2 = new Label(core.UiText.tr("Applied On"));
        header2.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        header2.setPrefWidth(120);

        Label header3 = new Label(core.UiText.tr("Status"));
        header3.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        header3.setPrefWidth(100);

        Label header4 = new Label(core.UiText.tr("Actions"));
        header4.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        header4.setPrefWidth(100);

        header.getChildren().addAll(header1, header2, header3, header4);

        return header;
    }

    private HBox createTableRow(String position, String date, String status, String statusType, boolean canWithdraw, String applicationId, TAApplicationRecord record) {
        HBox row = new HBox();
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setSpacing(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label positionLabel = new Label(position);
        positionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        positionLabel.setPrefWidth(300);

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        dateLabel.setPrefWidth(120);

        Label statusLabel = new Label(status);
        statusLabel.setPrefWidth(100);

        switch (statusType) {
            case "pending":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b45309; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-background-color: #fef3c7;");
                break;
            case "pass":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #15803d; -fx-border-color: #86efac; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-background-color: #dcfce7;");
                break;
            case "fail":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-background-color: #fef2f2;");
                break;
            case "withdrawn":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-background-color: #f8fafc;");
                break;
            default:
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 10 2 10; -fx-background-color: #f8fafc;");
                break;
        }

        HBox actionBox = new HBox();
        actionBox.setPrefWidth(100);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        if (canWithdraw) {
            Button withdrawButton = new Button(core.UiText.tr("Withdraw"));
            withdrawButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #ffffff; -fx-padding: 4 12 4 12; -fx-cursor: hand;");
            withdrawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> e.consume());
            withdrawButton.setOnAction(e -> {
                boolean success = recordManager.withdrawApplication(applicationId);
                if (success) {
                    System.out.println("Application withdrawn successfully: " + applicationId);
                    refreshAfterWithdrawal();
                } else {
                    System.out.println("Withdrawal failed: " + applicationId);
                }
            });
            actionBox.getChildren().add(withdrawButton);
        } else {
            Label noActionLabel = new Label("—");
            noActionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
            actionBox.getChildren().add(noActionLabel);
        }

        row.getChildren().addAll(positionLabel, dateLabel, statusLabel, actionBox);

        // 添加点击事件，打开详情页
        row.setOnMouseClicked(e -> {
            ApplicationDetailView detailView = new ApplicationDetailView(record);
            if (row.getScene() != null && row.getScene().getWindow() != null) {
                detailView.showDialog((javafx.stage.Stage) row.getScene().getWindow());
            }
        });

        return row;
    }

    private String getStatusDisplay(String status) {
        switch (status) {
            case TAApplicationRecord.STATUS_PENDING:
                return core.UiText.tr("Under Review");
            case TAApplicationRecord.STATUS_APPROVED:
                return core.UiText.tr("Approved");
            case TAApplicationRecord.STATUS_REJECTED:
                return core.UiText.tr("Rejected");
            case TAApplicationRecord.STATUS_WITHDRAWN:
                return core.UiText.tr("Withdrawn");
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
}
