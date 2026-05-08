<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<main class="content-wrapper">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    
    <div class="dashboard-header">
        <div>
            <h2>🏆 Ranked Bid Leaderboard</h2>
            <p class="subtitle">Tender ID: ${tenderId}</p>
        </div>
    </div>

    <div class="card">
        <h3>📊 Final Scores (Sorted by Weighted Score Descending)</h3>
        
        <c:choose>
            <c:when test="${empty leaderboard}">
                <div class="empty-state">
                    <p>No bids have been evaluated yet.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="data-table leaderboard-table">
                        <thead>
                            <tr>
                                <th>Rank</th>
                                <th>Supplier</th>
                                <th>Bid Amount (M)</th>
                                <th>Timeline (Days)</th>
                                <th>Price Score (40%)</th>
                                <th>Technical Score (35%)</th>
                                <th>Timeline Score (25%)</th>
                                <th>Final Score</th>
                                <th>Evaluators</th>
                                <th>Action</th>
                             </tr
                        </thead>
                        <tbody>
                            <c:forEach var="entry" items="${leaderboard}">
                                <tr>
                                    <td class="rank-cell">
                                        <c:choose>
                                            <c:when test="${entry.rank == 1}">🥇 ${entry.rank}</c:when>
                                            <c:when test="${entry.rank == 2}">🥈 ${entry.rank}</c:when>
                                            <c:when test="${entry.rank == 3}">🥉 ${entry.rank}</c:when>
                                            <c:otherwise>${entry.rank}</c:otherwise>
                                        </c:choose>
                                     </td
                                    <td><strong>${entry.supplierName}</strong></td
                                    <td><fmt:formatNumber value="${entry.bidAmount}" pattern="#,##0.00"/></td
                                    <td>${entry.timelineDays} days</td
                                    <td class="score-cell"><fmt:formatNumber value="${entry.priceScore}" pattern="#0.00"/>%</td
                                    <td class="score-cell"><fmt:formatNumber value="${entry.technicalScore}" pattern="#0.00"/>%</td
                                    <td class="score-cell"><fmt:formatNumber value="${entry.timelineScore}" pattern="#0.00"/>%</td
                                    <td class="final-score-cell">
                                        <strong><fmt:formatNumber value="${entry.finalScore}" pattern="#0.00"/>%</strong>
                                     </td
                                    <td>${entry.evaluatorCount} evaluator(s)</td
                                    <td>
                                        <c:if test="${entry.rank == 1}">
                                            <a href="${pageContext.request.contextPath}/officer/award?tenderId=${tenderId}&bidId=${entry.bidId}" 
                                               class="btn-award">
                                                🏆 Award Contract
                                            </a>
                                        </c:if>
                                     </td
                                 </tr
                            </c:forEach>
                        </tbody>
                    </table
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    
    <div class="back-button">
        <a href="${pageContext.request.contextPath}/officer/dashboard" class="btn btn-secondary">Back to Dashboard</a>
    </div>
</main>

<style>
    .rank-cell {
        font-size: 1.2rem;
        font-weight: bold;
        text-align: center;
    }
    
    .score-cell {
        text-align: center;
        background: #f8f9fa;
    }
    
    .final-score-cell {
        text-align: center;
        background: #d4edda;
        font-size: 1.1rem;
    }
    
    .btn-award {
        background: #28a745;
        color: white;
        padding: 6px 12px;
        border-radius: 6px;
        text-decoration: none;
        font-size: 0.8rem;
        display: inline-block;
    }
    
    .btn-award:hover {
        background: #218838;
    }
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>