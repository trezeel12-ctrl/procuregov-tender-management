<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<main class="content-wrapper">

    <!-- HEADER SECTION -->
    <div class="dashboard-header">
        <div class="header-title">
            <h2>Procurement Officer Dashboard</h2>
            <p class="subtitle">
                Welcome back, <c:out value="${sessionScope.user.fullName}" default="Officer"/>
            </p>
            <p class="date-info">
                <fmt:formatDate value="<%= new java.util.Date() %>" pattern="EEEE, dd MMMM yyyy"/>
            </p>
        </div>
        <div class="header-actions">
            <a href="${pageContext.request.contextPath}/officer/tender?action=create" class="btn btn-primary btn-lg">
                <span class="btn-icon">+</span> Create New Tender
            </a>
        </div>
    </div>

    <!-- STATISTICS CARDS - MAIN KPI -->
    <div class="kpi-grid">
        <div class="kpi-card">
            <div class="kpi-icon total">📋</div>
            <div class="kpi-content">
                <h3>Total Tenders</h3>
                <div class="kpi-value">${totalTenders}</div>
                <p>All procurement records</p>
            </div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon open">🟢</div>
            <div class="kpi-content">
                <h3>Open for Bids</h3>
                <div class="kpi-value">${openCount}</div>
                <p>Active tenders accepting submissions</p>
            </div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon eval">📊</div>
            <div class="kpi-content">
                <h3>Under Evaluation</h3>
                <div class="kpi-value">${underEvalCount}</div>
                <p>Tenders being reviewed</p>
            </div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon awarded">🏆</div>
            <div class="kpi-content">
                <h3>Awarded Contracts</h3>
                <div class="kpi-value">${awardedCount}</div>
                <p>Successfully finalized</p>
            </div>
        </div>
    </div>

    <!-- SECONDARY METRICS -->
    <div class="metrics-panel">
        <div class="metric-item">
            <span class="metric-label">Draft</span>
            <span class="metric-value">${draftCount}</span>
        </div>
        <div class="metric-item">
            <span class="metric-label">Closed</span>
            <span class="metric-value">${closedCount}</span>
        </div>
        <div class="metric-item">
            <span class="metric-label">Evaluated</span>
            <span class="metric-value">${evaluatedCount}</span>
        </div>
        <div class="metric-item">
            <span class="metric-label">Total Bids</span>
            <span class="metric-value">${totalBidsCount}</span>
        </div>
        <div class="metric-item">
            <span class="metric-label">Active Evaluators</span>
            <span class="metric-value">${totalEvaluators}</span>
        </div>
        <div class="metric-item">
            <span class="metric-label">Completion Rate</span>
            <span class="metric-value">${completionRate}%</span>
        </div>
    </div>

    <!-- ALERT MESSAGES -->
    <c:if test="${not empty param.success}">
        <div class="alert alert-success">
            <span class="alert-icon">✓</span> Operation completed successfully.
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-error">
            <span class="alert-icon">⚠</span> Something went wrong. Please try again.
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-error">
            <span class="alert-icon">⚠</span> ${errorMessage}
        </div>
    </c:if>
    <!-- Email Status Monitor (Optional - for debugging) -->
    <c:if test="${param.success == 'award_finalized'}">
        <div id="emailStatusMonitor" style="position: fixed; bottom: 20px; right: 20px; background: #0d6e2e; color: white; padding: 12px 20px; border-radius: 8px; font-size: 13px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 1000; min-width: 250px;">
            <div style="display: flex; align-items: center; gap: 10px;">
                <span style="font-size: 18px;">📧</span>
                <div style="flex: 1;">
                    <strong>Email notifications</strong><br>
                    <span style="font-size: 11px; opacity: 0.9;">Being sent in background...</span>
                </div>
                <div class="spinner" style="width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.8s linear infinite;"></div>
            </div>
            <div style="font-size: 10px; margin-top: 8px; opacity: 0.8; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 6px;">
                You can continue working
            </div>
        </div>

        <style>
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
        </style>

        <script>
            // Auto-hide the email status after 8 seconds
            setTimeout(function() {
                var monitor = document.getElementById('emailStatusMonitor');
                if (monitor) {
                    monitor.style.transition = 'opacity 1s';
                    monitor.style.opacity = '0';
                    setTimeout(function() {
                        if (monitor) monitor.remove();
                    }, 1000);
                }
            }, 8000);
        </script>
    </c:if>

    <!-- QUICK ACTIONS SECTION -->
    <div class="actions-section">
        <h3>Quick Actions</h3>
        <div class="actions-grid">
            <a href="${pageContext.request.contextPath}/officer/tender?action=create" class="action-card">
                <div class="action-icon green">📄</div>
                <div class="action-text">
                    <strong>Create Tender</strong>
                    <span>Start a new procurement process</span>
                </div>
                <div class="action-arrow">→</div>
            </a>
            <a href="${pageContext.request.contextPath}/officer/tender?action=list&status=OPEN" class="action-card">
                <div class="action-icon blue">🟢</div>
                <div class="action-text">
                    <strong>Open Tenders</strong>
                    <span>View tenders accepting bids</span>
                </div>
                <div class="action-arrow">→</div>
            </a>
            <a href="${pageContext.request.contextPath}/officer/tender?action=list&status=UNDER_EVALUATION" class="action-card">
                <div class="action-icon orange">📊</div>
                <div class="action-text">
                    <strong>Pending Evaluation</strong>
                    <span>Track evaluation progress</span>
                </div>
                <div class="action-arrow">→</div>
            </a>
            <a href="${pageContext.request.contextPath}/officer/award" class="action-card">
                <div class="action-icon purple">🏆</div>
                <div class="action-text">
                    <strong>Award Management</strong>
                    <span>Review and finalize awards</span>
                </div>
                <div class="action-arrow">→</div>
            </a>
        </div>
    </div>

    <!-- INSIGHTS SECTION -->
    <div class="insights-section">
        <h3>Procurement Insights</h3>
        <div class="insights-grid">
            <div class="insight-card">
                <div class="insight-header">
                    <span class="insight-icon">💰</span>
                    <span class="insight-title">Total Procurement Value</span>
                </div>
                <div class="insight-amount">
                    M <fmt:formatNumber value="${totalEstimatedValue}" pattern="#,##0.00"/>
                </div>
                <p class="insight-note">Total estimated value across all tenders</p>
            </div>
            <div class="insight-card">
                <div class="insight-header">
                    <span class="insight-icon">📊</span>
                    <span class="insight-title">Average Bids per Tender</span>
                </div>
                <div class="insight-amount">
                    <c:choose>
                        <c:when test="${totalTenders > 0}">
                            <fmt:formatNumber value="${totalBidsCount / totalTenders}" pattern="#0.0"/>
                        </c:when>
                        <c:otherwise>0</c:otherwise>
                    </c:choose>
                </div>
                <p class="insight-note">Average number of bids received per tender</p>
            </div>
            <div class="insight-card">
                <div class="insight-header">
                    <span class="insight-icon">📈</span>
                    <span class="insight-title">Evaluation Progress</span>
                </div>
                <div class="progress-wrapper">
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" style="width: ${completionRate}%;"></div>
                    </div>
                    <div class="progress-stats">
                        <span class="progress-percent">${completionRate}% Complete</span>
                        <span class="progress-status">${awardedCount} of ${totalTenders} tenders awarded</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- RECENT ACTIVITY PREVIEW -->
    <div class="activity-section">
        <h3>Recent Procurement Activity</h3>
        <div class="activity-list">
            <div class="activity-row">
                <div class="activity-icon">📄</div>
                <div class="activity-details">
                    <strong>Tender Management</strong>
                    <p>Create, edit, and publish tenders for suppliers</p>
                </div>
            </div>
            <div class="activity-row">
                <div class="activity-icon">📊</div>
                <div class="activity-details">
                    <strong>Evaluation Oversight</strong>
                    <p>Monitor evaluator progress and review scores</p>
                </div>
            </div>
            <div class="activity-row">
                <div class="activity-icon">🏆</div>
                <div class="activity-details">
                    <strong>Contract Awarding</strong>
                    <p>Select winning bids and issue award notices</p>
                </div>
            </div>
        </div>
    </div>

