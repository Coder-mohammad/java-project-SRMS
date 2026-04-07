package com.srms;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings("serial")
public class UpdateStudentScreen extends JFrame {
  private JTextField rollNoField, nameField;
  private JComboBox<String> branchCombo, semesterCombo;
  private String originalRollNo;
  private Runnable onUpdateSuccess; // Callback to refresh manage screen

  public UpdateStudentScreen(String rollNo, Runnable onUpdateSuccess) {
    this.originalRollNo = rollNo;
    this.onUpdateSuccess = onUpdateSuccess;
    setTitle("Student Result Management System - Update Student");
    setSize(400, 300);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(230, 240, 250));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Header Title
    JLabel titleLabel = new JLabel("Update Student Details");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(new Color(200, 220, 240));
    titleLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    panel.add(titleLabel, gbc);

    // Fields
    rollNoField = new JTextField(15);
    rollNoField.setEditable(false); // Do not allow changing roll number
    nameField = new JTextField(15);
    branchCombo = new JComboBox<>(
        new String[] { "Computer Science", "Information Technology", "Electronics", "Mechanical",
            "Artificial Intelligence", "Civil Engineering", "Data Science", "Mechanical Engineering" });
    semesterCombo = new JComboBox<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" });

    addField(panel, gbc, "Roll No:", rollNoField, 1);
    addField(panel, gbc, "Name:", nameField, 2);
    addField(panel, gbc, "Branch:", branchCombo, 3);
    addField(panel, gbc, "Semester:", semesterCombo, 4);

    // Buttons Panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(230, 240, 250));
    JButton updateBtn = new JButton("Update");
    updateBtn.setBackground(new Color(50, 100, 200));
    updateBtn.setForeground(Color.WHITE);
    updateBtn.setOpaque(true);
    updateBtn.setBorderPainted(false);

    JButton cancelBtn = new JButton("Cancel");

    updateBtn.addActionListener(e -> updateStudent());
    cancelBtn.addActionListener(e -> dispose());

    buttonPanel.add(updateBtn);
    buttonPanel.add(cancelBtn);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(buttonPanel, gbc);

    add(panel);
    
    // Load existing data
    loadStudentData();
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int y) {
    gbc.gridx = 0;
    gbc.gridy = y;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    panel.add(new JLabel(label), gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(comp, gbc);
  }
  
  private void loadStudentData() {
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT roll_no, name, course, semester FROM student WHERE roll_no = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, originalRollNo);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          rollNoField.setText(rs.getString("roll_no"));
          nameField.setText(rs.getString("name"));
          branchCombo.setSelectedItem(rs.getString("course"));
          semesterCombo.setSelectedItem(String.valueOf(rs.getInt("semester")));
        } else {
          JOptionPane.showMessageDialog(this, "Student not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
          dispose();
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Fetching Data: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateStudent() {
    String name = nameField.getText().trim();
    String course = (String) branchCombo.getSelectedItem();
    String semester = (String) semesterCombo.getSelectedItem();

    if (name.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Error: Name cannot be empty", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "UPDATE student SET name = ?, course = ?, semester = ? WHERE roll_no = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, name);
        pstmt.setString(2, course);
        pstmt.setInt(3, Integer.parseInt(semester));
        pstmt.setString(4, originalRollNo);

        int rows = pstmt.executeUpdate();
        if (rows > 0) {
          JOptionPane.showMessageDialog(this, "Student Details Updated Successfully!");
          if (onUpdateSuccess != null) {
            onUpdateSuccess.run();
          }
          dispose();
        } else {
          JOptionPane.showMessageDialog(this, "Failed to update student.", "Update Error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Updating Student: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
