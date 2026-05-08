package com.procuregov.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralized application constants to ensure consistency across Servlets, Services, and DAOs.
 * Prevents magic strings and simplifies configuration changes.
 * 
 * Exam Compliance:
 * - Module 1: User roles and authentication constants
 * - Module 2: Tender lifecycle, categories, file upload constraints
 * - Module 4: Evaluation scoring weights and validation rules
 * - Module 5: JNDI and configuration keys
 */
public final class AppConstants {
    
    // Prevent instantiation - utility class only
    private AppConstants() {}

    // ==================== USER ROLES (Module 1 Requirement) ====================
    /** Supplier role - can register, view tenders, submit bids */
    public static final String ROLE_SUPPLIER = "SUPPLIER";
    
    /** Procurement Officer role - can create/manage tenders, award contracts */
    public static final String ROLE_OFFICER = "OFFICER";
    
    /** Evaluation Committee role - can score bids, view evaluation panel */
    public static final String ROLE_EVALUATOR = "EVALUATOR";
    
    /** Valid roles set for validation */
    public static final Set<String> VALID_ROLES = new HashSet<String>(Arrays.asList(
        ROLE_SUPPLIER, ROLE_OFFICER, ROLE_EVALUATOR
    ));
    
    /**
     * Validates if a role string is valid.
     * @param role the role string to validate
     * @return true if role is one of the defined constants
     */
    public static boolean isValidRole(String role) {
        return role != null && VALID_ROLES.contains(role.toUpperCase());
    }

    // ==================== TENDER STATUS LIFECYCLE (Module 2 Requirement) ====================
    /** Initial status - tender is being prepared, not visible to suppliers */
    public static final String STATUS_DRAFT = "DRAFT";
    
    /** Tender is published and visible to suppliers for bidding */
    public static final String STATUS_OPEN = "OPEN";
    
    /** Bidding period has ended, no more bids accepted */
    public static final String STATUS_CLOSED = "CLOSED";
    
    /** Evaluation committee is scoring bids */
    public static final String STATUS_UNDER_EVALUATION = "UNDER_EVALUATION";
    
    /** Evaluation complete, ready for award decision */
    public static final String STATUS_EVALUATED = "EVALUATED";
    
    /** Contract awarded to winning supplier */
    public static final String STATUS_AWARDED = "AWARDED";
    
    /** Valid statuses set for validation */
    public static final Set<String> VALID_STATUSES = new HashSet<String>(Arrays.asList(
        STATUS_DRAFT, STATUS_OPEN, STATUS_CLOSED, 
        STATUS_UNDER_EVALUATION, STATUS_EVALUATED, STATUS_AWARDED
    ));
    
    /**
     * Validates if a status string is valid.
     * @param status the status string to validate
     * @return true if status is one of the defined constants
     */
    public static boolean isValidStatus(String status) {
        return status != null && VALID_STATUSES.contains(status.toUpperCase());
    }
    
