package com.srms;

/**
 * Holds the currently logged-in admin's session info.
 * Set by LoginScreen after successful authentication.
 * Read by all screens to apply branch-based filtering.
 */
public class AppSession {

  public static String username = "";
  public static String branch   = "";  // "All" = Super Admin
  public static String role     = "";  // "super_admin" | "branch_admin"

  /** Returns true if the logged-in user is a Super Admin (can see all branches). */
  public static boolean isSuperAdmin() {
    return "All".equals(branch) || "super_admin".equals(role);
  }

  /** Returns the SQL WHERE clause fragment to filter by the current admin's branch.
   *  e.g. " AND s.course='Computer Science'"  or ""  for super admin. */
  public static String branchFilter(String tableAlias) {
    if (isSuperAdmin()) return "";
    return " AND " + tableAlias + ".course='" + branch.replace("'", "''") + "'";
  }

  /** Same as branchFilter but for direct student table (no alias needed). */
  public static String branchFilterDirect() {
    if (isSuperAdmin()) return "";
    return " AND course='" + branch.replace("'", "''") + "'";
  }

  /** Clear session (on logout). */
  public static void clear() {
    username = ""; branch = ""; role = "";
  }
}
