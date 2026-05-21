package TA.java.service;

import core.LanguageManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FileUploaderTranslationTest {

    private static final String TEST_STUDENT_ID = "__translation_test__";

    public static void main(String[] args) {
        FileUploaderTranslationTest test = new FileUploaderTranslationTest();
        test.testAttachmentUploadComponentUsesSelectedLanguage();
    }

    public void testAttachmentUploadComponentUsesSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            VBox uploadComponent = runOnFxThread(() -> new FileUploader(TEST_STUDENT_ID).getUploadComponent());
            List<String> labels = collectLabels(uploadComponent);

            require(labels.contains("\u9644\u4ef6"), "attachment title should be translated");
            require(labels.contains("\u9009\u62e9\u6587\u4ef6"), "choose file button should be translated");
            require(labels.contains("\u652f\u6301 Word\uff08.doc\u3001.docx\uff09\u548c PDF\uff08.pdf\uff09\u6587\u4ef6\uff0c\u6700\u5927 10MB"),
                "file format hint should be translated");
            require(labels.contains("\u5c1a\u672a\u4e0a\u4f20\u6587\u4ef6"), "empty attachment message should be translated");
        } finally {
            cleanupTestUploadDirectory();
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    private VBox runOnFxThread(FxTask<VBox> task) {
        startToolkit();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<VBox> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(task.run());
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        try {
            require(latch.await(5, TimeUnit.SECONDS), "JavaFX task timed out");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX task", e);
        }

        if (failure.get() != null) {
            throw new IllegalStateException("JavaFX task failed", failure.get());
        }

        return result.get();
    }

    private void startToolkit() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            require(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit startup timed out");
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit can only be started once in the same test JVM.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting JavaFX toolkit", e);
        }
    }

    private List<String> collectLabels(Node node) {
        List<String> labels = new ArrayList<>();
        collectLabels(node, labels);
        return labels;
    }

    private void collectLabels(Node node, List<String> labels) {
        if (node instanceof Labeled labeled) {
            labels.add(labeled.getText());
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectLabels(child, labels);
            }
        }
    }

    private void cleanupTestUploadDirectory() {
        Path uploadDir = Path.of("resources", "Data", "Uploads", TEST_STUDENT_ID);
        if (!Files.exists(uploadDir)) {
            return;
        }

        try (var paths = Files.walk(uploadDir)) {
            paths.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to delete test upload path " + path, e);
                    }
                });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clean test upload directory", e);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface FxTask<T> {
        T run();
    }
}
