# Project Title
**Student Result Management System (SRMS)**

---

## Introduction

The Student Result Management System is a Java-based, web-enabled application designed to automate the process of managing student academic results. Traditional manual result processing is time-consuming, error-prone, and inefficient. This system allows authorized users to enter marks, automatically calculate totals, percentages, grades, and securely store results in a relational database. It improves accuracy, reduces manual effort, and enables fast retrieval of student results through a user-friendly interface.

---

## Problem Statement

In many educational institutions, student results are managed manually or using basic systems that are time-consuming, error-prone, and difficult to maintain. Manual calculation of marks, percentages, and grades increases the risk of mistakes and delays in result publication. Additionally, storing and retrieving academic records without a centralized system can lead to data loss and inefficiency. Hence, there is a need for an automated and secure student result management system.

---

## Objectives of the Project

* To maintain student result data digitally
* To automate marks management
* To reduce manual errors and paperwork
* To provide fast search and retrieval of student records
* To generate reports efficiently
* To ensure secure and reliable data storage

---

## Scope of the Project

* Can be used in schools, colleges, and universities
* Supports result management and tracking
* Allows authorized users to add, update, and view student data
* Suitable for academic record automation using Java and databases
* Can be enhanced with login security and web interface in the future

---

## Technologies Used

* **Programming Language:** Java (Core Java)
* **Database Access:** JDBC (Java Database Connectivity)
* **Database Management:** Relational Database (MySQL / Oracle)
* **User Interface:** Console-based or Web-based Menu-Driven Interface (Swing / AWT for Desktop GUI)

---

## Modules Description

### 1. Login Module
Provides secure access to authorized users such as administrators and faculty members by validating login credentials.

### 2. Student Details Module
Stores and manages student information such as roll number, name, course, and semester for proper identification.

### 3. Marks Entry Module
Allows authorized users to enter subject-wise internal and external marks with validation checks.

### 4. Result Calculation Module
Automatically calculates total marks, percentage, grade, and pass/fail status based on predefined grading criteria.

### 5. Result Display Module
Retrieves and displays student results using roll number or other search criteria for quick access.

### 6. Database Management Module
Handles data storage, updating, deletion, and retrieval using JDBC, ensuring secure and reliable database operations.

---

## Database Design

The system uses a relational database to store student and marks information efficiently while ensuring data integrity and avoiding redundancy.

**Objectives of Database Design:**
* Efficient storage of academic data
* Avoid data redundancy
* Maintain data accuracy and integrity
* Enable fast search and report generation

### Student Table

| Field Name | Data Type | Description |
| :--- | :--- | :--- |
| `student_id` | INT (Primary Key) | Unique Student ID |
| `roll_no` | VARCHAR | Student Roll Number |
| `name` | VARCHAR | Student Name |
| `course` | VARCHAR | Course Name |
| `semester` | INT | Semester Number |

### Marks Table

| Field Name | Data Type | Description |
| :--- | :--- | :--- |
| `mark_id` | INT (Primary Key) | Unique Mark Record ID |
| `roll_no` | VARCHAR (Foreign Key) | Student Roll Number |
| `subject_code` | VARCHAR | Subject Code |
| `internal_marks` | INT | Internal Marks |
| `external_marks` | INT | External Marks |
| `total_marks` | INT | Total Marks |
| `percentage` | FLOAT | Percentage |
| `grade` | VARCHAR | Grade Obtained |
| `status` | VARCHAR | Pass / Fail Status |

### Relationship Between Tables

* One student can have multiple marks records (one-to-many relationship)
* `roll_no` acts as Primary Key in the Student table
* `roll_no` acts as Foreign Key in the Marks table
* Ensures data consistency and integrity

---

## SQL Commands

### Student Table
```sql
CREATE TABLE student (
  student_id INT PRIMARY KEY AUTO_INCREMENT,
  roll_no VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(50) NOT NULL,
  course VARCHAR(50) NOT NULL,
  semester INT NOT NULL
);
```

### Marks Table
```sql
CREATE TABLE marks (
  mark_id INT PRIMARY KEY AUTO_INCREMENT,
  roll_no VARCHAR(20),
  subject_code VARCHAR(20) NOT NULL,
  internal_marks INT NOT NULL,
  external_marks INT NOT NULL,
  total_marks INT,
  percentage FLOAT,
  grade VARCHAR(5),
  status VARCHAR(10),
  FOREIGN KEY (roll_no) REFERENCES student(roll_no)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
```

---

## Abstract

The Online Student Result Management System (OSRMS) is a Java-based web-enabled application developed to automate and simplify the management of student academic results. It replaces manual calculation and paper-based records with a centralized digital platform that ensures accuracy, efficiency, and secure access. The system supports role-based access for administrators, faculty, and students, automatically calculates results, and ensures data integrity using relational databases and JDBC connectivity.

---

## Keywords
Student Result Management System, Java, JDBC, Database Management System, Web-Based Application, Academic Automation, Result Processing, Role-Based Access Control
