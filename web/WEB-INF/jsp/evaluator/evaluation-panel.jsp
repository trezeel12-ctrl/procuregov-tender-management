<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<main class="content-wrapper">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    
    <div class="evaluation-header">
        <div>
            <h1>Bid Evaluation Panel</h1>
            <p class="tender-info">Tender: ${tender.referenceNo} - ${tender.title}</p>
        </div>
        <div class="status-badge ${tender.status.toLowerCase()}">${tender.status}</div>
    </div>

    <!-- Tender Notice PDF Download Button -->
    <div class="tender-notice-bar">
        <div class="tender-notice-info">
            <span class="notice-icon">📄</span>
            <span>Tender Notice Document</span>
        </div>
        <c:if test="${not empty tender.noticeFilePath}">
            <a href="${pageContext.request.contextPath}/download?path=${tender.noticeFilePath}" class="btn-download-notice" target="_blank">
                Download Tender Notice (PDF)
            </a>
        </c:if>
        <c:if test="${empty tender.noticeFilePath}">
            <span class="no-notice">No tender notice available</span>
        </c:if>
    </div>

    <c:if test="${not empty param.success}">
        <div class="alert alert-success">Scores submitted successfully!</div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-error">Failed to submit scores. Please try again.</div>
    </c:if>

    <c:choose>
        <c:when test="${!hasSubmitted}">
            <form action="${pageContext.request.contextPath}/evaluation" method="post" id="evaluationForm">
                <input type="hidden" name="action" value="submitScores">
                <input type="hidden" name="tenderId" value="${tender.tenderId}">
                
                <div class="evaluation-card">
                    <div class="card-header">
                        <h3>Score Each Bid</h3>
                        <p>Enter Technical Score (0-100) for each bid. Click on any row to view the supplier's technical statement.</p>
                    </div>
                    
                    <div class="table-container">
                        <table class="evaluation-table">
                            <thead>
                                <tr>
                                    <th>Supplier</th>
                                    <th>Bid Amount (M)</th>
                                    <th>Timeline (Days)</th>
                                    <th>Price Score<br><small>40% Weight</small></th>
                                    <th>Timeline Score<br><small>25% Weight</small></th>
                                    <th>Technical Score<br><small>35% Weight</small></th>
                                    <th>Weighted Total<br><small>100%</small></th>
                                    <th>Documents</th>
                                 </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="bid" items="${bids}">
                                    <input type="hidden" name="bidIds" value="${bid.bidId}">
                                    <tr class="bid-row" data-bid-id="${bid.bidId}" data-supplier="${bid.supplierName}" data-statement="${bid.technicalStatement}">
                                        <td class="supplier-cell">
                                            <strong>${bid.supplierName}</strong>
                                            <div class="bid-id">BID-${bid.bidId}</div>
                                        </td>
                                        <td class="amount-cell"><fmt:formatNumber value="${bid.bidAmount}" pattern="#,##0.00"/></td>
                                        <td class="timeline-cell">${bid.proposedTimelineDays}</td>
                                        <td class="score-cell price-score"><fmt:formatNumber value="${bid.priceScore}" pattern="#0.00"/>%</td>
                                        <td class="score-cell timeline-score"><fmt:formatNumber value="${bid.timelineScore}" pattern="#0.00"/>%</td>
                                        <td>
                                            <input type="number" name="technical_${bid.bidId}" 
                                                   class="tech-input" 
                                                   min="0" max="100" step="1" 
                                                   value="0"
                                                   data-bid="${bid.bidId}"
                                                   oninput="calculateTotal(this)"
                                                   required>
                                         </td>
                                        <td class="total-cell" id="total_${bid.bidId}">0.00</td>
                                        <td class="document-cell">
                                            <c:if test="${not empty bid.supportingDocPath}">
                                                <a href="${pageContext.request.contextPath}/download?path=${bid.supportingDocPath}" class="btn-doc" target="_blank" title="View Supporting Document">
                                                    📎 View Doc
                                                </a>
                                            </c:if>
                                            <c:if test="${empty bid.supportingDocPath}">
                                                <span class="no-doc">No document</span>
                                            </c:if>
                                            <button type="button" class="btn-statement" onclick="showStatement('${bid.supplierName}', '${bid.technicalStatement}')" title="View Technical Statement">
                                                📋 Statement
                                            </button>
                                         </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table
                    </div>
                    
                    <div class="form-actions">
                        <button type="button" class="btn-submit" onclick="validateAllScores()">Submit Evaluation Scores</button>
                        <a href="${pageContext.request.contextPath}/evaluator/dashboard" class="btn-cancel">Cancel</a>
                    </div>
                    <p class="warning-note">Once submitted, scores cannot be modified. You must score all bids.</p>
                </div>
            </form>
        </c:when>
        <c:otherwise>
            <div class="submitted-card">
                <div class="checkmark">✓</div>
                <h3>Evaluation Complete!</h3>
                <p>You have already submitted your evaluation scores for this tender.</p>
                <a href="${pageContext.request.contextPath}/evaluator/dashboard" class="btn-primary">Back to Dashboard</a>
            </div>
        </c:otherwise>
    </c:choose>
    
    <!-- Modal for Technical Statement -->
    <div id="statementModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Technical Statement</h3>
                <span class="close" onclick="closeModal()">&times;</span>
            </div>
            <div class="modal-body">
                <p id="modalSupplierName"></p>
                <div id="modalStatementText"></div>
            </div>
        </div>
    </div>
    
    <div class="guidelines-card">
        <h3>Scoring Guidelines</h3>
        <div class="guidelines-grid">
            <div class="guideline">
                <span class="icon">💰</span>
                <div>
                    <strong>Price Score (40% weight)</strong>
                    <p class="formula">(This Bid Amount / Highest Bid Amount) × 100</p>
                    <p class="example">Higher bid = Higher score</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">📋</span>
                <div>
                    <strong>Technical Score (35% weight)</strong>
                    <p>Enter 0-100 based on compliance</p>
                    <p class="example">Click "Statement" to view supplier's technical proposal</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">⏱️</span>
                <div>
                    <strong>Timeline Score (25% weight)</strong>
                    <p class="formula">(Shortest Timeline / This Timeline) × 100</p>
                    <p class="example">Shorter timeline = Higher score</p>
                </div>
            </div>
            <div class="guideline">
                <span class="icon">📎</span>
                <div>
                    <strong>Supporting Documents</strong>
                    <p>Click "View Doc" to see supplier's uploaded PDF</p>
                    <p class="example">Review technical proposal before scoring</p>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
