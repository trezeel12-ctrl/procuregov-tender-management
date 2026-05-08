package com.procuregov.servlet;

import java.util.Map;
import java.util.HashMap;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.AwardDAOImpl;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.service.BidService;
import com.procuregov.service.TenderService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SupplierDashboardServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierDashboardServlet.class.getName());
    private TenderService tenderService;
    private BidService bidService;
    private AwardDAO awardDAO;
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        TenderDAO tenderDAO = new TenderDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();
        tenderService = new TenderService(tenderDAO);
        bidService = new BidService(bidDAO, tenderDAO);
        awardDAO = new AwardDAOImpl();
        this.tenderDAO = tenderDAO;
        logger.info("SupplierDashboardServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Auto-close expired tenders
        int closed = tenderService.autoCloseExpiredTenders();
        if (closed > 0) {
            logger.info("Auto-closed " + closed + " expired tenders");
        }

        if (!AuthUtil.requireSupplier(req, resp)) {
            return;
        }

        try {
            int supplierId = AuthUtil.getSessionUserId(req);
            logger.info("Supplier ID: " + supplierId);

            // Get MY BIDS
            List<Bid> myBids = bidService.getBidsBySupplier(supplierId);
            if (myBids == null) myBids = new ArrayList<>();

            // Initialize statistics variables
            int totalBids = myBids.size();
            int awardedBids = 0;
            int pendingReviewBids = 0;
            BigDecimal totalBidAmount = BigDecimal.ZERO;
            
            // Create a new list with winner flag
            List<Map<String, Object>> bidsWithWinnerInfo = new ArrayList<>();

            // Calculate statistics and determine winners
            for (Bid bid : myBids) {
                // Add to total bid amount
                if (bid.getBidAmount() != null) {
                    totalBidAmount = totalBidAmount.add(bid.getBidAmount());
                }
                
                // Get tender details
                Tender tender = tenderDAO.findById(bid.getTenderId());
                String tenderStatus = tender != null ? tender.getStatus() : bid.getTenderStatus();
                
                // Check if this supplier won this tender
                boolean isWinner = false;
                if ("AWARDED".equals(tenderStatus)) {
                    Award award = awardDAO.findByTenderId(bid.getTenderId());
                    if (award != null && award.getWinningBidId() == bid.getBidId()) {
                        isWinner = true;
                        awardedBids++;
                    }
                }
                
                // Count pending review
                if ("UNDER_EVALUATION".equals(tenderStatus) || "EVALUATED".equals(tenderStatus)) {
                    pendingReviewBids++;
                }
                
                // Create map with additional info
                Map<String, Object> bidInfo = new java.util.HashMap<>();
                bidInfo.put("bid", bid);
                bidInfo.put("isWinner", isWinner);
                bidInfo.put("tenderStatus", tenderStatus);
                bidsWithWinnerInfo.add(bidInfo);
            }

            // Calculate success rate
            int successRate = 0;
            if (totalBids > 0) {
                successRate = (awardedBids * 100) / totalBids;
            }

            // Get OPEN tenders (available for bidding)
            List<Tender> openTenders = tenderService.getTenders("OPEN", null);
            if (openTenders == null) openTenders = new ArrayList<>();

            // Set attributes for JSP
            req.setAttribute("tenders", openTenders);
            req.setAttribute("bidsWithWinnerInfo", bidsWithWinnerInfo);
            req.setAttribute("totalBids", totalBids);
            req.setAttribute("awardedBids", awardedBids);
            req.setAttribute("pendingReviewBids", pendingReviewBids);
            req.setAttribute("totalBidAmount", totalBidAmount);
            req.setAttribute("openTendersCount", openTenders.size());
            req.setAttribute("successRate", successRate);

            logger.info("Supplier Stats - Total Bids: " + totalBids + 
                       ", Awarded: " + awardedBids + 
                       ", Pending: " + pendingReviewBids + 
                       ", Total Value: " + totalBidAmount +
                       ", Success Rate: " + successRate + "%");

            req.getRequestDispatcher("/WEB-INF/jsp/supplier/dashboard.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("errorMessage", "Unable to load dashboard. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/supplier/dashboard.jsp")
                    .forward(req, resp);
        }
    }
}