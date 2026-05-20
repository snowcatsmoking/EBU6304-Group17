package Admin;

import TA.java.TAApplication;
import TA.java.TAApplicationRecord;
import TA.java.TAApplicationRecordManager;
import data.UserDataManager;
import data.WorkloadConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class WorkloadView {

    private final WorkloadConfigManager configManager;
    private final UserDataManager userDataManager;
    private final TAApplicationRecordManager appRecordManager;

    public WorkloadView() {
        this.configManager    = new WorkloadConfigManager();
        this.userDataManager  = new UserDataManager();
        this.appRecordManager = new TAApplicationRecordManager();
    }

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #f8fafc;");

        Label pageTitle = new Label("Workload Alert");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Set the maximum number of approved positions per TA per semester and monitor overloads.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Threshold card (holds a ref to the table card so we can refresh it)
        VBox[] tableCardHolder = new VBox[1];
        VBox thresholdCard = buildThresholdCard(page, tableCardHolder);

        // Build workload table with current threshold
        int threshold = configManager.getThreshold();
        Map<String, Integer> approvedCount = buildApprovedCountMap();
        VBox tableCard = buildWorkloadCard(approvedCount, threshold);
        tableCardHolder[0] = tableCard;

        page.getChildren().addAll(pageTitle, subtitle, thresholdCard, tableCard);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Threshold setting card ────────────────────────────────────────

    private VBox buildThresholdCard(VBox page, VBox[] tableCardHolder) {
        VBox card = buildCard("Threshold Setting");

        HBox body = new HBox(12);
        body.setPadding(new Insets(20));
        body.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Max approved positions per semester:");
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        TextField field = new TextField(String.valueOf(configManager.getThreshold()));
        field.setPrefWidth(70);
        field.setStyle(
            "-fx-font-size: 13px; -fx-background-color: #ffffff;" +
            "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 6 10 6 10;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;");

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
            "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1;" +
            "-fx-border-color: #6366f1; -fx-border-width: 1; -fx-padding: 6 18 6 18; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;");

        Label feedback = new Label();
        feedback.setStyle("-fx-font-size: 12px;");

        saveBtn.setOnAction(e -> {
            String text = field.getText().trim();
            int value;
            try {
                value = Integer.parseInt(text);
                if (value < 1) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedback.setText("Please enter a positive integer.");
                feedback.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
                return;
            }
            configManager.saveThreshold(value);
            feedback.setText("Saved.");
            feedback.setStyle("-fx-font-size: 12px; -fx-text-fill: #16a34a;");

            // Refresh the workload table with the new threshold
            Map<String, Integer> counts = buildApprovedCountMap();
            VBox newTable = buildWorkloadCard(counts, value);
            int idx = page.getChildren().indexOf(tableCardHolder[0]);
            if (idx >= 0) page.getChildren().set(idx, newTable);
            tableCardHolder[0] = newTable;
        });

        body.getChildren().addAll(label, field, saveBtn, feedback);
        card.getChildren().add(body);
        return card;
    }

    // ── Workload table card ───────────────────────────────────────────

    private VBox buildWorkloadCard(Map<String, Integer> approvedCount, int threshold) {
        long overCount  = approvedCount.values().stream().filter(n -> n > threshold).count();
        long warnCount  = approvedCount.values().stream().filter(n -> n == threshold).count();
        String cardTitle = "TA Workload Overview";
        if (overCount > 0 || warnCount > 0) {
            cardTitle += "  —  " + overCount + " overload, " + warnCount + " at limit";
        }

        VBox card = buildCard(cardTitle);

        VBox table = new VBox(0);
        table.getChildren().add(buildHeaderRow());

        List<TAApplication> tas = userDataManager.getAllTAs();
        if (tas.isEmpty()) {
            HBox empty = new HBox(new Label("No TAs registered yet."));
            empty.setPadding(new Insets(20));
            ((Label) empty.getChildren().get(0))
                .setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
            table.getChildren().add(empty);
        } else {
            // Sort: overload first, then warning, then normal
            tas.sort(Comparator.comparingInt(
                (TAApplication ta) -> approvedCount.getOrDefault(ta.getTAId(), 0)).reversed());
            for (TAApplication ta : tas) {
                int count = approvedCount.getOrDefault(ta.getTAId(), 0);
                table.getChildren().add(buildDataRow(ta, count, threshold));
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private HBox buildHeaderRow() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        row.getChildren().addAll(
            headerCell("Student ID",          150),
            headerCell("Name",                150),
            headerCell("Major",               200),
            headerCell("Approved Positions",  160),
            headerCell("Status",              160)
        );
        return row;
    }

    private HBox buildDataRow(TAApplication ta, int count, int threshold) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);

        String rowBg;
        if (count > threshold)      rowBg = "#fff5f5";
        else if (count == threshold) rowBg = "#fffbf0";
        else                         rowBg = "#ffffff";
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0; -fx-background-color: " + rowBg + ";");

        Label lId    = cell(nvl(ta.getTAId()),   150, "#1e293b");
        Label lName  = cell(nvl(ta.getName()),   150, "#1e293b");
        Label lMajor = cell(nvl(ta.getMajor()),  200, "#64748b");
        Label lCount = cell(String.valueOf(count), 160, "#334155");

        Label statusLabel;
        if (count > threshold) {
            statusLabel = statusBadge("Overloaded", "#dc2626", "#fee2e2", "#fecaca");
        } else if (count == threshold) {
            statusLabel = statusBadge("At Limit", "#d97706", "#fffbeb", "#fde68a");
        } else {
            statusLabel = statusBadge("Normal", "#16a34a", "#f0fdf4", "#bbf7d0");
        }

        HBox statusWrap = new HBox(statusLabel);
        statusWrap.setPrefWidth(160);
        statusWrap.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(lId, lName, lMajor, lCount, statusWrap);
        return row;
    }

    // ── Data aggregation ──────────────────────────────────────────────

    private Map<String, Integer> buildApprovedCountMap() {
        Map<String, Integer> map = new HashMap<>();
        for (TAApplicationRecord app : appRecordManager.getAllApplications()) {
            if (TAApplicationRecord.STATUS_APPROVED.equals(app.getStatus())) {
                map.merge(app.getTaStudentId(), 1, Integer::sum);
            }
        }
        return map;
    }

    // ── Shared helpers ────────────────────────────────────────────────

    private VBox buildCard(String title) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        HBox header = new HBox();
        header.setPadding(new Insets(16, 20, 14, 20));
        header.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        header.getChildren().add(titleLabel);
        card.getChildren().add(header);
        return card;
    }

    private Label headerCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private Label cell(String text, double width, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private Label statusBadge(String text, String textColor, String bgColor, String borderColor) {
        Label lbl = new Label(text);
        lbl.setStyle(String.format(
            "-fx-font-size: 11px; -fx-text-fill: %s; -fx-background-color: %s;" +
            "-fx-border-color: %s; -fx-border-width: 1; -fx-padding: 2 8 2 8;",
            textColor, bgColor, borderColor));
        return lbl;
    }

    private String nvl(String s) {
        return s != null && !s.isEmpty() ? s : "—";
    }
}
