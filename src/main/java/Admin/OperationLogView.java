package Admin;

import data.LogManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class OperationLogView {

    private final LogManager logManager;

    public OperationLogView() {
        this.logManager = new LogManager();
    }

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #fafafa;");

        // ── Page title ───────────────────────────────────────────────
        Label pageTitle = new Label("Operation Logs");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        List<LogManager.LogEntry> logs = logManager.getAllLogs();

        Label countLabel = new Label(logs.isEmpty() ? "No records yet." : logs.size() + " record(s)");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        // ── Card ─────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");

        // Header
        HBox headerRow = new HBox();
        headerRow.setPadding(new Insets(10, 20, 10, 20));
        headerRow.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        headerRow.getChildren().addAll(
            headerCell("Time",      160),
            headerCell("Operator",  120),
            headerCell("Action",    110),
            headerCell("Target",    130),
            headerCell("Detail",    300)
        );
        card.getChildren().add(headerRow);

        if (logs.isEmpty()) {
            Label empty = new Label("No operation records yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 24 20 24 20;");
            card.getChildren().add(empty);
        } else {
            for (LogManager.LogEntry entry : logs) {
                card.getChildren().add(buildLogRow(entry));
            }
        }

        page.getChildren().addAll(pageTitle, countLabel, card);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa; -fx-background: #fafafa;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private Label headerCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private HBox buildLogRow(LogManager.LogEntry entry) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0; -fx-background-color: #ffffff;");

        Label lTime = cell(entry.getTimestamp(), 160, "#aaaaaa");
        Label lOp   = cell(entry.getAdminId(),   120, "#222222");
        Label lAct  = cell(entry.getAction(),     110, "#222222");
        Label lTgt  = cell(entry.getTarget(),     130, "#555555");
        Label lDet  = cell(entry.getDetail() != null ? entry.getDetail() : "—", 300, "#888888");

        row.getChildren().addAll(lTime, lOp, lAct, lTgt, lDet);
        return row;
    }

    private Label cell(String text, double width, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
        lbl.setPrefWidth(width);
        return lbl;
    }
}
