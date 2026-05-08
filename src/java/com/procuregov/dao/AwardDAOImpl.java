package com.procuregov.dao;

import com.procuregov.model.Award;
import com.procuregov.util.DBConnectionPool;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of AwardDAO using JDBC and connection pooling.
 * Handles all award-related database operations.
 */
public class AwardDAOImpl implements AwardDAO {
    
    private static final Logger logger = Logger.getLogger(AwardDAOImpl.class.getName());

    private Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection();
    }

    @Override
    public Award findByTenderId(int tenderId) {
        String sql = "SELECT * FROM awards WHERE tender_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tenderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToAward(rs) : null;
            }
        } catch (SQLException e) {
            logger.severe("[AwardDAO.findByTenderId] Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Award findByAwardId(int awardId) {
        String sql = "SELECT * FROM awards WHERE award_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, awardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToAward(rs) : null;
            }
        } catch (SQLException e) {
            logger.severe("[AwardDAO.findByAwardId] Error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int insert(Award award) {
        String sql = "INSERT INTO awards (tender_id, winning_bid_id, awarded_value, justification, award_date, awarded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, award.getTenderId());
            pstmt.setInt(2, award.getWinningBidId());
            pstmt.setBigDecimal(3, award.getAwardedValue());
            pstmt.setString(4, award.getJustification());
            pstmt.setDate(5, Date.valueOf(award.getAwardDate()));
            pstmt.setInt(6, award.getAwardedBy());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        logger.info("[AwardDAO.insert] Successfully inserted award ID: " + generatedId);
                        return generatedId;
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.severe("[AwardDAO.insert] Error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Award> findAllAwards() {
        List<Award> awards = new ArrayList<>();
        String sql = "SELECT * FROM awards ORDER BY award_date DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                awards.add(mapResultSetToAward(rs));
            }
            logger.info("[AwardDAO.findAllAwards] Found " + awards.size() + " awards");
        } catch (SQLException e) {
            logger.severe("[AwardDAO.findAllAwards] Error: " + e.getMessage());
        }
        return awards;
    }

    @Override
    public List<Award> findAwardsByOfficer(int officerId) {
        List<Award> awards = new ArrayList<>();
        String sql = "SELECT * FROM awards WHERE awarded_by = ? ORDER BY award_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, officerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    awards.add(mapResultSetToAward(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("[AwardDAO.findAwardsByOfficer] Error: " + e.getMessage());
        }
        return awards;
    }

    @Override
    public boolean updateAward(Award award) {
        String sql = "UPDATE awards SET awarded_value = ?, justification = ? WHERE award_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, award.getAwardedValue());
            pstmt.setString(2, award.getJustification());
            pstmt.setInt(3, award.getAwardId());
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            logger.severe("[AwardDAO.updateAward] Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAward(int awardId) {
        String sql = "DELETE FROM awards WHERE award_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, awardId);
            int deleted = pstmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            logger.severe("[AwardDAO.deleteAward] Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int getTotalAwardedValue() {
        String sql = "SELECT SUM(awarded_value) FROM awards";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("[AwardDAO.getTotalAwardedValue] Error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getAwardCount() {
        String sql = "SELECT COUNT(*) FROM awards";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("[AwardDAO.getAwardCount] Error: " + e.getMessage());
        }
        return 0;
    }

    private Award mapResultSetToAward(ResultSet rs) throws SQLException {
        Award a = new Award();
        a.setAwardId(rs.getInt("award_id"));
        a.setTenderId(rs.getInt("tender_id"));
        a.setWinningBidId(rs.getInt("winning_bid_id"));
        a.setAwardedValue(rs.getBigDecimal("awarded_value"));
        a.setJustification(rs.getString("justification"));
        Date sqlDate = rs.getDate("award_date");
        a.setAwardDate(sqlDate != null ? sqlDate.toLocalDate() : null);
        a.setAwardedBy(rs.getInt("awarded_by"));
        return a;
    }
}