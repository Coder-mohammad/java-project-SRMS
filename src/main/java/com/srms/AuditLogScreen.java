package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class AuditLogScreen extends JFrame {

  private DefaultTableModel model;
  private JTextField searchField;

  public AuditLogScreen() {
    setTitle("SRMS - Audit Log");
    setSize(780, 480);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JLabel title = new JLabel("System Audit Log", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // Search bar
    JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchRow.setBackground(ThemeManager.getBg());
    JLabel sl = new JLabel("Filter by Action:");
    sl.setForeground(ThemeManager.getFg());
    searchRow.add(sl);
    searchField = new JTextField(20);
    searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      public void changedUpdate(javax.swing.event.DocumentEvent e) { load(); }
      public void removeUpdate (javax.swing.event.DocumentEvent e) { load(); }
      public void insertUpdate (javax.swing.event.DocumentEvent e) { load(); }
    });
    searchRow.add(searchField);
    JButton ref = new JButton("Refresh All"); ref.addActionListener(e -> { searchField.setText(""); load(); });
    searchRow.add(ref);
    JButton clearLog = new JButton("Clear Log");
    clearLog.setBackground(new Color(180, 40, 40)); clearLog.setForeground(Color.WHITE);
    clearLog.setOpaque(true); clearLog.setBorderPainted(false);
    clearLog.addActionListener(e -> clearAuditLog());
    searchRow.add(clearLog);
    main.add(searchRow, BorderLayout.BEFORE_FIRST_LINE);

    // Table
    String[] cols = {"#","Action","Details","Date & Time"};
    model = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setFont(new Font("Arial", Font.PLAIN, 12));
    table.setRowHeight(22);
    table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    table.getTableHeader().setBackground(ThemeManager.getHeaderBg());
    // Widen columns
    table.getColumnModel().getColumn(0).setMaxWidth(40);
    table.getColumnModel().getColumn(1).setPreferredWidth(130);
    table.getColumnModel().getColumn(2).setPreferredWidth(380);
    table.getColumnModel().getColumn(3).setPreferredWidth(160);
    main.add(new JScrollPane(table), BorderLayout.CENTER);

    // Footer
    JButton close = new JButton("Close"); close.addActionListener(e -> dispose());
    JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bot.setBackground(ThemeManager.getBg()); bot.add(close);
    main.add(bot, BorderLayout.SOUTH);

    add(main);
    load();
  }

  private void load() {
    model.setRowCount(0);
    String keyword = searchField.getText().trim();
    String sql = "SELECT id, action, details, performed_at FROM audit_log";
    if (!keyword.isEmpty()) sql += " WHERE action LIKE '%" + keyword + "%' OR details LIKE '%" + keyword + "%'";
    sql += " ORDER BY performed_at DESC";

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt  = conn.createStatement();
         ResultSet rs    = stmt.executeQuery(sql)) {
      int row = 1;
      while (rs.next()) {
        Vector<Object> r = new Vector<>();
        r.add(row++);
        r.add(rs.getString("action"));
        r.add(rs.getString("details"));
        r.add(rs.getString("performed_at"));
        model.addRow(r);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearAuditLog() {
    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to clear the entire audit log?\nThis cannot be undone.",
        "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirm != JOptionPane.YES_OPTION) return;
    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt  = conn.createStatement()) {
      stmt.execute("DELETE FROM audit_log");
      model.setRowCount(0);
      JOptionPane.showMessageDialog(this, "Audit log cleared.", "Done", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}
