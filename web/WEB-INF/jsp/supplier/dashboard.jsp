<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <!-- HEADER SECTION -->
    <div class="dashboard-header">
        <div class="header-title">
            <h2>Supplier Dashboard</h2>
            <p class="subtitle">Welcome back, ${sessionScope.user.fullName}</p>
            <p class="date-info">
                <fmt:formatDate value="<%= new java.util.Date() %>" pattern="EEEE, dd MMMM yyyy"/>
            </p>
        </div>
        <div class="header-stats">
            <div class="header-badge">
                <span class="badge-label">Registered Since</span>
                <span class="badge-value">
                    <c:choose>
                        <c:when test="${not empty sessionScope.user.createdAt}">
                            ${sessionScope.user.createdAt.toString().substring(0, 10)}
                        </c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>
    </div>

    <!-- ALERT MESSAGES -->
    <c:if test="${param.success == 'bid_submitted'}">
        <div class="alert alert-success">Your bid has been submitted successfully!</div>
    </c:if>
    <c:if test="${param.error == 'bid_failed'}">
        <div class="alert alert-error">Failed to submit bid. Please try again.</div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>

    <!-- STATISTICS CARDS -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">📋</div>
            <div>
                <h3>Total Bids</h3>
                <h2>${totalBids}</h2>
                <p>Bids submitted</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div>
                <h3>Pending Review</h3>
                <h2>${pendingReviewBids}</h2>
                <p>Bids under evaluation</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🏆</div>
            <div>
                <h3>Awarded</h3>
                <h2>${awardedBids}</h2>
                <p>Contracts won</p>
            </div>
        </div>
    </div>

    <!-- SECONDARY STATS -->
    <div class="secondary-stats">
        <div class="secondary-stat">
            <span class="stat-label">Total Bid Value</span>
            <span class="stat-number">M <fmt:formatNumber value="${totalBidAmount}" pattern="#,##0.00"/></span>
        </div>
        <div class="secondary-stat">
            <span class="stat-label">Success Rate</span>
            <span class="stat-number">
                <c:choose>
                    <c:when test="${totalBids > 0}">${successRate}%</c:when>
                    <c:otherwise>0%</c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <!-- OPEN TENDERS SECTION -->
    <div class="card">
        <div class="card-header">
            <h3>Open Tenders Available for Bidding</h3>
            <p class="card-subtitle">Browse and submit bids for tenders accepting submissions</p>
        </div>
        
        <c:choose>
            <c:when test="${empty tenders}">
                <div class="empty-state">
                    <p>No open tenders available at this time.</p>
                    <p>Please check back later for new opportunities.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table" id="openTendersTable">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Value (M)</th>
                                <th>Closing Date</th>
                                <th>Time Left</th>
                                <th>Action</th>
                              </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${tenders}">
                                <c:set var="closingDateTimeStr" value="${t.closingDateTime.toString().replace(' ', 'T')}" />
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="date-cell">
                                        ${t.closingDateTime.minusHours(2).toString().substring(0, 16).replace('T', ' ')}
                                    </td>
                                    <td class="deadline-cell countdown" data-deadline="${closingDateTimeStr}">
                                        <span class="loading">Calculating...</span>
                                    </td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/supplier/tender?id=${t.tenderId}" class="btn-view">View and Bid</a>
                                    </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- MY SUBMITTED BIDS SECTION -->
    <div class="card">
        <div class="card-header">
            <h3>My Submitted Bids</h3>
            <p class="card-subtitle">Track your bid submissions and current status</p>
        </div>
        
        <c:choose>
            <c:when test="${empty bidsWithWinnerInfo}">
                <div class="empty-state">
                    <p>You haven't submitted any bids yet.</p>
                    <p>Browse open tenders above to place your first bid.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Tender Reference</th>
                                <th>Bid Amount (M)</th>
                                <th>Timeline</th>
                                <th>Submitted Date</th>
                                <th>Current Status</th>
                                <th>Document</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="bidInfo" items="${bidsWithWinnerInfo}">
                                <c:set var="bid" value="${bidInfo.bid}" />
                                <c:set var="isWinner" value="${bidInfo.isWinner}" />
                                <c:set var="tenderStatus" value="${bidInfo.tenderStatus}" />
                                <tr>
                                    <td class="ref-cell">
                                        <strong>${bid.tenderReference}</strong>
                                        <br>
                                        <span class="tender-id">Tender ID: ${bid.tenderId}</span>
                                    </td>
                                    <td class="value-cell"><fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></td>
                                    <td class="timeline-cell">${bid.proposedTimelineDays} days</td>
                                    <td class="date-cell">
                                        ${bid.submittedAt.toString().substring(0, 10)}
                                        <br>
                                        <span class="time-small">${bid.submittedAt.toString().substring(11, 16)}</span>
                                    </td>
                                    <td class="status-cell">
                                        <c:choose>
                                            <c:when test="${tenderStatus == 'AWARDED'}">
                                                <c:choose>
                                                    <c:when test="${isWinner}">
                                                        <span class="badge status-awarded">Contract Awarded - You Won</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge status-awarded-other">Awarded to Another Supplier</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${tenderStatus == 'UNDER_EVALUATION'}">
                                                <span class="badge status-under_evaluation">Under Evaluation</span>
                                            </c:when>
                                            <c:when test="${tenderStatus == 'EVALUATED'}">
                                                <span class="badge status-evaluated">Evaluation Complete</span>
                                            </c:when>
                                            <c:when test="${tenderStatus == 'OPEN'}">
                                                <span class="badge status-open">Bidding Open</span>
                                            </c:when>
                                            <c:when test="${tenderStatus == 'CLOSED'}">
                                                <span class="badge status-closed">Closed</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge status-draft">${tenderStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="action-cell">
                                        <c:if test="${not empty bid.supportingDocPath}">
                                            <a href="${pageContext.request.contextPath}/download?path=${bid.supportingDocPath}" class="btn-download" target="_blank">View Document</a>
                                        </c:if>
                                        <c:if test="${empty bid.supportingDocPath}">
                                            <span class="no-doc">No document</span>
                                        </c:if>
                                    </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
                <div class="table-footer">
                    <div class="footer-stats">
                        <span class="stat-item">Total Bids: <strong>${fn:length(bidsWithWinnerInfo)}</strong></span>
                        <span class="stat-item">Total Value: <strong>M <fmt:formatNumber value="${totalBidAmount}" pattern="#,##0.00"/></strong></span>
                        <span class="stat-item">Won: <strong>${awardedBids}</strong></span>
                    </div>
                    <a href="${pageContext.request.contextPath}/supplier/bids" class="btn-secondary">View All Bids</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<script>
