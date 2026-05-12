package TA.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FilterComponent {

    public interface FilterListener {
        void onFilter(String courseName, String availableTime, String recruitmentCount);
        void onReset();
    }

    private FilterListener listener;

    private TextField courseNameField;
    private DatePicker availableTimeField;
    private TextField recruitmentCountField;

    public FilterComponent() {
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    public HBox createComponent() {
        VBox mainContainer = new VBox();
        mainContainer.setSpacing(12);

        HBox filterRow = new HBox();
        filterRow.setSpacing(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        VBox courseBox = new VBox();
        courseBox.setSpacing(4);
        Label courseLabel = new Label("Course Name:");
        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        courseNameField = new TextField();
        courseNameField.setPromptText("Enter course name");
        courseNameField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        courseNameField.setPrefWidth(200);
        courseBox.getChildren().addAll(courseLabel, courseNameField);

        VBox timeBox = new VBox();
        timeBox.setSpacing(4);
        Label timeLabel = new Label("Available Time:");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        availableTimeField = new DatePicker();
        availableTimeField.setPromptText("Select date");
        availableTimeField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        availableTimeField.setPrefWidth(200);
        timeBox.getChildren().addAll(timeLabel, availableTimeField);

        VBox countBox = new VBox();
        countBox.setSpacing(4);
        Label countLabel = new Label("Openings:");
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
        recruitmentCountField = new TextField();
        recruitmentCountField.setPromptText("Enter number");
        recruitmentCountField.setStyle("-fx-font-size: 14px; -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
        recruitmentCountField.setPrefWidth(120);
        countBox.getChildren().addAll(countLabel, recruitmentCountField);

        filterRow.getChildren().addAll(courseBox, timeBox, countBox);

        HBox buttonRow = new HBox();
        buttonRow.setSpacing(12);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button filterButton = new Button("Filter");
        filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        filterButton.setOnMouseEntered(e ->
            filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;")
        );
        filterButton.setOnMouseExited(e ->
            filterButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;")
        );
        filterButton.setOnAction(e -> applyFilter());

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 24 8 24; -fx-cursor: hand;");
        resetButton.setOnAction(e -> resetFilter());

        buttonRow.getChildren().addAll(filterButton, resetButton);

        mainContainer.getChildren().addAll(filterRow, buttonRow);

        HBox wrapper = new HBox();
        wrapper.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        wrapper.setPadding(new Insets(16, 16, 16, 16));
        wrapper.setHgrow(mainContainer, javafx.scene.layout.Priority.ALWAYS);
        wrapper.getChildren().add(mainContainer);

        return wrapper;
    }

    private void applyFilter() {
        if (listener != null) {
            String availableTimeStr = "";
            if (availableTimeField.getValue() != null) {
                availableTimeStr = availableTimeField.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            listener.onFilter(
                courseNameField.getText().trim(),
                availableTimeStr,
                recruitmentCountField.getText().trim()
            );
        }
    }

    private void resetFilter() {
        courseNameField.clear();
        availableTimeField.setValue(null);
        recruitmentCountField.clear();
        if (listener != null) {
            listener.onReset();
        }
    }
}
