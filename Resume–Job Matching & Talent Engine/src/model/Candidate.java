package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Candidate Model
 * Represents a job applicant profile containing normalized and raw skill lists,
 * experience in years, and availability status.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class Candidate {
    private String id;
    private String name;
    private List<String> rawSkills;
    private List<String> skills; // Normalized canonical skills
    private int experienceYears;
    private String availability;
    private boolean isSample;

    public Candidate() {
        this.rawSkills = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.isSample = false;
    }

    public Candidate(String id, String name, List<String> rawSkills, List<String> skills,
                     int experienceYears, String availability, boolean isSample) {
        this.id = id;
        this.name = name;
        this.rawSkills = (rawSkills != null) ? new ArrayList<>(rawSkills) : new ArrayList<>();
        this.skills = (skills != null) ? new ArrayList<>(skills) : new ArrayList<>();
        this.experienceYears = experienceYears;
        this.availability = availability != null ? availability : "Immediate";
        this.isSample = isSample;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getRawSkills() { return rawSkills; }
    public void setRawSkills(List<String> rawSkills) { this.rawSkills = rawSkills; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public boolean isSample() { return isSample; }
    public void setSample(boolean sample) { isSample = sample; }

    /**
     * Converts Candidate to a clean JSON string representation.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(id)).append("\",");
        sb.append("\"name\":\"").append(escapeJson(name)).append("\",");
        sb.append("\"experienceYears\":").append(experienceYears).append(",");
        sb.append("\"availability\":\"").append(escapeJson(availability)).append("\",");
        sb.append("\"isSample\":").append(isSample).append(",");

        sb.append("\"rawSkills\":[");
        for (int i = 0; i < rawSkills.size(); i++) {
            sb.append("\"").append(escapeJson(rawSkills.get(i))).append("\"");
            if (i < rawSkills.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"skills\":[");
        for (int i = 0; i < skills.size(); i++) {
            sb.append("\"").append(escapeJson(skills.get(i))).append("\"");
            if (i < skills.size() - 1) sb.append(",");
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
