package com.procuregov.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bid {
    
    private int bidId;
    private int tenderId;
    private int supplierId;
    private BigDecimal bidAmount;
    private String technicalStatement;
    private int proposedTimelineDays;
    private String supportingDocPath;
    private LocalDateTime submittedAt;
    
    // Additional fields for display and scoring
    private String supplierName;
    private String tenderStatus;
    private String tenderReference;
    private double priceScore;
    private double timelineScore;
    private double technicalScore;
    private double weightedTotal;

    // Constructors
    public Bid() {}
    
    public Bid(int tenderId, int supplierId, BigDecimal bidAmount, String technicalStatement, 
               int proposedTimelineDays, String supportingDocPath) {
        this.tenderId = tenderId;
        this.supplierId = supplierId;
        this.bidAmount = bidAmount;
        this.technicalStatement = technicalStatement;
        this.proposedTimelineDays = proposedTimelineDays;
        this.supportingDocPath = supportingDocPath;
    }

    // Getters and Setters
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }

    public String getTechnicalStatement() { return technicalStatement; }
    public void setTechnicalStatement(String technicalStatement) { this.technicalStatement = technicalStatement; }

    public int getProposedTimelineDays() { return proposedTimelineDays; }
    public void setProposedTimelineDays(int proposedTimelineDays) { this.proposedTimelineDays = proposedTimelineDays; }

    public String getSupportingDocPath() { return supportingDocPath; }
    public void setSupportingDocPath(String supportingDocPath) { this.supportingDocPath = supportingDocPath; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getTenderStatus() { return tenderStatus; }
    public void setTenderStatus(String tenderStatus) { this.tenderStatus = tenderStatus; }

    public String getTenderReference() { return tenderReference; }
    public void setTenderReference(String tenderReference) { this.tenderReference = tenderReference; }

    public double getPriceScore() { return priceScore; }
    public void setPriceScore(double priceScore) { this.priceScore = priceScore; }

    public double getTimelineScore() { return timelineScore; }
    public void setTimelineScore(double timelineScore) { this.timelineScore = timelineScore; }

    public double getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(double technicalScore) { this.technicalScore = technicalScore; }

    public double getWeightedTotal() { return weightedTotal; }
    public void setWeightedTotal(double weightedTotal) { this.weightedTotal = weightedTotal; }

    @Override
    public String toString() {
        return "Bid{" +
                "bidId=" + bidId +
                ", tenderId=" + tenderId +
                ", supplierId=" + supplierId +
                ", bidAmount=" + bidAmount +
                ", proposedTimelineDays=" + proposedTimelineDays +
                '}';
    }
}