<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <div class="dashboard-header">
        <div>
            <h2>My Award Notices</h2>
            <p class="subtitle">View all awarded tenders you bid on</p>
        </div>
        <div>
            <span class="badge-info">Won: ${wonCount} | Not Won: ${notWonCount}</span>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty awardNotices}">
            <div class="empty-state">
                <p>No award notices available.</p>
                <p>You have not bid on any awarded tenders yet.</p>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Browse Open Tenders</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="award-notices-grid">
                <c:forEach var="notice" items="${awardNotices}">
                    <c:set var="t" value="${notice.tender}" />
                    <c:set var="b" value="${notice.bid}" />
                    <c:set var="a" value="${notice.award}" />
                    <c:set var="isWinner" value="${notice.isWinner}" />
                    
                    <div class="award-card ${isWinner ? 'winner-card' : 'not-winner-card'}">
                        <div class="award-card-header ${isWinner ? 'winner-header' : 'not-winner-header'}">
                            <div class="award-ref">${t.referenceNo}</div>
                            <div class="award-status">AWARDED</div>
                        </div>
                        <div class="award-card-body">
                            <h3 class="award-title">${t.title}</h3>
                            <div class="award-details">
                                <div class="detail-row">
                                    <span class="detail-label">Category:</span>
                                    <span class="detail-value">${t.category}</span>
                                </div>
                                <div class="detail-row">
                                    <span class="detail-label">Your Bid Amount:</span>
                                    <span class="detail-value">M <fmt:formatNumber value="${b.bidAmount}" pattern="#,##0.00"/></span>
                                </div>
                                
                                <c:if test="${isWinner}">
                                    <div class="detail-row">
                                        <span class="detail-label">Award Date:</span>
                                        <span class="detail-value">${a.awardDate}</span>
                                    </div>
                                    <div class="detail-row">
                                        <span class="detail-label">Awarded Value:</span>
                                        <span class="detail-value amount">M <fmt:formatNumber value="${a.awardedValue}" pattern="#,##0.00"/></span>
                                    </div>
                                </c:if>
                                
                                <c:if test="${not isWinner}">
                                    <div class="detail-row">
                                        <span class="detail-label">Outcome:</span>
                                        <span class="detail-value not-won">Contract awarded to another supplier</span>
                                    </div>
                                </c:if>
                            </div>
                            <div class="award-actions">
                                <c:if test="${isWinner}">
                                    <a href="${pageContext.request.contextPath}/supplier/award-notice?tenderId=${t.tenderId}" class="btn-view-award">
                                        View Full Award Notice
                                    </a>
                                </c:if>
                                <c:if test="${not isWinner}">
                                    <span class="badge-not-won">Not Selected</span>
                                </c:if>
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

.award-notices-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 1.5rem;
    margin-top: 0.5rem;
}

.award-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    border: 1px solid #e8f5e9;
    transition: transform 0.2s, box-shadow 0.2s;
}

.award-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(13,110,46,0.12);
}

.winner-card {
    border-left: 4px solid #0d6e2e;
}

.not-winner-card {
    border-left: 4px solid #9ca3af;
    opacity: 0.85;
}

.award-card-header {
    padding: 1rem 1.25rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: white;
}

.winner-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
}

.not-winner-header {
    background: #6b7280;
}

.award-ref {
    font-weight: 700;
    font-size: 0.9rem;
    letter-spacing: 0.5px;
}

.award-status {
    background: rgba(255,255,255,0.2);
    padding: 0.2rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
}

.award-card-body {
    padding: 1.25rem;
}

.award-title {
    margin: 0 0 1rem 0;
    font-size: 1rem;
    font-weight: 600;
    color: #1f2937;
    line-height: 1.4;
}

.award-details {
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

.detail-value.amount {
    color: #0d6e2e;
    font-weight: 700;
}

.detail-value.not-won {
    color: #6b7280;
    font-style: italic;
}

.award-actions {
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

.badge-not-won {
    background: #e5e7eb;
    color: #4b5563;
    padding: 0.4rem 0.8rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    display: inline-block;
}

.empty-state {
    text-align: center;
    padding: 3rem;
    background: white;
    border-radius: 16px;
    color: #6b7280;
}

.btn-primary {
    background: #0d6e2e;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    text-decoration: none;
    display: inline-block;
    margin-top: 1rem;
}

.btn-primary:hover {
    background: #0a5524;
}

@media (max-width: 768px) {
    .award-notices-grid {
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