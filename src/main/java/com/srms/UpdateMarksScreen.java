package com.srms;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings("serial")
public class UpdateMarksScreen extends JFrame {
  private JTextField rollNoField, internalMarksField, externalMarksField;
  private JComboBox<String> subjectCodeCombo;
  private JButton searchBtn, updateBtn, cancelBtn;

  public UpdateMarksScreen() {
    setTitle("Student Result Management System - Update Marks");
    setSize(550, 400);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(new Color(230, 240, 250));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Header Title
    JLabel titleLabel = new JLabel("Update Marks Screen", SwingConstants.CENTER);
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
    gbc.weightx = 1.0;

    Dimension fieldSize = new Dimension(280, 28);
    Dimension btnSize = new Dimension(100, 32);

    // Roll No Row
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.3;
    gbc.anchor = GridBagConstraints.WEST;
    JLabel searchLabel = new JLabel("Roll No (Search):");
    searchLabel.setFont(new Font("Arial", Font.BOLD, 13));
    formPanel.add(searchLabel, gbc);

    // Search Field and Button Sub-panel to keep them together inline properly
    JPanel searchSubPanel = new JPanel(new BorderLayout(5, 0));
    searchSubPanel.setBackground(new Color(230, 240, 250));

    rollNoField = new JTextField();
    rollNoField.setPreferredSize(new Dimension(175, 28)); // leave room for button
    searchSubPanel.add(rollNoField, BorderLayout.CENTER);

    searchBtn = new JButton("Search");
    searchBtn.setPreferredSize(new Dimension(100, 28));
    searchBtn.setBackground(new Color(60, 120, 200));
    searchBtn.setForeground(Color.WHITE);
    searchBtn.setOpaque(true);
    searchBtn.setBorderPainted(false);
    searchBtn.addActionListener(e -> searchStudentMarks());
    searchSubPanel.add(searchBtn, BorderLayout.EAST);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    formPanel.add(searchSubPanel, gbc);

    // Other Fields (initially disabled until searched)
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
    subjectCodeCombo.setEnabled(false);

    internalMarksField = new JTextField();
    internalMarksField.setPreferredSize(fieldSize);
    internalMarksField.setEnabled(false);

    externalMarksField = new JTextField();
    externalMarksField.setPreferredSize(fieldSize);
    externalMarksField.setEnabled(false);

    addField(formPanel, gbc, "Subject Code:", subjectCodeCombo, 1);
    addField(formPanel, gbc, "Internal Marks:", internalMarksField, 2);
    addField(formPanel, gbc, "External Marks:", externalMarksField, 3);

    mainPanel.add(formPanel, BorderLayout.CENTER);

    // Buttons Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.setBackground(new Color(230, 240, 250));

    updateBtn = new JButton("Update");
    updateBtn.setPreferredSize(btnSize);
    updateBtn.setBackground(new Color(50, 100, 200));
    updateBtn.setForeground(Color.WHITE);
    updateBtn.setOpaque(true);
    updateBtn.setBorderPainted(false);
    updateBtn.setEnabled(false);

    cancelBtn = new JButton("Cancel");
    cancelBtn.setPreferredSize(btnSize);

    updateBtn.addActionListener(e -> updateMarks());
    cancelBtn.addActionListener(e -> dispose());

    buttonPanel.add(updateBtn);
    buttonPanel.add(cancelBtn);

    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(mainPanel);
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int y) {
    gbc.gridx = 0;
    gbc.gridy = y;
    gbc.gridwidth = 1;
    gbc.weightx = 0.3;
    gbc.anchor = GridBagConstraints.WEST;

    JLabel jlbl = new JLabel(label);
    jlbl.setFont(new Font("Arial", Font.BOLD, 13));
    panel.add(jlbl, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(comp, gbc);
  }

  private void searchStudentMarks() {
    String rollNo = rollNoField.getText().trim();
    if (rollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter Roll No to search.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT * FROM marks WHERE roll_no = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
          String fetchedSubject = rs.getString("subject_code");
          // Attempt to select the specific subject from DB, or fallback to index 0 if not
          // found
          subjectCodeCombo.setSelectedItem(fetchedSubject);
          if (subjectCodeCombo.getSelectedIndex() == -1) {
            subjectCodeCombo.addItem(fetchedSubject);
            subjectCodeCombo.setSelectedItem(fetchedSubject);
          }

          internalMarksField.setText(String.valueOf(rs.getInt("internal_marks")));
          externalMarksField.setText(String.valueOf(rs.getInt("external_marks")));

          subjectCodeCombo.setEnabled(true);
          internalMarksField.setEnabled(true);
          externalMarksField.setEnabled(true);
          updateBtn.setEnabled(true);
          rollNoField.setEnabled(false); // Lock role number
          searchBtn.setEnabled(false);
        } else {
          JOptionPane.showMessageDialog(this, "Marks not found for this Roll No.", "Info",
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Searching: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateMarks() {
    String rollNo = rollNoField.getText().trim();
    String subjectCode = (String) subjectCodeCombo.getSelectedItem();

    try {
      int internal = Integer.parseInt(internalMarksField.getText().trim());
      int external = Integer.parseInt(externalMarksField.getText().trim());

      if (subjectCode == null || subjectCode.equals("- Select Subject -")) {
        JOptionPane.showMessageDialog(this, "Subject Code cannot be empty.", "Validation Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Validate marks range
      if (internal < 0 || internal > 100 || external < 0 || external > 100) {
        JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100!", "Input Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      int total = internal + external;
      double percentage = (total / 100.0) * 100.0;

      String status = (internal >= 15 && external >= 35) ? "Pass" : "Fail";
      String grade = status.equals("Pass") ? calculateGrade(percentage) : "F";

      try (Connection conn = DatabaseManager.getConnection()) {
        String sql = "UPDATE marks SET subject_code=?, internal_marks=?, external_marks=?, total_marks=?, percentage=?, grade=?, status=? WHERE roll_no=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, subjectCode);
          pstmt.setInt(2, internal);
          pstmt.setInt(3, external);
          pstmt.setInt(4, total);
          pstmt.setDouble(5, percentage);
          pstmt.setString(6, grade);
          pstmt.setString(7, status);
          pstmt.setString(8, rollNo);

          int count = pstmt.executeUpdate();
          if (count > 0) {
            JOptionPane.showMessageDialog(this, "Marks Updated Successfully!");
            dispose();
          } else {
            JOptionPane.showMessageDialog(this, "Failed to update marks.");
          }
        }
      }
    } catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Marks must be numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error updating: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private String calculateGrade(double percentage) {
    if (percentage >= 90)
      return "A+";
    if (percentage >= 80)
      return "A";
    if (percentage >= 70)
      return "B";
    if (percentage >= 60)
      return "C";
    if (percentage >= 50)
      return "D";
    if (percentage >= 40)
      return "E";
    return "F";
  }
}
