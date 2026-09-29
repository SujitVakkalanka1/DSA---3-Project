# TALENT ENGINE
## Algorithmic Résumé–Job Matching & Talent-Marketplace Engine

A deterministic candidate-to-role matching and workforce-staffing engine built from scratch using pure Java SE 17 and classical Data Structures & Algorithms. Redesigned with the Cybera visual design language.

---

## 📌 Project Overview

- **Project Title**: TALENT ENGINE — Résumé–Job Matching & Talent-Marketplace Engine
- **Course**: Data Structures and Algorithms - 3 (`25CS2103E`)
- **Academic Year**: 2026–2027
- **Team**: 14 | **Section**: 10
- **Institution**: KL University, Hyderabad - 500090, Telangana, India
- **Submitted By**:
  - **Vakkalanka Sai Bhaskara Sujit** (`2520090085`)
  - **Mahankali Sai Tarun** (`2520090020`)
- **Faculty Guide**: **K. Chandusha**, Asst. Prof., CS&IT

---

## 🎯 Key Design & Architectural Philosophy

1. **Zero External Frameworks (Pure Java Standard Library)**:
   - Built natively with Java SE 17's built-in `com.sun.net.httpserver.HttpServer`.
   - Zero Spring Boot, Node.js, Python, Hibernate, React, or external JAR dependencies.
   - Fast, reliable compilation with standard `javac` and immediate execution.

2. **Full 6-Module DSA-3 Integration**:
   - Every algorithmic paradigm serves a genuine, natural purpose in talent matching.
   - Distinguishes exact solutions (Bitmask DP, Hungarian) from approximations (Greedy Set Cover) and feasibility models (Network Flow).

3. **Deterministic & Explainable Decisions (No Black-Box AI Claims)**:
   - Evaluates fit using transparent mathematical scoring, string alignment, and graph algorithms.
   - Every match includes an audit trail of verified skills, missing gaps, and experience weighting.

4. **Cybera Editorial Visual Design**:
   - Ultra-dark ink canvas (`#030403`), warm cream typography (`#FBFBE0`), signal lime technical accents (`#ECF223`), and subtle emerald forest glow (`#124A15`).
   - Clean, professional typography with zero horizontal scroll and zero generic dashboard styling.

---

## 🧠 Data Structures & Algorithms (Full 6-Module Coverage)

| Module | Algorithm | Java Implementation File | Data Structure / Paradigm | Complexity | Project Purpose |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Module 1** | **Inverted Index & Pipeline** | [`src/dsa/SkillIndexer.java`](src/dsa/SkillIndexer.java) | `HashMap<String, Set<String>>` | $O(1)$ avg lookup | Fast indexing of skills $\to$ candidate and job postings without linear scans. |
| **Module 2** | **KMP & Rabin-Karp** | [`src/dsa/StringAlgorithms.java`](src/dsa/StringAlgorithms.java) | Prefix table $\pi$, Polynomial Rolling Hash, Z-array, Suffix LCP | KMP: $O(N + M)$<br>RK: $O(N + M)$ avg<br>Kasai LCP: $O(N)$ | Searching exact skill phrases and repeated terminology in unstructured résumés. |
| **Module 3** | **Levenshtein, Damerau & Bitmask DP** | [`src/dsa/LevenshteinDistance.java`](src/dsa/LevenshteinDistance.java)<br>[`src/dsa/BitmaskTeamOptimizer.java`](src/dsa/BitmaskTeamOptimizer.java) | 2D DP Table & Bitmask States | Levenshtein: $O(M \times N)$<br>Bitmask DP: $O(2^K \cdot N)$ | Typo/transposition normalization and mathematically exact minimal team staffing. |
| **Module 4** | **Hungarian & Edmonds-Karp Flow** | [`src/dsa/HungarianAlgorithm.java`](src/dsa/HungarianAlgorithm.java)<br>[`src/dsa/NetworkFlowEngine.java`](src/dsa/NetworkFlowEngine.java) | Kuhn-Munkres & BFS Augmenting Paths (Residual Graph) | Hungarian: $O(N^3)$<br>Flow: $O(V \cdot E^2)$ | Weighted 1:1 optimal allocation (Hungarian) vs capacity-constrained quota matching with Min-Cut bottleneck analysis. |
| **Module 5** | **Greedy Set Cover** | [`src/dsa/GreedySetCover.java`](src/dsa/GreedySetCover.java) | Greedy Approximation of NP-Hard problem | $O(\|U\| \times N)$<br>Ratio: $H(\|U\|) \le \ln\|U\| + 1$ | Staffs minimal project team covering required skills in polynomial time. |
| **Module 6** | **Randomized QuickSort & Parallel Streams** | [`src/dsa/RandomizedAlgorithms.java`](src/dsa/RandomizedAlgorithms.java)<br>[`src/dsa/ParallelAnalytics.java`](src/dsa/ParallelAnalytics.java) | Random Pivot Partitioning & Java ForkJoinPool | Exp: $O(N \log N)$<br>Worst: $O(N^2)$<br>Parallel: Workload/CPU dependent | Adversarial-resistant match ranking and multi-core parallel stream analytics reductions. |

