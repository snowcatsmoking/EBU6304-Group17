package TA.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ResumeKeywordExtractor {

    private static final List<String> KEYWORD_DICTIONARY = new ArrayList<>();

    static {
        KEYWORD_DICTIONARY.addAll(SkillUtils.getDefaultSkills());
        KEYWORD_DICTIONARY.addAll(Arrays.asList(
            "Excel",
            "PowerPoint",
            "R",
            "MATLAB",
            "Statistics",
            "Deep Learning",
            "Database",
            "Spring",
            "JavaScript",
            "React",
            "Node.js",
            "Linux",
            "Teaching",
            "Grading",
            "Assessment",
            "Tutorial",
            "Workshop",
            "Project Experience",
            "TA Experience",
            "Certificate",
            "Award"
        ));
    }

    private ResumeKeywordExtractor() {
    }

    public static List<String> extractKeywords(String resumeText) {
        return extractKeywords(resumeText, null, null);
    }

    public static List<String> extractKeywords(String resumeText, String profileSkills, TAJob job) {
        Set<String> extracted = new LinkedHashSet<>();
        String text = normalizeText(resumeText);

        for (String skill : SkillUtils.parseSkills(profileSkills)) {
            extracted.add(skill);
        }

        if (job != null) {
            for (String requiredSkill : job.getRequiredSkills()) {
                if (containsPhrase(text, requiredSkill)) {
                    extracted.add(requiredSkill);
                }
            }
        }

        for (String keyword : KEYWORD_DICTIONARY) {
            if (containsPhrase(text, keyword)) {
                extracted.add(keyword);
            }
        }

        if (containsAny(text, "project", "projects", "项目")) {
            extracted.add("Project Experience");
        }
        if (containsAny(text, "teaching assistant", " ta ", "助教", "辅导")) {
            extracted.add("TA Experience");
        }
        if (containsAny(text, "research", "论文", "研究")) {
            extracted.add("Research");
        }
        if (containsAny(text, "certificate", "certification", "证书")) {
            extracted.add("Certificate");
        }
        if (containsAny(text, "award", "scholarship", "奖")) {
            extracted.add("Award");
        }

        return SkillUtils.normalizeKeywords(extracted);
    }

    private static String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        return " " + text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ") + " ";
    }

    private static boolean containsPhrase(String text, String phrase) {
        if (text.isEmpty() || phrase == null || phrase.trim().isEmpty()) {
            return false;
        }
        return text.contains(phrase.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean containsAny(String text, String... phrases) {
        for (String phrase : phrases) {
            if (containsPhrase(text, phrase)) {
                return true;
            }
        }
        return false;
    }
}
