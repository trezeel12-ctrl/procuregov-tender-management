package com.procuregov.dao;

import com.procuregov.model.User;
import com.procuregov.util.DBConnectionPool;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of UserDAO using JDBC and connection pooling.
 * All SQLExceptions are caught, logged, and handled gracefully.
 */
public class UserDAOImpl implements UserDAO {
    
    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());
    
    private Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection();
    }

    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        } catch (SQLException e) {
            logger.severe("[UserDAO.findById] Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToUser(rs) : null;
            }
        } catch (SQLException e) {
            logger.severe("[UserDAO.findByEmail] Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int insert(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, role, registration_number, physical_address, contact_number, failed_login_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getRegistrationNumber());
            pstmt.setString(6, user.getPhysicalAddress());
            pstmt.setString(7, user.getContactNumber());
            pstmt.setInt(8, user.getFailedLoginAttempts());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            logger.severe("[UserDAO.insert] Error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean updateFailedAttempts(int userId, int attempts) {
        String sql = "UPDATE users SET failed_login_attempts = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attempts);
            pstmt.setInt(2, userId);
            int updated = pstmt.executeUpdate();
            logger.info("[UserDAO.updateFailedAttempts] User " + userId + " now has " + attempts + " failed attempts");
            return updated > 0;
        } catch (SQLException e) {
            logger.severe("[UserDAO.updateFailedAttempts] Error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_login_attempts = 0, last_failed_attempt = NULL WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int updated = pstmt.executeUpdate();
            logger.info("[UserDAO.resetFailedAttempts] Reset failed attempts for user " + userId);
            return updated > 0;
        } catch (SQLException e) {
            logger.severe("[UserDAO.resetFailedAttempts] Error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateLastFailedAttempt(int userId, LocalDateTime timestamp) {
        String sql = "UPDATE users SET last_failed_attempt = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(timestamp));
            pstmt.setInt(2, userId);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            logger.severe("[UserDAO.updateLastFailedAttempt] Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.severe("[UserDAO.findByRole] Error: " + e.getMessage());
        }
        return users;
    }
    
    @Override
    public List<Integer> findUserIdsByRole(String role) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT user_id FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            logger.severe("[UserDAO.findUserIdsByRole] Error: " + e.getMessage());
        }
        return ids;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setRegistrationNumber(rs.getString("registration_number"));
        u.setPhysicalAddress(rs.getString("physical_address"));
        u.setContactNumber(rs.getString("contact_number"));
        u.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        
        Timestamp lastAttemptTs = rs.getTimestamp("last_failed_attempt");
        if (lastAttemptTs != null) {
            u.setLastFailedAttempt(lastAttemptTs.toLocalDateTime());
        }
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            u.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        return u;
    }
}