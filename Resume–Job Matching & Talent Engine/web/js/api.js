/**
 * Talent Engine API Client
 * Native fetch client calling Java Standard Library HttpServer REST endpoints.
 * Course: DSA-3 (25CS2103E) - Talent Engine
 */

const API = {
  baseUrl: '',

  async request(endpoint, options = {}) {
    const url = this.baseUrl + endpoint;
    const config = {
      headers: {
        'Content-Type': 'application/json',
      },
      ...options,
    };

    try {
      const res = await fetch(url, config);
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.error || `Request failed with status ${res.status}`);
      }
      return await res.json();
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err);
      throw err;
    }
  },

  // Candidates
  getCandidates() {
    return this.request('/api/candidates');
  },

  addCandidate(candidateData) {
    return this.request('/api/candidates', {
      method: 'POST',
      body: JSON.stringify(candidateData),
    });
  },

  updateCandidate(candidateData) {
    return this.request('/api/candidates', {
      method: 'PUT',
      body: JSON.stringify(candidateData),
    });
  },

  deleteCandidate(id) {
    return this.request(`/api/candidates?id=${encodeURIComponent(id)}`, {
      method: 'DELETE',
    });
  },

  // Jobs
  getJobs() {
    return this.request('/api/jobs');
  },

  addJob(jobData) {
    return this.request('/api/jobs', {
      method: 'POST',
      body: JSON.stringify(jobData),
    });
  },

  updateJob(jobData) {
    return this.request('/api/jobs', {
      method: 'PUT',
      body: JSON.stringify(jobData),
    });
  },

  deleteJob(id) {
    return this.request(`/api/jobs?id=${encodeURIComponent(id)}`, {
      method: 'DELETE',
    });
  },

  // Matching & Matrices
  calculateMatch(candidateId, jobId) {
    return this.request('/api/match', {
      method: 'POST',
      body: JSON.stringify({ candidateId, jobId }),
    });
  },

  getMatchMatrix() {
    return this.request('/api/match/matrix');
  },

  // Hungarian Algorithm Assignments (Module 4 weighted 1:1)
  getAssignments() {
    return this.request('/api/assignments');
  },

  // Network Flow Edmonds-Karp Capacity Allocation (Module 4)
  getNetworkFlow() {
    return this.request('/api/network-flow');
  },

  // Team Optimizer - Greedy Set Cover (Module 5 Approximation)
  optimizeTeam(skills) {
    return this.request('/api/optimize-team', {
      method: 'POST',
      body: JSON.stringify({ skills }),
    });
  },

  // Team Optimizer - Exact Bitmask DP (Module 3 Exact)
  optimizeTeamBitmask(skills) {
    return this.request('/api/bitmask-team', {
      method: 'POST',
      body: JSON.stringify({ skills }),
    });
  },

  // String Algorithms Search (Module 2 KMP, Rabin-Karp, Z-search)
  searchString(text, pattern) {
    return this.request('/api/string-search', {
      method: 'POST',
      body: JSON.stringify({ text, pattern }),
    });
  },

  // Analytics & Inverted Index
  getAnalytics() {
    return this.request('/api/analytics');
  },

  getSkillIndex() {
    return this.request('/api/skill-index');
  },

  // Parallel Stream Benchmark (Module 6)
  getParallelBenchmark() {
    return this.request('/api/parallel-benchmark');
  },

  // Skill Normalization (Levenshtein & Damerau)
  normalizeSkill(skill) {
    return this.request('/api/normalize', {
      method: 'POST',
      body: JSON.stringify({ skill }),
    });
  },

  // Reset to sample state
  resetData() {
    return this.request('/api/reset', {
      method: 'POST',
    });
  }
};

// Aliased to window.TalentApi as well
window.API = API;
window.TalentApi = API;
