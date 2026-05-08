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
            <h2>Evaluation Committee Dashboard</h2>
            <p class="subtitle">Welcome, ${sessionScope.user.fullName}</p>
            <p class="date-info">
                <fmt:formatDate value="<%= new java.util.Date() %>" pattern="EEEE, dd MMMM yyyy"/>
            </p>
        </div>
        <div class="header-stats">
            <div class="header-badge">
                <span class="badge-label">Evaluator ID</span>
                <span class="badge-value">${evaluatorId}</span>
            </div>
        </div>
    </div>

    <!-- ALERT MESSAGES -->
    <c:if test="${not empty successMessage}">
        <div class="alert alert-success">${successMessage}</div>
    </c:if>
    <c:if test="${not empty infoMessage}">
        <div class="alert alert-info">${infoMessage}</div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">${errorMessage}</div>
    </c:if>

    <!-- STATISTICS CARDS -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-icon">🔒</div>
            <div>
                <h3>Closed Tenders</h3>
                <h2>${totalClosed}</h2>
                <p>Ready for evaluation</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">⚙️</div>
            <div>
                <h3>Under Evaluation</h3>
                <h2>${totalUnderEval}</h2>
                <p>In progress</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">✅</div>
            <div>
                <h3>Evaluated</h3>
                <h2>${totalEvaluated}</h2>
                <p>Completed</p>
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">📊</div>
            <div>
                <h3>My Progress</h3>
                <h2>${myCompletedCount}/${totalUnderEval}</h2>
                <p>Evaluations completed</p>
            </div>
        </div>
    </div>

    <!-- SECONDARY STATS -->
    <div class="secondary-stats">
        <div class="secondary-stat">
            <span class="stat-label">Total Tenders</span>
            <span class="stat-number">${totalTenders}</span>
        </div>
        <div class="secondary-stat">
            <span class="stat-label">Total Value</span>
            <span class="stat-number">M <fmt:formatNumber value="${totalValue}" pattern="#,##0.00"/></span>
        </div>
        <div class="secondary-stat">
            <span class="stat-label">Evaluators</span>
            <span class="stat-number">${totalEvaluators}</span>
        </div>
        <div class="secondary-stat">
            <span class="stat-label">Progress</span>
            <div class="progress-small">
                <div class="progress-bar-small" style="width: ${completionPercentage}%;"></div>
                <span class="stat-number">${completionPercentage}%</span>
            </div>
        </div>
    </div>

    <!-- SECTION 1: CLOSED TENDERS -->
    <div class="card">
        <div class="card-header">
            <h3>Closed Tenders - Ready for Evaluation</h3>
            <p class="card-subtitle">Tenders that have passed the deadline and are awaiting evaluation</p>
        </div>
        
        <c:choose>
            <c:when test="${empty closedTenders}">
                <div class="empty-state">No closed tenders available.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Value (M)</th>
                                <th>Closing Date</th>
                                <th>Action</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${closedTenders}">
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="date-cell">${t.closingDateTime.toString().substring(0, 10)}</td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/evaluation?tenderId=${t.tenderId}" class="btn-start">Start Evaluation</a>
                                     </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- SECTION 2: UNDER_EVALUATION TENDERS -->
    <div class="card">
        <div class="card-header">
            <h3>Under Evaluation - In Progress</h3>
            <p class="card-subtitle">Tenders currently being evaluated by committee members</p>
        </div>
        
        <c:choose>
            <c:when test="${empty underEvalTenders}">
                <div class="empty-state">No tenders currently under evaluation.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Value (M)</th>
                                <th>My Status</th>
                                <th>Progress</th>
                                <th>Action</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${underEvalTenders}">
                                <c:set var="myCompleted" value="${evaluatorStatus[t.tenderId]}" />
                                <c:set var="completedCount" value="${completedCountMap[t.tenderId]}" />
                                <c:set var="totalEval" value="${totalEvaluators}" />
                                <c:set var="percent" value="${(completedCount / totalEval) * 100}" />
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="status-cell">
                                        <c:choose>
                                            <c:when test="${myCompleted}">
                                                <span class="badge-success">You have evaluated</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-pending">Pending your evaluation</span>
                                            </c:otherwise>
                                        </c:choose>
                                     </td>
                                    <td class="progress-cell">
                                        <div class="progress-wrapper">
                                            <div class="progress-container">
                                                <div class="progress-bar" style="width: ${percent}%;"></div>
                                            </div>
                                            <div class="progress-stats">
                                                <span class="completed-count">${completedCount}</span>
                                                <span class="total-count">/${totalEval} evaluators completed</span>
                                            </div>
                                        </div>
                                     </td>
                                    <td class="action-cell">
                                        <c:choose>
                                            <c:when test="${myCompleted}">
                                                <span class="badge-completed">Evaluation Complete</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/evaluation?tenderId=${t.tenderId}" class="btn-continue">Continue Evaluation</a>
                                            </c:otherwise>
                                        </c:choose>
                                     </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- SECTION 3: EVALUATED TENDERS -->
    <div class="card">
        <div class="card-header">
            <h3>Evaluated Tenders - Complete</h3>
            <p class="card-subtitle">Evaluation complete, ready for Procurement Officer to award</p>
        </div>
        
        <c:choose>
            <c:when test="${empty evaluatedTenders}">
                <div class="empty-state">No evaluated tenders yet.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Value (M)</th>
                                <th>Status</th>
                                <th>Message</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${evaluatedTenders}">
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="status-cell"><span class="badge-evaluated">EVALUATED</span></td>
                                    <td class="message-cell">Ready for Procurement Officer to award</td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- GUIDELINES CARD -->
    <div class="guidelines-card">
        <h3>Evaluation Guidelines</h3>
        <div class="guidelines-grid">
            <div class="guideline">
                <span class="icon">💰</span>
                <div>
                    <strong>Price Score (40% weight)</strong>
                    <p class="formula">(Lowest Bid / This Bid) × 100</p>
                    <p class="example">Automatically calculated</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">📋</span>
                <div>
                    <strong>Technical Score (35% weight)</strong>
                    <p>Enter 0-100 based on compliance</p>
                    <p class="example">Example: Score 70 → Weighted = 24.5</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">⏱️</span>
                <div>
                    <strong>Timeline Score (25% weight)</strong>
                    <p class="formula">(Shortest Timeline / This Timeline) × 100</p>
                    <p class="example">Automatically calculated</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">🔒</span>
                <div>
                    <strong>Confidentiality</strong>
                    <p>You cannot see other evaluators' scores until you submit</p>
                </div>
            </div>
        </div>
    </div>

