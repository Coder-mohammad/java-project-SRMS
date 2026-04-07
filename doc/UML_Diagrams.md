# SRMS UML Diagrams

This document contains the structural and behavioral UML diagrams for the Student Result Management System (SRMS). 
These diagrams use Mermaid.js syntax, which can be rendered directly in most Markdown viewers (including GitHub).

---

## 1. Use Case Diagram
This diagram shows the interactions between the system's users (Actors) and the functionalities (Use Cases) it provides.

```mermaid
usecaseDiagram
    actor Administrator as "Administrator / Faculty"

    package "Student Result Management System" {
        usecase UC1 as "Secure Login"
        usecase UC2 as "Add New Student"
        usecase UC3 as "Manage Students (View/Delete)"
        usecase UC4 as "Enter Subject Marks"
        usecase UC5 as "Auto-Calculate Results & Grades"
        usecase UC6 as "View Results / Report Cards"
    }

    Administrator --> UC1
    Administrator --> UC2
    Administrator --> UC3
    Administrator --> UC4
    Administrator --> UC6
    
    UC4 ..> UC5 : <<includes>>
```

---

## 2. Class Diagram
This diagram outlines the system's class structure, showing the primary UI components, the database manager, and their relationships.

```mermaid
classDiagram
    class SRMS {
        +main(args: String[])
    }

    class DatabaseManager {
        -URL: String$
        +getConnection() Connection$
        +initializeDatabase()$
    }

    class LoginScreen {
        +LoginScreen()
    }

    class MainDashboard {
        +MainDashboard()
    }

    class AddStudentScreen {
        -rollNoField: JTextField
        -nameField: JTextField
        -branchCombo: JComboBox
        -semesterCombo: JComboBox
        +AddStudentScreen()
        -saveStudent()
    }

    class ManageStudentsScreen {
        -model: DefaultTableModel
        +ManageStudentsScreen()
        -loadData()
        -deleteStudent(rollNo: String)
    }

    class MarksEntryScreen {
        -rollNoField: JTextField
        -subjectCodeField: JTextField
        -internalMarksField: JTextField
        -externalMarksField: JTextField
        -totalMarksField: JTextField
        -gradeField: JTextField
        +MarksEntryScreen()
        -submitMarks()
    }

    class ViewReportsScreen {
        +ViewReportsScreen()
        -loadData(model: DefaultTableModel)
    }

    SRMS --> DatabaseManager : "Initializes DB"
    SRMS ..> LoginScreen : "Instantiates"
    LoginScreen ..> MainDashboard : "Instantiates on Success"
    
    MainDashboard ..> AddStudentScreen : "Navigates"
    MainDashboard ..> ManageStudentsScreen : "Navigates"
    MainDashboard ..> MarksEntryScreen : "Navigates"
    MainDashboard ..> ViewReportsScreen : "Navigates"

    AddStudentScreen --> DatabaseManager : "Uses (INSERT)"
    ManageStudentsScreen --> DatabaseManager : "Uses (SELECT/DELETE)"
    MarksEntryScreen --> DatabaseManager : "Uses (INSERT)"
    ViewReportsScreen --> DatabaseManager : "Uses (SELECT JOIN)"
```

---

## 3. Object Diagram
This diagram represents a snapshot of the instantiated objects at a specific moment in time (e.g., when the Admin is actively adding a student via the dashboard).

```mermaid
classDiagram
    %% Object Diagram simulated using Class Diagram syntax
    object SRMS_App {
        status = "Running"
    }
    
    object dbConnection : DatabaseManager {
        url = "jdbc:sqlite:srms.db"
        status = "Connected"
    }

    object mainScreen : MainDashboard {
        isVisible = true
    }

    object addScreen : AddStudentScreen {
        isVisible = true
        rollNoField = "1001"
        nameField = "John Doe"
        branchCombo = "Computer Science"
        semesterCombo = "3"
    }

    SRMS_App --> mainScreen : "Hosts"
    mainScreen --> addScreen : "Opened"
    addScreen --> dbConnection : "Sending Data"
```

---

## 4. Activity Diagram
This diagram shows the step-by-step workflow of a crucial process in the application: The workflow from logging in to successfully entering marks and viewing the report.

```mermaid
stateDiagram-v2
    [*] --> StartApp

    StartApp --> LoginScreen: Launch Application
    
    state LoginScreen {
        [*] --> EnterCredentials
        EnterCredentials --> ValidateCredentials
    }
    
    LoginScreen --> MainDashboard: Successful Login
    LoginScreen --> LoginScreen: Invalid Credentials
    
    state MainDashboard {
        [*] --> MenuSelection
        
        MenuSelection --> AddStudent
        AddStudent --> FillStudentData
        FillStudentData --> SaveStudentDB
        SaveStudentDB --> MenuSelection
        
        MenuSelection --> EnterMarks
        EnterMarks --> FillMarksData
        FillMarksData --> CalculateTotalAndGrade
        CalculateTotalAndGrade --> SaveMarksDB
        SaveMarksDB --> MenuSelection
        
        MenuSelection --> ViewReports
        ViewReports --> FetchJoinedDataDB
        FetchJoinedDataDB --> DisplayTable
        DisplayTable --> MenuSelection
    }
    
    MainDashboard --> [*]: Exit
```
