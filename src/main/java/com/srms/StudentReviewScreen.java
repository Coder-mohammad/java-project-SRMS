package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

@SuppressWarnings("serial")
public class StudentReviewScreen extends JFrame {
  private JTextField rollNoField, nameField, gradeField;
  private JComboBox<String> subjectCombo, reviewTypeCombo;
  private JTextArea commentArea;
  private JButton searchBtn, saveBtn, clearBtn, backBtn;
  private DefaultTableModel tableModel;
  private JTable historyTable;

  public StudentReviewScreen() {
    setTitle("Student Result Management System - Student Review & Comment");
    setSize(600, 650);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBackground(new Color(230, 240, 250));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Form Panel (Top)
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(new Color(230, 240, 250));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Header Title
    JLabel titleLabel = new JLabel("Add Student Review & Comment", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(new Color(200, 220, 240));
    titleLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    formPanel.add(titleLabel, gbc);

    // Roll Number Search
    gbc.gridwidth = 1;
    gbc.gridy = 1;
    formPanel.add(new JLabel("Roll Number:"), gbc);
    rollNoField = new JTextField(15);
    gbc.gridx = 1;
    formPanel.add(rollNoField, gbc);
    searchBtn = createStyledButton("Search");
    searchBtn.addActionListener(e -> searchStudent());
    gbc.gridx = 2;
    formPanel.add(searchBtn, gbc);

    // Other Form Fields
    nameField = new JTextField(20);
    nameField.setEditable(false);

    subjectCombo = new JComboBox<>(new String[] { "- Select Subject -" });
    subjectCombo.setEnabled(false);
    subjectCombo.addActionListener(e -> updateGradeForSubject());

    gradeField = new JTextField(10);
    gradeField.setEditable(false);

    reviewTypeCombo = new JComboBox<>(new String[] { "Excellent", "Good", "Average", "Needs Improvement", "Poor" });
    reviewTypeCombo.setEnabled(false);

    commentArea = new JTextArea(4, 20);
    commentArea.setLineWrap(true);
    commentArea.setWrapStyleWord(true);
    commentArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    commentArea.setEnabled(false);
    JScrollPane commentScroll = new JScrollPane(commentArea);

    addField(formPanel, gbc, "Student Name:", nameField, 2);
    addField(formPanel, gbc, "Subject:", subjectCombo, 3);
    addField(formPanel, gbc, "Grade:", gradeField, 4);
    addField(formPanel, gbc, "Review Type:", reviewTypeCombo, 5);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    formPanel.add(new JLabel("Comment / Feedback:"), gbc);
    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    formPanel.add(commentScroll, gbc);

    // Buttons Panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
    buttonPanel.setBackground(new Color(230, 240, 250));

    saveBtn = createStyledButton("Save Review");
    saveBtn.setEnabled(false);
    saveBtn.addActionListener(e -> saveReview());

    clearBtn = createStyledButton("Clear Form");
    clearBtn.addActionListener(e -> clearForm());

    backBtn = createStyledButton("Back");
    backBtn.addActionListener(e -> dispose());

    buttonPanel.add(saveBtn);
    buttonPanel.add(clearBtn);
    buttonPanel.add(backBtn);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 3;
    formPanel.add(buttonPanel, gbc);

    mainPanel.add(formPanel, BorderLayout.NORTH);

    // History Table Panel (Bottom)
    JPanel historyPanel = new JPanel(new BorderLayout());
    JLabel historyTitle = new JLabel("Previous Reviews History", SwingConstants.LEFT);
    historyTitle.setFont(new Font("Arial", Font.BOLD, 12));
    historyTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    historyPanel.add(historyTitle, BorderLayout.NORTH);

    String[] columns = { "Subject", "Grade", "Review Type", "Feedback" };
    tableModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    historyTable = new JTable(tableModel);
    historyTable.setFillsViewportHeight(true);
    JScrollPane tableScroll = new JScrollPane(historyTable);
    historyPanel.add(tableScroll, BorderLayout.CENTER);

    mainPanel.add(historyPanel, BorderLayout.CENTER);

    add(mainPanel);
  }

  private JButton createStyledButton(String text) {
    JButton btn = new JButton(text);
    btn.setBackground(new Color(50, 100, 200));
    btn.setForeground(Color.WHITE);
    btn.setOpaque(true);
    btn.setBorderPainted(false);
    return btn;
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String label, JComponent comp, int y) {
    gbc.gridx = 0;
    gbc.gridy = y;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.EAST;
    panel.add(new JLabel(label), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(comp, gbc);
  }

  private void searchStudent() {
    String rollNo = rollNoField.getText().trim();
    if (rollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter a Roll Number.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      // 1. Fetch Student Name
      String studentSql = "SELECT name FROM student WHERE roll_no = ?";
      try (PreparedStatement checkStmt = conn.prepareStatement(studentSql)) {
        checkStmt.setString(1, rollNo);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
          nameField.setText(rs.getString("name"));
        } else {
          JOptionPane.showMessageDialog(this, "Error: Student ID does not exist", "Validation Error",
              JOptionPane.ERROR_MESSAGE);
          clearForm();
          return;
        }
      }

      // 2. Clear old subjects & Load Subjects for this student
      subjectCombo.removeAllItems();
      subjectCombo.addItem("- Select Subject -");
      String marksSql = "SELECT subject_code FROM marks WHERE roll_no = ?";
      boolean hasMarks = false;
      try (PreparedStatement pstmt = conn.prepareStatement(marksSql)) {
        pstmt.setString(1, rollNo);
        ResultSet rsMarks = pstmt.executeQuery();
        while (rsMarks.next()) {
          subjectCombo.addItem(rsMarks.getString("subject_code"));
          hasMarks = true;
        }
      }

      if (hasMarks) {
        subjectCombo.setEnabled(true);
        reviewTypeCombo.setEnabled(true);
        commentArea.setEnabled(true);
        saveBtn.setEnabled(true);
        rollNoField.setEnabled(false);
        loadReviewHistory(rollNo);
      } else {
        JOptionPane.showMessageDialog(this, "No marks have been entered for this student yet.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
      }

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Searching: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateGradeForSubject() {
    String selectedSub = (String) subjectCombo.getSelectedItem();
    if (selectedSub == null || selectedSub.equals("- Select Subject -")) {
      gradeField.setText("");
      return;
    }

    String rollNo = rollNoField.getText().trim();
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT grade FROM marks WHERE roll_no = ? AND subject_code = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        pstmt.setString(2, selectedSub);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          gradeField.setText(rs.getString("grade"));
        }
      }
    } catch (Exception ex) {
      System.err.println("Error fetching grade: " + ex.getMessage());
    }
  }

  private void loadReviewHistory(String rollNo) {
    tableModel.setRowCount(0);
    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "SELECT subject_code, grade, review_type, comment FROM student_reviews WHERE roll_no = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
          Vector<Object> row = new Vector<>();
          row.add(rs.getString("subject_code"));
          row.add(rs.getString("grade"));
          row.add(rs.getString("review_type"));
          row.add(rs.getString("comment"));
          tableModel.addRow(row);
        }
      }
    } catch (Exception ex) {
      System.err.println("Error fetching history: " + ex.getMessage());
    }
  }

  private void saveReview() {
    String rollNo = rollNoField.getText().trim();
    String subject = (String) subjectCombo.getSelectedItem();
    String grade = gradeField.getText().trim();
    String reviewType = (String) reviewTypeCombo.getSelectedItem();
    String comment = commentArea.getText().trim();

    if (subject == null || subject.equals("- Select Subject -")) {
      JOptionPane.showMessageDialog(this, "Please select a Subject.", "Validation Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (comment.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter feedback commentary.", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try (Connection conn = DatabaseManager.getConnection()) {
      String sql = "INSERT INTO student_reviews (roll_no, subject_code, grade, review_type, comment) VALUES (?, ?, ?, ?, ?)";
      try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, rollNo);
        pstmt.setString(2, subject);
        pstmt.setString(3, grade);
        pstmt.setString(4, reviewType);
        pstmt.setString(5, comment);
        pstmt.executeUpdate();
      }
      JOptionPane.showMessageDialog(this, "Review saved successfully!");
      loadReviewHistory(rollNo);
      commentArea.setText("");
      subjectCombo.setSelectedIndex(0);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error saving review: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearForm() {
    rollNoField.setEnabled(true);
    rollNoField.setText("");
    nameField.setText("");

    subjectCombo.removeAllItems();
    subjectCombo.addItem("- Select Subject -");
    subjectCombo.setEnabled(false);

    gradeField.setText("");
    reviewTypeCombo.setEnabled(false);
    reviewTypeCombo.setSelectedIndex(0);

    commentArea.setText("");
    commentArea.setEnabled(false);

    saveBtn.setEnabled(false);
    tableModel.setRowCount(0);
  }
}
