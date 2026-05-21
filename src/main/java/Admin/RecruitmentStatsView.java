package Admin;

import TA.java.TAApplicationRecord;
import TA.java.TAApplicationRecordManager;
import TA.java.TAJob;
import data.JobDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.*;

public class RecruitmentStatsView {

    private final JobDataManager jobDataManager;
    private final TAApplicationRecordManager appRecordManager;

    public RecruitmentStatsView() {
        this.jobDataManager   = new JobDataManager();
        this.appRecordManager = new TAApplicationRecordManager();
    }

    public ScrollPane build() {
        List<TAJob> jobs         = jobDataManager.getAllJobs();
        List<TAApplicationRecord> apps = appRecordManager.getAllApplications();

        // ── Per-job aggregation ───────────────────────────────────────
        Map<String, Integer> appliedByJob  = new HashMap<>();
        Map<String, Integer> approvedByJob = new HashMap<>();
        for (TAApplicationRecord app : apps) {
            String jid = app.getJobId();
            if (jid == null) continue;
            appliedByJob.merge(jid, 1, Integer::sum);
            if (TAApplicationRecord.STATUS_APPROVED.equals(app.getStatus())) {
                approvedByJob.merge(jid, 1, Integer::sum);
            }
        }

        // ── Per-MO aggregation ────────────────────────────────────────
        // moId -> [jobCount, totalApplied, totalApproved]
        Map<String, int[]> moStats = new LinkedHashMap<>();
        for (TAJob job : jobs) {
            String moId = job.getMoStaffId() != null ? job.getMoStaffId() : "—";
            moStats.computeIfAbsent(moId, k -> new int[3]);
            int applied  = appliedByJob.getOrDefault(job.getJobId(), 0);
            int approved = approvedByJob.getOrDefault(job.getJobId(), 0);
            moStats.get(moId)[0]++;
            moStats.get(moId)[1] += applied;
            moStats.get(moId)[2] += approved;
        }

        // ── Layout ────────────────────────────────────────────────────
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #f8fafc;");

        Label pageTitle = new Label("Recruitment Statistics");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Real-time overview of recruitment progress across all positions and organisers.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // ── Summary stat cards ────────────────────────────────────────
        long totalApproved = apps.stream()
            .filter(a -> TAApplicationRecord.STATUS_APPROVED.equals(a.getStatus())).count();
        int totalQuota = jobs.stream().mapToInt(TAJob::getRecruitmentCount).sum();
        String overallRate = totalQuota == 0 ? "—"
            : Math.min(100, (int) (totalApproved * 100L / totalQuota)) + "%";

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard(String.valueOf(jobs.size()),  "Total Positions"),
            statCard(String.valueOf(apps.size()),  "Total Applications"),
            statCard(String.valueOf(totalApproved),"Total Hired"),
            statCard(overallRate,                  "Overall Fill Rate")
        );

        page.getChildren().addAll(pageTitle, subtitle, statsRow,
            buildJobStatsCard(jobs, appliedByJob, approvedByJob),
            buildMoStatsCard(moStats));

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Per-job table ─────────────────────────────────────────────────

