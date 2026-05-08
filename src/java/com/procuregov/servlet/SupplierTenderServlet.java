package com.procuregov.servlet;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.ArrayList;

public class SupplierTenderServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierTenderServlet.class.getName());
    private TenderService tenderService;
    private BidService bidService;

    @Override
    public void init() throws ServletException {
        TenderDAO tenderDAO = new TenderDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();
        tenderService = new TenderService(tenderDAO);
        bidService = new BidService(bidDAO, tenderDAO);
        logger.info("SupplierTenderServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Step 1: Auto-close expired tenders FIRST
        try {
            int closedCount = tenderService.autoCloseExpiredTenders();
            if (closedCount > 0) {
                logger.info("[SupplierTenderServlet] Auto-closed " + closedCount + " expired tenders");
            }
        } catch (Exception e) {
            logger.warning("[SupplierTenderServlet] Auto-close error: " + e.getMessage());
        }

        System.out.println("SupplierTenderServlet: doGet called with id=" + req.getParameter("id"));

        // Step 2: Role guard - Only suppliers
        if (!AuthUtil.requireSupplier(req, resp)) {
            logger.warning("[SupplierTenderServlet] Authentication failed");
            return;
        }

        try {
            // Step 3: Get and validate tender ID
            String idParam = req.getParameter("id");
            logger.info("[SupplierTenderServlet] Tender ID param: " + idParam);

            if (idParam == null || idParam.trim().isEmpty()) {
                logger.warning("[SupplierTenderServlet] Missing tender ID");
                resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
                return;
            }

            int tenderId = Integer.parseInt(idParam);
            logger.info("[SupplierTenderServlet] Fetching tender: " + tenderId);

            // Step 4: Fetch tender from database
            Tender tender = tenderService.getTenderById(tenderId);

            if (tender == null) {
                logger.warning("[SupplierTenderServlet] Tender not found: " + tenderId);
                resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=not_found");
                return;
            }

            logger.info("[SupplierTenderServlet] Tender found: " + tender.getTitle() + ", Status: " + tender.getStatus());

            // Step 5: Get current supplier info
            int supplierId = AuthUtil.getSessionUserId(req);

            // Step 6: Check if supplier already submitted a bid (Module 3 requirement)
            boolean hasBid = bidService.hasSupplierBid(tenderId, supplierId);

            // Step 7: Get all bids for this tender
            List<Bid> bids = bidService.getBidsByTender(tenderId);
            if (bids == null) bids = new ArrayList<>();

            // Step 8: Server-side closing date check (Module 3 requirement)
            boolean isClosed = LocalDateTime.now().isAfter(tender.getClosingDateTime());

            // Step 9: Determine if supplier can submit bid
            boolean canSubmitBid = "OPEN".equals(tender.getStatus()) && !isClosed && !hasBid;

            // Step 10: Store attributes for JSP
            req.setAttribute("tender", tender);
            req.setAttribute("hasBid", hasBid);
            req.setAttribute("isClosed", isClosed);
            req.setAttribute("canSubmitBid", canSubmitBid);
            req.setAttribute("bids", bids);
            req.setAttribute("hasBids", !bids.isEmpty());

            logger.info("[SupplierTenderServlet] hasBid=" + hasBid + ", isClosed=" + isClosed + 
                        ", canSubmitBid=" + canSubmitBid + ", bidCount=" + bids.size());

            // Step 11: Forward to tender detail JSP
            req.getRequestDispatcher("/WEB-INF/jsp/supplier/tender.jsp")
                    .forward(req, resp);

        } catch (NumberFormatException e) {
            logger.warning("[SupplierTenderServlet] Invalid tender ID format: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=invalid_id");
        } catch (Exception e) {
            logger.severe("[SupplierTenderServlet] ERROR: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=system_error");
        }
    }
}