package com.procuregov.service;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles bid submission validation, ownership checks, and retrieval.
 * Closing date enforcement is handled in Servlet per exam requirement.
 */
public class BidService {
    private final BidDAO bidDAO;
    private final TenderDAO tenderDAO;
    private static final Logger logger = Logger.getLogger(BidService.class.getName());

    public BidService(BidDAO bidDAO, TenderDAO tenderDAO) {
        this.bidDAO = bidDAO;
        this.tenderDAO = tenderDAO;
    }

    /**
     * Submits a bid after basic validation. Returns generated bid_id or 0 on failure.
     */
    public int submitBid(Bid bid) {
        if (bid == null || bid.getBidAmount() == null || bid.getTechnicalStatement() == null) {
            return 0;
        }
        // Server-side closing date check should happen in Servlet, but we double-check here for safety
        Tender tender = tenderDAO.findById(bid.getTenderId());
        if (tender != null && LocalDateTime.now().isAfter(tender.getClosingDateTime())) {
            System.err.println("[BidService] Rejected: Tender closed");
            return 0;
        }
        
        if (hasSupplierBid(bid.getTenderId(), bid.getSupplierId())) {
            System.err.println("[BidService] Rejected: Duplicate submission");
            return 0;
        }
        
        return bidDAO.insert(bid);
    }

    /**
     * Checks if a supplier has already submitted a bid for a specific tender.
     */
    public boolean hasSupplierBid(int tenderId, int supplierId) {
        return bidDAO.hasSupplierBid(tenderId, supplierId);
    }

    /**
     * Gets all bids for a specific tender.
     */
    public List<Bid> getBidsByTender(int tenderId) {
        return bidDAO.findByTenderId(tenderId);
    }

    /**
     * Gets all bids submitted by a specific supplier.
     */
    public List<Bid> getBidsBySupplier(int supplierId) {
        try {
            return bidDAO.findBySupplierId(supplierId);
        } catch (Exception e) {
            logger.severe("getBidsBySupplier error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}