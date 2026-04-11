package Admin;

import ZiqianCao.java.TAApplication;
import LoginScreen.User;
import data.LogManager;
import data.UserDataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class UserManagementView {

    private final String adminId;
    private final UserDataManager userDataManager;
    private final LogManager logManager;

    static class UserRow {
        final String account;
        final String name;
        final String role;
        final String department;

        UserRow(String account, String name, String role, String department) {
            this.account = account;
            this.name = name;
            this.role = role;
            this.department = department;
        }
    }

    public UserManagementView(String adminId) {
        this.adminId = adminId;
        this.userDataManager = new UserDataManager();
        this.logManager = new LogManager();
    }

    public ScrollPane build() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setStyle("-fx-background-color: #fafafa;");

        // ── Page title ───────────────────────────────────────────────
        Label pageTitle = new Label("User Management");
        pageTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        // ── Build row data ───────────────────────────────────────────
        List<UserRow> rows = buildRows();

        Label countLabel = new Label(rows.size() + " account(s)");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

        // ── Table card ───────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1;");

        // Header row
        card.getChildren().add(buildHeaderRow());

        // Data rows — keep a reference to the card so we can remove rows on delete
        for (UserRow row : rows) {
            card.getChildren().add(buildDataRow(row, card));
        }

        if (rows.isEmpty()) {
            Label empty = new Label("No users registered yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa; -fx-padding: 24 20 24 20;");
            card.getChildren().add(empty);
        }

        page.getChildren().addAll(pageTitle, countLabel, card);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa; -fx-background: #fafafa;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // ── Row data ─────────────────────────────────────────────────────

    private List<UserRow> buildRows() {
        List<UserRow> rows = new ArrayList<>();
        for (TAApplication ta : userDataManager.getAllTAs()) {
            rows.add(new UserRow(
                ta.getTAId(),
                ta.getName() != null ? ta.getName() : "—",
                "TA",
                ta.getMajor() != null ? ta.getMajor() : "—"
            ));
        }
        for (User mo : userDataManager.getAllMOs()) {
            rows.add(new UserRow(
                mo.getAccount(),
                mo.getName() != null && !mo.getName().isEmpty() ? mo.getName() : "—",
                "MO",
                "—"
            ));
        }
        return rows;
    }

    // ── Header row ───────────────────────────────────────────────────

    private HBox buildHeaderRow() {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #fafafa; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");

        row.getChildren().addAll(
            headerCell("Account",       120),
            headerCell("Name",          110),
            headerCell("Role",          160),
            headerCell("Major / Dept.", 200),
            headerCell("Actions",       200)
        );
        return row;
    }

    private Label headerCell(String text, double width) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-weight: 500;");
        lbl.setPrefWidth(width);
        return lbl;
    }

    // ── Data row ─────────────────────────────────────────────────────

    private HBox buildDataRow(UserRow userRow, VBox parentCard) {
        HBox row = new HBox();
        row.setPadding(new Insets(13, 20, 13, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0; -fx-background-color: #ffffff;");

        Label lAccount = new Label(userRow.account);
        lAccount.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222;");
        lAccount.setPrefWidth(120);

        Label lName = new Label(userRow.name);
        lName.setStyle("-fx-font-size: 13px; -fx-text-fill: #222222;");
        lName.setPrefWidth(110);

        // Role badge
        Label badge = roleBadge(userRow.role);
        HBox badgeWrap = new HBox(badge);
        badgeWrap.setPrefWidth(160);
        badgeWrap.setAlignment(Pos.CENTER_LEFT);

        Label lDept = new Label(userRow.department);
        lDept.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        lDept.setPrefWidth(200);

        // Actions
        Button resetBtn = new Button("Reset Password");
        resetBtn.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #333333; -fx-background-color: #ffffff;" +
            "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 5 12 5 12;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
            "-fx-font-size: 11px; -fx-text-fill: #cc0000; -fx-background-color: #ffffff;" +
            "-fx-border-color: #eeaaaa; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 5 12 5 12;");

        resetBtn.setOnAction(e -> showResetPasswordDialog(userRow.account, userRow.role));
        deleteBtn.setOnAction(e -> showDeleteConfirmDialog(userRow, row, parentCard));

        HBox actions = new HBox(8, resetBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPrefWidth(200);

        row.getChildren().addAll(lAccount, lName, badgeWrap, lDept, actions);
        return row;
    }

    private Label roleBadge(String role) {
        Label badge = new Label();
        if ("TA".equals(role)) {
            badge.setText("Applicant");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #003399; -fx-background-color: #eef4ff;" +
                "-fx-border-color: #aabbcc; -fx-border-width: 1; -fx-padding: 2 8 2 8;");
        } else if ("MO".equals(role)) {
            badge.setText("Module Organiser");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #663300; -fx-background-color: #fff8ee;" +
                "-fx-border-color: #ddccaa; -fx-border-width: 1; -fx-padding: 2 8 2 8;");
        } else {
            badge.setText(role);
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #333333; -fx-background-color: #f0f0f0;" +
                "-fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-padding: 2 8 2 8;");
        }
        return badge;
    }

    // ── Dialogs ───────────────────────────────────────────────────────

    private void showResetPasswordDialog(String account, String role) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + account);

        ButtonType confirmType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        PasswordField pwField = new PasswordField();
        pwField.setPromptText("New password (min 6 chars)");
        pwField.setStyle("-fx-font-size: 13px; -fx-padding: 8 10 8 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm new password");
        confirmField.setStyle("-fx-font-size: 13px; -fx-padding: 8 10 8 10; -fx-border-color: #cccccc; -fx-border-width: 1;");

        VBox content = new VBox(10,
            new Label("New password:"), pwField,
            new Label("Confirm password:"), confirmField);
        content.setPadding(new Insets(10, 0, 0, 0));
        content.setPrefWidth(320);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != confirmType) return;
            String newPw = pwField.getText();
            if (newPw.length() < 6) { showAlert("Error", "Password must be at least 6 characters."); return; }
            if (!newPw.equals(confirmField.getText())) { showAlert("Error", "Passwords do not match."); return; }
            if (userDataManager.resetPassword(account, role, newPw)) {
                logManager.log(adminId, "Reset Password", account, "Role: " + role);
                showInfo("Success", "Password for " + account + " has been reset.");
            } else {
                showAlert("Error", "Reset failed — user not found.");
            }
        });
    }

    private void showDeleteConfirmDialog(UserRow userRow, HBox rowNode, VBox parentCard) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Delete account: " + userRow.name + "?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            if (userDataManager.deleteUser(userRow.account, userRow.role)) {
                logManager.log(adminId, "Delete Account", userRow.account, "Role: " + userRow.role);
                parentCard.getChildren().remove(rowNode);
                showInfo("Deleted", "Account " + userRow.name + " has been removed.");
            } else {
                showAlert("Error", "Delete failed — user not found.");
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Warning"); a.setHeaderText(title); a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Done"); a.setHeaderText(title); a.setContentText(msg);
        a.showAndWait();
    }
}
