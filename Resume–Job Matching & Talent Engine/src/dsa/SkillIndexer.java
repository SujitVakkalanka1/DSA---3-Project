package dsa;

import model.Candidate;
import model.Job;

import java.util.*;

/**
 * Skill Inverted Index using HashMap and HashSet
 * Maps each canonical skill to the set of candidate IDs and job IDs that require/have it.
 *
 * Provides O(1) average time lookup to answer queries such as:
 * - "Which candidates have Java?"
 * - "Which jobs require SQL?"
 *
 * Time Complexity:
 * - Skill Lookup: O(1) average case via HashMap hashing.
 * - Indexing: O(K) where K is the number of skills in the candidate/job.
 * Space Complexity: O(S * N) where S is number of distinct skills and N is average postings per skill.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class SkillIndexer {

    // Inverted Index: Skill Name -> Set of Candidate IDs
    private final Map<String, Set<String>> skillToCandidateIds;

    // Inverted Index: Skill Name -> Set of Job IDs
    private final Map<String, Set<String>> skillToJobIds;

    public SkillIndexer() {
        this.skillToCandidateIds = new HashMap<>();
        this.skillToJobIds = new HashMap<>();
    }

    /**
     * Clear and rebuild the entire index from scratch given the full candidate and job lists.
     */
    public synchronized void rebuildIndex(List<Candidate> candidates, List<Job> jobs) {
        skillToCandidateIds.clear();
        skillToJobIds.clear();

        if (candidates != null) {
            for (Candidate c : candidates) {
                indexCandidate(c);
            }
        }

        if (jobs != null) {
            for (Job j : jobs) {
                indexJob(j);
            }
        }
    }

    /**
     * Indexes a candidate's normalized skills into the HashMap inverted index.
     */
    public synchronized void indexCandidate(Candidate candidate) {
        if (candidate == null || candidate.getSkills() == null) return;
        String cid = candidate.getId();

        for (String skill : candidate.getSkills()) {
            String canonical = skill.trim();
            if (canonical.isEmpty()) continue;

            skillToCandidateIds.computeIfAbsent(canonical, k -> new HashSet<>()).add(cid);
        }
    }

    /**
     * Removes a candidate ID from all skill postings.
     */
    public synchronized void removeCandidate(String candidateId) {
        if (candidateId == null) return;
        for (Set<String> cids : skillToCandidateIds.values()) {
            cids.remove(candidateId);
        }
    }

    /**
     * Indexes a job's required skills into the HashMap inverted index.
     */
    public synchronized void indexJob(Job job) {
        if (job == null || job.getRequiredSkills() == null) return;
        String jid = job.getId();

        for (String skill : job.getRequiredSkills()) {
            String canonical = skill.trim();
            if (canonical.isEmpty()) continue;

            skillToJobIds.computeIfAbsent(canonical, k -> new HashSet<>()).add(jid);
        }
    }

    /**
     * Removes a job ID from all skill postings.
     */
    public synchronized void removeJob(String jobId) {
        if (jobId == null) return;
        for (Set<String> jids : skillToJobIds.values()) {
            jids.remove(jobId);
        }
    }

    /**
     * Returns candidate IDs associated with a specific skill in O(1) average time.
     */
    public synchronized Set<String> getCandidateIdsBySkill(String skill) {
        if (skill == null) return Collections.emptySet();
        return skillToCandidateIds.getOrDefault(skill.trim(), Collections.emptySet());
    }

    /**
     * Returns job IDs requiring a specific skill in O(1) average time.
     */
    public synchronized Set<String> getJobIdsBySkill(String skill) {
        if (skill == null) return Collections.emptySet();
        return skillToJobIds.getOrDefault(skill.trim(), Collections.emptySet());
    }

    /**
     * Returns a sorted list of all unique skills currently present in the index.
     */
    public synchronized List<String> getAllIndexedSkills() {
        Set<String> allSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allSkills.addAll(skillToCandidateIds.keySet());
        allSkills.addAll(skillToJobIds.keySet());
        return new ArrayList<>(allSkills);
    }

    /**
     * Returns frequency map of candidate skills for analytics.
     */
    public synchronized Map<String, Integer> getSkillFrequencyMap() {
        Map<String, Integer> freq = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : skillToCandidateIds.entrySet()) {
            freq.put(entry.getKey(), entry.getValue().size());
        }
        return freq;
    }

    /**
     * Serializes the current inverted index state to JSON for frontend inspection.
     */
    public synchronized String toJson() {
        List<String> allSkills = getAllIndexedSkills();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < allSkills.size(); i++) {
            String skill = allSkills.get(i);
            Set<String> cids = skillToCandidateIds.getOrDefault(skill, Collections.emptySet());
            Set<String> jids = skillToJobIds.getOrDefault(skill, Collections.emptySet());

            sb.append("{");
            sb.append("\"skill\":\"").append(skill).append("\",");

            sb.append("\"candidateIds\":[");
            int ci = 0;
            for (String cid : cids) {
                sb.append("\"").append(cid).append("\"");
                if (++ci < cids.size()) sb.append(",");
            }
            sb.append("],");

            sb.append("\"jobIds\":[");
            int ji = 0;
            for (String jid : jids) {
                sb.append("\"").append(jid).append("\"");
                if (++ji < jids.size()) sb.append(",");
            }
            sb.append("]");

            sb.append("}");
            if (i < allSkills.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