function updateCountdowns() {
    const countdownElements = document.querySelectorAll('.countdown');
    const now = new Date();
    
    countdownElements.forEach(element => {
        const deadlineStr = element.getAttribute('data-deadline');
        if (!deadlineStr) return;
        
        let deadline;
        if (deadlineStr.includes('T')) {
            deadline = new Date(deadlineStr + 'Z');
            deadline.setHours(deadline.getHours() - 4);
        } else {
            deadline = new Date(deadlineStr.replace(' ', 'T') + 'Z');
            deadline.setHours(deadline.getHours() - 4);
        }
        
        const diff = deadline - now;
        
        if (diff <= 0) {
            element.innerHTML = '<span class="deadline-expired">Closed</span>';
            return;
        }
        
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (86400000)) / 3600000);
        const minutes = Math.floor((diff % 3600000) / 60000);
        
        let html = '';
        
        if (days > 7) {
            html = '<span class="deadline-ok">' + days + ' days left</span>';
        } else if (days > 0) {
            html = '<span class="deadline-warning">' + days + ' days, ' + hours + ' hrs left</span>';
        } else if (hours > 0) {
            html = '<span class="deadline-today">' + hours + ' hours, ' + minutes + ' min left</span>';
        } else if (minutes > 0) {
            html = '<span class="deadline-today">' + minutes + ' minutes left</span>';
        } else {
            html = '<span class="deadline-today">Closing now</span>';
        }
        
        element.innerHTML = html;
    });
}

setInterval(updateCountdowns, 60000);
updateCountdowns();
</script>

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

.header-title h2 {
    margin: 0 0 0.25rem 0;
    color: white;
    font-size: 1.5rem;
}

.subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 0.9rem;
}

