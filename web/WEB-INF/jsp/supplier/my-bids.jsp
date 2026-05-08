<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <div class="dashboard-header">
        <div>
            <h2>My Submitted Bids</h2>
            <p class="subtitle">Track all your bid submissions and their current status</p>
        </div>
        <div>
            <span class="badge-info">Total: ${fn:length(myBids)} bid(s)</span>
        </div>
    </div>

    <c:if test="${not empty param.success}">
        <div class="alert alert-success">Bid submitted successfully!</div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-error">Failed to submit bid. Please try again.</div>
    </c:if>

    <c:choose>
        <c:when test="${empty myBids}">
            <div class="empty-state">
                <p>You haven't submitted any bids yet.</p>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Browse Open Tenders</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="card">
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Tender Reference</th>
                                <th>Bid Amount (M)</th>
                                <th>Timeline (Days)</th>
                                <th>Submission Date</th>
                                <th>Current Status</th>
                                <th>Bid Document</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="bid" items="${myBids}">
                                <tr>
                                    <td class="ref-cell">
                                        <strong>${bid.tenderReference}</strong>
                                        <br>
                                        <span class="tender-id">Tender ID: ${bid.tenderId}</span>
                                     </td>
                                    <td class="amount-cell">
                                        <fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/>
                                     </td>
                                    <td class="timeline-cell">${bid.proposedTimelineDays} days</td>
                                    <td class="date-cell">
                                        ${bid.submittedAt.toString().substring(0, 10)}
                                        <br>
                                        <span class="time-small">${bid.submittedAt.toString().substring(11, 16)}</span>
                                     </td>
                                    <td class="status-cell">
                                        <c:choose>
                                            <c:when test="${bid.tenderStatus == 'AWARDED'}">
                                                <span class="badge status-awarded">🏆Contract Awarded</span>
                                            </c:when>
                                            <c:when test="${bid.tenderStatus == 'UNDER_EVALUATION'}">
                                                <span class="badge status-under_evaluation">Under Evaluation</span>
                                            </c:when>
                                            <c:when test="${bid.tenderStatus == 'EVALUATED'}">
                                                <span class="badge status-evaluated">Evaluation Complete</span>
                                            </c:when>
                                            <c:when test="${bid.tenderStatus == 'OPEN'}">
                                                <span class="badge status-open">Bidding Open</span>
                                            </c:when>
                                            <c:when test="${bid.tenderStatus == 'CLOSED'}">
                                                <span class="badge status-closed">Closed</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge status-draft">${bid.tenderStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                     </td>
                                    <td class="action-cell">
                                        <c:if test="${not empty bid.supportingDocPath}">
                                            <a href="${pageContext.request.contextPath}/download?path=${bid.supportingDocPath}" class="btn-download" target="_blank">📎 View Document</a>
                                        </c:if>
                                        <c:if test="${empty bid.supportingDocPath}">
                                            <span class="no-doc">No document</span>
                                        </c:if>
                                     </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="table-footer">
                    <div class="footer-stats">
                        <span class="stat-item">
                            <strong>Total Bids:</strong> ${fn:length(myBids)}
                        </span>
                        <span class="stat-item">
                            <strong>Total Value:</strong> M <fmt:formatNumber value="${totalBidAmount}" pattern="#,##0.00"/>
                        </span>
                        <span class="stat-item">
                            <strong>Awarded:</strong> ${awardedBids}
                        </span>
                    </div>
                    <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn-secondary">← Back to Dashboard</a>
                </div>
            </div>
        </c:otherwise>
    </c:choose>

</main>

<style>
.dashboard-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
    padding: 1.5rem 2rem;
    border-radius: 16px;
    margin-bottom: 1.5rem;
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.dashboard-header h2 {
    margin: 0;
    color: white;
    font-size: 1.5rem;
}

.dashboard-header p {
    color: white;
}

.subtitle {
    margin: 0.25rem 0 0;
    opacity: 0.9;
    font-size: 0.85rem;
}

.badge-info {
    background: rgba(255,255,255,0.2);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
}

.card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    overflow: hidden;
    border: 1px solid #e8f5e9;
}

.table-responsive {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
}

.data-table th {
    background: #f0fdf4;
    padding: 0.85rem 1rem;
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

.tender-id {
    font-size: 0.7rem;
    color: #6b7280;
}

.amount-cell {
    font-weight: 600;
    color: #0a5524;
}

.timeline-cell {
    text-align: center;
}

.date-cell {
    font-size: 0.85rem;
}

.time-small {
    font-size: 0.7rem;
    color: #6b7280;
}

.status-cell {
    min-width: 140px;
}

.badge {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-awarded { background: #d1fae5; color: #065f46; }
.status-under_evaluation { background: #fef3c7; color: #92400e; }
.status-evaluated { background: #cffafe; color: #155e75; }
.status-open { background: #dbeafe; color: #1e40af; }
.status-closed { background: #fee2e2; color: #991b1b; }

.btn-download {
    padding: 0.25rem 0.75rem;
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.7rem;
    display: inline-block;
}

.btn-download:hover {
    background: #0d6e2e;
    color: white;
}

.no-doc {
    color: #9ca3af;
    font-size: 0.7rem;
}

.table-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    background: #f8faf8;
    border-top: 1px solid #e8f5e9;
    flex-wrap: wrap;
    gap: 1rem;
}

.footer-stats {
    display: flex;
    gap: 1.5rem;
}

.stat-item {
    font-size: 0.85rem;
    color: #4b5563;
}

.stat-item strong {
    color: #0d6e2e;
}

.btn-secondary {
    background: #6c757d;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    text-decoration: none;
    font-size: 0.85rem;
}

.btn-secondary:hover {
    background: #5a6268;
}

.alert {
    padding: 0.75rem 1rem;
    border-radius: 12px;
    margin-bottom: 1rem;
}

.alert-success {
    background: #d1fae5;
    color: #065f46;
    border-left: 4px solid #059669;
}

.alert-error {
    background: #fee2e2;
    color: #991b1b;
    border-left: 4px solid #dc2626;
}

.empty-state {
    text-align: center;
    padding: 3rem;
    color: #6b7280;
}

@media (max-width: 768px) {
    .dashboard-header {
        flex-direction: column;
        text-align: center;
    }
    .table-footer {
        flex-direction: column;
        text-align: center;
    }
    .footer-stats {
        flex-direction: column;
        gap: 0.5rem;
    }
    .data-table th, .data-table td {
        padding: 0.5rem;
        font-size: 0.7rem;
    }
    .btn-download {
        padding: 0.2rem 0.5rem;
        font-size: 0.65rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>