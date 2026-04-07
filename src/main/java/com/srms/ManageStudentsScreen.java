package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

@SuppressWarnings("serial")
public class ManageStudentsScreen extends JFrame {
  private DefaultTableModel model;
  private JComboBox<String> branchFilterCombo;
  private JComboBox<String> semesterFilterCombo;
  private JTable table;
  private boolean allSelected = false;

  public ManageStudentsScreen() {
    setTitle("Student Result Management System - Manage Students");
    setSize(500, 350);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(new Color(230, 240, 250));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Header Title and Filters panel
    JPanel topPanel = new JPanel(new BorderLayout());

    JLabel titleLabel = new JLabel("Manage Students Screen (Delete/View All)");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
    titleLabel.setOpaque(true);
    titleLabel.setBackground(new Color(200, 220, 240));
    titleLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    titleLabel.setPreferredSize(new Dimension(getWidth(), 30));
    titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
    topPanel.add(titleLabel, BorderLayout.NORTH);

    // Filters Panel
    JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filtersPanel.setBackground(new Color(230, 240, 250));

    filtersPanel.add(new JLabel("Branch:"));
    branchFilterCombo = new JComboBox<>(
        new String[] { "All", "Computer Science", "Information Technology", "Electronics", "Mechanical",
            "Artificial Intelligence", "Civil Engineering", "Data Science", "Mechanical Engineering" });
    branchFilterCombo.addActionListener(e -> loadData());
    filtersPanel.add(branchFilterCombo);

    filtersPanel.add(new JLabel("Semester:"));
    semesterFilterCombo = new JComboBox<>(new String[] { "All", "1", "2", "3", "4", "5", "6", "7", "8" });
    semesterFilterCombo.addActionListener(e -> loadData());
    filtersPanel.add(semesterFilterCombo);

    // Branch admins see only their branch
    if (!AppSession.isSuperAdmin()) {
      branchFilterCombo.setSelectedItem(AppSession.branch);
      branchFilterCombo.setEnabled(false);
    }

    // Select All / Deselect All button
    JButton selectAllBtn = new JButton("Select All");
    selectAllBtn.setBackground(new Color(80, 80, 160));
    selectAllBtn.setForeground(Color.WHITE);
    selectAllBtn.setOpaque(true);
    selectAllBtn.setBorderPainted(false);
    selectAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
    selectAllBtn.addActionListener(e -> {
      allSelected = !allSelected;
      for (int i = 0; i < model.getRowCount(); i++) {
        model.setValueAt(allSelected, i, 0);
      }
      selectAllBtn.setText(allSelected ? "Deselect All" : "Select All");
    });
    filtersPanel.add(selectAllBtn);

    topPanel.add(filtersPanel, BorderLayout.SOUTH);
    panel.add(topPanel, BorderLayout.NORTH);

    // Table
    String[] columnNames = { "Select", "Roll Number", "Student Name", "Course Branch", "Semester" };
    model = new DefaultTableModel(columnNames, 0) {
      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0)
          return Boolean.class;
        return super.getColumnClass(columnIndex);
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return column == 0; // only 'Select' column is editable
      }
    };

    loadData();

    table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setFont(new Font("Arial", Font.PLAIN, 13));
    table.setRowHeight(24);
    table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
    table.getTableHeader().setBackground(new Color(200, 220, 240));
    // Fix column widths
    table.getColumnModel().getColumn(0).setMaxWidth(55);   // Select
    table.getColumnModel().getColumn(1).setPreferredWidth(100); // Roll No
    table.getColumnModel().getColumn(2).setPreferredWidth(140); // Name
    table.getColumnModel().getColumn(3).setPreferredWidth(160); // Branch
    table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Semester

    // Clicking the "Select" column header toggles all rows
    table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        int col = table.columnAtPoint(e.getPoint());
        if (col == 0) {
          allSelected = !allSelected;
          for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(allSelected, i, 0);
          }
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane, BorderLayout.CENTER);

    // Footer with Back Button and Delete Button
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBackground(new Color(230, 240, 250));

    JButton updateBtn = new JButton("Update Selected");
    updateBtn.setBackground(new Color(50, 150, 50));
    updateBtn.setForeground(Color.WHITE);
    updateBtn.setOpaque(true);
    updateBtn.setBorderPainted(false);
    updateBtn.addActionListener(e -> {
      java.util.List<String> selectedRollNos = new java.util.ArrayList<>();
      for (int i = 0; i < model.getRowCount(); i++) {
        Boolean isSelected = (Boolean) model.getValueAt(i, 0);
        if (isSelected != null && isSelected) {
          selectedRollNos.add((String) model.getValueAt(i, 1));
        }
      }

      if (selectedRollNos.size() != 1) {
        JOptionPane.showMessageDialog(this, "Please check the box next to exactly one student to update.");
        return;
      }

      String rollNoToUpdate = selectedRollNos.get(0);
      new UpdateStudentScreen(rollNoToUpdate, () -> loadData()).setVisible(true);
    });

    JButton deleteBtn = new JButton("Delete Selected");
    deleteBtn.setBackground(new Color(200, 50, 50));
    deleteBtn.setForeground(Color.WHITE);
    deleteBtn.setOpaque(true);
    deleteBtn.setBorderPainted(false);
    deleteBtn.addActionListener(e -> {
      java.util.List<String> selectedRollNos = new java.util.ArrayList<>();
      for (int i = 0; i < model.getRowCount(); i++) {
        Boolean isSelected = (Boolean) model.getValueAt(i, 0);
        if (isSelected != null && isSelected) {
          selectedRollNos.add((String) model.getValueAt(i, 1));
        }
      }

      if (selectedRollNos.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please check the box next to at least one student to delete.");
        return;
      }

      deleteStudents(selectedRollNos);
    });

    JButton backBtn = new JButton("Back");
    backBtn.setBackground(new Color(50, 100, 200));
    backBtn.setForeground(Color.WHITE);
    backBtn.setOpaque(true);
    backBtn.setBorderPainted(false);
    backBtn.addActionListener(e -> dispose());

    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(false);
    buttonPanel.add(updateBtn);
    buttonPanel.add(deleteBtn);
    buttonPanel.add(backBtn);
    bottomPanel.add(buttonPanel, BorderLayout.NORTH);

    JLabel footerLabel = new JLabel("Tip: Click the 'Select' column header or use 'Select All' to toggle all rows",
        SwingConstants.CENTER);
    footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    footerLabel.setForeground(new Color(80, 80, 80));
    bottomPanel.add(footerLabel, BorderLayout.SOUTH);

    panel.add(bottomPanel, BorderLayout.SOUTH);

    add(panel);
  }

  private void loadData() {
    model.setRowCount(0); // clear existing data

    String selectedBranch = (String) branchFilterCombo.getSelectedItem();
    String selectedSemester = (String) semesterFilterCombo.getSelectedItem();

    String sql = "SELECT roll_no, name, course, semester FROM student WHERE 1=1";

    if (selectedBranch != null && !selectedBranch.equals("All")) {
      sql += " AND course = '" + selectedBranch + "'";
    }
    if (selectedSemester != null && !selectedSemester.equals("All")) {
      sql += " AND semester = " + selectedSemester;
    }

    sql += " ORDER BY roll_no";

    try (Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        Vector<Object> row = new Vector<>();
        row.add(false); // Select checkbox
        row.add(rs.getString("roll_no"));
        row.add(rs.getString("name"));
        row.add(rs.getString("course"));
        row.add(rs.getInt("semester"));
        model.addRow(row);
      }

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error Loading Students: " + ex.getMessage(), "Database Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private void deleteStudents(java.util.List<String> rollNos) {
    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to delete " + rollNos.size()
            + " selected student(s)?\nThis will also delete their marks!",
        "Confirm Delete", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
      String sql = "DELETE FROM student WHERE roll_no = ?";
      try (Connection conn = DatabaseManager.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {

        int successCount = 0;
        for (String rollNo : rollNos) {
          pstmt.setString(1, rollNo);
          successCount += pstmt.executeUpdate();
        }

        if (successCount > 0) {
          JOptionPane.showMessageDialog(this, successCount + " Student(s) Deleted Successfully.");
          loadData(); // refresh the table
        } else {
          JOptionPane.showMessageDialog(this, "Failed to delete students.");
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error Deleting Students: " + ex.getMessage(), "Database Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
