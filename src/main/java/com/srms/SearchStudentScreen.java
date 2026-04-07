package com.srms;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@SuppressWarnings("serial")
public class SearchStudentScreen extends JFrame {
  private JTextField rollNoField;
  private JTextArea resultArea;

  public SearchStudentScreen() {
    setTitle("Student Result Management System - Search Student");
    setSize(500, 400);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(new Color(230, 240, 250));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Top Search Panel
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    searchPanel.setBackground(new Color(230, 240, 250));

    searchPanel.add(new JLabel("Enter Roll No:"));
    rollNoField = new JTextField(15);
    searchPanel.add(rollNoField);

    JButton searchBtn = new JButton("Search");
    searchBtn.setBackground(new Color(60, 120, 200));
    searchBtn.setForeground(Color.WHITE);
    searchBtn.setOpaque(true);
    searchBtn.setBorderPainted(false);
    searchBtn.addActionListener(e -> performSearch());
    searchPanel.add(searchBtn);

    panel.add(searchPanel, BorderLayout.NORTH);

    // Results Area
    resultArea = new JTextArea();
    resultArea.setEditable(false);
    resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JScrollPane scrollPane = new JScrollPane(resultArea);
    panel.add(scrollPane, BorderLayout.CENTER);

    // Default message
    resultArea.setText("Enter Roll No above and click Search to view student details and marks.");
    resultArea.setCaretPosition(0);

    // Footer
    JPanel bottomPanel = new JPanel();
    bottomPanel.setBackground(new Color(230, 240, 250));
    JButton backBtn = new JButton("Back");
    backBtn.setBackground(new Color(50, 100, 200));
    backBtn.setForeground(Color.WHITE);
    backBtn.setOpaque(true);
    backBtn.setBorderPainted(false);
    backBtn.addActionListener(e -> dispose());
    bottomPanel.add(backBtn);
    panel.add(bottomPanel, BorderLayout.SOUTH);

    add(panel);
  }

  private void performSearch() {
    String rollNo = rollNoField.getText().trim();
    if (rollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter a Roll No to search.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT s.name, s.course, s.semester, m.subject_code, m.internal_marks, m.external_marks, m.total_marks, m.percentage, m.grade, m.status "
          +
          "FROM student s LEFT JOIN marks m ON s.roll_no = m.roll_no WHERE s.roll_no = ?";

      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
          StringBuilder sb = new StringBuilder();
          sb.append("--- Student Details ---\n");
          sb.append("Roll No: ").append(rollNo).append("\n");
          sb.append("Name:    ").append(rs.getString("name")).append("\n");
          sb.append("Branch:  ").append(rs.getString("course")).append("\n");
          sb.append("Sem:     ").append(rs.getInt("semester")).append("\n\n");

          String subject = rs.getString("subject_code");
          if (subject != null) {
            sb.append("--- Academic Record ---\n");
            sb.append("Subject Code:   ").append(subject).append("\n");
            sb.append("Internal Marks: ").append(rs.getInt("internal_marks")).append("\n");
            sb.append("External Marks: ").append(rs.getInt("external_marks")).append("\n");
            sb.append("Total Marks:    ").append(rs.getInt("total_marks")).append("\n");
            sb.append("Percentage:     ").append(rs.getDouble("percentage")).append("%\n");
            sb.append("Grade:          ").append(rs.getString("grade")).append("\n");
            sb.append("Status:         ").append(rs.getString("status")).append("\n");
          } else {
            sb.append("No marks recorded for this student yet.");
          }

          resultArea.setText(sb.toString());
          resultArea.setCaretPosition(0);
        } else {
          resultArea.setText("Student with Roll No '" + rollNo + "' not found.");
          resultArea.setCaretPosition(0);
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Searching: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
