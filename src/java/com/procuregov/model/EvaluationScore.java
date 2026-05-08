package com.procuregov.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JavaBean representing a single evaluator's scoring for a specific bid.
 * Maps directly to the 'evaluation_scores' table.
 */
public class EvaluationScore {
    private int scoreId;
    private int bidId;
    private int evaluatorId;
    private BigDecimal priceScore;     // Auto-calculated
    private BigDecimal technicalScore; // Manual input (0-100)
    private BigDecimal timelineScore;  // Auto-calculated
    private BigDecimal weightedTotal;  // Final weighted score
    private LocalDateTime submittedAt;

    public EvaluationScore() {}

    public int getScoreId() { return scoreId; }
    public void setScoreId(int scoreId) { this.scoreId = scoreId; }

    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }

    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }

    public BigDecimal getPriceScore() { return priceScore; }
    public void setPriceScore(BigDecimal priceScore) { this.priceScore = priceScore; }

    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }

    public BigDecimal getTimelineScore() { return timelineScore; }
    public void setTimelineScore(BigDecimal timelineScore) { this.timelineScore = timelineScore; }

    public BigDecimal getWeightedTotal() { return weightedTotal; }
    public void setWeightedTotal(BigDecimal weightedTotal) { this.weightedTotal = weightedTotal; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    @Override
    public String toString() {
        return "EvaluationScore{bidId=" + bidId + ", evaluatorId=" + evaluatorId + ", weightedTotal=" + weightedTotal + "}";
    }
}