package com.procuregov.servlet;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluationDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.EvaluationScore;
import com.procuregov.model.Tender;
import com.procuregov.service.EvaluationService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EvaluationServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(EvaluationServlet.class.getName());
    private EvaluationService evalService;

    @Override
    public void init() throws ServletException {
        TenderDAO tDAO = new TenderDAOImpl();
        BidDAO bDAO = new BidDAOImpl();
        EvaluationDAO eDAO = new EvaluationDAOImpl();
        UserDAO uDAO = new UserDAOImpl();
        evalService = new EvaluationService(eDAO, bDAO, tDAO, uDAO);
        logger.info("EvaluationServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        if (!AuthUtil.requireRole(req, resp, "OFFICER", "EVALUATOR")) {
            logger.warning("Access denied - not officer or evaluator");
            return;
        }

        String action = req.getParameter("action");
        String tenderIdParam = req.getParameter("tenderId");

        if (tenderIdParam == null || tenderIdParam.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/evaluator/dashboard");
            return;
        }

        int tenderId = Integer.parseInt(tenderIdParam);

        if ("leaderboard".equals(action)) {
            List<Map<String, Object>> leaderboard = evalService.getRankedLeaderboard(tenderId);
            req.setAttribute("leaderboard", leaderboard);
            req.setAttribute("tenderId", tenderId);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/leaderboard.jsp").forward(req, resp);
            return;
        }

        Tender tender = evalService.getTenderById(tenderId);
        
        if (tender != null && "CLOSED".equals(tender.getStatus())) {
            boolean transitioned = evalService.startEvaluation(tenderId);
            if (transitioned) {
                logger.info("Tender " + tenderId + " auto-transitioned from CLOSED to UNDER_EVALUATION");
                tender = evalService.getTenderById(tenderId);
            }
        }

        int evaluatorId = AuthUtil.getSessionUserId(req);

        try {
            List<Bid> bids = evalService.getBidsWithAutoScores(tenderId);
            
            logger.info("Loaded " + bids.size() + " bids for tender " + tenderId);
            for (Bid bid : bids) {
                logger.info("Bid ID: " + bid.getBidId() + 
                           ", Price Score: " + bid.getPriceScore() + 
                           ", Timeline Score: " + bid.getTimelineScore());
            }
            
            req.setAttribute("bids", bids);
            req.setAttribute("tender", tender);

            boolean hasSubmitted = evalService.hasEvaluatorSubmittedForTender(tenderId, evaluatorId);
            req.setAttribute("hasSubmitted", hasSubmitted);

            req.getRequestDispatcher("/WEB-INF/jsp/evaluator/evaluation-panel.jsp")
               .forward(req, resp);

        } catch (Exception e) {
            logger.severe("Error in doGet: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/evaluator/dashboard?error=system");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        if (!AuthUtil.requireRole(req, resp, "OFFICER", "EVALUATOR")) {
            logger.warning("Access denied - not officer or evaluator");
            return;
        }

        String action = req.getParameter("action");

        if ("submitScores".equals(action)) {
            int tenderId = Integer.parseInt(req.getParameter("tenderId"));
            int evaluatorId = AuthUtil.getSessionUserId(req);
            HttpSession session = req.getSession();

            try {
                String[] bidParams = req.getParameterValues("bidIds");
                int scoresSubmitted = 0;

                if (bidParams != null) {
                    for (String bidIdStr : bidParams) {
                        int bidId = Integer.parseInt(bidIdStr);
                        String techScoreStr = req.getParameter("technical_" + bidId);

                        if (techScoreStr != null && !techScoreStr.isEmpty()) {
                            BigDecimal technicalScore = new BigDecimal(techScoreStr);

                            EvaluationScore score = new EvaluationScore();
                            score.setBidId(bidId);
                            score.setEvaluatorId(evaluatorId);
                            score.setTechnicalScore(technicalScore);

                            if (evalService.submitScores(score)) {
                                scoresSubmitted++;
                                logger.info("Saved score for bid " + bidId + " by evaluator " + evaluatorId);
                            }
                        }
                    }
                }

                if (scoresSubmitted > 0) {
                    session.setAttribute("evaluationMessage", "Your evaluation has been submitted successfully!");
                    
                    boolean allCompleted = evalService.checkAndUpdateTenderStatus(tenderId);

                    if (allCompleted) {
                        logger.info("Tender " + tenderId + " - All evaluators completed. Status changed to EVALUATED");
                        session.setAttribute("infoMessage", "All evaluators have completed! Tender status is now EVALUATED.");
                    } else {
                        int completedCount = evalService.getCompletedEvaluatorsCount(tenderId);
                        int totalEvaluators = evalService.getTotalEvaluatorsCount();
                        session.setAttribute("infoMessage", "You have completed your evaluation. Waiting for " + (totalEvaluators - completedCount) + " more evaluator(s) to complete.");
                    }

                    resp.sendRedirect(req.getContextPath() + "/evaluator/dashboard");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/evaluation?tenderId=" + tenderId + "&error=failed");
                }

            } catch (Exception e) {
                logger.severe("Error submitting scores: " + e.getMessage());
                e.printStackTrace();
                resp.sendRedirect(req.getContextPath() + "/evaluation?tenderId=" + tenderId + "&error=failed");
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/evaluator/dashboard");
        }
    }
}