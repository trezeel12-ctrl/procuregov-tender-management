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
import com.procuregov.service.EvaluationService;
import com.procuregov.service.TenderService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EvaluatorDashboardServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(EvaluatorDashboardServlet.class.getName());
    private TenderService tenderService;
    private EvaluationService evalService;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        TenderDAO tenderDAO = new TenderDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();
        EvaluationDAO evaluationDAO = new EvaluationDAOImpl();
        userDAO = new UserDAOImpl();
        
        tenderService = new TenderService(tenderDAO);
        evalService = new EvaluationService(evaluationDAO, bidDAO, tenderDAO, userDAO);
        logger.info("EvaluatorDashboardServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        logger.info("EvaluatorDashboardServlet GET request");
        
        // Auto-close expired tenders
        int closed = tenderService.autoCloseExpiredTenders();
        if (closed > 0) {
            logger.info("Auto-closed " + closed + " expired tenders");
        }
        
        if (!AuthUtil.requireEvaluator(req, resp)) {
            return;
        }
        
        HttpSession session = req.getSession();
        int evaluatorId = AuthUtil.getSessionUserId(req);
        
        // Get messages from session
        String evaluationMessage = (String) session.getAttribute("evaluationMessage");
        if (evaluationMessage != null) {
            req.setAttribute("successMessage", evaluationMessage);
            session.removeAttribute("evaluationMessage");
        }
        
        String infoMessage = (String) session.getAttribute("infoMessage");
        if (infoMessage != null) {
            req.setAttribute("infoMessage", infoMessage);
            session.removeAttribute("infoMessage");
        }
        
        try {
            // Get tenders by status
            List<Tender> closedTenders = tenderService.getTenders("CLOSED", null);
            List<Tender> underEvalTenders = tenderService.getTenders("UNDER_EVALUATION", null);
            List<Tender> evaluatedTenders = tenderService.getTenders("EVALUATED", null);
            
            if (closedTenders == null) closedTenders = new ArrayList<>();
            if (underEvalTenders == null) underEvalTenders = new ArrayList<>();
            if (evaluatedTenders == null) evaluatedTenders = new ArrayList<>();
            
            // Calculate statistics
            int totalClosed = closedTenders.size();
            int totalUnderEval = underEvalTenders.size();
            int totalEvaluated = evaluatedTenders.size();
            int totalTenders = totalClosed + totalUnderEval + totalEvaluated;
            
            // Calculate total procurement value for evaluation
            BigDecimal totalValue = BigDecimal.ZERO;
            for (Tender t : closedTenders) {
                if (t.getEstimatedValue() != null) {
                    totalValue = totalValue.add(t.getEstimatedValue());
                }
            }
            for (Tender t : underEvalTenders) {
                if (t.getEstimatedValue() != null) {
                    totalValue = totalValue.add(t.getEstimatedValue());
                }
            }
            
            // Get all evaluators
            List<Integer> allEvaluators = userDAO.findUserIdsByRole("EVALUATOR");
            int totalEvaluators = allEvaluators != null ? allEvaluators.size() : 0;
            
            // Calculate completion status for each UNDER_EVALUATION tender
            Map<Integer, Boolean> evaluatorStatus = new HashMap<>();
            Map<Integer, Integer> completedCountMap = new HashMap<>();
            Map<Integer, Integer> myScoresMap = new HashMap<>();
            
            for (Tender tender : underEvalTenders) {
                // Check if current evaluator has submitted
                boolean currentEvaluatorSubmitted = evalService.hasEvaluatorSubmittedForTender(tender.getTenderId(), evaluatorId);
                evaluatorStatus.put(tender.getTenderId(), currentEvaluatorSubmitted);
                
                // Count how many evaluators have completed this tender
                int completedCount = 0;
                for (int evalId : allEvaluators) {
                    if (evalService.hasEvaluatorSubmittedForTender(tender.getTenderId(), evalId)) {
                        completedCount++;
                    }
                }
                completedCountMap.put(tender.getTenderId(), completedCount);
                myScoresMap.put(tender.getTenderId(), currentEvaluatorSubmitted ? 1 : 0);
                
                logger.info("Tender " + tender.getTenderId() + " - Completed: " + completedCount + "/" + totalEvaluators);
            }
            
            // Calculate completion percentage
            int myCompletedCount = 0;
            for (boolean status : evaluatorStatus.values()) {
                if (status) myCompletedCount++;
            }
            int completionPercentage = underEvalTenders.isEmpty() ? 0 : (myCompletedCount * 100) / underEvalTenders.size();
            
            // Set attributes
            req.setAttribute("closedTenders", closedTenders);
            req.setAttribute("underEvalTenders", underEvalTenders);
            req.setAttribute("evaluatedTenders", evaluatedTenders);
            req.setAttribute("evaluatorStatus", evaluatorStatus);
            req.setAttribute("completedCountMap", completedCountMap);
            req.setAttribute("myScoresMap", myScoresMap);
            req.setAttribute("totalEvaluators", totalEvaluators);
            req.setAttribute("evaluatorId", evaluatorId);
            
            // Statistics for dashboard
            req.setAttribute("totalClosed", totalClosed);
            req.setAttribute("totalUnderEval", totalUnderEval);
            req.setAttribute("totalEvaluated", totalEvaluated);
            req.setAttribute("totalTenders", totalTenders);
            req.setAttribute("totalValue", totalValue);
            req.setAttribute("completionPercentage", completionPercentage);
            req.setAttribute("myCompletedCount", myCompletedCount);
            
            req.getRequestDispatcher("/WEB-INF/jsp/evaluator/dashboard.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("EvaluatorDashboardServlet Error: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/auth?error=system");
        }
    }
}