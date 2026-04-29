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
        filterBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        filterBox.setPadding(new Insets(16, 16, 16, 16));
        filterBox.setSpacing(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label courseLabel = new Label("Course Name:");
        courseLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        courseNameField = new TextField();
        courseNameField.setPromptText("Enter course name");
        courseNameField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        courseNameField.setPrefWidth(150);

        Label timeLabel = new Label("Available Time:");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        availableTimeField = new TextField();
        availableTimeField.setPromptText("YYYY-MM-DD");
        availableTimeField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        availableTimeField.setPrefWidth(150);

        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");
        recruitmentCountField = new TextField();
        recruitmentCountField.setPromptText("Enter number");
        recruitmentCountField.setStyle("-fx-font-size: 13px; -fx-padding: 6 12 6 12; -fx-border-color: #cccccc; -fx-border-width: 1;");
        recruitmentCountField.setPrefWidth(100);

        Button filterButton = new Button("Filter");
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        filterButton.setOnAction(e -> applyFilter());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
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
