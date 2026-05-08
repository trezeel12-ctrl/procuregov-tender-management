<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <!-- ================= ALERT MESSAGES ================= -->
    <c:if test="${param.success == 'created'}">
        <div class="alert alert-success">Tender created successfully!</div>
    </c:if>
    <c:if test="${param.success == 'updated'}">
        <div class="alert alert-success">Tender updated successfully!</div>
    </c:if>
    <c:if test="${param.success == 'published'}">
        <div class="alert alert-success">Tender published and is now OPEN for bidding!</div>
    </c:if>
    <c:if test="${param.success == 'draft_saved'}">
        <div class="alert alert-success">Draft saved successfully!</div>
    </c:if>
    <c:if test="${param.success == 'status'}">
        <div class="alert alert-success">Tender status updated successfully!</div>
    </c:if>

    <c:if test="${param.error == 'notfound'}">
        <div class="alert alert-error">Tender not found.</div>
    </c:if>
    <c:if test="${param.error == 'cannot_edit'}">
        <div class="alert alert-error">Only DRAFT tenders can be edited.</div>
    </c:if>
    <c:if test="${param.error == 'invalid_transition'}">
        <div class="alert alert-error">Invalid status transition.</div>
    </c:if>
        
    <c:if test="${param.error == 'missing_id'}">
        <div class="alert alert-error">Missing tender ID.</div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>
    <c:if test="${param.error == 'no_evaluation'}">
        <div class="alert alert-error">
            ❌ Cannot change status. This tender has not been evaluated yet. 
            Please ensure at least one evaluation has been submitted before changing status.
        </div>
    </c:if>

    <!-- ================= CREATE / EDIT FORM ================= -->
    <c:if test="${param.action == 'create' || param.action == 'edit'}">
        <div class="form-container">
            <div class="form-header">
                <h2>
                    <c:choose>
                        <c:when test="${param.action == 'edit'}">Edit Tender</c:when>
                        <c:otherwise>Create New Tender</c:otherwise>
                    </c:choose>
                </h2>
                <p class="subtitle">
                    <c:choose>
                        <c:when test="${param.action == 'edit'}">Modify tender details. Only DRAFT tenders can be edited.</c:when>
                        <c:otherwise>Fill in the details below to create a new tender notice.</c:otherwise>
                    </c:choose>
                </p>
            </div>

            <form method="post" action="${pageContext.request.contextPath}/officer/tender" enctype="multipart/form-data" class="tender-form">
                <input type="hidden" name="action" value="${param.action == 'edit' ? 'update' : 'create'}"/>
                <c:if test="${not empty tender}">
                    <input type="hidden" name="tenderId" value="${tender.tenderId}"/>
                </c:if>

                <div class="form-group">
                    <label>Reference Number</label>
                    <c:choose>
                        <c:when test="${empty tender}">
                            <input type="text" value="Auto-generated (MPW-YYYY-NNNN)" disabled class="form-control"/>
                            <small>The reference number will be generated automatically when you save.</small>
                        </c:when>
                        <c:otherwise>
                            <input type="text" value="${tender.referenceNo}" readonly class="form-control readonly"/>
                            <small>Reference number is system-generated and cannot be changed.</small>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="form-group">
                    <label>Title <span class="required">*</span></label>
                    <input type="text" name="title" value="${tender.title}" required class="form-control" placeholder="Enter tender title"/>
                </div>

                <div class="form-group">
                    <label>Category <span class="required">*</span></label>
                    <select name="category" required class="form-control">
                        <option value="">Select Category</option>
                        <option value="Construction" ${tender.category == 'Construction' ? 'selected' : ''}>Construction</option>
                        <option value="Roads" ${tender.category == 'Roads' ? 'selected' : ''}>Roads</option>
                        <option value="Electrical" ${tender.category == 'Electrical' ? 'selected' : ''}>Electrical</option>
                        <option value="Plumbing" ${tender.category == 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                        <option value="General Services" ${tender.category == 'General Services' ? 'selected' : ''}>General Services</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Estimated Value (Maloti) <span class="required">*</span></label>
                    <input type="number" name="estimatedValue" step="0.01" min="0" value="${tender.estimatedValue}" required class="form-control" placeholder="Enter estimated contract value"/>
                </div>

                <div class="form-group">
                    <label>Description <span class="required">*</span></label>
                    <textarea name="description" rows="5" required class="form-control" placeholder="Provide detailed scope of work, requirements, and specifications...">${tender.description}</textarea>
                    <small>Maximum 2000 characters. Be specific about requirements.</small>
                </div>

                <div class="form-group">
                    <label>Closing Date & Time <span class="required">*</span></label>
                    <input type="datetime-local" name="closingDateTime" required class="form-control" value="${not empty tender.closingDateTime ? tender.closingDateTime.toString().replace(' ', 'T').substring(0,16) : ''}"/>
                    <small>Bids must be submitted before this date and time.</small>
                </div>

                <div class="form-group">
                    <label>Tender Notice Document (PDF) <span class="required">*</span></label>
                    <c:choose>
                        <c:when test="${empty tender}">
                            <input type="file" name="noticeDoc" accept=".pdf" required class="form-control"/>
                            <small>Upload the official tender notice in PDF format (max 5MB).</small>
                        </c:when>
                        <c:otherwise>
                            <p class="file-info">Current file: <a href="${pageContext.request.contextPath}/download?path=${tender.noticeFilePath}" target="_blank">Download Existing PDF</a></p>
                            <input type="file" name="noticeDoc" accept=".pdf" class="form-control"/>
                            <small>Leave empty to keep existing file. Upload new file to replace.</small>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="form-actions">
                    <button type="submit" name="saveType" value="draft" class="btn btn-outline">Save as Draft</button>
                    <button type="submit" name="saveType" value="publish" class="btn btn-primary">Publish Tender</button>
                    <a href="${pageContext.request.contextPath}/officer/tender?action=list" class="btn btn-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </c:if>

    <!-- ================= LIST VIEW ================= -->
    <c:if test="${param.action != 'create' && param.action != 'edit' && param.action != 'view'}">

        <div class="dashboard-header">
            <div>
                <h2>Tender Management</h2>
                <p class="subtitle">Manage all procurement tenders</p>
            </div>
            <a href="${pageContext.request.contextPath}/officer/tender?action=create" class="btn btn-primary">+ Create New Tender</a>
        </div>

        <!-- STATISTICS BADGES -->
        <div class="stats-container">
            <div class="stat-item">
                <span class="stat-number">${totalTenders}</span>
                <span class="stat-name">Total</span>
            </div>
            <div class="stat-item draft">
                <span class="stat-number">${draftCount}</span>
                <span class="stat-name">Draft</span>
            </div>
            <div class="stat-item open">
                <span class="stat-number">${openCount}</span>
                <span class="stat-name">Open</span>
            </div>
            <div class="stat-item closed">
                <span class="stat-number">${closedCount}</span>
                <span class="stat-name">Closed</span>
            </div>
            <div class="stat-item evaluation">
                <span class="stat-number">${underEvalCount}</span>
                <span class="stat-name">Evaluation</span>
            </div>
            <div class="stat-item awarded">
                <span class="stat-number">${awardedCount}</span>
                <span class="stat-name">Awarded</span>
            </div>
        </div>

        <!-- SEARCH AND FILTER SECTION -->
        <div class="filter-section">
            <form method="get" action="${pageContext.request.contextPath}/officer/tender" class="filter-form" id="filterForm">
                <input type="hidden" name="action" value="list"/>
                
                <div class="search-row">
                    <div class="search-input-wrapper">
                        <input type="text" name="search" placeholder="Search by reference, title or description..." value="${searchTerm}" class="search-field"/>
                        <button type="submit" class="btn btn-primary search-btn">Search</button>
                    </div>
                </div>
                
                <div class="filter-row">
                    <div class="filter-field">
                        <label>Status</label>
                        <select name="status" class="filter-select">
                            <option value="">All Statuses</option>
                            <c:forEach var="s" items="${allStatuses}">
                                <option value="${s}" ${currentStatus == s ? 'selected' : ''}>${s}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="filter-field">
                        <label>Category</label>
                        <select name="category" class="filter-select">
                            <option value="">All Categories</option>
                            <c:forEach var="c" items="${allCategories}">
                                <option value="${c}" ${currentCategory == c ? 'selected' : ''}>${c}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="filter-field">
                        <label>Sort By</label>
                        <select name="sortBy" class="filter-select">
                            <option value="created_at" ${currentSortBy == 'created_at' ? 'selected' : ''}>Created Date</option>
                            <option value="ref" ${currentSortBy == 'ref' ? 'selected' : ''}>Reference Number</option>
                            <option value="title" ${currentSortBy == 'title' ? 'selected' : ''}>Title</option>
                            <option value="value" ${currentSortBy == 'value' ? 'selected' : ''}>Estimated Value</option>
                            <option value="closing_date" ${currentSortBy == 'closing_date' ? 'selected' : ''}>Closing Date</option>
                            <option value="status" ${currentSortBy == 'status' ? 'selected' : ''}>Status</option>
                        </select>
                    </div>
                    
                    <div class="filter-field">
                        <label>Order</label>
                        <select name="sortDir" class="filter-select">
                            <option value="DESC" ${currentSortDir == 'DESC' ? 'selected' : ''}>Descending</option>
                            <option value="ASC" ${currentSortDir == 'ASC' ? 'selected' : ''}>Ascending</option>
                        </select>
                    </div>
                    
                    <div class="filter-field filter-actions">
                        <label>&nbsp;</label>
                        <div class="filter-buttons">
                            <button type="submit" class="btn btn-primary">Apply Filters</button>
                            <a href="${pageContext.request.contextPath}/officer/tender?action=list" class="btn btn-outline">Reset</a>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <!-- RESULTS COUNT -->
        <div class="results-header">
            <span class="results-count">Found ${fn:length(tenders)} tender(s)</span>
        </div>

        <!-- TENDER TABLE WITH IN-TABLE STATUS DROPDOWN -->
        <c:choose>
            <c:when test="${empty tenders}">
                <div class="empty-state">
                    <p>No tenders found matching your criteria.</p>
                    <a href="${pageContext.request.contextPath}/officer/tender?action=create" class="btn btn-primary">Create Your First Tender</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Value (M)</th>
                                <th>Closing Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${tenders}">
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="date-cell">${t.closingDateTime.toString().substring(0, 10)}</td>
                                    <td class="status-cell">
                                        <span class="badge status-${fn:toLowerCase(t.status)}">${t.status}</span>
                                     </td>
                                    <td class="actions-cell">
                                        <!-- View Button -->
                                        <a href="${pageContext.request.contextPath}/officer/tender?action=view&tenderId=${t.tenderId}" class="btn-view">View</a>

                                        <!-- Edit Button (DRAFT only) -->
                                        <c:if test="${t.status == 'DRAFT'}">
                                            <a href="${pageContext.request.contextPath}/officer/tender?action=edit&tenderId=${t.tenderId}" class="btn-edit">Edit</a>
                                        </c:if>

                                        <c:if test="${t.status == 'EVALUATED'}">
                                            <a href="${pageContext.request.contextPath}/officer/award?tenderId=${t.tenderId}" class="btn-award">Award</a>
                                        </c:if>

                                        <!-- Status Change Dropdown (NOT for EVALUATED or AWARDED) -->
                                        <c:if test="${t.status != 'AWARDED' && t.status != 'EVALUATED' && t.status != 'OPEN'}">
                                            <form method="post" action="${pageContext.request.contextPath}/officer/tender" class="status-form" onsubmit="return confirmStatusChange('${t.referenceNo}', '${t.status}', this.querySelector('select').value)">
                                                <input type="hidden" name="action" value="updateStatus"/>
                                                <input type="hidden" name="tenderId" value="${t.tenderId}"/>
                                                <input type="hidden" name="currentStatus" value="${t.status}"/>
                                                <select name="newStatus" class="status-select" onchange="this.form.submit()">
                                                    <option value="">Change Status</option>
                                                    <c:if test="${t.status == 'DRAFT'}">
                                                        <option value="OPEN">📢 Publish (→ OPEN)</option>
                                                    </c:if>
                                                    <c:if test="${t.status == 'CLOSED'}">
                                                        <option value="UNDER_EVALUATION">📊 Start Evaluation (→ UNDER_EVALUATION)</option>
                                                    </c:if>
                                                    <c:if test="${t.status == 'UNDER_EVALUATION'}">
                                                        <option value="EVALUATED">✅ Mark as Evaluated (→ EVALUATED)</option>
                                                    </c:if>
                                                </select>
                                            </form>
                                        </c:if>
                                     </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </c:if>

    <!-- ================= VIEW SINGLE ================= -->
    <c:if test="${param.action == 'view'}">
        <div class="tender-header">
            <div>
                <h2>${tender.referenceNo}</h2>
                <p class="tender-title">${tender.title}</p>
            </div>
            <span class="badge status-${fn:toLowerCase(tender.status)}">${tender.status}</span>
        </div>

        <div class="tender-details-grid">
            <div class="detail-card">
                <span class="detail-label">Category</span>
                <span class="detail-value">${tender.category}</span>
            </div>
            <div class="detail-card">
                <span class="detail-label">Estimated Value</span>
                <span class="detail-value">M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></span>
            </div>
            <div class="detail-card">
                <span class="detail-label">Closing Date</span>
                <span class="detail-value">${tender.closingDateTime.toString().substring(0, 16).replace('T', ' ')}</span>
            </div>
            <div class="detail-card">
                <span class="detail-label">Created By</span>
                <span class="detail-value">Officer ID: ${tender.createdBy}</span>
            </div>
            <div class="detail-card full-width">
                <span class="detail-label">Description</span>
                <p class="detail-value description-text">${tender.description}</p>
            </div>
            <c:if test="${not empty tender.noticeFilePath}">
                <div class="detail-card full-width">
                    <span class="detail-label">Tender Notice</span>
                    <a href="${pageContext.request.contextPath}/download?file=${tender.noticeFilePath}" target="_blank">Download PDF</a>
                </div>
            </c:if>
        </div>

        <c:if test="${tender.status != 'AWARDED'}">
            <div class="status-transition-card">
                <h3>Update Tender Status</h3>
                <form method="post" action="${pageContext.request.contextPath}/officer/tender">
                    <input type="hidden" name="action" value="updateStatus"/>
                    <input type="hidden" name="tenderId" value="${tender.tenderId}"/>
                    <div class="status-select-group">
                        <select name="newStatus" required>
                            <option value="">Select New Status</option>
                            <c:if test="${tender.status == 'DRAFT'}">
                                <option value="OPEN">Publish (DRAFT → OPEN)</option>
                            </c:if>
                            <c:if test="${tender.status == 'CLOSED'}">
                                <option value="UNDER_EVALUATION">Start Evaluation (CLOSED → UNDER_EVALUATION)</option>
                            </c:if>
                            <c:if test="${tender.status == 'UNDER_EVALUATION'}">
                                <option value="EVALUATED">Complete Evaluation (UNDER_EVALUATION → EVALUATED)</option>
                            </c:if>
                            <c:if test="${tender.status == 'EVALUATED'}">
                                <option value="AWARDED">Award Contract (EVALUATED → AWARDED)</option>
                            </c:if>
                        </select>
                        <button type="submit" class="btn btn-primary">Apply Status Change</button>
                    </div>
                </form>
                <p class="status-note">Status changes follow the procurement lifecycle and cannot be reversed.</p>
            </div>
        </c:if>

        <!-- SHOW COMPLETED MESSAGE FOR AWARDED TENDERS -->
        <c:if test="${tender.status == 'AWARDED'}">
            <div class="alert alert-success">
                <strong>✓ Contract Awarded</strong>
                <p>This tender has been successfully awarded. The procurement cycle is complete.</p>
            </div>
        </c:if>

        <div class="back-link">
            <a href="${pageContext.request.contextPath}/officer/tender?action=list" class="btn btn-secondary">← Back to Tender List</a>
        </div>
    </c:if>

</main>

<script>
function confirmStatusChange(reference, currentStatus, newStatus) {
    if (!newStatus) return false;
    
    var messages = {
        'DRAFT_OPEN': 'Are you sure you want to publish tender ' + reference + '? It will become OPEN for bidding.',
        'OPEN_CLOSED': 'Are you sure you want to close bidding for tender ' + reference + '? No more bids will be accepted.',
        'CLOSED_UNDER_EVALUATION': 'Are you sure you want to start evaluation for tender ' + reference + '?',
        'UNDER_EVALUATION_EVALUATED': 'Are you sure you want to mark tender ' + reference + ' as EVALUATED?',
        'EVALUATED_AWARDED': 'Are you sure you want to award tender ' + reference + '?'
    };
    
    var key = currentStatus + '_' + newStatus;
    var message = messages[key] || 'Change status of tender ' + reference + ' from ' + currentStatus + ' to ' + newStatus + '?';
    
    return confirm(message);
}
</script>

<style>
/* Statistics Container */
.stats-container {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
}

.stat-item {
    background: white;
    border-radius: 12px;
    padding: 0.75rem 1.5rem;
    text-align: center;
    min-width: 90px;
    border: 1px solid #e8f5e9;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.stat-number {
    display: block;
    font-size: 1.5rem;
    font-weight: 700;
    color: #0d6e2e;
}

.stat-name {
    font-size: 0.7rem;
    color: #6b7280;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.stat-item.draft .stat-number { color: #6b7280; }
.stat-item.open .stat-number { color: #0d6e2e; }
.stat-item.closed .stat-number { color: #d97706; }
.stat-item.evaluation .stat-number { color: #0891b2; }
.stat-item.awarded .stat-number { color: #059669; }

/* Filter Section */
.filter-section {
    background: white;
    border-radius: 12px;
    padding: 1.25rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e8f5e9;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.search-row {
    margin-bottom: 1rem;
}

.search-input-wrapper {
    display: flex;
    gap: 0.5rem;
}

.search-field {
    flex: 1;
    padding: 0.7rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 0.9rem;
}

.search-field:focus {
    outline: none;
    border-color: #0d6e2e;
    box-shadow: 0 0 0 3px rgba(13,110,46,0.1);
}

.filter-row {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
    align-items: flex-end;
}

.filter-field {
    flex: 1;
    min-width: 140px;
}

.filter-field label {
    display: block;
    font-size: 0.7rem;
    font-weight: 600;
    color: #6b7280;
    margin-bottom: 0.25rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.filter-select {
    width: 100%;
    padding: 0.6rem 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: white;
    font-size: 0.85rem;
}

.filter-actions {
    flex: 0.5;
    min-width: auto;
}

.filter-buttons {
    display: flex;
    gap: 0.5rem;
}

.results-header {
    margin-bottom: 1rem;
}

.results-count {
    font-size: 0.85rem;
    color: #6b7280;
    font-weight: 500;
}

.table-wrapper {
    overflow-x: auto;
    background: white;
    border-radius: 12px;
    border: 1px solid #e8f5e9;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
}

.data-table th {
    background: #f0fdf4;
    padding: 1rem;
    text-align: left;
    font-weight: 600;
    color: #0d6e2e;
    border-bottom: 2px solid #e8f5e9;
}

.data-table td {
    padding: 0.85rem 1rem;
    border-bottom: 1px solid #e8f5e9;
    vertical-align: middle;
}

.data-table tr:hover td {
    background: #f8faf8;
}

.ref-cell strong {
    color: #0d6e2e;
}

.value-cell {
    font-weight: 600;
    color: #0a5524;
}

.badge {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    text-transform: uppercase;
}

.badge.status-draft { background: #f3f4f6; color: #4b5563; }
.badge.status-open { background: #d1fae5; color: #065f46; }
.badge.status-closed { background: #fed7aa; color: #92400e; }
.badge.status-under_evaluation { background: #fef3c7; color: #92400e; }
.badge.status-evaluated { background: #cffafe; color: #155e75; }
.badge.status-awarded { background: #d1fae5; color: #065f46; }

.actions-cell {
    white-space: nowrap;
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
    align-items: center;
}

.btn-view, .btn-edit, .btn-award {
    padding: 0.25rem 0.6rem;
    font-size: 0.7rem;
    border-radius: 6px;
    text-decoration: none;
    display: inline-block;
}

.btn-view {
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
}

.btn-view:hover {
    background: #0d6e2e;
    color: white;
}

.btn-edit {
    background: #d97706;
    color: white;
    border: none;
}

.btn-edit:hover {
    background: #b45f06;
}

.btn-award {
    background: #059669;
    color: white;
    border: none;
}

.btn-award:hover {
    background: #047857;
}

.status-select {
    padding: 0.25rem 0.5rem;
    font-size: 0.7rem;
    border-radius: 6px;
    border: 1px solid #ddd;
    background: white;
    cursor: pointer;
}

.status-select:hover {
    border-color: #0d6e2e;
}

@media (max-width: 768px) {
    .filter-row {
        flex-direction: column;
    }
    .filter-field {
        width: 100%;
    }
    .filter-actions {
        width: 100%;
    }
    .filter-buttons {
        width: 100%;
    }
    .filter-buttons .btn {
        flex: 1;
    }
    .actions-cell {
        flex-direction: column;
    }
    .data-table th, .data-table td {
        padding: 0.5rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>