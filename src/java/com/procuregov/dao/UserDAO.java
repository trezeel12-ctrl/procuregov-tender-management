package com.procuregov.dao;

import com.procuregov.model.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object interface for User entity.
 * Defines contract for all user-related database operations.
 */
public interface UserDAO {
    
    /** Finds a user by primary key */
    User findById(int userId);

    /** Finds a user by email address */
    User findByEmail(String email);

    /** Inserts a new user and returns generated user_id */
    int insert(User user);

    /** Updates failed login attempts count */
    boolean updateFailedAttempts(int userId, int attempts);
    
    /** Resets failed login attempts to 0 */
    boolean resetFailedAttempts(int userId);
    
    /** Updates the last failed attempt timestamp */
    boolean updateLastFailedAttempt(int userId, LocalDateTime timestamp);

    /** Finds all users by role */
    List<User> findByRole(String role);
    
    /** Finds all user IDs by role */
    List<Integer> findUserIdsByRole(String role);
}