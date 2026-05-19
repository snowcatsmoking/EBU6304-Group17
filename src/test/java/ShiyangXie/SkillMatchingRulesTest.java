package ShiyangXie;

import TA.java.ResumeKeywordExtractor;
import TA.java.SkillMatchResult;
import TA.java.SkillMatcher;
import TA.java.SkillUtils;
import TA.java.TAApplicationRecord;
import TA.java.TAJob;

import java.util.Arrays;
import java.util.List;

public class SkillMatchingRulesTest {

    public static void main(String[] args) {
        testSkillNormalizationAndValidation();
        testResumeKeywordExtraction();
        testSkillMatchScoreAndExplanation();
        System.out.println("SkillMatchingRulesTest passed.");
    }

    private static void testSkillNormalizationAndValidation() {
        List<String> normalized = SkillUtils.normalizeSkills(Arrays.asList(
            " Python ",
            "python",
            "Machine Learning",
            "Communication"
        ));

        require(normalized.size() == 3, "duplicate skills should be removed case-insensitively");
        require(normalized.contains("Python"), "trimmed skill should be kept");
        require(SkillUtils.validateSkills(Arrays.asList("This label is definitely too long")) != null,
            "long skill labels should be rejected before saving");
    }

    private static void testResumeKeywordExtraction() {
        List<String> keywords = ResumeKeywordExtractor.extractKeywords(
            "I used Python and machine learning in a data analysis project. I also worked as a TA.",
            "SQL, Java",
            null
        );

        require(keywords.contains("Python"), "resume should extract Python");
        require(keywords.contains("Machine Learning"), "resume should extract Machine Learning");
        require(keywords.contains("TA Experience"), "resume should extract TA experience");
        require(keywords.contains("SQL"), "profile skills should be merged into keywords");
    }

    private static void testSkillMatchScoreAndExplanation() {
        TAJob job = new TAJob();
        job.setRequiredSkills(Arrays.asList("Python", "Machine Learning", "Java"));

        TAApplicationRecord record = new TAApplicationRecord();
        record.setSkills("Python");
        record.setResumeKeywords(Arrays.asList("Machine Learning", "Communication"));

        SkillMatchResult result = SkillMatcher.calculate(job, record);

        require(result.getScore() == 67, "two of three required skills should produce 67 percent");
        require(result.getMatchedSkills().contains("Python"), "matched skills should include Python");
        require(result.getMatchedSkills().contains("Machine Learning"), "matched skills should include Machine Learning");
        require(result.getMissingSkills().contains("Java"), "missing skills should include Java");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
