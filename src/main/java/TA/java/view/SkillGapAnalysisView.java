package TA.java.view;
import TA.java.TAJob;
import TA.java.TAApplication;
import TA.java.SkillUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillGapAnalysisView {

    private static final Map<String, String> SKILL_SUGGESTIONS = new HashMap<>();

    static {
        SKILL_SUGGESTIONS.put("java", "Java: Practice object-oriented programming with projects like library management systems. Use online platforms like LeetCode for algorithm practice.");
        SKILL_SUGGESTIONS.put("python", "Python: Start with basic syntax, then explore data analysis libraries (pandas, numpy). Build small automation scripts for practice.");
        SKILL_SUGGESTIONS.put("javascript", "JavaScript: Learn ES6+ features, then explore frameworks like React or Node.js. Build interactive web projects.");
        SKILL_SUGGESTIONS.put("sql", "SQL: Practice database design and query optimization. Work on projects involving data retrieval and manipulation.");
        SKILL_SUGGESTIONS.put("database", "Database: Study relational database concepts, normalization, and learn both SQL and NoSQL databases.");
        SKILL_SUGGESTIONS.put("machine learning", "Machine Learning: Start with linear regression, then progress to classification algorithms. Use scikit-learn for hands-on practice.");
        SKILL_SUGGESTIONS.put("data analysis", "Data Analysis: Learn Excel advanced functions, Python pandas, and visualization tools like Tableau.");
        SKILL_SUGGESTIONS.put("communication", "Communication: Practice technical writing and presentations. Join discussion groups to improve verbal skills.");
        SKILL_SUGGESTIONS.put("teamwork", "Teamwork: Participate in group projects, learn version control (Git), and collaborative development practices.");
        SKILL_SUGGESTIONS.put("problem solving", "Problem Solving: Practice algorithmic challenges on platforms like HackerRank to improve analytical thinking.");
        SKILL_SUGGESTIONS.put("c++", "C++: Focus on memory management and performance optimization. Build projects using STL containers and algorithms.");
        SKILL_SUGGESTIONS.put("web", "Web Development: Learn HTML/CSS basics, then JavaScript. Progress to front-end frameworks or back-end technologies.");
        SKILL_SUGGESTIONS.put("programming", "Programming: Start with one language (Python recommended), understand fundamentals, then explore data structures and algorithms.");
        SKILL_SUGGESTIONS.put("teaching", "Teaching: Practice explaining concepts clearly. Help classmates understand topics to improve instructional skills.");
        SKILL_SUGGESTIONS.put("research", "Research: Learn academic writing conventions, literature review methods, and data collection techniques.");
    }

    public static void showSkillGapAnalysis(TAJob job, TAApplication applicant) {
        Stage stage = new Stage();
        stage.setTitle("Skill Gap Analysis");
        stage.initModality(Modality.WINDOW_MODAL);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f8fafc;");
        root.setPadding(new Insets(24, 24, 24, 24));
        root.setSpacing(20);
        root.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("Skill Gap Analysis");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox contentBox = new VBox();
        contentBox.setSpacing(16);

        AnalysisResult analysis = buildAnalysis(job, applicant);
        List<String> matchedSkills = analysis.getMatchedSkills();
        List<String> missingSkills = analysis.getMissingSkills();

        VBox summaryBox = new VBox();
        summaryBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        summaryBox.setPadding(new Insets(16, 16, 16, 16));
        summaryBox.setSpacing(12);

        Label summaryTitle = new Label("Application Summary");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        HBox matchRateBox = new HBox();
        matchRateBox.setSpacing(8);
        matchRateBox.setAlignment(Pos.CENTER_LEFT);
        Label matchRateLabel = new Label("Match Rate: ");
        matchRateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        Label matchRateValue = new Label(analysis.getMatchRate() + "%");
        matchRateValue.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #15803d;");
        matchRateBox.getChildren().addAll(matchRateLabel, matchRateValue);

        HBox matchedBox = new HBox();
        matchedBox.setSpacing(8);
        matchedBox.setAlignment(Pos.CENTER_LEFT);
        Label matchedLabel = new Label("Matched Skills: ");
        matchedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        Label matchedValue = new Label(matchedSkills.isEmpty() ? "None" : String.join(", ", matchedSkills));
        matchedValue.setStyle("-fx-font-size: 13px; -fx-text-fill: #15803d;");
        matchedBox.getChildren().addAll(matchedLabel, matchedValue);

        summaryBox.getChildren().addAll(summaryTitle, matchRateBox, matchedBox);

        VBox gapBox = new VBox();
        gapBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4);");
        gapBox.setPadding(new Insets(16, 16, 16, 16));
        gapBox.setSpacing(12);

        Label gapTitle = new Label("Skills to Improve");
        gapTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");

        if (missingSkills.isEmpty()) {
            Label allMatchedLabel = new Label("Great job! You meet all the skill requirements for this position.");
            allMatchedLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #15803d;");
            gapBox.getChildren().addAll(gapTitle, allMatchedLabel);
        } else {
            Label gapIntro = new Label("The following skills are required but not found in your profile:");
            gapIntro.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

            VBox skillBadges = new VBox();
            skillBadges.setSpacing(8);

            for (String skill : missingSkills) {
                HBox skillRow = new HBox();
                skillRow.setSpacing(8);
                skillRow.setAlignment(Pos.CENTER_LEFT);

                Label badge = new Label(skill);
                badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc2626; -fx-background-color: #fee2e2; -fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10 4 10;");
                skillRow.getChildren().add(badge);

                skillBadges.getChildren().add(skillRow);
            }

            gapBox.getChildren().addAll(gapTitle, gapIntro, skillBadges);
        }

        VBox suggestionBox = new VBox();
        suggestionBox.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fde68a; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12;");
        suggestionBox.setPadding(new Insets(16, 16, 16, 16));
        suggestionBox.setSpacing(12);

        Label suggestionTitle = new Label("Improvement Suggestions");
        suggestionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #92400e;");

        if (missingSkills.isEmpty()) {
            Label suggestionText = new Label("You are well-prepared for this position! Continue to maintain and improve your skills.");
            suggestionText.setStyle("-fx-font-size: 13px; -fx-text-fill: #92400e; -fx-wrap-text: true;");
            suggestionText.setMaxWidth(500);
            suggestionText.setTextAlignment(TextAlignment.LEFT);
            suggestionBox.getChildren().addAll(suggestionTitle, suggestionText);
        } else {
            VBox suggestionList = new VBox();
            suggestionList.setSpacing(12);

            for (String skill : missingSkills) {
                String suggestion = getSuggestion(skill);
                VBox suggestionItem = new VBox();
                suggestionItem.setSpacing(4);

                Label skillLabel = new Label(skill.toUpperCase());
                skillLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #92400e;");

                Label suggestionText = new Label(suggestion);
                suggestionText.setStyle("-fx-font-size: 12px; -fx-text-fill: #78350f; -fx-wrap-text: true;");
                suggestionText.setMaxWidth(480);
                suggestionText.setTextAlignment(TextAlignment.LEFT);

                suggestionItem.getChildren().addAll(skillLabel, suggestionText);
                suggestionList.getChildren().add(suggestionItem);
            }

            suggestionBox.getChildren().addAll(suggestionTitle, suggestionList);
        }

        Button closeButton = new Button("Got It");
        closeButton.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff; -fx-background-color: #6366f1; -fx-background-radius: 8; -fx-padding: 10 40; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());

        contentBox.getChildren().addAll(summaryBox, gapBox, suggestionBox);
        root.getChildren().addAll(titleLabel, contentBox, closeButton);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(scrollPane, 550, 600);
        scene.getStylesheets().add("data:text/css,");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    static AnalysisResult buildAnalysis(TAJob job, TAApplication applicant) {
        List<String> applicantSkillList = applicant == null
            ? new ArrayList<>()
            : SkillUtils.parseSkills(applicant.getSkill());
        List<String> requiredSkillList = getRequiredSkillList(job);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String required : requiredSkillList) {
            boolean found = applicantSkillList.stream()
                .anyMatch(skill -> SkillUtils.looselyMatches(required, skill));
            if (found) {
                matchedSkills.add(required);
            } else {
                missingSkills.add(required);
            }
        }

        return new AnalysisResult(matchedSkills, missingSkills, calculateMatchRate(matchedSkills, requiredSkillList));
    }

    private static List<String> getRequiredSkillList(TAJob job) {
        if (job == null) {
            return new ArrayList<>();
        }

        List<String> requiredSkills = SkillUtils.normalizeSkills(job.getRequiredSkills());
        if (!requiredSkills.isEmpty()) {
            return requiredSkills;
        }

        return SkillUtils.parseSkills(job.getRequirements());
    }

    private static int calculateMatchRate(List<String> matched, List<String> required) {
        if (required.isEmpty()) {
            return 100;
        }
        return (int) Math.round((matched.size() * 100.0) / required.size());
    }

    private static String getSuggestion(String skill) {
        String normalizedSkill = SkillUtils.normalizeForCompare(skill);
        for (Map.Entry<String, String> entry : SKILL_SUGGESTIONS.entrySet()) {
            String normalizedKey = SkillUtils.normalizeForCompare(entry.getKey());
            if (normalizedSkill.contains(normalizedKey) || normalizedKey.contains(normalizedSkill)) {
                return entry.getValue();
            }
        }
        return "Consider learning " + skill + " through online courses (Coursera, Udemy), practice projects, and relevant documentation.";
    }

    static final class AnalysisResult {
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final int matchRate;

        AnalysisResult(List<String> matchedSkills, List<String> missingSkills, int matchRate) {
            this.matchedSkills = new ArrayList<>(matchedSkills);
            this.missingSkills = new ArrayList<>(missingSkills);
            this.matchRate = matchRate;
        }

        public List<String> getMatchedSkills() {
            return new ArrayList<>(matchedSkills);
        }

        public List<String> getMissingSkills() {
            return new ArrayList<>(missingSkills);
        }

        public int getMatchRate() {
            return matchRate;
        }
    }
}
