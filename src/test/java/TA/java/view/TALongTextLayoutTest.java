package TA.java.view;

import TA.java.TAApplication;
import TA.java.TAJob;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TALongTextLayoutTest {

    private static final String LONG_REQUIREMENTS =
        "Strong Java programming experience, data structure knowledge, weekly lab support, grading, "
            + "communication skills, and enough detail to exceed one normal line in the application dialog.";

    private static final String LONG_SKILLS =
        "Java, JavaFX, Python, machine learning coursework, database design, lab tutoring, grading, "
            + "team communication, Git, debugging, and technical writing";

    public static void main(String[] args) {
        try {
            TALongTextLayoutTest test = new TALongTextLayoutTest();
            test.testApplicationDialogLongLabelsWrap();
            test.testProfileSkillsUsesMultilineInput();
        } finally {
            Platform.exit();
        }
    }

    public void testApplicationDialogLongLabelsWrap() {
        runOnFxThread(() -> {
            TAApplicationFormView formView = new TAApplicationFormView(createJob(), "__layout_test__");
            setField(formView, "currentUser", new TAApplication(
                "Layout Tester",
                "__layout_test__",
                "Software Engineering",
                "123456789",
                "layout@example.com",
                "2026-06-01",
                LONG_SKILLS
            ));

            VBox jobInfoBox = invokeVBox(formView, "createJobInfoBox");
            VBox personalInfoBox = invokeVBox(formView, "createPersonalInfoBox");

            Label requirements = findLabel(jobInfoBox, LONG_REQUIREMENTS);
            require(requirements.isWrapText(), "requirements should wrap instead of being clipped");
            require(requirements.getMaxWidth() < Double.MAX_VALUE, "requirements should have a constrained wrapping width");

            Label requiredSkills = findLabel(jobInfoBox, "Java, Python, JavaFX, tutoring, grading");
            require(requiredSkills.isWrapText(), "required skills should wrap instead of being clipped");
            require(requiredSkills.getMaxWidth() < Double.MAX_VALUE, "required skills should have a constrained wrapping width");

            Label profileSkills = findLabel(personalInfoBox, LONG_SKILLS);
            require(profileSkills.isWrapText(), "profile skills should wrap in the application dialog");
            require(profileSkills.getMaxWidth() < Double.MAX_VALUE, "profile skills should have a constrained wrapping width");
            return null;
        });
    }

    public void testProfileSkillsUsesMultilineInput() {
        runOnFxThread(() -> {
            ProfileView profileView = new ProfileView();
            VBox field = invokeVBox(
                profileView,
                "createFormField",
                new Class<?>[] { String.class, String.class, String.class },
                "Skills",
                LONG_SKILLS,
                "skill"
            );

            List<TextArea> textAreas = collectNodes(field, TextArea.class);
            require(textAreas.size() == 1, "skills should use a multiline TextArea");
            require(textAreas.get(0).isWrapText(), "skills TextArea should wrap long content");
            require(collectNodes(field, TextField.class).isEmpty(), "skills should not use a single-line TextField");
            return null;
        });
    }

    private TAJob createJob() {
        return new TAJob(
            "layout-job",
            "Teaching Assistant",
            "Software Engineering",
            "EBU6304",
            2,
            LONG_REQUIREMENTS,
            "2026-06-30",
            "MOPan",
            false,
            List.of("Java", "Python", "JavaFX", "tutoring", "grading")
        );
    }

    private VBox invokeVBox(Object target, String methodName) {
        return invokeVBox(target, methodName, new Class<?>[0]);
    }

    private VBox invokeVBox(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return (VBox) method.invoke(target, args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set " + fieldName, e);
        }
    }

    private Label findLabel(Node root, String expectedText) {
        for (Label label : collectNodes(root, Label.class)) {
            if (expectedText.equals(label.getText())) {
                return label;
            }
        }
        throw new IllegalStateException("Expected label [" + expectedText + "]");
    }

    private <T extends Node> List<T> collectNodes(Node node, Class<T> type) {
        List<T> nodes = new ArrayList<>();
        collectNodes(node, type, nodes);
        return nodes;
    }

    private <T extends Node> void collectNodes(Node node, Class<T> type, List<T> nodes) {
        if (type.isInstance(node)) {
            nodes.add(type.cast(node));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectNodes(child, type, nodes);
            }
        }
    }

    private <T> T runOnFxThread(FxTask<T> task) {
        startToolkit();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
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
            // JavaFX toolkit can only be started once in the same JVM.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting JavaFX toolkit", e);
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
