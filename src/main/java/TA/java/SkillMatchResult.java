package TA.java;

import java.util.ArrayList;
import java.util.List;

public class SkillMatchResult {
    private final int score;
    private final List<String> requiredSkills;
    private final List<String> candidateKeywords;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final String explanation;

    public SkillMatchResult(
        int score,
        List<String> requiredSkills,
        List<String> candidateKeywords,
        List<String> matchedSkills,
        List<String> missingSkills,
        String explanation
    ) {
        this.score = score;
        this.requiredSkills = copy(requiredSkills);
        this.candidateKeywords = copy(candidateKeywords);
        this.matchedSkills = copy(matchedSkills);
        this.missingSkills = copy(missingSkills);
        this.explanation = explanation == null ? "" : explanation;
    }

    public int getScore() {
        return score;
    }

    public List<String> getRequiredSkills() {
        return copy(requiredSkills);
    }

    public List<String> getCandidateKeywords() {
        return copy(candidateKeywords);
    }

    public List<String> getMatchedSkills() {
        return copy(matchedSkills);
    }

    public List<String> getMissingSkills() {
        return copy(missingSkills);
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean hasRequirements() {
        return !requiredSkills.isEmpty();
    }

    private static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
