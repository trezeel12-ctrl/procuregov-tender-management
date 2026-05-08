package com.procuregov.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Dedicated service class for all score calculations.
 * Module 4 Requirement: "All score calculations must be performed in a dedicated service class"
 */
public class ScoreCalculator {

    private static final BigDecimal PRICE_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal TECHNICAL_WEIGHT = new BigDecimal("0.35");
    private static final BigDecimal TIMELINE_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Calculate Price Score: (Lowest Bid Amount / This Bid Amount) × 100
     */
    public BigDecimal calculatePriceScore(BigDecimal lowestBid, BigDecimal thisBid) {
        if (lowestBid == null || thisBid == null || lowestBid.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rawScore = lowestBid.multiply(HUNDRED).divide(thisBid, 4, RoundingMode.HALF_UP);
        return rawScore.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Timeline Score: (Shortest Proposed Timeline / This Bid's Timeline) × 100
     */
    public BigDecimal calculateTimelineScore(int shortestTimeline, int thisTimeline) {
        if (shortestTimeline <= 0 || thisTimeline <= 0) {
            return BigDecimal.ZERO;
        }
        double rawScore = (double) shortestTimeline / thisTimeline * 100;
        return BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Weighted Total: (Price × 0.40) + (Technical × 0.35) + (Timeline × 0.25)
     */
    public BigDecimal calculateWeightedTotal(BigDecimal priceScore, BigDecimal technicalScore, BigDecimal timelineScore) {
        if (priceScore == null) priceScore = BigDecimal.ZERO;
        if (technicalScore == null) technicalScore = BigDecimal.ZERO;
        if (timelineScore == null) timelineScore = BigDecimal.ZERO;

        BigDecimal weightedPrice = priceScore.multiply(PRICE_WEIGHT);
        BigDecimal weightedTech = technicalScore.multiply(TECHNICAL_WEIGHT);
        BigDecimal weightedTimeline = timelineScore.multiply(TIMELINE_WEIGHT);
        
        return weightedPrice.add(weightedTech).add(weightedTimeline)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Final Score by averaging Weighted Totals from multiple evaluators.
     */
    public BigDecimal calculateFinalScore(List<BigDecimal> weightedTotals) {
        if (weightedTotals == null || weightedTotals.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal total : weightedTotals) {
            if (total != null) {
                sum = sum.add(total);
            }
        }
        return sum.divide(BigDecimal.valueOf(weightedTotals.size()), 2, RoundingMode.HALF_UP);
    }
}