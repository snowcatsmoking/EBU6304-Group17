# EBU6304 Group 17 – TA Recruitment System

> BUPT International School Teaching Assistant Recruitment System
> EBU6304 Software Engineering Group Project

---

## Team Members

| Name | GitHub | Role |
|------|--------|------|
| TBD  | TBD    | Project Lead |
| TBD  | TBD    | Developer |
| TBD  | TBD    | Developer |
| TBD  | TBD    | Developer |
| TBD  | TBD    | Developer |
| TBD  | TBD    | Developer |

---

## Project Overview

A Java desktop application that streamlines the Teaching Assistant recruitment process at BUPT International School, replacing the current Excel/form-based workflow.

**Key users:**
- **TA (Teaching Assistant)** – create profile, upload CV, browse and apply for jobs, track application status
- **MO (Module Organiser)** – post job openings, review and select applicants
- **Admin** – oversee TA workload across all modules

**Tech stack:**
- Language: Java (Swing desktop application)
- Data storage: JSON files (no database)
- Build tool: TBD

---

## Branch Strategy

We use a centralised branching model:

```
main
 └── dev
      ├── feature/name-feature
      ├── feature/name-feature
      └── ...
```

| Branch | Purpose |
|--------|---------|
| `main` | Stable releases only. Merged from `dev` before each assessment. |
| `dev` | Integration branch. All feature branches merge here via PR. |
| `feature/name-feature` | Each member's development branch. One branch per feature. |

### Rules
1. **Never push directly to `main` or `dev`.**
2. All work happens on a `feature/` branch.
3. When a feature is complete, open a **Pull Request to `dev`**.
4. The project lead reviews and merges all PRs.
5. Before each assessment, `dev` is merged into `main` and a version tag is created.

### Version Tags
| Tag | Assessment |
|-----|-----------|
| `v1.0` | First assessment – 22 March |
| `v2.0` | Intermediate assessment – 12 April |
| `v4.0` | Final assessment – 24 May |

---

## Getting Started

### Clone the repository
```bash
git clone https://github.com/snowcatsmoking/EBU6304-Group17.git
cd EBU6304-Group17
```

### Create your feature branch
```bash
git checkout dev
git pull origin dev
git checkout -b feature/yourname-featurename
```

### Daily workflow
```bash
# Before starting work, sync with dev
git pull origin dev

# After finishing work
git add .
git commit -m "feat: short description of what you did"
git push origin feature/yourname-featurename

# Then open a Pull Request to dev on GitHub
```

### Commit message format
```
feat:     new feature
fix:      bug fix
docs:     documentation update
test:     adding or updating tests
refactor: code restructure without behaviour change
```

---

## Assessment Schedule

| Date | Milestone | Weight |
|------|-----------|--------|
| 22 March 2026 | First Assessment – Product Backlog, Prototype, Report | 30% |
| 12 April 2026 | Intermediate Assessment – Demo & Viva | 20% |
| 24 May 2026   | Final Assessment – Software, Video, Report, Demo & Viva | 50% |

---

## Repository Structure

```
EBU6304-Group17/
├── docs/
│   ├── assessment1/      # Assessment 1: Backlog, Prototype, Report
│   ├── assessment2/      # Assessment 2: Design documents
│   └── assessment3/      # Assessment 3: Testing reports
├── src/
│   ├── main/java/        # Production source code
│   └── test/java/        # Test source code
├── resources/
│   ├── handout/          # Original course handout
│   ├── analysis/         # Internal working notes and analysis
│   ├── figma/            # Prototype source files
│   └── survey/           # Survey and interview evidence
└── README.md
```
