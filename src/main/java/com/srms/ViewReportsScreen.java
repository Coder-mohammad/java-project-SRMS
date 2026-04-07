package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class ViewReportsScreen extends JFrame {

  private DefaultTableModel model;
  private JComboBox<String> branchFilterCombo, semesterFilterCombo;
  private JTextField nameFilterField;
  private JTable table;

  private static final String[] COL_NAMES = {
      "Roll Number","Student Name","Course Branch","Semester",
      "Subject Code","Total Marks","Grade","Result Status","Feedback","Published"
  };

  public ViewReportsScreen() {
    setTitle("SRMS - View Reports");
    setSize(850, 500);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(ThemeManager.getBg());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // ── Top: title + filters ─────────────────────────────────────────────────
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBackground(ThemeManager.getBg());

    JLabel titleLabel = new JLabel("View Reports Screen");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(ThemeManager.getHeaderBg());
    titleLabel.setForeground(ThemeManager.getFg());
    titleLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    titleLabel.setPreferredSize(new Dimension(0, 30));
    topPanel.add(titleLabel, BorderLayout.NORTH);

    JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filtersPanel.setBackground(ThemeManager.getBg());

    filtersPanel.add(lbl("Name:"));
    nameFilterField = new JTextField(10);
    nameFilterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData(); }
      public void removeUpdate (javax.swing.event.DocumentEvent e) { loadData(); }
      public void insertUpdate (javax.swing.event.DocumentEvent e) { loadData(); }
    });
    filtersPanel.add(nameFilterField);

    filtersPanel.add(lbl("Branch:"));
    branchFilterCombo = new JComboBox<>(new String[]{
        "All","Computer Science","Information Technology","Electronics","Mechanical",
        "Artificial Intelligence","Civil Engineering","Data Science","Mechanical Engineering"});
    branchFilterCombo.addActionListener(e -> loadData());
    filtersPanel.add(branchFilterCombo);

    filtersPanel.add(lbl("Semester:"));
    semesterFilterCombo = new JComboBox<>(new String[]{"All","1","2","3","4","5","6","7","8"});
    semesterFilterCombo.addActionListener(e -> loadData());
    filtersPanel.add(semesterFilterCombo);

    // Branch admins see only their own branch
    if (!AppSession.isSuperAdmin()) {
      branchFilterCombo.setSelectedItem(AppSession.branch);
      branchFilterCombo.setEnabled(false);
    }

    topPanel.add(filtersPanel, BorderLayout.SOUTH);
    panel.add(topPanel, BorderLayout.NORTH);

    // ── Table ────────────────────────────────────────────────────────────────
    model = new DefaultTableModel(COL_NAMES, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    loadData();
    panel.add(new JScrollPane(table), BorderLayout.CENTER);

    // ── Bottom: action buttons ────────────────────────────────────────────────
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBackground(ThemeManager.getBg());

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    btnRow.setBackground(ThemeManager.getBg());

    btnRow.add(actionBtn("Export CSV",         new Color(30,130,60),  e -> exportToCSV()));
    btnRow.add(actionBtn("Print Result Card",  new Color(60,100,200), e -> printResultCard()));
    btnRow.add(actionBtn("Toggle Publish",     new Color(160,100,20), e -> togglePublish()));
    btnRow.add(actionBtn("Back",               new Color(50,100,200), e -> dispose()));

    bottomPanel.add(btnRow, BorderLayout.NORTH);

    JLabel footerLabel = new JLabel("Select a row to Print Result Card or Toggle Publish status", SwingConstants.CENTER);
    footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    footerLabel.setForeground(ThemeManager.getFg());
    bottomPanel.add(footerLabel, BorderLayout.SOUTH);

    panel.add(bottomPanel, BorderLayout.SOUTH);
    add(panel);
  }

  // ── SQL data loader ──────────────────────────────────────────────────────────

  private void loadData() {
    model.setRowCount(0);
    String branch = (String) branchFilterCombo.getSelectedItem();
    String sem    = (String) semesterFilterCombo.getSelectedItem();
    String name   = nameFilterField.getText().trim();

    String sql =
        "SELECT s.roll_no, s.name, s.course, s.semester, m.subject_code, " +
        "m.total_marks, m.grade, m.status, r.comment, m.published " +
        "FROM student s " +
        "JOIN marks m ON s.roll_no = m.roll_no " +
        "LEFT JOIN student_reviews r ON m.roll_no = r.roll_no AND m.subject_code = r.subject_code " +
        "WHERE 1=1";
    if (branch != null && !branch.equals("All")) sql += " AND s.course='" + branch + "'";
    if (sem    != null && !sem.equals("All"))    sql += " AND s.semester=" + sem;
    if (!name.isEmpty())                         sql += " AND s.name LIKE '%" + name + "%'";
    sql += " ORDER BY s.roll_no";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        row.add(rs.getString("roll_no"));
        row.add(rs.getString("name"));
        row.add(rs.getString("course"));
        row.add(rs.getString("semester"));
        row.add(rs.getString("subject_code"));
        row.add(rs.getInt("total_marks"));
        row.add(rs.getString("grade"));
        row.add(rs.getString("status"));
        String fb = rs.getString("comment");
        row.add(fb != null ? fb : "No Feedback");
        row.add(rs.getInt("published") == 1 ? "Yes" : "No");
        model.addRow(row);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ── Export CSV ───────────────────────────────────────────────────────────────

  private void exportToCSV() {
    if (model.getRowCount() == 0) {
      JOptionPane.showMessageDialog(this, "No data to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    JFileChooser fc = new JFileChooser();
    fc.setSelectedFile(new File("SRMS_Results.csv"));
    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File f = fc.getSelectedFile();
      try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
        // header
        pw.println(String.join(",", COL_NAMES));
        // rows
        for (int r = 0; r < model.getRowCount(); r++) {
          StringBuilder sb = new StringBuilder();
          for (int c = 0; c < model.getColumnCount(); c++) {
            if (c > 0) sb.append(",");
            String v = model.getValueAt(r, c) != null ? model.getValueAt(r, c).toString() : "";
            if (v.contains(",") || v.contains("\"")) v = "\"" + v.replace("\"", "\"\"") + "\"";
            sb.append(v);
          }
          pw.println(sb);
        }
        DatabaseManager.logAction("EXPORT_CSV", "Results exported to " + f.getName());
        JOptionPane.showMessageDialog(this, "Exported to:\n" + f.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  // ── Print Result Card ────────────────────────────────────────────────────────

  private void printResultCard() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Please select a row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    String rollNo = (String) model.getValueAt(row, 0);
    new ResultCardScreen(rollNo).setVisible(true);
  }

  // ── Toggle Publish ────────────────────────────────────────────────────────────

  private void togglePublish() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Please select a row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    String rollNo  = (String) model.getValueAt(row, 0);
    String subject = (String) model.getValueAt(row, 4);
    String current = (String) model.getValueAt(row, 9);
    int    newVal  = current.equals("Yes") ? 0 : 1;
    String label   = newVal == 1 ? "Published" : "Unpublished";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "UPDATE marks SET published=? WHERE roll_no=? AND subject_code=?")) {
      ps.setInt(1, newVal);
      ps.setString(2, rollNo);
      ps.setString(3, subject);
      ps.executeUpdate();
      DatabaseManager.logAction("TOGGLE_PUBLISH",
          rollNo + " / " + subject + " -> " + label);
      JOptionPane.showMessageDialog(this, "Result marked as: " + label);
      loadData();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private JLabel lbl(String t) {
    JLabel l = new JLabel(t);
    l.setForeground(ThemeManager.getFg());
    return l;
  }

  private JButton actionBtn(String text, Color bg, java.awt.event.ActionListener al) {
    JButton b = new JButton(text);
    b.setBackground(bg); b.setForeground(Color.WHITE);
    b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
    b.addActionListener(al);
    return b;
  }
}
