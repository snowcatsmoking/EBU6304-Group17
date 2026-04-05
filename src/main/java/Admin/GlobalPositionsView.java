package Admin;

import ZiqianCao.java.TAApplicationRecord;
import ZiqianCao.java.TAApplicationRecordManager;
import ZiqianCao.java.TAJob;
import data.JobDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalPositionsView {

    private final JobDataManager jobDataManager;
    private final TAApplicationRecordManager appRecordManager;

    public GlobalPositionsView() {
        this.jobDataManager   = new JobDataManager();
        this.appRecordManager = new TAApplicationRecordManager();
    }

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #fafafa;");

        Label pageTitle = new Label("Global Positions");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        // Pre-compute per-job application counts
        List<TAApplicationRecord> allApps = appRecordManager.getAllApplications();
        Map<String, Integer> totalByJob    = new HashMap<>();
        Map<String, Integer> acceptedByJob = new HashMap<>();
        for (TAApplicationRecord app : allApps) {
            String jid = app.getJobId();
            totalByJob.merge(jid, 1, Integer::sum);
            if (TAApplicationRecord.STATUS_APPROVED.equals(app.getStatus())) {
                acceptedByJob.merge(jid, 1, Integer::sum);
            }
        }

        List<TAJob> jobs = jobDataManager.getAllJobs();

        Label countLabel = new Label("共 " + jobs.size() + " 个岗位");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        // Card
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");

        card.getChildren().add(buildHeaderRow());

        if (jobs.isEmpty()) {
            Label empty = new Label("No positions published yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 24 20 24 20;");
            card.getChildren().add(empty);
        } else {
            for (TAJob job : jobs) {
                int total    = totalByJob.getOrDefault(job.getJobId(), 0);
                int accepted = acceptedByJob.getOrDefault(job.getJobId(), 0);
                card.getChildren().add(buildJobRow(job, total, accepted));
            }
        }

        page.getChildren().addAll(pageTitle, countLabel, card);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa; -fx-background: #fafafa;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private HBox buildHeaderRow() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        row.getChildren().addAll(
            headerCell("Position",           240),
            headerCell("Posted By",          120),
            headerCell("Openings / Accepted", 160),
            headerCell("Applications",        110),
            headerCell("Deadline",            120),
            headerCell("Status",              80)
        );
        return row;
    }

    private HBox buildJobRow(TAJob job, int totalApps, int accepted) {
        HBox row = new HBox();
        row.setPadding(new Insets(13, 20, 13, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0; -fx-background-color: #ffffff;");

        // Position name + course
        VBox posBox = new VBox(2);
        posBox.setPrefWidth(240);
        Label posName = new Label(job.getPositionName() != null ? job.getPositionName() : "—");
        posName.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222;");
        Label courseName = new Label(job.getCourseName() != null ? job.getCourseName() : "");
        courseName.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");
        posBox.getChildren().addAll(posName, courseName);

        Label lPublisher = cell(job.getPublisher() != null ? job.getPublisher() : "—", 120, "#555555");

        String openings = job.getRecruitmentCount() + " / " + accepted;
        Label lOpenings = cell(openings, 160, "#333333");

        Label lApps = cell(String.valueOf(totalApps), 110, "#333333");

        Label lDeadline = cell(job.getDeadline() != null ? job.getDeadline() : "—", 120, "#555555");

        // Status badge
        boolean isOpen = !job.isActive();
        Label statusBadge = new Label(isOpen ? "Open" : "Closed");
        statusBadge.setStyle(isOpen
            ? "-fx-font-size: 11px; -fx-text-fill: #006600; -fx-background-color: #eeffee;" +
              "-fx-border-color: #aaddaa; -fx-border-width: 1; -fx-padding: 2 8 2 8;"
            : "-fx-font-size: 11px; -fx-text-fill: #666666; -fx-background-color: #f0f0f0;" +
              "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 2 8 2 8;");
        HBox statusWrap = new HBox(statusBadge);
        statusWrap.setPrefWidth(80);
        statusWrap.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(posBox, lPublisher, lOpenings, lApps, lDeadline, statusWrap);
        return row;
    }

    private Label headerCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    private Label cell(String text, double width, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
        lbl.setPrefWidth(width);
        return lbl;
    }
}
