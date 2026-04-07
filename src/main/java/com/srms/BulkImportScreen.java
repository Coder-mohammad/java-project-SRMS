package com.srms;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;

@SuppressWarnings("serial")
public class BulkImportScreen extends JFrame {

  private DefaultTableModel previewModel;
  private JLabel statusLabel;
  private java.util.List<String[]> parsedRows = new java.util.ArrayList<>();

  public BulkImportScreen() {
    setTitle("SRMS - Bulk Import Students");
    setSize(720, 520);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    JLabel title = new JLabel("Bulk Import Students from CSV", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 17));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    // Instructions
    JTextArea info = new JTextArea(
        "CSV Format (no header needed):\n" +
        "  Roll No, Name, Branch, Semester\n\n" +
        "Example:\n" +
        "  CS001, Alice Johnson, Computer Science, 3\n" +
        "  IT002, Bob Smith, Information Technology, 5\n\n" +
        "Duplicate Roll Numbers will be skipped automatically.");
    info.setEditable(false); info.setOpaque(false);
    info.setFont(new Font("Monospaced", Font.PLAIN, 12));
    info.setForeground(ThemeManager.getFg());
    info.setBorder(BorderFactory.createTitledBorder("Instructions"));
    main.add(info, BorderLayout.BEFORE_FIRST_LINE);

    // Preview table
    String[] cols = {"Roll No","Name","Branch","Semester"};
    previewModel = new DefaultTableModel(cols, 0) {
      public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable previewTable = new JTable(previewModel);
    previewTable.setFillsViewportHeight(true);
    previewTable.setFont(new Font("Arial", Font.PLAIN, 12));
    previewTable.setRowHeight(22);
    previewTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

    JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
    tablePanel.setBackground(ThemeManager.getBg());
    JLabel pl = new JLabel("Preview (rows to import):", SwingConstants.LEFT);
    pl.setFont(new Font("Arial", Font.BOLD, 13));
    pl.setForeground(ThemeManager.getFg());
    tablePanel.add(pl, BorderLayout.NORTH);
    tablePanel.add(new JScrollPane(previewTable), BorderLayout.CENTER);
    main.add(tablePanel, BorderLayout.CENTER);

    // Bottom buttons
    statusLabel = new JLabel(" ", SwingConstants.CENTER);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
    statusLabel.setForeground(new Color(30, 130, 60));

    JButton browseBtn = styledBtn("Browse CSV File", ThemeManager.getAccent());
    JButton importBtn = styledBtn("Import to Database", new Color(50, 150, 50));
    JButton clearBtn  = styledBtn("Clear",             new Color(150, 60, 60));
    JButton closeBtn  = styledBtn("Close",             new Color(80, 80, 80));

    browseBtn.addActionListener(e -> browseCSV());
    importBtn.addActionListener(e -> importData());
    clearBtn.addActionListener(e  -> { previewModel.setRowCount(0); parsedRows.clear(); statusLabel.setText(" "); });
    closeBtn.addActionListener(e  -> dispose());

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
    btnRow.setBackground(ThemeManager.getBg());
    btnRow.add(browseBtn); btnRow.add(importBtn); btnRow.add(clearBtn); btnRow.add(closeBtn);

    JPanel bot = new JPanel(new BorderLayout(0, 4));
    bot.setBackground(ThemeManager.getBg());
    bot.add(statusLabel, BorderLayout.NORTH);
    bot.add(btnRow, BorderLayout.CENTER);
    main.add(bot, BorderLayout.SOUTH);

    add(main);
  }

  private void browseCSV() {
    JFileChooser fc = new JFileChooser();
    fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
    if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

    parsedRows.clear();
    previewModel.setRowCount(0);

    try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
      String line;
      int lineNum = 0;
      while ((line = br.readLine()) != null) {
        lineNum++;
        line = line.trim();
        if (line.isEmpty()) continue;
        // Skip header row if it starts with non-numeric text in first col
        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
          statusLabel.setText("Line " + lineNum + " skipped (needs 4 columns).");
          statusLabel.setForeground(Color.ORANGE);
          continue;
        }
        String rollNo = parts[0].trim();
        String name   = parts[1].trim();
        String branch = parts[2].trim();
        String sem    = parts[3].trim();

        // Skip header-like rows
        if (rollNo.equalsIgnoreCase("roll no") || rollNo.equalsIgnoreCase("roll_no")) continue;

        try { Integer.parseInt(sem); } catch (NumberFormatException ex) { continue; }

        parsedRows.add(new String[]{rollNo, name, branch, sem});
        previewModel.addRow(new Object[]{rollNo, name, branch, sem});
      }
      statusLabel.setText(parsedRows.size() + " row(s) ready for import.");
      statusLabel.setForeground(new Color(30, 130, 60));
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void importData() {
    if (parsedRows.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No data to import. Please browse a CSV file first.", "Info", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    int inserted = 0, skipped = 0;
    String sql = "INSERT INTO student (roll_no, name, course, semester) VALUES (?,?,?,?)";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      for (String[] row : parsedRows) {
        try {
          ps.setString(1, row[0]); ps.setString(2, row[1]);
          ps.setString(3, row[2]); ps.setInt(4, Integer.parseInt(row[3]));
          ps.executeUpdate();
          inserted++;
        } catch (SQLException ex) {
          skipped++; // duplicate or constraint violation
        }
      }
      DatabaseManager.logAction("BULK_IMPORT",
          inserted + " students imported, " + skipped + " skipped.");
      JOptionPane.showMessageDialog(this,
          "Import complete!\n" + inserted + " inserted,  " + skipped + " skipped (duplicates).",
          "Import Result", JOptionPane.INFORMATION_MESSAGE);
      statusLabel.setText("Done: " + inserted + " imported, " + skipped + " skipped.");
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Import error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private JButton styledBtn(String t, Color bg) {
    JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
    b.setOpaque(true); b.setBorderPainted(false); return b;
  }
}
