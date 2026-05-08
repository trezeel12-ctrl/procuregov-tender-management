package com.procuregov.servlet;

import com.procuregov.dao.TenderDAO;
import com.procuregov.dao.TenderDAOImpl;
import com.procuregov.model.Tender;
import com.procuregov.service.TenderService;
import com.procuregov.util.AppConstants;
import com.procuregov.util.AuthUtil;
import com.procuregov.util.FileUploader;
import com.procuregov.dao.BidDAO;
import com.procuregov.dao.BidDAOImpl;
import com.procuregov.dao.EvaluationDAO;
import com.procuregov.dao.EvaluationDAOImpl;
import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.service.EvaluationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller for Procurement Officer tender management.
 * Handles creation, listing, editing, searching, sorting and status transitions.
 * 
 * Exam Compliance:
 * - Module 2: File upload using Part API, no third-party libraries
 * - Module 2: Tender JavaBean encapsulation before DAO operations
 * - Module 2: Officers can edit tenders only while in DRAFT status
 * - Module 5: DAO pattern for data access, exceptions logged
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
public class TenderServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TenderServlet.class.getName());
    private TenderService tenderService;
    private String uploadBasePath;
    private EvaluationService evalService;

    // Date formatter for UI display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public void init() throws ServletException {
        TenderDAO dao = new TenderDAOImpl();
        tenderService = new TenderService(dao);

        // Initialize EvaluationService for evaluation checks
        BidDAO bidDAO = new BidDAOImpl();
        EvaluationDAO evaluationDAO = new EvaluationDAOImpl();
        UserDAO userDAO = new UserDAOImpl();
        evalService = new EvaluationService(evaluationDAO, bidDAO, dao, userDAO);

        uploadBasePath = getServletContext().getInitParameter("upload.base.path");
        if (uploadBasePath == null || uploadBasePath.isEmpty()) {
            logger.severe("[TenderServlet.init] upload.base.path not configured in web.xml");
            uploadBasePath = getServletContext().getRealPath("/uploads");
        }
        logger.info("[TenderServlet.init] Upload base path: " + uploadBasePath);
        logger.info("[TenderServlet.init] TenderService and EvaluationService initialized successfully");
    }

    // =========================================================
    // DO GET - HANDLE VIEW, CREATE, EDIT, LIST, SEARCH, SORT
    // =========================================================
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!AuthUtil.requireOfficer(req, resp)) {
            logger.warning("[TenderServlet.doGet] Access denied - not an officer");
            return;
        }

        // Auto-close expired tenders before displaying
        autoCloseExpiredTenders();
        
        // Get all tenders with filters for the dropdowns
        List<Tender> allTenders = tenderService.getTenders(null, null);
        req.setAttribute("allTenders", allTenders);
        
        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "create":
                    handleCreateForm(req, resp);
                    break;
                case "edit":
                    handleEditForm(req, resp);
                    break;
                case "view":
                    handleViewTender(req, resp);
                    break;
                case "list":
                default:
                    handleListTenders(req, resp);
                    break;
            }
        } catch (Exception e) {
            logger.severe("[TenderServlet.doGet] Error: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("errorMessage", "An error occurred while processing your request.");
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
        }
    }

    /**
     * Handles create tender form display.
     */
    private void handleCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logger.info("[TenderServlet] Displaying create tender form");
        req.setAttribute("action", "create");
        req.setAttribute("pageTitle", "Create New Tender");
        req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
    }

    /**
     * Handles edit tender form display (only for DRAFT tenders).
     */
    private void handleEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String tenderIdParam = req.getParameter("tenderId");
        if (tenderIdParam == null || tenderIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=missing_id");
            return;
        }
        
        int tenderId = Integer.parseInt(tenderIdParam);
        Tender tender = tenderService.getTenderById(tenderId);
        
        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=notfound");
            return;
        }
        
        if (!AppConstants.STATUS_DRAFT.equals(tender.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=cannot_edit");
            return;
        }
        
        logger.info("[TenderServlet] Editing tender ID: " + tenderId);
        req.setAttribute("action", "edit");
        req.setAttribute("pageTitle", "Edit Tender");
        req.setAttribute("tender", tender);
        req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
    }

    /**
     * Handles view tender details.
     */
    private void handleViewTender(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String tenderIdParam = req.getParameter("tenderId");
        if (tenderIdParam == null || tenderIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=missing_id");
            return;
        }
        
        int tenderId = Integer.parseInt(tenderIdParam);
        Tender tender = tenderService.getTenderById(tenderId);
        
        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=notfound");
            return;
        }
        
        logger.info("[TenderServlet] Viewing tender ID: " + tenderId);
        req.setAttribute("action", "view");
        req.setAttribute("pageTitle", "View Tender Details");
        req.setAttribute("tender", tender);
        req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
    }

    /**
     * Handles list tenders with search, filter, and sort.
     */
    private void handleListTenders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // Get filter parameters
        String statusFilter = req.getParameter("status");
        String categoryFilter = req.getParameter("category");
        String searchTerm = req.getParameter("search");
        String sortBy = req.getParameter("sortBy");
        String sortDir = req.getParameter("sortDir");
        
        // Normalize sort direction
        if (sortDir == null || sortDir.isEmpty()) {
            sortDir = "DESC";
        } else if (!"ASC".equalsIgnoreCase(sortDir)) {
            sortDir = "DESC";
        } else {
            sortDir = "ASC";
        }
        
        // Fetch tenders with filters
        List<Tender> tenders = tenderService.getTenders(
            statusFilter, categoryFilter, searchTerm, sortBy, sortDir
        );
        
        if (tenders == null) {
            tenders = new java.util.ArrayList<>();
        }
        
        // Calculate statistics for the overview section
        List<Tender> allTenders = tenderService.getTenders(null, null);
        if (allTenders == null) allTenders = new java.util.ArrayList<>();
        
        int draftCount = 0, openCount = 0, closedCount = 0, underEvalCount = 0, evaluatedCount = 0, awardedCount = 0;
        BigDecimal totalEstimatedValue = BigDecimal.ZERO;
        
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
        
        int completionRate = allTenders.isEmpty() ? 0 : (awardedCount * 100) / allTenders.size();
        
        // Set attributes for JSP
        req.setAttribute("tenders", tenders);
        req.setAttribute("searchTerm", searchTerm);
        req.setAttribute("currentStatus", statusFilter);
        req.setAttribute("currentCategory", categoryFilter);
        req.setAttribute("currentSortBy", sortBy);
        req.setAttribute("currentSortDir", sortDir);
        
        // Statistics for overview
        req.setAttribute("totalTenders", allTenders.size());
        req.setAttribute("draftCount", draftCount);
        req.setAttribute("openCount", openCount);
        req.setAttribute("closedCount", closedCount);
        req.setAttribute("underEvalCount", underEvalCount);
        req.setAttribute("evaluatedCount", evaluatedCount);
        req.setAttribute("awardedCount", awardedCount);
        req.setAttribute("totalEstimatedValue", totalEstimatedValue);
        req.setAttribute("completionRate", completionRate);
        
        // Lists for dropdowns
        req.setAttribute("allStatuses", tenderService.getAllStatuses());
        req.setAttribute("allCategories", tenderService.getAllCategories());
        
        logger.info("[TenderServlet] Found " + tenders.size() + " tenders with filters");
        
        req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
    }

    // =========================================================
    // DO POST - HANDLE CREATE, UPDATE, STATUS CHANGE
    // =========================================================
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!AuthUtil.requireOfficer(req, resp)) {
            logger.warning("[TenderServlet.doPost] Access denied - not an officer");
            return;
        }

        String action = req.getParameter("action");

        try {
            switch (action) {
                case "create":
                    handleCreateTender(req, resp);
                    break;
                case "update":
                    handleUpdateTender(req, resp);
                    break;
                case "updateStatus":
                    handleStatusUpdate(req, resp);
                    break;
                default:
                    resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list");
                    break;
            }
        } catch (Exception e) {
            logger.severe("[TenderServlet.doPost] Error: " + e.getMessage());
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=true");
        }
    }

    // =========================================================
    // CREATE TENDER HANDLER
    // =========================================================
    
    private void handleCreateTender(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        logger.info("[TenderServlet] Creating new tender...");

        String saveType = req.getParameter("saveType");
        boolean publishTender = "publish".equalsIgnoreCase(saveType);
        boolean saveDraft = "draft".equalsIgnoreCase(saveType);

        // Validate and get parameters
        String title = req.getParameter("title");
        String category = req.getParameter("category");
        String description = req.getParameter("description");
        String estimatedValueStr = req.getParameter("estimatedValue");
        String closingDateStr = req.getParameter("closingDateTime");

        // Validate required fields for publishing
        if (publishTender) {
            String validationError = validateRequiredFields(title, category, description, estimatedValueStr, closingDateStr);
            if (validationError != null) {
                req.setAttribute("error", validationError);
                req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
                return;
            }
        }

        // Parse estimated value
        BigDecimal estimatedValue = null;
        try {
            if (estimatedValueStr != null && !estimatedValueStr.trim().isEmpty()) {
                estimatedValue = new BigDecimal(estimatedValueStr);
            }
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid estimated value format.");
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
            return;
        }

        // Parse and convert closing date timezone
        LocalDateTime closingDateTime = parseAndConvertClosingDateTime(closingDateStr);
        if (closingDateTime == null && publishTender) {
            req.setAttribute("error", "Invalid closing date format.");
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
            return;
        }

        // Handle file upload
        Part filePart = req.getPart("noticeDoc");
        String filePath = null;

        if (publishTender) {
            if (filePart == null || filePart.getSize() == 0) {
                req.setAttribute("error", "Tender notice PDF is required for publishing.");
                req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
                return;
            }
            
            if (!FileUploader.isValidType(filePart, "pdf")) {
                req.setAttribute("error", "Tender notice must be a PDF file.");
                forwardWithError(req, resp, "/WEB-INF/jsp/officer/tender.jsp");
                return;
            }
            
            if (!FileUploader.isValidSize(filePart, AppConstants.MAX_TENDER_FILE_SIZE)) {
                req.setAttribute("error", "Tender notice must be under 5MB.");
                forwardWithError(req, resp, "/WEB-INF/jsp/officer/tender.jsp");
                return;
            }
            
            filePath = FileUploader.save(filePart, uploadBasePath, "tenders");
            logger.info("[TenderServlet] File saved: " + filePath);
        }

        // Build Tender object
        Tender tender = new Tender();
        tender.setTitle(title != null ? title.trim() : null);
        tender.setCategory(category != null ? category.trim() : null);
        tender.setDescription(description != null ? description.trim() : null);
        tender.setEstimatedValue(estimatedValue);
        tender.setClosingDateTime(closingDateTime);
        tender.setNoticeFilePath(filePath);
        tender.setCreatedBy(AuthUtil.getSessionUserId(req));
        tender.setReferenceNo(generateReferenceNumber());
        
        if (publishTender) {
            tender.setStatus(AppConstants.STATUS_OPEN);
        } else {
            tender.setStatus(AppConstants.STATUS_DRAFT);
        }

        boolean success = tenderService.createTender(tender);

        if (success) {
            logger.info("[TenderServlet] Tender created successfully: " + tender.getReferenceNo());
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&success=created");
        } else {
            logger.severe("[TenderServlet] Failed to create tender");
            req.setAttribute("error", "Failed to create tender. Please try again.");
            req.setAttribute("tender", tender);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
        }
    }

    // =========================================================
    // UPDATE TENDER HANDLER
    // =========================================================
    
    private void handleUpdateTender(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        logger.info("[TenderServlet] Updating tender...");

        String tenderIdParam = req.getParameter("tenderId");
        if (tenderIdParam == null || tenderIdParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=missing_id");
            return;
        }

        int tenderId = Integer.parseInt(tenderIdParam);
        Tender existing = tenderService.getTenderById(tenderId);

        if (existing == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=not_found");
            return;
        }

        if (!AppConstants.STATUS_DRAFT.equals(existing.getStatus())) {
            logger.warning("[TenderServlet] Attempted edit on non-draft tender: " + tenderId);
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=cannot_edit_non_draft");
            return;
        }
        
        String submitType = req.getParameter("saveType");
        boolean publishTender = "publish".equalsIgnoreCase(submitType);

        // Get form data
        String title = req.getParameter("title");
        String category = req.getParameter("category");
        String description = req.getParameter("description");
        String estimatedValueStr = req.getParameter("estimatedValue");
        String closingDateStr = req.getParameter("closingDateTime");

        // Validate for publish
        if (publishTender) {
            String validationError = validateRequiredFields(title, category, description, estimatedValueStr, closingDateStr);
            if (validationError != null) {
                req.setAttribute("error", validationError);
                req.setAttribute("tender", existing);
                req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
                return;
            }
        }

        // Parse estimated value
        BigDecimal estimatedValue = existing.getEstimatedValue();
        try {
            if (estimatedValueStr != null && !estimatedValueStr.trim().isEmpty()) {
                estimatedValue = new BigDecimal(estimatedValueStr.trim());
            }
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid estimated value format.");
            req.setAttribute("tender", existing);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
            return;
        }

        // Parse closing date
        LocalDateTime closingDateTime = parseAndConvertClosingDateTime(closingDateStr);
        if (closingDateTime == null && publishTender) {
            req.setAttribute("error", "Invalid closing date format.");
            req.setAttribute("tender", existing);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
            return;
        } else if (closingDateTime == null) {
            closingDateTime = existing.getClosingDateTime();
        }

        // Handle file upload for publish
        Part filePart = req.getPart("noticeDoc");
        String filePath = existing.getNoticeFilePath();

        if (publishTender && filePart != null && filePart.getSize() > 0) {
            if (!FileUploader.isValidType(filePart, "pdf")) {
                req.setAttribute("error", "Notice must be a PDF file.");
                req.setAttribute("tender", existing);
                req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
                return;
            }
            if (!FileUploader.isValidSize(filePart, AppConstants.MAX_TENDER_FILE_SIZE)) {
                req.setAttribute("error", "PDF exceeds size limit of 5MB.");
                req.setAttribute("tender", existing);
                req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
                return;
            }
            filePath = FileUploader.save(filePart, uploadBasePath, "tenders");
            logger.info("[TenderServlet] New file uploaded: " + filePath);
        }

        // Update tender
        existing.setTitle(title != null ? title.trim() : existing.getTitle());
        existing.setCategory(category != null ? category.trim() : existing.getCategory());
        existing.setDescription(description != null ? description.trim() : existing.getDescription());
        existing.setEstimatedValue(estimatedValue);
        existing.setClosingDateTime(closingDateTime);
        existing.setNoticeFilePath(filePath);
        
        if (publishTender) {
            existing.setStatus(AppConstants.STATUS_OPEN);
            logger.info("[TenderServlet] Publishing tender ID: " + tenderId);
        } else {
            existing.setStatus(AppConstants.STATUS_DRAFT);
        }

        boolean success = tenderService.updateTender(existing);

        if (success) {
            logger.info("[TenderServlet] Tender updated successfully: " + tenderId);
            String successParam = publishTender ? "published" : "draft_saved";
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&success=" + successParam);
        } else {
            logger.warning("[TenderServlet] Failed to update tender: " + tenderId);
            req.setAttribute("error", "Update failed. Please try again.");
            req.setAttribute("tender", existing);
            req.getRequestDispatcher("/WEB-INF/jsp/officer/tender.jsp").forward(req, resp);
        }
    }

    // =========================================================
    // STATUS UPDATE HANDLER
    // =========================================================
    
    private void handleStatusUpdate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int tenderId = Integer.parseInt(req.getParameter("tenderId"));
        String newStatus = req.getParameter("newStatus");
        String currentStatus = req.getParameter("currentStatus");

        Tender tender = tenderService.getTenderById(tenderId);
        if (tender == null) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=notfound");
            return;
        }

        if (newStatus == null || newStatus.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=invalid_status");
            return;
        }

        // ========== NEW: CHECK IF TENDER HAS BEEN EVALUATED BEFORE ALLOWING STATUS CHANGE ==========
        // For transitions from CLOSED to UNDER_EVALUATION or UNDER_EVALUATION to EVALUATED
        // the tender must have at least one evaluation score
        if ("UNDER_EVALUATION".equals(newStatus) || "EVALUATED".equals(newStatus)) {
            // We need access to EvaluationService - add this field to your servlet
            // Add this line at the top of the servlet: private EvaluationService evalService;
            // And initialize it in init() method

            boolean hasEvaluation = evalService.hasAnyEvaluationForTender(tenderId);
            int evaluationCount = evalService.getEvaluationCountForTender(tenderId);

            if (!hasEvaluation) {
                logger.warning("Status change blocked: Tender " + tenderId + " has no evaluation scores. Cannot change to " + newStatus);
                resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=no_evaluation");
                return;
            }

            logger.info("Tender " + tenderId + " has " + evaluationCount + " evaluation score(s). Status change allowed.");
        }
        // =========================================================================================

        // Validate transition
        if (!tenderService.isValidTransition(currentStatus, newStatus)) {
            logger.warning("Invalid transition from " + currentStatus + " to " + newStatus);
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=invalid_transition");
            return;
        }

        // Special check for publishing: ensure closing date is in future
        if ("OPEN".equals(newStatus) && tender.getClosingDateTime().isBefore(LocalDateTime.now())) {
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=closing_date_passed");
            return;
        }

        boolean updated = tenderService.updateTenderStatus(tenderId, newStatus);

        if (updated) {
            logger.info("Tender " + tenderId + " status updated from " + currentStatus + " to " + newStatus);
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&success=status");
        } else {
            logger.warning("Failed to update status for tender: " + tenderId);
            resp.sendRedirect(req.getContextPath() + "/officer/tender?action=list&error=status_failed");
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================
    
    /**
     * Validates required fields for tender creation/update.
     */
    private String validateRequiredFields(String title, String category, String description, 
                                          String estimatedValue, String closingDate) {
        if (title == null || title.trim().isEmpty()) return "Title is required.";
        if (category == null || category.trim().isEmpty()) return "Category is required.";
        if (description == null || description.trim().isEmpty()) return "Description is required.";
        if (estimatedValue == null || estimatedValue.trim().isEmpty()) return "Estimated value is required.";
        if (closingDate == null || closingDate.trim().isEmpty()) return "Closing date is required.";
        return null;
    }
    
    /**
     * Parses closing date string and converts to UTC.
     */
    private LocalDateTime parseAndConvertClosingDateTime(String closingDateStr) {
        if (closingDateStr == null || closingDateStr.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime closingDateTime = LocalDateTime.parse(closingDateStr, formatter);
            
            // Add 4 hours to match timezone offset
            closingDateTime = closingDateTime.plusHours(4);
            logger.info("[TenderServlet] Original: " + closingDateStr + " | Adjusted: " + closingDateTime);
            
            // Convert to UTC
            ZonedDateTime zoned = closingDateTime.atZone(ZoneId.of("Africa/Maseru"));
            ZonedDateTime utc = zoned.withZoneSameInstant(ZoneId.of("UTC"));
            closingDateTime = utc.toLocalDateTime();
            logger.info("[TenderServlet] Converted closing time to UTC: " + closingDateTime);
            
            return closingDateTime;
        } catch (Exception e) {
            logger.warning("[TenderServlet] Failed to parse closing date: " + closingDateStr);
            return null;
        }
    }
    
    /**
     * Auto-closes expired tenders.
     */
    private void autoCloseExpiredTenders() {
        try {
            int closed = tenderService.autoCloseExpiredTenders();
            if (closed > 0) {
                logger.info("[TenderServlet] Auto-closed " + closed + " expired tenders");
            }
        } catch (Exception e) {
            logger.warning("[TenderServlet] Auto-close error: " + e.getMessage());
        }
    }
    
    /**
     * Generates a unique reference number in format: MPW-YYYY-NNNN
     */
    private String generateReferenceNumber() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String sequence = String.format("%04d", (int)(System.currentTimeMillis() % 10000));
        return "MPW-" + year + "-" + sequence;
    }
    
    /**
     * Forwards to error page with form data preserved.
     */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String path) 
            throws ServletException, IOException {
        req.setAttribute("title", req.getParameter("title"));
        req.setAttribute("category", req.getParameter("category"));
        req.setAttribute("description", req.getParameter("description"));
        req.setAttribute("estimatedValue", req.getParameter("estimatedValue"));
        req.setAttribute("closingDateTime", req.getParameter("closingDateTime"));
        req.getRequestDispatcher(path).forward(req, resp);
    }
}