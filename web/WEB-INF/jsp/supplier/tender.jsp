<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<main class="content-wrapper">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    
    <div class="tender-header">
        <div>
            <h2>${tender.referenceNo}</h2>
            <p class="tender-title">${tender.title}</p>
        </div>
        <span class="badge status-${tender.status.toLowerCase()}">${tender.status}</span>
    </div>

    <div class="tender-details-grid">
        <div class="detail-card">
            <span class="detail-label">Category</span>
            <span class="detail-value">${tender.category}</span>
        </div>
        
        <input type="hidden" id="closingDateTimeHidden" value="${tender.closingDateTime}" />
        
        <div class="detail-card">
            <span class="detail-label">Estimated Value</span>
            <span class="detail-value">M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></span>
        </div>
        <div class="detail-card">
            <span class="detail-label">Closing Deadline</span>
            <span class="detail-value">${tender.closingDateTime.minusHours(2).toString().substring(0, 16).replace('T', ' ')}</span>
        </div>
        <div class="detail-card">
            <span class="detail-label">Time Remaining</span>
            <span class="detail-value" id="timeRemaining">Calculating...</span>
        </div>
        <div class="detail-card full-width">
            <span class="detail-label">Description</span>
            <p class="description-text">${tender.description}</p>
        </div>
        <c:if test="${not empty tender.noticeFilePath}">
            <div class="detail-card full-width">
                <span class="detail-label">Tender Notice</span>
                <a href="${pageContext.request.contextPath}/download?path=${tender.noticeFilePath}" class="btn-download" target="_blank">Download Official Notice (PDF)</a>
            </div>
        </c:if>
    </div>

    <div class="bidding-card">
        <c:choose>
            <c:when test="${tender.status == 'OPEN' && !hasBid}">
                <div class="bidding-open">
                    <div class="alert alert-info">
                        <strong>Bidding Open!</strong> This tender is currently accepting submissions.
                    </div>
                    <form action="${pageContext.request.contextPath}/supplier/bid-submit" method="post" enctype="multipart/form-data" class="bid-form" id="bidForm">
                        <input type="hidden" name="tenderId" value="${tender.tenderId}">
                        
                        <div class="form-group">
                            <label>Bid Amount (Maloti) <span class="required">*</span></label>
                            <input type="number" name="bidAmount" step="0.01" min="1" required class="form-control" placeholder="Enter your bid amount">
                            <small>Your proposed contract value in Lesotho Maloti</small>
                        </div>
                        
                        <div class="form-group">
                            <label>Technical Statement <span class="required">*</span></label>
                            <textarea name="technicalStatement" maxlength="600" rows="4" required class="form-control" placeholder="Explain how your company meets the requirements..."></textarea>
                            <div class="char-counter">
                                <span id="charCount">0</span>/600 characters
                            </div>
                            <small>Describe your technical approach, methodology, and qualifications</small>
                        </div>
                        
                        <div class="form-group">
                            <label>Proposed Timeline (Days) <span class="required">*</span></label>
                            <input type="number" name="timelineDays" min="1" required class="form-control" placeholder="Enter number of days">
                            <small>Estimated completion time in calendar days</small>
                        </div>
                        
                        <div class="form-group">
                            <label>Supporting Document <span class="required">*</span></label>
                            <input type="file" name="supportingDoc" accept=".pdf,.docx" required class="form-control-file">
                            <small>PDF or DOCX format, maximum 10MB. Upload your company profile, technical proposal, or supporting documents.</small>
                        </div>
                        
                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary" onclick="return validateForm()">
                                Submit Bid Securely
                            </button>
                            <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-secondary">Cancel</a>
                        </div>
                    </form>
                </div>
            </c:when>
            <c:when test="${tender.status == 'OPEN' && hasBid}">
                <div class="alert alert-warning">
                    <strong>Already Submitted!</strong> You have already submitted a bid for this tender. Only one submission is permitted.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:when>
            <c:when test="${tender.status == 'CLOSED'}">
                <div class="alert alert-secondary">
                    <strong>Bidding Closed</strong> The deadline for this tender has passed. No further submissions will be accepted.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:when>
            <c:when test="${tender.status == 'UNDER_EVALUATION'}">
                <div class="alert alert-info">
                    <strong>Under Evaluation</strong> Your bid is being reviewed by the evaluation committee.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:when>
            <c:when test="${tender.status == 'EVALUATED'}">
                <div class="alert alert-info">
                    <strong>Evaluation Complete</strong> The evaluation process is complete. Award announcement pending.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:when>
            <c:when test="${tender.status == 'AWARDED'}">
                <div class="alert alert-success">
                    <strong>Contract Awarded</strong> This tender has been awarded. Check your dashboard for results.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:when>
            <c:otherwise>
                <div class="alert alert-secondary">
                    <strong>Not Available</strong> This tender is not currently open for bidding.
                </div>
                <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Back to Dashboard</a>
            </c:otherwise>
        </c:choose>
    </div>