.date-info {
    font-size: 0.8rem;
    opacity: 0.7;
    margin-top: 0.25rem;
}

.header-badge {
    background: rgba(255,255,255,0.15);
    padding: 0.5rem 1rem;
    border-radius: 12px;
    text-align: center;
}

.badge-label {
    display: block;
    font-size: 0.7rem;
    opacity: 0.8;
}

.badge-value {
    display: block;
    font-size: 0.9rem;
    font-weight: 600;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.25rem;
    margin-bottom: 1rem;
}

.stat-card {
    background: white;
    border-radius: 16px;
    padding: 1.25rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    border: 1px solid #e8f5e9;
    transition: transform 0.2s;
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(13,110,46,0.1);
}

.stat-icon {
    font-size: 2.2rem;
}

.stat-card h3 {
    font-size: 0.75rem;
    color: #6b7280;
    margin-bottom: 0.25rem;
    font-weight: 500;
}

.stat-card h2 {
    font-size: 1.8rem;
    font-weight: 700;
    color: #0d6e2e;
    margin: 0;
    line-height: 1.2;
}

.stat-card p {
    font-size: 0.7rem;
    color: #9ca3af;
    margin-top: 0.25rem;
}

.secondary-stats {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.secondary-stat {
    background: #f0fdf4;
    border-radius: 12px;
    padding: 0.75rem 1.5rem;
    display: flex;
    gap: 0.5rem;
    align-items: baseline;
    flex-wrap: wrap;
}

.stat-label {
    font-size: 0.75rem;
    color: #6b7280;
}

.stat-number {
    font-size: 1rem;
    font-weight: 700;
    color: #0d6e2e;
}

.card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 1.5rem;
    overflow: hidden;
    border: 1px solid #e8f5e9;
}

.card-header {
    padding: 1rem 1.5rem;
    background: #f8faf8;
    border-bottom: 1px solid #e8f5e9;
}

.card-header h3 {
    margin: 0 0 0.25rem 0;
    color: #0d6e2e;
    font-size: 1.1rem;
}

.card-subtitle {
    margin: 0;
    font-size: 0.75rem;
    color: #6b7280;
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

.value-cell {
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

.badge {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-open { background: #d1fae5; color: #065f46; }
.status-closed { background: #fed7aa; color: #92400e; }
.status-under_evaluation { background: #fef3c7; color: #92400e; }
.status-evaluated { background: #cffafe; color: #155e75; }
.status-awarded { background: #d1fae5; color: #065f46; font-weight: 700; }
.status-awarded-other { background: #e5e7eb; color: #4b5563; font-weight: 600; }
.status-draft { background: #f3f4f6; color: #4b5563; }

.award-badge {
    margin-top: 0.5rem;
    padding: 0.25rem 0.5rem;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    font-size: 0.65rem;
    font-weight: 600;
}

.award-badge.winner {
    background: #d1fae5;
    color: #065f46;
}

.award-badge.not-winner {
    background: #f3f4f6;
    color: #6b7280;
}

.award-icon {
    font-size: 0.75rem;
}

.btn-award-notice {
    display: inline-block;
    margin-top: 0.5rem;
    padding: 0.25rem 0.6rem;
    background: #059669;
    color: white;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.7rem;
    font-weight: 500;
}

.btn-award-notice:hover {
    background: #047857;
}

.deadline-ok {
    color: #0d6e2e;
    font-weight: 600;
}

.deadline-warning {
    color: #d97706;
    font-weight: 600;
}

.deadline-today {
    color: #dc2626;
    font-weight: 700;
}

.deadline-expired {
    color: #6b7280;
    font-weight: 600;
}

.loading {
    color: #9ca3af;
    font-style: italic;
}

.btn-view, .btn-download {
    padding: 0.25rem 0.75rem;
    font-size: 0.7rem;
    border-radius: 6px;
    text-decoration: none;
    display: inline-block;
}

.btn-view {
    background: #0d6e2e;
    color: white;
    border: none;
}

.btn-view:hover {
    background: #0a5524;
}

.btn-download {
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
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
    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    .dashboard-header {
        flex-direction: column;
        text-align: center;
    }
    .secondary-stats {
        flex-direction: column;
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
    .btn-view, .btn-download {
        padding: 0.2rem 0.5rem;
        font-size: 0.65rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>