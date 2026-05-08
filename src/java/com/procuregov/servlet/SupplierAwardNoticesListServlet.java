package com.procuregov.servlet;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.AwardDAOImpl;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SupplierAwardNoticesListServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierAwardNoticesListServlet.class.getName());
    private AwardDAO awardDAO;
    private BidDAO bidDAO;
    private TenderDAO tenderDAO;

    @Override
    public void init() throws ServletException {
        awardDAO = new AwardDAOImpl();
        bidDAO = new BidDAOImpl();
        tenderDAO = new TenderDAOImpl();
        logger.info("SupplierAwardNoticesListServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check if supplier is logged in
        if (!AuthUtil.requireSupplier(req, resp)) {
            logger.warning("Supplier not authenticated");
            return;
        }

        int supplierId = AuthUtil.getSessionUserId(req);
        logger.info("Supplier ID: " + supplierId + " viewing award notices");

        try {
            // Get ALL bids submitted by this supplier (not just awarded ones)
            List<Bid> myBids = bidDAO.findBySupplierId(supplierId);
            
            // Collect ALL tenders that the supplier bid on (whether won or not)
            List<Map<String, Object>> awardNotices = new ArrayList<>();
            int wonCount = 0;
            int notWonCount = 0;
            
            for (Bid bid : myBids) {
                Tender tender = tenderDAO.findById(bid.getTenderId());
                if (tender == null) continue;
                
                // Only show tenders that are AWARDED (finalized)
                if (!"AWARDED".equals(tender.getStatus())) {
                    continue;
                }
                
                // Check if this supplier won this tender
                Award award = awardDAO.findByTenderId(bid.getTenderId());
                boolean isWinner = false;
                
                if (award != null && award.getWinningBidId() == bid.getBidId()) {
                    isWinner = true;
                    wonCount++;
                } else {
                    notWonCount++;
                }
                
                Map<String, Object> notice = new HashMap<>();
                notice.put("tender", tender);
                notice.put("bid", bid);
                notice.put("award", award);
                notice.put("isWinner", isWinner);
                
                awardNotices.add(notice);
                
                logger.info("Tender " + tender.getReferenceNo() + " - Winner: " + isWinner);
            }
            
            logger.info("Total awarded tenders: " + awardNotices.size());
            logger.info("Won: " + wonCount + ", Not Won: " + notWonCount);
            
            req.setAttribute("awardNotices", awardNotices);
            req.setAttribute("wonCount", wonCount);
            req.setAttribute("notWonCount", notWonCount);
            
            req.getRequestDispatcher("/WEB-INF/jsp/supplier/award-notices-list.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=system");
        }
    }
}