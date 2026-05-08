package com.procuregov.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Asynchronous email service using thread pool.
 * Emails are sent in background without blocking the user.
 */
public class AsyncEmailService {
    
    private static final Logger logger = Logger.getLogger(AsyncEmailService.class.getName());
    
    // Create a separate thread pool for email sending
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(3);
    
    static {
        // Add shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down email executor service...");
            emailExecutor.shutdown();
            try {
                if (!emailExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    emailExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                emailExecutor.shutdownNow();
            }
            logger.info("Email executor service shutdown complete");
        }));
    }
    
    /**
     * Send award notification email asynchronously.
     * This method returns IMMEDIATELY - email sends in background.
     * The user will NOT wait for the email to be sent.
     *
     * @param emailService The email service instance
     * @param toEmail Recipient email address
     * @param supplierName Supplier's company name
     * @param tenderReference Tender reference number
     * @param tenderTitle Tender title
     * @param outcome "Won" or "Not Won"
     * @param bidAmount Supplier's bid amount
     * @param awardedValue Awarded value (if winner)
     * @param awardNoticeUrl URL for award notice
     * @param baseUrl Base URL of application
     */
    public static void sendAwardNotificationAsync(final EmailService emailService,
                                                   final String toEmail,
                                                   final String supplierName,
                                                   final String tenderReference,
                                                   final String tenderTitle,
                                                   final String outcome,
                                                   final String bidAmount,
                                                   final String awardedValue,
                                                   final String awardNoticeUrl,
                                                   final String baseUrl) {
        
        // Submit to thread pool - this returns immediately
        emailExecutor.submit(() -> {
            try {
                logger.info("🎯 [ASYNC] Starting email to: " + toEmail + " for tender: " + tenderReference);
                
                long startTime = System.currentTimeMillis();
                
                // This is the actual email sending - happens in background thread
                boolean sent = emailService.sendAwardNotification(
                    toEmail, supplierName, tenderReference, tenderTitle,
                    outcome, bidAmount, awardedValue, awardNoticeUrl, baseUrl
                );
                
                long duration = System.currentTimeMillis() - startTime;
                
                if (sent) {
                    logger.info("✅ [ASYNC] Email sent successfully to: " + toEmail + 
                               " (took " + duration + "ms)");
                } else {
                    logger.warning("❌ [ASYNC] Email failed to: " + toEmail);
                }
            } catch (Exception e) {
                logger.severe("❌ [ASYNC] Email error for " + toEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Log that email was queued (not sent yet)
        logger.info("📧 [QUEUED] Email queued for background sending to: " + toEmail);
    }
    
    /**
     * Send multiple award notifications asynchronously.
     * Emails are sent in parallel using the thread pool.
     * This method returns IMMEDIATELY.
     *
     * @param emailService Email service instance
     * @param emailBatch List of email details to send
     */
    public static void sendBatchAwardNotificationsAsync(EmailService emailService,
                                                         java.util.List<EmailBatchItem> emailBatch) {
        
        if (emailBatch == null || emailBatch.isEmpty()) {
            logger.info("No emails to send in batch");
            return;
        }
        
        logger.info("📧📧 [BATCH] Queueing " + emailBatch.size() + " emails for background sending");
        
        long startTime = System.currentTimeMillis();
        
        // Submit all emails to thread pool - each runs in parallel
        for (EmailBatchItem item : emailBatch) {
            sendAwardNotificationAsync(emailService,
                item.toEmail,
                item.supplierName,
                item.tenderReference,
                item.tenderTitle,
                item.outcome,
                item.bidAmount,
                item.awardedValue,
                item.awardNoticeUrl,
                item.baseUrl
            );
        }
        
        long queueTime = System.currentTimeMillis() - startTime;
        logger.info("✅ [BATCH] All " + emailBatch.size() + " emails queued in " + queueTime + "ms");
        logger.info("📧 Emails will be sent in background. User can continue working immediately.");
    }
    
    /**
     * Send winner email only asynchronously.
     */
    public static void sendWinnerNotificationAsync(EmailService emailService,
                                                    String toEmail,
                                                    String supplierName,
                                                    String tenderReference,
                                                    String tenderTitle,
                                                    String bidAmount,
                                                    String awardedValue,
                                                    String awardNoticeUrl,
                                                    String baseUrl) {
        sendAwardNotificationAsync(emailService, toEmail, supplierName, 
            tenderReference, tenderTitle, "Won", bidAmount, awardedValue, 
            awardNoticeUrl, baseUrl);
    }
    
    /**
     * Send loser email only asynchronously.
     */
    public static void sendLoserNotificationAsync(EmailService emailService,
                                                   String toEmail,
                                                   String supplierName,
                                                   String tenderReference,
                                                   String tenderTitle,
                                                   String bidAmount,
                                                   String awardNoticeUrl,
                                                   String baseUrl) {
        sendAwardNotificationAsync(emailService, toEmail, supplierName, 
            tenderReference, tenderTitle, "Not Won", bidAmount, "", 
            awardNoticeUrl, baseUrl);
    }
    
    /**
     * Helper class for batch email items
     */
    public static class EmailBatchItem {
        public String toEmail;
        public String supplierName;
        public String tenderReference;
        public String tenderTitle;
        public String outcome;
        public String bidAmount;
        public String awardedValue;
        public String awardNoticeUrl;
        public String baseUrl;
        
        public EmailBatchItem(String toEmail, String supplierName, 
                             String tenderReference, String tenderTitle,
                             String outcome, String bidAmount, 
                             String awardedValue, String awardNoticeUrl,
                             String baseUrl) {
            this.toEmail = toEmail;
            this.supplierName = supplierName;
            this.tenderReference = tenderReference;
            this.tenderTitle = tenderTitle;
            this.outcome = outcome;
            this.bidAmount = bidAmount;
            this.awardedValue = awardedValue;
            this.awardNoticeUrl = awardNoticeUrl;
            this.baseUrl = baseUrl;
        }
    }
    
    /**
     * Get queue status (for monitoring)
     */
    public static String getQueueStatus() {
        if (emailExecutor instanceof java.util.concurrent.ThreadPoolExecutor) {
            java.util.concurrent.ThreadPoolExecutor tpe = 
                (java.util.concurrent.ThreadPoolExecutor) emailExecutor;
            return "Active threads: " + tpe.getActiveCount() + 
                   ", Queue size: " + tpe.getQueue().size() +
                   ", Completed tasks: " + tpe.getCompletedTaskCount();
        }
        return "Queue status unavailable";
    }
}