// Modal functions
function showStatement(supplierName, statement) {
    var modal = document.getElementById('statementModal');
    var supplierSpan = document.getElementById('modalSupplierName');
    var statementDiv = document.getElementById('modalStatementText');
    
    supplierSpan.innerHTML = '<strong>Supplier:</strong> ' + supplierName;
    statementDiv.innerHTML = '<strong>Technical Statement:</strong><br><div class="statement-text">' + 
                             (statement ? escapeHtml(statement) : 'No technical statement provided') + '</div>';
    
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('statementModal').style.display = 'none';
}

// Close modal when clicking outside
window.onclick = function(event) {
    var modal = document.getElementById('statementModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/\n/g, '<br>');
}

function calculateTotal(inputElement) {
    var row = inputElement.closest('tr');
    if (!row) return;
    
    var priceCell = row.querySelector('.price-score');
    var timelineCell = row.querySelector('.timeline-score');
    var techInput = row.querySelector('.tech-input');
    var totalCell = row.querySelector('.total-cell');
    
    if (!priceCell || !timelineCell || !techInput || !totalCell) return;
    
    var priceText = priceCell.innerText.replace('%', '');
    var timelineText = timelineCell.innerText.replace('%', '');
    var technical = parseFloat(techInput.value) || 0;
    
    var priceScore = parseFloat(priceText) || 0;
    var timelineScore = parseFloat(timelineText) || 0;
    
    if (technical < 0) technical = 0;
    if (technical > 100) technical = 100;
    if (techInput.value != technical) techInput.value = technical;
    
    var weightedPrice = (priceScore * 40) / 100;
    var weightedTimeline = (timelineScore * 25) / 100;
    var weightedTechnical = (technical * 35) / 100;
    var total = weightedPrice + weightedTimeline + weightedTechnical;
    
    totalCell.innerText = total.toFixed(2);
    
    if (total >= 70) {
        totalCell.style.background = "#d1fae5";
        totalCell.style.color = "#065f46";
        totalCell.style.fontWeight = "bold";
    } else if (total >= 50) {
        totalCell.style.background = "#fef3c7";
        totalCell.style.color = "#92400e";
    } else {
        totalCell.style.background = "#fee2e2";
        totalCell.style.color = "#991b1b";
    }
}

function validateAllScores() {
    var inputs = document.querySelectorAll('.tech-input');
    for (var i = 0; i < inputs.length; i++) {
        var val = inputs[i].value;
        if (val === null || val === '') {
            alert('Please enter technical scores for all bids before submitting.');
            inputs[i].focus();
            return false;
        }
    }
    if (confirm('Are you sure you want to submit these scores? Once submitted, you cannot change them.')) {
        document.getElementById('evaluationForm').submit();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    var inputs = document.querySelectorAll('.tech-input');
    for (var i = 0; i < inputs.length; i++) {
        calculateTotal(inputs[i]);
    }
});
</script>

<style>
.evaluation-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
    padding: 1.5rem 2rem;
    border-radius: 16px;
    margin-bottom: 1rem;
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.tender-notice-bar {
    background: #f0fdf4;
    border: 1px solid #e8f5e9;
    border-radius: 12px;
    padding: 0.75rem 1rem;
    margin-bottom: 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.tender-notice-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 500;
    color: #0d6e2e;
}

.notice-icon {
    font-size: 1.2rem;
}

.btn-download-notice {
    background: #0d6e2e;
    color: white;
    padding: 0.4rem 1rem;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.8rem;
}

.btn-download-notice:hover {
    background: #0a5524;
}

.evaluation-header h1 {
    margin: 0 0 0.25rem 0;
    font-size: 1.5rem;
}

.tender-info {
    margin: 0;
    opacity: 0.9;
    font-size: 0.85rem;
}

.status-badge {
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-weight: 600;
    font-size: 0.8rem;
}

.status-badge.under_evaluation {
    background: #fcd116;
    color: #1a3a5c;
}

.evaluation-card {
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
}

.table-container {
    overflow-x: auto;
    padding: 0 1rem;
}

.evaluation-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
}

