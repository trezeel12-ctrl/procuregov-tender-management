package com.procuregov.dao;

import com.procuregov.model.Bid;
import java.math.BigDecimal;
import java.util.List;

public interface BidDAO {

    Bid findById(int bidId);
    
    List<Bid> findByTenderId(int tenderId);
    
    List<Bid> findBySupplierId(int supplierId);
    
    List<Bid> findByTenderIdWithScores(int tenderId);
    
    boolean hasSupplierBid(int tenderId, int supplierId);
    
    int insert(Bid bid);
    
    boolean update(Bid bid);
    
    boolean delete(int bidId);
    
    BigDecimal getLowestBidAmount(int tenderId);
    
    int getShortestTimeline(int tenderId);
    
    int countByTenderId(int tenderId);
    
    BigDecimal getHighestBidAmount(int tenderId);
    
}