</main>

<script>
// Get the closing date from hidden input
const closingDateStr = document.getElementById('closingDateTimeHidden').value;
console.log('Closing Date String:', closingDateStr);

function updateTimeRemaining() {
    const element = document.getElementById('timeRemaining');
    if (!element) return;
    
    if (!closingDateStr || closingDateStr === '') {
        element.innerHTML = 'Date not available';
        return;
    }
    
    let closingDate;
    if (closingDateStr.includes('T')) {
        closingDate = new Date(closingDateStr);
        closingDate.setHours(closingDate.getHours() - 2);
    } else {
        closingDate = new Date(closingDateStr.replace(' ', 'T'));
        closingDate.setHours(closingDate.getHours() - 2);
    }
    
    if (isNaN(closingDate.getTime())) {
        element.innerHTML = 'Invalid date';
        return;
    }
    
    const now = new Date();
    const diff = closingDate - now;
    
    if (diff <= 0) {
        element.innerHTML = 'Closed';
        return;
    }
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (86400000)) / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    
    let text = '';
    if (days > 0) {
        text = days + ' days, ' + hours + ' hours, ' + minutes + ' minutes left';
    } else if (hours > 0) {
        text = hours + ' hours, ' + minutes + ' minutes left';
    } else if (minutes > 0) {
        text = minutes + ' minutes left';
    } else {
        text = 'Closing now';
    }
    
    element.innerHTML = text;
}

updateTimeRemaining();
setInterval(updateTimeRemaining, 60000);
</script>

<style>
.tender-header {
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

.tender-header h2 {
    margin: 0 0 0.25rem 0;
    color: white;
    font-size: 1.3rem;
}

.tender-header p {
    color: white;
}

.tender-title {
    margin: 0;
    opacity: 0.9;
    font-size: 0.9rem;
}

.tender-details-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.detail-card {
    background: white;
    border-radius: 12px;
    padding: 1rem;
    border: 1px solid #e8f5e9;
}

.detail-card.full-width {
    grid-column: 1 / -1;
}

.detail-label {
    display: block;
    font-size: 0.7rem;
    color: #6b7280;
    margin-bottom: 0.25rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.detail-value {
    font-size: 0.95rem;
    font-weight: 500;
    color: #1f2937;
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

.description-text {
    line-height: 1.6;
    margin: 0;
}

.bidding-card {
    background: white;
    border-radius: 16px;
    padding: 1.5rem;
    border: 1px solid #e8f5e9;
    margin-top: 0.5rem;
}

.bid-form {
    margin-top: 1rem;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.5rem;
    color: #1f2937;
}

.form-group small {
    font-size: 0.7rem;
    color: #6b7280;
    display: block;
    margin-top: 0.25rem;
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

textarea.form-control {
    resize: vertical;
}

.form-control-file {
    width: 100%;
    padding: 0.5rem 0;
}

.char-counter {
    text-align: right;
    font-size: 0.7rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.required {
    color: #dc2626;
}

.form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1.5rem;
}

.btn {
    padding: 0.6rem 1.2rem;
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

.btn-primary {
    background: #0d6e2e;
    color: white;
}

.btn-primary:hover {
    background: #0a5524;
    transform: translateY(-1px);
}

.btn-secondary {
    background: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background: #5a6268;
}

.btn-download {
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
    padding: 0.4rem 0.8rem;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.8rem;
    display: inline-block;
    transition: all 0.2s;
}

.btn-download:hover {
    background: #0d6e2e;
    color: white;
}

.alert {
    padding: 0.75rem 1rem;
    border-radius: 10px;
    margin-bottom: 1rem;
}

.alert-info {
    background: #cffafe;
    color: #155e75;
    border-left: 4px solid #0891b2;
}

.alert-warning {
    background: #fef3c7;
    color: #92400e;
    border-left: 4px solid #d97706;
}

.alert-success {
    background: #d1fae5;
    color: #065f46;
    border-left: 4px solid #059669;
}

.alert-secondary {
    background: #f3f4f6;
    color: #4b5563;
    border-left: 4px solid #6b7280;
}

.badge {
    display: inline-block;
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-open { background: #d1fae5; color: #065f46; }
.status-closed { background: #fed7aa; color: #92400e; }
.status-under_evaluation { background: #fef3c7; color: #92400e; }
.status-evaluated { background: #cffafe; color: #155e75; }
.status-awarded { background: #d1fae5; color: #065f46; }

@media (max-width: 768px) {
    .tender-header {
        flex-direction: column;
        text-align: center;
    }
    .form-actions {
        flex-direction: column;
    }
    .form-actions .btn {
        width: 100%;
        justify-content: center;
    }
    .tender-details-grid {
        grid-template-columns: 1fr;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>