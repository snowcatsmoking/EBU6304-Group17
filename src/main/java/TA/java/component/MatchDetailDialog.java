package TA.java.component;
import TA.java.model.MatchingResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MatchDetailDialog {

    public static void show(MatchingResult result, String positionName, String courseName) {
        Stage stage = new Stage();
        stage.setTitle("Match Details - " + positionName);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);

        VBox root = new VBox();
        root.setPadding(new Insets(30));
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #ffffff;");

        // Header with percentage
        HBox headerBox = new HBox();
        headerBox.setSpacing(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("AI Match Analysis");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        int pct = result.getPercentage();
        String barColor = pct >= 70 ? "#22c55e" : pct >= 40 ? "#f59e0b" : "#ef4444";

        Label percentLabel = new Label(pct + "%");
        percentLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: " + barColor + ";");

        headerBox.getChildren().addAll(titleLabel);
        HBox spacer = new HBox();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Score section
        VBox scoreBox = new VBox();
        scoreBox.setSpacing(10);
        scoreBox.setPadding(new Insets(20));
        scoreBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");

        HBox scoreRow = new HBox();
        scoreRow.setSpacing(16);
        scoreRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(pct / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(14);
        progressBar.setStyle("-fx-accent: " + barColor + ";");

        percentLabel = new Label(pct + "% Match");
        percentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: " + barColor + ";");

        scoreRow.getChildren().addAll(progressBar, percentLabel);

        Label reasonLabel = new Label(result.getReason() != null ? result.getReason() : "No summary provided");
        reasonLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-wrap-text: true;");

        scoreBox.getChildren().addAll(scoreRow, reasonLabel);

        // Analysis section
        VBox analysisBox = createSection("Analysis",
                result.getAnalysis() != null ? result.getAnalysis() : "No detailed analysis available.");

        // Improvements section
        VBox improvementsBox = createSection("How to Improve",
                result.getImprovements() != null ? result.getImprovements() : "No improvement suggestions available.");

        // Position info
        HBox infoBox = new HBox();
        infoBox.setSpacing(30);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 0, 0, 0));

        Label posLabel = new Label("Position: " + positionName);
        posLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        Label courseLabel = new Label("Course: " + courseName);
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        infoBox.getChildren().addAll(posLabel, courseLabel);

        root.getChildren().addAll(titleLabel, scoreBox, analysisBox, improvementsBox, infoBox);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(scrollPane, 520, 520);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }

    private static VBox createSection(String title, String content) {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-wrap-text: true; -fx-line-spacing: 4px;");
        contentLabel.setMaxWidth(440);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }
}
