package TA.java.view;

import TA.java.TAApplication;
import TA.java.TAJob;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SkillGapAnalysisScrollTest {

    public static void main(String[] args) {
        try {
            SkillGapAnalysisScrollTest test = new SkillGapAnalysisScrollTest();
            test.testDialogResetsScrollToTopAfterShown();
            System.out.println("SkillGapAnalysisScrollTest passed.");
        } finally {
            Platform.exit();
        }
    }

    public void testDialogResetsScrollToTopAfterShown() {
        AtomicReference<Stage> stageRef = new AtomicReference<>();
        AtomicReference<ScrollPane> scrollPaneRef = new AtomicReference<>();

        try {
            runOnFxThread(() -> {
                SkillGapAnalysisView.showSkillGapAnalysis(createTallJob(), createApplicantWithoutMatches());

                Stage stage = findSkillGapStage();
                ScrollPane scrollPane = (ScrollPane) stage.getScene().getRoot();
                require(stage.getOnShown() != null, "skill gap dialog should reset scroll position after it is shown");

                scrollPane.setVvalue(1.0);
                stage.getOnShown().handle(new WindowEvent(stage, WindowEvent.WINDOW_SHOWN));

                stageRef.set(stage);
                scrollPaneRef.set(scrollPane);
                return null;
            });

            drainFxEvents();

            runOnFxThread(() -> {
                require(scrollPaneRef.get().getVvalue() == 0.0,
                    "skill gap dialog should start at the top of the scroll pane");
                return null;
            });
        } finally {
            runOnFxThread(() -> {
                if (stageRef.get() != null) {
                    stageRef.get().close();
                }
                return null;
            });
        }
    }

    private TAJob createTallJob() {
        return new TAJob(
            "__skill_gap_scroll_test__",
            "Teaching Assistant",
            "Software Engineering",
            "EBU6304",
            2,
            "Support labs, grading, and student questions.",
            "2026-06-30",
            "MOPan",
            true,
            Arrays.asList(
                "Java", "Python", "SQL", "Database", "Machine Learning",
                "Data Analysis", "Communication", "Teamwork", "Problem Solving",
                "C++", "Web", "Programming", "Teaching", "Research"
            )
        );
    }

    private TAApplication createApplicantWithoutMatches() {
        return new TAApplication(
            "Scroll Tester",
            "__skill_gap_scroll_ta__",
            "Software Engineering",
            "123456789",
            "scroll@example.com",
            "Tuesday",
            "Excel"
        );
    }

    private Stage findSkillGapStage() {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage stage
                && stage.isShowing()
                && "Skill Gap Analysis".equals(stage.getTitle())) {
                return stage;
            }
        }
        throw new IllegalStateException("Skill Gap Analysis stage was not shown");
    }

    private void drainFxEvents() {
        runOnFxThread(() -> null);
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
