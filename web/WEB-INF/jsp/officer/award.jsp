<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <!-- AWARD HEADER -->
    <div class="award-header">
        <div>
            <h2>Tender Award & Contract Allocation</h2>
            <p class="subtitle">Reference: ${tender.referenceNo} | Status: <span class="status-tag evaluated">${tender.status}</span></p>
        </div>
        <div class="award-date">
            <span class="date-label">Award Date</span>
            <span class="date-value"><fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd MMMM yyyy"/></span>
        </div>
    </div>

    <c:if test="${not empty param.error}">
        <div class="alert alert-error">Failed to finalize award. Please try again.</div>
    </c:if>
    <c:if test="${not empty param.success}">
        <div class="alert alert-success">Award finalized successfully!</div>
    </c:if>

    <!-- LEADERBOARD SECTION -->
    <div class="leaderboard-card">
        <div class="card-header">
            <h3>Final Ranked Leaderboard</h3>
            <p class="card-subtitle">Evaluated bids sorted by weighted score (highest to lowest)</p>
        </div>
        <div class="table-container">
            <table class="leaderboard-table">
                <thead>
                    <tr>
                        <th width="8%">Rank</th>
                        <th width="25%">Supplier</th>
                        <th width="15%">Bid Amount (M)</th>
                        <th width="12%">Final Score</th>
                        <th width="10%">Price (40%)</th>
                        <th width="10%">Technical (35%)</th>
                        <th width="10%">Timeline (25%)</th>
                     </tr>
                </thead>
                <tbody>
                    <c:forEach var="entry" items="${leaderboard}" varStatus="loop">
                        <c:set var="isWinner" value="${loop.index == 0}"/>
                        <tr>
                            <td class="rank-cell ${isWinner ? 'winner-rank' : ''}">
                                <c:choose>
                                    <c:when test="${loop.index == 0}"><span class="rank-icon">🥇</span> 1</c:when>
                                    <c:when test="${loop.index == 1}"><span class="rank-icon">🥈</span> 2</c:when>
                                    <c:when test="${loop.index == 2}"><span class="rank-icon">🥉</span> 3</c:when>
                                    <c:otherwise>${loop.index + 1}</c:otherwise>
                                </c:choose>
                            </td>
                            <td class="supplier-cell ${isWinner ? 'winner-supplier' : ''}">
                                <strong>${entry.supplierName}</strong>
                                <c:if test="${isWinner}"><span class="winner-badge">Recommended Winner</span></c:if>
                            </td>
                            <td class="amount-cell"><fmt:formatNumber value="${entry.bidAmount}" pattern="#,##0.00"/></td>
                            <td class="score-highlight"><fmt:formatNumber value="${entry.finalScore}" pattern="#0.00"/>%</td>
                            <td class="score-cell"><fmt:formatNumber value="${entry.priceScore}" pattern="#0.00"/>%</td>
                            <td class="score-cell"><fmt:formatNumber value="${entry.technicalScore}" pattern="#0.00"/>%</td>
                            <td class="score-cell"><fmt:formatNumber value="${entry.timelineScore}" pattern="#0.00"/>%</td>
                         </tr>
                    </c:forEach>
                </tbody>
            </table
        </div>
        <div class="leaderboard-footer">
            <span class="total-bids">Total Bids Evaluated: <strong>${fn:length(leaderboard)}</strong></span>
        </div>
    </div>

    <!-- AWARD FORM -->
    <div class="award-form-card">
        <div class="card-header">
            <h3>Award Contract Details</h3>
            <p class="card-subtitle">Select the winning supplier and enter contract details</p>
        </div>
        
        <form action="${pageContext.request.contextPath}/officer/award" method="post" class="award-form" id="awardForm">
            <div class="form-grid">
                <div class="form-group full-width">
                    <label for="winningBidId">Select Winning Bid <span class="required">*</span></label>
                    <select name="winningBidId" id="winningBidId" required class="form-control">
                        <option value="">-- Select Winning Supplier --</option>
                        <c:forEach var="entry" items="${leaderboard}" varStatus="loop">
                            <option value="${entry.bidId}" data-amount="${entry.bidAmount}" ${loop.index == 0 ? 'selected' : ''}>
                                ${entry.supplierName} - Score: ${entry.finalScore}% - Bid: M <fmt:formatNumber value="${entry.bidAmount}" pattern="#,##0.00"/>
                            </option>
                        </c:forEach>
                    </select>
                    <small>Select the supplier who will be awarded the contract</small>
                </div>
                
                <div class="form-row">
                    <div class="form-group half">
                        <label for="awardedValue">Awarded Contract Value (Maloti) <span class="required">*</span></label>
                        <div class="input-with-prefix">
                            <span class="input-prefix">M</span>
                            <input type="number" name="awardedValue" id="awardedValue" step="0.01" 
                                   required placeholder="Enter final agreed amount" class="form-control">
                        </div>
                        <small>Final negotiated contract value</small>
                    </div>
                    
                    <div class="form-group half">
                        <label>Suggested Value</label>
                        <div class="suggested-value" id="suggestedValue">--</div>
                        <small>Based on selected supplier's bid</small>
                    </div>
                </div>
                
                <div class="form-group full-width">
                    <label for="justification">Award Justification <span class="required">*</span></label>
                    <textarea name="justification" id="justification" rows="4" required 
                              placeholder="Provide detailed justification for selecting this bid based on evaluation criteria (e.g., best value for money, technical compliance, delivery timeline)..."
                              class="form-control"></textarea>
                    <small>Include key reasons for selection such as price competitiveness, technical capability, and proposed timeline</small>
                </div>
                
                <input type="hidden" name="tenderId" value="${tender.tenderId}">
                
                <div class="form-actions">
                    <button type="submit" class="btn btn-success" onclick="return confirmAward()">
                        <span class="btn-icon">📜</span> Finalize Award & Notify Suppliers
                    </button>
                    <a href="${pageContext.request.contextPath}/officer/dashboard" class="btn btn-secondary">Cancel</a>
                </div>
            </div>
        </form>
    </div>

    <!-- AWARD SUMMARY (Visible after selection) -->
    <div class="info-note">
        <p><strong>Note:</strong> Once awarded, the tender status will change to "AWARDED" and all bidding suppliers will be notified via email. This action cannot be undone.</p>
    </div>