---

## 📂 Project Directory Structure

```
Resume–Job Matching & Talent Engine/
├── src/
│   ├── model/
│   │   ├── Candidate.java           # Applicant model (raw & normalized skills, exp, availability)
│   │   ├── Job.java                 # Job role model (required skills, min experience, department)
│   │   └── MatchResult.java         # Score breakdown, matched & missing skill lists
│   ├── dsa/
│   │   ├── SkillIndexer.java        # HashMap/HashSet inverted index (Module 1)
│   │   ├── StringAlgorithms.java    # KMP, Rabin-Karp, Z, Suffix Array & LCP (Module 2)
│   │   ├── LevenshteinDistance.java # 2D DP Levenshtein & Damerau transposition distance (Module 3)
│   │   ├── BitmaskTeamOptimizer.java# Exact Bitmask DP team cover O(2^K · N) (Module 3)
│   │   ├── HungarianAlgorithm.java  # Kuhn-Munkres O(N³) optimal bipartite assignment (Module 4)
│   │   ├── NetworkFlowEngine.java   # Edmonds-Karp Max-Flow & Min-Cut bottleneck analysis (Module 4)
│   │   ├── GreedySetCover.java      # Minimal team staffing with harmonic approximation bound (Module 5)
│   │   ├── RandomizedAlgorithms.java# Randomized QuickSort candidate match ranker (Module 6)
│   │   ├── ParallelAnalytics.java   # Multi-core ForkJoin parallel stream benchmark (Module 6)
│   │   └── WeightedMatcher.java     # Deterministic compatibility evaluation (80/20)
│   ├── storage/
│   │   └── DataStore.java           # In-memory storage, sample data seeding & file persistence
│   ├── server/
│   │   ├── SimpleHttpServer.java    # Built-in Java HttpServer & static file routing
│   │   └── ApiHandler.java          # REST API endpoints for candidate/job CRUD and DSA runs
│   ├── AlgorithmTest.java           # Automated 9-test verification suite across all 6 modules
│   ├── ServerIntegrationTest.java   # End-to-end 26-endpoint integration test suite
│   └── Main.java                    # Entry point starting server on port 8080
├── web/
│   ├── index.html                   # Cybera editorial landing page with live normalizer widget
│   ├── dashboard.html               # Talent Pool Overview, metrics strip, matching activity
│   ├── candidates.html              # Skill Intelligence directory with KMP phrase search
│   ├── jobs.html                    # Role Requirements with candidate pool match counts
│   ├── match.html                   # Candidate–Role Matching with 80/20 breakdown & matrix
│   ├── assignments.html             # Hungarian 1:1 allocation & Network Flow capacity matching
│   ├── team-optimizer.html          # Team Skill Optimizer (Exact Bitmask DP vs Greedy Set Cover)
│   ├── analytics.html               # Talent Pool Analytics with live parallel stream benchmark
│   ├── architecture.html            # System Architecture & Technical Details (full viva guide)
│   ├── css/
│   │   └── style.css                # Cybera-inspired dark theme design system
│   └── js/
│       ├── api.js                   # REST API client
│       └── app.js                   # Common UI utilities, score pills, and toast alerts
├── data/
│   └── talent_data.json             # Local JSON file storage
├── run.bat                          # One-click Windows runner
├── run.sh                           # One-click Linux/Mac runner
├── .gitignore                       # Git ignore file for compiled classes
└── README.md                        # Documentation & viva guide
```

