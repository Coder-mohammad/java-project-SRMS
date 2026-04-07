# Student Result Management System (SRMS)

**Student Result Management System (SRMS)** is a comprehensive desktop application developed using **Java Swing** and **MySQL**. It provides a digitized, secure, and user-friendly platform for academic institutions to manage student records, examinations, grades, and faculty feedback efficiently.

---

## 🚀 Key Features

### 1. Unified Dashboard
- Intuitive grid layout serving as the central control panel for academic administrators and faculty.
- Seamless navigation between all distinct modules inside the system.

### 2. Student Management
- **Add New Students**: Register students with their Roll Number, Name, Course (Branch/Specialization), Semester, and Section.
- **Manage Students**: View all registered students in an interactive table format.
- **Responsive Filtering**: Filter students instantly by Course Branch and Semester.
- **Bulk Operations**: Delete outdated or incorrectly entered student records directly from the management grid.

### 3. Examination & Marks Processing
- **Subject Registration**: Dedicated dropdown selectors pre-configured with the exact academic syllabus (e.g., *Data Mining (241AI003)*).
- **Marks Entry**: Form interfaces to log internal marks (max 50) and external marks (max 50).
- **Marks Update**: Fetch existing marks using a student's Roll Number and securely update their scores in specific subjects.
- **Automated Grading Algorithm**: 
  - Calculates Total Marks and Percentage automatically.
  - Dictates minimum passing criteria (Internals ≥ 15, Externals ≥ 35) to set `Pass` or `Fail` status.
  - Automatically converts percentage into appropriate Letter Grades (A+, A, B, C, D, E, F).

### 4. Faculty Review & Feedback Module
- **Search Integration**: Automatically binds the student and subject records to the review interface.
- **Review Categorization**: Select between standardized parameters (Excellent, Good, Average, Needs Improvement, Poor).
- **Custom Feedback Log**: Add personalized textual observations to student subject results.
- **Historical Audit**: Review the log of feedback provided across previous subject entries.

### 5. Comprehensive Reporting & Export
- **View Results Report**: Generates a composite overview of all students, combining their registered demographic details with their academic performance and faculty feedback.
- **Report Filtering**: Sort analytical data by specific Student Name, Branch, or Semester to monitor cohort performance.

### 6. Student Portal (Inquiry Interface)
- An isolated, restricted-access inquiry page designed for students.
- **Result Lookup**: Students enter their Roll Number to render their unique, personalized marks ledger.
- **Complete Transparency**: The inquiry view outputs Internal/External breakdown, Totals, Grades, Status, and **Faculty Feedback comments**.

---

## 🛠 Technology Stack

- **Language:** Java 8+
- **GUI Framework:** Java Swing, AWT
- **Database Architecture:** MySQL 8.0+
- **Database Connectivity:** JDBC (Java Database Connectivity)
- **Layout Management:** `BorderLayout`, `GridBagLayout`, `FlowLayout`

---

## 📁 Database Schema

The core MySQL schema encompasses three interlinked tables:

### 1. `student` (Demographic Records)
- `roll_no` (Primary Key, VARCHAR)
- `name` (VARCHAR)
- `course` (VARCHAR)
- `semester` (INT)
- `section` (VARCHAR)

### 2. `marks` (Academic Assessment)
- `id` (Primary Key, INT AUTO_INCREMENT)
- `roll_no` (Foreign Key referencing `student`)
- `subject_code` (VARCHAR)
- `internal_marks` (INT)
- `external_marks` (INT)
- `total_marks` (INT)
- `percentage` (FLOAT)
- `grade` (VARCHAR)
- `status` (VARCHAR)

### 3. `student_reviews` (Faculty Feedback)
- `id` (Primary Key, INT AUTO_INCREMENT)
- `roll_no` (Foreign Key referencing `student`)
- `subject_code` (VARCHAR)
- `review_type` (VARCHAR)
- `comment` (TEXT)
- `created_at` (TIMESTAMP)

---

## 💻 Setup and Execution Guide

### Prerequisites
1. Ensure you have the **Java Development Kit (JDK 8 or higher)** installed.
2. Ensure you have **MySQL Server** installed, running, and accessible.
3. Obtain the **MySQL Connector/J (JDBC driver)** `.jar` file and place it inside the `lib/` directory of the project.

### Database Configuration
When the application starts, it immediately establishes a connection using the credentials programmed in `DatabaseManager.java`. 
1. Open `src/main/java/com/srms/DatabaseManager.java`.
2. Locate the static connection parameters:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/srms_db";
   private static final String USER = "root";
   private static final String PASSWORD = "your_mysql_password"; // Ensure this matches your local admin password
   ```
3. Update `PASSWORD` if necessary.

**Note:** You do *not* need to manually create the tables. `DatabaseManager.initializeDatabase()` safely and automatically creates the schema `srms_db` and builds all requisite tables on the first boot.

### Compiling and Running
To execute the application manually via the terminal:

1. **Navigate** to your project root folder:
   ```bash
   cd "Java Project SRMS"
   ```
2. **Compile** all Java classes into the `bin/` directory:
   ```bash
   javac -Xlint:all -cp "bin:lib/*" -d bin src/main/java/com/srms/*.java
   ```
3. **Run** the main application entry point:
   ```bash
   java -cp "bin:lib/*" com.srms.SRMS
   ```

*(Alternatively, use an IDE like Eclipse, IntelliJ, or VS Code by directing the build path to load libraries inside `lib/`)*

---

## 🎨 User Interface (UI) Highlights

The project has recently undergone expansive UI polishing to deliver an enterprise-tier visual experience:
- Modern, clean **academic blue/white** color palette layout.
- Responsive, auto-centering GUI windows utilizing **GridBagLayout**.
- Input validation parameters resolving oversized spacing, component stretching, and font clipping errors.
- Automatic scrolling (`JScrollPane`) applied universally across Data Tables and Student Feedback interfaces.
