# EBU6304 Group 17 – TA Recruitment System

> BUPT International School Teaching Assistant Recruitment System  
> EBU6304 Software Engineering Group Project

---

## Team Members

| Name        | GitHub | Role |
|-------------|--------|------|
| Minghui Pan | [snowcatsmoking](https://github.com/snowcatsmoking) | Project Lead |
| Ziqian Cao  | [laishengzuoyun](https://github.com/laishengzuoyun) | Developer |
| Hongze Zhao | [ZHngze](https://github.com/ZHngze) | Developer |
| Shiyang Xie | [MAVERICKDGD](https://github.com/MAVERICKDGD) | Developer |
| Zishen Ma   | [ArimaKana608](https://github.com/ArimaKana608) | Developer |
| Haoyang Qin | [zhengtingxia](https://github.com/zhengtingxia) | Developer |

---

## Project Overview

A JavaFX desktop application that streamlines the Teaching Assistant recruitment process at BUPT International School, replacing the current Excel/form-based workflow.

**Three user roles:**
- **TA (Teaching Assistant Applicant)** – complete profile, browse open positions, apply, upload CV, track application status and review feedback
- **MO (Module Organiser)** – post positions with deadlines, review applications (approve / reject with comments, undo decisions), manage position status (open / close / reopen)
- **Admin** – manage all user accounts, view global positions, inspect operation logs

**Tech stack:**
- Language: Java 21
- UI Framework: JavaFX 21
- Data storage: JSON files via Jackson (no database)
- Build tool: Maven

---

## Features

### TA Side
- Register / login with role detection
- Dashboard with live stats (submitted, approved, under review, available positions)
- Browse and filter positions (by course name, available time, openings)
- Apply for positions with profile snapshot; upload Word / PDF attachments
- Track application history with date-range filtering
- View application details including MO review comments
- Withdraw pending applications

### MO Side
- Post new positions with deadline validation (must be at least tomorrow)
- My Positions list with per-position pending-review badge
- Close / Reopen positions (with confirmation dialog); expired positions shown separately
- View applicant details (full profile: name, major, phone, email, available time, skills)
- Approve or reject applications with optional review comment
- Undo Approve / Reject decisions (reset back to Pending)
- Batch approve / reject across multiple applications
- Pagination for position and applicant lists

### Admin Side
- User account management (view, reset password, delete)
- Global positions overview
- Operation log viewer

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.6+

### Clone and run
```bash
git clone https://github.com/snowcatsmoking/EBU6304-Group17.git
cd EBU6304-Group17
mvn javafx:run
```

### Default entry point
`src/main/java/ZiqianCao/java/TAPositionListUI.java`  
(or launch via `LoginScreen/LoginLauncher.java` for the login screen)

### Test accounts (local data)
| Role | Account | Password |
|------|---------|----------|
| TA   | TAPan   | (set at registration) |
| MO   | MOPan   | (set at registration) |
| Admin | Admin001 | (requires admin auth code: `BUPTAdmin`) |

---

## Repository Structure

```
EBU6304-Group17/
├── src/
│   ├── main/java/
│   │   ├── core/                  # AppNavigator (shared Stage routing)
│   │   ├── data/                  # DataConfig, JobDataManager, UserDataManager,
│   │   │                          #   LogManager, LocalStorageManager
│   │   ├── LoginScreen/           # LoginView, UserManager, User, LoginLauncher
│   │   ├── Admin/                 # AdminDashboard, MODashboard,
│   │   │                          #   UserManagementView, GlobalPositionsView,
│   │   │                          #   OperationLogView, DashboardView (admin)
│   │   └── ZiqianCao/java/        # TA-side UI & logic:
│   │                              #   TAPositionListUI, DashboardView,
│   │                              #   MyApplicationsView, ApplicationDetailView,
│   │                              #   ProfileView, TAApplicationFormView,
│   │                              #   FileUploader, TAJob, TAApplication,
│   │                              #   TAApplicationRecord(Manager),
│   │                              #   TAApplicationManager
│   └── test/java/                 # Unit tests
├── data/                          # Runtime JSON data (gitignored in part)
│   ├── TAData/                    # TA profile JSON files
│   ├── MOData/                    # MO profile JSON files
│   ├── AdminData/                 # Admin account JSON files
│   ├── JobData/                   # Posted position JSON files
│   ├── ApplicationData/           # Application record JSON files
│   ├── Logs/                      # Operation log JSON files
│   └── Uploads/                   # Uploaded CV / attachment files
├── docs/                          # Assessment documents
├── resources/                     # Handout, prototype, analysis, survey
├── Product_Backlog.xlsx
├── pom.xml
└── README.md
```

---

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable releases only. Merged from `dev` before each assessment. |
| `dev`  | Integration branch. All feature branches merge here via PR. |
| `<name>` | Each member's personal development branch. |

### Rules
1. Never push directly to `main` or `dev`.
2. All work happens on a personal branch.
3. When work is complete, open a **Pull Request to `dev`**.
4. The project lead reviews and merges all PRs.

### Commit message format
```
feat:     new feature
fix:      bug fix
docs:     documentation update
refactor: code restructure without behaviour change
test:     adding or updating tests
```

---

## Assessment Schedule

| Date | Milestone | Weight |
|------|-----------|--------|
| 22 March 2026   | First Assessment – Product Backlog, Prototype, Report | 30% |
| 12 April 2026   | Intermediate Assessment – Demo & Viva | 20% |
| 24 May 2026     | Final Assessment – Software, Video, Report, Demo & Viva | 50% |
