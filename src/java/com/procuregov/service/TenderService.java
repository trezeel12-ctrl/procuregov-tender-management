package com.procuregov.service;

import com.procuregov.dao.TenderDAO;
import com.procuregov.model.Tender;
import com.procuregov.util.AppConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * TenderService - Business logic for tender management.
 * 
 * Exam Compliance:
 * - Module 2: Tender data encapsulated in JavaBean before passing to DAO
 * - Module 2: Reference number generation MPW-YYYY-NNNN
 * - Module 2: Status transition enforcement
 * - Module 4: Auto-close expired tenders
 */
public class TenderService {

    private static final Logger logger = Logger.getLogger(TenderService.class.getName());
    private final TenderDAO tenderDAO;

    // Tender lifecycle sequence (STRICT ORDER)
    private static final List<String> VALID_SEQUENCE = Arrays.asList(
            AppConstants.STATUS_DRAFT,
            AppConstants.STATUS_OPEN,
            AppConstants.STATUS_CLOSED,
            AppConstants.STATUS_UNDER_EVALUATION,
            AppConstants.STATUS_EVALUATED,
            AppConstants.STATUS_AWARDED
    );

    // Valid statuses for dropdown filters
    private static final List<String> ALL_STATUSES = Arrays.asList(
            "DRAFT", "OPEN", "CLOSED", "UNDER_EVALUATION", "EVALUATED", "AWARDED"
    );

    // Valid categories for dropdown filters
    private static final List<String> ALL_CATEGORIES = Arrays.asList(
            "Construction", "Roads", "Electrical", "Plumbing", "General Services"
    );

    public TenderService(TenderDAO tenderDAO) {
        if (tenderDAO == null) {
            throw new IllegalArgumentException("TenderDAO cannot be null");
        }
        this.tenderDAO = tenderDAO;
    }

    // =========================================================
    // BASIC FETCH METHODS
    // =========================================================