.evaluation-table th {
    background: #0d6e2e;
    color: white;
    padding: 12px 10px;
    text-align: center;
    font-weight: 600;
}

.evaluation-table th small {
    font-size: 0.65rem;
    font-weight: normal;
    opacity: 0.8;
    display: block;
}

.evaluation-table td {
    padding: 12px 10px;
    border-bottom: 1px solid #e8f5e9;
    text-align: center;
    vertical-align: middle;
}

.evaluation-table tr:hover {
    background: #f8faf8;
}

.supplier-cell {
    text-align: left !important;
}

.bid-id {
    font-size: 0.65rem;
    color: #6b7280;
}

.amount-cell {
    font-weight: 600;
    color: #0a5524;
}

.score-cell {
    background: #f0fdf4;
    font-weight: 500;
}

.total-cell {
    font-weight: 700;
    font-size: 1rem;
    padding: 8px;
    border-radius: 6px;
}

.document-cell {
    white-space: nowrap;
}

.tech-input {
    width: 80px;
    padding: 6px 8px;
    text-align: center;
    border: 2px solid #ddd;
    border-radius: 6px;
    font-size: 0.9rem;
}

.tech-input:focus {
    outline: none;
    border-color: #0d6e2e;
}

.btn-doc, .btn-statement {
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
    padding: 4px 8px;
    border-radius: 6px;
    font-size: 0.7rem;
    cursor: pointer;
    margin: 0 2px;
    text-decoration: none;
    display: inline-block;
}

.btn-doc:hover, .btn-statement:hover {
    background: #0d6e2e;
    color: white;
}

.no-doc {
    color: #9ca3af;
    font-size: 0.7rem;
}

.form-actions {
    padding: 1.5rem;
    text-align: center;
    border-top: 1px solid #e8f5e9;
}

.btn-submit {
    background: #0d6e2e;
    color: white;
    padding: 12px 30px;
    border: none;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    margin-right: 1rem;
}

.btn-submit:hover {
    background: #0a5524;
}

.btn-cancel {
    background: #6c757d;
    color: white;
    padding: 12px 30px;
    border-radius: 8px;
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 600;
}

.warning-note {
    text-align: center;
    font-size: 0.75rem;
    color: #92400e;
    padding-bottom: 1rem;
    margin: 0;
}

.guidelines-card {
    background: #f0fdf4;
    border-radius: 16px;
    padding: 1.25rem;
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

.formula {
    font-family: monospace;
    background: white;
    display: inline-block;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.7rem;
}

.example {
    font-size: 0.65rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.submitted-card {
    text-align: center;
    padding: 3rem;
    background: white;
    border-radius: 16px;
}

.checkmark {
    font-size: 3rem;
    margin-bottom: 1rem;
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

.btn-primary {
    background: #0d6e2e;
    color: white;
    padding: 8px 16px;
    border-radius: 8px;
    text-decoration: none;
    display: inline-block;
    margin-top: 1rem;
}

/* Modal Styles */
.modal {
    display: none;
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
}

.modal-content {
    background-color: white;
    margin: 10% auto;
    padding: 0;
    width: 500px;
    max-width: 90%;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.modal-header {
    padding: 1rem 1.5rem;
    background: #0d6e2e;
    color: white;
    border-radius: 16px 16px 0 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h3 {
    margin: 0;
}

.close {
    color: white;
    font-size: 28px;
    font-weight: bold;
    cursor: pointer;
}

.close:hover {
    color: #fcd116;
}

.modal-body {
    padding: 1.5rem;
}

.modal-body p {
    margin-bottom: 1rem;
}

.statement-text {
    background: #f8faf8;
    padding: 1rem;
    border-radius: 8px;
    line-height: 1.6;
    margin-top: 0.5rem;
}

@media (max-width: 768px) {
    .guidelines-grid {
        grid-template-columns: 1fr;
    }
    .evaluation-header {
        flex-direction: column;
        text-align: center;
    }
    .evaluation-table th, .evaluation-table td {
        padding: 6px 4px;
        font-size: 0.7rem;
    }
    .tech-input {
        width: 60px;
        padding: 4px;
        font-size: 0.7rem;
    }
    .modal-content {
        width: 95%;
        margin: 20% auto;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>