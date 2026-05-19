package TA.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class SkillUtils {

    public static final int MAX_SKILL_COUNT = 10;
    public static final int MAX_KEYWORD_COUNT = 20;
    public static final int MAX_LABEL_LENGTH = 20;

    private static final List<String> DEFAULT_SKILLS = Arrays.asList(
        "Python",
        "Java",
        "Data Analysis",
        "Machine Learning",
        "Documentation",
        "Course Tutoring",
        "Lab Support",
        "Frontend",
        "Backend",
        "Communication",
        "SQL",
        "JavaFX",
        "Testing",
        "Project Management",
        "Research",
        "Mathematics",
        "C++",
        "Git",
        "HTML",
        "CSS"
    );

    private SkillUtils() {
    }

    public static List<String> getDefaultSkills() {
        return new ArrayList<>(DEFAULT_SKILLS);
    }

    public static List<String> parseSkills(String rawText) {
        return normalizeSkills(splitLabels(rawText));
    }

    public static List<String> parseKeywords(String rawText) {
        return normalizeKeywords(splitLabels(rawText));
    }

    public static List<String> normalizeSkills(Collection<String> values) {
        return normalizeLabels(values, MAX_SKILL_COUNT);
    }

    public static List<String> normalizeKeywords(Collection<String> values) {
        return normalizeLabels(values, MAX_KEYWORD_COUNT);
    }

    public static String validateSkills(Collection<String> values) {
        List<String> labels = compactLabels(values);
        if (labels.size() > MAX_SKILL_COUNT) {
            return "Please keep skill requirements to " + MAX_SKILL_COUNT + " items or fewer.";
        }
        for (String label : labels) {
            if (label.length() > MAX_LABEL_LENGTH) {
                return "Each skill must be " + MAX_LABEL_LENGTH + " characters or fewer: " + label;
            }
        }
        return null;
    }

    public static String toDisplayText(Collection<String> values, String emptyText) {
        List<String> labels = normalizeLabels(values, values == null ? 0 : values.size());
        if (labels.isEmpty()) {
            return emptyText;
        }
        return String.join(", ", labels);
    }

    public static String toEditableText(Collection<String> values) {
        List<String> labels = normalizeLabels(values, values == null ? 0 : values.size());
        return String.join(", ", labels);
    }

    public static boolean looselyMatches(String expected, String actual) {
        String left = normalizeForCompare(expected);
        String right = normalizeForCompare(actual);
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        return left.length() >= 3 && right.length() >= 3 && (left.contains(right) || right.contains(left));
    }

    public static String normalizeForCompare(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static List<String> splitLabels(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(rawText.split("[,，;；\\n\\r\\t]+"))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toList());
    }

    private static List<String> normalizeLabels(Collection<String> values, int maxCount) {
        Map<String, String> unique = new LinkedHashMap<>();
        for (String value : compactLabels(values)) {
            String trimmed = value.length() > MAX_LABEL_LENGTH ? value.substring(0, MAX_LABEL_LENGTH) : value;
            unique.putIfAbsent(normalizeForCompare(trimmed), trimmed);
            if (maxCount > 0 && unique.size() >= maxCount) {
                break;
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static List<String> compactLabels(Collection<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim().replaceAll("\\s+", " ");
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