</main>

<style>
/* Header */
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

.header-title p {
    color: white;
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

/* Statistics Cards */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
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

/* Secondary Stats */
.secondary-stats {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
}

.secondary-stat {
    background: #f0fdf4;
    border-radius: 12px;
    padding: 0.75rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
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

.progress-small {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.progress-bar-small {
    width: 60px;
    height: 6px;
    background: #0d6e2e;
    border-radius: 3px;
}

/* Cards */
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

/* Tables */
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

.value-cell {
    font-weight: 600;
    color: #0a5524;
}

.date-cell {
    font-family: monospace;
    font-size: 0.8rem;
}

.status-cell {
    text-align: center;
}

.progress-cell {
    min-width: 180px;
}

.action-cell {
    text-align: center;
    white-space: nowrap;
}

/* Badges */
.badge-success, .badge-pending, .badge-completed, .badge-evaluated {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
}

.badge-success { background: #d1fae5; color: #065f46; }
.badge-pending { background: #fef3c7; color: #92400e; }
.badge-completed { background: #059669; color: white; }
.badge-evaluated { background: #0891b2; color: white; }

/* Progress */
.progress-wrapper {
    min-width: 160px;
}

.progress-container {
    background: #e8f5e9;
    border-radius: 10px;
    height: 8px;
    overflow: hidden;
}

.progress-bar {
    background: #0d6e2e;
    border-radius: 10px;
    height: 8px;
    transition: width 0.3s ease;
}

.progress-stats {
    margin-top: 5px;
    font-size: 0.7rem;
}

.completed-count {
    font-weight: bold;
    color: #0d6e2e;
}

.total-count {
    color: #6b7280;
}

/* Buttons */
.btn-start, .btn-continue {
    background: #0d6e2e;
    color: white;
    padding: 6px 12px;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.8rem;
    display: inline-block;
    transition: background 0.2s;
}

.btn-start:hover, .btn-continue:hover {
    background: #0a5524;
}

/* Guidelines */
.guidelines-card {
    background: #f0fdf4;
    border-radius: 16px;
    padding: 1.25rem;
    margin-top: 0.5rem;
}

.guidelines-card h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: #0d6e2e;
}

.guidelines-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1rem;
}

.guideline {
    display: flex;
    gap: 0.75rem;
    align-items: flex-start;
}

.guideline .icon {
    font-size: 1.3rem;
}

.guideline strong {
    display: block;
    font-size: 0.8rem;
    color: #0d6e2e;
}

.formula, .example {
    font-size: 0.7rem;
    color: #6b7280;
    margin: 0.25rem 0 0;
}

/* Alerts */
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

.alert-info {
    background: #cffafe;
    color: #155e75;
    border-left: 4px solid #0891b2;
}

.alert-error {
    background: #fee2e2;
    color: #991b1b;
    border-left: 4px solid #dc2626;
}

.empty-state {
    text-align: center;
    padding: 2rem;
    color: #6b7280;
}

/* Responsive */
@media (max-width: 768px) {
    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    .guidelines-grid {
        grid-template-columns: 1fr;
    }
    .dashboard-header {
        flex-direction: column;
        text-align: center;
    }
    .data-table th, .data-table td {
        padding: 0.5rem;
        font-size: 0.7rem;
    }
    .progress-cell {
        min-width: 120px;
    }
    .action-cell {
        white-space: normal;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>