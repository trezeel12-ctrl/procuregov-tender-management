package com.procuregov.service;

import java.util.Properties;
import java.util.logging.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 * Email notification service for award notifications.
 * Module 6 Requirement: JavaMail API to send emails to suppliers.
 */
public class EmailService {
    
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());
    
    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final boolean useTls;
    
    /**
     * Constructor for EmailService.
     * @param smtpHost SMTP server host (e.g., smtp.gmail.com)
     * @param smtpPort SMTP server port (587 for TLS, 465 for SSL)
     * @param username Email account username
     * @param password Email account password
     */
    public EmailService(String smtpHost, String smtpPort, String username, String password) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.useTls = true;
        logger.info("EmailService initialized with host: " + smtpHost);
    }
    
    /**
     * Sends award notification to a supplier.
     * 
     * @param toEmail Supplier's email address
     * @param supplierName Supplier's company name
     * @param tenderReference Tender reference number
     * @param tenderTitle Tender title
     * @param outcome "Won" or "Not Won"
     * @param bidAmount Supplier's bid amount
     * @param awardedValue Awarded contract value (if won)
     * @param awardNoticeUrl URL to view award notice
     * @param baseUrl Base URL of the application
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendAwardNotification(String toEmail, String supplierName, 
                                          String tenderReference, String tenderTitle,
                                          String outcome, String bidAmount, 
                                          String awardedValue, String awardNoticeUrl,
                                          String baseUrl) {
        try {
            // Create mail session
            Session session = createMailSession();
            
            // Create email message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "ProcureGov"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(getEmailSubject(tenderReference, outcome));
            
            // Mark as important to help avoid spam
            message.setHeader("X-Priority", "1");
            message.setHeader("Importance", "high");
            message.setHeader("X-MSMail-Priority", "High");
            
            // Set email content (HTML format)
            message.setContent(getEmailContent(supplierName, tenderReference, tenderTitle, 
                              outcome, bidAmount, awardedValue, awardNoticeUrl, baseUrl), 
                              "text/html; charset=UTF-8");
            
            // Send email
            Transport.send(message);
            logger.info("Email sent to " + toEmail + " for tender " + tenderReference);
            return true;
            
        } catch (Exception e) {
            logger.severe("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates and configures the mail session.
     */
    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        
        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
        }
        
        // SSL/TLS configuration for Gmail
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.from", username);
        
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
    
    /**
     * Gets the email subject line.
     */
    private String getEmailSubject(String tenderReference, String outcome) {
        if ("Won".equals(outcome)) {
            return "CONGRATULATIONS! You Won the Tender - " + tenderReference;
        } else {
            return "Tender Award Update - " + tenderReference;
        }
    }
    
    /**
     * Gets the HTML email content - Optimized to avoid spam filters.
     */
    private String getEmailContent(String supplierName, String tenderReference, 
                                   String tenderTitle, String outcome, 
                                   String bidAmount, String awardedValue, 
                                   String awardNoticeUrl, String baseUrl) {
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>ProcureGov Award Notice</title>");
        html.append("</head>");
        html.append("<body style='font-family: Arial, Helvetica, sans-serif; background-color: #f4f7f9; margin: 0; padding: 20px;'>");
        html.append("<div style='max-width: 550px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>");
        
        // Simple Header - No emojis, clean design
        html.append("<div style='background-color: #0d6e2e; padding: 15px 20px; text-align: center;'>");
        html.append("<h2 style='color: #ffffff; margin: 0; font-size: 20px;'>ProcureGov</h2>");
        html.append("<p style='color: #e8f5e9; margin: 5px 0 0; font-size: 12px;'>Ministry of Public Works - Lesotho</p>");
        html.append("</div>");
        
        // Body
        html.append("<div style='padding: 20px;'>");
        
        // Greeting
        html.append("<p style='font-size: 14px; color: #333333;'>Dear <strong>").append(supplierName).append("</strong>,</p>");
        
        // Outcome Box
        if ("Won".equals(outcome)) {
            html.append("<div style='background-color: #d1fae5; padding: 12px; border-radius: 6px; margin: 15px 0; text-align: center; border-left: 4px solid #0d6e2e;'>");
            html.append("<p style='font-size: 16px; font-weight: bold; color: #065f46; margin: 0;'>Congratulations!</p>");
            html.append("<p style='font-size: 13px; color: #065f46; margin: 5px 0 0;'>You have been awarded the contract.</p>");
            html.append("</div>");
        } else {
            html.append("<div style='background-color: #fef3c7; padding: 12px; border-radius: 6px; margin: 15px 0; text-align: center; border-left: 4px solid #d97706;'>");
            html.append("<p style='font-size: 13px; color: #92400e; margin: 0;'>The tender has been awarded to another supplier.</p>");
            html.append("</div>");
        }
        
        // Tender Information Table
        html.append("<h3 style='color: #0d6e2e; font-size: 14px; margin: 20px 0 10px;'>Tender Information</h3>");
        html.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");
        html.append("<tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 8px 0; width: 40%;'><strong>Reference:</strong></td>");
        html.append("<td style='padding: 8px 0;'>").append(tenderReference).append("</td></tr>");
        html.append("<tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 8px 0;'><strong>Title:</strong></td>");
        html.append("<td style='padding: 8px 0;'>").append(tenderTitle).append("</td></tr>");
        html.append("<tr style='border-bottom: 1px solid #e5e7eb;'><td style='padding: 8px 0;'><strong>Your Bid Amount:</strong></td>");
        html.append("<td style='padding: 8px 0;'>M ").append(bidAmount).append("</td></tr>");
        
        if ("Won".equals(outcome) && awardedValue != null && !awardedValue.isEmpty()) {
            html.append("<tr><td style='padding: 8px 0;'><strong>Awarded Value:</strong></td>");
            html.append("<td style='padding: 8px 0;'><strong style='color: #0d6e2e;'>M ").append(awardedValue).append("</strong></td></tr>");
        }
        html.append("</table>");
        
        // Action Button (only for winners)
        if ("Won".equals(outcome)) {
            html.append("<div style='text-align: center; margin: 25px 0 15px;'>");
            html.append("<a href='").append(awardNoticeUrl).append("' style='display: inline-block; background-color: #0d6e2e; color: #ffffff; text-decoration: none; padding: 10px 20px; border-radius: 6px; font-size: 14px; font-weight: bold;'>View Award Notice</a>");
            html.append("</div>");
        } else {
            html.append("<div style='text-align: center; margin: 25px 0 15px;'>");
            html.append("<p style='font-size: 12px; color: #6b7280;'>Thank you for your participation.</p>");
            html.append("</div>");
        }
        
        // Footer with whitelist instruction
        html.append("<hr style='border: none; border-top: 1px solid #e5e7eb; margin: 20px 0 10px;'>");
        html.append("<p style='font-size: 11px; color: #9ca3af; text-align: center; margin: 0;'>");
        html.append("This is an automated message from ProcureGov.<br>");
        html.append("Ministry of Public Works, Kingdom of Lesotho<br>");
        html.append("<strong style='color: #0d6e2e;'>To ensure delivery, please add ").append(username).append(" to your address book.</strong><br>");
        html.append("<a href='mailto:procurement@gov.ls' style='color: #9ca3af;'>procurement@gov.ls</a>");
        html.append("</p>");
        
        html.append("</div>");
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    /**
     * Test method to verify email configuration.
     */
    public boolean sendTestEmail(String toEmail, String subject, String body) {
        try {
            Session session = createMailSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "ProcureGov Test"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            logger.info("Test email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            logger.severe("Test email failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}