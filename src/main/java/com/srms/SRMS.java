package com.srms;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SRMS {
  public static void main(String[] args) {
    // Set Look and Feel to match OS defaults for a better looking UI
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Initialize the database and create tables if they don't exist
    DatabaseManager.initializeDatabase();

    // Run GUI on the Event Dispatch Thread
    SwingUtilities.invokeLater(() -> {
      LoginScreen loginScreen = new LoginScreen();
      loginScreen.setVisible(true);
    });
  }
}
