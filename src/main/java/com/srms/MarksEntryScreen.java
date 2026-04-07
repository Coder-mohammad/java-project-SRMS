package com.srms;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings("serial")
public class MarksEntryScreen extends JFrame {
  private JTextField rollNoField, internalMarksField, externalMarksField;
  private JComboBox<String> subjectCodeCombo;
  private JTextField totalMarksField, gradeField;

  public MarksEntryScreen() {
    setTitle("Student Result Management System - Enter Marks");
    setSize(550, 400);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(new Color(230, 240, 250));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Header Title
    JLabel titleLabel = new JLabel("Enter Marks Entry Screen", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
    titleLabel.setForeground(new Color(30, 80, 150));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
    mainPanel.add(titleLabel, BorderLayout.NORTH);

    // Form Panel
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(new Color(230, 240, 250));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 10, 8, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0; // Allow components to expand nicely

    // Fields Setup (Equal consistent sizing)
    Dimension fieldSize = new Dimension(280, 28);
    rollNoField = new JTextField();
    rollNoField.setPreferredSize(fieldSize);

    String[] subjects = {
        "- Select Subject -",
        "Fundamentals of Data Science (241CS034)",
        "Data Mining (241AI003)",
        "Advanced Data Structures & Algorithm Analysis (241CS010)",
        "Information Retrieval Systems (241AI026)",
        "Java Programming (241IT006)",
        "Operating Systems (241CS013)",
        "Probability and Statistics (241MA009)",
        "Computer Vision (241AI007)",
        "Artificial Intelligence (241AI002)",
        "Cyber Security Essentials (241CS032)"
    };
    subjectCodeCombo = new JComboBox<>(subjects);
    subjectCodeCombo.setPreferredSize(fieldSize);

    internalMarksField = new JTextField();
    internalMarksField.setPreferredSize(fieldSize);
    externalMarksField = new JTextField();
    externalMarksField.setPreferredSize(fieldSize);
    totalMarksField = new JTextField();
    totalMarksField.setPreferredSize(fieldSize);
    totalMarksField.setEditable(false);
    gradeField = new JTextField();
    gradeField.setPreferredSize(fieldSize);
    gradeField.setEditable(false);

    addField(formPanel, gbc, "Roll No:", rollNoField, 0);
    addField(formPanel, gbc, "Subject Code:", subjectCodeCombo, 1);
    addField(formPanel, gbc, "Internal Marks:", internalMarksField, 2);
    addField(formPanel, gbc, "External Marks:", externalMarksField, 3);
    addField(formPanel, gbc, "Total Marks (Auto):", totalMarksField, 4);
    addField(formPanel, gbc, "Grade (Auto):", gradeField, 5);

    mainPanel.add(formPanel, BorderLayout.CENTER);

    // Buttons Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.setBackground(new Color(230, 240, 250));

    Dimension btnSize = new Dimension(100, 32);
    JButton submitBtn = new JButton("Submit");
    submitBtn.setPreferredSize(btnSize);
    submitBtn.setBackground(new Color(50, 100, 200));
    submitBtn.setForeground(Color.WHITE);
    submitBtn.setOpaque(true);
    submitBtn.setBorderPainted(false);

    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.setPreferredSize(btnSize);

    submitBtn.addActionListener(e -> submitMarks());
    cancelBtn.addActionListener(e -> dispose());

    buttonPanel.add(submitBtn);
    buttonPanel.add(cancelBtn);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int y) {
    gbc.gridx = 0;
    gbc.gridy = y;
    gbc.gridwidth = 1;
    gbc.weightx = 0.3; // Give label less width ratio than component
    gbc.anchor = GridBagConstraints.WEST;

    JLabel jlbl = new JLabel(label);
    jlbl.setFont(new Font("Arial", Font.BOLD, 13));
    panel.add(jlbl, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7; // Give component more width ratio
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(comp, gbc);
  }

  private void submitMarks() {
    String rollNo = rollNoField.getText().trim();
    String subjectCode = (String) subjectCodeCombo.getSelectedItem();

    if (rollNo.isEmpty() || subjectCode == null || subjectCode.equals("- Select Subject -")) {
      JOptionPane.showMessageDialog(this, "Roll No and Subject Code are required!");
      return;
    }

    try {
      int internal = Integer.parseInt(internalMarksField.getText().trim());
      int external = Integer.parseInt(externalMarksField.getText().trim());

      // Validate marks range
      if (internal < 0 || internal > 100 || external < 0 || external > 100) {
        JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100!", "Input Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      int total = internal + external;
      float percentage = (total / 100.0f) * 100; // max total = 100 (50 internal + 50 external)

      String status = (internal >= 15 && external >= 35) ? "Pass" : "Fail";
      String grade = "F";

      if (status.equals("Pass")) {
        if (percentage >= 90) {
          grade = "A+";
        } else if (percentage >= 80) {
          grade = "A";
        } else if (percentage >= 70) {
          grade = "B";
        } else if (percentage >= 60) {
          grade = "C";
        } else if (percentage >= 50) {
          grade = "D";
        } else if (percentage >= 40) {
          grade = "E";
        }
      }

      totalMarksField.setText(String.valueOf(total));
      gradeField.setText(grade);

      try (Connection conn = DatabaseManager.getConnection()) {
        // Verify student exists
        String checkSql = "SELECT roll_no FROM student WHERE roll_no = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
          checkStmt.setString(1, rollNo);
          try (ResultSet rs = checkStmt.executeQuery()) {
            if (!rs.next()) {
              JOptionPane.showMessageDialog(this, "Error: Student ID does not exist", "Validation Error",
                  JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
        }

        // Insert marks
        String sql = "INSERT INTO marks (roll_no, subject_code, internal_marks, external_marks, total_marks, percentage, grade, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, rollNo);
          pstmt.setString(2, subjectCode);
          pstmt.setInt(3, internal);
          pstmt.setInt(4, external);
          pstmt.setInt(5, total);
          pstmt.setFloat(6, percentage);
          pstmt.setString(7, grade);
          pstmt.setString(8, status);

          pstmt.executeUpdate();
        }
        JOptionPane.showMessageDialog(this, "Marks Submitted Successfully!\nTotal: " + total + "\nGrade: " + grade);
        dispose();
      }
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Marks must be valid numeric values!", "Input Error",
          JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Saving Marks: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
