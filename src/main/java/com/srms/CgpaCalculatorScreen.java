package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class CgpaCalculatorScreen extends JFrame {

  private JTextField rollNoField, nameField;
  private JButton searchBtn;
  private DefaultTableModel semModel, subjectModel;
  private JLabel cgpaLabel;

  public CgpaCalculatorScreen() {
    setTitle("SRMS - CGPA / SGPA Calculator");
    setSize(720, 560);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    // Title
    JLabel title = new JLabel("CGPA / SGPA Calculator", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // Search row
    JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchRow.setBackground(ThemeManager.getBg());
    searchRow.add(fl("Roll No:"));
    rollNoField = new JTextField(15);
    searchRow.add(rollNoField);
    searchBtn = new JButton("Calculate");
    searchBtn.setBackground(ThemeManager.getAccent()); searchBtn.setForeground(Color.WHITE);
    searchBtn.setOpaque(true); searchBtn.setBorderPainted(false);
    searchBtn.addActionListener(e -> calculate());
    searchRow.add(searchBtn);
    searchRow.add(fl("   Name:"));
    nameField = new JTextField(18); nameField.setEditable(false);
    searchRow.add(nameField);
    main.add(searchRow, BorderLayout.BEFORE_FIRST_LINE);

    // CGPA display
    cgpaLabel = new JLabel("CGPA: —", SwingConstants.CENTER);
    cgpaLabel.setFont(new Font("Arial", Font.BOLD, 22));
    cgpaLabel.setForeground(new Color(30, 130, 80));

    // Subject-wise table
    String[] subCols = {"Subject Code","Internal","External","Total","Grade","Grade Points"};
    subjectModel = new DefaultTableModel(subCols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable subTable = new JTable(subjectModel);
    subTable.setFillsViewportHeight(true); subTable.setFont(new Font("Arial", Font.PLAIN, 12));
    subTable.setRowHeight(22);
    subTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

    // SGPA per semester table
    String[] semCols = {"Semester","Total Marks","Avg Marks","SGPA (10-pt)"};
    semModel = new DefaultTableModel(semCols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable semTable = new JTable(semModel);
    semTable.setFillsViewportHeight(true); semTable.setFont(new Font("Arial", Font.PLAIN, 12));
    semTable.setRowHeight(22);
    semTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

    JPanel cgpaRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
    cgpaRow.setBackground(ThemeManager.getBg()); cgpaRow.add(cgpaLabel);

    JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
    leftPanel.setBackground(ThemeManager.getBg());
    JLabel subTitle = new JLabel("All Subject Marks", SwingConstants.LEFT);
    subTitle.setFont(new Font("Arial", Font.BOLD, 13)); subTitle.setForeground(ThemeManager.getFg());
    leftPanel.add(subTitle, BorderLayout.NORTH);
    leftPanel.add(new JScrollPane(subTable), BorderLayout.CENTER);

    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.setBackground(ThemeManager.getBg());
    JLabel semTitle = new JLabel("Semester-wise SGPA", SwingConstants.LEFT);
    semTitle.setFont(new Font("Arial", Font.BOLD, 13)); semTitle.setForeground(ThemeManager.getFg());
    rightPanel.add(semTitle, BorderLayout.NORTH);
    rightPanel.add(new JScrollPane(semTable), BorderLayout.CENTER);

    JSplitPane splitH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
    splitH.setDividerLocation(380); splitH.setContinuousLayout(true);

    JPanel center = new JPanel(new BorderLayout(5, 8));
    center.setBackground(ThemeManager.getBg());
    center.add(cgpaRow, BorderLayout.NORTH);
    center.add(splitH, BorderLayout.CENTER);
    main.add(center, BorderLayout.CENTER);

    JButton close = new JButton("Close"); close.addActionListener(e -> dispose());
    JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bot.setBackground(ThemeManager.getBg()); bot.add(close);
    main.add(bot, BorderLayout.SOUTH);

    add(main);
  }

  private void calculate() {
    String rollNo = rollNoField.getText().trim();
    if (rollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter a Roll Number.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
    subjectModel.setRowCount(0);
    semModel.setRowCount(0);
    cgpaLabel.setText("CGPA: —");

    try (Connection conn = DatabaseManager.getConnection()) {
      // Student info
      try (PreparedStatement ps = conn.prepareStatement(
          "SELECT name FROM student WHERE roll_no=?")) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
          JOptionPane.showMessageDialog(this, "Student not found!", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        nameField.setText(rs.getString("name"));
      }

      // Marks + per-semester SGPA
      // grade -> grade point mapping
      // A+=10, A=9, B=8, C=7, D=6, E=5, F=0
      String mSql = "SELECT m.subject_code, m.internal_marks, m.external_marks, m.total_marks, m.grade, s.semester " +
                    "FROM marks m JOIN student s ON m.roll_no=s.roll_no " +
                    "WHERE m.roll_no=? ORDER BY s.semester, m.subject_code";
      try (PreparedStatement ps = conn.prepareStatement(mSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();

        java.util.Map<Integer, java.util.List<double[]>> semMap = new java.util.LinkedHashMap<>();
        // each entry in list: [total_marks, grade_points]
        while (rs.next()) {
          String grade = rs.getString("grade");
          double gp = gradePoint(grade);
          int total  = rs.getInt("total_marks");
          int sem    = rs.getInt("semester");

          subjectModel.addRow(new Object[]{
              rs.getString("subject_code"),
              rs.getInt("internal_marks"), rs.getInt("external_marks"),
              total, grade, String.format("%.1f", gp)
          });
          semMap.computeIfAbsent(sem, k -> new java.util.ArrayList<>())
                .add(new double[]{total, gp});
        }

        // Build SGPA rows + compute CGPA
        double cgpaSum = 0; int semCount = 0;
        for (java.util.Map.Entry<Integer, java.util.List<double[]>> e : semMap.entrySet()) {
          int semester = e.getKey();
          java.util.List<double[]> rows = e.getValue();
          double totSum = 0, gpSum = 0;
          for (double[] d : rows) { totSum += d[0]; gpSum += d[1]; }
          double avg  = totSum / rows.size();
          double sgpa = gpSum  / rows.size();
          semModel.addRow(new Object[]{
              semester, (int) totSum, String.format("%.1f", avg), String.format("%.2f", sgpa)
          });
          cgpaSum += sgpa; semCount++;
        }

        if (semCount > 0) {
          double cgpa = cgpaSum / semCount;
          cgpaLabel.setText(String.format("CGPA: %.2f / 10.00", cgpa));
        } else {
          cgpaLabel.setText("No marks found.");
        }
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private double gradePoint(String grade) {
    if (grade == null) return 0;
    switch (grade) {
      case "A+": return 10;
      case "A":  return 9;
      case "B":  return 8;
      case "C":  return 7;
      case "D":  return 6;
      case "E":  return 5;
      default:   return 0; // F
    }
  }

  private JLabel fl(String t) {
    JLabel l = new JLabel(t); l.setForeground(ThemeManager.getFg()); return l;
  }
}
