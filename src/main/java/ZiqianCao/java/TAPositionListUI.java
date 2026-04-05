package ZiqianCao.java;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TAPositionListUI extends Application {

    private BorderPane root;
    private StackPane rootContainer;
    private Label navItem1;
    private Label navItem2;
    private Label navItem3;
    private Label navItem4;
    private DashboardView dashboardView;
    private List<TAJob> jobList;
    private TAApplicationRecordManager recordManager;
    private String currentStudentId = "2024999";

    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
    }

    private Stage primaryStage;
    private VBox overlay;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        rootContainer = new StackPane();
        rootContainer.setStyle("-fx-background-color: #f5f5f5;");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        recordManager = new TAApplicationRecordManager();
        initJobList();

        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        dashboardView = new DashboardView();
        dashboardView.setCurrentStudentId(currentStudentId);
        dashboardView.setNavigationListener(() -> switchToView("profile"));
        dashboardView.setLogoutListener(() -> {
            primaryStage.close();
            try {
                new LoginScreen.LoginView().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        root.setCenter(dashboardView.getView());
        navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");

        overlay = new VBox();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setVisible(false);

        rootContainer.getChildren().addAll(root, overlay);

        Scene scene = new Scene(rootContainer, 1200, 700);
        primaryStage.setTitle("TA Application System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initJobList() {
        jobList = new ArrayList<>();
        jobList.add(new TAJob("J001", "软件工程课程助教", "软件工程", "EBU6301", 2, "3.5及以上", "2025年9月15日", "张老师", false));
        jobList.add(new TAJob("J002", "数据结构助教", "数据结构", "EBU6302", 3, "3.0及以上", "2025年10月1日", "李老师", false));
        jobList.add(new TAJob("J003", "算法竞赛指导助教", "算法竞赛", "EBU6303", 1, "有竞赛经验者优先", "已截止", "王老师", true));
        jobList.add(new TAJob("J004", "Java程序设计助教", "Java程序设计", "EBU6304", 2, "熟悉Java编程", "2025年10月15日", "赵老师", false));
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setSpacing(0);
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("TA系统");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        titleLabel.setPadding(new Insets(0, 0, 20, 16));

        VBox navBox = new VBox();
        navBox.setSpacing(0);
        navBox.setAlignment(Pos.TOP_LEFT);

        navItem1 = createNavItem("控制台", "dashboard");
        navItem2 = createNavItem("岗位列表", "positions");
        navItem3 = createNavItem("我的申请", "applications");
        navItem4 = createNavItem("个人档案", "profile");

        navBox.getChildren().addAll(navItem1, navItem2, navItem3, navItem4);

        sidebar.getChildren().addAll(titleLabel, navBox);

        return sidebar;
    }

    private Label createNavItem(String text, String viewName) {
        Label item = new Label(text);
        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        item.setPrefWidth(220);
        item.setPrefHeight(40);
        item.setMinHeight(40);
        item.setMaxHeight(40);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setOnMouseClicked(e -> switchToView(viewName));
        return item;
    }

    private void resetNavItems() {
        navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem3.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
        navItem4.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
    }

    private void switchToView(String viewName) {
        resetNavItems();
        switch (viewName) {
            case "dashboard":
                root.setCenter(dashboardView.getView());
                navItem1.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "positions":
                root.setCenter(createPositionListView());
                navItem2.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "applications":
                root.setCenter(new MyApplicationsView(currentStudentId).getView());
                navItem3.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
            case "profile":
                ProfileView profileView = new ProfileView();
                profileView.setCurrentStudentId(currentStudentId);
                root.setCenter(profileView.getView());
                navItem4.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-padding: 10 16 10 16; -fx-border-width: 0 0 0 3; -fx-border-color: #000000; -fx-background-color: #f0f0f0; -fx-cursor: hand;");
                break;
        }
    }

    private VBox createPositionListView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Button courseButton = new Button("课程");
        courseButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");

        Button activityButton = new Button("活动");
        activityButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");

        Label filterLabel = new Label("筛选");
        filterLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-underline: true; -fx-cursor: hand;");

        Label resetLabel = new Label("重置");
        resetLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-underline: true; -fx-cursor: hand;");

        filterBox.getChildren().addAll(courseButton, activityButton, filterLabel, resetLabel);

        VBox positionListBox = new VBox();
        positionListBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        positionListBox.setSpacing(0);

        for (TAJob job : jobList) {
            VBox positionBox = createPositionBox(job);
            positionListBox.getChildren().add(positionBox);
        }

        content.getChildren().addAll(filterBox, positionListBox);

        return content;
    }

    private VBox createPositionBox(TAJob job) {
        VBox positionBox = new VBox();
        positionBox.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        positionBox.setPadding(new Insets(16, 16, 16, 16));
        positionBox.setSpacing(8);
        positionBox.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox();
        titleBox.setSpacing(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(job.getPositionName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        if (job.isActive()) {
            Label closedLabel = new Label("不可申请");
            closedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, closedLabel);
        } else {
            titleBox.getChildren().add(titleLabel);
        }

        HBox infoBox = new HBox();
        infoBox.setSpacing(24);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("所属课程/活动: " + job.getCourseName());
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label countLabel = new Label("招聘人数: " + job.getRecruitmentCount() + "人");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label requirementLabel = new Label("任职要求: " + job.getRequirements());
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox deadlineBox = new HBox();
        deadlineBox.setSpacing(24);
        deadlineBox.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("申请截止时间: " + job.getDeadline());
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label publisherLabel = new Label("发布人: " + job.getPublisher());
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        if (job.isActive()) {
            Button closedButton = new Button("不可申请");
            closedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #999999; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            actionBox.getChildren().add(closedButton);
        } else {
            boolean hasApplied = recordManager.hasDuplicateApplication(currentStudentId, job.getJobId());
            boolean profileComplete = checkProfileComplete();
            
            if (hasApplied) {
                Button appliedButton = new Button("申请中");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #1890ff; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else if (!profileComplete) {
                Button incompleteButton = new Button("请完善档案");
                incompleteButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                incompleteButton.setOnAction(e -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("档案不完整");
                    alert.setHeaderText("申请前请先完善个人档案");
                    alert.setContentText("请前往「个人档案」页面填写：\n• 姓名\n• 专业\n• 联系电话\n• 可任职时间\n• 专业技能");
                    alert.showAndWait();
                    switchToView("profile");
                });
                actionBox.getChildren().add(incompleteButton);
            } else {
                Button applyButton = new Button("申请");
                applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                applyButton.setOnAction(e -> openApplicationForm(job));
                actionBox.getChildren().add(applyButton);
            }
        }

        infoBox.getChildren().addAll(courseLabel, countLabel, requirementLabel);
        deadlineBox.getChildren().addAll(deadlineLabel, publisherLabel);
        positionBox.getChildren().addAll(titleBox, infoBox, deadlineBox, actionBox);

        return positionBox;
    }

    private boolean checkProfileComplete() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("resources/Data/TAData/" + currentStudentId + ".json");
            if (file.exists()) {
                TAApplication user = mapper.readValue(file, TAApplication.class);
                return user.getName() != null && !user.getName().trim().isEmpty()
                    && user.getMajor() != null && !user.getMajor().trim().isEmpty()
                    && user.getPhone() != null && !user.getPhone().trim().isEmpty()
                    && user.getAvailableTime() != null && !user.getAvailableTime().trim().isEmpty()
                    && user.getSkill() != null && !user.getSkill().trim().isEmpty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openApplicationForm(TAJob job) {
        if (!checkProfileComplete()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("申请被拒绝");
            alert.setHeaderText("个人档案不完整");
            alert.setContentText("申请TA岗位前请先完善个人档案！\n\n必填项：姓名、专业、联系电话、可任职时间、专业技能\n\n请前往「个人档案」页面填写完整后再申请。");
            alert.showAndWait();
            return;
        }

        overlay.setVisible(true);
        TAApplicationFormView formView = new TAApplicationFormView(job, currentStudentId);
        formView.setApplicationListener(() -> {
            overlay.setVisible(false);
            switchToView("positions");
        });
        formView.setDialogCloseListener(() -> {
            overlay.setVisible(false);
        });
        formView.showDialog(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
