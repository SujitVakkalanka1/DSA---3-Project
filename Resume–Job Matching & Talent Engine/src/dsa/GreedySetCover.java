package dsa;

import model.Candidate;

import java.util.*;

/**
 * Greedy Set Cover Algorithm
 * Approximates the NP-Hard Minimum Set Cover problem to staff the smallest project team
 * that covers all required project skills.
 *
 * Algorithm Strategy:
 * 1. Initialize Universe U = set of required project skills.
 * 2. Uncovered skills = U.
 * 3. In each greedy iteration:
 *    - Scan available candidates.
 *    - Pick candidate C who covers the maximum number of CURRENTLY UNCOVERED skills.
 *    - Add C to the selected team.
 *    - Remove C's newly covered skills from Uncovered.
 *    - Repeat until all skills are covered or no candidate adds any new coverage.
 *
 * Approximation Ratio: H(|U|) = 1 + 1/2 + ... + 1/|U| <= ln(|U|) + 1.
 * Time Complexity: O(K * N) where K is number of required skills and N is candidate pool size.
 * Course: DSA-3 (25CS2103E) - Team 14
 */
public class GreedySetCover {

    /**
     * Details about an individual candidate's marginal contribution when selected.
     */
    public static class StepSelection {
        public int stepNumber;
        public Candidate candidate;
        public List<String> newlyCoveredSkills;
        public String rationale;

        public StepSelection(int step, Candidate candidate, List<String> newlyCovered, String rationale) {
            this.stepNumber = step;
            this.candidate = candidate;
            this.newlyCoveredSkills = newlyCovered;
            this.rationale = rationale;
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"stepNumber\":").append(stepNumber).append(",");
            sb.append("\"candidate\":").append(candidate.toJson()).append(",");
            sb.append("\"rationale\":\"").append(escape(rationale)).append("\",");
            sb.append("\"newlyCoveredSkills\":[");
            for (int i = 0; i < newlyCoveredSkills.size(); i++) {
                sb.append("\"").append(escape(newlyCoveredSkills.get(i))).append("\"");
                if (i < newlyCoveredSkills.size() - 1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");
            return sb.toString();
        }

        private String escape(String s) {
            return s == null ? "" : s.replace("\"", "\\\"");
        }
    }

    /**
     * Result of the Greedy Set Cover optimization.
     */
    public static class TeamOptimizationResult {
        public List<String> requiredSkills;
        public List<StepSelection> selectedSteps;
        public List<Candidate> selectedTeam;
        public List<String> coveredSkills;
        public List<String> uncoveredSkills;
        public double coveragePercentage;

        public TeamOptimizationResult() {
            this.requiredSkills = new ArrayList<>();
            this.selectedSteps = new ArrayList<>();
            this.selectedTeam = new ArrayList<>();
            this.coveredSkills = new ArrayList<>();
            this.uncoveredSkills = new ArrayList<>();
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"coveragePercentage\":").append(Math.round(coveragePercentage * 10.0) / 10.0).append(",");
            sb.append("\"teamSize\":").append(selectedTeam.size()).append(",");

