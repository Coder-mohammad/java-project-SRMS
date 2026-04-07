package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class ManageAdminsScreen extends JFrame {

  private static final String[] BRANCHES = {
      "All",
      "Computer Science", "Information Technology", "Electronics",
      "Mechanical", "Artificial Intelligence", "Civil Engineering",
      "Data Science", "Mechanical Engineering"
  };

  private DefaultTableModel model;

  public ManageAdminsScreen() {
    setTitle("SRMS - Manage Admins (Super Admin Only)");
    setSize(650, 480);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

    JLabel title = new JLabel("Manage Admin Accounts", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 17));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // ── Admin table ───────────────────────────────────────────────────────────
    String[] cols = {"Username", "Branch", "Role"};
    model = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable table = new JTable(model);
    table.setFont(new Font("Arial", Font.PLAIN, 13));
    table.setRowHeight(24);
    table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
    table.getTableHeader().setBackground(new Color(200,220,245));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getColumnModel().getColumn(0).setPreferredWidth(150);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    table.getColumnModel().getColumn(2).setPreferredWidth(120);

    main.add(new JScrollPane(table), BorderLayout.CENTER);

    // ── Add admin form ────────────────────────────────────────────────────────
    JPanel addForm = new JPanel(new GridBagLayout());
    addForm.setBackground(ThemeManager.getBg());
    addForm.setBorder(BorderFactory.createTitledBorder("Add New Admin"));
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(5, 8, 5, 8);

    JTextField unField = new JTextField(14);
    JPasswordField pwField = new JPasswordField(14);
    JComboBox<String> branchCombo = new JComboBox<>(BRANCHES);
    JComboBox<String> roleCombo = new JComboBox<>(new String[]{"branch_admin","super_admin"});

    g.gridx=0; g.gridy=0; addForm.add(lbl("Username:"), g);
    g.gridx=1; addForm.add(unField, g);
    g.gridx=2; addForm.add(lbl("Password:"), g);
    g.gridx=3; addForm.add(pwField, g);

    g.gridx=0; g.gridy=1; addForm.add(lbl("Branch:"), g);
    g.gridx=1; addForm.add(branchCombo, g);
    g.gridx=2; addForm.add(lbl("Role:"), g);
    g.gridx=3; addForm.add(roleCombo, g);

    // When role is super_admin, branch auto-sets to All
    roleCombo.addActionListener(e -> {
      if ("super_admin".equals(roleCombo.getSelectedItem())) {
        branchCombo.setSelectedItem("All");
        branchCombo.setEnabled(false);
      } else {
        branchCombo.setEnabled(true);
        if ("All".equals(branchCombo.getSelectedItem())) branchCombo.setSelectedIndex(1);
      }
    });
    branchCombo.setEnabled(false); // default role is branch_admin — but combo starts at All
    roleCombo.setSelectedIndex(0); // branch_admin

    JButton addBtn = new JButton("Add Admin");
    addBtn.setBackground(new Color(40,110,220)); addBtn.setForeground(Color.WHITE);
    addBtn.setOpaque(true); addBtn.setBorderPainted(false);
    addBtn.addActionListener(e -> {
      String un = unField.getText().trim();
      String pw = new String(pwField.getPassword()).trim();
      String br = (String) branchCombo.getSelectedItem();
      String ro = (String) roleCombo.getSelectedItem();
      if (un.isEmpty() || pw.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username and password are required.");
        return;
      }
      if (DatabaseManager.createAdmin(un, pw, br, ro)) {
        DatabaseManager.logAction("CREATE_ADMIN", "Created admin: " + un + " | Branch: " + br + " | Role: " + ro);
        JOptionPane.showMessageDialog(this, "Admin '" + un + "' created successfully!");
        unField.setText(""); pwField.setText("");
        loadAdmins();
      } else {
        JOptionPane.showMessageDialog(this, "Failed to create admin. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
      }
    });
    g.gridx=0; g.gridy=2; g.gridwidth=4; g.anchor=GridBagConstraints.CENTER;
    addForm.add(addBtn, g);

    // ── Bottom buttons ────────────────────────────────────────────────────────
    JButton deleteBtn = new JButton("Delete Selected Admin");
    deleteBtn.setBackground(new Color(180,40,40)); deleteBtn.setForeground(Color.WHITE);
    deleteBtn.setOpaque(true); deleteBtn.setBorderPainted(false);
    deleteBtn.addActionListener(e -> {
      int row = table.getSelectedRow();
      if (row < 0) { JOptionPane.showMessageDialog(this, "Select an admin first."); return; }
      String un = (String) model.getValueAt(row, 0);
      if ("admin".equals(un)) { JOptionPane.showMessageDialog(this, "Cannot delete the main super admin."); return; }
      int c = JOptionPane.showConfirmDialog(this, "Delete admin '" + un + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
      if (c == JOptionPane.YES_OPTION) {
        if (DatabaseManager.deleteAdmin(un)) {
          DatabaseManager.logAction("DELETE_ADMIN", "Deleted admin: " + un);
          JOptionPane.showMessageDialog(this, "Admin deleted.");
          loadAdmins();
        }
      }
    });

    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> dispose());

    JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
    bot.setBackground(ThemeManager.getBg());
    bot.add(deleteBtn); bot.add(closeBtn);

    JPanel south = new JPanel(new BorderLayout());
    south.setBackground(ThemeManager.getBg());
    south.add(addForm, BorderLayout.NORTH);
    south.add(bot, BorderLayout.SOUTH);
    main.add(south, BorderLayout.SOUTH);

    add(main);
    loadAdmins();
  }

  private void loadAdmins() {
    model.setRowCount(0);
    try (Connection conn = DatabaseManager.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery("SELECT username, branch, role FROM admin_users ORDER BY username")) {
      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        row.add(rs.getString("username"));
        row.add(rs.getString("branch"));
        row.add(rs.getString("role"));
        model.addRow(row);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
  }

  private JLabel lbl(String t) {
    JLabel l = new JLabel(t); l.setForeground(ThemeManager.getFg()); return l;
  }
}
