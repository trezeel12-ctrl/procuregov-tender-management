<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <div class="dashboard-header">
        <div>
            <h2>Contract Award Management</h2>
            <p class="subtitle">Review evaluated tenders and award contracts</p>
        </div>
    </div>

    <!-- EVALUATED TENDERS (Ready for Award) -->
    <div class="card">
        <div class="card-header">
            <h3>📋 Tenders Ready for Award</h3>
            <p class="card-subtitle">These tenders have completed evaluation and are awaiting contract award</p>
        </div>
        
        <c:choose>
            <c:when test="${empty evaluatedTenders}">
                <div class="empty-state">
                    <p>No evaluated tenders ready for award at this time.</p>
                    <p>When evaluators complete their assessments, tenders will appear here.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Estimated Value (M)</th>
                                <th>Closing Date</th>
                                <th>Status</th>
                                <th>Action</th>
                             </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${evaluatedTenders}">
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="date-cell">${t.closingDateTime.toString().substring(0, 10)}</td>
                                    <td class="status-cell">
                                        <span class="badge status-evaluated">EVALUATED</span>
                                    </td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/officer/award?tenderId=${t.tenderId}" class="btn-award">Award Contract</a>
                                    </td>
                                 </tr>
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- AWARDED TENDERS (Already Awarded) -->
    <!-- AWARDED TENDERS (Already Awarded) -->
    <div class="card">
        <div class="card-header">
            <h3>🏆 Awarded Contracts</h3>
            <p class="card-subtitle">Contracts that have been finalized and awarded</p>
        </div>

        <c:choose>
            <c:when test="${empty awardedDetails}">
                <div class="empty-state">
                    <p>No contracts have been awarded yet.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Estimated Value (M)</th>
                                <th>Winner</th>
                                <th>Award Date</th>
                                <th>Awarded Value</th>
                                <th>Action</th>
                             </tr
                        </thead>
                        <tbody>
                            <c:forEach var="detail" items="${awardedDetails}">
                                <c:set var="t" value="${detail.tender}" />
                                <c:set var="a" value="${detail.award}" />
                                <c:set var="s" value="${detail.supplier}" />
                                <tr>
                                    <td class="ref-cell"><strong>${t.referenceNo}</strong></td>
                                    <td class="title-cell">${t.title}</td>
                                    <td class="category-cell">${t.category}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></td>
                                    <td class="supplier-cell">${s.fullName}</td>
                                    <td class="date-cell">${a.awardDate}</td>
                                    <td class="value-cell"><fmt:formatNumber value="${a.awardedValue}" pattern="#,##0.00"/></td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/officer/award?action=viewAwarded&tenderId=${t.tenderId}" class="btn-view-award">
                                            View Award Details
                                        </a>
                                    </td>
                                 </tr
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="info-note">
        <p><strong>Note:</strong> Only tenders with status "EVALUATED" can be awarded. Once awarded, the status changes to "AWARDED" and all bidding suppliers are notified.</p>
    </div>

</main>

<style>
.dashboard-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
    padding: 1.5rem 2rem;
    border-radius: 16px;
    margin-bottom: 1.5rem;
    color: white;
}

.dashboard-header h2 {
    margin: 0 0 0.25rem 0;
    color: white;
    font-size: 1.5rem;
}

.dashboard-header p {
    color: white;
}

.dashboard-header .subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 0.85rem;
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

.value-cell {
    font-weight: 600;
    color: #0a5524;
}

.date-cell {
    font-family: monospace;
    font-size: 0.8rem;
}

.badge {
    display: inline-block;
    padding: 0.25rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-evaluated {
    background: #cffafe;
    color: #155e75;
}

.status-awarded {
    background: #d1fae5;
    color: #065f46;
}

.btn-award {
    background: #0d6e2e;
    color: white;
    padding: 0.4rem 0.8rem;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.75rem;
    display: inline-block;
}

.btn-award:hover {
    background: #0a5524;
}

.empty-state {
    text-align: center;
    padding: 2rem;
    color: #6b7280;
}

.info-note {
    background: #fef3c7;
    border-left: 4px solid #d97706;
    padding: 0.75rem 1rem;
    border-radius: 8px;
    font-size: 0.8rem;
    color: #92400e;
}

.info-note p {
    margin: 0;
}

@media (max-width: 768px) {
    .data-table th, .data-table td {
        padding: 0.5rem;
        font-size: 0.7rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>