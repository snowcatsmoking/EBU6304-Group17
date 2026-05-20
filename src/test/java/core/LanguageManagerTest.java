package core;

public class LanguageManagerTest {

    public static void main(String[] args) {
        LanguageManagerTest test = new LanguageManagerTest();
        test.testUiTextTranslationUsesSelectedLanguage();
        test.testTaUiTextCoversLoginProfileDashboardPositionsAndApplications();
    }

    public void testUiTextTranslationUsesSelectedLanguage() {
        LanguageManager languageManager = LanguageManager.getInstance();

        languageManager.setLanguage("en");
        require("Log In".equals(languageManager.text("Log In")),
            "English UI text should remain unchanged");
        require("Profile saved successfully.".equals(languageManager.text("个人档案已保存！")),
            "Chinese hardcoded UI text should be translated in English mode");

        languageManager.setLanguage("zh");
        require("登录".equals(languageManager.text("Log In")),
            "Chinese UI text should be translated");
        require("课程组织者控制台".equals(languageManager.text("Module Organiser Console")),
            "MO console title should be translated");
        require("Unmapped Text".equals(languageManager.text("Unmapped Text")),
            "unmapped UI text should fall back to the original text");
    }

    public void testTaUiTextCoversLoginProfileDashboardPositionsAndApplications() {
        LanguageManager languageManager = LanguageManager.getInstance();
        languageManager.setLanguage("zh");

        requireEquals("\u8d26\u53f7\uff08\u5b66\u53f7 / \u5de5\u53f7\uff09", languageManager.text("Account (Student ID / Staff ID)"));
        requireEquals("\u8bf7\u8f93\u5165\u8d26\u53f7", languageManager.text("Enter your account"));
        requireEquals("\u5bc6\u7801", languageManager.text("Password"));
        requireEquals("\u8bf7\u8f93\u5165\u5bc6\u7801", languageManager.text("Enter your password"));
        requireEquals("\u8bf7\u5148\u9009\u62e9\u89d2\u8272\uff0c\u518d\u8f93\u5165\u6570\u5b57", languageManager.text("Select role first, then enter numbers"));

        requireEquals("\u59d3\u540d", languageManager.text("Name"));
        requireEquals("\u5b66\u53f7", languageManager.text("Student ID"));
        requireEquals("\u4e13\u4e1a", languageManager.text("Major"));
        requireEquals("\u7535\u8bdd", languageManager.text("Phone"));
        requireEquals("\u90ae\u7bb1", languageManager.text("Email"));
        requireEquals("\u6280\u80fd", languageManager.text("Skills"));

        requireEquals("\u5ba1\u6838\u4e2d", languageManager.text("Under Review"));
        requireEquals("\u5df2\u901a\u8fc7", languageManager.text("Approved"));
        requireEquals("\u5df2\u62d2\u7edd", languageManager.text("Rejected"));
        requireEquals("\u5df2\u64a4\u56de", languageManager.text("Withdrawn"));

        requireEquals("\u8bf7\u8f93\u5165\u8bfe\u7a0b\u540d\u79f0", languageManager.text("Enter course name"));
        requireEquals("\u9009\u62e9\u65e5\u671f", languageManager.text("Select date"));
        requireEquals("\u8bf7\u8f93\u5165\u4eba\u6570", languageManager.text("Enter number"));
        requireEquals("\u8981\u6c42\uff1aAlgorithms", languageManager.text("Requirements: Algorithms"));
        requireEquals("\u64cd\u4f5c", languageManager.text("Actions"));
    }

    private void requireEquals(String expected, String actual) {
        require(expected.equals(actual), "Expected [" + expected + "] but got [" + actual + "]");
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
