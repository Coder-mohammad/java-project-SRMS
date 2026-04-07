package com.srms;

import java.security.MessageDigest;
import java.sql.*;

public class DatabaseManager {
  private static final String URL =
      "jdbc:mysql://localhost:3306/srms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  private static final String USER = "root";
  private static final String PASSWORD = "taufeeq@786";

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
    }
  }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
  }

  // ─── Password Hashing ────────────────────────────────────────────────────────

  public static String hashPassword(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(password.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Hashing error", e);
    }
  }

  /**
   * Verifies credentials and returns [username, branch, role] if valid, or null.
   */
  public static String[] verifyAdmin(String username, String password) {
    String hash = hashPassword(password);
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "SELECT username, branch, role FROM admin_users WHERE username=? AND password_hash=?")) {
      ps.setString(1, username);
      ps.setString(2, hash);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return new String[]{
            rs.getString("username"),
            rs.getString("branch"),
            rs.getString("role")
        };
      }
    } catch (Exception e) {
      System.err.println("verifyAdmin: " + e.getMessage());
    }
    return null;
  }

  /** Legacy helper used by ChangePasswordScreen. */
  public static boolean verifyAdminPassword(String username, String password) {
    return verifyAdmin(username, password) != null;
  }

  public static boolean updateAdminPassword(String username, String newHash) {
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "UPDATE admin_users SET password_hash=? WHERE username=?")) {
      ps.setString(1, newHash);
      ps.setString(2, username);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      System.err.println("updateAdminPassword: " + e.getMessage());
      return false;
    }
  }

  // ─── Admin Management ────────────────────────────────────────────────────────

  public static boolean createAdmin(String username, String password, String branch, String role) {
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO admin_users (username, password_hash, branch, role) VALUES (?,?,?,?)")) {
      ps.setString(1, username);
      ps.setString(2, hashPassword(password));
      ps.setString(3, branch);
      ps.setString(4, role);
      ps.executeUpdate();
      return true;
    } catch (Exception e) {
      System.err.println("createAdmin: " + e.getMessage());
      return false;
    }
  }

  public static boolean deleteAdmin(String username) {
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "DELETE FROM admin_users WHERE username=? AND username != 'admin'")) {
      ps.setString(1, username);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      System.err.println("deleteAdmin: " + e.getMessage());
      return false;
    }
  }

  // ─── Audit Logging ───────────────────────────────────────────────────────────

  public static void logAction(String action, String details) {
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO audit_log (action, details, performed_at) VALUES (?, ?, NOW())")) {
      ps.setString(1, action);
      ps.setString(2, "[" + AppSession.username + "] " + details);
      ps.executeUpdate();
    } catch (Exception e) {
      System.err.println("logAction error: " + e.getMessage());
    }
  }

  // ─── Database Initialisation ──────────────────────────────────────────────────

  public static void initializeDatabase() {
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {

      // Student table
      stmt.execute("CREATE TABLE IF NOT EXISTS student (" +
          "student_id INT PRIMARY KEY AUTO_INCREMENT," +
          "roll_no VARCHAR(50) UNIQUE NOT NULL," +
          "name VARCHAR(100) NOT NULL," +
          "course VARCHAR(100) NOT NULL," +
          "semester INT NOT NULL)");

      // Marks table
      stmt.execute("CREATE TABLE IF NOT EXISTS marks (" +
          "mark_id INT PRIMARY KEY AUTO_INCREMENT," +
          "roll_no VARCHAR(50)," +
          "subject_code VARCHAR(200) NOT NULL," +
          "internal_marks INT NOT NULL," +
          "external_marks INT NOT NULL," +
          "total_marks INT," +
          "percentage DOUBLE," +
          "grade VARCHAR(5)," +
          "status VARCHAR(10)," +
          "published TINYINT(1) DEFAULT 0," +
          "FOREIGN KEY (roll_no) REFERENCES student(roll_no) ON DELETE CASCADE ON UPDATE CASCADE)");

      try { stmt.execute("ALTER TABLE marks ADD COLUMN published TINYINT(1) DEFAULT 0"); }
      catch (SQLException ignored) {}

      // Reviews table
      stmt.execute("CREATE TABLE IF NOT EXISTS student_reviews (" +
          "review_id INT PRIMARY KEY AUTO_INCREMENT," +
          "roll_no VARCHAR(50)," +
          "subject_code VARCHAR(200) NOT NULL," +
          "grade VARCHAR(5)," +
          "review_type VARCHAR(50)," +
          "comment TEXT," +
          "FOREIGN KEY (roll_no) REFERENCES student(roll_no) ON DELETE CASCADE ON UPDATE CASCADE)");

      // Admin users table (with branch + role)
      stmt.execute("CREATE TABLE IF NOT EXISTS admin_users (" +
          "id INT PRIMARY KEY AUTO_INCREMENT," +
          "username VARCHAR(50) UNIQUE NOT NULL," +
          "password_hash VARCHAR(64) NOT NULL," +
          "branch VARCHAR(100) DEFAULT 'All'," +
          "role VARCHAR(20) DEFAULT 'super_admin')");

      // Add branch/role columns to existing databases
      try { stmt.execute("ALTER TABLE admin_users ADD COLUMN branch VARCHAR(100) DEFAULT 'All'"); }
      catch (SQLException ignored) {}
      try { stmt.execute("ALTER TABLE admin_users ADD COLUMN role VARCHAR(20) DEFAULT 'super_admin'"); }
      catch (SQLException ignored) {}

      // Ensure super admin exists with correct role/branch
      ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin_users WHERE username='admin'");
      if (rs.next() && rs.getInt(1) == 0) {
        String h = hashPassword("admin");
        stmt.execute("INSERT INTO admin_users (username,password_hash,branch,role) VALUES ('admin','" + h + "','All','super_admin')");
      } else {
        // Make sure existing admin has correct branch/role
        stmt.execute("UPDATE admin_users SET branch='All', role='super_admin' WHERE username='admin'");
      }

      // Attendance table
      stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
          "id INT PRIMARY KEY AUTO_INCREMENT," +
          "roll_no VARCHAR(50) NOT NULL," +
          "subject_code VARCHAR(200) NOT NULL," +
          "attendance_date DATE NOT NULL," +
          "status ENUM('Present','Absent','Late') NOT NULL," +
          "FOREIGN KEY (roll_no) REFERENCES student(roll_no) ON DELETE CASCADE ON UPDATE CASCADE)");

      // Audit log table
      stmt.execute("CREATE TABLE IF NOT EXISTS audit_log (" +
          "id INT PRIMARY KEY AUTO_INCREMENT," +
          "action VARCHAR(100) NOT NULL," +
          "details TEXT," +
          "performed_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

      System.out.println("Database initialized successfully.");
    } catch (SQLException e) {
      System.err.println("initializeDatabase error: " + e.getMessage());
    }
  }
}
