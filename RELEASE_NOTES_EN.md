# Sprint 1 Release - P0 Core Features

## Version Info

| Item | Content |
|------|---------|
| **Version** | v1.0.0 |
| **Release Date** | 2026-04-11 |
| **Sprint** | 1 |
| **Type** | P0 Core Features Release |

---

## Completed Features

### P0-001 Create a personal teaching assistant application file
- Enter name, student ID, major, phone, email, available time, skills
- Information automatically saved in JSON format
- Same student ID cannot register twice

---

### P0-002 View the list of teaching assistant positions you can apply for
- Display core position info: position name, course/activity, number of openings, requirements, application deadline, publisher
- Closed positions marked as unavailable

---

### P0-003 Submit an application for a teaching assistant position
- Click "Apply" button on position list page
- System checks if already applied for same position, duplicate application is blocked with prompt
- Unique application record generated after submission, status "Under Review"
- Application information linked with applicant's personal profile

---

### P0-004 Check the status of your application
- Enter application records page, view all application records
- Each record shows position name, application time, current status (Under Review/Withdrawn/Approved/Rejected)
- Status synchronized with MO's decision in real-time

---

### P0-005 Release teaching assistant positions
- Enter position publish page, fill in position information
- Published positions automatically appear in public listing
- Support setting application deadline, position becomes unavailable after deadline

---

### P0-006 View the list of TAs for this position
- Enter "My Positions" page
- Page shows all applicants' basic info and application time for position
- Also displays application status for current MO's published positions

---

### P0-007 Single review of applicants' applications
- Click an application in list to select "Approve" or "Reject"
- Applicant's application status automatically updates after selection
- Status synchronized to applicant's application records page

---

### P0-009 Role login verification
- User enters login page, inputs username and password
- System validates credentials, shows prompt on validation failure
- Different roles redirected to corresponding consoles (Applicant/MO/Admin)
- Login info can be saved

---

### P0-011 Basic error handling
- Friendly prompts for required fields empty, duplicate application, closed position application, invalid account login, etc.
- Prompts clearly guide user on next steps
- System won't crash on exceptions

---

### P0-012 Core function interface interaction
- Smooth navigation between login page, position list page, my applications page, personal profile page
- Smooth page switching via top or sidebar navigation buttons
- User state and filled content preserved during page switching

---

### P0-013 Withdraw unreviewed applications
- Can withdraw applications with "Under Review" status
- After withdrawal, record status changes to "Withdrawn"
- Corresponding position on list page becomes available again

---

### P0-014 Removed posted posts
- Support editing position info and requirements
- Edited position info synchronized to position list

---

## Data Persistence

| Module | Storage Location | Format |
|--------|-----------------|--------|
| TA Personal Profile | `resources/Data/TAData/` | JSON |
| Position Info | `resources/Data/JobData/` | JSON |
| Application Records | `resources/Data/ApplicationData/` | JSON |

---

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | JDK 11+ |
| JavaFX | 17+ |
| Jackson | 2.x |
| Maven | 3.x |

---

## How to Run

```bash
# Compile project
mvn clean compile

# Run application
mvn javafx:run
```

---


