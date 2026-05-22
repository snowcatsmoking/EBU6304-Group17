package Admin;

import TA.java.TAApplication;
import TA.java.TAApplicationRecord;
import TA.java.TAApplicationRecordManager;
import LoginScreen.User;
import data.JobDataManager;
import data.LogManager;
import data.UserDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class DashboardView {

    private final UserDataManager userDataManager;
    private final JobDataManager jobDataManager;
    private final TAApplicationRecordManager appRecordManager;
    private final LogManager logManager;

    public DashboardView() {
        this.userDataManager   = new UserDataManager();
        this.jobDataManager    = new JobDataManager();
        this.appRecordManager  = new TAApplicationRecordManager();
        this.logManager        = new LogManager();
    }

    public ScrollPane build() {
        VBox page = new VBox();
        page.setSpacing(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #f8fafc;");

        // ── Page title ───────────────────────────────────────────────
        Label pageTitle = new Label("Dashboard");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // ── Stats cards ──────────────────────────────────────────────
        List<TAApplication> tas   = userDataManager.getAllTAs();
        List<User>          mos   = userDataManager.getAllMOs();
        List<?>             jobs  = jobDataManager.getAllJobs();
        List<TAApplicationRecord> apps = appRecordManager.getAllApplications();
        long accepted = apps.stream()
            .filter(a -> TAApplicationRecord.STATUS_APPROVED.equals(a.getStatus()))
            .count();

        int totalUsers = tas.size() + mos.size();

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard(String.valueOf(totalUsers), "Registered Users"),
            statCard(String.valueOf(jobs.size()),  "Published Positions"),
            statCard(String.valueOf(apps.size()),  "Total Applications"),
            statCard(String.valueOf(accepted),     "Accepted")
        );

        // ── Account Management mini-table ────────────────────────────
        VBox accountCard = buildCard("Account Management", buildAccountTable(tas, mos));

        // ── Recent logs ──────────────────────────────────────────────
        List<LogManager.LogEntry> logs = logManager.getAllLogs();
        VBox logsCard = buildCard("Recent Operation Logs", buildRecentLogs(logs));

        page.getChildren().addAll(pageTitle, statsRow, accountCard, logsCard);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Stat card ────────────────────────────────────────────────────

    private VBox statCard(String number, String description) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label num = new Label(number);
        num.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #6366f1;");

        Label desc = new Label(description);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(num, desc);
        return card;
    }

    // ── Card wrapper ─────────────────────────────────────────────────

    private VBox buildCard(String title, VBox content) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1;" +
            "-fx-border-radius: 12; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");

        HBox cardHeader = new HBox();
        cardHeader.setPadding(new Insets(16, 20, 14, 20));
        cardHeader.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        cardHeader.getChildren().add(titleLabel);

        card.getChildren().addAll(cardHeader, content);
        return card;
    }

    // ── Account mini-table ───────────────────────────────────────────

    private VBox buildAccountTable(List<TAApplication> tas, List<User> mos) {
        VBox tableBox = new VBox(0);

        // Header row
        tableBox.getChildren().add(tableHeaderRow("Account", "Name", "Role", "Major / Dept."));

        // TA rows (show at most 5)
        int taCount = 0;
        for (TAApplication ta : tas) {
            if (taCount++ >= 5) break;
            tableBox.getChildren().add(tableDataRow(
                ta.getTAId(),
                ta.getName() != null ? ta.getName() : "—",
                "Applicant",    "#003399", "#eef4ff", "#aabbcc",
                ta.getMajor()  != null ? ta.getMajor() : "—"
            ));
        }

        // MO rows (show at most 3)
        int moCount = 0;
        for (User mo : mos) {
            if (moCount++ >= 3) break;
            tableBox.getChildren().add(tableDataRow(
                mo.getAccount(),
                mo.getName() != null && !mo.getName().isEmpty() ? mo.getName() : "—",
                "Module Organiser", "#663300", "#fff8ee", "#ddccaa",
                "—"
            ));
        }

        if (tas.isEmpty() && mos.isEmpty()) {
            Label empty = new Label("No users registered yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 20 20 20 20;");
            tableBox.getChildren().add(empty);
        }

        return tableBox;
    }

    private HBox tableHeaderRow(String... cols) {
        HBox row = new HBox();
        row.setPadding(new Insets(8, 20, 8, 20));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        double[] widths = {130, 120, 160, 200};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 500;");
            lbl.setPrefWidth(i < widths.length ? widths[i] : 160);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox tableDataRow(String account, String name, String roleName,
                               String roleColor, String roleBg, String roleBorder,
                               String dept) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        Label lAccount = new Label(account);
        lAccount.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        lAccount.setPrefWidth(130);

        Label lName = new Label(name);
        lName.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        lName.setPrefWidth(120);

        Label badge = new Label(roleName);
        badge.setStyle(String.format(
            "-fx-font-size: 11px; -fx-text-fill: %s; -fx-background-color: %s;" +
            "-fx-border-color: %s; -fx-border-width: 1; -fx-padding: 2 8 2 8;" +
            "-fx-border-radius: 4; -fx-background-radius: 4;",
            roleColor, roleBg, roleBorder));
        HBox badgeWrap = new HBox(badge);
        badgeWrap.setPrefWidth(160);
        badgeWrap.setAlignment(Pos.CENTER_LEFT);

        Label lDept = new Label(dept);
        lDept.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        lDept.setPrefWidth(200);

        row.getChildren().addAll(lAccount, lName, badgeWrap, lDept);
        return row;
    }

    // ── Recent logs ──────────────────────────────────────────────────

    private VBox buildRecentLogs(List<LogManager.LogEntry> logs) {
        VBox box = new VBox(0);

        if (logs.isEmpty()) {
            Label empty = new Label("No operation records yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 20 20 20 20;");
            box.getChildren().add(empty);
            return box;
        }

        List<LogManager.LogEntry> recent = logs.subList(0, Math.min(6, logs.size()));
        for (LogManager.LogEntry entry : recent) {
            HBox logRow = new HBox();
            logRow.setPadding(new Insets(10, 20, 10, 20));
            logRow.setAlignment(Pos.CENTER_LEFT);
            logRow.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

            String desc = entry.getAdminId() + " · " + entry.getAction() + " → " + entry.getTarget();
            if (entry.getDetail() != null && !entry.getDetail().isEmpty()) {
                desc += "  (" + entry.getDetail() + ")";
            }

            Label text = new Label(desc);
            text.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
            HBox.setHgrow(text, Priority.ALWAYS);

            Label time = new Label(entry.getTimestamp());
            time.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

            logRow.getChildren().addAll(text, time);
            box.getChildren().add(logRow);
        }

        return box;
    }
}
