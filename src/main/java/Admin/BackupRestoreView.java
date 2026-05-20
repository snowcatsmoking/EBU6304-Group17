package Admin;

import data.DataConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.*;

public class BackupRestoreView {

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #f8fafc;");

        Label pageTitle = new Label("Backup & Restore");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Protect your data by creating backups and restoring from previous snapshots.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        page.getChildren().addAll(pageTitle, subtitle, buildBackupCard(), buildRestoreCard());

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Backup card ───────────────────────────────────────────────────

    private VBox buildBackupCard() {
        VBox card = buildCard("Backup System Data");

        Label desc = new Label(
            "Export all system data (users, jobs, application records, logs) as a single ZIP file.\n" +
            "Store the file in a safe location for future recovery."
        );
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        desc.setWrapText(true);

        Button backupBtn = new Button("Backup Now");
        styleActionButton(backupBtn, "#6366f1", "#ffffff", "#6366f1");
        backupBtn.setOnAction(e -> handleBackup(backupBtn.getScene().getWindow()));

        HBox btnRow = new HBox(backupBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(16, desc, btnRow);
        content.setPadding(new Insets(20));
        card.getChildren().add(content);
        return card;
    }

    // ── Restore card ──────────────────────────────────────────────────

    private VBox buildRestoreCard() {
        VBox card = buildCard("Restore System Data");

        Label desc = new Label(
            "Select a backup ZIP file to restore. All current data will be overwritten with the\n" +
            "backup contents. This operation cannot be undone."
        );
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        desc.setWrapText(true);

        Label warning = new Label("⚠  Restoring will permanently overwrite existing data.");
        warning.setStyle("-fx-font-size: 12px; -fx-text-fill: #d97706;" +
                "-fx-background-color: #fffbeb; -fx-border-color: #fde68a; -fx-border-width: 1;" +
                "-fx-padding: 8 12 8 12; -fx-border-radius: 8; -fx-background-radius: 8;");
        warning.setMaxWidth(Double.MAX_VALUE);

        Button restoreBtn = new Button("Restore from Backup");
        styleActionButton(restoreBtn, "#ffffff", "#ef4444", "#ef4444");
        restoreBtn.setOnAction(e -> handleRestore(restoreBtn.getScene().getWindow()));

        HBox btnRow = new HBox(restoreBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(16, desc, warning, btnRow);
        content.setPadding(new Insets(20));
        card.getChildren().add(content);
        return card;
    }

    // ── Backup logic ──────────────────────────────────────────────────

    private void handleBackup(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Backup File");
        String defaultName = "backup_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));

        File dest = chooser.showSaveDialog(owner);
        if (dest == null) return;

        try {
            createZipBackup(dest);
            alert(Alert.AlertType.INFORMATION, "Backup Successful",
                    "System data has been backed up to:\n" + dest.getAbsolutePath());
        } catch (IOException ex) {
            alert(Alert.AlertType.ERROR, "Backup Failed",
                    "An error occurred while creating the backup:\n" + ex.getMessage());
        }
    }

    private void createZipBackup(File dest) throws IOException {
        String[] dataDirs = {
            DataConfig.TA_DIR, DataConfig.MO_DIR, DataConfig.ADMIN_DIR,
            DataConfig.JOB_DIR, DataConfig.APPLICATION_DIR, DataConfig.FAVORITE_DIR,
            DataConfig.LOG_DIR, DataConfig.MATCHING_DIR
        };

        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)))) {
            for (String dir : dataDirs) {
                File folder = new File(dir);
                if (!folder.exists()) continue;
                File[] files = folder.listFiles();
                if (files == null) continue;
                for (File file : files) {
                    if (!file.isFile()) continue;
                    String entryName = dir + file.getName();
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(file.toPath(), zos);
                    zos.closeEntry();
                }
            }
            // Include session file
            File session = new File(DataConfig.BASE_DIR + "session.json");
            if (session.exists()) {
                zos.putNextEntry(new ZipEntry(DataConfig.BASE_DIR + "session.json"));
                Files.copy(session.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    // ── Restore logic ─────────────────────────────────────────────────

    private void handleRestore(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Backup File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));

        File src = chooser.showOpenDialog(owner);
        if (src == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Restore");
        confirm.setHeaderText("Overwrite current data?");
        confirm.setContentText(
            "All current system data will be replaced with the contents of:\n" +
            src.getName() + "\n\nThis cannot be undone. Continue?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            try {
                restoreFromZip(src);
                alert(Alert.AlertType.INFORMATION, "Restore Successful",
                        "System data has been restored from:\n" + src.getAbsolutePath() +
                        "\n\nPlease restart the application for changes to take effect.");
            } catch (IOException ex) {
                alert(Alert.AlertType.ERROR, "Restore Failed",
                        "An error occurred while restoring data:\n" + ex.getMessage());
            }
        });
    }

    private void restoreFromZip(File src) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(src)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }
                File outFile = new File(entry.getName());
                // Safety check: prevent zip-slip path traversal
                String canonicalOut = outFile.getCanonicalPath();
                String canonicalBase = new File(DataConfig.BASE_DIR).getCanonicalPath();
                if (!canonicalOut.startsWith(canonicalBase)) {
                    zis.closeEntry();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
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

    private void styleActionButton(Button btn, String textColor, String bgColor, String borderColor) {
        String style = String.format(
            "-fx-font-size: 13px; -fx-text-fill: %s; -fx-background-color: %s;" +
            "-fx-border-color: %s; -fx-border-width: 1; -fx-padding: 8 20 8 20; -fx-cursor: hand;" +
            "-fx-border-radius: 8; -fx-background-radius: 8;",
            textColor, bgColor, borderColor);
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style + "-fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle(style));
    }

    private void alert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
