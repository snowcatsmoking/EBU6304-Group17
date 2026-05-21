package TA.java.component;

import core.LanguageManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TAAlertDialogTranslationTest {

    public static void main(String[] args) {
        TAAlertDialogTranslationTest test = new TAAlertDialogTranslationTest();
        test.testProfileAlertContentUsesSelectedLanguage();
        test.testProfileExportMessageTranslatesDynamicPathPrefix();
        test.testProfileEmailErrorMessageUsesSelectedLanguage();
    }

    public void testProfileAlertContentUsesSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            List<String> labels = runOnFxThread(() -> {
                List<String> values = new ArrayList<>();
                values.addAll(collectLabels(createTitle("Save Successful")));
                values.addAll(collectLabels(createMessageArea(
                    "Saved",
                    "Your profile changes have been saved.",
                    TAAlertDialog.Variant.SUCCESS
                )));
                values.addAll(collectLabels(createButtonRow("Done", "Cancel")));
                return values;
            });

            requireContains(labels, "\u4fdd\u5b58\u6210\u529f");
            requireContains(labels, "\u5df2\u4fdd\u5b58");
            requireContains(labels, "\u4f60\u7684\u8d44\u6599\u4fee\u6539\u5df2\u4fdd\u5b58\u3002");
            requireContains(labels, "\u5b8c\u6210");
            requireContains(labels, "\u53d6\u6d88");
        } finally {
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    public void testProfileExportMessageTranslatesDynamicPathPrefix() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            List<String> labels = runOnFxThread(() -> collectLabels(createMessageArea(
                "Export ready",
                "File saved to:\nC:/tmp/profile.json",
                TAAlertDialog.Variant.EXPORT
            )));

            requireContains(labels, "\u5bfc\u51fa\u5df2\u5c31\u7eea");
            requireContains(labels, "\u6587\u4ef6\u5df2\u4fdd\u5b58\u81f3\uff1a\nC:/tmp/profile.json");
        } finally {
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    public void testProfileEmailErrorMessageUsesSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            String message = "Invalid email format. Email must follow standard format:\n" +
                "- Contains '@' symbol in correct position\n" +
                "- Local part before '@' cannot start or end with '.'\n" +
                "- Domain part after '@' must have at least one '.' (like .com, .org)\n" +
                "- No consecutive '.' in local part\n" +
                "- No '.' immediately before or after '@'";

            List<String> labels = runOnFxThread(() -> collectLabels(createMessageArea(
                "Email format is incorrect",
                message,
                TAAlertDialog.Variant.ERROR
            )));

            requireContains(labels, "\u90ae\u7bb1\u683c\u5f0f\u4e0d\u6b63\u786e");
            requireAnyTextContains(labels, "\u90ae\u7bb1\u683c\u5f0f\u65e0\u6548");
            requireNoTextContains(labels, "Invalid email format");
            requireNoTextContains(labels, "Contains '@' symbol");
        } finally {
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    private Node createTitle(String title) {
        return invokeNode("createTitle", new Class<?>[] { String.class }, title);
    }

    private Node createMessageArea(String header, String message, TAAlertDialog.Variant variant) {
        Object palette = invokePalette(variant);
        return invokeNode(
            "createMessageArea",
            new Class<?>[] { String.class, String.class, palette.getClass(), TAAlertDialog.Variant.class },
            header,
            message,
            palette,
            variant
        );
    }

    private Node createButtonRow(String primaryText, String secondaryText) {
        return invokeNode(
            "createButtonRow",
            new Class<?>[] { String.class, String.class, String.class, Stage.class, boolean[].class },
            primaryText,
            secondaryText,
            "#6366f1",
            new Stage(),
            new boolean[] { false }
        );
    }

    private Object invokePalette(TAAlertDialog.Variant variant) {
        try {
            Class<?> paletteClass = Class.forName("TA.java.component.TAAlertDialog$Palette");
            Method method = paletteClass.getDeclaredMethod("forVariant", TAAlertDialog.Variant.class);
            method.setAccessible(true);
            return method.invoke(null, variant);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create dialog palette", e);
        }
    }

    private Node invokeNode(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = TAAlertDialog.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (Node) method.invoke(null, args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
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

    private List<String> runOnFxThread(FxTask<List<String>> task) {
        startToolkit();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> result = new AtomicReference<>();
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
            // JavaFX toolkit is a singleton in the same test JVM.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting JavaFX toolkit", e);
        }
    }

    private void requireContains(List<String> values, String expected) {
        require(values.contains(expected), "Expected UI text [" + expected + "] in " + values);
    }

    private void requireAnyTextContains(List<String> values, String expectedSubstring) {
        for (String value : values) {
            if (value != null && value.contains(expectedSubstring)) {
                return;
            }
        }
        throw new IllegalStateException("Expected some UI text to contain [" + expectedSubstring + "] in " + values);
    }

    private void requireNoTextContains(List<String> values, String unexpectedSubstring) {
        for (String value : values) {
            require(value == null || !value.contains(unexpectedSubstring),
                "Unexpected UI text [" + unexpectedSubstring + "] in " + values);
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