    /**
     * Validates if a status transition is allowed per Module 2 workflow.
     * Valid transitions: DRAFT→OPEN→CLOSED→UNDER_EVALUATION→EVALUATED→AWARDED
     * @param fromStatus current status
     * @param toStatus proposed new status
     * @return true if transition is valid, false otherwise
     */
    public static boolean isValidTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) return false;
        
        String from = fromStatus.toUpperCase();
        String to = toStatus.toUpperCase();
        
        // Only allow forward transitions in the lifecycle
        if (STATUS_DRAFT.equals(from) && STATUS_OPEN.equals(to)) return true;
        if (STATUS_OPEN.equals(from) && STATUS_CLOSED.equals(to)) return true;
        if (STATUS_CLOSED.equals(from) && STATUS_UNDER_EVALUATION.equals(to)) return true;
        if (STATUS_UNDER_EVALUATION.equals(from) && STATUS_EVALUATED.equals(to)) return true;
        if (STATUS_EVALUATED.equals(from) && STATUS_AWARDED.equals(to)) return true;
        
        // Allow staying in same status (no-op)
        if (from.equals(to)) return true;
        
        return false;
    }
    
    /**
     * Gets the next valid status in the lifecycle.
     * @param currentStatus current status
     * @return next valid status, or null if at end of lifecycle
     */
    public static String getNextStatus(String currentStatus) {
        if (currentStatus == null) return null;
        
        String status = currentStatus.toUpperCase();
        
        if (STATUS_DRAFT.equals(status)) return STATUS_OPEN;
        if (STATUS_OPEN.equals(status)) return STATUS_CLOSED;
        if (STATUS_CLOSED.equals(status)) return STATUS_UNDER_EVALUATION;
        if (STATUS_UNDER_EVALUATION.equals(status)) return STATUS_EVALUATED;
        if (STATUS_EVALUATED.equals(status)) return STATUS_AWARDED;
        
        return null; // Already at final status
    }

    // ==================== TENDER CATEGORIES (Module 2 Requirement) ====================
    /** Valid tender categories - must match database ENUM */
    public static final String[] CATEGORIES = {
        "Construction", 
        "Roads", 
        "Electrical", 
        "Plumbing", 
        "General Services"
    };
    
    /** Valid categories set for validation */
    public static final Set<String> VALID_CATEGORIES = new HashSet<String>(Arrays.asList(CATEGORIES));
    
    /**
     * Validates if a category string is valid.
     * @param category the category string to validate
     * @return true if category is one of the defined constants
     */
    public static boolean isValidCategory(String category) {
        return category != null && VALID_CATEGORIES.contains(category.trim());
    }

    // ==================== FILE UPLOAD CONSTRAINTS (Module 2 Requirement) ====================
    /** Maximum size for tender notice PDF uploads: 5MB */
    public static final long MAX_TENDER_FILE_SIZE = 5 * 1024 * 1024; // 5,242,880 bytes
    
    /** Maximum size for bid supporting document uploads: 10MB */
    public static final long MAX_BID_FILE_SIZE = 10 * 1024 * 1024; // 10,485,760 bytes
    
    /** Allowed MIME types for tender notices (PDF only per requirement) */
    public static final String[] ALLOWED_TENDER_MIME_TYPES = {
        "application/pdf"
    };
    
    /** Allowed MIME types for bid supporting documents */
    public static final String[] ALLOWED_BID_MIME_TYPES = {
        "application/pdf", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/msword" // .doc
    };
    
    /** Valid tender MIME types set for validation */
    public static final Set<String> VALID_TENDER_MIME_TYPES = new HashSet<String>(
        Arrays.asList(ALLOWED_TENDER_MIME_TYPES)
    );
    
    /** Valid bid MIME types set for validation */
    public static final Set<String> VALID_BID_MIME_TYPES = new HashSet<String>(
        Arrays.asList(ALLOWED_BID_MIME_TYPES)
    );
    
    /**
     * Validates if a MIME type is allowed for tender notices.
     * @param mimeType the MIME type string to validate
     * @return true if MIME type is allowed for tenders
     */
    public static boolean isValidTenderMimeType(String mimeType) {
        return mimeType != null && VALID_TENDER_MIME_TYPES.contains(mimeType.toLowerCase().trim());
    }
    
    /**
     * Validates if a MIME type is allowed for bid documents.
     * @param mimeType the MIME type string to validate
     * @return true if MIME type is allowed for bids
     */
    public static boolean isValidBidMimeType(String mimeType) {
        return mimeType != null && VALID_BID_MIME_TYPES.contains(mimeType.toLowerCase().trim());
    }
    
    /**
     * Formats file size in human-readable format (e.g., "5.2 MB").
     * @param bytes file size in bytes
     * @return human-readable size string
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    // ==================== EVALUATION SCORING (Module 4 Requirement) ====================
    /** Weight for Price Score in weighted total calculation (40%) */
    public static final double WEIGHT_PRICE = 0.40;
    
    /** Weight for Technical Compliance Score in weighted total calculation (35%) */
    public static final double WEIGHT_TECHNICAL = 0.35;
    
    /** Weight for Delivery Timeline Score in weighted total calculation (25%) */
    public static final double WEIGHT_TIMELINE = 0.25;
    
    /** Minimum score per criterion (0-100 scale) */
    public static final double MIN_SCORE = 0.0;
    
    /** Maximum score per criterion (0-100 scale) */
    public static final double MAX_SCORE = 100.0;
    
    /**
     * Calculates weighted total score from individual criterion scores.
     * Formula: (price * 0.40) + (technical * 0.35) + (timeline * 0.25)
     * @param priceScore score for price criterion (0-100)
     * @param technicalScore score for technical criterion (0-100)
     * @param timelineScore score for timeline criterion (0-100)
     * @return weighted total score rounded to 2 decimal places
     */
    public static double calculateWeightedTotal(double priceScore, double technicalScore, double timelineScore) {
        // Validate scores are in valid range
        priceScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, priceScore));
        technicalScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, technicalScore));
        timelineScore = Math.max(MIN_SCORE, Math.min(MAX_SCORE, timelineScore));
        
        // Calculate weighted total
        double total = (priceScore * WEIGHT_PRICE) + 
                      (technicalScore * WEIGHT_TECHNICAL) + 
                      (timelineScore * WEIGHT_TIMELINE);
        
        // Round to 2 decimal places
        return Math.round(total * 100.0) / 100.0;
    }
    
    /**
     * Validates if a score is within the valid range (0-100).
     * @param score the score to validate
     * @return true if score is between MIN_SCORE and MAX_SCORE inclusive
     */
    public static boolean isValidScore(double score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    // ==================== REFERENCE NUMBER FORMAT (Module 2 Requirement) ====================
    /** Prefix for tender reference numbers */
    public static final String REFERENCE_PREFIX = "MPW";
    
    /** Format pattern for reference numbers: MPW-YYYY-NNNN */
    public static final String REFERENCE_PATTERN = "%s-%d-%04d";
    
    /**
     * Generates a reference number in the format: MPW-YYYY-NNNN
     * @param year the year (e.g., 2026)
     * @param sequence the sequence number (1-9999)
     * @return formatted reference number string
     */
    public static String generateReferenceNumber(int year, int sequence) {
        return String.format(REFERENCE_PATTERN, REFERENCE_PREFIX, year, sequence);
    }
    
    /**
     * Validates if a reference number matches the expected format.
     * @param referenceNo the reference number to validate
     * @return true if format matches MPW-YYYY-NNNN
     */
    public static boolean isValidReferenceFormat(String referenceNo) {
        if (referenceNo == null) return false;
        return referenceNo.matches("^MPW-\\d{4}-\\d{4}$");
    }

    // ==================== CONFIGURATION KEYS (Module 5 Requirement) ====================
    /** JNDI name for Tomcat DataSource connection pool */
    public static final String JNDI_DATASOURCE_NAME = "jdbc/ProcureGovDB";
    
    /** Full JNDI lookup path for DataSource */
    public static final String JNDI_FULL_PATH = "java:comp/env/" + JNDI_DATASOURCE_NAME;
    
    /** web.xml context-param name for configurable upload directory (outside WAR) */
    public static final String UPLOAD_BASE_PATH_PARAM = "upload.base.path";
    
    /** Default upload subdirectory for tender notices */
    public static final String UPLOAD_SUBDIR_TENDERS = "tenders";
    
    /** Default upload subdirectory for bid documents */
    public static final String UPLOAD_SUBDIR_BIDS = "bids";
    
    /** Application character encoding (must match web.xml context-param) */
    public static final String CHARACTER_ENCODING = "UTF-8";
    
    /** Session attribute name for authenticated user */
    public static final String SESSION_USER = "user";
    
    /** Session attribute name for user role */
    public static final String SESSION_USER_ROLE = "userRole";
    
    /** Session attribute name for failed login attempts */
    public static final String SESSION_FAILED_ATTEMPTS = "failedAttempts";
    
    /** Maximum failed login attempts before account lockout */
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
    
    /** Session timeout in minutes (must match web.xml session-config) */
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    // ==================== ERROR MESSAGES (User-Friendly) ====================
    public static final String ERROR_INVALID_ROLE = "Invalid user role.";
    public static final String ERROR_INVALID_STATUS = "Invalid tender status.";
    public static final String ERROR_INVALID_TRANSITION = "Invalid status transition.";
    public static final String ERROR_INVALID_CATEGORY = "Invalid tender category.";
    public static final String ERROR_FILE_TOO_LARGE = "File exceeds maximum size limit.";
    public static final String ERROR_INVALID_FILE_TYPE = "File type not allowed.";
    public static final String ERROR_INVALID_SCORE = "Score must be between 0 and 100.";
    public static final String ERROR_INVALID_REFERENCE = "Reference number format invalid.";
    public static final String ERROR_DATABASE = "Database error occurred. Please try again.";
    public static final String ERROR_UNAUTHORIZED = "You do not have permission to perform this action.";
}