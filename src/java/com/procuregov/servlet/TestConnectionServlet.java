package com.procuregov.servlet;

import com.procuregov.util.DBConnectionPool;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Diagnostic Servlet to verify JNDI connection pool and users table data.
 * For testing only. Remove before final submission.
 */
public class TestConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        out.println("<!DOCTYPE html><html><head><title>DB Connection Test</title>");
        out.println("<style>body{font-family:sans-serif;margin:30px;background:#f8f9fa;color:#333;} h2,h3{color:#1a4789;} .success{color:#16a34a;font-weight:bold;} .error{color:#dc2626;font-weight:bold;} table{border-collapse:collapse;width:100%;background:#fff;} th,td{border:1px solid #ddd;padding:10px;text-align:left;} th{background:#e2e8f0;} code{background:#f1f5f9;padding:2px 6px;border-radius:4px;font-size:0.9em;}</style></head><body>");
        out.println("<h2>🔍 ProcureGov Database Connection Test</h2>");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // STEP 1: Test JNDI Connection Pool
            out.println("<h3>Step 1: JNDI Connection Pool</h3>");
            conn = DBConnectionPool.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                out.println("<p class='success'>✅ Successfully obtained connection from Tomcat JNDI pool.</p>");
                out.println("<p><strong>Database URL:</strong> " + conn.getMetaData().getURL() + "</p>");
                out.println("<p><strong>Current Schema:</strong> " + conn.getMetaData().getUserName() + "</p>");
            } else {
                out.println("<p class='error'>❌ Connection is null or closed. Check META-INF/context.xml</p>");
                return;
            }

            // STEP 2: Check if users table exists & count records
            out.println("<h3>Step 2: Users Table Status</h3>");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM users");
            
            if (rs.next()) {
                int count = rs.getInt("total");
                out.println("<p>📊 Total users in database: <strong>" + count + "</strong></p>");

                if (count > 0) {
                    // STEP 3: Show sample records (email + hash prefix)
                    out.println("<h3>Step 3: Sample User Records</h3>");
                    out.println("<table><tr><th>ID</th><th>Email</th><th>Role</th><th>Password Hash (First 15 chars)</th></tr>");
                    
                    rs = stmt.executeQuery("SELECT user_id, email, role, password_hash FROM users LIMIT 10");
                    while (rs.next()) {
                        String hash = rs.getString("password_hash");
                        String shortHash = (hash != null && hash.length() > 15) ? hash.substring(0, 15) + "..." : hash;
                        
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("user_id") + "</td>");
                        out.println("<td>" + rs.getString("email") + "</td>");
                        out.println("<td>" + rs.getString("role") + "</td>");
                        out.println("<td><code>" + shortHash + "</code></td>");
                        out.println("</tr>");
                    }
                    out.println("</table>");
                    
                    out.println("<p class='success'>✅ Database is connected and users table contains data.</p>");
                } else {
                    out.println("<p class='error'>⚠️ The 'users' table exists but is EMPTY. Please run your schema.sql INSERT statements.</p>");
                }
            }

        } catch (SQLException e) {
            out.println("<h3 class='error'>❌ Database Error</h3>");
            out.println("<p><strong>Error Message:</strong> " + e.getMessage() + "</p>");
            out.println("<p><strong>SQL State:</strong> " + e.getSQLState() + "</p>");
            out.println("<p><strong>Error Code:</strong> " + e.getErrorCode() + "</p>");
        } finally {
            // Safe resource cleanup
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }

        out.println("<hr><p><a href='auth?action=login'>← Back to Login</a></p></body></html>");
    }
}