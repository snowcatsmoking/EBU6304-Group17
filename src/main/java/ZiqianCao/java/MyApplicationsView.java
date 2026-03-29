package ZiqianCao.java;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MyApplicationsView {

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #fafafa;");

        VBox content = new VBox();
        content.setPadding(new Insets(40, 40, 40, 40));
        content.setSpacing(20);
        content.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("我的申请");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label emptyLabel = new Label("暂无申请记录");
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");

        content.getChildren().addAll(titleLabel, emptyLabel);
        root.setCenter(content);

        return root;
    }
}
