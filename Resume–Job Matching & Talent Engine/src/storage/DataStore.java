package storage;

import dsa.LevenshteinDistance;
import dsa.SkillIndexer;
import model.Candidate;
import model.Job;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * DataStore handles in-memory candidate and job state with automated sample data seeding
 * and local JSON file persistence.
 *
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class DataStore {

    private static final String DATA_FILE = "data/talent_data.json";

    private final List<Candidate> candidates;
    private final List<Job> jobs;
    private final SkillIndexer skillIndexer;
    private int candidateCounter = 100;
    private int jobCounter = 100;

    public DataStore(SkillIndexer skillIndexer) {
        this.candidates = new ArrayList<>();
        this.jobs = new ArrayList<>();
        this.skillIndexer = skillIndexer;
        loadDataOrSeed();
    }

    public synchronized List<Candidate> getAllCandidates() {
        return new ArrayList<>(candidates);
    }

    public synchronized List<Job> getAllJobs() {
        return new ArrayList<>(jobs);
    }

    public synchronized Candidate getCandidateById(String id) {
        if (id == null) return null;
        for (Candidate c : candidates) {
            if (c.getId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }

    public synchronized Job getJobById(String id) {
        if (id == null) return null;
        for (Job j : jobs) {
            if (j.getId().equalsIgnoreCase(id.trim())) return j;
        }
        return null;
    }

    public synchronized Candidate addCandidate(String name, List<String> rawSkills, int experienceYears, String availability) {
        String id = "C-" + (++candidateCounter);
        List<String> normalized = LevenshteinDistance.normalizeList(rawSkills);
        Candidate c = new Candidate(id, name, rawSkills, normalized, experienceYears, availability, false);
        candidates.add(c);
        skillIndexer.indexCandidate(c);
        saveToFile();
        return c;
    }

    public synchronized Candidate updateCandidate(String id, String name, List<String> rawSkills, int experienceYears, String availability) {
        Candidate existing = getCandidateById(id);
        if (existing == null) return null;

        skillIndexer.removeCandidate(existing.getId());
        existing.setName(name);
        existing.setRawSkills(rawSkills);
        List<String> normalized = LevenshteinDistance.normalizeList(rawSkills);
        existing.setSkills(normalized);
        existing.setExperienceYears(experienceYears);
        existing.setAvailability(availability);

        skillIndexer.indexCandidate(existing);
        saveToFile();
        return existing;
    }

    public synchronized boolean deleteCandidate(String id) {
        Candidate existing = getCandidateById(id);
        if (existing != null) {
            candidates.remove(existing);
            skillIndexer.removeCandidate(id);
            saveToFile();
            return true;
        }
        return false;
    }

    public synchronized Job addJob(String title, String department, List<String> requiredSkills, int minExperienceYears, String description) {
        String id = "J-" + (++jobCounter);
        List<String> normalized = LevenshteinDistance.normalizeList(requiredSkills);
        Job j = new Job(id, title, department, normalized, minExperienceYears, description, false);
        jobs.add(j);
        skillIndexer.indexJob(j);
        saveToFile();
        return j;
    }

    public synchronized Job updateJob(String id, String title, String department, List<String> requiredSkills, int minExperienceYears, String description) {
        Job existing = getJobById(id);
        if (existing == null) return null;

        skillIndexer.removeJob(existing.getId());
        existing.setTitle(title);
        existing.setDepartment(department);
        List<String> normalized = LevenshteinDistance.normalizeList(requiredSkills);
        existing.setRequiredSkills(normalized);
        existing.setMinExperienceYears(minExperienceYears);
        existing.setDescription(description);

        skillIndexer.indexJob(existing);
        saveToFile();
        return existing;
    }

    public synchronized boolean deleteJob(String id) {
        Job existing = getJobById(id);
        if (existing != null) {
            jobs.remove(existing);
            skillIndexer.removeJob(id);
            saveToFile();
            return true;
        }
        return false;
    }

    /**
     * Resets candidate and job collections to realistic sample seed data.
     */
    public synchronized void resetToSampleData() {
        candidates.clear();
        jobs.clear();
        seedDefaultData();
        skillIndexer.rebuildIndex(candidates, jobs);
        saveToFile();
    }

    private void seedDefaultData() {
        candidateCounter = 100;
        jobCounter = 100;

        // Realistic candidate profiles demonstrating various technical specializations
        createSeedCandidate("C-101", "Aarav Shah", Arrays.asList("Java", "DSA", "SQL", "Git"), 2, "Immediate");
        createSeedCandidate("C-102", "Maya Patel", Arrays.asList("HTML", "CSS", "Javascript", "React"), 3, "Immediate");
        createSeedCandidate("C-103", "Rahul Kumar", Arrays.asList("Java", "SQL", "Git", "Docker"), 2, "2 Weeks");
        createSeedCandidate("C-104", "Ananya Sharma", Arrays.asList("Pyhton", "SQL", "Statistics", "Data Analysis"), 3, "Immediate");
        createSeedCandidate("C-105", "Vikram Singh", Arrays.asList("Python", "Docker", "Kubernetes", "AWS"), 4, "1 Month");
        createSeedCandidate("C-106", "Priya Nair", Arrays.asList("Javascript", "Typescript", "React", "Nodejs"), 3, "Immediate");
        createSeedCandidate("C-107", "Rohan Mehta", Arrays.asList("C++", "DSA", "Algorithms", "Linux"), 2, "Immediate");
        createSeedCandidate("C-108", "Sneha Verma", Arrays.asList("Java", "Spring Boot", "Postgres", "REST API"), 3, "2 Weeks");

        // Realistic job postings
        createSeedJob("J-101", "Java Backend Developer", "Backend Engineering",
            Arrays.asList("Java", "DSA", "SQL", "Git"), 2,
            "Build scalable enterprise backend services and database access pipelines.");

        createSeedJob("J-102", "Frontend Engineer", "UI/UX Engineering",
            Arrays.asList("HTML", "CSS", "JavaScript", "React"), 2,
            "Develop modern, responsive web interfaces with strong UX fundamentals.");

        createSeedJob("J-103", "Data Analyst", "Data & Analytics",
            Arrays.asList("Python", "SQL", "Statistics", "Data Analysis"), 2,
            "Analyze talent and performance metrics, design queries and statistical reports.");

        createSeedJob("J-104", "Cloud DevOps Engineer", "Infrastructure",
            Arrays.asList("Docker", "Kubernetes", "AWS", "Linux"), 3,
            "Manage deployment pipelines, container orchestration, and cloud infrastructure.");

        createSeedJob("J-105", "Fullstack Developer", "Core Engineering",
            Arrays.asList("React", "Node.js", "TypeScript", "PostgreSQL"), 3,
            "End-to-end feature delivery across frontend and service layers.");
            
        createSeedJob("J-106", "Mobile App Developer", "Mobile Engineering",
            Arrays.asList("Swift", "Kotlin", "React Native", "Git"), 2,
            "Design and build advanced applications for iOS and Android platforms.");

        createSeedJob("J-107", "Machine Learning Engineer", "Data & Analytics",
            Arrays.asList("Python", "TensorFlow", "PyTorch", "SQL"), 4,
            "Design and implement machine learning models and AI systems.");

        createSeedJob("J-108", "Security Engineer", "Security",
            Arrays.asList("Python", "Linux", "Networking", "Cryptography"), 3,
            "Ensure infrastructure and application security through threat modeling and penetration testing.");

        createSeedJob("J-109", "QA Automation Engineer", "Quality Assurance",
            Arrays.asList("Selenium", "Java", "Cypress", "Jira"), 2,
            "Build automated test suites and ensure high software quality standards.");

        createSeedJob("J-110", "Site Reliability Engineer (SRE)", "Infrastructure",
            Arrays.asList("Linux", "Kubernetes", "Terraform", "Go"), 4,
            "Maintain highly available, scalable, and reliable production systems.");

        createSeedJob("J-111", "Game Developer", "Game Design",
            Arrays.asList("C++", "C#", "Unity", "Unreal Engine"), 2,
            "Develop robust game logic and engines for multi-platform deployment.");

        createSeedJob("J-112", "Blockchain Developer", "Web3 Engineering",
            Arrays.asList("Solidity", "Web3.js", "Rust", "Cryptography"), 3,
            "Design and develop decentralized applications and smart contracts.");

        createSeedJob("J-113", "Embedded Systems Engineer", "Hardware & IoT",
            Arrays.asList("C", "C++", "Microcontrollers", "RTOS"), 3,
            "Write low-level code for microcontrollers and IoT devices.");

        createSeedJob("J-114", "Systems Engineer", "Core Engineering",
            Arrays.asList("C", "C++", "Rust", "Linux"), 4,
            "Develop performant, low-level OS components and networking modules.");

        createSeedJob("J-115", "Database Administrator", "Infrastructure",
            Arrays.asList("SQL", "PostgreSQL", "Oracle", "MongoDB"), 5,
            "Optimize, secure, and maintain enterprise database systems.");

        createSeedJob("J-116", "Cloud Architect", "Infrastructure",
            Arrays.asList("AWS", "Azure", "GCP", "System Architecture"), 8,
            "Design resilient and scalable cloud architectures and strategies.");
    }

    private void createSeedCandidate(String id, String name, List<String> rawSkills, int exp, String avail) {
        List<String> normalized = LevenshteinDistance.normalizeList(rawSkills);
        Candidate c = new Candidate(id, name, rawSkills, normalized, exp, avail, true);
        candidates.add(c);
        int num = Integer.parseInt(id.replace("C-", ""));
        if (num > candidateCounter) candidateCounter = num;
    }

    private void createSeedJob(String id, String title, String dept, List<String> reqSkills, int exp, String desc) {
        List<String> normalized = LevenshteinDistance.normalizeList(reqSkills);
        Job j = new Job(id, title, dept, normalized, exp, desc, true);
        jobs.add(j);
        int num = Integer.parseInt(id.replace("J-", ""));
        if (num > jobCounter) jobCounter = num;
    }

    private void loadDataOrSeed() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            resetToSampleData();
            return;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            parseAndLoadJson(content);
            if (candidates.isEmpty() && jobs.isEmpty()) {
                resetToSampleData();
            } else {
                skillIndexer.rebuildIndex(candidates, jobs);
            }
        } catch (Exception e) {
            System.err.println("Notice: Initializing fresh sample data (" + e.getMessage() + ")");
            resetToSampleData();
        }
    }

    private void parseAndLoadJson(String json) {
        // Simple and robust parser for our own saved JSON structure
        try {
            int candIdx = json.indexOf("\"candidates\":[");
            int jobIdx = json.indexOf("\"jobs\":[");
            if (candIdx != -1 && jobIdx != -1) {
                // Parse candidates
                String candSection = json.substring(candIdx + 13, jobIdx);
                parseCandidateArray(candSection);

                // Parse jobs
                String jobSection = json.substring(jobIdx + 7);
                parseJobArray(jobSection);
            }
        } catch (Exception e) {
            System.err.println("JSON parse fallback: " + e.getMessage());
        }
    }

    private void parseCandidateArray(String section) {
        int start = 0;
        while ((start = section.indexOf("{", start)) != -1) {
            int end = section.indexOf("}", start);
            if (end == -1) break;
            String objStr = section.substring(start, end + 1);

            String id = extractValue(objStr, "id");
            String name = extractValue(objStr, "name");
            String expStr = extractValue(objStr, "experienceYears");
            String avail = extractValue(objStr, "availability");
            String isSampleStr = extractValue(objStr, "isSample");
            List<String> rawSkills = extractStringList(objStr, "rawSkills");
            List<String> skills = extractStringList(objStr, "skills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            boolean isSample = Boolean.parseBoolean(isSampleStr);

            Candidate c = new Candidate(id, name, rawSkills, skills, exp, avail, isSample);
            candidates.add(c);

            try {
                int num = Integer.parseInt(id.replace("C-", ""));
                if (num > candidateCounter) candidateCounter = num;
            } catch (Exception ignored) {}

            start = end + 1;
        }
    }

    private void parseJobArray(String section) {
        int start = 0;
        while ((start = section.indexOf("{", start)) != -1) {
            int end = section.indexOf("}", start);
            if (end == -1) break;
            String objStr = section.substring(start, end + 1);

            String id = extractValue(objStr, "id");
            String title = extractValue(objStr, "title");
            String dept = extractValue(objStr, "department");
            String expStr = extractValue(objStr, "minExperienceYears");
            String desc = extractValue(objStr, "description");
            String isSampleStr = extractValue(objStr, "isSample");
            List<String> reqSkills = extractStringList(objStr, "requiredSkills");

            int exp = expStr.isEmpty() ? 0 : Integer.parseInt(expStr);
            boolean isSample = Boolean.parseBoolean(isSampleStr);

            Job j = new Job(id, title, dept, reqSkills, exp, desc, isSample);
            jobs.add(j);

            try {
                int num = Integer.parseInt(id.replace("J-", ""));
                if (num > jobCounter) jobCounter = num;
            } catch (Exception ignored) {}

            start = end + 1;
        }
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        int valStart = idx + pattern.length();
        while (valStart < json.length() && (json.charAt(valStart) == ' ' || json.charAt(valStart) == '\t')) valStart++;

        if (valStart < json.length() && json.charAt(valStart) == '"') {
            int valEnd = json.indexOf("\"", valStart + 1);
            return (valEnd != -1) ? json.substring(valStart + 1, valEnd) : "";
        } else {
            int valEnd = valStart;
            while (valEnd < json.length() && json.charAt(valEnd) != ',' && json.charAt(valEnd) != '}' && json.charAt(valEnd) != ']') {
                valEnd++;
            }
            return json.substring(valStart, valEnd).trim();
        }
    }

    private List<String> extractStringList(String json, String key) {
        List<String> list = new ArrayList<>();
        String pattern = "\"" + key + "\":[";
        int idx = json.indexOf(pattern);
        if (idx == -1) return list;
        int arrStart = idx + pattern.length();
        int arrEnd = json.indexOf("]", arrStart);
        if (arrEnd == -1) return list;
        String inside = json.substring(arrStart, arrEnd);
        String[] parts = inside.split(",");
        for (String p : parts) {
            String clean = p.trim().replace("\"", "");
            if (!clean.isEmpty()) list.add(clean);
        }
        return list;
    }

    public synchronized void saveToFile() {
        try {
            File dir = new File("data");
            if (!dir.exists()) dir.mkdirs();

            StringBuilder sb = new StringBuilder();
            sb.append("{\n  \"candidates\": [\n");
            for (int i = 0; i < candidates.size(); i++) {
                sb.append("    ").append(candidates.get(i).toJson());
                if (i < candidates.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n  \"jobs\": [\n");
            for (int i = 0; i < jobs.size(); i++) {
                sb.append("    ").append(jobs.get(i).toJson());
                if (i < jobs.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n}");

            Files.write(Paths.get(DATA_FILE), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Failed to persist data: " + e.getMessage());
        }
    }
}
