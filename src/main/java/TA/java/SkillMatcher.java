package TA.java;

import java.util.ArrayList;
import java.util.List;

public final class SkillMatcher {

    private SkillMatcher() {
    }

    public static SkillMatchResult calculate(TAJob job, TAApplicationRecord record) {
        List<String> requiredSkills = job == null ? new ArrayList<>() : job.getRequiredSkills();
        List<String> candidateKeywords = collectCandidateKeywords(record);

        if (requiredSkills.isEmpty()) {
            return new SkillMatchResult(
                0,
                requiredSkills,
                candidateKeywords,
                new ArrayList<>(),
                new ArrayList<>(),
                "No skill requirements have been set for this position."
            );
        }

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {
            boolean matched = candidateKeywords.stream()
                .anyMatch(candidateSkill -> SkillUtils.looselyMatches(requiredSkill, candidateSkill));
            if (matched) {
                matchedSkills.add(requiredSkill);
            } else {
                missingSkills.add(requiredSkill);
            }
        }

        int score = Math.round((matchedSkills.size() * 100f) / requiredSkills.size());
        String explanation = "Matched: " + matchedSkills.size() + " of " + requiredSkills.size()
            + " required skills.";

        return new SkillMatchResult(score, requiredSkills, candidateKeywords, matchedSkills, missingSkills, explanation);
    }

    private static List<String> collectCandidateKeywords(TAApplicationRecord record) {
        List<String> keywords = new ArrayList<>();
        if (record == null) {
            return keywords;
        }
        keywords.addAll(SkillUtils.parseSkills(record.getSkills()));
        keywords.addAll(record.getEffectiveKeywords());
        keywords.addAll(ResumeKeywordExtractor.extractKeywords(record.getResumeText(), record.getSkills(), null));
        return SkillUtils.normalizeKeywords(keywords);
    }
}
