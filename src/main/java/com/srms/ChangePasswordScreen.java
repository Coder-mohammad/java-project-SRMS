package com.srms;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class ChangePasswordScreen extends JFrame {

  private JPasswordField currentField, newField, confirmField;

  public ChangePasswordScreen() {
    setTitle("SRMS - Change Password");
    setSize(400, 280);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    JPanel main = new JPanel(new BorderLayout(10, 10));
    main.setBackground(ThemeManager.getBg());
    main.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

    JLabel title = new JLabel("Change Password — " + AppSession.username, SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 15));
    title.setForeground(ThemeManager.getAccent());
    main.add(title, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
    form.setBackground(ThemeManager.getBg());

    form.add(fl("Current Password:")); currentField = new JPasswordField();  form.add(currentField);
    form.add(fl("New Password:"));     newField     = new JPasswordField();  form.add(newField);
    form.add(fl("Confirm Password:")); confirmField = new JPasswordField();  form.add(confirmField);
    main.add(form, BorderLayout.CENTER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    btnRow.setBackground(ThemeManager.getBg());
    JButton save = btn("Save", new Color(50,150,50));
    JButton cancel = btn("Cancel", new Color(100,100,100));
    save.addActionListener(e -> changePassword());
    cancel.addActionListener(e -> dispose());
    btnRow.add(save); btnRow.add(cancel);
    main.add(btnRow, BorderLayout.SOUTH);

    add(main);
  }

  private void changePassword() {
    String cur  = new String(currentField.getPassword());
    String nw   = new String(newField.getPassword());
    String conf = new String(confirmField.getPassword());

    if (cur.isEmpty() || nw.isEmpty() || conf.isEmpty()) {
      JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!nw.equals(conf)) {
      JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (nw.length() < 4) {
      JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!DatabaseManager.verifyAdminPassword(AppSession.username, cur)) {
      JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    String newHash = DatabaseManager.hashPassword(nw);
    if (DatabaseManager.updateAdminPassword(AppSession.username, newHash)) {
      DatabaseManager.logAction("CHANGE_PASSWORD", "Password changed successfully.");
      JOptionPane.showMessageDialog(this, "Password changed successfully!");
      dispose();
    } else {
      JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private JLabel fl(String t) { JLabel l = new JLabel(t); l.setForeground(ThemeManager.getFg()); return l; }
  private JButton btn(String t, Color bg) {
    JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
    b.setOpaque(true); b.setBorderPainted(false); b.setPreferredSize(new Dimension(110, 30));
    return b;
  }
}
