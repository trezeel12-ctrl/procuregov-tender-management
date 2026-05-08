package com.procuregov.dao;

import com.procuregov.model.Tender;
import java.util.List;

/**
 * Data Access Object interface for Tender entity.
 * Defines all database operations for tender management.
 */
public interface TenderDAO {

    Tender findById(int tenderId);
    
    List<Tender> findAll();
    
    List<Tender> findByStatus(String status);
    
    List<Tender> findByCategory(String category);
    
    List<Tender> findByStatusAndCategory(String status, String category);
    
    int insert(Tender tender);
    
    boolean update(Tender tender);
    
    boolean updateStatus(int tenderId, String newStatus);
    
    int closeExpiredTenders();
    
    int updateStatusByCondition(String sql);
    
    List<Tender> findBySearchAndFilters(String search, String status, String category, String sortBy, String sortDir);
    
    int getTotalTenderCount();
    
    int countByStatus(String status);
}