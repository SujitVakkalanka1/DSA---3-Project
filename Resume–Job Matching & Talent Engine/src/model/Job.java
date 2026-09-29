package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Job Model
 * Represents a job posting with required skill requirements, minimum experience,
 * department, and description.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class Job {
    private String id;
    private String title;
    private String department;
    private List<String> requiredSkills;
    private int minExperienceYears;
    private String description;
    private boolean isSample;

    public Job() {
        this.requiredSkills = new ArrayList<>();
        this.isSample = false;
    }

    public Job(String id, String title, String department, List<String> requiredSkills,
               int minExperienceYears, String description, boolean isSample) {
        this.id = id;
        this.title = title;
        this.department = (department != null) ? department : "General";
        this.requiredSkills = (requiredSkills != null) ? new ArrayList<>(requiredSkills) : new ArrayList<>();
        this.minExperienceYears = minExperienceYears;
        this.description = (description != null) ? description : "";
        this.isSample = isSample;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public int getMinExperienceYears() { return minExperienceYears; }
    public void setMinExperienceYears(int minExperienceYears) { this.minExperienceYears = minExperienceYears; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSample() { return isSample; }
    public void setSample(boolean sample) { isSample = sample; }

    /**
     * Converts Job to a clean JSON string representation.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(id)).append("\",");
        sb.append("\"title\":\"").append(escapeJson(title)).append("\",");
        sb.append("\"department\":\"").append(escapeJson(department)).append("\",");
        sb.append("\"minExperienceYears\":").append(minExperienceYears).append(",");
        sb.append("\"description\":\"").append(escapeJson(description)).append("\",");
        sb.append("\"isSample\":").append(isSample).append(",");

        sb.append("\"requiredSkills\":[");
        for (int i = 0; i < requiredSkills.size(); i++) {
            sb.append("\"").append(escapeJson(requiredSkills.get(i))).append("\"");
            if (i < requiredSkills.size() - 1) sb.append(",");
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
