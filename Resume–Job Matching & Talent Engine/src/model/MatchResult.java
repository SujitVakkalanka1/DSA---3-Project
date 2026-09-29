package model;

import java.util.ArrayList;
import java.util.List;

/**
 * MatchResult Model
 * Contains detailed matching calculations between a Candidate and a Job,
 * including score breakdown, matched skills, missing skills, and explanation.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class MatchResult {
    private Candidate candidate;
    private Job job;
    private double totalScore;        // Overall percentage (0-100)
    private double skillScore;        // Skill component contribution
    private double experienceScore;   // Experience component contribution
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String explanation;

    public MatchResult() {
        this.matchedSkills = new ArrayList<>();
        this.missingSkills = new ArrayList<>();
    }

    public MatchResult(Candidate candidate, Job job, double totalScore, double skillScore,
                       double experienceScore, List<String> matchedSkills,
                       List<String> missingSkills, String explanation) {
        this.candidate = candidate;
        this.job = job;
        this.totalScore = Math.round(totalScore * 10.0) / 10.0;
        this.skillScore = Math.round(skillScore * 10.0) / 10.0;
        this.experienceScore = Math.round(experienceScore * 10.0) / 10.0;
        this.matchedSkills = (matchedSkills != null) ? new ArrayList<>(matchedSkills) : new ArrayList<>();
        this.missingSkills = (missingSkills != null) ? new ArrayList<>(missingSkills) : new ArrayList<>();
        this.explanation = explanation;
    }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public double getSkillScore() { return skillScore; }
    public void setSkillScore(double skillScore) { this.skillScore = skillScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"candidate\":").append(candidate != null ? candidate.toJson() : "null").append(",");
        sb.append("\"job\":").append(job != null ? job.toJson() : "null").append(",");
        sb.append("\"candidateName\":\"").append(candidate != null ? escapeJson(candidate.getName()) : "").append("\",");
        sb.append("\"jobTitle\":\"").append(job != null ? escapeJson(job.getTitle()) : "").append("\",");
        sb.append("\"totalScore\":").append(totalScore).append(",");
        sb.append("\"skillScore\":").append(skillScore).append(",");
        sb.append("\"experienceScore\":").append(experienceScore).append(",");
        sb.append("\"explanation\":\"").append(escapeJson(explanation)).append("\",");

        sb.append("\"matchedSkills\":[");
        for (int i = 0; i < matchedSkills.size(); i++) {
            sb.append("\"").append(escapeJson(matchedSkills.get(i))).append("\"");
            if (i < matchedSkills.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"missingSkills\":[");
        for (int i = 0; i < missingSkills.size(); i++) {
            sb.append("\"").append(escapeJson(missingSkills.get(i))).append("\"");
            if (i < missingSkills.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
