package com.procuregov.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Utility class for hashing passwords using SHA-256.
 * Module 1 requirement: passwords must not be stored in plain text.
 */
public final class PasswordHasher {
    private static final Logger logger = Logger.getLogger(PasswordHasher.class.getName());
    
    // Private constructor - utility class
    private PasswordHasher() {}

    /**
     * Hashes a plain text password using SHA-256 algorithm.
     * @param plainPassword the raw password entered by the user
     * @return 64-character lowercase hex string SHA-256 hash
     */
    public static String hashSHA256(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            logger.warning("PasswordHasher: Cannot hash null or empty password");
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Use UTF-8 encoding for consistent hashing
            byte[] hashBytes = digest.digest(
                plainPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.severe("PasswordHasher: SHA-256 algorithm unavailable: " + e.getMessage());
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
    
    /**
     * Alias for hashSHA256() for backward compatibility.
     */
    public static String hash(String plainPassword) {
        return hashSHA256(plainPassword);
    }

    /**
     * Test method: Run this main() to generate hashes for schema.sql seed data.
     * Remove or comment out before final submission.
     */
    public static void main(String[] args) {
        String[] testPasswords = {"123456", "admin123", "password123"};
        System.out.println("=== Password Hash Generator ===");
        for (String pwd : testPasswords) {
            System.out.println("Password: " + pwd);
            System.out.println("SHA-256 : " + hashSHA256(pwd));
            System.out.println("---");
        }
    }
}