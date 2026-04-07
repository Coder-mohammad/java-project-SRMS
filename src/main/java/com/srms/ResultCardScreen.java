package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;

@SuppressWarnings("serial")
public class ResultCardScreen extends JFrame implements Printable {

  private final String rollNo;
  private JPanel cardPanel;

  public ResultCardScreen(String rollNo) {
    this.rollNo = rollNo;
    setTitle("Result Card - " + rollNo);
    setSize(680, 680);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(Color.WHITE);
    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    cardPanel = buildCard();
    main.add(new JScrollPane(cardPanel), BorderLayout.CENTER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
    btnRow.setBackground(Color.WHITE);
    JButton printBtn = new JButton("\uD83D\uDDA8 Print / Save as PDF");
    printBtn.setBackground(new Color(50, 100, 200));
    printBtn.setForeground(Color.WHITE); printBtn.setOpaque(true); printBtn.setBorderPainted(false);
    printBtn.setFont(new Font("Arial", Font.BOLD, 13));
    printBtn.addActionListener(e -> printCard());

    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> dispose());

    btnRow.add(printBtn); btnRow.add(closeBtn);
    main.add(btnRow, BorderLayout.SOUTH);
    add(main);
  }

  // ── Build the visual card ────────────────────────────────────────────────────

  private JPanel buildCard() {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(60, 120, 200), 2),
        BorderFactory.createEmptyBorder(20, 30, 20, 30)));

    addCentered(card, new Font("Arial", Font.BOLD, 18), new Color(20, 60, 140),
        "STUDENT RESULT MANAGEMENT SYSTEM");
    addCentered(card, new Font("Arial", Font.ITALIC, 12), Color.GRAY,
        "Official Marksheet / Result Card");

    card.add(Box.createVerticalStrut(12));
    card.add(separator());
    card.add(Box.createVerticalStrut(12));

    try (Connection conn = DatabaseManager.getConnection()) {
      // Student info
      String sql = "SELECT name, course, semester FROM student WHERE roll_no=?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
          infoRow(card, "Roll Number:",    rollNo);
          infoRow(card, "Student Name:",   rs.getString("name"));
          infoRow(card, "Course / Branch:",rs.getString("course"));
          infoRow(card, "Semester:",       String.valueOf(rs.getInt("semester")));
        }
      }

      card.add(Box.createVerticalStrut(15));

      JLabel ml = new JLabel("Subject-wise Marks:");
      ml.setFont(new Font("Arial", Font.BOLD, 13));
      ml.setAlignmentX(Component.LEFT_ALIGNMENT);
      card.add(ml);
      card.add(Box.createVerticalStrut(6));

      // Marks table
      String[] cols = {"Subject Code","Internal","External","Total","Grade","Status"};
      DefaultTableModel tm = new DefaultTableModel(cols, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
      };
      int totalSum = 0; int subjCount = 0;

      String mSql = "SELECT subject_code, internal_marks, external_marks, total_marks, grade, status " +
                    "FROM marks WHERE roll_no=?";
      try (PreparedStatement ps = conn.prepareStatement(mSql)) {
        ps.setString(1, rollNo);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          tm.addRow(new Object[]{
              rs.getString("subject_code"),
              rs.getInt("internal_marks"), rs.getInt("external_marks"),
              rs.getInt("total_marks"),    rs.getString("grade"), rs.getString("status")});
          totalSum += rs.getInt("total_marks");
          subjCount++;
        }
      }

      JTable t = new JTable(tm);
      t.setFont(new Font("Arial", Font.PLAIN, 12));
      t.setRowHeight(24);
      t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
      t.getTableHeader().setBackground(new Color(200, 220, 240));
      JScrollPane sp = new JScrollPane(t);
      sp.setAlignmentX(Component.LEFT_ALIGNMENT);
      sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(80, t.getRowHeight() * (tm.getRowCount() + 1) + 30)));
      card.add(sp);

      if (subjCount > 0) {
        card.add(Box.createVerticalStrut(12));
        infoRow(card, "Total Marks (Sum):", String.valueOf(totalSum));
        infoRow(card, "Average Marks:", String.format("%.1f", (double) totalSum / subjCount));
      }

    } catch (Exception ex) {
      JLabel err = new JLabel("Error: " + ex.getMessage());
      err.setForeground(Color.RED);
      card.add(err);
    }

    card.add(Box.createVerticalStrut(15));
    card.add(separator());
    card.add(Box.createVerticalStrut(6));
    addCentered(card, new Font("Arial", Font.ITALIC, 10), Color.GRAY,
        "Generated by SRMS v2.0  |  " + new java.util.Date());
    return card;
  }

  // ── Printing ─────────────────────────────────────────────────────────────────

  private void printCard() {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(this);
    if (job.printDialog()) {
      try {
        job.print();
        DatabaseManager.logAction("PRINT_RESULT_CARD", "Result card printed for: " + rollNo);
      } catch (PrinterException ex) {
        JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  @Override
  public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
    if (pageIndex > 0) return NO_SUCH_PAGE;
    Graphics2D g2 = (Graphics2D) g;
    g2.translate(pf.getImageableX(), pf.getImageableY());
    double scale = Math.min(
        pf.getImageableWidth()  / cardPanel.getWidth(),
        pf.getImageableHeight() / cardPanel.getHeight());
    g2.scale(scale, scale);
    cardPanel.printAll(g2);
    return PAGE_EXISTS;
  }

  // ── UI helpers ───────────────────────────────────────────────────────────────

  private void addCentered(JPanel p, Font font, Color color, String text) {
    JLabel l = new JLabel(text, SwingConstants.CENTER);
    l.setFont(font); l.setForeground(color);
    l.setAlignmentX(Component.CENTER_ALIGNMENT);
    p.add(l);
  }

  private void infoRow(JPanel p, String label, String value) {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
    row.setBackground(Color.WHITE);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel lbl = new JLabel(label); lbl.setFont(new Font("Arial", Font.BOLD, 13));
    lbl.setPreferredSize(new Dimension(160, 25));
    JLabel val = new JLabel(value); val.setFont(new Font("Arial", Font.PLAIN, 13));
    row.add(lbl); row.add(val); p.add(row);
  }

  private JSeparator separator() {
    JSeparator s = new JSeparator();
    s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
    return s;
  }
}
