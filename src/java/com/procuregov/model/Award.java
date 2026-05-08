package com.procuregov.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JavaBean representing the final contract award for a tender.
 * Maps directly to the 'awards' table.
 */
public class Award {
    private int awardId;
    private int tenderId;
    private int winningBidId;
    private BigDecimal awardedValue;
    private String justification;
    private LocalDate awardDate;
    private int awardedBy; // Procurement Officer user_id

    public Award() {}

    public int getAwardId() { return awardId; }
    public void setAwardId(int awardId) { this.awardId = awardId; }

    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }

    public int getWinningBidId() { return winningBidId; }
    public void setWinningBidId(int winningBidId) { this.winningBidId = winningBidId; }

    public BigDecimal getAwardedValue() { return awardedValue; }
    public void setAwardedValue(BigDecimal awardedValue) { this.awardedValue = awardedValue; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public LocalDate getAwardDate() { return awardDate; }
    public void setAwardDate(LocalDate awardDate) { this.awardDate = awardDate; }

    public int getAwardedBy() { return awardedBy; }
    public void setAwardedBy(int awardedBy) { this.awardedBy = awardedBy; }

    @Override
    public String toString() {
        return "Award{tenderId=" + tenderId + ", winningBidId=" + winningBidId + ", value=" + awardedValue + "}";
    }
}