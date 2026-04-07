package com.srms;

import java.awt.*;
import javax.swing.*;

public class ThemeManager {
  private static boolean isDark = false;

  // Dark theme colors
  public static final Color DARK_BG = new Color(30, 33, 40);
  public static final Color DARK_PANEL = new Color(42, 46, 57);
  public static final Color DARK_CARD = new Color(52, 57, 70);
  public static final Color DARK_FG = new Color(220, 225, 235);
  public static final Color DARK_ACCENT = new Color(88, 150, 255);
  public static final Color DARK_HEADER = new Color(35, 95, 175);

  // Light theme colors
  public static final Color LIGHT_BG = new Color(230, 240, 250);
  public static final Color LIGHT_PANEL = new Color(240, 245, 250);
  public static final Color LIGHT_CARD = new Color(200, 220, 240);
  public static final Color LIGHT_FG = Color.BLACK;
  public static final Color LIGHT_ACCENT = new Color(60, 120, 200);
  public static final Color LIGHT_HEADER = new Color(200, 220, 240);

  public static boolean isDark() { return isDark; }
  public static void toggle() { isDark = !isDark; }

  public static Color getBg()     { return isDark ? DARK_BG     : LIGHT_BG; }
  public static Color getPanelBg(){ return isDark ? DARK_PANEL  : LIGHT_PANEL; }
  public static Color getCardBg() { return isDark ? DARK_CARD   : LIGHT_CARD; }
  public static Color getFg()     { return isDark ? DARK_FG     : LIGHT_FG; }
  public static Color getAccent() { return isDark ? DARK_ACCENT : LIGHT_ACCENT; }
  public static Color getHeaderBg(){ return isDark ? DARK_HEADER : LIGHT_HEADER; }

  /** Recursively apply the current theme colours to every component in a container. */
  public static void applyTheme(Container root) {
    applyRec(root);
  }

  private static void applyRec(Container c) {
    Color bg = getBg();
    Color fg = getFg();
    for (Component comp : c.getComponents()) {
      if (comp instanceof JTable) {
        JTable t = (JTable) comp;
        t.setBackground(isDark ? DARK_CARD : Color.WHITE);
        t.setForeground(fg);
        t.setGridColor(isDark ? new Color(70, 75, 90) : Color.LIGHT_GRAY);
        t.getTableHeader().setBackground(isDark ? DARK_HEADER : LIGHT_HEADER);
        t.getTableHeader().setForeground(isDark ? Color.WHITE : LIGHT_FG);
      } else if (comp instanceof JScrollPane) {
        comp.setBackground(bg);
        JScrollPane sp = (JScrollPane) comp;
        Component view = sp.getViewport().getView();
        if (view instanceof Container) applyRec((Container) view);
      } else if (comp instanceof JButton) {
        // buttons keep their own colours
      } else if (comp instanceof JTextArea) {
        comp.setBackground(isDark ? DARK_CARD : Color.WHITE);
        comp.setForeground(fg);
      } else if (comp instanceof JTextField || comp instanceof JPasswordField) {
        comp.setBackground(isDark ? DARK_CARD : Color.WHITE);
        comp.setForeground(fg);
      } else if (comp instanceof JComboBox) {
        comp.setBackground(isDark ? DARK_CARD : Color.WHITE);
        comp.setForeground(fg);
      } else if (comp instanceof JLabel) {
        comp.setForeground(fg);
        if (((JComponent) comp).isOpaque()) comp.setBackground(getHeaderBg());
      } else if (comp instanceof JPanel) {
        comp.setBackground(bg);
      }
      if (comp instanceof Container) applyRec((Container) comp);
    }
  }
}
