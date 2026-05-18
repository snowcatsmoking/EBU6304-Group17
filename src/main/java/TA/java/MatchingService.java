package TA.java;

import TA.java.utils.TAApplicationUtils;
import ai.service.AIService;
import ai.model.AIResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import data.DataConfig;
import data.JobDataManager;

import java.io.File;
import java.io.IOException;

public class MatchingService {
    private static final String MATCHING_DIR = DataConfig.MATCHING_DIR;
    private final ObjectMapper objectMapper;
    private final AIService aiService;

    public MatchingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(MATCHING_DIR);
        this.aiService = new AIService();
    }

    /** Get cached result or null */
    public MatchingResult getCachedResult(String studentId, String jobId) {
        File file = getResultFile(studentId, jobId);
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, MatchingResult.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /** Compute matching score via AI, with caching */
    public MatchingResult computeMatch(String studentId, String jobId) throws Exception {
        return computeMatch(studentId, jobId, false);
    }

    /** Compute matching score via AI, with optional cache bypass */
    public MatchingResult computeMatch(String studentId, String jobId, boolean forceRecompute) throws Exception {
        if (!forceRecompute) {
            MatchingResult cached = getCachedResult(studentId, jobId);
            if (cached != null) {
                return cached;
            }
        }

        TAApplication profile = TAApplicationUtils.loadUser(studentId);
        TAJob job = new JobDataManager().getJobById(jobId);
        if (job == null) {
            throw new Exception("Position not found");
        }

        String prompt = buildMatchPrompt(profile, job);
        AIResponse aiResp = aiService.sendMessage(prompt);

        MatchingResult result = parseResponse(aiResp.answer, studentId, jobId);
        saveResult(result);
        return result;
    }

    private String buildMatchPrompt(TAApplication profile, TAJob job) {
        return String.format(
            "You are a skill-matching evaluator for a TA recruitment system. Compare the applicant against the job and return ONLY a JSON object (no markdown, no code block).\n\n" +
            "Applicant:\n" +
            "- Skills: %s\n" +
            "- Major: %s\n" +
            "- Available: %s\n\n" +
            "Position:\n" +
            "- Title: %s\n" +
            "- Course: %s\n" +
            "- Requirements: %s\n\n" +
            "Evaluate match percentage (0-100). Return JSON with these fields:\n" +
            "- \"percentage\": number 0-100\n" +
            "- \"reason\": one brief sentence summarizing the match\n" +
            "- \"analysis\": detailed analysis of skill overlap, major relevance, and gaps (2-4 sentences)\n" +
            "- \"improvements\": specific actionable suggestions to improve the match (2-4 sentences, like skills to learn or experience to gain)\n" +
            "Return exactly: {\"percentage\": <number>, \"reason\": \"...\", \"analysis\": \"...\", \"improvements\": \"...\"}",
            profile.getSkill(),
            profile.getMajor(),
            profile.getAvailableTime(),
            job.getPositionName(),
            job.getCourseName(),
            job.getRequirements()
        );
    }

    private MatchingResult parseResponse(String answer, String studentId, String jobId) throws Exception {
        if (answer == null || answer.trim().isEmpty()) {
            return new MatchingResult(studentId, jobId, 0, "AI service returned empty response");
        }

        // Extract JSON from possible markdown code blocks
        String json = answer.trim();
        if (json.startsWith("```")) {
            int start = json.indexOf("{");
            int end = json.lastIndexOf("}");
            if (start >= 0 && end > start) {
                json = json.substring(start, end + 1);
            }
        }

        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            int percentage = clamp(root.path("percentage").asInt(0), 0, 100);
            String reason = root.path("reason").asText("No reason provided");
            String analysis = root.path("analysis").asText("");
            String improvements = root.path("improvements").asText("");
            return new MatchingResult(studentId, jobId, percentage, reason, analysis, improvements);
        } catch (Exception e) {
            // Fallback: try to extract a number from the response
            String numOnly = answer.replaceAll("[^0-9]", " ").trim();
            String[] parts = numOnly.split("\\s+");
            for (String part : parts) {
                try {
                    int val = Integer.parseInt(part);
                    if (val >= 0 && val <= 100) {
                        return new MatchingResult(studentId, jobId, val, answer);
                    }
                } catch (NumberFormatException ignored) {}
            }
            return new MatchingResult(studentId, jobId, 0, "Could not parse AI response");
        }
    }

    private void saveResult(MatchingResult result) {
        try {
            File file = getResultFile(result.getStudentId(), result.getJobId());
            objectMapper.writeValue(file, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getResultFile(String studentId, String jobId) {
        return new File(MATCHING_DIR + studentId + "_" + jobId + ".json");
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