---

## 🚀 How to Run the Project Locally

### Prerequisites
- **Java 17** or higher (JDK 17, 21, or 25 LTS).
- Modern web browser (Chrome, Edge, Firefox).

### Method 1: Using the Windows Runner Script
```cmd
run.bat
```

### Method 2: Using the Shell Script (Linux / macOS)
```bash
chmod +x run.sh
./run.sh
```

### Method 3: Manual Compilation & Execution
From the root folder of the project, run:
```bash
# 1. Compile all Java source files
javac -encoding UTF-8 -d bin src/model/*.java src/dsa/*.java src/storage/*.java src/server/*.java src/*.java

# 2. Run Algorithm Verification Test Suite (9 / 9 tests)
java -cp bin AlgorithmTest

# 3. Run Server Integration Test Suite (26 / 26 endpoints)
java -cp bin ServerIntegrationTest

# 4. Start the application
java -cp bin Main
```

Open your browser and navigate to:
```
http://localhost:8080
```

---

## 🎓 Viva Questions & Answers Reference

### Q1: How does KMP achieve $O(N + M)$ time without backtracking?
> **Answer**: KMP precomputes the Prefix Failure Function $\pi[i]$ in $O(M)$ time, where $\pi[i]$ is the length of the longest proper prefix of pattern $[0..i]$ that is also a suffix. When a mismatch occurs, instead of resetting the text index, the pattern shifts to index $\pi[j-1]$. Characters in the text are never scanned backwards, guaranteeing $O(N + M)$ worst-case time.

### Q2: What is the recurrence relation for Levenshtein and Damerau-Levenshtein?
> **Answer**: 
> $$dp[i][j] = \min(dp[i-1][j] + 1, dp[i][j-1] + 1, dp[i-1][j-1] + \text{cost})$$
> In Damerau-Levenshtein, adjacent transpositions are checked:
> $$\text{if } i > 1, j > 1 \text{ and } s_1[i-1] == s_2[j-2] \text{ and } s_1[i-2] == s_2[j-1]:$$
> $$dp[i][j] = \min(dp[i][j], dp[i-2][j-2] + 1)$$
> This allows single-step typing errors like `"jsavscript"` $\to$ `"javascript"` to be resolved with an edit distance of 1 instead of 2.

### Q3: What is the difference between Hungarian Algorithm and Network Flow?
> **Answer**: 
> - **Hungarian Algorithm (Kuhn-Munkres)** solves the **Weighted 1:1 Bijective Assignment Problem** in $O(N^3)$ polynomial time, minimizing total skill gap.
> - **Network Flow (Edmonds-Karp)** solves **Capacity-Constrained Feasible Matching** where roles can have headcount quotas $> 1$. Furthermore, the **Max-Flow Min-Cut Theorem** isolates hiring bottlenecks via the reachable cut in the residual network.

### Q4: Why compare Exact Bitmask DP with Greedy Set Cover?
> **Answer**: Minimum Set Cover is NP-Hard. Exact Bitmask DP solves the problem optimally in $O(2^K \cdot N)$ time for small universes ($K \le 18$), but becomes intractable for large $K$. Greedy Set Cover iteratively picks candidates with maximum marginal contribution, providing an $O(|U| \cdot N)$ polynomial approximation with a provable harmonic bound $H(|U|) \le \ln |U| + 1$.

### Q5: Why choose Randomized QuickSort over Deterministic QuickSort?
> **Answer**: Deterministic QuickSort degrades to $O(N^2)$ on already-sorted or reverse-sorted inputs. Selecting a random pivot uniformly from $[low, high]$ makes consistently bad partitions extraordinarily unlikely ($\sim 2^N / N! \to 0$), guaranteeing expected $O(N \log N)$ time and $O(\log N)$ auxiliary space.

### Q6: How do Java Parallel Streams behave?
> **Answer**: Parallel Streams utilize Java's common `ForkJoinPool`, dividing tasks across available CPU cores. Performance is **workload, dataset size, and hardware-dependent**. For small batches, thread overhead can exceed computational savings; for large candidate batches, multi-core parallelism provides significant speedup, as measured by our empirical live benchmark.
