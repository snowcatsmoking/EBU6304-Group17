package TA.java.service;

import core.UiText;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileUploader {

    private static final String UPLOAD_DIR = "resources/Data/Uploads/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private String studentId;
    private List<String> uploadedFiles;
    private VBox fileListContainer;

    public FileUploader(String studentId) {
        this.studentId = studentId;
        this.uploadedFiles = new ArrayList<>();
        initUploadDirectory();
        loadExistingFiles();
    }

    private void initUploadDirectory() {
        File uploadDir = new File(UPLOAD_DIR + studentId);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    private void loadExistingFiles() {
        File userDir = new File(UPLOAD_DIR + studentId);
        File[] files = userDir.listFiles();
        if (files != null) {
            for (File file : files) {
                uploadedFiles.add(file.getName());
            }
        }
    }

    public VBox getUploadComponent() {
        VBox container = new VBox();
        container.setSpacing(12);
        container.setPadding(new Insets(0, 0, 0, 0));

        Label titleLabel = new Label(UiText.tr("Attachments"));
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        Button uploadButton = new Button(UiText.tr("Choose File"));
        uploadButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        uploadButton.setOnMouseEntered(e ->
            uploadButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #4f46e5; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;")
        );
        uploadButton.setOnMouseExited(e ->
            uploadButton.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;")
        );
        uploadButton.setOnAction(e -> openFileChooser());

        Label hintLabel = new Label(UiText.tr("Supports Word (.doc, .docx) and PDF (.pdf) files, max 10MB"));
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        HBox uploadBox = new HBox();
        uploadBox.setSpacing(12);
        uploadBox.setAlignment(Pos.CENTER_LEFT);
        uploadBox.getChildren().addAll(uploadButton, hintLabel);

        fileListContainer = new VBox();
        fileListContainer.setSpacing(8);
        refreshFileList();

        container.getChildren().addAll(titleLabel, uploadBox, fileListContainer);

        return container;
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(UiText.tr("Choose File"));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter(UiText.tr("Word and PDF Files"), "*.doc", "*.docx", "*.pdf"),
            new FileChooser.ExtensionFilter(UiText.tr("Word Files"), "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter(UiText.tr("PDF Files"), "*.pdf")
        );

        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            if (selectedFile.length() > MAX_FILE_SIZE) {
                System.out.println("File size cannot exceed 10MB");
                return;
            }

            String fileName = selectedFile.getName();
            String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

            if (fileExt.equals("doc") || fileExt.equals("docx") || fileExt.equals("pdf")) {
                saveFile(selectedFile);
            } else {
                System.out.println("Unsupported file format");
            }
        }
    }

    private void saveFile(File sourceFile) {
        try {
            Path targetPath = Paths.get(UPLOAD_DIR + studentId, sourceFile.getName());
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            if (!uploadedFiles.contains(sourceFile.getName())) {
                uploadedFiles.add(sourceFile.getName());
            }

            refreshFileList();
            System.out.println("File uploaded successfully: " + sourceFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("File upload failed: " + e.getMessage());
        }
    }

    private void deleteFile(String fileName) {
        File file = new File(UPLOAD_DIR + studentId + "/" + fileName);
        if (file.exists()) {
            file.delete();
        }
        uploadedFiles.remove(fileName);
        refreshFileList();
        System.out.println("File deleted: " + fileName);
    }

    private void refreshFileList() {
        fileListContainer.getChildren().clear();

        if (uploadedFiles.isEmpty()) {
            Label emptyLabel = new Label(UiText.tr("No files uploaded"));
            emptyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 8 0 8 0;");
            fileListContainer.getChildren().add(emptyLabel);
        } else {
            for (String fileName : uploadedFiles) {
                HBox fileItem = createFileItem(fileName);
                fileListContainer.getChildren().add(fileItem);
            }
        }
    }

    private HBox createFileItem(String fileName) {
        HBox item = new HBox();
        item.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setSpacing(12);
        item.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(getFileIcon(fileName));
        iconLabel.setStyle("-fx-font-size: 18px;");

        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");

        Button deleteButton = new Button(UiText.tr("Delete"));
        deleteButton.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff4d4f; -fx-background-color: transparent; -fx-cursor: hand; -fx-border-width: 0;");
        deleteButton.setOnAction(e -> deleteFile(fileName));

        item.getChildren().addAll(iconLabel, nameLabel, deleteButton);
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        return item;
    }

    private String getFileIcon(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "pdf":
                return "📄";
            case "doc":
            case "docx":
                return "📝";
            default:
                return "📁";
        }
    }

    public List<String> getUploadedFiles() {
        return new ArrayList<>(uploadedFiles);
    }
}
