package com.srms;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

@SuppressWarnings("serial")
public class AddStudentScreen extends JFrame {
  private JTextField rollNoField, nameField, sectionField;
  private JComboBox<String> branchCombo, semesterCombo;

  public AddStudentScreen() {
    setTitle("Student Result Management System - Add Student");
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
    JLabel titleLabel = new JLabel("Add Student Screen");
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
    nameField = new JTextField(15);
    branchCombo = new JComboBox<>(
        new String[] { "Computer Science", "Information Technology", "Electronics", "Mechanical",
            "Artificial Intelligence", "Civil Engineering", "Data Science", "Mechanical Engineering" });

    // Branch admins can only add students to their own branch
    if (!AppSession.isSuperAdmin()) {
      branchCombo.setSelectedItem(AppSession.branch);
      branchCombo.setEnabled(false);
    }
    semesterCombo = new JComboBox<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" });
    sectionField = new JTextField(15);

    addField(panel, gbc, "Roll No:", rollNoField, 1);
    addField(panel, gbc, "Name:", nameField, 2);
    addField(panel, gbc, "Branch:", branchCombo, 3);
    addField(panel, gbc, "Semester:", semesterCombo, 4);
    addField(panel, gbc, "Section:", sectionField, 5); // Section is in UI but not in DB schema, we will ignore it in
                                                       // insert

    // Buttons Panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(230, 240, 250));
    JButton saveBtn = new JButton("Save");
    saveBtn.setBackground(new Color(50, 100, 200));
    saveBtn.setForeground(Color.WHITE);
    saveBtn.setOpaque(true);
    saveBtn.setBorderPainted(false);

    JButton cancelBtn = new JButton("Cancel");

    saveBtn.addActionListener(e -> saveStudent());
    cancelBtn.addActionListener(e -> dispose());

    buttonPanel.add(saveBtn);
    buttonPanel.add(cancelBtn);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(buttonPanel, gbc);

    add(panel);
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

  private void saveStudent() {
    String rollNo = rollNoField.getText().trim();
    String name = nameField.getText().trim();
    String course = (String) branchCombo.getSelectedItem();
    String semester = (String) semesterCombo.getSelectedItem();

    if (rollNo.isEmpty() || name.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Error: Roll No and Name cannot be empty", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO student (roll_no, name, course, semester) VALUES (?, ?, ?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        pstmt.setString(2, name);
        pstmt.setString(3, course);
        pstmt.setInt(4, Integer.parseInt(semester));

        pstmt.executeUpdate();
      }
      JOptionPane.showMessageDialog(this, "Student Added Successfully!");
      dispose();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Saving Student: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
