package com.srms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class MainDashboard extends JFrame {

  public MainDashboard() {
    setTitle("SRMS - Admin Portal");
    setSize(640, AppSession.isSuperAdmin() ? 700 : 640);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
    buildUI();
  }

  private void buildUI() {
    getContentPane().removeAll();

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(ThemeManager.getBg());

    // ── Header ────────────────────────────────────────────────────────────────
    JPanel header = new JPanel(new BorderLayout(10, 0));
    header.setBackground(new Color(20, 60, 140));
    header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

    JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
    headerText.setOpaque(false);

    JLabel titleLbl = new JLabel("Admin Portal", SwingConstants.CENTER);
    titleLbl.setFont(new Font("Arial", Font.BOLD, 20));
    titleLbl.setForeground(Color.WHITE);

    String branchDisplay = AppSession.isSuperAdmin()
        ? "Super Admin  |  All Branches"
        : "Branch: " + AppSession.branch;
    JLabel subLbl = new JLabel("Logged in as: " + AppSession.username + "  |  " + branchDisplay, SwingConstants.CENTER);
    subLbl.setFont(new Font("Arial", Font.PLAIN, 11));
    subLbl.setForeground(new Color(180, 210, 255));

    headerText.add(titleLbl);
    headerText.add(subLbl);
    header.add(headerText, BorderLayout.CENTER);

    JButton dmBtn = new JButton(ThemeManager.isDark() ? "Light Mode" : "Dark Mode");
    dmBtn.setFont(new Font("Arial", Font.BOLD, 11));
    dmBtn.setBackground(new Color(40, 90, 180));
    dmBtn.setForeground(Color.WHITE);
    dmBtn.setOpaque(true); dmBtn.setBorderPainted(false); dmBtn.setFocusPainted(false);
    dmBtn.addActionListener(e -> { ThemeManager.toggle(); buildUI(); revalidate(); repaint(); });
    header.add(dmBtn, BorderLayout.EAST);
    root.add(header, BorderLayout.NORTH);

    // ── Content ───────────────────────────────────────────────────────────────
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBackground(ThemeManager.getBg());
    content.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

    // SECTION 1 — Student Management
    content.add(sectionHeader("Student Management", new Color(30, 90, 190)));
    content.add(Box.createVerticalStrut(6));
    content.add(buttonRow(
        menuBtn("Add Student",      new Color(40,110,220), e -> new AddStudentScreen().setVisible(true)),
        menuBtn("Manage Students",  new Color(40,110,220), e -> new ManageStudentsScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(5));
    content.add(buttonRow(
        menuBtn("Bulk Import (CSV)", new Color(40,110,220), e -> new BulkImportScreen().setVisible(true)),
        menuBtn("Search Student",    new Color(40,110,220), e -> new SearchStudentScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(14));

    // SECTION 2 — Marks & Results
    content.add(sectionHeader("Marks & Results", new Color(20, 120, 90)));
    content.add(Box.createVerticalStrut(6));
    content.add(buttonRow(
        menuBtn("Enter Marks",       new Color(25,140,100), e -> new MarksEntryScreen().setVisible(true)),
        menuBtn("Update Marks",      new Color(25,140,100), e -> new UpdateMarksScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(5));
    content.add(buttonRow(
        menuBtn("View Results",      new Color(25,140,100), e -> new ViewReportsScreen().setVisible(true)),
        menuBtn("Review & Comment",  new Color(25,140,100), e -> new StudentReviewScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(14));

    // SECTION 3 — Analytics & Reports
    content.add(sectionHeader("Analytics & Reports", new Color(130, 70, 10)));
    content.add(Box.createVerticalStrut(6));
    content.add(buttonRow(
        menuBtn("Statistics Dashboard", new Color(170,95,18), e -> new StatsDashboardScreen().setVisible(true)),
        menuBtn("Rank List / Toppers",  new Color(170,95,18), e -> new TopperRankScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(5));
    content.add(buttonRow(
        menuBtn("CGPA Calculator",       new Color(170,95,18), e -> new CgpaCalculatorScreen().setVisible(true)),
        menuBtn("Attendance Tracking",   new Color(170,95,18), e -> new AttendanceScreen().setVisible(true))
    ));
    content.add(Box.createVerticalStrut(14));

    // SECTION 4 — System & Administration
    content.add(sectionHeader("System & Administration", new Color(100, 50, 160)));
    content.add(Box.createVerticalStrut(6));
    content.add(buttonRow(
        menuBtn("Change Password", new Color(120,65,190), e -> new ChangePasswordScreen().setVisible(true)),
        // Audit Log: Super Admin only
        AppSession.isSuperAdmin()
            ? menuBtn("Audit Log", new Color(120,65,190), e -> new AuditLogScreen().setVisible(true))
            : new JLabel()
    ));
    // Manage Admins — Super Admin only
    if (AppSession.isSuperAdmin()) {
      content.add(Box.createVerticalStrut(5));
      content.add(buttonRow(
          menuBtn("Manage Admins", new Color(80,40,140), e -> new ManageAdminsScreen().setVisible(true)),
          new JLabel()
      ));
    }
    content.add(Box.createVerticalStrut(5));
    content.add(buttonRow(
        menuBtn("Logout", new Color(180,40,40), e -> {
          AppSession.clear();
          dispose();
          new LoginScreen().setVisible(true);
        }),
        new JLabel()
    ));
    content.add(Box.createVerticalStrut(8));

    JScrollPane scroll = new JScrollPane(content,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    root.add(scroll, BorderLayout.CENTER);

    setContentPane(root);
    revalidate();
    repaint();
  }

  private JPanel sectionHeader(String title, Color color) {
    JPanel bar = new JPanel(new BorderLayout());
    bar.setBackground(color);
    bar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    JLabel lbl = new JLabel(title);
    lbl.setFont(new Font("Arial", Font.BOLD, 13));
    lbl.setForeground(Color.WHITE);
    bar.add(lbl, BorderLayout.WEST);
    return bar;
  }

  private JPanel buttonRow(Component left, Component right) {
    JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
    row.setBackground(ThemeManager.getBg());
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
    row.add(left);
    row.add(right);
    return row;
  }

  private JButton menuBtn(String label, Color bg, ActionListener action) {
    JButton btn = new JButton(label);
    btn.setBackground(bg); btn.setForeground(Color.WHITE);
    btn.setFont(new Font("Arial", Font.BOLD, 13));
    btn.setOpaque(true); btn.setBorderPainted(false); btn.setFocusPainted(false);
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 10));
    btn.addActionListener(action);
    Color hover = bg.darker();
    btn.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
      public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg);   }
    });
    return btn;
  }
}
