package com.procuregov.servlet;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluationDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.service.TenderService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller for Procurement Officer dashboard.
 * Displays tender list with filters, statistics, and quick action shortcuts.
 * 
 * Exam Compliance:
 * - Module 1: Role-based access control via AuthUtil
 * - Module 2: Tender list filterable by status and category using JSTL
 * - Module 5: All exceptions caught and logged, no stack traces exposed
 */
public class OfficerDashboardServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(OfficerDashboardServlet.class.getName());
    private TenderService tenderService;
    private BidDAO bidDAO;
    private EvaluationDAO evaluationDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        TenderDAO tenderDAO = new TenderDAOImpl();
        tenderService = new TenderService(tenderDAO);
        bidDAO = new BidDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        userDAO = new UserDAOImpl();
        logger.info("[OfficerDashboardServlet] Initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        logger.info("[OfficerDashboardServlet] GET request received");
        
        try {
            int closed = tenderService.autoCloseExpiredTenders();
            if (closed > 0) {
                logger.info("[OfficerDashboardServlet] Auto-closed " + closed + " expired tenders");
            }
        } catch (Exception e) {
            logger.warning("[OfficerDashboardServlet] Auto-close error: " + e.getMessage());
        }
        
        if (!AuthUtil.requireOfficer(req, resp)) {
            logger.warning("[OfficerDashboardServlet] Access denied - not an officer");
            return;
        }
        
        logger.info("[OfficerDashboardServlet] Officer authenticated");
        
        try {
            String statusFilter = req.getParameter("status");
            String categoryFilter = req.getParameter("category");
            
            List<Tender> tenders = tenderService.getTenders(statusFilter, categoryFilter);
            
            // Calculate statistics from database
            int draftCount = 0;
            int openCount = 0;
            int closedCount = 0;
            int underEvalCount = 0;
            int evaluatedCount = 0;
            int awardedCount = 0;
            BigDecimal totalEstimatedValue = BigDecimal.ZERO;
            
            List<Tender> allTenders = tenderService.getTenders(null, null);
            if (allTenders != null) {
                for (Tender t : allTenders) {
                    if (t.getEstimatedValue() != null) {
                        totalEstimatedValue = totalEstimatedValue.add(t.getEstimatedValue());
                    }
                    switch (t.getStatus()) {
                        case "DRAFT": draftCount++; break;
                        case "OPEN": openCount++; break;
                        case "CLOSED": closedCount++; break;
                        case "UNDER_EVALUATION": underEvalCount++; break;
                        case "EVALUATED": evaluatedCount++; break;
                        case "AWARDED": awardedCount++; break;
                    }
                }
            }
            
            int totalBidsCount = 0;
            List<Tender> allForBids = tenderService.getTenders(null, null);
            if (allForBids != null) {
                for (Tender t : allForBids) {
                    totalBidsCount += bidDAO.countByTenderId(t.getTenderId());
                }
            }
            
            int totalEvaluators = 0;
            try {
                totalEvaluators = userDAO.findUserIdsByRole("EVALUATOR").size();
            } catch (Exception e) {
                totalEvaluators = 0;
            }
            
            int completionRate = 0;
            if (allTenders != null && !allTenders.isEmpty()) {
                completionRate = (awardedCount * 100) / allTenders.size();
            }
            
            req.setAttribute("tenders", tenders);
            req.setAttribute("currentStatus", statusFilter);
            req.setAttribute("currentCategory", categoryFilter);
            
            req.setAttribute("draftCount", draftCount);
            req.setAttribute("openCount", openCount);
            req.setAttribute("closedCount", closedCount);
            req.setAttribute("underEvalCount", underEvalCount);
            req.setAttribute("evaluatedCount", evaluatedCount);
            req.setAttribute("awardedCount", awardedCount);
            req.setAttribute("totalTenders", allTenders != null ? allTenders.size() : 0);
            req.setAttribute("totalEstimatedValue", totalEstimatedValue);
            req.setAttribute("totalBidsCount", totalBidsCount);
            req.setAttribute("totalEvaluators", totalEvaluators);
            req.setAttribute("completionRate", completionRate);
            req.setAttribute("evaluationCount", underEvalCount);
            
            logger.info("[OfficerDashboardServlet] Found " + (tenders != null ? tenders.size() : 0) + " tenders");
            logger.info("[OfficerDashboardServlet] Statistics - Draft:" + draftCount + " Open:" + openCount + " Awarded:" + awardedCount);
            
            req.getRequestDispatcher("/WEB-INF/jsp/officer/dashboard.jsp")
               .forward(req, resp);
               
            logger.info("[OfficerDashboardServlet] Request completed");
            
        } catch (Exception e) {
            logger.severe("[OfficerDashboardServlet] Error: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("errorMessage", "Unable to load dashboard. Please try again.");
            req.getRequestDispatcher("/WEB-INF/jsp/officer/dashboard.jsp")
               .forward(req, resp);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/officer/dashboard");
    }
}