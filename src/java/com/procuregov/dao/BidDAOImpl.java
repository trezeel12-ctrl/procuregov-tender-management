package com.procuregov.dao;

import com.procuregov.model.Bid;
import com.procuregov.util.DBConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * BidDAO Implementation - Handles all database operations for Bid entities.
 * 
 * Exam Compliance:
 * - Module 5: All database operations go through dedicated DAO classes
 * - Module 5: Each DAO implements a corresponding interface
 * - Module 5: SQLExceptions caught and logged, never displayed to user
 * - Module 3: Bid data encapsulated in JavaBean before passing to DAO
 */
public class BidDAOImpl implements BidDAO {

    private static final Logger logger = Logger.getLogger(BidDAOImpl.class.getName());

    /**
     * Gets a database connection from the JNDI connection pool.
     * @return Connection from pool
     * @throws Exception if connection fails
     */
    private Connection getConnection() throws Exception {
        return DBConnectionPool.getConnection();
    }

    /**
     * Finds a bid by its primary key.
     * @param bidId the bid ID to search for
     * @return Bid object if found, null otherwise
     */
    @Override
    public Bid findById(int bidId) {
        String sql = "SELECT b.*, u.full_name as supplier_name, t.status as tender_status, t.reference_no as tender_reference " +
                     "FROM bids b " +
                     "LEFT JOIN users u ON b.supplier_id = u.user_id " +
                     "LEFT JOIN tenders t ON b.tender_id = t.tender_id " +
                     "WHERE b.bid_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bidId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Bid bid = mapRow(rs);
                bid.setSupplierName(rs.getString("supplier_name"));
                bid.setTenderStatus(rs.getString("tender_status"));
                bid.setTenderReference(rs.getString("tender_reference"));
                return bid;
            }
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.findById] Error fetching bid ID " + bidId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds all bids for a specific tender.
     * Includes supplier name for display in evaluation panel.
     * @param tenderId the tender ID to search for
     * @return List of Bid objects for the tender
     */
    @Override
    public List<Bid> findByTenderId(int tenderId) {
        List<Bid> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as supplier_name, t.status as tender_status, t.reference_no as tender_reference " +
                     "FROM bids b " +
                     "LEFT JOIN users u ON b.supplier_id = u.user_id " +
                     "LEFT JOIN tenders t ON b.tender_id = t.tender_id " +
                     "WHERE b.tender_id = ? ORDER BY b.bid_amount ASC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bid bid = mapRow(rs);
                bid.setSupplierName(rs.getString("supplier_name"));
                bid.setTenderStatus(rs.getString("tender_status"));
                bid.setTenderReference(rs.getString("tender_reference"));
                list.add(bid);
            }
            logger.info("[BidDAOImpl.findByTenderId] Found " + list.size() + " bids for tender " + tenderId);
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.findByTenderId] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Finds all bids submitted by a specific supplier.
     * Includes tender status and reference for display in supplier dashboard.
     * @param supplierId the supplier ID to search for
     * @return List of Bid objects submitted by the supplier
     */
    @Override
    public List<Bid> findBySupplierId(int supplierId) {
        List<Bid> list = new ArrayList<>();
        String sql = "SELECT b.*, t.status as tender_status, t.reference_no as tender_reference, " +
                     "u.full_name as supplier_name " +
                     "FROM bids b " +
                     "LEFT JOIN tenders t ON b.tender_id = t.tender_id " +
                     "LEFT JOIN users u ON b.supplier_id = u.user_id " +
                     "WHERE b.supplier_id = ? ORDER BY b.submitted_at DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bid bid = mapRow(rs);
                bid.setTenderStatus(rs.getString("tender_status"));
                bid.setTenderReference(rs.getString("tender_reference"));
                bid.setSupplierName(rs.getString("supplier_name"));
                list.add(bid);
            }
            logger.info("[BidDAOImpl.findBySupplierId] Found " + list.size() + " bids for supplier " + supplierId);
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.findBySupplierId] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks if a supplier has already submitted a bid for a specific tender.
     * Module 3: Enforces one bid per supplier per tender.
     * @param tenderId the tender ID to check
     * @param supplierId the supplier ID to check
     * @return true if supplier has already bid, false otherwise
     */
    @Override
    public boolean hasSupplierBid(int tenderId, int supplierId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE tender_id = ? AND supplier_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ps.setInt(2, supplierId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean hasBid = rs.getInt(1) > 0;
                logger.info("[BidDAOImpl.hasSupplierBid] Supplier " + supplierId + " has bid on tender " + tenderId + ": " + hasBid);
                return hasBid;
            }
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.hasSupplierBid] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inserts a new bid into the database.
     * @param bid the Bid JavaBean to insert
     * @return generated bid_id if successful, 0 otherwise
     */
    @Override
    public int insert(Bid bid) {
        String sql = "INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_statement, " +
                     "proposed_timeline_days, supporting_doc_path, submitted_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bid.getTenderId());
            ps.setInt(2, bid.getSupplierId());
            ps.setBigDecimal(3, bid.getBidAmount());
            ps.setString(4, bid.getTechnicalStatement());
            ps.setInt(5, bid.getProposedTimelineDays());
            ps.setString(6, bid.getSupportingDocPath());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    logger.info("[BidDAOImpl.insert] Successfully inserted bid ID " + generatedId);
                    return generatedId;
                }
            }
            logger.warning("[BidDAOImpl.insert] Insert executed but no ID generated");
            return 0;
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.insert] Error inserting bid: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Gets the lowest bid amount for a specific tender.
     * Used for Price Score calculation in Module 4.
     * @param tenderId the tender ID
     * @return lowest bid amount as BigDecimal, or null if no bids
     */
    @Override
    public BigDecimal getLowestBidAmount(int tenderId) {
        String sql = "SELECT MIN(bid_amount) FROM bids WHERE tender_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal lowest = rs.getBigDecimal(1);
                logger.info("[BidDAOImpl.getLowestBidAmount] Lowest bid for tender " + tenderId + ": " + lowest);
                return lowest;
            }
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.getLowestBidAmount] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the shortest proposed timeline (in days) for a specific tender.
     * Used for Timeline Score calculation in Module 4.
     * @param tenderId the tender ID
     * @return shortest timeline in days, or 0 if no bids
     */
    @Override
    public int getShortestTimeline(int tenderId) {
        String sql = "SELECT MIN(proposed_timeline_days) FROM bids WHERE tender_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int shortest = rs.getInt(1);
                logger.info("[BidDAOImpl.getShortestTimeline] Shortest timeline for tender " + tenderId + ": " + shortest);
                return shortest;
            }
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.getShortestTimeline] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Updates an existing bid (rarely used, mainly for testing).
     * @param bid the Bid object with updated values
     * @return true if update succeeded, false otherwise
     */
    @Override
    public boolean update(Bid bid) {
        String sql = "UPDATE bids SET bid_amount = ?, technical_statement = ?, " +
                     "proposed_timeline_days = ?, supporting_doc_path = ? WHERE bid_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, bid.getBidAmount());
            ps.setString(2, bid.getTechnicalStatement());
            ps.setInt(3, bid.getProposedTimelineDays());
            ps.setString(4, bid.getSupportingDocPath());
            ps.setInt(5, bid.getBidId());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("[BidDAOImpl.update] Successfully updated bid ID " + bid.getBidId());
                return true;
            }
            logger.warning("[BidDAOImpl.update] No rows updated for bid ID " + bid.getBidId());
            return false;
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.update] Error updating bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a bid by ID (used for testing/admin purposes).
     * @param bidId the bid ID to delete
     * @return true if deletion succeeded, false otherwise
     */
    @Override
    public boolean delete(int bidId) {
        String sql = "DELETE FROM bids WHERE bid_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bidId);
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                logger.info("[BidDAOImpl.delete] Successfully deleted bid ID " + bidId);
                return true;
            }
            logger.warning("[BidDAOImpl.delete] No rows deleted for bid ID " + bidId);
            return false;
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.delete] Error deleting bid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Counts total bids for a specific tender.
     * @param tenderId the tender ID
     * @return total number of bids
     */
    @Override
    public int countByTenderId(int tenderId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE tender_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            logger.severe("countByTenderId error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets all bids for a tender with their evaluation scores already calculated.
     * Used for the evaluation panel to display pre-calculated scores.
     * @param tenderId the tender ID
     * @return List of bids with score information
     */
    @Override
    public List<Bid> findByTenderIdWithScores(int tenderId) {
        List<Bid> list = new ArrayList<>();
        String sql = "SELECT b.*, u.full_name as supplier_name, " +
                     "AVG(es.price_score) as avg_price_score, " +
                     "AVG(es.timeline_score) as avg_timeline_score, " +
                     "AVG(es.technical_score) as avg_technical_score, " +
                     "AVG(es.weighted_total) as avg_weighted_total " +
                     "FROM bids b " +
                     "LEFT JOIN users u ON b.supplier_id = u.user_id " +
                     "LEFT JOIN evaluation_scores es ON b.bid_id = es.bid_id " +
                     "WHERE b.tender_id = ? " +
                     "GROUP BY b.bid_id ORDER BY avg_weighted_total DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bid bid = mapRow(rs);
                bid.setSupplierName(rs.getString("supplier_name"));
                bid.setPriceScore(rs.getDouble("avg_price_score"));
                bid.setTimelineScore(rs.getDouble("avg_timeline_score"));
                bid.setTechnicalScore(rs.getDouble("avg_technical_score"));
                list.add(bid);
            }
            logger.info("[BidDAOImpl.findByTenderIdWithScores] Found " + list.size() + " bids with scores for tender " + tenderId);
        } catch (Exception e) {
            logger.severe("[BidDAOImpl.findByTenderIdWithScores] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Maps a ResultSet row to a Bid JavaBean.
     * @param rs the ResultSet positioned on a row
     * @return populated Bid object
     * @throws SQLException if mapping fails
     */
    private Bid mapRow(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getInt("bid_id"));
        bid.setTenderId(rs.getInt("tender_id"));
        bid.setSupplierId(rs.getInt("supplier_id"));
        bid.setBidAmount(rs.getBigDecimal("bid_amount"));
        bid.setTechnicalStatement(rs.getString("technical_statement"));
        bid.setProposedTimelineDays(rs.getInt("proposed_timeline_days"));
        bid.setSupportingDocPath(rs.getString("supporting_doc_path"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) {
            bid.setSubmittedAt(ts.toLocalDateTime());
        }
        return bid;
    }
    
    @Override
    public BigDecimal getHighestBidAmount(int tenderId) {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE tender_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tenderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal highest = rs.getBigDecimal(1);
                logger.info("Highest bid for tender " + tenderId + ": " + highest);
                return highest;
            }
        } catch (Exception e) {
            logger.severe("getHighestBidAmount error: " + e.getMessage());
        }
        return null;
    }
    
}