</main>

<script>
    // Auto-populate awarded value when supplier is selected
    document.getElementById('winningBidId').addEventListener('change', function() {
        var selectedOption = this.options[this.selectedIndex];
        var bidAmount = selectedOption.getAttribute('data-amount');
        var suggestedSpan = document.getElementById('suggestedValue');
        var awardedInput = document.getElementById('awardedValue');
        
        if (bidAmount) {
            var formattedAmount = parseFloat(bidAmount).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            suggestedSpan.innerHTML = 'M ' + formattedAmount;
            // Optionally pre-fill the awarded value field
            if (!awardedInput.value) {
                awardedInput.value = bidAmount;
            }
        } else {
            suggestedSpan.innerHTML = '--';
        }
    });
    
    function confirmAward() {
        var selectedOption = document.getElementById('winningBidId');
        var selectedText = selectedOption.options[selectedOption.selectedIndex]?.text || 'the selected supplier';
        var justification = document.getElementById('justification').value;
        
        return confirm('Are you sure you want to award the contract to ' + selectedText + '?\n\nThis will notify all bidding suppliers. This action cannot be undone.');
    }
    
    // Trigger initial suggested value
    document.addEventListener('DOMContentLoaded', function() {
        var event = new Event('change');
        document.getElementById('winningBidId').dispatchEvent(event);
    });
</script>

<style>
/* Award Header */
.award-header {
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

.award-header h2 {
    margin: 0 0 0.25rem 0;
    color: white;
    font-size: 1.5rem;
}

.award-header p {
    color: white;
}

.award-header .subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 0.85rem;
}

.status-tag {
    display: inline-block;
    padding: 0.2rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    background: #fcd116;
    color: #1a3a5c;
}

.award-date {
    text-align: right;
    background: rgba(255,255,255,0.15);
    padding: 0.5rem 1rem;
    border-radius: 12px;
}

