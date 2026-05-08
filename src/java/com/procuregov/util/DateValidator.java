package com.procuregov.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Server-side date/time validation utility.
 * Enforces tender closing deadlines using LocalDateTime as required by Module 3.
 */
public final class DateValidator {
    private DateValidator() {}
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Checks if the current server time has passed the specified deadline.
     * @param closingDateTime the tender's configured closing time
     * @return true if deadline has passed
     */
    public static boolean isDeadlinePassed(LocalDateTime closingDateTime) {
        if (closingDateTime == null) return true;
        return LocalDateTime.now().isAfter(closingDateTime);
    }

    /**
     * Safely parses a datetime string from form input.
     * @param dateString input in yyyy-MM-ddTHH:mm format
     * @return parsed LocalDateTime or null if invalid
     */
    public static LocalDateTime parse(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateString.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("[DateValidator] Invalid date format: " + dateString);
            return null;
        }
    }
}