    /**
     * Fetches a tender by its ID.
     * @param tenderId the tender ID
     * @return Tender object or null if not found
     */
    public Tender getTenderById(int tenderId) {
        try {
            return tenderDAO.findById(tenderId);
        } catch (Exception e) {
            logger.severe("Error fetching tender: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches tenders with optional filters (status, category, search, sort).
     * @param status status filter (nullable)
     * @param category category filter (nullable)
     * @param search search term for reference/title/description (nullable)
     * @param sortBy column to sort by (ref, title, value, closing_date, status)
     * @param sortDir sort direction (ASC or DESC)
     * @return filtered and sorted list of tenders
     */
    public List<Tender> getTenders(String status, String category, String search, String sortBy, String sortDir) {
        try {
            // Normalize inputs
            status = normalize(status);
            category = normalize(category);
            search = normalize(search);
            sortBy = normalizeSortBy(sortBy);
            sortDir = normalizeSortDir(sortDir);

            // Fetch base list from DAO with filters
            List<Tender> tenders = fetchFromDAO(status, category);
            
            if (tenders == null) {
                return new ArrayList<>();
            }
            
            // Apply search filter (in-memory for flexibility)
            if (search != null && !search.isEmpty()) {
                tenders = filterBySearch(tenders, search.toLowerCase());
            }
            
            // Apply sorting
            tenders = sortTenders(tenders, sortBy, sortDir);
            
            logger.info("getTenders: status=" + status + ", category=" + category + 
                       ", search=" + search + ", sortBy=" + sortBy + ", count=" + tenders.size());
            
            return tenders;
            
        } catch (Exception e) {
            logger.severe("Error fetching tenders: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Legacy method for backward compatibility.
     */
    public List<Tender> getTenders(String status, String category) {
        return getTenders(status, category, null, "created_at", "DESC");
    }

    /**
     * Fetches tenders by role (for role-based dashboards).
     */
    public List<Tender> getTendersForRole(String role) {
        try {
            List<Tender> all = tenderDAO.findAll();
            if (role == null) return all;
            switch (role) {
                case AppConstants.ROLE_SUPPLIER:
                    return filterByStatus(all, AppConstants.STATUS_OPEN);
                case AppConstants.ROLE_EVALUATOR:
                    // Evaluators see CLOSED and UNDER_EVALUATION
                    List<Tender> result = new ArrayList<>();
                    result.addAll(filterByStatus(all, AppConstants.STATUS_CLOSED));
                    result.addAll(filterByStatus(all, AppConstants.STATUS_UNDER_EVALUATION));
                    return result;
                case AppConstants.ROLE_OFFICER:
                    return all;
                default:
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.severe("Error fetching tenders by role: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // =========================================================
    // FILTERING AND SORTING HELPERS
    // =========================================================

    private List<Tender> fetchFromDAO(String status, String category) {
        if (status != null && category != null) {
            return tenderDAO.findByStatusAndCategory(status, category);
        }
        if (status != null) {
            return tenderDAO.findByStatus(status);
        }
        if (category != null) {
            return tenderDAO.findByCategory(category);
        }
        return tenderDAO.findAll();
    }

    private List<Tender> filterBySearch(List<Tender> tenders, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return tenders;
        }
        return tenders.stream()
            .filter(t -> 
                (t.getReferenceNo() != null && t.getReferenceNo().toLowerCase().contains(searchTerm)) ||
                (t.getTitle() != null && t.getTitle().toLowerCase().contains(searchTerm)) ||
                (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchTerm))
            )
            .collect(Collectors.toList());
    }

    private List<Tender> sortTenders(List<Tender> tenders, String sortBy, String sortDir) {
        Comparator<Tender> comparator = getComparator(sortBy);
        if (comparator == null) {
            return tenders;
        }
        if ("DESC".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return tenders.stream().sorted(comparator).collect(Collectors.toList());
    }

    private Comparator<Tender> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Tender::getCreatedAt);
        switch (sortBy) {
            case "ref":
            case "reference":
                return Comparator.comparing(Tender::getReferenceNo, Comparator.nullsLast(String::compareTo));
            case "title":
                return Comparator.comparing(Tender::getTitle, Comparator.nullsLast(String::compareTo));
            case "value":
            case "estimated_value":
                return Comparator.comparing(Tender::getEstimatedValue, Comparator.nullsLast(Comparator.naturalOrder()));
            case "closing_date":
                return Comparator.comparing(Tender::getClosingDateTime, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status":
                return Comparator.comparing(Tender::getStatus, Comparator.nullsLast(String::compareTo));
            case "created_at":
            default:
                return Comparator.comparing(Tender::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
    }

    // =========================================================
    // CREATE / UPDATE
    // =========================================================

    /**
     * Creates a new tender with DRAFT status.
     * @param tender the Tender object to create
     * @return true if successful, false otherwise
     */
    public boolean createTender(Tender tender) {
        if (tender == null) {
            logger.warning("createTender: Tender is null");
            return false;
        }
        try {
            int id = tenderDAO.insert(tender);
            boolean success = id > 0;
            if (success) {
                logger.info("createTender: Successfully created tender ID " + id);
            }
            return success;
        } catch (Exception e) {
            logger.severe("Create tender failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing tender (only allowed in DRAFT status).
     * @param tender the Tender object with updated values
     * @return true if successful, false otherwise
     */
    public boolean updateTender(Tender tender) {
        if (tender == null || tender.getTenderId() <= 0) {
            logger.warning("updateTender: Invalid tender data");
            return false;
        }
        
        Tender existing = tenderDAO.findById(tender.getTenderId());
        if (existing == null) {
            logger.warning("updateTender: Tender not found: " + tender.getTenderId());
            return false;
        }
        
        if (!AppConstants.STATUS_DRAFT.equals(existing.getStatus())) {
            logger.warning("updateTender: Cannot edit non-draft tender. Current status: " + existing.getStatus());
            return false;
        }
        
        try {
            boolean success = tenderDAO.update(tender);
            if (success) {
                logger.info("updateTender: Successfully updated tender ID " + tender.getTenderId());
            }
            return success;
        } catch (Exception e) {
            logger.severe("Update failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // STATUS MANAGEMENT (LIFECYCLE)
    // =========================================================

    /**
     * Officer publishes tender: DRAFT → OPEN
     */
    public boolean publishTender(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("publishTender: Tender not found: " + tenderId);
                return false;
            }
            if (!AppConstants.STATUS_DRAFT.equals(tender.getStatus())) {
                logger.warning("publishTender: Tender not in DRAFT status: " + tender.getStatus());
                return false;
            }
            if (tender.getClosingDateTime().isBefore(LocalDateTime.now())) {
                logger.warning("publishTender: Closing date is in the past");
                return false;
            }
            boolean updated = tenderDAO.updateStatus(tenderId, AppConstants.STATUS_OPEN);
            logger.info("publishTender: Tender " + tenderId + " published to OPEN");
            return updated;
        } catch (Exception e) {
            logger.severe("publishTender error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auto-close expired tenders: OPEN → CLOSED
     */
    public int autoCloseExpiredTenders() {
        try {
            int closed = tenderDAO.closeExpiredTenders();
            if (closed > 0) {
                logger.info("autoCloseExpiredTenders: Closed " + closed + " expired tenders");
            }
            return closed;
        } catch (Exception e) {
            logger.severe("autoCloseExpiredTenders error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Evaluator starts evaluation: CLOSED → UNDER_EVALUATION
     */
    public boolean startEvaluation(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("startEvaluation: Tender not found: " + tenderId);
                return false;
            }
            if (!AppConstants.STATUS_CLOSED.equals(tender.getStatus())) {
                logger.warning("startEvaluation: Tender not in CLOSED status: " + tender.getStatus());
                return false;
            }
            boolean updated = tenderDAO.updateStatus(tenderId, AppConstants.STATUS_UNDER_EVALUATION);
            if (updated) {
                logger.info("startEvaluation: Tender " + tenderId + " moved to UNDER_EVALUATION");
            }
            return updated;
        } catch (Exception e) {
            logger.severe("startEvaluation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Auto-transition: UNDER_EVALUATION → EVALUATED (called after all evaluators submit)
     */
    public boolean transitionToEvaluated(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("transitionToEvaluated: Tender not found: " + tenderId);
                return false;
            }
            if (!AppConstants.STATUS_UNDER_EVALUATION.equals(tender.getStatus())) {
                logger.warning("transitionToEvaluated: Tender not in UNDER_EVALUATION status: " + tender.getStatus());
                return false;
            }
            boolean updated = tenderDAO.updateStatus(tenderId, AppConstants.STATUS_EVALUATED);
            if (updated) {
                logger.info("transitionToEvaluated: Tender " + tenderId + " moved to EVALUATED");
            }
            return updated;
        } catch (Exception e) {
            logger.severe("transitionToEvaluated error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Officer awards contract: EVALUATED → AWARDED
     */
    public boolean awardTender(int tenderId) {
        try {
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.warning("awardTender: Tender not found: " + tenderId);
                return false;
            }
            if (!AppConstants.STATUS_EVALUATED.equals(tender.getStatus())) {
                logger.warning("awardTender: Tender not in EVALUATED status: " + tender.getStatus());
                return false;
            }
            boolean updated = tenderDAO.updateStatus(tenderId, AppConstants.STATUS_AWARDED);
            if (updated) {
                logger.info("awardTender: Tender " + tenderId + " awarded");
            }
            return updated;
        } catch (Exception e) {
            logger.severe("awardTender error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generic status update with transition validation.
     */
    public boolean updateStatus(int tenderId, String currentStatus, String newStatus) {
        if (!isValidTransition(currentStatus, newStatus)) {
            logger.warning("updateStatus: Invalid transition from " + currentStatus + " to " + newStatus);
            return false;
        }
        try {
            return tenderDAO.updateStatus(tenderId, newStatus);
        } catch (Exception e) {
            logger.severe("Status update failed: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTenderStatus(int tenderId, String status) {
        try {
            return tenderDAO.updateStatus(tenderId, status);
        } catch (Exception e) {
            logger.severe("updateTenderStatus error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates if a status transition follows the lifecycle sequence.
     */
    public boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        int current = VALID_SEQUENCE.indexOf(currentStatus);
        int next = VALID_SEQUENCE.indexOf(newStatus);
        return next == current + 1;
    }

    // =========================================================
    // REFERENCE NUMBER GENERATION
    // =========================================================

    /**
     * Generates a unique reference number in format: MPW-YYYY-NNNN
     */
    public String generateReferenceNumber() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = "MPW-" + year + "-";
        List<Tender> all = tenderDAO.findAll();
        int max = 0;
        for (Tender t : all) {
            if (t.getReferenceNo() != null && t.getReferenceNo().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(t.getReferenceNo().replace(prefix, ""));
                    if (num > max) max = num;
                } catch (Exception ignored) {}
            }
        }
        return prefix + String.format("%04d", max + 1);
    }

    // =========================================================
    // UTILITY METHODS
    // =========================================================

    /**
     * Gets all available status values for filter dropdowns.
     */
    public List<String> getAllStatuses() {
        return ALL_STATUSES;
    }

    /**
     * Gets all available category values for filter dropdowns.
     */
    public List<String> getAllCategories() {
        return ALL_CATEGORIES;
    }

    /**
     * Gets statistics counts for the dashboard.
     */
    public Map<String, Integer> getTenderStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        List<Tender> all = tenderDAO.findAll();
        stats.put("total", all.size());
        stats.put("draft", filterByStatus(all, AppConstants.STATUS_DRAFT).size());
        stats.put("open", filterByStatus(all, AppConstants.STATUS_OPEN).size());
        stats.put("closed", filterByStatus(all, AppConstants.STATUS_CLOSED).size());
        stats.put("underEvaluation", filterByStatus(all, AppConstants.STATUS_UNDER_EVALUATION).size());
        stats.put("evaluated", filterByStatus(all, AppConstants.STATUS_EVALUATED).size());
        stats.put("awarded", filterByStatus(all, AppConstants.STATUS_AWARDED).size());
        return stats;
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("all")) {
            return null;
        }
        return value.trim();
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "created_at";
        }
        return sortBy.trim();
    }

    private String normalizeSortDir(String sortDir) {
        if (sortDir == null || sortDir.trim().isEmpty()) {
            return "DESC";
        }
        return "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
    }

    private List<Tender> filterByStatus(List<Tender> list, String status) {
        List<Tender> result = new ArrayList<>();
        for (Tender t : list) {
            if (status.equalsIgnoreCase(t.getStatus())) {
                result.add(t);
            }
        }
        return result;
    }
}