</main>

<style>
/* Dashboard Header */
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
    font-weight: 600;
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

.btn-lg {
    padding: 0.75rem 1.5rem;
    font-size: 0.95rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.btn-icon {
    font-size: 1.2rem;
    font-weight: bold;
}

/* KPI Cards Grid */
.kpi-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.25rem;
    margin-bottom: 1.5rem;
}

.kpi-card {
    background: white;
    border-radius: 16px;
    padding: 1.25rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    border: 1px solid #e8f5e9;
    transition: transform 0.2s, box-shadow 0.2s;
}

.kpi-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(13,110,46,0.1);
}

.kpi-icon {
    font-size: 2.2rem;
    width: 55px;
    height: 55px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 14px;
}

.kpi-icon.total { background: #e8f5e9; }
.kpi-icon.open { background: #d1fae5; }
.kpi-icon.eval { background: #fef3c7; }
.kpi-icon.awarded { background: #d1fae5; }

.kpi-content {
    flex: 1;
}

.kpi-content h3 {
    font-size: 0.75rem;
    color: #6b7280;
    margin-bottom: 0.25rem;
    font-weight: 500;
    letter-spacing: 0.3px;
}

.kpi-value {
    font-size: 1.8rem;
    font-weight: 700;
    color: #0d6e2e;
    line-height: 1.2;
}

.kpi-content p {
    font-size: 0.7rem;
    color: #9ca3af;
    margin-top: 0.25rem;
}

/* Secondary Metrics Panel */
.metrics-panel {
    background: #f8faf8;
    border-radius: 16px;
    padding: 1rem 1.5rem;
    display: flex;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 1rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e8f5e9;
}

.metric-item {
    text-align: center;
    flex: 1;
    min-width: 80px;
}

.metric-label {
    display: block;
    font-size: 0.7rem;
    color: #6b7280;
    margin-bottom: 0.25rem;
}

.metric-value {
    display: block;
    font-size: 1.2rem;
    font-weight: 700;
    color: #0d6e2e;
}

/* Actions Section */
.actions-section {
    background: white;
    border-radius: 16px;
    padding: 1.25rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e8f5e9;
}

.actions-section h3 {
    margin-bottom: 1rem;
    color: #0d6e2e;
    font-size: 1rem;
}

.actions-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1rem;
}

.action-card {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.9rem;
    background: #f0fdf4;
    border-radius: 12px;
    text-decoration: none;
    transition: all 0.2s;
    border: 1px solid transparent;
}

.action-card:hover {
    background: #0d6e2e;
    transform: translateY(-2px);
    border-color: #0d6e2e;
}

.action-card:hover .action-text strong,
.action-card:hover .action-text span,
.action-card:hover .action-arrow {
    color: white;
}

.action-icon {
    font-size: 1.5rem;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
}

.action-icon.green { background: #d1fae5; }
.action-icon.blue { background: #dbeafe; }
.action-icon.orange { background: #fef3c7; }
.action-icon.purple { background: #ede9fe; }

.action-card:hover .action-icon {
    background: rgba(255,255,255,0.2);
}

.action-text {
    flex: 1;
}

.action-text strong {
    display: block;
    font-size: 0.85rem;
    color: #1f2937;
    margin-bottom: 0.2rem;
}

.action-text span {
    font-size: 0.7rem;
    color: #6b7280;
}

.action-arrow {
    font-size: 1.2rem;
    color: #0d6e2e;
    opacity: 0;
    transition: opacity 0.2s, transform 0.2s;
}

.action-card:hover .action-arrow {
    opacity: 1;
    transform: translateX(3px);
}

/* Insights Section */
.insights-section {
    background: white;
    border-radius: 16px;
    padding: 1.25rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e8f5e9;
}

.insights-section h3 {
    margin-bottom: 1rem;
    color: #0d6e2e;
    font-size: 1rem;
}

.insights-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.25rem;
}

.insight-card {
    background: #f8faf8;
    border-radius: 12px;
    padding: 1rem;
}

.insight-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.insight-icon {
    font-size: 1.2rem;
}

.insight-title {
    font-size: 0.75rem;
    color: #6b7280;
}

.insight-amount {
    font-size: 1.3rem;
    font-weight: 700;
    color: #0d6e2e;
    margin-bottom: 0.25rem;
}

.insight-note {
    font-size: 0.65rem;
    color: #9ca3af;
}

.progress-wrapper {
    margin-top: 0.5rem;
}

.progress-bar-container {
    background: #e8f5e9;
    border-radius: 10px;
    height: 8px;
    overflow: hidden;
}

.progress-bar-fill {
    background: #0d6e2e;
    border-radius: 10px;
    height: 100%;
    transition: width 0.5s ease;
}

.progress-stats {
    margin-top: 0.5rem;
    display: flex;
    justify-content: space-between;
    font-size: 0.7rem;
}

.progress-percent {
    font-weight: 600;
    color: #0d6e2e;
}

.progress-status {
    color: #6b7280;
}

/* Activity Section */
.activity-section {
    background: linear-gradient(135deg, #f0fdf4 0%, #e8f5e9 100%);
    border-radius: 16px;
    padding: 1.25rem;
}

.activity-section h3 {
    margin-bottom: 1rem;
    color: #0d6e2e;
    font-size: 1rem;
}

.activity-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.activity-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.75rem;
    background: white;
    border-radius: 12px;
}

.activity-icon {
    font-size: 1.5rem;
    width: 45px;
    height: 45px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0fdf4;
    border-radius: 10px;
}

.activity-details {
    flex: 1;
}

.activity-details strong {
    display: block;
    font-size: 0.85rem;
    color: #1f2937;
    margin-bottom: 0.2rem;
}

.activity-details p {
    font-size: 0.7rem;
    color: #6b7280;
    margin: 0;
}

/* Alerts */
.alert {
    padding: 0.85rem 1rem;
    border-radius: 12px;
    margin-bottom: 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
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

.alert-icon {
    font-weight: bold;
    font-size: 1rem;
}

/* Responsive */
@media (max-width: 1024px) {
    .kpi-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    
    .actions-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    
    .insights-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 640px) {
    .dashboard-header {
        flex-direction: column;
        text-align: center;
    }
    
    .kpi-grid {
        grid-template-columns: 1fr;
    }
    
    .metrics-panel {
        flex-wrap: wrap;
    }
    
    .actions-grid {
        grid-template-columns: 1fr;
    }
    
    .insights-grid {
        grid-template-columns: 1fr;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>