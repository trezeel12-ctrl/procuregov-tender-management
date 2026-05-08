<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<main class="content-wrapper">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <div class="bid-header">
        <div>
            <h2>Submit Bid</h2>
            <p class="subtitle">Tender: ${tender.referenceNo} - ${tender.title}</p>
        </div>
        <div>
            <span class="deadline-badge">
                Closing: ${tender.closingDateTime.toString().substring(0, 16).replace('T', ' ')}
            </span>
        </div>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="alert alert-error">
            <span class="alert-icon">⚠️</span> ${errorMsg}
        </div>
    </c:if>

    <div class="bid-container">
        <form action="${pageContext.request.contextPath}/supplier/bid-submit" method="post" enctype="multipart/form-data" class="bid-form">
            <input type="hidden" name="action" value="submit">
            <input type="hidden" name="tenderId" value="${tender.tenderId}">

            <div class="form-card">
                <h3>Bid Details</h3>

                <div class="form-group">
                    <label for="bidAmount">Bid Amount (Maloti) <span class="required">*</span></label>
                    <div class="input-wrapper">
                        <span class="input-prefix">M</span>
                        <input type="number" id="bidAmount" name="bidAmount" step="0.01" min="1" required 
                               placeholder="Enter your bid amount" class="form-control">
                    </div>
                    <small>Enter your total proposed contract value in Lesotho Maloti.</small>
                </div>

                <div class="form-group">
                    <label for="technicalStatement">Technical Compliance Statement <span class="required">*</span></label>
                    <textarea id="technicalStatement" name="technicalStatement" maxlength="600" rows="5" required 
                              class="form-control" placeholder="Describe how your solution meets the tender requirements..."></textarea>
                    <div class="char-counter">
                        <span id="charCount">0</span>/600 characters
                    </div>
                    <small>Explain your technical approach, methodology, and qualifications.</small>
                </div>

                <div class="form-row">
                    <div class="form-group half">
                        <label for="timelineDays">Proposed Delivery Timeline <span class="required">*</span></label>
                        <input type="number" id="timelineDays" name="timelineDays" min="1" required 
                               placeholder="Number of days" class="form-control">
                        <small>Calendar days required to complete the work.</small>
                    </div>
                </div>

                <div class="form-group">
                    <label for="supportingDoc">Supporting Document <span class="required">*</span></label>
                    <input type="file" id="supportingDoc" name="supportingDoc" 
                           accept=".pdf,.docx" required class="form-control-file">
                    <small>Upload your company profile, technical proposal, or supporting documents (PDF or DOCX, max 10MB).</small>
                </div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary btn-submit">
                    <span class="btn-icon">📤</span> Submit Bid Securely
                </button>
                <a href="${pageContext.request.contextPath}/supplier/tender?id=${tender.tenderId}" class="btn btn-secondary">Cancel</a>
            </div>
        </form>

        <div class="info-panel">
            <h4>Submission Guidelines</h4>
            <ul>
                <li>Bid amount must be in Lesotho Maloti (M)</li>
                <li>Technical statement maximum 600 characters</li>
                <li>Supporting document must be PDF or DOCX format</li>
                <li>Maximum file size: 10MB</li>
                <li>Only one bid submission per tender allowed</li>
                <li>Bids cannot be modified after submission</li>
            </ul>
        </div>
    </div>

    <div class="notice-box">
        <p><strong>⚠️ Server-Side Enforcement Notice:</strong> All submissions are timestamped against the Ministry server clock. If the server time exceeds the tender closing deadline, your submission will be automatically rejected regardless of your local system time or browser display.</p>
    </div>

</main>

<script>
    // Character counter for technical statement
    const textarea = document.getElementById('technicalStatement');
    const charCountSpan = document.getElementById('charCount');
    
    if (textarea && charCountSpan) {
        textarea.addEventListener('input', function() {
            const count = this.value.length;
            charCountSpan.textContent = count;
            if (count > 550) {
                charCountSpan.style.color = '#d97706';
            } else {
                charCountSpan.style.color = '#6b7280';
            }
        });
    }
</script>

<style>
.bid-header {
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

.bid-header h2 {
    margin: 0 0 0.25rem 0;
    font-size: 1.5rem;
}

.bid-header .subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 0.9rem;
}

.deadline-badge {
    background: rgba(255,255,255,0.2);
    padding: 0.5rem 1rem;
    border-radius: 20px;
    font-size: 0.85rem;
}

.bid-container {
    display: grid;
    grid-template-columns: 1fr 300px;
    gap: 1.5rem;
    margin-bottom: 1.5rem;
}

.form-card {
    background: white;
    border-radius: 16px;
    padding: 1.5rem;
    border: 1px solid #e8f5e9;
}

.form-card h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    color: #0d6e2e;
    border-left: 4px solid #0d6e2e;
    padding-left: 0.75rem;
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
    display: block;
    font-size: 0.7rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.required {
    color: #dc2626;
}

.input-wrapper {
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

.form-control {
    flex: 1;
    padding: 0.7rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 0.9rem;
    transition: all 0.2s;
    width: 100%;
}

.form-control:focus {
    outline: none;
    border-color: #0d6e2e;
    box-shadow: 0 0 0 3px rgba(13,110,46,0.1);
}

textarea.form-control {
    resize: vertical;
}

.char-counter {
    text-align: right;
    font-size: 0.7rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.form-group.half {
    margin-bottom: 0;
}

.form-control-file {
    width: 100%;
    padding: 0.5rem 0;
}

.info-panel {
    background: #f0fdf4;
    border-radius: 16px;
    padding: 1.5rem;
    border: 1px solid #e8f5e9;
    height: fit-content;
}

.info-panel h4 {
    margin-top: 0;
    margin-bottom: 0.75rem;
    color: #0d6e2e;
}

.info-panel ul {
    margin: 0;
    padding-left: 1.2rem;
    color: #4b5563;
    font-size: 0.85rem;
}

.info-panel li {
    margin-bottom: 0.5rem;
}

.form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1rem;
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

.notice-box {
    background: #fef3c7;
    border-left: 4px solid #d97706;
    padding: 1rem;
    border-radius: 8px;
    font-size: 0.8rem;
    color: #92400e;
}

.notice-box p {
    margin: 0;
}

.alert {
    padding: 0.75rem 1rem;
    border-radius: 12px;
    margin-bottom: 1rem;
}

.alert-error {
    background: #fee2e2;
    color: #991b1b;
    border-left: 4px solid #dc2626;
}

@media (max-width: 768px) {
    .bid-container {
        grid-template-columns: 1fr;
    }
    .form-row {
        grid-template-columns: 1fr;
    }
    .bid-header {
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
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>