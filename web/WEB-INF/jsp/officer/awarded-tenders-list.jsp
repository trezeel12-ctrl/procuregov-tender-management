<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <div class="dashboard-header">
        <div>
            <h2>Awarded Tenders</h2>
            <p class="subtitle">View all successfully awarded contracts</p>
        </div>
        <div>
            <span class="badge-info">Total Awarded: ${fn:length(awardedDetails)}</span>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty awardedDetails}">
            <div class="empty-state">
                <p>No contracts have been awarded yet.</p>
                <p>When tenders are evaluated and awarded, they will appear here.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="awarded-grid">
                <c:forEach var="detail" items="${awardedDetails}">
                    <c:set var="t" value="${detail.tender}" />
                    <c:set var="a" value="${detail.award}" />
                    <c:set var="s" value="${detail.supplier}" />
                    
                    <div class="awarded-card">
                        <div class="awarded-card-header">
                            <div class="awarded-ref">${t.referenceNo}</div>
                            <div class="awarded-status">AWARDED</div>
                        </div>
                        <div class="awarded-card-body">
                            <h3 class="awarded-title">${t.title}</h3>
                            <div class="awarded-details">
                                <div class="detail-row">
                                    <span class="detail-label">Winner:</span>
                                    <span class="detail-value winner">${s.fullName}</span>
                                </div>
                                <div class="detail-row">
                                    <span class="detail-label">Awarded Value:</span>
                                    <span class="detail-value amount">M <fmt:formatNumber value="${a.awardedValue}" pattern="#,##0.00"/></span>
                                </div>
                                <div class="detail-row">
                                    <span class="detail-label">Original Estimate:</span>
                                    <span class="detail-value">M <fmt:formatNumber value="${t.estimatedValue}" pattern="#,##0.00"/></span>
                                </div>
                                <div class="detail-row">
                                    <span class="detail-label">Award Date:</span>
                                    <span class="detail-value">${a.awardDate}</span>
                                </div>
                                <div class="detail-row">
                                    <span class="detail-label">Category:</span>
                                    <span class="detail-value">${t.category}</span>
                                </div>
                            </div>
                            <div class="awarded-actions">
                                <a href="${pageContext.request.contextPath}/officer/award?action=viewAwarded&tenderId=${t.tenderId}" class="btn-view-award">
                                    View Full Award Details
                                </a>
                            </div>
                        </div>
                    </div>
                </c:forEach>
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

.badge-info {
    background: rgba(255,255,255,0.2);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
}

.awarded-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 1.5rem;
    margin-top: 0.5rem;
}

.awarded-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    border: 1px solid #e8f5e9;
    transition: transform 0.2s, box-shadow 0.2s;
}

.awarded-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(13,110,46,0.12);
}

.awarded-card-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
    padding: 1rem 1.25rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: white;
}

.awarded-ref {
    font-weight: 700;
    font-size: 0.9rem;
    letter-spacing: 0.5px;
}

.awarded-status {
    background: rgba(255,255,255,0.2);
    padding: 0.2rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
}

.awarded-card-body {
    padding: 1.25rem;
}

.awarded-title {
    margin: 0 0 1rem 0;
    font-size: 1rem;
    font-weight: 600;
    color: #1f2937;
    line-height: 1.4;
}

.awarded-details {
    margin-bottom: 1.25rem;
}

.detail-row {
    display: flex;
    justify-content: space-between;
    padding: 0.5rem 0;
    border-bottom: 1px solid #e8f5e9;
}

.detail-label {
    font-size: 0.75rem;
    color: #6b7280;
    font-weight: 500;
}

.detail-value {
    font-size: 0.8rem;
    color: #1f2937;
    font-weight: 500;
}

.detail-value.winner {
    color: #0d6e2e;
    font-weight: 600;
}

.detail-value.amount {
    color: #0d6e2e;
    font-weight: 700;
}

.awarded-actions {
    text-align: center;
    margin-top: 1rem;
}

.btn-view-award {
    background: #0d6e2e;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    text-decoration: none;
    font-size: 0.8rem;
    display: inline-block;
    transition: background 0.2s;
}

.btn-view-award:hover {
    background: #0a5524;
}

.empty-state {
    text-align: center;
    padding: 3rem;
    background: white;
    border-radius: 16px;
    color: #6b7280;
}

@media (max-width: 768px) {
    .awarded-grid {
        grid-template-columns: 1fr;
    }
    .dashboard-header {
        flex-direction: column;
        text-align: center;
    }
    .detail-row {
        flex-direction: column;
        gap: 0.25rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>