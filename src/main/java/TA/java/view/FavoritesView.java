package TA.java.view;
import TA.java.TAJob;
import TA.java.service.FavoriteManager;
import TA.java.model.Favorite;
import TA.java.TAApplicationRecordManager;
import TA.java.service.MatchingService;
import TA.java.model.MatchingResult;
import TA.java.component.MatchDetailDialog;
import TA.java.SkillUtils;
import TA.java.utils.TAApplicationUtils;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FavoritesView {

    public interface NavigationListener {
        void onNavigateTo(String viewName);
    }

    public interface ApplicationListener {
        void onApplyForPosition(TAJob job);
        void onCompleteProfile();
    }

    private String currentStudentId;
    private FavoriteManager favoriteManager;
    private TAApplicationRecordManager recordManager;
    private NavigationListener navigationListener;
    private ApplicationListener applicationListener;
    private MatchingService matchingService;

    public FavoritesView(String studentId, FavoriteManager favoriteManager, TAApplicationRecordManager recordManager) {
        this.currentStudentId = studentId;
        this.favoriteManager = favoriteManager;
        this.recordManager = recordManager;
    }

    public void setMatchingService(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    public void setNavigationListener(NavigationListener listener) {
        this.navigationListener = listener;
    }

    public void setApplicationListener(ApplicationListener listener) {
        this.applicationListener = listener;
    }

    public VBox getView() {
        VBox content = new VBox();
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setSpacing(20);

        Label titleLabel = new Label("My Favorite Positions");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        VBox favoritesList = new VBox();
        favoritesList.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        favoritesList.setSpacing(0);

        List<Favorite> favorites = favoriteManager.getFavoritesByTA(currentStudentId);
        if (favorites.isEmpty()) {
            Label emptyLabel = new Label("No favorites yet. Click the star icon on positions to add them here.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setPadding(new Insets(40, 20, 40, 20));
            favoritesList.getChildren().add(emptyLabel);
        } else {
            data.JobDataManager jobDataManager = new data.JobDataManager();
            for (Favorite fav : favorites) {
                TAJob job = jobDataManager.getJobById(fav.getJobId());
                if (job != null) {
                    VBox favBox = createFavoritePositionBox(job);
                    favoritesList.getChildren().add(favBox);
                }
            }
        }

        content.getChildren().addAll(titleLabel, favoritesList);
        core.UiText.localize(content);
        return content;
    }

    private VBox createFavoritePositionBox(TAJob job) {
        VBox positionBox = new VBox();
        positionBox.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        positionBox.setPadding(new Insets(16, 16, 16, 16));
        positionBox.setSpacing(8);
        positionBox.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox();
        titleBox.setSpacing(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(job.getPositionName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        Button unfavButton = new Button("★");
        unfavButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffd700; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
        unfavButton.setOnAction(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), unfavButton);
            scaleUp.setToX(1.5);
            scaleUp.setToY(1.5);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), unfavButton);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

            SequentialTransition anim = new SequentialTransition(scaleUp, scaleDown);

            scaleUp.setOnFinished(ev -> {
                favoriteManager.removeFavorite(currentStudentId, job.getJobId());
                if (navigationListener != null) {
                    navigationListener.onNavigateTo("favorites");
                }
            });

            anim.play();
        });

        boolean manuallyClosed = job.isActive();
        boolean expired = isDeadlineExpired(job);

        if (manuallyClosed) {
            Label badge = new Label("Closed");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, unfavButton);
        } else if (expired) {
            Label badge = new Label("Expired");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #b45309; -fx-background-color: #fef3c7; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, unfavButton);
        } else {
            titleBox.getChildren().addAll(titleLabel, unfavButton);
        }

        HBox infoBox = new HBox();
        infoBox.setSpacing(24);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course/Activity: " + job.getCourseName());
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label countLabel = new Label("Openings: " + job.getRecruitmentCount());
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label requirementLabel = new Label("Requirements: " + job.getRequirements());
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        requirementLabel.setWrapText(true);

        Label requiredSkillsLabel = new Label("Required Skills: "
            + SkillUtils.toDisplayText(job.getRequiredSkills(), "No specific skill requirements"));
        requiredSkillsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        requiredSkillsLabel.setWrapText(true);

        HBox deadlineBox = new HBox();
        deadlineBox.setSpacing(24);
        deadlineBox.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("Deadline: " + job.getDeadline());
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label publisherLabel = new Label("Posted By: " + job.getPublisher());
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        if (manuallyClosed) {
            Button closedButton = new Button("Closed by Organiser");
            closedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            closedButton.setDisable(true);
            actionBox.getChildren().add(closedButton);
        } else if (expired) {
            Button expiredButton = new Button("Deadline Passed");
            expiredButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            expiredButton.setDisable(true);
            actionBox.getChildren().add(expiredButton);
        } else {
            boolean hasApplied = recordManager.hasDuplicateApplication(currentStudentId, job.getJobId());
            boolean profileComplete = TAApplicationUtils.checkProfileComplete(currentStudentId);

            if (hasApplied) {
                Button appliedButton = new Button("Applied");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else if (!profileComplete) {
                Button incompleteButton = new Button("Complete Profile");
                incompleteButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #b45309; -fx-background-color: #fef3c7; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                incompleteButton.setOnAction(e -> {
                    if (applicationListener != null) {
                        applicationListener.onCompleteProfile();
                    }
                });
                actionBox.getChildren().add(incompleteButton);
            } else {
                Button applyButton = new Button("Apply");
                applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                applyButton.setOnMouseEntered(e ->
                    applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: hand;")
                );
                applyButton.setOnMouseExited(e ->
                    applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 6 20 6 20; -fx-cursor: hand;")
                );
                applyButton.setOnAction(e -> {
                    if (applicationListener != null) {
                        applicationListener.onApplyForPosition(job);
                    }
                });
                actionBox.getChildren().add(applyButton);
            }
        }

        infoBox.getChildren().addAll(courseLabel, countLabel, requirementLabel);
        deadlineBox.getChildren().addAll(deadlineLabel, publisherLabel);

        HBox matchingBox = createMatchingBox(job);
        positionBox.getChildren().addAll(titleBox, infoBox, requiredSkillsLabel, deadlineBox, actionBox, matchingBox);

        return positionBox;
    }

    private HBox createMatchingBox(TAJob job) {
        HBox matchingBox = new HBox();
        matchingBox.setAlignment(Pos.CENTER_LEFT);
        matchingBox.setPadding(new Insets(4, 0, 0, 0));
        matchingBox.setSpacing(10);

        boolean profileComplete = TA.java.utils.TAApplicationUtils.checkProfileComplete(currentStudentId);
        if (!profileComplete || matchingService == null) {
            return matchingBox;
        }

        MatchingResult cached = matchingService.getCachedResult(currentStudentId, job.getJobId());
        if (cached != null) {
            showMatchingBar(matchingBox, cached, job);
        } else {
            showMatchButton(matchingBox, job);
        }

        return matchingBox;
    }

    private void showMatchButton(HBox matchingBox, TAJob job) {
        Button matchButton = new Button("AI Match");
        matchButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #6366f1; -fx-background-color: #eef2ff; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 14 4 14; -fx-cursor: hand;");
        matchButton.setOnMouseEntered(e ->
            matchButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 14 4 14; -fx-cursor: hand;")
        );
        matchButton.setOnMouseExited(e ->
            matchButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #6366f1; -fx-background-color: #eef2ff; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 14 4 14; -fx-cursor: hand;")
        );
        matchButton.setOnAction(e -> {
            matchingBox.getChildren().clear();
            Label loadingLabel = new Label("Matching...");
            loadingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6366f1;");
            ProgressBar loadingBar = new ProgressBar();
            loadingBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            loadingBar.setPrefWidth(80);
            loadingBar.setStyle("-fx-accent: #6366f1;");
            matchingBox.getChildren().addAll(loadingLabel, loadingBar);

            new Thread(() -> {
                try {
                    MatchingResult result = matchingService.computeMatch(currentStudentId, job.getJobId());
                    Platform.runLater(() -> {
                        matchingBox.getChildren().clear();
                        showMatchingBar(matchingBox, result, job);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        matchingBox.getChildren().clear();
                        Label errorLabel = new Label("Match failed");
                        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
                        matchingBox.getChildren().add(errorLabel);
                    });
                }
            }).start();
        });
        matchingBox.getChildren().add(matchButton);
    }

    private void showMatchingBar(HBox matchingBox, MatchingResult result, TAJob job) {
        Label matchLabel = new Label("Match:");
        matchLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        int percentage = result.getPercentage();
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setPrefWidth(100);
        String barColor = percentage >= 70 ? "#22c55e" : percentage >= 40 ? "#f59e0b" : "#ef4444";
        progressBar.setStyle("-fx-accent: " + barColor + ";");

        Label percentLabel = new Label(percentage + "%");
        percentLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: " + barColor + ";");

        Button detailButton = new Button("Details");
        detailButton.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-background-color: transparent; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 8 2 8; -fx-cursor: hand;");
        detailButton.setOnMouseEntered(e ->
            detailButton.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-border-color: #6366f1; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 8 2 8; -fx-cursor: hand;")
        );
        detailButton.setOnMouseExited(e ->
            detailButton.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-background-color: transparent; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2 8 2 8; -fx-cursor: hand;")
        );
        detailButton.setOnAction(e -> MatchDetailDialog.show(result, job.getPositionName(), job.getCourseName()));

        matchingBox.getChildren().addAll(matchLabel, progressBar, percentLabel, detailButton);

        if (result.getReason() != null && !result.getReason().isEmpty()) {
            Tooltip tooltip = new Tooltip(result.getReason());
            Tooltip.install(matchingBox, tooltip);
        }
    }

    private boolean isDeadlineExpired(TAJob job) {
        if (job.getDeadline() == null || job.getDeadline().trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate deadline = LocalDate.parse(job.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return deadline.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}
