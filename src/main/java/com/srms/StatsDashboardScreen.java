package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class StatsDashboardScreen extends JFrame {

  private JLabel totalVal, passVal, failVal, avgVal;
  private DefaultTableModel topModel;
  private JComboBox<String> branchBox, semBox;

  public StatsDashboardScreen() {
    setTitle("SRMS - Statistics Dashboard");
    setSize(750, 560);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

    JLabel title = new JLabel("Statistics Dashboard", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // ── Filters ───────────────────────────────────────────────────────────────
    JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filterRow.setBackground(ThemeManager.getBg());
    filterRow.add(fl("Branch:"));
    branchBox = new JComboBox<>(new String[]{
        "All","Computer Science","Information Technology","Electronics","Mechanical",
        "Artificial Intelligence","Civil Engineering","Data Science","Mechanical Engineering"});
    branchBox.addActionListener(e -> loadStats());
    filterRow.add(branchBox);
    filterRow.add(fl("  Semester:"));
    semBox = new JComboBox<>(new String[]{"All","1","2","3","4","5","6","7","8"});
    semBox.addActionListener(e -> loadStats());
    filterRow.add(semBox);
    JButton ref = new JButton("Refresh");
    ref.addActionListener(e -> loadStats());
    filterRow.add(ref);

    // Branch admins see only their own branch
    if (!AppSession.isSuperAdmin()) {
      branchBox.setSelectedItem(AppSession.branch);
      branchBox.setEnabled(false);
    }

    // ── Stat cards ───────────────────────────────────────────────────────────
    JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
    cards.setBackground(ThemeManager.getBg());
    totalVal = bigLbl("0"); passVal = bigLbl("0");
    failVal  = bigLbl("0"); avgVal  = bigLbl("0.0");
    cards.add(card("Total Students", totalVal, new Color(60,120,200)));
    cards.add(card("Passed",         passVal,  new Color(50,160,80)));
    cards.add(card("Failed",         failVal,  new Color(200,60,60)));
    cards.add(card("Avg Marks",      avgVal,   new Color(180,120,20)));

    JPanel top = new JPanel(new BorderLayout(5, 5));
    top.setBackground(ThemeManager.getBg());
    top.add(filterRow, BorderLayout.NORTH);
    top.add(cards, BorderLayout.CENTER);

    // ── Top performers table ──────────────────────────────────────────────────
    JLabel tLabel = new JLabel("Top 10 Performers", SwingConstants.LEFT);
    tLabel.setFont(new Font("Arial", Font.BOLD, 14));
    tLabel.setForeground(ThemeManager.getFg());

    String[] cols = {"Rank","Roll No","Student Name","Branch","Semester","Total","Average"};
    topModel = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(topModel);
    table.setFillsViewportHeight(true);
    table.setFont(new Font("Arial", Font.PLAIN, 12));
    table.setRowHeight(22);

    JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
    tablePanel.setBackground(ThemeManager.getBg());
    tablePanel.add(tLabel, BorderLayout.NORTH);
    tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, tablePanel);
    split.setDividerLocation(170);
    split.setDividerSize(4);
    split.setContinuousLayout(true);
    main.add(split, BorderLayout.CENTER);

    JButton close = new JButton("Close");
    close.addActionListener(e -> dispose());
    JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bot.setBackground(ThemeManager.getBg());
    bot.add(close);
    main.add(bot, BorderLayout.SOUTH);

    add(main);
    loadStats();
  }

  private void loadStats() {
    String branch = (String) branchBox.getSelectedItem();
    String sem    = (String) semBox.getSelectedItem();

    String where = " WHERE 1=1";
    if (branch != null && !branch.equals("All")) where += " AND s.course='" + branch + "'";
    if (sem    != null && !sem.equals("All"))    where += " AND s.semester=" + sem;

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt  = conn.createStatement()) {

      ResultSet rs;

      rs = stmt.executeQuery(
          "SELECT COUNT(DISTINCT s.roll_no) FROM student s" + where);
      if (rs.next()) totalVal.setText(String.valueOf(rs.getInt(1)));

      rs = stmt.executeQuery(
          "SELECT COUNT(DISTINCT m.roll_no) FROM marks m JOIN student s ON m.roll_no=s.roll_no" +
          where + " AND m.status='Pass'");
      if (rs.next()) passVal.setText(String.valueOf(rs.getInt(1)));

      rs = stmt.executeQuery(
          "SELECT COUNT(DISTINCT m.roll_no) FROM marks m JOIN student s ON m.roll_no=s.roll_no" +
          where + " AND m.status='Fail'");
      if (rs.next()) failVal.setText(String.valueOf(rs.getInt(1)));

      rs = stmt.executeQuery(
          "SELECT AVG(m.total_marks) FROM marks m JOIN student s ON m.roll_no=s.roll_no" + where);
      if (rs.next()) {
        double a = rs.getDouble(1);
        avgVal.setText(rs.wasNull() ? "N/A" : String.format("%.1f", a));
      }

      // Top 10 performers
      topModel.setRowCount(0);
      rs = stmt.executeQuery(
          "SELECT s.roll_no, s.name, s.course, s.semester," +
          " SUM(m.total_marks) AS tot, AVG(m.total_marks) AS avg " +
          "FROM marks m JOIN student s ON m.roll_no=s.roll_no" + where +
          " GROUP BY s.roll_no, s.name, s.course, s.semester" +
          " ORDER BY tot DESC LIMIT 10");
      int rank = 1;
      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        row.add(rank++);
        row.add(rs.getString("roll_no"));
        row.add(rs.getString("name"));
        row.add(rs.getString("course"));
        row.add(rs.getInt("semester"));
        row.add(rs.getInt("tot"));
        row.add(String.format("%.1f", rs.getDouble("avg")));
        topModel.addRow(row);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private JPanel card(String label, JLabel valueLabel, Color accent) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(ThemeManager.isDark() ? ThemeManager.DARK_CARD : Color.WHITE);
    p.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
        BorderFactory.createEmptyBorder(12, 8, 12, 8)));
    JLabel lbl = new JLabel(label, SwingConstants.CENTER);
    lbl.setFont(new Font("Arial", Font.PLAIN, 12));
    lbl.setForeground(ThemeManager.getFg());
    p.add(lbl, BorderLayout.NORTH);
    p.add(valueLabel, BorderLayout.CENTER);
    return p;
  }

  private JLabel bigLbl(String txt) {
    JLabel l = new JLabel(txt, SwingConstants.CENTER);
    l.setFont(new Font("Arial", Font.BOLD, 30));
    return l;
  }

  private JLabel fl(String t) {
    JLabel l = new JLabel(t);
    l.setForeground(ThemeManager.getFg());
    return l;
  }
}
