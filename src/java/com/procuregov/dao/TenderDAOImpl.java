package com.procuregov.dao;

import com.procuregov.model.Tender;
import com.procuregov.util.DBConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * TenderDAO Implementation - Handles all database operations for Tender entities.
 * 
 * Exam Compliance:
 * - Module 5: All database operations go through dedicated DAO classes
 * - Module 5: Each DAO implements a corresponding interface
 * - Module 5: SQLExceptions caught and logged, never displayed to user
 * - Module 2: Tender data encapsulated in JavaBean before passing to DAO
 */
public class TenderDAOImpl implements TenderDAO {
    
    private static final Logger logger = Logger.getLogger(TenderDAOImpl.class.getName());

    /**
     * Gets a database connection from the JNDI connection pool.
     * @return Connection from pool
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection();
    }

    // =========================================================
    // BASIC CRUD OPERATIONS
    // =========================================================

    @Override
    public Tender findById(int tenderId) {
        String sql = "SELECT * FROM tenders WHERE tender_id = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, tenderId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findById] Error fetching tender ID " + tenderId + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Tender> findAll() {
        List<Tender> list = new ArrayList<>();
        String sql = "SELECT * FROM tenders ORDER BY created_at DESC";
        
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findAll] Error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Tender> findByStatus(String status) {
        List<Tender> list = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, status);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findByStatus] Error fetching tenders with status " + status + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Tender> findByCategory(String category) {
        List<Tender> list = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE category = ? ORDER BY created_at DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, category);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findByCategory] Error fetching tenders with category " + category + ": " + e.getMessage());
        }
        return list;
    }
    
    @Override
    public List<Tender> findByStatusAndCategory(String status, String category) {
        List<Tender> list = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE status = ? AND category = ? ORDER BY created_at DESC";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, category);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findByStatusAndCategory] Error fetching tenders: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Tender> findBySearchAndFilters(String search, String status, String category, String sortBy, String sortDir) {
        List<Tender> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tenders WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add search condition
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (reference_no LIKE ? OR title LIKE ? OR description LIKE ?)");
            String searchPattern = "%" + search.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        // Add status filter
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // Add category filter
        if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        // Add sorting
        String sortColumn = getSortColumn(sortBy);
        String direction = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(sortColumn).append(" ").append(direction);
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.findBySearchAndFilters] Error: " + e.getMessage());
        }
        return list;
    }

    private String getSortColumn(String sortBy) {
        if (sortBy == null) return "created_at";
        switch (sortBy) {
            case "ref": return "reference_no";
            case "title": return "title";
            case "value": return "estimated_value";
            case "closing_date": return "closing_datetime";
            case "status": return "status";
            default: return "created_at";
        }
    }

    @Override
    public int insert(Tender t) {
        String sql = "INSERT INTO tenders (reference_no, title, category, description, estimated_value, closing_datetime, notice_file_path, status, created_by, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, t.getReferenceNo());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getCategory());
            ps.setString(4, t.getDescription());
            ps.setBigDecimal(5, t.getEstimatedValue());
            ps.setTimestamp(6, Timestamp.valueOf(t.getClosingDateTime()));
            ps.setString(7, t.getNoticeFilePath());
            ps.setString(8, t.getStatus());
            ps.setInt(9, t.getCreatedBy());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        logger.info("[TenderDAOImpl.insert] Successfully inserted tender ID " + generatedId);
                        return generatedId;
                    }
                }
            }
            
            logger.warning("[TenderDAOImpl.insert] Insert executed but no ID generated");
            return 0;
            
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.insert] Error inserting tender: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean update(Tender t) {
        String sql = "UPDATE tenders SET title = ?, category = ?, description = ?, " +
                     "estimated_value = ?, closing_datetime = ?, notice_file_path = ?, " +
                     "status = ?, updated_at = NOW() WHERE tender_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getCategory());
            ps.setString(3, t.getDescription());
            ps.setBigDecimal(4, t.getEstimatedValue());
            ps.setTimestamp(5, Timestamp.valueOf(t.getClosingDateTime()));
            ps.setString(6, t.getNoticeFilePath());
            ps.setString(7, t.getStatus());
            ps.setInt(8, t.getTenderId());

            int rowsUpdated = ps.executeUpdate();
            
            if (rowsUpdated > 0) {
                logger.info("[TenderDAOImpl.update] Tender updated successfully. ID=" + t.getTenderId());
                return true;
            } else {
                logger.warning("[TenderDAOImpl.update] No rows updated for Tender ID " + t.getTenderId());
                return false;
            }

        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.update] Error updating tender: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStatus(int tenderId, String newStatus) {
        String sql = "UPDATE tenders SET status = ?, updated_at = NOW() WHERE tender_id = ?";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setInt(2, tenderId);
            
            int rowsUpdated = ps.executeUpdate();
            
            if (rowsUpdated > 0) {
                logger.info("[TenderDAOImpl.updateStatus] Successfully updated tender ID " + tenderId + " to status: " + newStatus);
                return true;
            } else {
                logger.warning("[TenderDAOImpl.updateStatus] No rows updated for tender ID " + tenderId);
                return false;
            }
            
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.updateStatus] Error updating status for tender ID " + tenderId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public int closeExpiredTenders() {
        String sql = "UPDATE tenders SET status = 'CLOSED', updated_at = NOW() " +
                     "WHERE status = 'OPEN' AND closing_datetime < NOW()";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                logger.info("[TenderDAOImpl.closeExpiredTenders] Closed " + rowsUpdated + " expired tenders");
            }

            return rowsUpdated;

        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.closeExpiredTenders] Error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int updateStatusByCondition(String sqlCondition) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCondition)) {
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("[TenderDAOImpl.updateStatusByCondition] Updated " + rowsUpdated + " rows");
            }
            return rowsUpdated;
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.updateStatusByCondition] Error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getTotalTenderCount() {
        String sql = "SELECT COUNT(*) FROM tenders";
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.getTotalTenderCount] Error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tenders WHERE status = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.severe("[TenderDAOImpl.countByStatus] Error: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    /**
     * Maps a ResultSet row to a Tender JavaBean.
     */
    private Tender mapRow(ResultSet rs) throws SQLException {
        Tender t = new Tender();
        
        t.setTenderId(rs.getInt("tender_id"));
        t.setReferenceNo(rs.getString("reference_no"));
        t.setTitle(rs.getString("title"));
        t.setCategory(rs.getString("category"));
        t.setDescription(rs.getString("description"));
        t.setEstimatedValue(rs.getBigDecimal("estimated_value"));
        
        Timestamp closingTs = rs.getTimestamp("closing_datetime");
        if (closingTs != null) {
            t.setClosingDateTime(closingTs.toLocalDateTime());
        }
        
        t.setNoticeFilePath(rs.getString("notice_file_path"));
        t.setStatus(rs.getString("status"));
        t.setCreatedBy(rs.getInt("created_by"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            t.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            t.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        
        return t;
    }
}