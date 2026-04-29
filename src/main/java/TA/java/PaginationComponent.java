package TA.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PaginationComponent {

    public interface PageChangeListener {
        void onPageChanged(int newPage);
    }

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private PageChangeListener listener;

    private HBox paginationBox;
    private Button prevButton;
    private Button nextButton;
    private Label pageInfoLabel;

    public PaginationComponent(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 1;
    }

    public void setPageChangeListener(PageChangeListener listener) {
        this.listener = listener;
    }

    public HBox createComponent() {
        paginationBox = new HBox();
        paginationBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 1;");
        paginationBox.setPadding(new Insets(16, 20, 16, 20));
        paginationBox.setSpacing(16);
        paginationBox.setAlignment(Pos.CENTER);

        prevButton = new Button("Previous");
        prevButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateUI();
                if (listener != null) {
                    listener.onPageChanged(currentPage);
                }
            }
        });

        pageInfoLabel = new Label();
        pageInfoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        nextButton = new Button("Next");
        nextButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateUI();
                if (listener != null) {
                    listener.onPageChanged(currentPage);
                }
            }
        });

        paginationBox.getChildren().addAll(prevButton, pageInfoLabel, nextButton);
        return paginationBox;
    }

    public void updateData(int totalItems) {
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (paginationBox != null) {
            updateUI();
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        if (paginationBox != null) {
            updateUI();
        }
    }

    private void updateUI() {
        if (prevButton == null || nextButton == null || pageInfoLabel == null) {
            return;
        }
        
        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == totalPages);

        if (currentPage == 1) {
            prevButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #cccccc; -fx-background-color: #f5f5f5; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: not-allowed;");
        } else {
            prevButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        }

        if (currentPage == totalPages) {
            nextButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #cccccc; -fx-background-color: #f5f5f5; -fx-border-color: #eeeeee; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: not-allowed;");
        } else {
            nextButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1; -fx-padding: 6 16 6 16; -fx-cursor: hand;");
        }

        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
    }
}
