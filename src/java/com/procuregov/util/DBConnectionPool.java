package com.procuregov.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database connections using Tomcat JNDI Connection Pool.
 * Strictly prohibits DriverManager as required by Module 5.
 * 
 * 📌 TOMCAT SETUP REQUIRED:
 * Add this to Tomcat's conf/context.xml or META-INF/context.xml in your project:
 * <Resource name="jdbc/ProcureGovDB" auth="Container" type="javax.sql.DataSource"
 *           maxTotal="20" maxIdle="10" maxWaitMillis="10000"
 *           username="root" password="your_password" driverClassName="com.mysql.cj.jdbc.Driver"
 *           url="jdbc:mysql://localhost:3306/Karabelo123456"/>
 */
public final class DBConnectionPool {
    private static DataSource dataSource;

    static {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/ProcureGovDB");
        } catch (NamingException e) {
            System.err.println("[DBConnectionPool] JNDI DataSource lookup failed. Ensure Tomcat context.xml is configured.");
            System.err.println("[DBConnectionPool] Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pooled database connection.
     * @return active Connection from the Tomcat connection pool
     * @throws SQLException if pool is exhausted or misconfigured
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized. Verify Tomcat JNDI configuration.");
        }
        return dataSource.getConnection();
    }

    /**
     * Safely returns a connection to the pool.
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } 
            catch (SQLException e) { System.err.println("[DBConnectionPool] Error closing connection: " + e.getMessage()); }
        }
    }
}