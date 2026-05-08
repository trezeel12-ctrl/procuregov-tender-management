package com.procuregov.servlet;

import com.procuregov.dao.BidDAO;
import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.model.Bid;
import com.procuregov.model.Tender;
import com.procuregov.service.BidService;
import com.procuregov.util.AppConstants;
import com.procuregov.util.AuthUtil;
import com.procuregov.util.FileUploader;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB threshold before writing to disk
    maxFileSize       = 1024 * 1024 * 10, // 10MB max for bid supporting documents (Module 3 Requirement)
    maxRequestSize    = 1024 * 1024 * 15  // 15MB max total request size
)
public class BidServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(BidServlet.class.getName());

    private BidService bidService;
    private TenderDAO tenderDAO;
    private String uploadBasePath;

    @Override
    public void init() throws ServletException {
        // Dependency injection of DAOs per Module 5 requirement
        TenderDAO tDAO = new TenderDAOImpl();
        BidDAO bDAO = new BidDAOImpl();
        
        bidService = new BidService(bDAO, tDAO);
        tenderDAO = tDAO;
        
        // Upload directory read from web.xml context-param — no hardcoded paths (Module 5 Requirement)
        uploadBasePath = getServletContext().getInitParameter(AppConstants.UPLOAD_BASE_PATH_PARAM);
        if (uploadBasePath == null || uploadBasePath.isEmpty()) {
            logger.severe("[BidServlet.init] upload.base.path not configured in web.xml");
        }
    }

    /**
     * Handles GET requests: view tender detail for bid submission.
     * Checks if tender is open and if supplier has already bid.
     * 
     * @param req  the HTTP request
     * @param resp the HTTP response
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Role guard: only Suppliers can access bid submission pages
        if (!AuthUtil.requireSupplier(req, resp)) {
            return;
        }

        String action = req.getParameter("action");

        if ("view".equals(action)) {
            try {
                int tenderId = Integer.parseInt(req.getParameter("id"));
                Tender tender = tenderDAO.findById(tenderId);
                
                if (tender == null) {
                    resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=tender_not_found");
                    return;
                }

                int supplierId = AuthUtil.getSessionUserId(req);
                
                // Module 3 Requirement: Check if supplier has already submitted a bid for this tender
                boolean alreadyBid = bidService.hasSupplierBid(tenderId, supplierId);
                
                // Module 3 Requirement: Server-side closing date check using LocalDateTime
                boolean isClosed = LocalDateTime.now().isAfter(tender.getClosingDateTime());

                req.setAttribute("tender", tender);
                req.setAttribute("alreadyBid", alreadyBid);
                req.setAttribute("isClosed", isClosed);
                
                req.getRequestDispatcher("/WEB-INF/jsp/supplier/tender.jsp")
                   .forward(req, resp);
                   
            } catch (NumberFormatException e) {
                logger.warning("[BidServlet.doGet] Invalid tender ID: " + e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=invalid_id");
            } catch (Exception e) {
                logger.severe("[BidServlet.doGet] Unexpected error: " + e.getMessage());
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            // Default: redirect to supplier dashboard
            resp.sendRedirect(req.getContextPath() + "/supplier/dashboard");
        }
    }

    /**
     * Handles POST requests: submit a new bid for a tender.
     * Enforces all Module 3 business rules server-side.
     * 
     * @param req  the HTTP request containing bid form data and file upload
     * @param resp the HTTP response for redirect after processing
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Role guard: only Suppliers can submit bids
        if (!AuthUtil.requireSupplier(req, resp)) {
            return;
        }

        try {
            int tenderId = Integer.parseInt(req.getParameter("tenderId"));
            Tender tender = tenderDAO.findById(tenderId);

            // Module 3 Requirement: SERVER-SIDE CLOSING DATE CHECK
            // Must use LocalDateTime comparison in Servlet — JSP must never make this determination
            if (tender == null || LocalDateTime.now().isAfter(tender.getClosingDateTime())) {
                req.setAttribute("errorMsg", "Submission rejected: Tender closing date has passed.");
                req.setAttribute("tender", tender);
                req.getRequestDispatcher("/WEB-INF/jsp/supplier/tender.jsp")
                   .forward(req, resp);
                return;
            }

            int supplierId = AuthUtil.getSessionUserId(req);

            // Module 3 Requirement: Enforce one bid per supplier per tender
            if (bidService.hasSupplierBid(tenderId, supplierId)) {
                req.setAttribute("errorMsg", "You have already submitted a bid for this tender. Only one submission is permitted.");
                req.setAttribute("tender", tender);
                req.getRequestDispatcher("/WEB-INF/jsp/supplier/tender.jsp")
                   .forward(req, resp);
                return;
            }

            // Module 3 Requirement: Bid data must be encapsulated in a JavaBean
            Bid bid = buildBidFromRequest(req);
            bid.setTenderId(tenderId);
            bid.setSupplierId(supplierId);

            // Module 3 Requirement: File upload via Part API — no third-party library
            Part filePart = req.getPart("supportingDoc");
            if (filePart != null && filePart.getSize() > 0) {
                // Validate file size (max 10MB per Module 3)
                if (!FileUploader.isValidSize(filePart, AppConstants.MAX_BID_FILE_SIZE)) {
                    req.setAttribute("errorMsg", "Supporting document must be under 10MB.");
                    req.setAttribute("tender", tender);
                    req.getRequestDispatcher("/WEB-INF/jsp/supplier/bid-submit.jsp")
                       .forward(req, resp);
                    return;
                }
                // Validate file type (PDF or DOCX per Module 3)
                String contentType = filePart.getContentType();
                if (!isValidBidDocumentType(contentType)) {
                    req.setAttribute("errorMsg", "Supporting document must be a PDF or DOCX file.");
                    req.setAttribute("tender", tender);
                    req.getRequestDispatcher("/WEB-INF/jsp/supplier/bid-submit.jsp")
                       .forward(req, resp);
                    return;
                }
                // Save file to server filesystem and get relative path for database
                String docPath = FileUploader.save(filePart, uploadBasePath, "bids");
                bid.setSupportingDocPath(docPath);
            } else {
                req.setAttribute("errorMsg", "Supporting document is required.");
                req.setAttribute("tender", tender);
                req.getRequestDispatcher("/WEB-INF/jsp/supplier/bid-submit.jsp")
                   .forward(req, resp);
                return;
            }

            // Persist bid via service layer
            int bidId = bidService.submitBid(bid);
            
            if (bidId > 0) {
                // Module 3 Requirement: POST-Redirect-GET pattern to prevent duplicate submissions
                resp.sendRedirect(req.getContextPath() + "/supplier/dashboard?success=bid_submitted");
            } else {
                req.setAttribute("errorMsg", "Failed to submit bid. Please try again.");
                req.setAttribute("tender", tender);
                req.getRequestDispatcher("/WEB-INF/jsp/supplier/bid-submit.jsp")
                   .forward(req, resp);
            }

        } catch (NumberFormatException e) {
            logger.warning("[BidServlet.doPost] Invalid numeric parameter: " + e.getMessage());
            req.setAttribute("errorMsg", "Invalid input format. Please check your entries.");
            req.getRequestDispatcher("/WEB-INF/jsp/supplier/bid-submit.jsp")
               .forward(req, resp);
        } catch (Exception e) {
            logger.severe("[BidServlet.doPost] Unexpected error: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Builds a Bid JavaBean from HTTP request parameters.
     * Encapsulates bid data in a JavaBean per Module 3 requirement.
     * 
     * @param req the HTTP request containing form parameters
     * @return populated Bid object (without bidId, tenderId, or supplierId)
     */
    private Bid buildBidFromRequest(HttpServletRequest req) {
        Bid bid = new Bid();
        
        // Parse monetary value safely
        String amountStr = req.getParameter("bidAmount");
        if (amountStr != null && !amountStr.trim().isEmpty()) {
            bid.setBidAmount(new BigDecimal(amountStr.trim()));
        }
        
        // Module 3 Requirement: Technical statement max 600 characters (enforced in JSP + Servlet)
        String statement = req.getParameter("technicalStatement");
        if (statement != null && statement.length() > 600) {
            statement = statement.substring(0, 600);
        }
        bid.setTechnicalStatement(statement);
        
        // Parse timeline days safely
        String daysStr = req.getParameter("timelineDays");
        if (daysStr != null && !daysStr.trim().isEmpty()) {
            try {
                bid.setProposedTimelineDays(Integer.parseInt(daysStr.trim()));
            } catch (NumberFormatException e) {
                bid.setProposedTimelineDays(0); // Will be validated by service layer
            }
        }
        
        return bid;
    }

    /**
     * Validates that a content type matches allowed bid document types.
     * Helper method for Module 3 file upload validation.
     * 
     * @param contentType the MIME type from the uploaded file part
     * @return true if type is allowed, false otherwise
     */
    private boolean isValidBidDocumentType(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase().trim();
        
        // Module 3 Requirement: Allow PDF or DOCX only
        return ct.equals("application/pdf") ||
               ct.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               ct.equals("application/msword");
    }
}