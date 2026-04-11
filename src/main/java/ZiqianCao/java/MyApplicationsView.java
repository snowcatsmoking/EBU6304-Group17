package ZiqianCao.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyApplicationsView {

    private String currentStudentId = "2024999";
    private TAApplicationRecordManager recordManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private javafx.scene.control.TextField availableTimeField;
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

    public BorderPane getView() {
        recordManager = new TAApplicationRecordManager();
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(40, 40, 40, 40));
        content.setSpacing(20);

        Label titleLabel = new Label("My Applications");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 添加时间筛选功能
        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label availableTimeLabel = new Label("Available Time:");
        availableTimeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        availableTimeField = new javafx.scene.control.TextField();
        availableTimeField.setPromptText("YYYY-MM-DD");
        availableTimeField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        availableTimeField.setPrefWidth(150);

        Button filterButton = new Button("Filter");
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        filterButton.setOnAction(e -> applyDateFilter());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        resetButton.setOnAction(e -> resetDateFilter());

        filterBox.getChildren().addAll(availableTimeLabel, availableTimeField, filterButton, resetButton);

        // 获取所有申请记录（含已撤回、未通过）
        allApplications = recordManager.getApplicationsByStudentId(currentStudentId);
        
        // 按照申请时间排序，从新到旧
        java.util.Collections.sort(allApplications, (a1, a2) -> {
            if (a1.getApplicationDate() != null && a2.getApplicationDate() != null) {
                return a2.getApplicationDate().compareTo(a1.getApplicationDate());
            }
            return 0;
        });
        
        // 初始化当前显示的记录为所有记录
        currentApplications = new java.util.ArrayList<>(allApplications);
        
        HBox tableHeader = createTableHeader();
        
        // 创建表格内容容器
        tableContentBox = new VBox();
        tableContentBox.setSpacing(0);
        
        // 创建空标签
        emptyLabel = new Label("No application records");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
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

        root.setCenter(content);

        return root;
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
        String availableTime = availableTimeField.getText().trim();

        List<TAApplicationRecord> filteredApplications = new java.util.ArrayList<>();
        
        for (TAApplicationRecord record : allApplications) {
            boolean match = true;
            
            if (!availableTime.isEmpty()) {
                try {
                    // 解析用户输入的日期
                    java.time.LocalDate inputDate = java.time.LocalDate.parse(availableTime, 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    // 解析申请日期
                    java.time.LocalDate applicationDate = record.getApplicationDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    // 只保留申请日期在输入日期及之前的记录
                    if (applicationDate.isAfter(inputDate)) {
                        match = false;
                    }
                } catch (Exception e) {
                    // 输入格式不正确，不匹配
                    match = false;
                }
            }
            
            if (match) {
                filteredApplications.add(record);
            }
        }
        
        // 更新当前显示的记录列表
        currentApplications = filteredApplications;
        
        // 更新空标签文本
        if (currentApplications.isEmpty()) {
            emptyLabel.setText("No matching applications");
        }
        
        // 刷新表格显示
        refreshTableContent();
    }

    private void resetDateFilter() {
        availableTimeField.clear();
        // 重置为显示所有记录
        currentApplications = new java.util.ArrayList<>(allApplications);
        emptyLabel.setText("No application records");
        // 刷新表格显示
        refreshTableContent();
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

    private HBox createTableRow(String position, String date, String status, String statusType, boolean canWithdraw, String applicationId, TAApplicationRecord record) {
        HBox row = new HBox();
        row.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
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
            withdrawButton.setOnAction(e -> {
                boolean success = recordManager.withdrawApplication(applicationId);
                if (success) {
                    System.out.println("成功撤回申请: " + applicationId);
                    // 刷新界面 - 更安全的方式
                    if (row.getScene() != null && row.getScene().getRoot() != null) {
                        BorderPane newView = getView();
                        // 查找 BorderPane 容器
                        Parent root = row.getScene().getRoot();
                        if (root instanceof StackPane) {
                            StackPane stackPane = (StackPane) root;
                            for (Node node : stackPane.getChildren()) {
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
}
