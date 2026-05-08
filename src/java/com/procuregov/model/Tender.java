package com.procuregov.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * =========================================================
 * Tender JavaBean
 * ---------------------------------------------------------
 * Represents a government tender entity.
 * Maps directly to the 'tenders' database table.
 *
 * Requirements Covered:
 * - Module 2: JavaBean encapsulation
 * - Module 5: Used in DAO layer
 * =========================================================
 */
public class Tender {

    // ================= PRIMARY KEY =================
    private int tenderId;

    // ================= IDENTIFICATION =================
    private String referenceNo;   // Format: MPW-YYYY-NNNN

    // ================= BASIC DETAILS =================
    private String title;
    private String category;
    private String description;

    // ================= FINANCIAL =================
    private BigDecimal estimatedValue;

    // ================= TIMELINE =================
    private LocalDateTime closingDateTime;

    // ================= FILE =================
    private String noticeFilePath;

    // ================= STATUS =================
    private String status;

    // ================= AUDIT FIELDS =================
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================= CONSTRUCTOR =================
    public Tender() {
        // Default constructor required for JavaBeans
    }

    // ================= GETTERS & SETTERS =================

    public int getTenderId() {
        return tenderId;
    }

    public void setTenderId(int tenderId) {
        this.tenderId = tenderId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public LocalDateTime getClosingDateTime() {
        return closingDateTime;
    }

    public void setClosingDateTime(LocalDateTime closingDateTime) {
        this.closingDateTime = closingDateTime;
    }

    public String getNoticeFilePath() {
        return noticeFilePath;
    }

    public void setNoticeFilePath(String noticeFilePath) {
        this.noticeFilePath = noticeFilePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ================= DEBUG METHOD =================
    @Override
    public String toString() {

        return "Tender {" +
                "referenceNo='" + referenceNo + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                "}";

    }
}