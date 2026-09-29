package dsa;

import model.Candidate;
import model.Job;
import model.MatchResult;

import java.util.*;

/**
 * Weighted Matching Algorithm
 * Evaluates candidate-to-job compatibility transparently without black-box models.
 *
 * Scoring Formula:
 * 1. Skill Overlap (80% weight):
 *    - Matched Skills = CandidateSkills ∩ JobRequiredSkills
 *    - Skill Match Ratio = |MatchedSkills| / |JobRequiredSkills|
 *    - Skill Score = Skill Match Ratio * 80.0
 *
 * 2. Experience Compatibility (20% weight):
 *    - If CandidateExperience >= JobMinExperience: Experience Score = 20.0
 *    - Else: Experience Score = (CandidateExperience / JobMinExperience) * 20.0
 *
 * Total Score = Skill Score + Experience Score (out of 100%).
 *
 * Time Complexity: O(S_c + S_j) using HashSet intersection, where S_c and S_j are skill counts.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class WeightedMatcher {

    public static final double SKILL_WEIGHT = 0.80;
    public static final double EXP_WEIGHT = 0.20;

    /**
     * Computes the transparent match result between a candidate and a job posting.
     */
    public static MatchResult computeMatch(Candidate candidate, Job job) {
        if (candidate == null || job == null) {
            return new MatchResult(candidate, job, 0.0, 0.0, 0.0,
                Collections.emptyList(), Collections.emptyList(), "Invalid candidate or job data.");
        }

        List<String> requiredSkills = job.getRequiredSkills() != null ? job.getRequiredSkills() : Collections.emptyList();
        List<String> candidateSkills = candidate.getSkills() != null ? candidate.getSkills() : Collections.emptyList();

        // Use HashSet for O(1) membership checks and intersection
        Set<String> candSkillSet = new HashSet<>();
        for (String s : candidateSkills) {
            candSkillSet.add(s.trim().toLowerCase());
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String req : requiredSkills) {
            if (candSkillSet.contains(req.trim().toLowerCase())) {
                matched.add(req);
            } else {
                missing.add(req);
            }
        }

        // 1. Skill Score Calculation
        double skillRatio = requiredSkills.isEmpty() ? 1.0 : ((double) matched.size() / requiredSkills.size());
        double skillScoreComponent = skillRatio * 100.0 * SKILL_WEIGHT;

        // 2. Experience Score Calculation
        int candExp = candidate.getExperienceYears();
        int reqExp = job.getMinExperienceYears();
        double expRatio = 1.0;
        if (reqExp > 0) {
            expRatio = Math.min(1.0, (double) candExp / reqExp);
        }
        double expScoreComponent = expRatio * 100.0 * EXP_WEIGHT;

        // 3. Combined Total Score
        double totalScore = skillScoreComponent + expScoreComponent;

        // Formulate deterministic explanation
        String explanation = String.format(
            "%d of %d required skills matched (%.0f%% skill weight) + %d/%d years experience matched (%.0f%% exp weight).",
            matched.size(), requiredSkills.size(), skillScoreComponent,
            candExp, reqExp, expScoreComponent
        );

        return new MatchResult(
            candidate, job, totalScore, skillScoreComponent, expScoreComponent,
            matched, missing, explanation
        );
    }
}
