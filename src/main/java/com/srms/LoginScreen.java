package com.srms;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
public class LoginScreen extends JFrame {

  private JTextField usernameField;
  private JPasswordField passwordField;
  private JButton loginBtn;
  private JLabel msgLabel;

  private int failedAttempts = 0;
  private static final int MAX_ATTEMPTS = 3;
  private static final int LOCKOUT_SECONDS = 30;

  public LoginScreen() {
    setTitle("SRMS - Admin Login");
    setSize(560, 480);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
    buildUI();
  }

  private void buildUI() {
    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(new Color(235, 243, 255));

    // Header
    JPanel header = new JPanel();
    header.setBackground(new Color(20, 60, 140));
    header.setBorder(BorderFactory.createEmptyBorder(22, 0, 22, 0));
    JLabel title = new JLabel("Student Result Management System");
    title.setFont(new Font("Arial", Font.BOLD, 20));
    title.setForeground(Color.WHITE);
    header.add(title);
    root.add(header, BorderLayout.NORTH);

    // Form
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(new Color(235, 243, 255));
    form.setBorder(BorderFactory.createEmptyBorder(30, 60, 20, 60));
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(12, 8, 12, 8);
    g.fill = GridBagConstraints.HORIZONTAL;

    g.gridx=0; g.gridy=0; g.anchor=GridBagConstraints.EAST; g.fill=GridBagConstraints.NONE;
    form.add(lbl("Username:"), g);
    usernameField = new JTextField(20);
    usernameField.setPreferredSize(new Dimension(240, 38));
    usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
    g.gridx=1; g.anchor=GridBagConstraints.WEST; g.fill=GridBagConstraints.HORIZONTAL;
    form.add(usernameField, g);

    g.gridx=0; g.gridy=1; g.anchor=GridBagConstraints.EAST; g.fill=GridBagConstraints.NONE;
    form.add(lbl("Password:"), g);
    passwordField = new JPasswordField(20);
    passwordField.setPreferredSize(new Dimension(240, 38));
    passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
    g.gridx=1; g.anchor=GridBagConstraints.WEST; g.fill=GridBagConstraints.HORIZONTAL;
    form.add(passwordField, g);

    // Message label
    msgLabel = new JLabel(" ", SwingConstants.CENTER);
    msgLabel.setFont(new Font("Arial", Font.BOLD, 13));
    msgLabel.setForeground(new Color(180, 30, 30));
    g.gridx=0; g.gridy=2; g.gridwidth=2; g.anchor=GridBagConstraints.CENTER;
    form.add(msgLabel, g);

    // Login button
    loginBtn = new JButton("Login");
    loginBtn.setBackground(new Color(20, 60, 140));
    loginBtn.setForeground(Color.WHITE);
    loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
    loginBtn.setOpaque(true); loginBtn.setBorderPainted(false); loginBtn.setFocusPainted(false);
    loginBtn.setPreferredSize(new Dimension(220, 44));
    g.gridx=0; g.gridy=3; g.gridwidth=2;
    form.add(loginBtn, g);

    // Student Portal button
    JButton studentBtn = new JButton("Student Result Portal \u2192");
    studentBtn.setBackground(new Color(235, 243, 255));
    studentBtn.setForeground(new Color(20, 60, 140));
    studentBtn.setFont(new Font("Arial", Font.BOLD, 14));
    studentBtn.setFocusPainted(false);
    studentBtn.setBorder(BorderFactory.createLineBorder(new Color(20, 60, 140), 1));
    studentBtn.setPreferredSize(new Dimension(220, 38));
    g.gridy=4;
    form.add(studentBtn, g);

    root.add(form, BorderLayout.CENTER);

    // Footer
    JLabel footer = new JLabel("SRMS v2.0  |  Multi-Branch Admin Portal", SwingConstants.CENTER);
    footer.setFont(new Font("Arial", Font.PLAIN, 13));
    footer.setForeground(new Color(80, 80, 100));
    footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
    root.add(footer, BorderLayout.SOUTH);

    setContentPane(root);

    // Actions
    loginBtn.addActionListener(e -> attemptLogin());
    passwordField.addActionListener(e -> attemptLogin());
    usernameField.addActionListener(e -> passwordField.requestFocus());

    studentBtn.addActionListener(e -> {
      dispose();
      new StudentSearchScreen().setVisible(true);
    });
  }

  private void attemptLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
      msgLabel.setText("Enter both username and password.");
      return;
    }

    String[] user = DatabaseManager.verifyAdmin(username, password);
    if (user != null) {
      // Set session
      AppSession.username = user[0];
      AppSession.branch   = user[1];
      AppSession.role     = user[2];

      DatabaseManager.logAction("LOGIN", "Logged in. Branch: " + AppSession.branch);
      dispose();
      new MainDashboard().setVisible(true);
    } else {
      failedAttempts++;
      if (failedAttempts >= MAX_ATTEMPTS) {
        lockout();
      } else {
        msgLabel.setText("Invalid credentials. Attempt " + failedAttempts + "/" + MAX_ATTEMPTS);
        passwordField.setText("");
      }
    }
  }

  private void lockout() {
    loginBtn.setEnabled(false);
    usernameField.setEnabled(false);
    passwordField.setEnabled(false);
    final int[] remaining = {LOCKOUT_SECONDS};
    Timer t = new Timer(1000, null);
    t.addActionListener(e -> {
      remaining[0]--;
      msgLabel.setText("Too many attempts. Wait " + remaining[0] + "s...");
      if (remaining[0] <= 0) {
        t.stop();
        failedAttempts = 0;
        loginBtn.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        msgLabel.setText("You may try again.");
        passwordField.setText("");
      }
    });
    t.start();
  }

  private JLabel lbl(String t) {
    JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.BOLD, 15)); return l;
  }
}
