package MO;

import TA.java.TAJob;
import core.LanguageManager;
import data.JobDataManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MODashboardNavigationTest {

    private static final String TEST_MO_ID = "__navigation_test_mo__";
    private static final String TEST_JOB_ID = "__navigation_test_job__";

    public static void main(String[] args) {
        MODashboardNavigationTest test = new MODashboardNavigationTest();
        test.testDetailAndBackNavigationKeepSharedContentPane();
        System.out.println("MODashboardNavigationTest passed.");
    }

    public void testDetailAndBackNavigationKeepSharedContentPane() {
        LanguageManager.getInstance().setLanguage("en");
        cleanupTestJobs();
        createTestJob();

        try {
            runOnFxThread(() -> {
                MODashboard dashboard = new MODashboard(TEST_MO_ID);
                BorderPane root = new BorderPane();
                BorderPane contentPane = new BorderPane();
                root.setCenter(contentPane);

                setField(dashboard, "root", root);
                setField(dashboard, "contentPane", contentPane);

                Node myPositionsView = invokeNodeMethod(dashboard, "buildMyPositionsView");
                contentPane.setCenter(myPositionsView);

                Button detailButton = findButton(myPositionsView, "View Details");
                require(detailButton != null, "View Details button should exist");
                detailButton.fire();

                require(root.getCenter() == contentPane,
                    "Opening job details should keep the shared content pane mounted");
                Node detailView = contentPane.getCenter();
                require(detailView != null && detailView != myPositionsView,
                    "Job detail view should be shown inside the shared content pane");

                Button backButton = findButton(detailView, "Back to My Positions");
                require(backButton != null, "Back to My Positions button should exist");
                backButton.fire();

                require(root.getCenter() == contentPane,
                    "Returning from job details should keep the shared content pane mounted");
                Node returnedView = contentPane.getCenter();
                require(returnedView != null && returnedView != detailView,
                    "My Positions should be restored inside the shared content pane");

                Node statisticsView = invokeNodeMethod(dashboard, "buildPositionStatisticsView");
                invokeVoidMethod(dashboard, "showWithFade", Node.class, statisticsView);
                require(contentPane.getCenter() == statisticsView,
                    "Sidebar-driven content changes should still target the visible content pane after returning");
                return null;
            });
        } finally {
            cleanupTestJobs();
        }
    }

    private void createTestJob() {
        TAJob job = new TAJob();
        job.setJobId(TEST_JOB_ID);
        job.setPositionName("Navigation Test Job");
        job.setCourseName("Navigation Course");
        job.setCourseCode("NAV001");
        job.setRecruitmentCount(1);
        job.setRequirements("Navigation coverage");
        job.setDeadline("2099-12-31");
        job.setPublisher("Navigation Tester");
        job.setMoStaffId(TEST_MO_ID);
        job.setActive(false);

        try {
            new JobDataManager().saveJob(job);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create test job", e);
        }
    }

    private void cleanupTestJobs() {
        new JobDataManager().deleteJob(TEST_JOB_ID);
    }

    private Node invokeNodeMethod(MODashboard dashboard, String methodName) {
        try {
            Method method = MODashboard.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (Node) method.invoke(dashboard);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }

    private void invokeVoidMethod(MODashboard dashboard, String methodName, Class<?> paramType, Object argument) {
        try {
            Method method = MODashboard.class.getDeclaredMethod(methodName, paramType);
            method.setAccessible(true);
            method.invoke(dashboard, argument);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke " + methodName, e);
        }
    }

    private void setField(MODashboard dashboard, String fieldName, Object value) {
        try {
            Field field = MODashboard.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dashboard, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field " + fieldName, e);
        }
    }

    private Button findButton(Node node, String expectedTextFragment) {
        if (node instanceof Button button && button.getText() != null
            && button.getText().contains(expectedTextFragment)) {
            return button;
        }
        if (node instanceof ScrollPane scrollPane && scrollPane.getContent() != null) {
            Button nested = findButton(scrollPane.getContent(), expectedTextFragment);
            if (nested != null) {
                return nested;
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button nested = findButton(child, expectedTextFragment);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private Node runOnFxThread(FxTask<Node> task) {
        startToolkit();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Node> result = new AtomicReference<>();
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
            // JavaFX toolkit is a singleton in the test JVM.
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
