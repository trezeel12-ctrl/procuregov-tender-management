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
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet for suppliers to view award notices for tenders they have won.
 * 
 * Exam Compliance:
 * - Module 3: Suppliers must never be able to view bids submitted by other suppliers
 * - Module 6: Award notice visible to suppliers who bid on the tender
 * - Security: Suppliers can only view award notices for tenders they won
 */
public class SupplierAwardNoticeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierAwardNoticeServlet.class.getName());
    private AwardDAO awardDAO;
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;

    @Override
    public void init() throws ServletException {
        awardDAO = new AwardDAOImpl();
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        logger.info("SupplierAwardNoticeServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Check if supplier is logged in
        if (!AuthUtil.requireSupplier(req, resp)) {
            logger.warning("Supplier not authenticated");
            return;
        }

        String tenderIdParam = req.getParameter("tenderId");
        if (tenderIdParam == null || tenderIdParam.isEmpty()) {
            logger.warning("Missing tenderId parameter");
            resp.sendRedirect(req.getContextPath() + "/supplier/award-notices?error=missing_tender");
            return;
        }

        int tenderId = Integer.parseInt(tenderIdParam);
        int supplierId = AuthUtil.getSessionUserId(req);
        logger.info("Supplier ID: " + supplierId + " viewing award for tender: " + tenderId);

        try {
            // STEP 1: Check if this supplier has a bid on this tender
            List<Bid> allBids = bidDAO.findByTenderId(tenderId);
            Bid supplierBid = null;
            for (Bid bid : allBids) {
                if (bid.getSupplierId() == supplierId) {
                    supplierBid = bid;
                    break;
                }
            }
            
            if (supplierBid == null) {
                logger.warning("Supplier " + supplierId + " did not bid on tender " + tenderId + " - ACCESS DENIED");
                resp.sendRedirect(req.getContextPath() + "/supplier/error?error=unauthorized");
                return;
            }
            
            // STEP 2: Get tender details
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("Tender not found: " + tenderId);
                resp.sendRedirect(req.getContextPath() + "/supplier/error?error=no_tender");
                return;
            }
            
            // STEP 3: Check if tender is AWARDED
            if (!"AWARDED".equals(tender.getStatus())) {
                logger.warning("Tender " + tenderId + " is not awarded. Status: " + tender.getStatus());
                resp.sendRedirect(req.getContextPath() + "/supplier/error?error=not_awarded");
                return;
            }
            
            // STEP 4: Get award details
            Award award = awardDAO.findByTenderId(tenderId);
            if (award == null) {
                logger.warning("No award record for tender " + tenderId);
                resp.sendRedirect(req.getContextPath() + "/supplier/error?error=no_award");
                return;
            }
            
            // STEP 5: Verify this supplier is the winner - CRITICAL SECURITY CHECK
            if (award.getWinningBidId() != supplierBid.getBidId()) {
                logger.warning("Supplier " + supplierId + " is NOT the winner of tender " + tenderId + " - ACCESS DENIED");
                resp.sendRedirect(req.getContextPath() + "/supplier/error?error=not_winner");
                return;
            }
            
            // STEP 6: Supplier is the winner - show award notice
            req.setAttribute("award", award);
            req.setAttribute("tender", tender);
            req.setAttribute("winningBid", supplierBid);
            req.setAttribute("supplierId", supplierId);
            req.setAttribute("tenderNoticePath", tender.getNoticeFilePath());
            req.setAttribute("bidDocumentPath", supplierBid.getSupportingDocPath());

            req.getRequestDispatcher("/WEB-INF/jsp/supplier/award-notice.jsp")
                    .forward(req, resp);

        } catch (NumberFormatException e) {
            logger.warning("Invalid tenderId format: " + tenderIdParam);
            resp.sendRedirect(req.getContextPath() + "/supplier/error?error=invalid_id");
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/supplier/error?error=system");
        }
    }
}