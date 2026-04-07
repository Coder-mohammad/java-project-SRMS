package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class TopperRankScreen extends JFrame {

  private DefaultTableModel model;
  private JComboBox<String> branchBox, semBox;

  public TopperRankScreen() {
    setTitle("SRMS - Rank List / Toppers");
    setSize(750, 480);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JLabel title = new JLabel("Student Rank List / Toppers", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // Filters
    JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filterRow.setBackground(ThemeManager.getBg());
    filterRow.add(fl("Branch:"));
    branchBox = new JComboBox<>(new String[]{
        "All","Computer Science","Information Technology","Electronics","Mechanical",
        "Artificial Intelligence","Civil Engineering","Data Science","Mechanical Engineering"});
    branchBox.addActionListener(e -> load());
    filterRow.add(branchBox);
    filterRow.add(fl("  Semester:"));
    semBox = new JComboBox<>(new String[]{"All","1","2","3","4","5","6","7","8"});
    semBox.addActionListener(e -> load());
    filterRow.add(semBox);
    JButton ref = new JButton("Refresh"); ref.addActionListener(e -> load());
    filterRow.add(ref);

    // Branch admins see only their own branch
    if (!AppSession.isSuperAdmin()) {
      branchBox.setSelectedItem(AppSession.branch);
      branchBox.setEnabled(false);
    }
    main.add(filterRow, BorderLayout.BEFORE_FIRST_LINE);

    // Table
    String[] cols = {"Rank","Roll No","Student Name","Branch","Semester","Total Marks","Avg Marks","Status"};
    model = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model) {
      @Override
      public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
        Component c = super.prepareRenderer(r, row, col);
        Object rankObj = model.getValueAt(row, 0);
        if (rankObj instanceof Integer) {
          int rank = (Integer) rankObj;
          if (!isRowSelected(row)) {
            if (rank == 1) c.setBackground(new Color(255, 215, 0));   // Gold
            else if (rank == 2) c.setBackground(new Color(192, 192, 192)); // Silver
            else if (rank == 3) c.setBackground(new Color(205, 127, 50)); // Bronze
            else c.setBackground(table.getBackground());
          }
        }
        return c;
      }
      private JTable table = this;
    };
    table.setFillsViewportHeight(true);
    table.setFont(new Font("Arial", Font.PLAIN, 13));
    table.setRowHeight(24);
    table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
    table.getTableHeader().setBackground(ThemeManager.getHeaderBg());

    // Center rank column
    DefaultTableCellRenderer center = new DefaultTableCellRenderer();
    center.setHorizontalAlignment(JLabel.CENTER);
    table.getColumnModel().getColumn(0).setCellRenderer(center);

    main.add(new JScrollPane(table), BorderLayout.CENTER);

    JButton close = new JButton("Close"); close.addActionListener(e -> dispose());
    JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bot.setBackground(ThemeManager.getBg()); bot.add(close);
    main.add(bot, BorderLayout.SOUTH);

    add(main);
    load();
  }

  private void load() {
    model.setRowCount(0);
    String branch = (String) branchBox.getSelectedItem();
    String sem    = (String) semBox.getSelectedItem();

    String where = " WHERE 1=1";
    if (branch != null && !branch.equals("All")) where += " AND s.course='" + branch + "'";
    if (sem    != null && !sem.equals("All"))    where += " AND s.semester=" + sem;

    String sql =
        "SELECT s.roll_no, s.name, s.course, s.semester," +
        " SUM(m.total_marks) AS tot, AVG(m.total_marks) AS avg," +
        " MAX(CASE WHEN m.status='Fail' THEN 'FAIL' ELSE 'PASS' END) AS overall " +
        "FROM marks m JOIN student s ON m.roll_no=s.roll_no" + where +
        " GROUP BY s.roll_no, s.name, s.course, s.semester ORDER BY tot DESC";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
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
        row.add(rs.getString("overall"));
        model.addRow(row);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private JLabel fl(String t) {
    JLabel l = new JLabel(t); l.setForeground(ThemeManager.getFg()); return l;
  }
}