    private VBox buildJobStatsCard(List<TAJob> jobs,
                                   Map<String, Integer> appliedByJob,
                                   Map<String, Integer> approvedByJob) {
        VBox card = buildCard("Statistics by Position  (" + jobs.size() + " position(s))");

        VBox table = new VBox(0);
        table.getChildren().add(jobHeaderRow());

        if (jobs.isEmpty()) {
            table.getChildren().add(emptyRow("No positions published yet."));
        } else {
            for (TAJob job : jobs) {
                int applied  = appliedByJob.getOrDefault(job.getJobId(), 0);
                int approved = approvedByJob.getOrDefault(job.getJobId(), 0);
                int quota    = job.getRecruitmentCount();
                table.getChildren().add(jobDataRow(job, applied, approved, quota));
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private HBox jobHeaderRow() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        row.getChildren().addAll(
            headerCell("Position / Course", 220),
            headerCell("Posted By",          130),
            headerCell("Quota",               70),
            headerCell("Applied",             80),
            headerCell("Hired",               70),
            headerCell("Fill Rate",          100),
            headerCell("Status",              80)
        );
        return row;
    }

    private HBox jobDataRow(TAJob job, int applied, int approved, int quota) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0; -fx-background-color: #ffffff;");

        VBox posBox = new VBox(2);
        posBox.setPrefWidth(220);
        Label posName = new Label(nvl(job.getPositionName()));
        posName.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        Label courseName = new Label(nvl(job.getCourseName()));
        courseName.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        posBox.getChildren().addAll(posName, courseName);

        Label lMo       = cell(nvl(job.getMoStaffId()), 130, "#64748b");
        Label lQuota    = cell(String.valueOf(quota),    70,  "#334155");
        Label lApplied  = cell(String.valueOf(applied),  80,  "#334155");
        Label lApproved = cell(String.valueOf(approved), 70,  "#334155");

        int pct = quota == 0 ? 0 : Math.min(100, approved * 100 / quota);
        String rateText = quota == 0 ? "—" : pct + "%";
        String rateColor = pct >= 100 ? "#16a34a" : pct >= 50 ? "#334155" : "#d97706";
        Label lRate = cell(rateText, 100, rateColor);
        lRate.setStyle("-fx-font-size: 13px; -fx-text-fill: " + rateColor + "; -fx-font-weight: bold;");
        lRate.setPrefWidth(100);

        boolean isOpen = !job.isActive();
        Label statusBadge = new Label(isOpen ? "Open" : "Closed");
        statusBadge.setStyle(isOpen
            ? "-fx-font-size: 11px; -fx-text-fill: #16a34a; -fx-background-color: #f0fdf4;" +
              "-fx-border-color: #bbf7d0; -fx-border-width: 1; -fx-padding: 2 8 2 8;"
            : "-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-background-color: #f8fafc;" +
              "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-padding: 2 8 2 8;");
        HBox statusWrap = new HBox(statusBadge);
        statusWrap.setPrefWidth(80);
        statusWrap.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(posBox, lMo, lQuota, lApplied, lApproved, lRate, statusWrap);
        return row;
    }

    // ── Per-MO table ──────────────────────────────────────────────────

    private VBox buildMoStatsCard(Map<String, int[]> moStats) {
        VBox card = buildCard("Statistics by Module Organiser  (" + moStats.size() + " organiser(s))");

        VBox table = new VBox(0);
        table.getChildren().add(moHeaderRow());

        if (moStats.isEmpty()) {
            table.getChildren().add(emptyRow("No data available."));
        } else {
            for (Map.Entry<String, int[]> entry : moStats.entrySet()) {
                table.getChildren().add(moDataRow(entry.getKey(), entry.getValue()));
            }
        }

        card.getChildren().add(table);
        return card;
    }

    private HBox moHeaderRow() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        row.getChildren().addAll(
            headerCell("MO Account",   200),
            headerCell("Posted Jobs",  130),
            headerCell("Total Applied",130),
            headerCell("Total Hired",  130)
        );
        return row;
    }

    private HBox moDataRow(String moId, int[] stats) {
        HBox row = new HBox();
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0; -fx-background-color: #ffffff;");

        row.getChildren().addAll(
            cell(moId,                  200, "#1e293b"),
            cell(String.valueOf(stats[0]), 130, "#334155"),
            cell(String.valueOf(stats[1]), 130, "#334155"),
            cell(String.valueOf(stats[2]), 130, "#334155")
        );
        return row;
    }

    // ── Shared helpers ────────────────────────────────────────────────

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

    private HBox emptyRow(String msg) {
        HBox row = new HBox();
        row.setPadding(new Insets(20));
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        row.getChildren().add(lbl);
        return row;
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

    private String nvl(String s) {
        return s != null && !s.isEmpty() ? s : "—";
    }
}
