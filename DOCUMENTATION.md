# 📘 Student Result Management System (SRMS) — Complete Documentation

**Version:** 2.0  
**Language:** Java 8+  
**GUI Framework:** Java Swing / AWT  
**Database:** MySQL 8.0+  
**Connectivity:** JDBC (Java Database Connectivity)

---

## 📋 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture](#3-system-architecture)
4. [Database Schema](#4-database-schema)
5. [Module Descriptions](#5-module-descriptions)
   - 5.1 [Login Screen](#51-login-screen)
   - 5.2 [Student Result Inquiry Portal](#52-student-result-inquiry-portal)
   - 5.3 [Main Dashboard (Admin)](#53-main-dashboard-admin)
   - 5.4 [Student Management](#54-student-management)
   - 5.5 [Marks & Results](#55-marks--results)
   - 5.6 [Analytics & Reports](#56-analytics--reports)
   - 5.7 [System & Administration](#57-system--administration)
6. [Security Features](#6-security-features)
7. [Setup & Execution Guide](#7-setup--execution-guide)
8. [File Structure](#8-file-structure)
9. [UI Highlights](#9-ui-highlights)
10. [Recent Updates (v2.0)](#10-recent-updates-v20)

---

## 1. Project Overview

The **Student Result Management System (SRMS)** is a comprehensive desktop application built using **Java Swing** and **MySQL**. It provides a digitized, secure, and user-friendly platform for academic institutions to manage student records, examination marks, grades, attendance, and faculty feedback.

### Key Goals
- Centralized management of student academic data
- Role-based access control (Super Admin / Branch Admin)
- Transparent student self-service result inquiry
- Automated grading, CGPA calculation, and reporting
- Secure access with SHA-256 password hashing and math CAPTCHA

---

## 2. Technology Stack

| Component          | Technology                        |
|--------------------|-----------------------------------|
| Language           | Java 8+                           |
| GUI Framework      | Java Swing, AWT                   |
| Database           | MySQL 8.0+                        |
| JDBC Driver        | MySQL Connector/J                 |
| Password Security  | SHA-256 Hashing                   |
| Layout Managers    | BorderLayout, GridBagLayout, FlowLayout, BoxLayout |
| Print Support      | Java Print API (`java.awt.print`) |

---

## 3. System Architecture

SRMS follows a **two-tier client-server architecture**:

```
┌─────────────────────────────────────────────┐
│              Presentation Layer              │
│         (Java Swing GUI - JFrames)           │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│              Business Logic Layer            │
│     (DatabaseManager, AppSession, etc.)      │
└─────────────────────┬───────────────────────┘
                      │  JDBC
┌─────────────────────▼───────────────────────┐
│              Data Layer                      │
│         (MySQL Database - srms)              │
└─────────────────────────────────────────────┘
```

### Core Classes

| Class                   | Role                                              |
|-------------------------|---------------------------------------------------|
| `SRMS.java`             | Application entry point; initializes DB & GUI     |
| `DatabaseManager.java`  | All SQL operations, connection management, hashing|
| `AppSession.java`       | Holds logged-in user's session (username, branch, role) |
| `ThemeManager.java`     | Manages Light/Dark mode toggle                    |
| `LoginScreen.java`      | Admin login form + Student Portal entry point     |
| `MainDashboard.java`    | Admin navigation hub with role-based menus        |
| `StudentSearchScreen.java` | Student self-service Result Inquiry Portal     |

---

## 4. Database Schema

The system uses a MySQL database named **`srms`** with the following tables:

### 4.1 `student` — Student Records
| Column       | Type          | Description                     |
|--------------|---------------|---------------------------------|
| `student_id` | INT (PK, AI)  | Auto-incremented primary key    |
| `roll_no`    | VARCHAR(50)   | Unique roll number (used as FK) |
| `name`       | VARCHAR(100)  | Full name of student            |
| `course`     | VARCHAR(100)  | Course / Branch name            |
| `semester`   | INT           | Current semester                |

### 4.2 `marks` — Academic Marks
| Column           | Type         | Description                          |
|------------------|--------------|--------------------------------------|
| `mark_id`        | INT (PK, AI) | Auto-incremented primary key         |
| `roll_no`        | VARCHAR(50)  | FK → student.roll_no                 |
| `subject_code`   | VARCHAR(200) | Subject name and code                |
| `internal_marks` | INT          | Internal exam marks (max 50)         |
| `external_marks` | INT          | External exam marks (max 50)         |
| `total_marks`    | INT          | Computed total                       |
| `percentage`     | DOUBLE       | Computed percentage                  |
| `grade`          | VARCHAR(5)   | Letter grade (A+, A, B, C, D, E, F) |
| `status`         | VARCHAR(10)  | Pass / Fail                          |
| `published`      | TINYINT(1)   | 1 = visible to student portal        |

### 4.3 `student_reviews` — Faculty Feedback
| Column         | Type         | Description                        |
|----------------|--------------|------------------------------------|
| `review_id`    | INT (PK, AI) | Auto-incremented primary key       |
| `roll_no`      | VARCHAR(50)  | FK → student.roll_no               |
| `subject_code` | VARCHAR(200) | Subject the feedback applies to    |
| `grade`        | VARCHAR(5)   | Grade at the time of review        |
| `review_type`  | VARCHAR(50)  | Excellent / Good / Average / etc.  |
| `comment`      | TEXT         | Free-text feedback from faculty    |

### 4.4 `admin_users` — System Users
| Column          | Type         | Description                          |
|-----------------|--------------|--------------------------------------|
| `id`            | INT (PK, AI) | Auto-incremented primary key         |
| `username`      | VARCHAR(50)  | Unique admin username                |
| `password_hash` | VARCHAR(64)  | SHA-256 hash of the password         |
| `branch`        | VARCHAR(100) | Branch assigned (`All` = Super Admin)|
| `role`          | VARCHAR(20)  | `super_admin` or `branch_admin`      |

### 4.5 `attendance` — Student Attendance
| Column            | Type         | Description                         |
|-------------------|--------------|-------------------------------------|
| `id`              | INT (PK, AI) | Auto-incremented primary key        |
| `roll_no`         | VARCHAR(50)  | FK → student.roll_no                |
| `subject_code`    | VARCHAR(200) | Subject of the attendance record    |
| `attendance_date` | DATE         | Date of attendance                  |
| `status`          | ENUM         | `Present`, `Absent`, or `Late`      |

### 4.6 `audit_log` — System Activity Log
| Column         | Type         | Description                        |
|----------------|--------------|------------------------------------|
| `id`           | INT (PK, AI) | Auto-incremented primary key       |
| `action`       | VARCHAR(100) | Action type (e.g., LOGIN, PRINT)   |
| `details`      | TEXT         | Username + description of action   |
| `performed_at` | DATETIME     | Timestamp of the action            |

> **Note:** All tables are auto-created on first launch via `DatabaseManager.initializeDatabase()`. No manual SQL setup required.

---

## 5. Module Descriptions

---

### 5.1 Login Screen

**File:** `LoginScreen.java`  
**Window Size:** 560 × 480

The application entry point supports two user flows:

#### 🔐 Admin Login
- Fields: **Username** and **Password**
- Credentials verified against `admin_users` table using SHA-256 hashed password
- On success: sets `AppSession` (username, branch, role) and opens `MainDashboard`
- **Brute-force protection:** After **3 failed attempts**, the login button is disabled for **30 seconds**

#### 🎓 Student Result Portal Button
- A dedicated **"Student Result Portal →"** button on the login screen
- No password required — students are identified by their Roll Number
- Clicking it opens the `StudentSearchScreen` directly
- This is a read-only, inquiry-only interface

---

### 5.2 Student Result Inquiry Portal

**File:** `StudentSearchScreen.java`  
**Window Size:** 860 × 680  
**Access:** Via "Student Result Portal" button on Login Screen

#### Flow
1. Student enters their **Roll Number**
2. A **Math CAPTCHA** dialog appears (see Security section)
3. On correct CAPTCHA → results are loaded from the database

#### Features

**Profile Card (Summary)**
- Name, Branch, Semester
- Total Marks, Average, CGPA (on a 10-point scale), Overall Grade, PASS/FAIL status

**Tab 1 — 📋 Marks & Grades**
- Table showing: Subject Code, Internal Marks, External Marks, Total, Percentage, Grade, Status, Faculty Feedback
- Rows are color-coded: 🟢 Green = Pass, 🔴 Red = Fail
- **Only published marks** (`published = 1`) are shown to students

**Tab 2 — 📅 Attendance**
- Subject-wise attendance records with date and status
- Color-coded: 🟢 Present, 🔴 Absent, 🟡 Late

**Tab 3 — 💬 Reviews & Feedback**
- Faculty comments and review types per subject

**Footer Actions**
| Button | Action |
|--------|--------|
| 🖨 Print Result Card | Opens printable `ResultCardScreen` |
| 💾 Export CSV | Saves result to a CSV file |
| ← Back to Login | Returns to `LoginScreen` |

#### Grade Scale
| Percentage | Grade | Grade Point |
|------------|-------|-------------|
| ≥ 90%      | A+    | 10          |
| ≥ 80%      | A     | 9           |
| ≥ 70%      | B     | 8           |
| ≥ 60%      | C     | 7           |
| ≥ 50%      | D     | 6           |
| ≥ 40%      | E     | 5           |
| < 40%      | F     | 0           |

#### Passing Criteria
- Internal Marks: **≥ 15** (out of 50)
- External Marks: **≥ 35** (out of 50)

---

### 5.3 Main Dashboard (Admin)

**File:** `MainDashboard.java`  
**Access:** Admin login only

The central navigation hub. Displays the logged-in admin's username and branch. Provides a **Dark/Light Mode** toggle button in the header.

Role-based visibility:
- **Super Admin** (`role = super_admin`, `branch = All`): Sees all branches, Audit Log, and Manage Admins
- **Branch Admin** (`role = branch_admin`): Sees only their assigned branch data

---

### 5.4 Student Management

#### Add Student — `AddStudentScreen.java`
- Register a new student: Roll No, Name, Course/Branch, Semester

#### Manage Students — `ManageStudentsScreen.java`
- View all students in a table
- Filter by Branch and Semester
- Delete student records (cascades to marks, reviews, attendance)

#### Bulk Import — `BulkImportScreen.java`
- Import multiple student records from a **CSV file**
- Format: `roll_no, name, course, semester`

#### Search Student — `SearchStudentScreen.java`
- Admin tool to quickly search any student by Roll Number
- Displays student details + all marks (including unpublished)

#### Update Student — `UpdateStudentScreen.java`
- Edit an existing student's name, course, or semester

---

### 5.5 Marks & Results

#### Enter Marks — `MarksEntryScreen.java`
- Select student by Roll Number
- Choose subject from a dropdown
- Enter Internal (max 50) and External (max 50) marks
- System auto-calculates: Total, Percentage, Grade, Status

#### Update Marks — `UpdateMarksScreen.java`
- Fetch and modify existing marks for a student-subject pair
- Recalculates grade and status on update

#### View Results — `ViewReportsScreen.java`
- Composite view of all students: demographics + marks + feedback
- Filterable by Name, Branch, Semester
- Supports **publishing** marks to make them visible in the Student Portal

#### Review & Comment — `StudentReviewScreen.java`
- Faculty adds review type (Excellent / Good / Average / Needs Improvement / Poor)
- Adds free-text feedback comment per subject
- History of all past feedback shown in a scrollable log

#### Result Card — `ResultCardScreen.java`
- Printable official result card with college header
- Subject-wise marks table, total and average summary
- Print / Save as PDF support via OS print dialog

---

### 5.6 Analytics & Reports

#### Statistics Dashboard — `StatsDashboardScreen.java`
- Aggregate statistics: pass/fail counts, average marks, grade distribution

#### Rank List / Toppers — `TopperRankScreen.java`
- Rankings of students by total marks or CGPA
- Filterable by branch and semester

#### CGPA Calculator — `CgpaCalculatorScreen.java`
- Calculates CGPA for a student across all entered subjects
- Displays per-subject grade points and cumulative average

#### Attendance Tracking — `AttendanceScreen.java`
- Admin can record daily attendance per student per subject
- Status options: Present, Absent, Late

---

### 5.7 System & Administration

#### Change Password — `ChangePasswordScreen.java`
- Any logged-in admin can change their own password
- Requires current password verification before update

#### Audit Log — `AuditLogScreen.java` *(Super Admin only)*
- Timestamped log of all significant system actions
- Actions include: LOGIN, MARKS_ENTRY, PRINT_RESULT_CARD, etc.

#### Manage Admins — `ManageAdminsScreen.java` *(Super Admin only)*
- Create new branch admin accounts
- Assign branch and role
- Delete existing admin accounts (cannot delete `admin`)

---

## 6. Security Features

### 6.1 SHA-256 Password Hashing
All admin passwords are stored as SHA-256 hashes — never in plaintext.

```java
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] hash = md.digest(password.getBytes("UTF-8"));
```

### 6.2 Brute-Force Lockout
- After **3 incorrect login attempts**, all login fields are disabled
- A 30-second countdown timer is displayed before retry is allowed

### 6.3 Math CAPTCHA (Student Portal)
Before viewing results, students must solve a randomly generated math problem:

- Operations: **Addition (+)**, **Subtraction (−)**, **Multiplication (×)**
- Numbers randomly chosen between 1 and 20
- Subtraction is always positive (larger number − smaller)
- Wrong answers show an error message; user must retry

**Example CAPTCHA questions:**
```
14 + 7 = ?
18 - 5 = ?
6 × 9 = ?
```

### 6.4 Role-Based Access Control
| Feature               | Branch Admin | Super Admin |
|-----------------------|:------------:|:-----------:|
| Add/Manage Students   | ✅ (own branch) | ✅ (all) |
| Enter/Update Marks    | ✅           | ✅          |
| View Audit Log        | ❌           | ✅          |
| Manage Admins         | ❌           | ✅          |

### 6.5 Result Publishing Gate
- Students only see marks where `published = 1`
- Unpublished marks are invisible in the Student Portal
- Admins control publishing via `ViewReportsScreen`

### 6.6 SQL Injection Prevention
All queries use **PreparedStatements** with parameterized inputs.

---

## 7. Setup & Execution Guide

### Prerequisites
1. **JDK 8 or higher** installed and added to PATH
2. **MySQL Server 8.0+** running locally
3. **MySQL Connector/J** JAR placed in the `lib/` directory

### Database Configuration
Open `src/main/java/com/srms/DatabaseManager.java` and update:

```java
private static final String URL =
    "jdbc:mysql://localhost:3306/srms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

> Tables are created automatically on first run — no manual SQL needed.

### Default Admin Credentials
| Username | Password | Role        | Branch |
|----------|----------|-------------|--------|
| `admin`  | `admin`  | super_admin | All    |

> ⚠️ **Change the default password immediately after first login.**

### Running the Application

#### Option 1: Using the provided script
```bash
cd "Java Project SRMS"
bash run.sh
```

#### Option 2: Manual compile & run
```bash
# Compile
javac -cp ".:lib/mysql-connector-j.jar" -d bin src/main/java/com/srms/*.java

# Run
java -cp "bin:lib/mysql-connector-j.jar" com.srms.SRMS
```

#### Option 3: IDE (IntelliJ / Eclipse / VS Code)
- Set `src/main/java` as the source root
- Add `lib/mysql-connector-j.jar` to the build path
- Run `com.srms.SRMS` as the main class

---

## 8. File Structure

```
Java Project SRMS/
├── src/
│   └── main/
│       └── java/
│           └── com/srms/
│               ├── SRMS.java                  # Entry point
│               ├── DatabaseManager.java       # SQL + connection + hashing
│               ├── AppSession.java            # Session state
│               ├── ThemeManager.java          # Light/Dark mode
│               ├── LoginScreen.java           # Admin login + Student Portal button
│               ├── MainDashboard.java         # Admin navigation hub
│               ├── StudentSearchScreen.java   # Student Result Inquiry Portal (w/ CAPTCHA)
│               ├── ResultCardScreen.java      # Printable result card
│               ├── AddStudentScreen.java      # Register new student
│               ├── ManageStudentsScreen.java  # View/filter/delete students
│               ├── UpdateStudentScreen.java   # Edit student details
│               ├── SearchStudentScreen.java   # Admin student search
│               ├── BulkImportScreen.java      # CSV bulk import
│               ├── MarksEntryScreen.java      # Enter subject marks
│               ├── UpdateMarksScreen.java     # Update existing marks
│               ├── ViewReportsScreen.java     # View + publish results
│               ├── StudentReviewScreen.java   # Faculty feedback
│               ├── StatsDashboardScreen.java  # Statistics overview
│               ├── TopperRankScreen.java      # Rank list
│               ├── CgpaCalculatorScreen.java  # CGPA calculator
│               ├── AttendanceScreen.java      # Attendance tracking
│               ├── ChangePasswordScreen.java  # Change admin password
│               ├── AuditLogScreen.java        # System audit log
│               └── ManageAdminsScreen.java    # Manage admin accounts
├── lib/
│   └── mysql-connector-j.jar                 # JDBC driver
├── bin/                                       # Compiled .class files
├── doc/                                       # JavaDoc (if generated)
├── pom.xml                                    # Maven config (optional)
├── run.sh                                     # Shell script to compile & run
├── srms.db                                    # (legacy SQLite, not used in v2)
└── DOCUMENTATION.md                          # This file
```

---

## 9. UI Highlights

- **Academic Blue/White** color palette throughout
- **Dark Mode** toggle available on the Admin Dashboard
- **Responsive tables** with scroll panes across all data views
- **Color-coded rows:** Pass = green background, Fail = red background
- **Hover effects** on all dashboard navigation buttons
- **Large, readable fonts** (14–20px) for comfortable desktop use
- **GridBagLayout** precision layout for all forms
- **Emoji-enhanced** tab labels and buttons for intuitive navigation

---

## 10. Recent Updates (v2.0)

### ✅ Student Portal Access Button
- Added a prominent **"Student Result Portal →"** button on the Login Screen
- Students no longer need to know a separate entry point — it's visible immediately on launch

### ✅ Enlarged Login Screen
- Window size increased from **420×340** to **560×480**
- Header font scaled from 15px to **20px**
- All form elements, labels, and buttons enlarged for better readability
- Input fields are taller (38px) with larger text (14px)

### ✅ Math CAPTCHA in Student Portal
- Students must solve a randomly generated math problem before results load
- Prevents automated scraping of student records
- Operations: **+**, **−**, **×** with numbers between 1–20
- Wrong answers are rejected with a clear error message; student may retry

### ✅ Result Publishing Workflow
- Admin controls which marks are visible to students via the `published` flag
- Unpublished results are never shown in the Student Portal

### ✅ Role-Based Dashboard
- Super Admins see all branches, audit logs, and admin management
- Branch Admins are scoped to their assigned branch only

---

*Documentation generated for SRMS v2.0 — April 2026*
