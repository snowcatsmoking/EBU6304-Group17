package MO;

import TA.java.TAJob;
import core.LanguageManager;
import data.JobDataManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MODashboardTranslationTest {

    private static final String TEST_MO_ID = "__translation_test_mo__";
    private static final String OPEN_JOB_ID = "__translation_test_mo_open_job__";
    private static final String CLOSED_JOB_ID = "__translation_test_mo_closed_job__";

    public static void main(String[] args) {
        MODashboardTranslationTest test = new MODashboardTranslationTest();
        test.testPositionStatisticsViewUsesSelectedLanguage();
        test.testApplicationReviewHeaderAndFiltersUseSelectedLanguage();
        test.testMyPositionsViewUsesSelectedLanguageWhenEmpty();
        test.testMyPositionsListActionsUseSelectedLanguage();
    }

    public void testPositionStatisticsViewUsesSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            Node view = runOnFxThread(() -> buildPrivateView("buildPositionStatisticsView"));
            List<String> texts = collectVisibleTexts(view);

            requireContains(texts, "\u5c97\u4f4d\u7edf\u8ba1");
            requireContains(texts, "\u8ddf\u8e2a\u5c97\u4f4d\u72b6\u6001\u3001\u7533\u8bf7\u6570\u91cf\u548c\u901a\u8fc7\u6570\u91cf\u3002");
            requireContains(texts, "\u5237\u65b0");
            requireContains(texts, "\u5f00\u653e\u5c97\u4f4d");
            requireContains(texts, "\u5df2\u5173\u95ed\u5c97\u4f4d");
            requireContains(texts, "\u5df2\u8fc7\u671f\u5c97\u4f4d");
            requireContains(texts, "\u603b\u7533\u8bf7\u6570");
            requireContains(texts, "\u603b\u901a\u8fc7\u6570");
        } finally {
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    public void testApplicationReviewHeaderAndFiltersUseSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");

        try {
            Node view = runOnFxThread(() -> buildPrivateView("buildApplicantReviewView"));
            List<String> texts = collectVisibleTexts(view);
            List<String> prompts = collectPrompts(view);
            List<String> comboItems = collectComboItems(view);

            requireContains(texts, "\u7533\u8bf7\u5ba1\u6838");
            requireContains(texts, "\u5ba1\u6838\u5f53\u524d\u8bfe\u7a0b\u7ec4\u7ec7\u8005\u53d1\u5e03\u5c97\u4f4d\u6536\u5230\u7684\u6240\u6709\u7533\u8bf7\u3002");
            requireContains(texts, "\u6e05\u9664\u7b5b\u9009");
            requireContains(texts, "\u9009\u62e9\u672c\u9875\u5f85\u5ba1\u6838");
            requireContains(texts, "\u6e05\u9664\u9009\u62e9");
            requireContains(texts, "\u6279\u91cf\u901a\u8fc7");
            requireContains(texts, "\u6279\u91cf\u62d2\u7edd");
            requireContains(texts, "\u5339\u914d\u7533\u8bf7\u6570\uff1a0 \u5f85\u5ba1\u6838\uff1a0");
            requireContains(texts, "\u5df2\u9009\u62e9\u5f85\u5ba1\u6838\u7533\u8bf7\uff1a0");
            requireContains(texts, "\u6ca1\u6709\u7533\u8bf7\u7b26\u5408\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u3002");

            requireContains(prompts, "\u6309\u4e13\u4e1a\u7b5b\u9009");
            requireContains(prompts, "\u6309\u53ef\u4efb\u804c\u65f6\u95f4\u7b5b\u9009");
            requireContains(prompts, "\u6309\u6280\u80fd\u6216\u5173\u952e\u8bcd\u7b5b\u9009");

            requireContains(comboItems, "\u5339\u914d\u5206\u6570");
            requireContains(comboItems, "\u7533\u8bf7\u65e5\u671f");
            requireContains(comboItems, "\u5ba1\u6838\u72b6\u6001");
            requireContains(comboItems, "\u59d3\u540d / \u5b66\u53f7");
        } finally {
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    public void testMyPositionsViewUsesSelectedLanguageWhenEmpty() {
        LanguageManager.getInstance().setLanguage("zh");
        cleanupTestJobs();

        try {
            Node view = runOnFxThread(() -> buildPrivateView("buildMyPositionsView", TEST_MO_ID));
            List<String> texts = collectVisibleTexts(view);

            requireContains(texts, "\u6211\u7684\u5c97\u4f4d");
            requireContains(texts, "\u4f60\u8fd8\u6ca1\u6709\u53d1\u5e03\u4efb\u4f55\u5c97\u4f4d\u3002\u70b9\u51fb\u5de6\u4fa7\u201c\u53d1\u5e03\u5c97\u4f4d\u201d\u5f00\u59cb\u3002");
        } finally {
            cleanupTestJobs();
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    public void testMyPositionsListActionsUseSelectedLanguage() {
        LanguageManager.getInstance().setLanguage("zh");
        cleanupTestJobs();

        try {
            createTestJob(OPEN_JOB_ID, "Open Translation Job", false);
            createTestJob(CLOSED_JOB_ID, "Closed Translation Job", true);

            Node view = runOnFxThread(() -> buildPrivateView("buildMyPositionsView", TEST_MO_ID));
            List<String> texts = collectVisibleTexts(view);

            requireContains(texts, "\u5f00\u653e");
            requireContains(texts, "\u5df2\u5173\u95ed");
            requireContains(texts, "\u62db\u8058\u4eba\u6570\uff1a2  \u622a\u6b62\u65f6\u95f4\uff1a2099-12-31");
            requireContains(texts, "\u6280\u80fd\u8981\u6c42\uff1aJava, Python");
            requireContains(texts, "\u7533\u8bf7\u4eba\u6570\uff1a0");
            requireContains(texts, "\u7f16\u8f91\u5c97\u4f4d");
            requireContains(texts, "\u67e5\u770b\u8be6\u60c5");
            requireContains(texts, "\u5173\u95ed\u5c97\u4f4d");
            requireContains(texts, "\u91cd\u65b0\u5f00\u653e\u5c97\u4f4d");
        } finally {
            cleanupTestJobs();
            LanguageManager.getInstance().setLanguage("en");
        }
    }

    private Node buildPrivateView(String methodName) {
        return buildPrivateView(methodName, TEST_MO_ID);
    }

    private Node buildPrivateView(String methodName, String moStaffId) {
        try {
            Method method = MODashboard.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return (Node) method.invoke(new MODashboard(moStaffId));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build " + methodName, e);
        }
    }

    private void createTestJob(String jobId, String positionName, boolean closed) {
        TAJob job = new TAJob();
        job.setJobId(jobId);
        job.setPositionName(positionName);
        job.setCourseName("Translation Course");
        job.setCourseCode("TR001");
        job.setRecruitmentCount(2);
        job.setRequirements("Translation coverage");
        job.setDeadline("2099-12-31");
        job.setPublisher("Translation Tester");
        job.setMoStaffId(TEST_MO_ID);
        job.setActive(closed);
        job.setRequiredSkills(Arrays.asList("Java", "Python"));

        try {
            new JobDataManager().saveJob(job);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create test job", e);
        }
    }

    private void cleanupTestJobs() {
        JobDataManager manager = new JobDataManager();
        manager.deleteJob(OPEN_JOB_ID);
        manager.deleteJob(CLOSED_JOB_ID);
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

    private List<String> collectVisibleTexts(Node node) {
        List<String> values = new ArrayList<>();
        collectVisibleTexts(node, values);
        return values;
    }

    private void collectVisibleTexts(Node node, List<String> values) {
        if (node instanceof Labeled labeled) {
            values.add(labeled.getText());
        }
        if (node instanceof ScrollPane scrollPane && scrollPane.getContent() != null) {
            collectVisibleTexts(scrollPane.getContent(), values);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectVisibleTexts(child, values);
            }
        }
    }

    private List<String> collectPrompts(Node node) {
        List<String> values = new ArrayList<>();
        collectPrompts(node, values);
        return values;
    }

    private void collectPrompts(Node node, List<String> values) {
        if (node instanceof TextInputControl input) {
            values.add(input.getPromptText());
        }
        if (node instanceof ScrollPane scrollPane && scrollPane.getContent() != null) {
            collectPrompts(scrollPane.getContent(), values);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectPrompts(child, values);
            }
        }
    }

    private List<String> collectComboItems(Node node) {
        List<String> values = new ArrayList<>();
        collectComboItems(node, values);
        return values;
    }

    private void collectComboItems(Node node, List<String> values) {
        if (node instanceof ComboBox<?> comboBox) {
            for (Object item : comboBox.getItems()) {
                values.add(String.valueOf(item));
            }
        }
        if (node instanceof ScrollPane scrollPane && scrollPane.getContent() != null) {
            collectComboItems(scrollPane.getContent(), values);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectComboItems(child, values);
            }
        }
    }

    private void requireContains(List<String> values, String expected) {
        require(values.contains(expected), "Expected UI text [" + expected + "] in " + values);
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
