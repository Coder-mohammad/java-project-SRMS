package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.Vector;

@SuppressWarnings("serial")
public class StudentSearchScreen extends JFrame implements Printable {

  // ── Search / student info fields ─────────────────────────────────────────────
  private JTextField rollNoField;
  private JLabel nameLabel, courseLabel, semesterLabel;
  private JLabel totalMarksLabel, avgLabel, cgpaLabel, overallGradeLabel, overallStatusLabel;

  // ── Marks table ──────────────────────────────────────────────────────────────
  private JTable resultTable;
  private DefaultTableModel model;

  // ── Attendance table ─────────────────────────────────────────────────────────
  private JTable attendTable;
  private DefaultTableModel attendModel;

  // ── Reviews table ────────────────────────────────────────────────────────────
  private JTable reviewTable;
  private DefaultTableModel reviewModel;

  // ── Holds current roll no ────────────────────────────────────────────────────
  private String currentRollNo = "";

  // ── CAPTCHA ───────────────────────────────────────────────────────────────────
  private int captchaAnswer = 0;
  private String captchaQuestion = "";
  private final Random captchaRandom = new Random();

  // ─────────────────────────────────────────────────────────────────────────────

  public StudentSearchScreen() {
    setTitle("Student Result Inquiry Portal");
    setSize(860, 680);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout(10, 10));
    root.setBackground(new Color(235, 243, 255));
    root.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));

    // ── Header Banner ─────────────────────────────────────────────────────────
    JPanel banner = new JPanel(new BorderLayout());
    banner.setBackground(new Color(30, 80, 160));
    banner.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

    JLabel bannerTitle = new JLabel("Student Result Inquiry Portal", SwingConstants.CENTER);
    bannerTitle.setFont(new Font("Arial", Font.BOLD, 20));
    bannerTitle.setForeground(Color.WHITE);
    banner.add(bannerTitle, BorderLayout.CENTER);

    JLabel subTag = new JLabel("SRMS v2.0", SwingConstants.RIGHT);
    subTag.setFont(new Font("Arial", Font.ITALIC, 11));
    subTag.setForeground(new Color(180, 210, 255));
    banner.add(subTag, BorderLayout.EAST);
    root.add(banner, BorderLayout.NORTH);

    // ── Search Row ────────────────────────────────────────────────────────────
    JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
    searchRow.setBackground(new Color(235, 243, 255));

    JLabel rl = new JLabel("Enter Roll Number:");
    rl.setFont(new Font("Arial", Font.BOLD, 13));
    searchRow.add(rl);

    rollNoField = new JTextField(18);
    rollNoField.setFont(new Font("Arial", Font.PLAIN, 14));
    rollNoField.setPreferredSize(new Dimension(200, 32));
    searchRow.add(rollNoField);

    JButton searchBtn = actionBtn("Search", new Color(30, 80, 160));
    searchBtn.setFont(new Font("Arial", Font.BOLD, 13));
    searchBtn.addActionListener(e -> searchResult());
    searchRow.add(searchBtn);

    JButton clearBtn = actionBtn("Clear", new Color(120, 120, 120));
    clearBtn.addActionListener(e -> clearAll());
    searchRow.add(clearBtn);

    // Allow Enter key to search
    rollNoField.addActionListener(e -> searchResult());

    root.add(searchRow, BorderLayout.BEFORE_FIRST_LINE);

    // ── Centre: Student Card + Tabs ───────────────────────────────────────────
    JPanel centre = new JPanel(new BorderLayout(0, 10));
    centre.setBackground(new Color(235, 243, 255));

    // Student profile card
    centre.add(buildProfileCard(), BorderLayout.NORTH);

    // Tabbed pane
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("Arial", Font.BOLD, 13));

    // Tab 1 — Marks & Grades
    tabs.addTab("📋 Marks & Grades", buildMarksPanel());

    // Tab 2 — Attendance
    tabs.addTab("📅 Attendance", buildAttendancePanel());

    // Tab 3 — Reviews & Feedback
    tabs.addTab("💬 Reviews & Feedback", buildReviewPanel());

    centre.add(tabs, BorderLayout.CENTER);
    root.add(centre, BorderLayout.CENTER);

    // ── Footer ────────────────────────────────────────────────────────────────
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
    footer.setBackground(new Color(235, 243, 255));

    JButton printBtn = actionBtn("🖨 Print Result Card", new Color(30, 120, 60));
    printBtn.addActionListener(e -> printResultCard());
    footer.add(printBtn);

    JButton csvBtn = actionBtn("💾 Export CSV", new Color(130, 80, 0));
    csvBtn.addActionListener(e -> exportCSV());
    footer.add(csvBtn);

    JButton backBtn = actionBtn("← Back to Login", new Color(80, 80, 80));
    backBtn.addActionListener(e -> { dispose(); new LoginScreen().setVisible(true); });
    footer.add(backBtn);

    root.add(footer, BorderLayout.SOUTH);
    add(root);
  }

  // ── Profile card with summary stats ──────────────────────────────────────────

  private JPanel buildProfileCard() {
    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(180, 210, 240), 1),
        BorderFactory.createEmptyBorder(10, 16, 10, 16)));

    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(3, 12, 3, 12);

    // Left block — identity
    JPanel left = new JPanel(new GridLayout(3, 2, 6, 4));
    left.setBackground(Color.WHITE);
    nameLabel    = statLbl("—"); courseLabel = statLbl("—"); semesterLabel = statLbl("—");
    left.add(bold("Name:"));       left.add(nameLabel);
    left.add(bold("Branch:"));     left.add(courseLabel);
    left.add(bold("Semester:"));   left.add(semesterLabel);

    // Right block — performance summary
    JPanel right = new JPanel(new GridLayout(3, 4, 6, 4));
    right.setBackground(Color.WHITE);
    totalMarksLabel = statLbl("—"); avgLabel = statLbl("—");
    cgpaLabel       = statLbl("—"); overallGradeLabel = statLbl("—");
    overallStatusLabel = new JLabel("—", SwingConstants.CENTER);
    overallStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));

    right.add(bold("Total Marks:")); right.add(totalMarksLabel);
    right.add(bold("Average:"));     right.add(avgLabel);
    right.add(bold("CGPA:"));        right.add(cgpaLabel);
    right.add(bold("Overall Grade:")); right.add(overallGradeLabel);
    right.add(bold("Result:"));       right.add(overallStatusLabel);
    right.add(new JLabel()); right.add(new JLabel()); // spacers

    g.gridx=0; g.gridy=0; g.anchor=GridBagConstraints.WEST;
    card.add(left, g);
    JSeparator sep = new JSeparator(JSeparator.VERTICAL);
    sep.setPreferredSize(new Dimension(2, 60));
    g.gridx=1; card.add(sep, g);
    g.gridx=2; card.add(right, g);

    return card;
  }

  // ── Marks tab ────────────────────────────────────────────────────────────────

  private JPanel buildMarksPanel() {
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBackground(Color.WHITE);
    p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    String[] cols = {"Subject Code","Internal","External","Total","%","Grade","Status","Feedback"};
    model = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    resultTable = new JTable(model) {
      @Override
      public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
        Component c = super.prepareRenderer(r, row, col);
        if (!isRowSelected(row)) {
          String status = model.getValueAt(row, 6) != null ? model.getValueAt(row, 6).toString() : "";
          c.setBackground("Pass".equalsIgnoreCase(status)
              ? new Color(220, 255, 225) : new Color(255, 225, 225));
        }
        return c;
      }
    };
    resultTable.setFont(new Font("Arial", Font.PLAIN, 12));
    resultTable.setRowHeight(24);
    resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    resultTable.getTableHeader().setBackground(new Color(200, 220, 245));
    resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    resultTable.getColumnModel().getColumn(0).setPreferredWidth(220);
    for (int i = 1; i <= 4; i++) resultTable.getColumnModel().getColumn(i).setPreferredWidth(65);
    resultTable.getColumnModel().getColumn(5).setPreferredWidth(55);
    resultTable.getColumnModel().getColumn(6).setPreferredWidth(65);
    resultTable.getColumnModel().getColumn(7).setPreferredWidth(220);

    p.add(new JScrollPane(resultTable,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
    return p;
  }

  // ── Attendance tab ───────────────────────────────────────────────────────────

  private JPanel buildAttendancePanel() {
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBackground(Color.WHITE);
    p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    String[] cols = {"Subject","Date","Status"};
    attendModel = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    attendTable = new JTable(attendModel) {
      @Override
      public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
        Component c = super.prepareRenderer(r, row, col);
        if (!isRowSelected(row)) {
          String s = attendModel.getValueAt(row, 2) != null ? attendModel.getValueAt(row, 2).toString() : "";
          if ("Present".equals(s))      c.setBackground(new Color(220, 255, 225));
          else if ("Absent".equals(s))  c.setBackground(new Color(255, 220, 220));
          else if ("Late".equals(s))    c.setBackground(new Color(255, 250, 210));
          else c.setBackground(Color.WHITE);
        }
        return c;
      }
    };
    attendTable.setFont(new Font("Arial", Font.PLAIN, 12));
    attendTable.setRowHeight(22);
    attendTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    attendTable.getTableHeader().setBackground(new Color(200, 220, 245));

    // Summary label
    JLabel attSummary = new JLabel("Search a student to view attendance.", SwingConstants.LEFT);
    attSummary.setFont(new Font("Arial", Font.ITALIC, 12));
    attSummary.setForeground(Color.GRAY);
    attSummary.setName("attSummary");

    p.add(attSummary, BorderLayout.NORTH);
    p.add(new JScrollPane(attendTable), BorderLayout.CENTER);
    return p;
  }

  // ── Reviews tab ──────────────────────────────────────────────────────────────

  private JPanel buildReviewPanel() {
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBackground(Color.WHITE);
    p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    String[] cols = {"Subject","Grade","Review Type","Feedback / Comment"};
    reviewModel = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    reviewTable = new JTable(reviewModel);
    reviewTable.setFont(new Font("Arial", Font.PLAIN, 12));
    reviewTable.setRowHeight(24);
    reviewTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    reviewTable.getTableHeader().setBackground(new Color(200, 220, 245));
    reviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    reviewTable.getColumnModel().getColumn(0).setPreferredWidth(220);
    reviewTable.getColumnModel().getColumn(1).setPreferredWidth(55);
    reviewTable.getColumnModel().getColumn(2).setPreferredWidth(130);
    reviewTable.getColumnModel().getColumn(3).setPreferredWidth(330);

    p.add(new JScrollPane(reviewTable,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
    return p;
  }

  // ── Search / data loading ────────────────────────────────────────────────────

  // ── CAPTCHA generation ───────────────────────────────────────────────────────

  private void generateCaptcha() {
    int a = captchaRandom.nextInt(20) + 1;   // 1–20
    int b = captchaRandom.nextInt(20) + 1;   // 1–20
    int op = captchaRandom.nextInt(3);        // 0=+, 1=−, 2=×
    switch (op) {
      case 0: captchaQuestion = a + " + " + b + " = ?"; captchaAnswer = a + b; break;
      case 1:
        // keep result positive
        if (a < b) { int tmp = a; a = b; b = tmp; }
        captchaQuestion = a + " - " + b + " = ?"; captchaAnswer = a - b; break;
      default: captchaQuestion = a + " × " + b + " = ?"; captchaAnswer = a * b; break;
    }
  }

  /** Shows CAPTCHA dialog; returns true only if user answers correctly. */
  private boolean showCaptchaDialog() {
    generateCaptcha();

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(new Color(235, 243, 255));
    panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 10, 20));

    JLabel infoLbl = new JLabel("Please solve the math problem to continue:", SwingConstants.CENTER);
    infoLbl.setFont(new Font("Arial", Font.PLAIN, 13));
    panel.add(infoLbl, BorderLayout.NORTH);

    JLabel questionLbl = new JLabel(captchaQuestion, SwingConstants.CENTER);
    questionLbl.setFont(new Font("Arial", Font.BOLD, 28));
    questionLbl.setForeground(new Color(20, 60, 140));
    questionLbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    panel.add(questionLbl, BorderLayout.CENTER);

    JTextField answerField = new JTextField(8);
    answerField.setFont(new Font("Arial", Font.BOLD, 16));
    answerField.setHorizontalAlignment(JTextField.CENTER);
    JPanel ansRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ansRow.setBackground(new Color(235, 243, 255));
    ansRow.add(new JLabel("Your answer: "));
    ansRow.add(answerField);
    panel.add(ansRow, BorderLayout.SOUTH);

    int result = JOptionPane.showConfirmDialog(
        this, panel, "🔒 CAPTCHA Verification",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result != JOptionPane.OK_OPTION) return false;

    String input = answerField.getText().trim();
    try {
      int userAnswer = Integer.parseInt(input);
      if (userAnswer == captchaAnswer) {
        return true;
      } else {
        JOptionPane.showMessageDialog(this,
            "❌ Incorrect answer! Expected: " + captchaAnswer + "\nPlease try again.",
            "CAPTCHA Failed", JOptionPane.WARNING_MESSAGE);
        return false;
      }
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,
          "Please enter a valid number.",
          "Invalid Input", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void searchResult() {
    String rollNo = rollNoField.getText().trim();
    if (rollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Please enter your Roll Number.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // ── CAPTCHA verification ─────────────────────────────────────────────────
    if (!showCaptchaDialog()) return;

    currentRollNo = rollNo;
    model.setRowCount(0);
    attendModel.setRowCount(0);
    reviewModel.setRowCount(0);

    try (Connection conn = DatabaseManager.getConnection()) {

      // ── 1. Student info ───────────────────────────────────────────────────
      String infoSql = "SELECT name, course, semester FROM student WHERE roll_no=?";
      try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
          JOptionPane.showMessageDialog(this,
              "No student found with Roll No: " + rollNo, "Not Found", JOptionPane.WARNING_MESSAGE);
          currentRollNo = "";
          return;
        }
        nameLabel.setText(rs.getString("name"));
        courseLabel.setText(rs.getString("course"));
        semesterLabel.setText(String.valueOf(rs.getInt("semester")));
      }

      // ── 2. Marks (only published) ─────────────────────────────────────────
      String marksSql =
          "SELECT m.subject_code, m.internal_marks, m.external_marks, m.total_marks, " +
          "       m.percentage, m.grade, m.status, r.comment " +
          "FROM marks m " +
          "LEFT JOIN student_reviews r ON m.roll_no=r.roll_no AND m.subject_code=r.subject_code " +
          "WHERE m.roll_no=? AND m.published=1";

      int totalSum = 0; int count = 0;
      boolean anyFail = false;
      double gpSum = 0;

      try (PreparedStatement ps = conn.prepareStatement(marksSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          String grade = rs.getString("grade");
          Vector<Object> row = new Vector<>();
          row.add(rs.getString("subject_code"));
          row.add(rs.getInt("internal_marks"));
          row.add(rs.getInt("external_marks"));
          row.add(rs.getInt("total_marks"));
          row.add(String.format("%.1f%%", rs.getDouble("percentage")));
          row.add(grade);
          row.add(rs.getString("status"));
          String fb = rs.getString("comment");
          row.add(fb != null ? fb : "—");
          model.addRow(row);

          totalSum += rs.getInt("total_marks");
          if ("Fail".equalsIgnoreCase(rs.getString("status"))) anyFail = true;
          gpSum += gradePoint(grade);
          count++;
        }
      }

      if (count > 0) {
        double avg  = (double) totalSum / count;
        double cgpa = gpSum / count;
        totalMarksLabel.setText(String.valueOf(totalSum));
        avgLabel.setText(String.format("%.1f", avg));
        cgpaLabel.setText(String.format("%.2f / 10", cgpa));
        overallGradeLabel.setText(gradeFromAvg(avg));
        overallStatusLabel.setText(anyFail ? "FAIL" : "PASS");
        overallStatusLabel.setForeground(anyFail ? new Color(200, 30, 30) : new Color(20, 140, 50));
      } else {
        totalMarksLabel.setText("—"); avgLabel.setText("—");
        cgpaLabel.setText("—"); overallGradeLabel.setText("—");
        overallStatusLabel.setText("Results not published yet");
        overallStatusLabel.setForeground(Color.GRAY);
      }

      // ── 3. Attendance ─────────────────────────────────────────────────────
      int present = 0, absent = 0, late = 0;
      String attSql = "SELECT subject_code, attendance_date, status FROM attendance WHERE roll_no=? ORDER BY attendance_date DESC";
      try (PreparedStatement ps = conn.prepareStatement(attSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          String s = rs.getString("status");
          if ("Present".equals(s)) present++;
          else if ("Absent".equals(s)) absent++;
          else if ("Late".equals(s)) late++;
          Vector<Object> row = new Vector<>();
          row.add(rs.getString("subject_code"));
          row.add(rs.getString("attendance_date"));
          row.add(s);
          attendModel.addRow(row);
        }
      }

      // ── 4. Reviews ────────────────────────────────────────────────────────
      String revSql = "SELECT subject_code, grade, review_type, comment FROM student_reviews WHERE roll_no=?";
      try (PreparedStatement ps = conn.prepareStatement(revSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          Vector<Object> row = new Vector<>();
          row.add(rs.getString("subject_code"));
          row.add(rs.getString("grade"));
          row.add(rs.getString("review_type"));
          String c = rs.getString("comment");
          row.add(c != null ? c : "—");
          reviewModel.addRow(row);
        }
      }

      // Update attendance summary label
      int total = present + absent + late;
      String pct = total > 0 ? String.format("%.0f%%", (present * 100.0 / total)) : "N/A";
      // (label referenced by name not accessible directly; update via text on related tab)

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ── Clear all ────────────────────────────────────────────────────────────────

  private void clearAll() {
    rollNoField.setText(""); currentRollNo = "";
    model.setRowCount(0); attendModel.setRowCount(0); reviewModel.setRowCount(0);
    nameLabel.setText("—"); courseLabel.setText("—"); semesterLabel.setText("—");
    totalMarksLabel.setText("—"); avgLabel.setText("—");
    cgpaLabel.setText("—"); overallGradeLabel.setText("—");
    overallStatusLabel.setText("—"); overallStatusLabel.setForeground(Color.BLACK);
  }

  // ── Print result card ─────────────────────────────────────────────────────────

  private void printResultCard() {
    if (currentRollNo.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Search a student first.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    new ResultCardScreen(currentRollNo).setVisible(true);
  }

  // ── Export CSV ────────────────────────────────────────────────────────────────

  private void exportCSV() {
    if (currentRollNo.isEmpty() || model.getRowCount() == 0) {
      JOptionPane.showMessageDialog(this, "Search a student with published results first.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    JFileChooser fc = new JFileChooser();
    fc.setSelectedFile(new File("Result_" + currentRollNo + ".csv"));
    if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
    try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
      pw.println("Roll No," + currentRollNo + ",Name," + nameLabel.getText());
      pw.println("Branch," + courseLabel.getText() + ",Semester," + semesterLabel.getText());
      pw.println();
      pw.println("Subject Code,Internal,External,Total,Percentage,Grade,Status,Feedback");
      for (int r = 0; r < model.getRowCount(); r++) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < model.getColumnCount(); c++) {
          if (c > 0) sb.append(",");
          String v = model.getValueAt(r, c) != null ? model.getValueAt(r, c).toString() : "";
          if (v.contains(",")) v = "\"" + v + "\"";
          sb.append(v);
        }
        pw.println(sb);
      }
      JOptionPane.showMessageDialog(this, "Exported to:\n" + fc.getSelectedFile().getAbsolutePath(), "Done", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ── Printable (for OS print dialog) ──────────────────────────────────────────

  @Override
  public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
    if (pageIndex > 0) return NO_SUCH_PAGE;
    Graphics2D g2 = (Graphics2D) g;
    g2.translate(pf.getImageableX(), pf.getImageableY());
    double scale = pf.getImageableWidth() / getWidth();
    g2.scale(scale, scale);
    printAll(g2);
    return PAGE_EXISTS;
  }

  // ── Grade helpers ─────────────────────────────────────────────────────────────

  private double gradePoint(String g) {
    if (g == null) return 0;
    switch (g) {
      case "A+": return 10; case "A": return 9; case "B": return 8;
      case "C":  return 7;  case "D": return 6; case "E": return 5;
      default:   return 0;
    }
  }

  private String gradeFromAvg(double avg) {
    if (avg >= 90) return "A+"; if (avg >= 80) return "A";
    if (avg >= 70) return "B";  if (avg >= 60) return "C";
    if (avg >= 50) return "D";  if (avg >= 40) return "E";
    return "F";
  }

  // ── UI helpers ────────────────────────────────────────────────────────────────

  private JButton actionBtn(String t, Color bg) {
    JButton b = new JButton(t);
    b.setBackground(bg); b.setForeground(Color.WHITE);
    b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
    return b;
  }

  private JLabel statLbl(String t) {
    JLabel l = new JLabel(t, SwingConstants.LEFT);
    l.setFont(new Font("Arial", Font.BOLD, 13));
    l.setForeground(new Color(30, 80, 160));
    return l;
  }

  private JLabel bold(String t) {
    JLabel l = new JLabel(t);
    l.setFont(new Font("Arial", Font.BOLD, 12));
    return l;
  }
}
