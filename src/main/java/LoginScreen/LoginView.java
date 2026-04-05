package LoginScreen;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView extends Application {

    private Stage primaryStage;
    private VBox loginPanel;
    private VBox registerPanel;
    private Label loginTab;
    private Label registerTab;

    public interface LoginHandler {
        void onLogin(String account, String password, String role);
        void onRegister(String account, String password, String role);
    }

    private LoginHandler loginHandler;

    public void setLoginHandler(LoginHandler handler) {
        this.loginHandler = handler;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        setLoginHandler(new LoginHandler() {
            @Override
            public void onLogin(String account, String password, String role) {
                System.out.println("登录: 账号=" + account + ", 密码=" + password + ", 身份=" + role);
            }

            @Override
            public void onRegister(String account, String password, String role) {
                System.out.println("注册: 账号=" + account + ", 密码=" + password + ", 角色=" + role);
            }
        });

        StackPane rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: #fafafa;");
        rootWrapper.setAlignment(Pos.CENTER);

        VBox container = new VBox();
        container.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");
        container.setPadding(new Insets(40, 40, 40, 40));
        container.setSpacing(0);
        container.setPrefWidth(500);
        container.setMaxWidth(500);

        Label titleLabel = new Label("TA 招聘管理系统");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #000000;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label subtitleLabel = new Label("Teaching Assistant Recruitment System");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888888;");
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox titleBox = new VBox();
        titleBox.setSpacing(8);
        titleBox.setPadding(new Insets(0, 0, 28, 0));
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        HBox tabsBox = createTabs();

        StackPane contentPane = new StackPane();
        contentPane.setPadding(new Insets(0, 0, 0, 0));

        loginPanel = createLoginPanel();
        registerPanel = createRegisterPanel();
        registerPanel.setVisible(false);

        contentPane.getChildren().addAll(loginPanel, registerPanel);

        container.getChildren().addAll(titleBox, tabsBox, contentPane);
        rootWrapper.getChildren().add(container);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(rootWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane, 900, 700);
        primaryStage.setTitle("TA招聘管理系统 - 登录");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTabs() {
        HBox tabsBox = new HBox();
        tabsBox.setStyle("-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");
        tabsBox.setPadding(new Insets(0, 0, 0, 0));
        tabsBox.setSpacing(0);

        loginTab = new Label("登录");
        loginTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-border-width: 0 0 2 0; -fx-border-color: #000000; -fx-cursor: hand;");
        loginTab.setAlignment(Pos.CENTER);
        loginTab.setPrefWidth(250);
        loginTab.setPadding(new Insets(10, 0, 10, 0));
        loginTab.setOnMouseClicked(e -> switchToLogin());

        registerTab = new Label("注册");
        registerTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        registerTab.setAlignment(Pos.CENTER);
        registerTab.setPrefWidth(250);
        registerTab.setPadding(new Insets(10, 0, 10, 0));
        registerTab.setOnMouseClicked(e -> switchToRegister());

        tabsBox.getChildren().addAll(loginTab, registerTab);

        return tabsBox;
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox();
        panel.setSpacing(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        VBox accountField = createFormField("账号 (学号 / 工号)", "请输入账号", false);
        VBox passwordField = createPasswordField("密码", "请输入密码");

        VBox roleBox = new VBox();
        roleBox.setSpacing(6);

        Label roleLabel = new Label("登录身份");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("-- 请选择登录身份 --", "应聘者 (TA Applicant)", "课程组织者 (Module Organiser)", "系统管理员 (Admin)");
        roleCombo.getSelectionModel().select(0);
        roleCombo.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1;");
        roleCombo.setPrefWidth(Double.MAX_VALUE);

        roleBox.getChildren().addAll(roleLabel, roleCombo);

        Label messageLabel = new Label("");
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPadding(new Insets(10, 0, 0, 0));
        messageLabel.setAlignment(Pos.CENTER);

        Button loginButton = new Button("登 录");
        loginButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #000000; -fx-padding: 11 0 11 0; -fx-cursor: hand;");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> {
            TextField accountInput = (TextField) accountField.getChildren().get(1);
            PasswordField passInput = (PasswordField) passwordField.getChildren().get(1);
            String role = roleCombo.getSelectionModel().getSelectedIndex() == 0 ? "" :
                          roleCombo.getSelectionModel().getSelectedItem().toString();

            String result = userManager.login(
                accountInput.getText(),
                passInput.getText(),
                role
            );

            if (result.startsWith("SUCCESS:")) {
                messageLabel.setText("登录成功！正在跳转...");
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #008800; -fx-alignment: center;");

                String userRole = result.substring(8);
                String studentId = accountInput.getText();
                System.out.println("登录成功，用户角色: " + userRole + ", 学号: " + studentId);

                if (userRole.equals("TA")) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                primaryStage.close();
                                ZiqianCao.java.TAPositionListUI ui = new ZiqianCao.java.TAPositionListUI();
                                ui.setCurrentStudentId(studentId);
                                ui.start(new Stage());
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else if (userRole.equals("ADMIN")) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                primaryStage.close();
                                Admin.AdminDashboard dashboard = new Admin.AdminDashboard(studentId);
                                dashboard.start(new Stage());
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    messageLabel.setText(userRole + " 界面开发中...");
                    messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #008800; -fx-alignment: center;");
                }

            } else {
                messageLabel.setText(result);
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-alignment: center;");
            }
        });

        panel.getChildren().addAll(accountField, passwordField, roleBox, loginButton, messageLabel);

        return panel;
    }

    private VBox createRegisterPanel() {
        VBox panel = new VBox();
        panel.setSpacing(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        VBox accountField = createFormField("账号 (学号 / 工号)", "请输入学号或工号", false);
        VBox passwordField = createPasswordField("密码", "请输入密码（至少6位）");
        VBox confirmField = createPasswordField("确认密码", "请再次输入密码");

        VBox roleBox = new VBox();
        roleBox.setSpacing(6);

        Label roleLabel = new Label("角色");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("-- 请选择角色 --", "应聘者 (TA Applicant)", "课程组织者 (Module Organiser)", "系统管理员 (Admin)");
        roleCombo.getSelectionModel().select(0);
        roleCombo.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1;");
        roleCombo.setPrefWidth(Double.MAX_VALUE);

        roleBox.getChildren().addAll(roleLabel, roleCombo);

        VBox authCodeBox = new VBox();
        authCodeBox.setSpacing(6);
        authCodeBox.setVisible(false);
        authCodeBox.setManaged(false);

        Label authCodeLabel = new Label("管理员授权码");
        authCodeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        PasswordField authCodeInput = new PasswordField();
        authCodeInput.setPromptText("请输入管理员授权码");
        authCodeInput.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10 12 10 12;");
        authCodeInput.setPrefWidth(Double.MAX_VALUE);

        authCodeBox.getChildren().addAll(authCodeLabel, authCodeInput);

        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains("系统管理员")) {
                authCodeBox.setVisible(true);
                authCodeBox.setManaged(true);
            } else {
                authCodeBox.setVisible(false);
                authCodeBox.setManaged(false);
            }
        });

        Label messageLabel = new Label("");
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPadding(new Insets(10, 0, 0, 0));
        messageLabel.setAlignment(Pos.CENTER);

        Button registerButton = new Button("注 册");
        registerButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #000000; -fx-padding: 11 0 11 0; -fx-cursor: hand;");
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> {
            TextField accountInput = (TextField) accountField.getChildren().get(1);
            PasswordField passInput = (PasswordField) passwordField.getChildren().get(1);
            PasswordField confirmInput = (PasswordField) confirmField.getChildren().get(1);
            String role = roleCombo.getSelectionModel().getSelectedIndex() == 0 ? "" :
                          roleCombo.getSelectionModel().getSelectedItem().toString();

            if (!passInput.getText().equals(confirmInput.getText())) {
                messageLabel.setText("两次密码不一致");
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-alignment: center;");
                return;
            }

            String result = userManager.register(
                accountInput.getText(),
                passInput.getText(),
                role,
                authCodeInput.getText()
            );

            if (result.equals("SUCCESS")) {
                messageLabel.setText("注册成功！请切换到登录页面");
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #008800; -fx-alignment: center;");
                accountInput.clear();
                passInput.clear();
                confirmInput.clear();
                authCodeInput.clear();
                roleCombo.getSelectionModel().select(0);
            } else {
                messageLabel.setText(result);
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-alignment: center;");
            }
        });

        panel.getChildren().addAll(accountField, passwordField, confirmField, roleBox, authCodeBox, registerButton, messageLabel);

        return panel;
    }

    private VBox createFormField(String label, String placeholder, boolean isPassword) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(6);

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        TextField inputField;
        if (isPassword) {
            inputField = new PasswordField();
        } else {
            inputField = new TextField();
        }
        inputField.setPromptText(placeholder);
        inputField.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10 12 10 12;");
        inputField.setPrefWidth(Double.MAX_VALUE);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");

        fieldBox.getChildren().addAll(labelLabel, inputField, errorLabel);

        return fieldBox;
    }

    private VBox createPasswordField(String label, String placeholder) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(6);

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        PasswordField inputField = new PasswordField();
        inputField.setPromptText(placeholder);
        inputField.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10 12 10 12;");
        inputField.setPrefWidth(Double.MAX_VALUE);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");

        fieldBox.getChildren().addAll(labelLabel, inputField, errorLabel);

        return fieldBox;
    }

    private void switchToLogin() {
        loginTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-border-width: 0 0 2 0; -fx-border-color: #000000; -fx-cursor: hand;");
        registerTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        loginPanel.setVisible(true);
        registerPanel.setVisible(false);
    }

    private void switchToRegister() {
        loginTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        registerTab.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000; -fx-border-width: 0 0 2 0; -fx-border-color: #000000; -fx-cursor: hand;");
        loginPanel.setVisible(false);
        registerPanel.setVisible(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