.date-label {
    display: block;
    font-size: 0.7rem;
    opacity: 0.8;
}

.date-value {
    display: block;
    font-size: 0.9rem;
    font-weight: 600;
}

/* Leaderboard Card */
.leaderboard-card {
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

.table-container {
    overflow-x: auto;
}

.leaderboard-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
}

.leaderboard-table th {
    background: #f0fdf4;
    padding: 0.85rem 1rem;
    text-align: left;
    font-weight: 600;
    color: #0d6e2e;
    border-bottom: 2px solid #e8f5e9;
}

.leaderboard-table td {
    padding: 0.85rem 1rem;
    border-bottom: 1px solid #e8f5e9;
    vertical-align: middle;
}

.leaderboard-table tr:hover td {
    background: #f8faf8;
}

.rank-cell {
    text-align: center;
    font-weight: 600;
    font-size: 0.9rem;
}

.rank-icon {
    margin-right: 2px;
}

.rank-cell.winner-rank {
    background: #d1fae5;
}

.supplier-cell strong {
    color: #0d6e2e;
}

.supplier-cell.winner-supplier strong {
    color: #0d6e2e;
    font-size: 1rem;
}

.winner-badge {
    display: inline-block;
    margin-left: 0.75rem;
    padding: 0.2rem 0.5rem;
    background: #d1fae5;
    color: #065f46;
    border-radius: 20px;
    font-size: 0.65rem;
    font-weight: 600;
}

.amount-cell {
    font-weight: 600;
    color: #0a5524;
}

.score-highlight {
    background: #d1fae5;
    font-weight: 700;
    text-align: center;
    color: #0d6e2e;
}

.score-cell {
    text-align: center;
    color: #6b7280;
}

.leaderboard-footer {
    padding: 0.75rem 1rem;
    background: #f8faf8;
    border-top: 1px solid #e8f5e9;
    text-align: right;
    font-size: 0.8rem;
}

.total-bids strong {
    color: #0d6e2e;
}

/* Award Form Card */
.award-form-card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 1.5rem;
    overflow: hidden;
    border: 1px solid #e8f5e9;
}

.form-grid {
    padding: 1.5rem;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group.full-width {
    width: 100%;
}

.form-group label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: #1f2937;
}

.form-group small {
    display: block;
    font-size: 0.7rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.required {
    color: #dc2626;
}

.form-control {
    width: 100%;
    padding: 0.7rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 0.9rem;
    transition: all 0.2s;
}

.form-control:focus {
    outline: none;
    border-color: #0d6e2e;
    box-shadow: 0 0 0 3px rgba(13,110,46,0.1);
}

select.form-control {
    cursor: pointer;
    background: white;
}

textarea.form-control {
    resize: vertical;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
    margin-bottom: 0;
}

.input-with-prefix {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.input-prefix {
    background: #f0fdf4;
    padding: 0.7rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-weight: 600;
    color: #0d6e2e;
}

.input-with-prefix .form-control {
    flex: 1;
}

.suggested-value {
    background: #f0fdf4;
    padding: 0.7rem 1rem;
    border: 1px solid #e8f5e9;
    border-radius: 8px;
    font-weight: 600;
    color: #0d6e2e;
}

.form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1.5rem;
}

.btn {
    padding: 0.7rem 1.5rem;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    border: none;
    transition: all 0.2s;
}

.btn-success {
    background: #0d6e2e;
    color: white;
}

.btn-success:hover {
    background: #0a5524;
}

.btn-secondary {
    background: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background: #5a6268;
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

.alert-error {
    background: #fee2e2;
    color: #991b1b;
    border-left: 4px solid #dc2626;
}

/* Responsive */
@media (max-width: 768px) {
    .award-header {
        flex-direction: column;
        text-align: center;
    }
    .award-date {
        text-align: center;
    }
    .form-row {
        grid-template-columns: 1fr;
    }
    .form-actions {
        flex-direction: column;
    }
    .form-actions .btn {
        width: 100%;
        justify-content: center;
    }
    .leaderboard-table th,
    .leaderboard-table td {
        padding: 0.5rem;
        font-size: 0.7rem;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>