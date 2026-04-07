package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

@SuppressWarnings("serial")
public class AttendanceScreen extends JFrame {

  private JTextField rollNoField, nameField;
  private JComboBox<String> subjectCombo, statusCombo;
  private JSpinner dateSpinner;
  private JButton searchBtn, saveBtn, clearBtn;
  private DefaultTableModel histModel;
  private static final String[] SUBJECTS = {
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

  public AttendanceScreen() {
    setTitle("SRMS - Attendance Tracking");
    setSize(680, 580);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JLabel title = new JLabel("Attendance Tracking", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // ── Form ─────────────────────────────────────────────────────────────────
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(ThemeManager.getBg());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(7, 8, 7, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Roll no + Search
    gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
    gbc.fill=GridBagConstraints.NONE;
    form.add(fl("Roll No:"), gbc);
    rollNoField = new JTextField(15);
    rollNoField.setPreferredSize(new Dimension(220, 30));
    gbc.gridx=1; gbc.anchor=GridBagConstraints.WEST; gbc.fill=GridBagConstraints.HORIZONTAL;
    form.add(rollNoField, gbc);
    searchBtn = styledBtn("Search", ThemeManager.getAccent());
    searchBtn.setPreferredSize(new Dimension(100, 30));
    searchBtn.addActionListener(e -> searchStudent());
    gbc.gridx=2; gbc.fill=GridBagConstraints.NONE; gbc.anchor=GridBagConstraints.WEST;
    form.add(searchBtn, gbc);

    // Name (readonly)
    gbc.gridx=0; gbc.gridy=1; gbc.anchor=GridBagConstraints.EAST;
    form.add(fl("Name:"), gbc);
    nameField = new JTextField(20); nameField.setEditable(false);
    gbc.gridx=1; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.WEST;
    form.add(nameField, gbc);

    // Subject
    gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
    form.add(fl("Subject:"), gbc);
    subjectCombo = new JComboBox<>(SUBJECTS); subjectCombo.setEnabled(false);
    gbc.gridx=1; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.WEST;
    form.add(subjectCombo, gbc);

    // Date
    gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
    form.add(fl("Date:"), gbc);
    dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor de = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(de); dateSpinner.setEnabled(false);
    gbc.gridx=1; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.WEST;
    form.add(dateSpinner, gbc);

    // Status
    gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=1; gbc.anchor=GridBagConstraints.EAST;
    form.add(fl("Status:"), gbc);
    statusCombo = new JComboBox<>(new String[]{"Present","Absent","Late"});
    statusCombo.setEnabled(false);
    gbc.gridx=1; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.WEST;
    form.add(statusCombo, gbc);

    // Buttons — all same size
    Dimension btnSize = new Dimension(140, 32);
    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
    btnRow.setBackground(ThemeManager.getBg());

    saveBtn = styledBtn("Save Attendance", new Color(50,150,50));
    saveBtn.setPreferredSize(btnSize);
    saveBtn.setEnabled(false);
    saveBtn.addActionListener(e -> saveAttendance());

    clearBtn = styledBtn("Clear", new Color(150,60,60));
    clearBtn.setPreferredSize(btnSize);
    clearBtn.addActionListener(e -> clearForm());

    JButton backBtn = styledBtn("Back", new Color(60,100,200));
    backBtn.setPreferredSize(btnSize);
    backBtn.addActionListener(e -> dispose());

    btnRow.add(saveBtn); btnRow.add(clearBtn); btnRow.add(backBtn);
    gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=3;
    gbc.anchor=GridBagConstraints.CENTER; gbc.fill=GridBagConstraints.NONE;
    form.add(btnRow, gbc);

    main.add(form, BorderLayout.CENTER);

    // ── History table ────────────────────────────────────────────────────────
    String[] cols = {"Subject","Date","Status"};
    histModel = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable hist = new JTable(histModel);
    hist.setFillsViewportHeight(true); hist.setFont(new Font("Arial", Font.PLAIN, 12));
    hist.setRowHeight(22);

    JPanel histPanel = new JPanel(new BorderLayout(5, 5));
    histPanel.setBackground(ThemeManager.getBg());
    JLabel hl = new JLabel("Attendance History", SwingConstants.LEFT);
    hl.setFont(new Font("Arial", Font.BOLD, 13)); hl.setForeground(ThemeManager.getFg());
    histPanel.add(hl, BorderLayout.NORTH);
    histPanel.add(new JScrollPane(hist), BorderLayout.CENTER);
    histPanel.setPreferredSize(new Dimension(0, 200));
    main.add(histPanel, BorderLayout.SOUTH);

    add(main);
  }

  private void searchStudent() {
    String rn = rollNoField.getText().trim();
    if (rn.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Enter a Roll Number.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT name FROM student WHERE roll_no=?")) {
      ps.setString(1, rn);
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) {
        JOptionPane.showMessageDialog(this, "Student not found!", "Error", JOptionPane.ERROR_MESSAGE);
        clearForm(); return;
      }
      nameField.setText(rs.getString("name"));
      subjectCombo.setEnabled(true); dateSpinner.setEnabled(true);
      statusCombo.setEnabled(true);  saveBtn.setEnabled(true);
      rollNoField.setEnabled(false);
      loadHistory(rn);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void saveAttendance() {
    String rn = rollNoField.getText().trim();
    String sub = (String) subjectCombo.getSelectedItem();
    if (sub == null || sub.startsWith("- ")) {
      JOptionPane.showMessageDialog(this, "Select a subject.", "Warning", JOptionPane.WARNING_MESSAGE);
      return;
    }
    java.util.Date d = (java.util.Date) dateSpinner.getValue();
    String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
    String status = (String) statusCombo.getSelectedItem();

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO attendance (roll_no, subject_code, attendance_date, status) VALUES (?,?,?,?)")) {
      ps.setString(1, rn); ps.setString(2, sub);
      ps.setString(3, date); ps.setString(4, status);
      ps.executeUpdate();
      DatabaseManager.logAction("ATTENDANCE", rn + " | " + sub + " | " + date + " | " + status);
      JOptionPane.showMessageDialog(this, "Attendance saved!");
      loadHistory(rn);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void loadHistory(String rn) {
    histModel.setRowCount(0);
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "SELECT subject_code, attendance_date, status FROM attendance WHERE roll_no=? ORDER BY attendance_date DESC")) {
      ps.setString(1, rn);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        row.add(rs.getString("subject_code"));
        row.add(rs.getString("attendance_date"));
        row.add(rs.getString("status"));
        histModel.addRow(row);
      }
    } catch (Exception ex) {
      System.err.println("loadHistory: " + ex.getMessage());
    }
  }

  private void clearForm() {
    rollNoField.setEnabled(true); rollNoField.setText("");
    nameField.setText("");
    subjectCombo.setSelectedIndex(0); subjectCombo.setEnabled(false);
    dateSpinner.setEnabled(false); statusCombo.setEnabled(false);
    saveBtn.setEnabled(false); histModel.setRowCount(0);
  }

  private JButton styledBtn(String t, Color bg) {
    JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
    b.setOpaque(true); b.setBorderPainted(false); return b;
  }

  private JLabel fl(String t) {
    JLabel l = new JLabel(t); l.setForeground(ThemeManager.getFg()); return l;
  }
}
