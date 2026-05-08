package com.procuregov.servlet;

import com.procuregov.dao.AwardDAO;
import com.procuregov.dao.AwardDAOImpl;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluationDAOImpl;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.Award;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.model.User;
import com.procuregov.service.EmailService;
import com.procuregov.service.AsyncEmailService; 
import com.procuregov.service.EvaluationService;
import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AwardServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(AwardServlet.class.getName());
    private AwardDAO awardDAO;
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;
    private UserDAO userDAO;
    private EvaluationService evalService;
    private EmailService emailService;

    @Override
    public void init() throws ServletException {
        awardDAO = new AwardDAOImpl();
        tenderDAO = new TenderDAOImpl();
        bidDAO = new BidDAOImpl();
        userDAO = new UserDAOImpl();
        EvaluationDAO evaluationDAO = new EvaluationDAOImpl();
        evalService = new EvaluationService(evaluationDAO, bidDAO, tenderDAO, userDAO);
        
        // Email Service Configuration - GMAIL SETUP
        emailService = new EmailService(
            "smtp.gmail.com",           // SMTP Host
            "587",                      // SMTP Port (TLS)
            "example@gmail.com",      // Your Gmail username
            "xxxxxxxxxxxxxxxx"          // Your Gmail App Password (no spaces)
        );
        logger.info("AwardServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        if (!AuthUtil.requireOfficer(req, resp)) return;

        String action = req.getParameter("action");
        String tenderIdParam = req.getParameter("tenderId");

        // Handle awarded tender detail view
        if ("viewAwarded".equals(action) && tenderIdParam != null && !tenderIdParam.isEmpty()) {
            int tenderId = Integer.parseInt(tenderIdParam);
            handleAwardedTenderDetail(req, resp, tenderId);
            return;
        }

        // Handle award form for evaluated tender
        if (tenderIdParam != null && !tenderIdParam.isEmpty()) {
            int tenderId = Integer.parseInt(tenderIdParam);
            handleAwardForm(req, resp, tenderId);
            return;
        }

        // Default: show award list page
        handleAwardList(req, resp);
    }
    
    private void handleAwardedTenderDetail(HttpServletRequest req, HttpServletResponse resp, int tenderId)
            throws ServletException, IOException {
        try {
            // Get award details
            Award award = awardDAO.findByTenderId(tenderId);
            if (award == null) {
                resp.sendRedirect(req.getContextPath() + "/officer/award?error=no_award");
                return;
            }
            
            // Get tender details
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                resp.sendRedirect(req.getContextPath() + "/officer/award?error=no_tender");
                return;
            }
            
            // Get winning bid details
            Bid winningBid = bidDAO.findById(award.getWinningBidId());
            if (winningBid == null) {
                resp.sendRedirect(req.getContextPath() + "/officer/award?error=no_bid");
                return;
            }
            
            // Get winning supplier details
            User supplier = userDAO.findById(winningBid.getSupplierId());
            
            req.setAttribute("award", award);
            req.setAttribute("tender", tender);
            req.setAttribute("winningBid", winningBid);
            req.setAttribute("supplier", supplier);
            
            req.getRequestDispatcher("/WEB-INF/jsp/officer/awarded-tender-detail.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("Error in handleAwardedTenderDetail: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/officer/award?error=system");
        }
    }
    
    private void handleAwardForm(HttpServletRequest req, HttpServletResponse resp, int tenderId)
            throws ServletException, IOException {
        try {
            List<Map<String, Object>> leaderboard = evalService.getRankedLeaderboard(tenderId);
            Tender tender = tenderDAO.findById(tenderId);
            
            req.setAttribute("leaderboard", leaderboard);
            req.setAttribute("tender", tender);
            req.setAttribute("tenderId", tenderId);
            
            req.getRequestDispatcher("/WEB-INF/jsp/officer/award.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("Error in handleAwardForm: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/officer/dashboard?error=system");
        }
    }
    
    private void handleAwardList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Tender> evaluatedTenders = tenderDAO.findByStatus("EVALUATED");
            if (evaluatedTenders == null) evaluatedTenders = new ArrayList<>();
            
            List<Tender> awardedTenders = tenderDAO.findByStatus("AWARDED");
            if (awardedTenders == null) awardedTenders = new ArrayList<>();
            
            // Get award details for each awarded tender
            List<Map<String, Object>> awardedDetails = new ArrayList<>();
            for (Tender tender : awardedTenders) {
                Award award = awardDAO.findByTenderId(tender.getTenderId());
                if (award != null) {
                    Bid winningBid = bidDAO.findById(award.getWinningBidId());
                    User supplier = null;
                    if (winningBid != null) {
                        supplier = userDAO.findById(winningBid.getSupplierId());
                    }
                    
                    java.util.Map<String, Object> detail = new java.util.HashMap<>();
                    detail.put("tender", tender);
                    detail.put("award", award);
                    detail.put("supplier", supplier);
                    awardedDetails.add(detail);
                }
            }
            
            req.setAttribute("evaluatedTenders", evaluatedTenders);
            req.setAttribute("awardedTenders", awardedTenders);
            req.setAttribute("awardedDetails", awardedDetails);
            
            req.getRequestDispatcher("/WEB-INF/jsp/officer/award-list.jsp")
               .forward(req, resp);
               
        } catch (Exception e) {
            logger.severe("Error in handleAwardList: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/officer/dashboard?error=system");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        if (!AuthUtil.requireOfficer(req, resp)) return;

        try {
            int tenderId = Integer.parseInt(req.getParameter("tenderId"));
            int winningBidId = Integer.parseInt(req.getParameter("winningBidId"));
            BigDecimal awardedValue = new BigDecimal(req.getParameter("awardedValue"));
            String justification = req.getParameter("justification");

            Award award = new Award();
            award.setTenderId(tenderId);
            award.setWinningBidId(winningBidId);
            award.setAwardedValue(awardedValue);
            award.setJustification(justification);
            award.setAwardDate(LocalDate.now());
            award.setAwardedBy(AuthUtil.getSessionUserId(req));

            // Insert award record
            int awardId = awardDAO.insert(award);

            if (awardId > 0) {
                // Update tender status to AWARDED
                tenderDAO.updateStatus(tenderId, "AWARDED");

                // START ASYNC EMAIL NOTIFICATIONS - DOES NOT BLOCK
                // This runs in background, user can immediately continue
                sendAwardNotificationsAsync(tenderId);

                // IMMEDIATE RESPONSE - User doesn't wait for emails
                String successMessage = "Contract awarded successfully! " +
                    "Award notice is now visible to all bidding suppliers. " +
                    "Email notifications are being sent in the background.";

                req.getSession().setAttribute("successMsg", successMessage);
                logger.info("Award finalized for tender " + tenderId + ". Emails queued for background sending.");

                resp.sendRedirect(req.getContextPath() + "/officer/dashboard?success=award_finalized");
            } else {
                resp.sendRedirect(req.getContextPath() + "/officer/award?tenderId=" + tenderId + "&error=award_failed");
            }
        } catch (Exception e) {
            logger.severe("Error in doPost: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/officer/dashboard?error=system");
        }
    }

   private void sendAwardNotificationsAsync(int tenderId) {
       try {
           // Log the start of async notification process
           logger.info("🚀 Starting ASYNC award notifications for tender ID: " + tenderId);
           long startTime = System.currentTimeMillis();

           // Get tender details
           Tender tender = tenderDAO.findById(tenderId);
           if (tender == null) {
               logger.warning("Cannot send notifications: Tender not found - " + tenderId);
               return;
           }

           // Get award details
           Award award = awardDAO.findByTenderId(tenderId);
           if (award == null) {
               logger.warning("Cannot send notifications: Award not found for tender - " + tenderId);
               return;
           }

           // Get all bids for this tender
           List<Bid> bids = bidDAO.findByTenderId(tenderId);
           if (bids == null || bids.isEmpty()) {
               logger.info("No bids found for tender " + tenderId + " - skipping notifications");
               return;
           }

           // Get base URL
           String baseUrl = getBaseUrl();

           int totalBidders = bids.size();
           logger.info("📧 Preparing " + totalBidders + " emails for tender: " + tender.getReferenceNo());

           // Prepare batch email items
           java.util.List<AsyncEmailService.EmailBatchItem> emailBatch = new java.util.ArrayList<>();

           for (Bid bid : bids) {
               // Get supplier details
               User supplier = userDAO.findById(bid.getSupplierId());
               if (supplier == null || supplier.getEmail() == null || supplier.getEmail().isEmpty()) {
                   logger.warning("Cannot send email: Supplier not found for bid " + bid.getBidId());
                   continue;
               }

               // Determine if this supplier won the tender
               boolean isWinner = (bid.getBidId() == award.getWinningBidId());
               String outcome = isWinner ? "Won" : "Not Won";

               // Format amounts
               String bidAmount = String.format("%.2f", bid.getBidAmount());
               String awardedValue = isWinner ? String.format("%.2f", award.getAwardedValue()) : "";

               // Build award notice URL
               String awardNoticeUrl = baseUrl + "/supplier/award-notice?tenderId=" + tenderId;

               // Add to batch for async sending
               emailBatch.add(new AsyncEmailService.EmailBatchItem(
                   supplier.getEmail(),
                   supplier.getFullName(),
                   tender.getReferenceNo(),
                   tender.getTitle(),
                   outcome,
                   bidAmount,
                   awardedValue,
                   awardNoticeUrl,
                   baseUrl
               ));

               logger.info("📧 Queued email for: " + supplier.getEmail() + " (Outcome: " + outcome + ")");
           }

           // Send ALL emails asynchronously in background
           // This call returns IMMEDIATELY - emails are queued, not sent yet
           AsyncEmailService.sendBatchAwardNotificationsAsync(emailService, emailBatch);

           long queueTime = System.currentTimeMillis() - startTime;
           logger.info("✅ " + emailBatch.size() + " emails queued in " + queueTime + "ms");
           logger.info("📧 Emails are now being sent in BACKGROUND threads.");
           logger.info("👤 User can continue using the system immediately.");

       } catch (Exception e) {
           logger.severe("Error queueing award notifications for tender " + tenderId + ": " + e.getMessage());
           e.printStackTrace();
           // Don't throw - email failure shouldn't break the award process
       }
   }
    
    private void sendAwardNotifications(int tenderId) {
        try {
            // Get tender details
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("Cannot send notifications: Tender not found - " + tenderId);
                return;
            }
            
            // Get award details
            Award award = awardDAO.findByTenderId(tenderId);
            if (award == null) {
                logger.warning("Cannot send notifications: Award not found for tender - " + tenderId);
                return;
            }
            
            // Get all bids for this tender
            List<Bid> bids = bidDAO.findByTenderId(tenderId);
            if (bids == null || bids.isEmpty()) {
                logger.info("No bids found for tender " + tenderId + " - skipping notifications");
                return;
            }
            
            // Get base URL using ngrok for external access
            String baseUrl = getBaseUrl();
            
            int emailSentCount = 0;
            int totalBidders = bids.size();
            
            logger.info("Sending award notifications for tender " + tender.getReferenceNo() + 
                       " to " + totalBidders + " bidders");
            logger.info("Using base URL: " + baseUrl);
            
            for (Bid bid : bids) {
                // Get supplier details
                User supplier = userDAO.findById(bid.getSupplierId());
                if (supplier == null || supplier.getEmail() == null || supplier.getEmail().isEmpty()) {
                    logger.warning("Cannot send email: Supplier not found for bid " + bid.getBidId());
                    continue;
                }
                
                // Determine if this supplier won the tender
                boolean isWinner = (bid.getBidId() == award.getWinningBidId());
                String outcome = isWinner ? "Won" : "Not Won";
                
                // Format amounts for email
                String bidAmount = String.format("%.2f", bid.getBidAmount());
                String awardedValue = isWinner ? String.format("%.2f", award.getAwardedValue()) : "";
                
                // Build award notice URL
                String awardNoticeUrl = baseUrl + "/supplier/award-notice?tenderId=" + tenderId;
                
                // Send email
                boolean sent = emailService.sendAwardNotification(
                    supplier.getEmail(),
                    supplier.getFullName(),
                    tender.getReferenceNo(),
                    tender.getTitle(),
                    outcome,
                    bidAmount,
                    awardedValue,
                    awardNoticeUrl,
                    baseUrl
                );
                
                if (sent) {
                    emailSentCount++;
                    logger.info("Email sent to " + supplier.getEmail() + 
                               " for tender " + tender.getReferenceNo() + 
                               " - Outcome: " + outcome);
                } else {
                    logger.warning("Failed to send email to " + supplier.getEmail() + 
                                  " for tender " + tender.getReferenceNo());
                }
            }
            
            logger.info("Award notifications completed: " + emailSentCount + "/" + totalBidders + " emails sent");
            
        } catch (Exception e) {
            logger.severe("Error sending award notifications for tender " + tenderId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the base URL of the application.
     * Uses ngrok public URL for external access from mobile devices.
     */
    private String getBaseUrl() {
        return "https://uneasily-amniotic-unburned.ngrok-free.dev/KarabeloMolefe2333937";
    }
}