            // Required skills
            sb.append("\"requiredSkills\":[");
            for (int i = 0; i < requiredSkills.size(); i++) {
                sb.append("\"").append(escape(requiredSkills.get(i))).append("\"");
                if (i < requiredSkills.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Covered skills
            sb.append("\"coveredSkills\":[");
            for (int i = 0; i < coveredSkills.size(); i++) {
                sb.append("\"").append(escape(coveredSkills.get(i))).append("\"");
                if (i < coveredSkills.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Uncovered skills
            sb.append("\"uncoveredSkills\":[");
            for (int i = 0; i < uncoveredSkills.size(); i++) {
                sb.append("\"").append(escape(uncoveredSkills.get(i))).append("\"");
                if (i < uncoveredSkills.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Selected steps (both "steps" and "selectedSteps" for compatibility)
            sb.append("\"steps\":[");
            for (int i = 0; i < selectedSteps.size(); i++) {
                sb.append(selectedSteps.get(i).toJson());
                if (i < selectedSteps.size() - 1) sb.append(",");
            }
            sb.append("],");
            sb.append("\"selectedSteps\":[");
            for (int i = 0; i < selectedSteps.size(); i++) {
                sb.append(selectedSteps.get(i).toJson());
                if (i < selectedSteps.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Selected team members
            sb.append("\"selectedTeam\":[");
            for (int i = 0; i < selectedTeam.size(); i++) {
                sb.append(selectedTeam.get(i).toJson());
                if (i < selectedTeam.size() - 1) sb.append(",");
            }
            sb.append("]");

            sb.append("}");
            return sb.toString();
        }

        private String escape(String s) {
            return s == null ? "" : s.replace("\"", "\\\"");
        }
    }

    /**
     * Executes Greedy Set Cover algorithm on a candidate pool for a target skill universe.
     */
    public static TeamOptimizationResult optimizeTeam(List<Candidate> candidatePool, List<String> targetSkills) {
        TeamOptimizationResult result = new TeamOptimizationResult();

        if (targetSkills == null || targetSkills.isEmpty() || candidatePool == null || candidatePool.isEmpty()) {
            return result;
        }

        // Universe of required skills
        Set<String> uncovered = new LinkedHashSet<>();
        for (String skill : targetSkills) {
            String trimmed = skill.trim();
            if (!trimmed.isEmpty()) uncovered.add(trimmed);
        }
        result.requiredSkills = new ArrayList<>(uncovered);

        Set<String> remainingPoolCandidateIds = new HashSet<>();
        Map<String, Candidate> candidateMap = new HashMap<>();
        for (Candidate c : candidatePool) {
            remainingPoolCandidateIds.add(c.getId());
            candidateMap.put(c.getId(), c);
        }

        Set<String> allCovered = new LinkedHashSet<>();
        int stepCount = 1;

        // Greedy iterative selection loop
        while (!uncovered.isEmpty()) {
            Candidate bestCandidate = null;
            List<String> bestNewlyCovered = new ArrayList<>();
            int maxContribution = 0;

            for (String cid : remainingPoolCandidateIds) {
                Candidate cand = candidateMap.get(cid);
                List<String> currentCandidateSkills = cand.getSkills() != null ? cand.getSkills() : Collections.emptyList();

                List<String> newlyCovered = new ArrayList<>();
                for (String candSkill : currentCandidateSkills) {
                    for (String uncSkill : uncovered) {
                        if (uncSkill.equalsIgnoreCase(candSkill.trim())) {
                            newlyCovered.add(uncSkill);
                        }
                    }
                }

                // Greedy choice: candidate covering the maximum newly uncovered skills
                if (newlyCovered.size() > maxContribution) {
                    maxContribution = newlyCovered.size();
                    bestCandidate = cand;
                    bestNewlyCovered = newlyCovered;
                }
            }

            // If no remaining candidate can cover any more skills, break early
            if (bestCandidate == null || maxContribution == 0) {
                break;
            }

            // Remove selected candidate from available pool
            remainingPoolCandidateIds.remove(bestCandidate.getId());

            // Remove newly covered skills from uncovered set
            for (String s : bestNewlyCovered) {
                uncovered.remove(s);
                allCovered.add(s);
            }

            String rationale = String.format(
                "Selected %s because they add %d new required skill(s): %s.",
                bestCandidate.getName(), bestNewlyCovered.size(), String.join(", ", bestNewlyCovered)
            );

            result.selectedSteps.add(new StepSelection(stepCount++, bestCandidate, bestNewlyCovered, rationale));
            result.selectedTeam.add(bestCandidate);
        }

        result.coveredSkills = new ArrayList<>(allCovered);
        result.uncoveredSkills = new ArrayList<>(uncovered);

        int totalRequired = result.requiredSkills.size();
        result.coveragePercentage = totalRequired > 0 ? ((double) result.coveredSkills.size() / totalRequired) * 100.0 : 0.0;

        return result;
    }
}
