package TA.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PositionListComponent {

    public interface PositionActionListener {
        void onApply(TAJob job);
        void onCompleteProfile();
    }

    private PositionActionListener listener;
    private FavoriteManager favoriteManager;
    private TAApplicationRecordManager recordManager;
    private String currentStudentId;

    public PositionListComponent(FavoriteManager favoriteManager, TAApplicationRecordManager recordManager, String currentStudentId) {
        this.favoriteManager = favoriteManager;
        this.recordManager = recordManager;
        this.currentStudentId = currentStudentId;
    }

    public void setPositionActionListener(PositionActionListener listener) {
        this.listener = listener;
    }

    public VBox createPositionList(List<TAJob> jobs, int currentPage, int pageSize) {
        VBox positionListBox = new VBox();
        positionListBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        positionListBox.setSpacing(0);

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, jobs.size());

        for (int i = start; i < end; i++) {
            TAJob job = jobs.get(i);
            VBox positionBox = createPositionBox(job);
            positionListBox.getChildren().add(positionBox);
        }

        return positionListBox;
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
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        boolean isFav = favoriteManager.isFavorite(currentStudentId, job.getJobId());
        Button favButton = new Button(isFav ? "★" : "☆");
        favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (isFav ? "#ffd700" : "#cccccc") + "; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
        favButton.setOnAction(e -> toggleFavorite(job, favButton));

        boolean manuallyClosed = job.isActive();
        boolean expired = isDeadlineExpired(job);

        if (manuallyClosed) {
            Label badge = new Label("Closed");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, favButton);
        } else if (expired) {
            Label badge = new Label("Expired");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #b08800; -fx-background-color: #fffbe6; -fx-border-color: #e0c860; -fx-border-width: 1; -fx-padding: 3 8 3 8;");
            titleBox.getChildren().addAll(titleLabel, badge, favButton);
        } else {
            titleBox.getChildren().addAll(titleLabel, favButton);
        }

        HBox infoBox = new HBox();
        infoBox.setSpacing(24);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course/Activity: " + job.getCourseName());
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label countLabel = new Label("Openings: " + job.getRecruitmentCount());
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label requirementLabel = new Label("Requirements: " + job.getRequirements());
        requirementLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox deadlineBox = new HBox();
        deadlineBox.setSpacing(24);
        deadlineBox.setAlignment(Pos.CENTER_LEFT);

        Label deadlineLabel = new Label("Deadline: " + job.getDeadline());
        deadlineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        Label publisherLabel = new Label("Posted By: " + job.getPublisher());
        publisherLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        if (manuallyClosed) {
            Button closedButton = new Button("Closed by Organiser");
            closedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #999999; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            closedButton.setDisable(true);
            actionBox.getChildren().add(closedButton);
        } else if (expired) {
            Button expiredButton = new Button("Deadline Passed");
            expiredButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #999999; -fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: not-allowed;");
            expiredButton.setDisable(true);
            actionBox.getChildren().add(expiredButton);
        } else {
            boolean hasApplied = recordManager.hasDuplicateApplication(currentStudentId, job.getJobId());
            boolean profileComplete = checkProfileComplete();

            if (hasApplied) {
                Button appliedButton = new Button("Applied");
                appliedButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #1890ff; -fx-padding: 6 20 6 20; -fx-cursor: default;");
                appliedButton.setDisable(true);
                actionBox.getChildren().add(appliedButton);
            } else if (!profileComplete) {
                Button incompleteButton = new Button("Complete Profile");
                incompleteButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeeba; -fx-border-width: 1; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                incompleteButton.setOnAction(e -> {
                    if (listener != null) {
                        listener.onCompleteProfile();
                    }
                });
                actionBox.getChildren().add(incompleteButton);
            } else {
                Button applyButton = new Button("Apply");
                applyButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 20 6 20; -fx-cursor: hand;");
                applyButton.setOnAction(e -> {
                    if (listener != null) {
                        listener.onApply(job);
                    }
                });
                actionBox.getChildren().add(applyButton);
            }
        }

        infoBox.getChildren().addAll(courseLabel, countLabel, requirementLabel);
        deadlineBox.getChildren().addAll(deadlineLabel, publisherLabel);
        positionBox.getChildren().addAll(titleBox, infoBox, deadlineBox, actionBox);

        return positionBox;
    }

    private void toggleFavorite(TAJob job, Button favButton) {
        boolean isCurrentlyFav = favoriteManager.isFavorite(currentStudentId, job.getJobId());
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), favButton);
        scaleUp.setToX(1.5);
        scaleUp.setToY(1.5);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), favButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        
        SequentialTransition anim = new SequentialTransition(scaleUp, scaleDown);
        
        if (isCurrentlyFav) {
            favoriteManager.removeFavorite(currentStudentId, job.getJobId());
            scaleUp.setOnFinished(ev -> {
                favButton.setText("☆");
                favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #cccccc; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
            });
        } else {
            favoriteManager.addFavorite(currentStudentId, job.getJobId());
            scaleUp.setOnFinished(ev -> {
                favButton.setText("★");
                favButton.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffd700; -fx-background-color: transparent; -fx-cursor: hand; -fx-border: none;");
            });
        }
        anim.play();
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

    private boolean checkProfileComplete() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(data.DataConfig.TA_DIR + currentStudentId + ".json");
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
}
