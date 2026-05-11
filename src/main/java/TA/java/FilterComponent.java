package TA.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class FilterComponent {

    public interface FilterListener {
        void onFilter(String courseName, String availableTime, String recruitmentCount);
        void onReset();
    }

    private FilterListener listener;

    private TextField courseNameField;
    private TextField availableTimeField;
    private TextField recruitmentCountField;

    public FilterComponent() {
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    public HBox createComponent() {
        HBox filterBox = new HBox();
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course Name:");
        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        courseNameField = new TextField();
        courseNameField.setPromptText("Enter course name");
        courseNameField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        courseNameField.setPrefWidth(150);

        Label timeLabel = new Label("Available Time:");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        availableTimeField = new TextField();
        availableTimeField.setPromptText("YYYY-MM-DD");
        availableTimeField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        availableTimeField.setPrefWidth(150);

        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        recruitmentCountField = new TextField();
        recruitmentCountField.setPromptText("Enter number");
        recruitmentCountField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        recruitmentCountField.setPrefWidth(100);

        Button filterButton = new Button("Filter");
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        filterButton.setOnMouseEntered(e ->
            filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;")
        );
        filterButton.setOnMouseExited(e ->
            filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;")
        );
        filterButton.setOnAction(e -> applyFilter());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
        resetButton.setOnAction(e -> resetFilter());

        filterBox.getChildren().addAll(courseLabel, courseNameField, timeLabel, availableTimeField, countLabel, recruitmentCountField, filterButton, resetButton);
        return filterBox;
    }

    private void applyFilter() {
        if (listener != null) {
            listener.onFilter(
                courseNameField.getText().trim(),
                availableTimeField.getText().trim(),
                recruitmentCountField.getText().trim()
            );
        }
    }

    private void resetFilter() {
        courseNameField.clear();
        availableTimeField.clear();
        recruitmentCountField.clear();
        if (listener != null) {
            listener.onReset();
        }
    }
}
