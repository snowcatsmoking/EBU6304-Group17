package Admin;

import TA.java.TAApplicationRecord;
import TA.java.TAApplicationRecordManager;
import TA.java.TAJob;
import data.JobDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataExportView {

    private final JobDataManager jobDataManager;
    private final TAApplicationRecordManager appRecordManager;

    @FunctionalInterface
    interface CsvDataProvider {
        List<String[]> get();
    }

    public DataExportView() {
        this.jobDataManager   = new JobDataManager();
        this.appRecordManager = new TAApplicationRecordManager();
    }

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #fafafa;");

        Label pageTitle = new Label("Data Export");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        Label subtitle = new Label("Export system data to CSV files for offline analysis.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #777777;");

        page.getChildren().addAll(
            pageTitle,
            subtitle,
            buildExportCard(
                "Job Postings",
                "All published positions including quota, deadline, and organiser information.",
                "jobs",
                this::buildJobsData
            ),
            buildExportCard(
                "Application Records",
                "All TA application records including applicant details, status, and review comments.",
                "applications",
                this::buildApplicationsData
            ),
            buildExportCard(
                "Recruitment Statistics",
                "Aggregated per-position recruitment progress: quota, applications, hired count, and fill rate.",
                "recruitment_stats",
                this::buildStatsData
            )
        );

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa; -fx-background: #fafafa;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Export card ───────────────────────────────────────────────────

    private VBox buildExportCard(String title, String description,
                                 String filenameStem, CsvDataProvider provider) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");

        HBox body = new HBox(16);
        body.setPadding(new Insets(20));
        body.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(6);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #222222;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");
        descLabel.setWrapText(true);

        Label formatBadge = new Label("CSV");
        formatBadge.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #005500; -fx-background-color: #eeffee;" +
            "-fx-border-color: #aaddaa; -fx-border-width: 1; -fx-padding: 2 8 2 8;");

        textBox.getChildren().addAll(titleLabel, descLabel, new HBox(formatBadge));

        Button exportBtn = new Button("Export CSV");
        String btnStyle =
            "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #111111;" +
            "-fx-border-color: #111111; -fx-border-width: 1; -fx-padding: 8 20 8 20; -fx-cursor: hand;";
        String btnHoverStyle =
            "-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #333333;" +
            "-fx-border-color: #333333; -fx-border-width: 1; -fx-padding: 8 20 8 20; -fx-cursor: hand;";
        exportBtn.setStyle(btnStyle);
        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle(btnHoverStyle));
        exportBtn.setOnMouseExited(e -> exportBtn.setStyle(btnStyle));
        exportBtn.setOnAction(e ->
            handleExport(exportBtn.getScene().getWindow(), filenameStem, provider));

        body.getChildren().addAll(textBox, exportBtn);
        card.getChildren().add(body);
        return card;
    }

    private void handleExport(Window owner, String filenameStem, CsvDataProvider provider) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV File");
        chooser.setInitialFileName(filenameStem + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV File", "*.csv"));

        File dest = chooser.showSaveDialog(owner);
        if (dest == null) return;

        try {
            writeCsv(dest, provider.get());
            alert(Alert.AlertType.INFORMATION, "Export Successful",
                "File saved to:\n" + dest.getAbsolutePath());
        } catch (IOException ex) {
            alert(Alert.AlertType.ERROR, "Export Failed",
                "An error occurred while exporting:\n" + ex.getMessage());
        }
    }

    // ── Data builders ─────────────────────────────────────────────────

    private List<String[]> buildJobsData() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
            "JobId", "PositionName", "CourseName", "CourseCode",
            "RecruitmentCount", "Publisher", "MoStaffId", "Deadline", "IsActive"
        });
        for (TAJob job : jobDataManager.getAllJobs()) {
            rows.add(new String[]{
                nvl(job.getJobId()),
                nvl(job.getPositionName()),
                nvl(job.getCourseName()),
                nvl(job.getCourseCode()),
                String.valueOf(job.getRecruitmentCount()),
                nvl(job.getPublisher()),
                nvl(job.getMoStaffId()),
                nvl(job.getDeadline()),
                String.valueOf(job.isActive())
            });
        }
        return rows;
    }

    private List<String[]> buildApplicationsData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
            "ApplicationId", "TaStudentId", "StudentName", "JobId",
            "PositionName", "CourseName", "Status",
            "ApplicationDate", "Reviewer", "ReviewComment"
        });
        for (TAApplicationRecord app : appRecordManager.getAllApplications()) {
            rows.add(new String[]{
                nvl(app.getApplicationId()),
                nvl(app.getTaStudentId()),
                nvl(app.getStudentName()),
                nvl(app.getJobId()),
                nvl(app.getPositionName()),
                nvl(app.getCourseName()),
                nvl(app.getStatus()),
                app.getApplicationDate() != null ? sdf.format(app.getApplicationDate()) : "",
                nvl(app.getReviewer()),
                nvl(app.getReviewComment())
            });
        }
        return rows;
    }

    private List<String[]> buildStatsData() {
        List<TAJob> jobs = jobDataManager.getAllJobs();
        List<TAApplicationRecord> apps = appRecordManager.getAllApplications();

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

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
            "JobId", "PositionName", "CourseName", "MoStaffId",
            "Quota", "Applied", "Approved", "FillRate"
        });
        for (TAJob job : jobs) {
            int applied  = appliedByJob.getOrDefault(job.getJobId(), 0);
            int approved = approvedByJob.getOrDefault(job.getJobId(), 0);
            int quota    = job.getRecruitmentCount();
            String rate  = quota == 0 ? "N/A" : Math.min(100, approved * 100 / quota) + "%";
            rows.add(new String[]{
                nvl(job.getJobId()),
                nvl(job.getPositionName()),
                nvl(job.getCourseName()),
                nvl(job.getMoStaffId()),
                String.valueOf(quota),
                String.valueOf(applied),
                String.valueOf(approved),
                rate
            });
        }
        return rows;
    }

    // ── CSV writer ────────────────────────────────────────────────────

    private void writeCsv(File dest, List<String[]> rows) throws IOException {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(dest), StandardCharsets.UTF_8))) {
            pw.print('﻿'); // UTF-8 BOM — ensures Excel opens Chinese characters correctly
            for (String[] row : rows) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(escapeCsv(row[i]));
                }
                pw.println(sb);
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    private void alert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
