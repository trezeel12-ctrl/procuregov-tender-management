package com.procuregov.service;

import com.procuregov.dao.UserDAO;
import com.procuregov.model.User;
import com.procuregov.util.PasswordHasher;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Handles user authentication and supplier registration logic.
 * Module 1: SHA-256 password hashing before persisting or comparing.
 * Module 1: Failed login attempts tracked in database with temporary lockout.
 */
public class AuthService {
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private final UserDAO userDAO;
    
    // Lockout duration in minutes
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public AuthService(UserDAO userDAO) {
        if (userDAO == null) {
            throw new IllegalArgumentException("UserDAO cannot be null");
        }
        this.userDAO = userDAO;
    }

    /**
     * Checks if a user account is locked due to too many failed attempts.
     * @param user the user to check
     * @return true if account is locked, false otherwise
     */
    public boolean isAccountLocked(User user) {
        if (user == null) return false;
        
        int failedAttempts = user.getFailedLoginAttempts();
        LocalDateTime lastFailedAttempt = user.getLastFailedAttempt();
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS && lastFailedAttempt != null) {
            LocalDateTime lockExpiryTime = lastFailedAttempt.plusMinutes(LOCKOUT_DURATION_MINUTES);
            if (LocalDateTime.now().isBefore(lockExpiryTime)) {
                return true;
            } else {
                // Lock has expired, reset attempts
                userDAO.resetFailedAttempts(user.getUserId());
                user.setFailedLoginAttempts(0);
                user.setLastFailedAttempt(null);
                return false;
            }
        }
        return false;
    }

    /**
     * Gets the remaining lockout time in minutes.
     * @param user the locked user
     * @return remaining minutes, or 0 if not locked
     */
    public long getRemainingLockoutMinutes(User user) {
        if (user == null || user.getLastFailedAttempt() == null) return 0;
        
        LocalDateTime lockExpiryTime = user.getLastFailedAttempt().plusMinutes(LOCKOUT_DURATION_MINUTES);
        long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), lockExpiryTime);
        return Math.max(minutesRemaining, 0);
    }

    /**
     * Records a failed login attempt in the database.
     * @param email the email that failed authentication
     * @return the updated failed attempt count
     */
    public int recordFailedAttempt(String email) {
        try {
            User user = userDAO.findByEmail(email);
            if (user == null) {
                logger.warning("recordFailedAttempt: user not found for email: " + email);
                return 0;
            }
            
            int newAttemptCount = user.getFailedLoginAttempts() + 1;
            userDAO.updateFailedAttempts(user.getUserId(), newAttemptCount);
            userDAO.updateLastFailedAttempt(user.getUserId(), LocalDateTime.now());
            
            logger.info("recordFailedAttempt: user " + email + " now has " + newAttemptCount + " failed attempts");
            return newAttemptCount;
            
        } catch (Exception e) {
            logger.severe("recordFailedAttempt error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Resets failed attempts for a user after successful login.
     * @param userId the user ID
     */
    public void resetFailedAttempts(int userId) {
        try {
            userDAO.resetFailedAttempts(userId);
            logger.info("resetFailedAttempts: reset attempts for user ID " + userId);
        } catch (Exception e) {
            logger.severe("resetFailedAttempts error: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user by email and plain text password.
     * Tracks failed attempts in database and implements lockout.
     * 
     * @param email email address entered on the login form
     * @param plainPassword plain text password entered on the login form
     * @return authenticated User object if credentials match, null otherwise
     */
    public User authenticate(String email, String plainPassword) {
        // Validate input
        if (email == null || email.trim().isEmpty() || 
            plainPassword == null || plainPassword.trim().isEmpty()) {
            logger.warning("authenticate: blank credentials provided");
            return null;
        }

        // Normalize email (database stores lowercase)
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPassword = plainPassword.trim();

        try {
            // Step 1: Fetch user by email
            User user = userDAO.findByEmail(normalizedEmail);
            if (user == null) {
                logger.warning("authenticate: no account found for email: " + normalizedEmail);
                return null;
            }

            // Step 2: Check if account is locked
            if (isAccountLocked(user)) {
                long minutesLeft = getRemainingLockoutMinutes(user);
                logger.warning("authenticate: account locked for " + normalizedEmail + ". Minutes remaining: " + minutesLeft);
                return null;
            }

            // Step 3: Hash the entered password using SHA-256
            String enteredHash = PasswordHasher.hashSHA256(normalizedPassword);
            String storedHash = user.getPasswordHash();

            // Step 4: Compare hashes
            if (enteredHash != null && enteredHash.equals(storedHash)) {
                // Successful login - reset failed attempts
                resetFailedAttempts(user.getUserId());
                user.setFailedLoginAttempts(0);
                user.setLastFailedAttempt(null);
                
                logger.info("authenticate: login successful for: " + normalizedEmail);
                return user;
            } else {
                // Failed login - record the attempt
                int newAttemptCount = recordFailedAttempt(normalizedEmail);
                int attemptsRemaining = Math.max(0, MAX_FAILED_ATTEMPTS - newAttemptCount);
                
                logger.warning("authenticate: password mismatch for: " + normalizedEmail + 
                              " (Attempt " + newAttemptCount + "/" + MAX_FAILED_ATTEMPTS + ")");
                
                // Update the user object with new attempts for session message
                User updatedUser = userDAO.findByEmail(normalizedEmail);
                if (updatedUser != null) {
                    user.setFailedLoginAttempts(updatedUser.getFailedLoginAttempts());
                    user.setLastFailedAttempt(updatedUser.getLastFailedAttempt());
                }
                
                return null;
            }

        } catch (Exception e) {
            logger.severe("authenticate: database error - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registers a new supplier account.
     * Hashes the plain text password before passing to the DAO.
     * 
     * @param supplier User JavaBean populated by RegisterServlet
     * @return generated user_id if registration succeeded, 0 if failed
     */
    public int registerSupplier(User supplier) {
        if (supplier == null || 
            supplier.getEmail() == null || supplier.getEmail().trim().isEmpty() ||
            supplier.getPasswordHash() == null || supplier.getPasswordHash().trim().isEmpty()) {
            logger.warning("registerSupplier: invalid supplier data provided");
            return 0;
        }

        try {
            // Normalize email
            supplier.setEmail(supplier.getEmail().trim().toLowerCase());

            // Hash the password BEFORE persisting (Module 1 requirement)
            String plainPassword = supplier.getPasswordHash();
            String hashedPassword = PasswordHasher.hashSHA256(plainPassword);
            supplier.setPasswordHash(hashedPassword);
            
            // Initialize failed attempts to 0
            supplier.setFailedLoginAttempts(0);
            supplier.setLastFailedAttempt(null);

            // Persist via DAO
            int userId = userDAO.insert(supplier);

            if (userId > 0) {
                logger.info("registerSupplier: success for " + supplier.getEmail() + " userId=" + userId);
            } else {
                logger.severe("registerSupplier: insert returned 0 for " + supplier.getEmail());
            }
            return userId;

        } catch (Exception e) {
            logger.severe("registerSupplier: error - " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Gets the maximum allowed failed attempts.
     * @return max failed attempts (3)
     */
    public int getMaxFailedAttempts() {
        return MAX_FAILED_ATTEMPTS;
    }
    
    /**
     * Gets the lockout duration in minutes.
     * @return lockout duration (15 minutes)
     */
    public int getLockoutDurationMinutes() {
        return LOCKOUT_DURATION_MINUTES;
    }
    
    public User getUserByEmail(String email) {
        try {
            String normalizedEmail = email.trim().toLowerCase();
            return userDAO.findByEmail(normalizedEmail);
        } catch (Exception e) {
            logger.severe("getUserByEmail error: " + e.getMessage());
            return null;
        }
    }
}