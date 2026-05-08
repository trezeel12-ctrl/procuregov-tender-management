package com.procuregov.service;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.UserDAO;
import com.procuregov.model.Bid;
import com.procuregov.model.EvaluationScore;
import com.procuregov.model.Tender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Logger;

public class EvaluationService {
    
    private static final Logger logger = Logger.getLogger(EvaluationService.class.getName());
    
    private final EvaluationDAO evaluationDAO;
    private final BidDAO bidDAO;
    private final TenderDAO tenderDAO;
    private final UserDAO userDAO;
    
    private static final BigDecimal PRICE_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal TECHNICAL_WEIGHT = new BigDecimal("0.35");
    private static final BigDecimal TIMELINE_WEIGHT = new BigDecimal("0.25");

    public EvaluationService(EvaluationDAO evaluationDAO, BidDAO bidDAO, TenderDAO tenderDAO, UserDAO userDAO) {
        this.evaluationDAO = evaluationDAO;
        this.bidDAO = bidDAO;
        this.tenderDAO = tenderDAO;
        this.userDAO = userDAO;
        logger.info("EvaluationService initialized");
    }

    // ==================== SCORE CALCULATION METHODS ====================
    
    public List<Bid> getBidsWithAutoScores(int tenderId) {
        try {
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) {
                logger.info("No bids found for tender: " + tenderId);
                return new ArrayList<>();
            }
            
            // Get HIGHEST bid amount for price score (higher bid = higher score)
            BigDecimal highestBid = bidDAO.getHighestBidAmount(tenderId);
            int shortestTimeline = bidDAO.getShortestTimeline(tenderId);
            
            logger.info("Tender " + tenderId + " - Highest Bid: " + highestBid + ", Shortest Timeline: " + shortestTimeline);
            
            for (Bid bid : bids) {
                // Price Score: (This Bid / Highest Bid) × 100 - Higher bid = Higher score
                double priceScore = calculateRawPriceScore(highestBid, bid.getBidAmount());
                double timelineScore = calculateRawTimelineScore(shortestTimeline, bid.getProposedTimelineDays());
                
                bid.setPriceScore(priceScore);
                bid.setTimelineScore(timelineScore);
                
                logger.info("Bid " + bid.getBidId() + " - Amount: " + bid.getBidAmount() + 
                           ", Price Score: " + priceScore + "%, Timeline Score: " + timelineScore + "%");
            }
            return bids;
        } catch (Exception e) {
            logger.severe("getBidsWithAutoScores error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate Price Score: (This Bid Amount / Highest Bid Amount) × 100
     * Higher bid amount = Higher percentage score
     */
    private double calculateRawPriceScore(BigDecimal highestBid, BigDecimal thisBid) {
        if (highestBid == null || thisBid == null || highestBid.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        double result = thisBid.doubleValue() / highestBid.doubleValue() * 100;
        return Math.round(result * 100.0) / 100.0;
    }
    
    /**
     * Calculate Timeline Score: (Shortest Timeline / This Timeline) × 100
     * Shorter timeline = Higher percentage score
     */
    private double calculateRawTimelineScore(int shortestTimeline, int thisTimeline) {
        if (shortestTimeline <= 0 || thisTimeline <= 0) {
            return 0;
        }
        double result = (double) shortestTimeline / thisTimeline * 100;
        return Math.round(result * 100.0) / 100.0;
    }
    
    /**
     * Calculate Weighted Total: (Price × 0.40) + (Technical × 0.35) + (Timeline × 0.25)
     */
    private double calculateWeightedTotal(double priceScore, double technicalScore, double timelineScore) {
        double result = (priceScore * 0.40) + (technicalScore * 0.35) + (timelineScore * 0.25);
        return Math.round(result * 100.0) / 100.0;
    }

    // ==================== SUBMIT SCORES ====================
    
    public boolean submitScores(EvaluationScore score) {
        if (score == null || score.getTechnicalScore() == null) {
            logger.warning("Invalid score data");
            return false;
        }
        
        try {
            if (evaluationDAO.hasEvaluatorSubmitted(score.getBidId(), score.getEvaluatorId())) {
                logger.warning("Evaluator " + score.getEvaluatorId() + " already submitted for bid " + score.getBidId());
                return false;
            }
            
            Bid bid = bidDAO.findById(score.getBidId());
            if (bid == null) {
                logger.warning("Bid not found: " + score.getBidId());
                return false;
            }
            
            int tenderId = bid.getTenderId();
            BigDecimal highestBid = bidDAO.getHighestBidAmount(tenderId);
            int shortestTimeline = bidDAO.getShortestTimeline(tenderId);
            
            double rawPriceScore = calculateRawPriceScore(highestBid, bid.getBidAmount());
            double rawTimelineScore = calculateRawTimelineScore(shortestTimeline, bid.getProposedTimelineDays());
            double technicalScore = score.getTechnicalScore().doubleValue();
            double weightedTotal = calculateWeightedTotal(rawPriceScore, technicalScore, rawTimelineScore);
            
            score.setPriceScore(BigDecimal.valueOf(rawPriceScore).setScale(2, RoundingMode.HALF_UP));
            score.setTimelineScore(BigDecimal.valueOf(rawTimelineScore).setScale(2, RoundingMode.HALF_UP));
            score.setWeightedTotal(BigDecimal.valueOf(weightedTotal).setScale(2, RoundingMode.HALF_UP));
            
            boolean saved = evaluationDAO.insert(score) > 0;
            if (saved) {
                logger.info("Scores saved for bid " + score.getBidId() + " by evaluator " + score.getEvaluatorId());
            }
            return saved;
            
        } catch (Exception e) {
            logger.severe("submitScores error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== STATUS TRANSITIONS ====================
    
    public Tender getTenderById(int tenderId) {
        try {
            return tenderDAO.findById(tenderId);
        } catch (Exception e) {
            logger.severe("getTenderById error: " + e.getMessage());
            return null;
        }
    }
    
    public boolean startEvaluation(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null || !"CLOSED".equals(tender.getStatus())) {
                return false;
            }
            boolean updated = tenderDAO.updateStatus(tenderId, "UNDER_EVALUATION");
            logger.info("Tender " + tenderId + " moved to UNDER_EVALUATION");
            return updated;
        } catch (Exception e) {
            logger.severe("startEvaluation error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean checkAndUpdateTenderStatus(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null || !"UNDER_EVALUATION".equals(tender.getStatus())) {
                return false;
            }
            
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) return false;
            
            List<Integer> evaluatorIds = userDAO.findUserIdsByRole("EVALUATOR");
            if (evaluatorIds == null || evaluatorIds.isEmpty()) return false;
            
            boolean allEvaluatorsCompleted = true;
            for (Bid bid : bids) {
                for (int evaluatorId : evaluatorIds) {
                    if (!evaluationDAO.hasEvaluatorSubmitted(bid.getBidId(), evaluatorId)) {
                        allEvaluatorsCompleted = false;
                        break;
                    }
                }
                if (!allEvaluatorsCompleted) break;
            }
            
            if (allEvaluatorsCompleted) {
                tenderDAO.updateStatus(tenderId, "EVALUATED");
                logger.info("Tender " + tenderId + " auto-transitioned to EVALUATED");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.severe("checkAndUpdateTenderStatus error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean hasEvaluatorSubmittedForTender(int tenderId, int evaluatorId) {
        try {
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) return false;
            for (Bid bid : bids) {
                if (!evaluationDAO.hasEvaluatorSubmitted(bid.getBidId(), evaluatorId)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.warning("hasEvaluatorSubmittedForTender error: " + e.getMessage());
            return false;
        }
    }
    
    public int getCompletedEvaluatorsCount(int tenderId) {
        try {
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) return 0;
            List<Integer> evaluatorIds = userDAO.findUserIdsByRole("EVALUATOR");
            if (evaluatorIds == null || evaluatorIds.isEmpty()) return 0;
            int completed = 0;
            for (int evaluatorId : evaluatorIds) {
                boolean allSubmitted = true;
                for (Bid bid : bids) {
                    if (!evaluationDAO.hasEvaluatorSubmitted(bid.getBidId(), evaluatorId)) {
                        allSubmitted = false;
                        break;
                    }
                }
                if (allSubmitted) {
                    completed++;
                }
            }
            return completed;
        } catch (Exception e) {
            logger.warning("getCompletedEvaluatorsCount error: " + e.getMessage());
            return 0;
        }
    }

    public int getTotalEvaluatorsCount() {
        try {
            List<Integer> evaluators = userDAO.findUserIdsByRole("EVALUATOR");
            return evaluators != null ? evaluators.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== LEADERBOARD ====================
    
    public List<Map<String, Object>> getRankedLeaderboard(int tenderId) {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        try {
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) return leaderboard;
            List<EvaluationScore> allScores = evaluationDAO.findByTenderId(tenderId);
            
            for (Bid bid : bids) {
                List<BigDecimal> weightedTotals = new ArrayList<>();
                double avgTechnical = 0;
                int scoreCount = 0;
                double priceScoreSum = 0;
                double timelineScoreSum = 0;
                
                for (EvaluationScore s : allScores) {
                    if (s.getBidId() == bid.getBidId()) {
                        weightedTotals.add(s.getWeightedTotal());
                        if (s.getTechnicalScore() != null) {
                            avgTechnical += s.getTechnicalScore().doubleValue();
                        }
                        if (s.getPriceScore() != null) {
                            priceScoreSum += s.getPriceScore().doubleValue();
                        }
                        if (s.getTimelineScore() != null) {
                            timelineScoreSum += s.getTimelineScore().doubleValue();
                        }
                        scoreCount++;
                    }
                }
                
                double finalScore = 0;
                double avgPriceScore = 0;
                double avgTimelineScore = 0;
                
                if (scoreCount > 0) {
                    for (BigDecimal wt : weightedTotals) {
                        finalScore += wt.doubleValue();
                    }
                    finalScore = finalScore / scoreCount;
                    avgTechnical = avgTechnical / scoreCount;
                    avgPriceScore = priceScoreSum / scoreCount;
                    avgTimelineScore = timelineScoreSum / scoreCount;
                }
                
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("bidId", bid.getBidId());
                entry.put("supplierName", bid.getSupplierName());
                entry.put("bidAmount", bid.getBidAmount());
                entry.put("timelineDays", bid.getProposedTimelineDays());
                entry.put("finalScore", Math.round(finalScore * 100) / 100.0);
                entry.put("priceScore", Math.round(avgPriceScore * 100) / 100.0);
                entry.put("technicalScore", Math.round(avgTechnical * 100) / 100.0);
                entry.put("timelineScore", Math.round(avgTimelineScore * 100) / 100.0);
                entry.put("evaluatorCount", scoreCount);
                leaderboard.add(entry);
            }
            
            leaderboard.sort((a, b) -> Double.compare(
                (Double) b.get("finalScore"), (Double) a.get("finalScore")
            ));
            
            int rank = 1;
            for (Map<String, Object> entry : leaderboard) {
                entry.put("rank", rank++);
            }
        } catch (Exception e) {
            logger.severe("getRankedLeaderboard error: " + e.getMessage());
        }
        return leaderboard;
    }
    
    public boolean hasAnyEvaluationForTender(int tenderId) {
        try {
            return evaluationDAO.hasAnyEvaluationForTender(tenderId);
        } catch (Exception e) {
            logger.severe("hasAnyEvaluationForTender error: " + e.getMessage());
            return false;
        }
    }

    public int getEvaluationCountForTender(int tenderId) {
        try {
            return evaluationDAO.getEvaluationCountForTender(tenderId);
        } catch (Exception e) {
            logger.severe("getEvaluationCountForTender error: " + e.getMessage());
            return 0;
        }
    }
}