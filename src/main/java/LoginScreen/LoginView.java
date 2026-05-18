package LoginScreen;

import TA.java.TAPositionListUI;
import data.LocalStorageManager;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Bounds;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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

    // Creature and eye animation state
    private static class CreatureData {
        final Pane pane;
        final HBox face;
        final Circle[] pupils;
        CreatureData(Pane pane, HBox face, Circle[] pupils) {
            this.pane = pane; this.face = face; this.pupils = pupils;
        }
    }

    private final List<CreatureData> allCreatures = new ArrayList<>();
    private AnimationTimer eyeTimer;
    private volatile double mouseScreenX = Double.NaN;
    private volatile double mouseScreenY = Double.NaN;
    private double mouseX = 600;
    private double mouseY = 375;
    private String currentEyeState = "idle";
    private boolean passwordVisible = false;

    // Input field refs for focus listeners
    private TextField loginAccountField;
    private PasswordField loginPasswordField;
    private TextField loginPasswordVisibleField;
    private StackPane loginPasswordWrapper;
    private TextField regAccountField;
    private PasswordField regPasswordField;

    public interface LoginHandler {
        void onLogin(String account, String password);
        void onRegister(String account, String password, String role);
    }

    private LoginHandler loginHandler;

    public void setLoginHandler(LoginHandler handler) {
        this.loginHandler = handler;
    }

    // ========== Creature Builder ==========

    private CreatureData createCreature(int height, String gradient,
                                         double eyeR, double pupilR) {
        Pane pane = new Pane();
        pane.setPrefSize(110, height);
        pane.setMinSize(110, height);
        pane.setMaxSize(110, height);
        pane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 14, 0, 5, 5);");

        Region body = new Region();
        body.setPrefSize(110, height);
        body.setMaxSize(110, height);
        body.setMinSize(110, height);
        body.setStyle(
            "-fx-background-color: " + gradient + ";" +
            "-fx-background-radius: 55 55 0 0;" +
            "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.15), 30, 0.5, 14, 0);"
        );

        Region highlight = new Region();
        highlight.setPrefSize(33, height * 0.35);
        highlight.setMaxSize(33, height * 0.35);
        highlight.setStyle("-fx-background-color: rgba(255,255,255,0.18);-fx-background-radius: 50%;");
        highlight.setLayoutX(13);
        highlight.setLayoutY(height * 0.08);

        HBox face = new HBox(16);
        face.setAlignment(Pos.CENTER);
        face.setLayoutY(24);
        face.layoutXProperty().bind(pane.widthProperty().subtract(face.widthProperty()).divide(2));

        Circle[] pupils = new Circle[2];
        for (int i = 0; i < 2; i++) {
            Circle eyeWhite = new Circle(eyeR, Color.WHITE);
            eyeWhite.setStroke(Color.rgb(0, 0, 0, 0.08));
            eyeWhite.setStrokeWidth(0.5);

            Circle pupil = new Circle(pupilR, Color.web("#1e293b"));
            Circle pupilDot = new Circle(pupilR * 0.38, Color.WHITE);
            pupilDot.setTranslateX(pupilR * 0.25);
            pupilDot.setTranslateY(-pupilR * 0.25);

            StackPane eyeStack = new StackPane(eyeWhite, pupil, pupilDot);
            pupils[i] = pupil;
            face.getChildren().add(eyeStack);
        }

        pane.getChildren().addAll(body, highlight, face);
        return new CreatureData(pane, face, pupils);
    }

    // ========== Eye Animation & Focus ==========

    private void setEyeState(String state) {
        if (state.equals(currentEyeState)) return;
        currentEyeState = state;

        double targetX = 0;
        switch (state) {
            case "account":         targetX = 14; break;
            case "password-hidden":
            case "password-visible": targetX = -14; break;
            default:                targetX = 0; break;
        }

        for (CreatureData c : allCreatures) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), c.face);
            tt.setToX(targetX);
            tt.play();
        }
    }

    private void setupEyeAnimation(Scene scene, HBox root) {
        root.setPickOnBounds(true);

        // Scene-level event filter: fires in capturing phase for ALL nodes
        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        // Root-level handlers as backup
        root.setOnMouseMoved(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
        root.setOnMouseDragged(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        // OS-level AWT listener: catches mouse events before JavaFX sees them
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
            awtEvent -> {
                if (awtEvent instanceof java.awt.event.MouseEvent me) {
                    java.awt.Point screenLoc = me.getLocationOnScreen();
                    // Convert screen → scene coordinates via root (must run on FX thread later)
                    // Store raw screen coords; timer will convert on FX thread
                    mouseScreenX = screenLoc.getX();
                    mouseScreenY = screenLoc.getY();
                }
            },
            java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK |
            java.awt.AWTEvent.MOUSE_EVENT_MASK
        );

        eyeTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Convert AWT screen coords to scene coords on FX thread
                if (!Double.isNaN(mouseScreenX)) {
                    try {
                        javafx.geometry.Point2D local = root.screenToLocal(
                            mouseScreenX, mouseScreenY);
                        if (local != null) {
                            mouseX = local.getX();
                            mouseY = local.getY();
                        }
                    } catch (Exception ignored) {}
                }

                for (CreatureData c : allCreatures) {
                    for (Circle pupil : c.pupils) {
                        StackPane eyeStack = (StackPane) pupil.getParent();
                        Bounds bounds = eyeStack.localToScene(eyeStack.getBoundsInLocal());
                        if (bounds == null) continue;
                        double eyeCX = bounds.getMinX() + bounds.getWidth() / 2;
                        double eyeCY = bounds.getMinY() + bounds.getHeight() / 2;
                        double maxMove = 4;
                        double moveX = 0, moveY = 0;

                        switch (currentEyeState) {
                            case "idle":
                                double dx = mouseX - eyeCX;
                                double dy = mouseY - eyeCY;
                                double dist = Math.min(Math.sqrt(dx * dx + dy * dy), 150);
                                double angle = Math.atan2(dy, dx);
                                moveX = Math.cos(angle) * (dist / 150) * maxMove;
                                moveY = Math.sin(angle) * (dist / 150) * maxMove;
                                break;
                            case "account":         moveX = 2;  moveY = 0;   break;
                            case "password-hidden":  moveX = 3;  moveY = 0;   break;
                            case "password-visible": moveX = 0;  moveY = -3.5; break;
                        }
                        pupil.setTranslateX(moveX);
                        pupil.setTranslateY(moveY);
                    }
                }
            }
        };

        scene.windowProperty().addListener((obs, oldWin, newWin) -> {
            if (newWin != null) {
                eyeTimer.start();
            } else {
                eyeTimer.stop();
            }
        });
        if (scene.getWindow() != null) eyeTimer.start();
    }

    private void addFocusListeners(TextField accountField, PasswordField passwordField) {
        accountField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) return;
            if (newVal) setEyeState("account");
            else if (!passwordField.isFocused()) setEyeState("idle");
        });
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) setEyeState(passwordVisible ? "password-visible" : "password-hidden");
            else if (!passwordVisible) setEyeState("idle");
        });
    }

    private void togglePasswordVisibility(Label toggleIcon) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            loginPasswordField.setVisible(false);
            loginPasswordField.setManaged(false);
            loginPasswordVisibleField.setVisible(true);
            loginPasswordVisibleField.setManaged(true);
            toggleIcon.setText("🙈");
            setEyeState("password-visible");
        } else {
            loginPasswordVisibleField.setVisible(false);
            loginPasswordVisibleField.setManaged(false);
            loginPasswordField.setVisible(true);
            loginPasswordField.setManaged(true);
            toggleIcon.setText("👁");
            if (loginPasswordField.isFocused()) setEyeState("password-hidden");
            else setEyeState("idle");
        }
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
        // ===== Root: HBox split layout =====
        HBox root = new HBox();
        root.getStyleClass().add("login-root");

        // ===== Left Side: creatures + text =====
        StackPane leftSide = new StackPane();
        leftSide.getStyleClass().add("left-side");
        leftSide.setMouseTransparent(true);
        HBox.setHgrow(leftSide, Priority.ALWAYS);

        // Text header (top-left overlay)
        Label mainTitle = new Label("BUPT Recruitment System");
        mainTitle.getStyleClass().add("main-title");
        mainTitle.setWrapText(true);

        Label subTitle = new Label("Focus on efficient work");
        subTitle.getStyleClass().add("sub-title");

        VBox textHeader = new VBox(12, mainTitle, subTitle);
        textHeader.setMaxWidth(550);
        StackPane.setAlignment(textHeader, Pos.TOP_LEFT);
        StackPane.setMargin(textHeader, new Insets(40, 0, 0, 50));

        // Fade-in animation for text
        FadeTransition titleFade = new FadeTransition(Duration.millis(1000), mainTitle);
        titleFade.setFromValue(0); titleFade.setToValue(1);
        titleFade.setDelay(Duration.millis(1000));
        FadeTransition subFade = new FadeTransition(Duration.millis(1000), subTitle);
        subFade.setFromValue(0); subFade.setToValue(1);
        subFade.setDelay(Duration.millis(1700));
        mainTitle.setOpacity(0);
        subTitle.setOpacity(0);
        titleFade.play();
        subFade.play();

        // Creatures
        CreatureData c1 = createCreature(320,
            "linear-gradient(to bottom, #fca5a5 0%, #f87171 20%, " +
            "#ef4444 50%, #dc2626 100%)", 12, 6.5);
        CreatureData c2 = createCreature(460,
            "linear-gradient(to bottom, #fde68a 0%, #fbbf24 20%, " +
            "#f59e0b 50%, #d97706 100%)", 13, 7);
        CreatureData c3 = createCreature(270,
            "linear-gradient(to bottom, #a5b4fc 0%, #818cf8 20%, " +
            "#6366f1 50%, #4f46e5 100%)", 11, 6);
        allCreatures.addAll(List.of(c1, c2, c3));

        HBox ground = new HBox(0, c1.pane, c2.pane, c3.pane);
        ground.setAlignment(Pos.BOTTOM_CENTER);

        Region groundShadow = new Region();
        groundShadow.getStyleClass().add("ground-shadow");

        VBox creaturesArea = new VBox(0, ground, groundShadow);
        creaturesArea.setAlignment(Pos.CENTER);

        leftSide.getChildren().addAll(creaturesArea, textHeader);

        // ===== Right Side: login card =====
        VBox rightSide = new VBox();
        rightSide.getStyleClass().add("right-side");
        rightSide.setPrefWidth(560);
        rightSide.setPadding(new Insets(24, 40, 36, 40));
        rightSide.setAlignment(Pos.TOP_CENTER);

        VBox container = new VBox();
        container.getStyleClass().add("login-card");
        container.setPadding(new Insets(32));
        container.setSpacing(0);
        container.setPrefWidth(480);
        container.setMaxWidth(480);

        Label titleLabel = new Label("TA Recruitment System");
        titleLabel.getStyleClass().add("login-title");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label subtitleLabel = new Label("Teaching Assistant Recruitment System");
        subtitleLabel.getStyleClass().add("login-subtitle");
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox titleBox = new VBox(10, titleLabel, subtitleLabel);
        titleBox.setPadding(new Insets(0, 0, 32, 0));

        StackPane tabsBox = createTabs();

        contentPane = new StackPane();
        contentPane.setPrefWidth(416);
        contentPane.setMaxWidth(416);

        loginPanel = createLoginPanel();
        loginPanel.setPrefWidth(416);
        loginPanel.setTranslateX(0);
        loginPanel.setOpacity(1);

        registerPanel = createRegisterPanel();
        registerPanel.setPrefWidth(416);
        registerPanel.setOpacity(0);
        registerPanel.setTranslateX(416);
        registerPanel.setManaged(false);

        contentPane.getChildren().addAll(loginPanel, registerPanel);

        // ScrollPane wraps only the form area so card scrolls internally
        ScrollPane formScroll = new ScrollPane(contentPane);
        formScroll.setFitToWidth(true);
        formScroll.getStyleClass().add("login-scroll");
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setPrefViewportHeight(420);
        formScroll.setMinViewportHeight(420);

        container.getChildren().addAll(titleBox, tabsBox, formScroll);
        rightSide.getChildren().add(container);

        root.getChildren().addAll(leftSide, rightSide);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(LoginView.class.getResource("/styles/login.css").toExternalForm());

        // Responsive font sizing for left-side text
        leftSide.widthProperty().addListener((obs, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            mainTitle.setStyle("-fx-font-size: " + Math.max(26, w * 0.060) + "px;");
            subTitle.setStyle("-fx-font-size: " + Math.max(13, w * 0.025) + "px;");
        });

        // Wire eye animation
        setupEyeAnimation(scene, root);

        return scene;
    }

    private StackPane createTabs() {
        StackPane tabsContainer = new StackPane();
        tabsContainer.getStyleClass().add("tabs-container");
        tabsContainer.setAlignment(Pos.TOP_LEFT);

        HBox tabsBox = new HBox();
        tabsBox.setSpacing(0);

        loginTab = new Label("Log In");
        loginTab.getStyleClass().addAll("tab-label", "tab-active");
        loginTab.setAlignment(Pos.CENTER);
        loginTab.setPrefWidth(204);
        loginTab.setOnMouseClicked(e -> switchToLogin());

        registerTab = new Label("Register");
        registerTab.getStyleClass().addAll("tab-label", "tab-inactive");
        registerTab.setAlignment(Pos.CENTER);
        registerTab.setPrefWidth(204);
        registerTab.setOnMouseClicked(e -> switchToRegister());

        tabsBox.getChildren().addAll(loginTab, registerTab);

        tabIndicator = new javafx.scene.shape.Rectangle(204, 36);
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
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        // Account field
        VBox accountBox = new VBox(8);
        Label accountLabel = new Label("Account (Student ID / Staff ID)");
        accountLabel.getStyleClass().add("form-label");
        loginAccountField = new TextField();
        loginAccountField.setPromptText("Enter your account");
        loginAccountField.getStyleClass().add("form-input");
        loginAccountField.setPrefWidth(Double.MAX_VALUE);
        accountBox.getChildren().addAll(accountLabel, loginAccountField);

        // Password field with visibility toggle
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("form-label");

        loginPasswordWrapper = new StackPane();
        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Enter your password");
        loginPasswordField.getStyleClass().add("form-input");
        loginPasswordField.setPrefWidth(Double.MAX_VALUE);

        loginPasswordVisibleField = new TextField();
        loginPasswordVisibleField.setPromptText("Enter your password");
        loginPasswordVisibleField.getStyleClass().add("form-input");
        loginPasswordVisibleField.setPrefWidth(Double.MAX_VALUE);
        loginPasswordVisibleField.setVisible(false);
        loginPasswordVisibleField.setManaged(false);

        loginPasswordField.textProperty().bindBidirectional(loginPasswordVisibleField.textProperty());

        Label toggleIcon = new Label("👁");
        toggleIcon.getStyleClass().add("password-toggle");
        StackPane.setAlignment(toggleIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleIcon, new Insets(0, 12, 0, 0));
        toggleIcon.setOnMouseClicked(e -> togglePasswordVisibility(toggleIcon));

        loginPasswordWrapper.getChildren().addAll(loginPasswordField, loginPasswordVisibleField, toggleIcon);
        passwordBox.getChildren().addAll(passwordLabel, loginPasswordWrapper);

        // Message label
        Label messageLabel = new Label("");
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPadding(new Insets(10, 0, 0, 0));
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.getStyleClass().add("message-label");

        // Login button
        Button loginButton = new Button("Log In");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setOnAction(e -> {
            String result = userManager.login(
                loginAccountField.getText(),
                loginPasswordField.getText()
            );

            if (result.startsWith("SUCCESS:")) {
                messageLabel.getStyleClass().removeAll("message-error");
                messageLabel.getStyleClass().add("message-success");
                messageLabel.setText("Login successful! Redirecting...");

                String userRole = result.substring(8);
                String studentId = loginAccountField.getText();

                if (userRole.equals("TA")) {
                    new LocalStorageManager().saveLastLogin(loginAccountField.getText(), userRole);
                    new Thread(() -> {
                        try { Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                TAPositionListUI ui = new TAPositionListUI();
                                ui.setCurrentStudentId(studentId);
                                ui.navigateTo();
                            });
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }).start();
                } else if (userRole.equals("ADMIN")) {
                    new LocalStorageManager().saveLastLogin(loginAccountField.getText(), userRole);
                    new Thread(() -> {
                        try { Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                Admin.AdminDashboard dashboard = new Admin.AdminDashboard(studentId);
                                dashboard.navigateTo();
                            });
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }).start();
                } else if (userRole.equals("MO")) {
                    new LocalStorageManager().saveLastLogin(loginAccountField.getText(), userRole);
                    new Thread(() -> {
                        try { Thread.sleep(500);
                            javafx.application.Platform.runLater(() -> {
                                Admin.MODashboard dashboard = new Admin.MODashboard(studentId);
                                dashboard.navigateTo();
                            });
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }).start();
                } else {
                    messageLabel.setText(userRole + " interface under development...");
                    messageLabel.getStyleClass().removeAll("message-error");
                    messageLabel.getStyleClass().add("message-success");
                }
            } else {
                messageLabel.getStyleClass().removeAll("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText(result);
            }
        });

        panel.getChildren().addAll(accountBox, passwordBox, loginButton, messageLabel);

        // Wire focus -> eye state
        addFocusListeners(loginAccountField, loginPasswordField);

        return panel;
    }

    private VBox createRegisterPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(24, 0, 0, 0));

        UserManager userManager = new UserManager();

        // Account field
        VBox accountBox = new VBox(8);
        Label accountLabel = new Label("Account (Student ID / Staff ID)");
        accountLabel.getStyleClass().add("form-label");
        regAccountField = new TextField();
        regAccountField.setPromptText("Enter student or staff ID");
        regAccountField.getStyleClass().add("form-input");
        regAccountField.setPrefWidth(Double.MAX_VALUE);
        accountBox.getChildren().addAll(accountLabel, regAccountField);

        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("form-label");
        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Enter password (min. 6 characters)");
        regPasswordField.getStyleClass().add("form-input");
        regPasswordField.setPrefWidth(Double.MAX_VALUE);
        passwordBox.getChildren().addAll(passwordLabel, regPasswordField);

        // Confirm password field
        VBox confirmBox = new VBox(8);
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.getStyleClass().add("form-label");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter password");
        confirmField.getStyleClass().add("form-input");
        confirmField.setPrefWidth(Double.MAX_VALUE);
        confirmBox.getChildren().addAll(confirmLabel, confirmField);

        // Role combo
        VBox roleBox = new VBox(6);
        Label roleLabel = new Label("Role");
        roleLabel.getStyleClass().add("form-label");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("-- Select Role --", "TA Applicant", "Module Organiser", "System Administrator (Admin)");
        roleCombo.getSelectionModel().select(0);
        roleCombo.getStyleClass().add("form-combo");
        roleCombo.setPrefWidth(Double.MAX_VALUE);
        roleBox.getChildren().addAll(roleLabel, roleCombo);

        // Admin auth code box
        VBox authCodeBox = new VBox(6);
        authCodeBox.setVisible(false);
        authCodeBox.setManaged(false);
        Label authCodeLabel = new Label("Admin Authorisation Code");
        authCodeLabel.getStyleClass().add("form-label");
        PasswordField authCodeInput = new PasswordField();
        authCodeInput.setPromptText("Enter admin authorisation code");
        authCodeInput.getStyleClass().add("form-input");
        authCodeInput.setPrefWidth(Double.MAX_VALUE);
        authCodeBox.getChildren().addAll(authCodeLabel, authCodeInput);

        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdmin = newVal != null && newVal.contains("Admin");
            authCodeBox.setVisible(isAdmin);
            authCodeBox.setManaged(isAdmin);
        });

        // Message label
        Label messageLabel = new Label("");
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setPadding(new Insets(10, 0, 0, 0));
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.getStyleClass().add("message-label");

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(Double.MAX_VALUE);
        registerButton.getStyleClass().add("btn-primary");
        registerButton.setOnAction(e -> {
            if (!regPasswordField.getText().equals(confirmField.getText())) {
                messageLabel.getStyleClass().removeAll("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText("Passwords do not match");
                return;
            }
            String role = roleCombo.getSelectionModel().getSelectedIndex() == 0 ? "" :
                          roleCombo.getSelectionModel().getSelectedItem().toString();

            String result = userManager.register(
                regAccountField.getText(), regPasswordField.getText(),
                role, authCodeInput.getText());

            if (result.equals("SUCCESS")) {
                messageLabel.getStyleClass().removeAll("message-error");
                messageLabel.getStyleClass().add("message-success");
                messageLabel.setText("Registration successful! Please switch to Log In");
                regAccountField.clear();
                regPasswordField.clear();
                confirmField.clear();
                authCodeInput.clear();
                roleCombo.getSelectionModel().select(0);
            } else {
                messageLabel.getStyleClass().removeAll("message-success");
                messageLabel.getStyleClass().add("message-error");
                messageLabel.setText(result);
            }
        });

        panel.getChildren().addAll(accountBox, passwordBox, confirmBox, roleBox, authCodeBox, registerButton, messageLabel);

        // Wire focus -> eye state
        addFocusListeners(regAccountField, regPasswordField);

        return panel;
    }

    private void switchToLogin() {
        if (isLoginActive) {
            return;
        }

        isLoginActive = true;

        loginTab.getStyleClass().remove("tab-inactive");
        loginTab.getStyleClass().add("tab-active");
        registerTab.getStyleClass().remove("tab-active");
        registerTab.getStyleClass().add("tab-inactive");

        indicatorTransition.stop();
        indicatorTransition.setFromX(tabIndicator.getTranslateX());
        indicatorTransition.setToX(0);
        indicatorTransition.play();

        loginPanel.setManaged(true);
        registerPanel.setManaged(true);

        TranslateTransition loginSlideIn = new TranslateTransition(Duration.millis(300), loginPanel);
        loginSlideIn.setFromX(-416);
        loginSlideIn.setToX(0);

        FadeTransition loginFadeIn = new FadeTransition(Duration.millis(300), loginPanel);
        loginFadeIn.setFromValue(0);
        loginFadeIn.setToValue(1);

        ParallelTransition loginIn = new ParallelTransition(loginSlideIn, loginFadeIn);

        TranslateTransition registerSlideOut = new TranslateTransition(Duration.millis(300), registerPanel);
        registerSlideOut.setFromX(0);
        registerSlideOut.setToX(416);

        FadeTransition registerFadeOut = new FadeTransition(Duration.millis(300), registerPanel);
        registerFadeOut.setFromValue(1);
        registerFadeOut.setToValue(0);

        ParallelTransition registerOut = new ParallelTransition(registerSlideOut, registerFadeOut);

        registerOut.setOnFinished(e -> {
            registerPanel.setTranslateX(416);
            registerPanel.setOpacity(0);
            registerPanel.setManaged(false);
        });

        registerOut.play();
        loginIn.play();
    }

    private void switchToRegister() {
        if (!isLoginActive) {
            return;
        }

        isLoginActive = false;

        loginTab.getStyleClass().remove("tab-active");
        loginTab.getStyleClass().add("tab-inactive");
        registerTab.getStyleClass().remove("tab-inactive");
        registerTab.getStyleClass().add("tab-active");

        indicatorTransition.stop();
        indicatorTransition.setFromX(tabIndicator.getTranslateX());
        indicatorTransition.setToX(204);
        indicatorTransition.play();

        loginPanel.setManaged(true);
        registerPanel.setManaged(true);

        TranslateTransition registerSlideIn = new TranslateTransition(Duration.millis(300), registerPanel);
        registerSlideIn.setFromX(416);
        registerSlideIn.setToX(0);

        FadeTransition registerFadeIn = new FadeTransition(Duration.millis(300), registerPanel);
        registerFadeIn.setFromValue(0);
        registerFadeIn.setToValue(1);

        ParallelTransition registerIn = new ParallelTransition(registerSlideIn, registerFadeIn);

        TranslateTransition loginSlideOut = new TranslateTransition(Duration.millis(300), loginPanel);
        loginSlideOut.setFromX(0);
        loginSlideOut.setToX(-416);

        FadeTransition loginFadeOut = new FadeTransition(Duration.millis(300), loginPanel);
        loginFadeOut.setFromValue(1);
        loginFadeOut.setToValue(0);

        ParallelTransition loginOut = new ParallelTransition(loginSlideOut, loginFadeOut);

        loginOut.setOnFinished(e -> {
            loginPanel.setTranslateX(-416);
            loginPanel.setOpacity(0);
            loginPanel.setManaged(false);
        });

        loginOut.play();
        registerIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
