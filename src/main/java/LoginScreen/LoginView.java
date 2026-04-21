package LoginScreen;

import TA.java.TAPositionListUI;
import data.LocalStorageManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView extends Application {

    private Stage primaryStage;
    private VBox loginPanel;
    private VBox registerPanel;
    private Label loginTab;
    private Label registerTab;
    private StackPane contentPane;
    private javafx.scene.shape.Rectangle tabIndicator;
    private TranslateTransition indicatorTransition;
    private boolean isLoginActive = true;

    public interface LoginHandler {
        void onLogin(String account, String password);
        void onRegister(String account, String password, String role);
    }

    private LoginHandler loginHandler;

    public void setLoginHandler(LoginHandler handler) {
        this.loginHandler = handler;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        core.AppNavigator.getInstance().init(primaryStage);

        setLoginHandler(new LoginHandler() {
            @Override
            public void onLogin(String account, String password) {
                System.out.println("登录: 账号=" + account + ", 密码=" + password);
            }

            @Override
            public void onRegister(String account, String password, String role) {
                System.out.println("注册: 账号=" + account + ", 密码=" + password + ", 角色=" + role);
            }
        });

        core.AppNavigator.getInstance().navigateTo(buildLoginScene(), "TA Recruitment System - Login");
    }

    public Scene buildLoginScene() {
        StackPane rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: #f8fafc;");
        rootWrapper.setAlignment(Pos.CENTER);

        VBox container = new VBox();
        container.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 8);"
        );
        container.setPadding(new Insets(48, 48, 48, 48));
        container.setSpacing(0);
        container.setPrefWidth(440);
        container.setMaxWidth(440);

        Label titleLabel = new Label("TA Recruitment System");
        titleLabel.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: #1e293b;"
        );
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label subtitleLabel = new Label("Teaching Assistant Recruitment System");
        subtitleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #64748b;"
        );
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox titleBox = new VBox();
        titleBox.setSpacing(10);
        titleBox.setPadding(new Insets(0, 0, 32, 0));
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        StackPane tabsBox = createTabs();

        contentPane = new StackPane();
        contentPane.setMinWidth(440);
        contentPane.setPrefWidth(440);
        contentPane.setMaxWidth(440);

        loginPanel = createLoginPanel();
        loginPanel.setPrefWidth(440);
        loginPanel.setMinWidth(440);
        loginPanel.setTranslateX(0);
        loginPanel.setOpacity(1);

        registerPanel = createRegisterPanel();
        registerPanel.setPrefWidth(440);
        registerPanel.setMinWidth(440);
        registerPanel.setOpacity(0);
        registerPanel.setTranslateX(440);

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

        return new Scene(scrollPane);
    }

    private StackPane createTabs() {
        StackPane tabsContainer = new StackPane();
        tabsContainer.setStyle(
            "-fx-background-color: #f1f5f9;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 4px;"
        );
        tabsContainer.setAlignment(Pos.TOP_LEFT);

        HBox tabsBox = new HBox();
        tabsBox.setSpacing(0);

        loginTab = new Label("Log In");
        loginTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );
        loginTab.setAlignment(Pos.CENTER);
        loginTab.setPrefWidth(210);
        loginTab.setOnMouseClicked(e -> switchToLogin());

        registerTab = new Label("Register");
        registerTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );
        registerTab.setAlignment(Pos.CENTER);
        registerTab.setPrefWidth(210);
        registerTab.setOnMouseClicked(e -> switchToRegister());

        tabsBox.getChildren().addAll(loginTab, registerTab);

        tabIndicator = new javafx.scene.shape.Rectangle(210, 36);
        tabIndicator.setArcWidth(8);
        tabIndicator.setArcHeight(8);
        tabIndicator.setFill(javafx.scene.paint.Color.WHITE);
        tabIndicator.setTranslateX(0);

        indicatorTransition = new TranslateTransition(Duration.millis(300), tabIndicator);
        indicatorTransition.setCycleCount(1);

        tabsContainer.getChildren().addAll(tabIndicator, tabsBox);

        return tabsContainer;
    }

    private VBox createLoginPanel() {
        VBox panel = new VBox();
        panel.setSpacing(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        VBox accountField = createFormField("Account (Student ID / Staff ID)", "Enter your account", false);
        VBox passwordField = createPasswordField("Password", "Enter your password");

        Label messageLabel = new Label("");
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPadding(new Insets(10, 0, 0, 0));
        messageLabel.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Log In");
        loginButton.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #6366f1;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12px 24px;" +
            "-fx-cursor: hand;"
        );
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setOnMouseEntered(e ->
            loginButton.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #4f46e5;" +
                "-fx-background-radius: 8px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 12px 24px;" +
                "-fx-cursor: hand;"
            )
        );
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #6366f1;" +
                "-fx-background-radius: 8px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 12px 24px;" +
                "-fx-cursor: hand;"
            );
        });
        loginButton.setOnAction(e -> {
            TextField accountInput = (TextField) accountField.getChildren().get(1);
            PasswordField passInput = (PasswordField) passwordField.getChildren().get(1);

            String result = userManager.login(
                accountInput.getText(),
                passInput.getText()
            );

            if (result.startsWith("SUCCESS:")) {
                messageLabel.setText("Login successful! Redirecting...");
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #008800; -fx-alignment: center;");

                String userRole = result.substring(8);
                String studentId = accountInput.getText();
                System.out.println("登录成功，用户角色: " + userRole + ", 学号: " + studentId);

                if (userRole.equals("TA/java")) {
                    new LocalStorageManager().saveLastLogin(accountInput.getText(), userRole);
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                TAPositionListUI ui = new TAPositionListUI();
                                ui.setCurrentStudentId(studentId);
                                ui.navigateTo();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else if (userRole.equals("ADMIN")) {
                    new LocalStorageManager().saveLastLogin(accountInput.getText(), userRole);
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                Admin.AdminDashboard dashboard = new Admin.AdminDashboard(studentId);
                                dashboard.navigateTo();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else if (userRole.equals("MO")) {
                    new LocalStorageManager().saveLastLogin(accountInput.getText(), userRole);
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                Admin.MODashboard dashboard = new Admin.MODashboard(studentId);
                                dashboard.navigateTo();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                } else {
                    messageLabel.setText(userRole + " interface under development...");
                    messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #008800; -fx-alignment: center;");
                }

            } else {
                messageLabel.setText(result);
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cc0000; -fx-alignment: center;");
            }
        });

        panel.getChildren().addAll(accountField, passwordField, loginButton, messageLabel);

        return panel;
    }

    private VBox createRegisterPanel() {
        VBox panel = new VBox();
        panel.setSpacing(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        VBox accountField = createFormField("Account (Student ID / Staff ID)", "Enter student or staff ID", false);
        VBox passwordField = createPasswordField("Password", "Enter password (min. 6 characters)");
        VBox confirmField = createPasswordField("Confirm Password", "Re-enter password");

        VBox roleBox = new VBox();
        roleBox.setSpacing(6);

        Label roleLabel = new Label("Role");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("-- Select Role --", "TA Applicant", "Module Organiser", "System Administrator (Admin)");
        roleCombo.getSelectionModel().select(0);
        roleCombo.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1;");
        roleCombo.setPrefWidth(Double.MAX_VALUE);

        roleBox.getChildren().addAll(roleLabel, roleCombo);

        VBox authCodeBox = new VBox();
        authCodeBox.setSpacing(6);
        authCodeBox.setVisible(false);
        authCodeBox.setManaged(false);

        Label authCodeLabel = new Label("Admin Authorisation Code");
        authCodeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        PasswordField authCodeInput = new PasswordField();
        authCodeInput.setPromptText("Enter admin authorisation code");
        authCodeInput.setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 10 12 10 12;");
        authCodeInput.setPrefWidth(Double.MAX_VALUE);

        authCodeBox.getChildren().addAll(authCodeLabel, authCodeInput);

        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains("Admin")) {
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

        Button registerButton = new Button("Register");
        registerButton.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: #6366f1;" +
            "-fx-background-radius: 8px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 12px 24px;" +
            "-fx-cursor: hand;"
        );
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.setOnMouseEntered(e ->
            registerButton.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #4f46e5;" +
                "-fx-background-radius: 8px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 12px 24px;" +
                "-fx-cursor: hand;"
            )
        );
        registerButton.setOnMouseExited(e -> {
            registerButton.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-background-color: #6366f1;" +
                "-fx-background-radius: 8px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 12px 24px;" +
                "-fx-cursor: hand;"
            );
        });
        registerButton.setOnAction(e -> {
            TextField accountInput = (TextField) accountField.getChildren().get(1);
            PasswordField passInput = (PasswordField) passwordField.getChildren().get(1);
            PasswordField confirmInput = (PasswordField) confirmField.getChildren().get(1);
            String role = roleCombo.getSelectionModel().getSelectedIndex() == 0 ? "" :
                          roleCombo.getSelectionModel().getSelectedItem().toString();

            if (!passInput.getText().equals(confirmInput.getText())) {
                messageLabel.setText("Passwords do not match");
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
                messageLabel.setText("Registration successful! Please switch to Log In");
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
        fieldBox.setSpacing(8);

        Label labelLabel = new Label(label);
        labelLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #374151;"
        );

        TextField inputField;
        if (isPassword) {
            inputField = new PasswordField();
        } else {
            inputField = new TextField();
        }
        inputField.setPromptText(placeholder);
        inputField.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-padding: 12px 16px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #1e293b;"
        );
        inputField.setPrefWidth(Double.MAX_VALUE);
        
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                inputField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #6366f1;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 12px 16px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 0, 0, 0, 3);"
                );
            } else {
                inputField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 12px 16px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #1e293b;"
                );
            }
        });

        Label errorLabel = new Label("");
        errorLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #ef4444;"
        );

        fieldBox.getChildren().addAll(labelLabel, inputField, errorLabel);

        return fieldBox;
    }

    private VBox createPasswordField(String label, String placeholder) {
        VBox fieldBox = new VBox();
        fieldBox.setSpacing(8);

        Label labelLabel = new Label(label);
        labelLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #374151;"
        );

        PasswordField inputField = new PasswordField();
        inputField.setPromptText(placeholder);
        inputField.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-padding: 12px 16px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #1e293b;"
        );
        inputField.setPrefWidth(Double.MAX_VALUE);
        
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                inputField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #6366f1;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 12px 16px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 0, 0, 0, 3);"
                );
            } else {
                inputField.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #e2e8f0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-padding: 12px 16px;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #1e293b;"
                );
            }
        });

        Label errorLabel = new Label("");
        errorLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #ef4444;"
        );

        fieldBox.getChildren().addAll(labelLabel, inputField, errorLabel);

        return fieldBox;
    }

    private void switchToLogin() {
        if (isLoginActive) {
            return;
        }
        
        isLoginActive = true;
        
        loginTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );
        registerTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );

        indicatorTransition.stop();
        indicatorTransition.setFromX(tabIndicator.getTranslateX());
        indicatorTransition.setToX(0);
        indicatorTransition.play();

        TranslateTransition loginSlideIn = new TranslateTransition(Duration.millis(300), loginPanel);
        loginSlideIn.setFromX(-440);
        loginSlideIn.setToX(0);

        FadeTransition loginFadeIn = new FadeTransition(Duration.millis(300), loginPanel);
        loginFadeIn.setFromValue(0);
        loginFadeIn.setToValue(1);

        ParallelTransition loginIn = new ParallelTransition(loginSlideIn, loginFadeIn);

        TranslateTransition registerSlideOut = new TranslateTransition(Duration.millis(300), registerPanel);
        registerSlideOut.setFromX(0);
        registerSlideOut.setToX(440);

        FadeTransition registerFadeOut = new FadeTransition(Duration.millis(300), registerPanel);
        registerFadeOut.setFromValue(1);
        registerFadeOut.setToValue(0);

        ParallelTransition registerOut = new ParallelTransition(registerSlideOut, registerFadeOut);

        registerOut.setOnFinished(e -> {
            registerPanel.setTranslateX(440);
            registerPanel.setOpacity(0);
        });

        registerOut.play();
        loginIn.play();
    }

    private void switchToRegister() {
        if (!isLoginActive) {
            return;
        }
        
        isLoginActive = false;
        
        loginTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 500;" +
            "-fx-text-fill: #64748b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );
        registerTab.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: #1e293b;" +
            "-fx-padding: 10px 0;" +
            "-fx-cursor: hand;"
        );

        indicatorTransition.stop();
        indicatorTransition.setFromX(tabIndicator.getTranslateX());
        indicatorTransition.setToX(210);
        indicatorTransition.play();

        TranslateTransition registerSlideIn = new TranslateTransition(Duration.millis(300), registerPanel);
        registerSlideIn.setFromX(440);
        registerSlideIn.setToX(0);

        FadeTransition registerFadeIn = new FadeTransition(Duration.millis(300), registerPanel);
        registerFadeIn.setFromValue(0);
        registerFadeIn.setToValue(1);

        ParallelTransition registerIn = new ParallelTransition(registerSlideIn, registerFadeIn);

        TranslateTransition loginSlideOut = new TranslateTransition(Duration.millis(300), loginPanel);
        loginSlideOut.setFromX(0);
        loginSlideOut.setToX(-440);

        FadeTransition loginFadeOut = new FadeTransition(Duration.millis(300), loginPanel);
        loginFadeOut.setFromValue(1);
        loginFadeOut.setToValue(0);

        ParallelTransition loginOut = new ParallelTransition(loginSlideOut, loginFadeOut);

        loginOut.setOnFinished(e -> {
            loginPanel.setTranslateX(-440);
            loginPanel.setOpacity(0);
        });

        loginOut.play();
        registerIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
