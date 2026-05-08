package com.procuregov.dao;

import com.procuregov.model.EvaluationScore;
import com.procuregov.util.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EvaluationDAOImpl implements EvaluationDAO {
    
    private static final Logger logger = Logger.getLogger(EvaluationDAOImpl.class.getName());

    private Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection();
    }

    @Override
    public EvaluationScore findByBidAndEvaluator(int bidId, int evaluatorId) {
        String sql = "SELECT * FROM evaluation_scores WHERE bid_id = ? AND evaluator_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bidId);
            ps.setInt(2, evaluatorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.severe("findByBidAndEvaluator error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<EvaluationScore> findByTenderId(int tenderId) {
        List<EvaluationScore> scores = new ArrayList<>();
        String sql = "SELECT es.* FROM evaluation_scores es " +
                     "JOIN bids b ON es.bid_id = b.bid_id " +
                     "WHERE b.tender_id = ? ORDER BY es.submitted_at";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("findByTenderId error: " + e.getMessage());
        }
        return scores;
    }

    @Override
    public List<EvaluationScore> findByEvaluatorId(int evaluatorId) {
        List<EvaluationScore> scores = new ArrayList<>();
        String sql = "SELECT * FROM evaluation_scores WHERE evaluator_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, evaluatorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("findByEvaluatorId error: " + e.getMessage());
        }
        return scores;
    }

    @Override
    public int insert(EvaluationScore score) {
        String sql = "INSERT INTO evaluation_scores (bid_id, evaluator_id, price_score, technical_score, timeline_score, weighted_total) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, score.getBidId());
            ps.setInt(2, score.getEvaluatorId());
            ps.setBigDecimal(3, score.getPriceScore());
            ps.setBigDecimal(4, score.getTechnicalScore());
            ps.setBigDecimal(5, score.getTimelineScore());
            ps.setBigDecimal(6, score.getWeightedTotal());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.severe("insert error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean update(EvaluationScore score) {
        String sql = "UPDATE evaluation_scores SET price_score=?, technical_score=?, timeline_score=?, weighted_total=? WHERE score_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, score.getPriceScore());
            ps.setBigDecimal(2, score.getTechnicalScore());
            ps.setBigDecimal(3, score.getTimelineScore());
            ps.setBigDecimal(4, score.getWeightedTotal());
            ps.setInt(5, score.getScoreId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("update error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Integer> getBidIdsForTender(int tenderId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT bid_id FROM bids WHERE tender_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("bid_id"));
            }
        } catch (SQLException e) {
            logger.severe("getBidIdsForTender error: " + e.getMessage());
        }
        return ids;
    }

    @Override
    public int countDistinctEvaluatorsForTender(int tenderId) {
        String sql = "SELECT COUNT(DISTINCT evaluator_id) FROM evaluation_scores WHERE bid_id IN (SELECT bid_id FROM bids WHERE tender_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("countDistinctEvaluatorsForTender error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean hasEvaluatorSubmitted(int bidId, int evaluatorId) {
        String sql = "SELECT COUNT(*) FROM evaluation_scores WHERE bid_id = ? AND evaluator_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bidId);
            ps.setInt(2, evaluatorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("hasEvaluatorSubmitted error: " + e.getMessage());
        }
        return false;
    }

    private EvaluationScore mapRow(ResultSet rs) throws SQLException {
        EvaluationScore score = new EvaluationScore();
        score.setScoreId(rs.getInt("score_id"));
        score.setBidId(rs.getInt("bid_id"));
        score.setEvaluatorId(rs.getInt("evaluator_id"));
        score.setPriceScore(rs.getBigDecimal("price_score"));
        score.setTechnicalScore(rs.getBigDecimal("technical_score"));
        score.setTimelineScore(rs.getBigDecimal("timeline_score"));
        score.setWeightedTotal(rs.getBigDecimal("weighted_total"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) {
            score.setSubmittedAt(ts.toLocalDateTime());
        }
        return score;
    }
    
    @Override
    public boolean hasAnyEvaluationForTender(int tenderId) {
        String sql = "SELECT COUNT(*) FROM evaluation_scores es " +
                     "JOIN bids b ON es.bid_id = b.bid_id " +
                     "WHERE b.tender_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("hasAnyEvaluationForTender error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int getEvaluationCountForTender(int tenderId) {
        String sql = "SELECT COUNT(*) FROM evaluation_scores es " +
                     "JOIN bids b ON es.bid_id = b.bid_id " +
                     "WHERE b.tender_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("getEvaluationCountForTender error: " + e.getMessage());
        }
        return 0;
    }
}