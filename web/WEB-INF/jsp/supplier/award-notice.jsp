<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">
    <div class="award-container">
        <div class="award-header">
            <h2>Official Award Notice</h2>
            <p class="congrats">Congratulations! You have been awarded the contract.</p>
        </div>

        <div class="award-content">
            <div class="award-section">
                <h3>📋 Tender Information</h3>
                <div class="info-row">
                    <span class="label">Tender Reference:</span>
                    <span class="value">${tender.referenceNo}</span>
                </div>
                <div class="info-row">
                    <span class="label">Tender Title:</span>
                    <span class="value">${tender.title}</span>
                </div>
                <div class="info-row">
                    <span class="label">Category:</span>
                    <span class="value">${tender.category}</span>
                </div>
                <div class="info-row">
                    <span class="label">Original Estimate:</span>
                    <span class="value">M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></span>
                </div>
            </div>

            <div class="award-section">
                <h3>💰 Contract Award Details</h3>
                <div class="info-row">
                    <span class="label">Award Date:</span>
                    <span class="value">${award.awardDate}</span>
                </div>
                <div class="info-row">
                    <span class="label">Your Bid Amount:</span>
                    <span class="value">M <fmt:formatNumber value="${winningBid.bidAmount}" pattern="#,##0.00"/></span>
                </div>
                <div class="info-row">
                    <span class="label">Awarded Value:</span>
                    <span class="value highlight">M <fmt:formatNumber value="${award.awardedValue}" pattern="#,##0.00"/></span>
                </div>
                <div class="info-row">
                    <span class="label">Proposed Timeline:</span>
                    <span class="value">${winningBid.proposedTimelineDays} days</span>
                </div>
            </div>

            <div class="award-section">
                <h3>📝 Justification</h3>
                <p class="justification-text">${award.justification}</p>
            </div>

            <div class="award-section">
                <h3>📎 Documents</h3>
                <div class="documents-list">
                    <div class="document-item">
                        <div class="doc-icon">📄</div>
                        <div class="doc-info">
                            <strong>Tender Notice Document</strong>
                            <p>Official tender notice published by the Ministry</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/download?file=${tender.noticeFilePath}" class="btn-download" target="_blank">
                            Download PDF
                        </a>
                    </div>
                    <div class="document-item">
                        <div class="doc-icon">📑</div>
                        <div class="doc-info">
                            <strong>Your Bid Document</strong>
                            <p>The technical proposal and supporting documents you submitted</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/download?file=${winningBid.supportingDocPath}" class="btn-download" target="_blank">
                            Download PDF
                        </a>
                    </div>
                </div>
            </div>

            <div class="award-section">
                <h3>📌 Next Steps</h3>
                <ul class="next-steps">
                    <li>✓ Contract preparation will commence within 5 working days</li>
                    <li>✓ You will receive the contract document via email</li>
                    <li>✓ Sign and return the contract within 14 days</li>
                    <li>✓ Project kickoff meeting will be scheduled after contract signing</li>
                    <li>✓ For any inquiries, contact the Procurement Unit</li>
                </ul>
            </div>

            <div class="award-footer">
                <p>This is an official notification from the Ministry of Public Works, Kingdom of Lesotho.</p>
                <p>This award notice is legally binding and serves as confirmation of contract award.</p>
                <div class="footer-actions">
                    <a href="${pageContext.request.contextPath}/supplier/award-notices" class="btn btn-outline">← Back to Award Notices</a>
                    <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-primary">Go to Dashboard</a>
                </div>
            </div>
        </div>
    </div>
</main>

<style>
.award-container {
    max-width: 900px;
    margin: 0 auto;
    background: white;
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.award-header {
    background: linear-gradient(135deg, #0d6e2e 0%, #0a5524 100%);
    color: white;
    text-align: center;
    padding: 2rem;
}

.award-header h2 {
    margin: 0 0 0.5rem 0;
    color: white;
    font-size: 1.8rem;
}

.award-header p {
    color: white;
}

.congrats {
    font-size: 1rem;
    opacity: 0.95;
}

.award-content {
    padding: 2rem;
}

.award-section {
    margin-bottom: 2rem;
    border-bottom: 1px solid #e8f5e9;
    padding-bottom: 1rem;
}

.award-section h3 {
    color: #0d6e2e;
    margin-bottom: 1rem;
    font-size: 1.1rem;
}

.info-row {
    display: flex;
    padding: 0.5rem 0;
    border-bottom: 1px solid #f0fdf4;
}

.info-row .label {
    width: 180px;
    font-weight: 600;
    color: #6b7280;
}

.info-row .value {
    flex: 1;
    color: #1f2937;
}

.highlight {
    color: #0d6e2e;
    font-weight: 700;
    font-size: 1.1rem;
}

.justification-text {
    background: #f8faf8;
    padding: 1rem;
    border-radius: 12px;
    line-height: 1.6;
    color: #4b5563;
}

.documents-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.document-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.75rem;
    background: #f8faf8;
    border-radius: 12px;
}

.doc-icon {
    font-size: 1.5rem;
}

.doc-info {
    flex: 1;
}

.doc-info strong {
    display: block;
    font-size: 0.85rem;
    color: #1f2937;
}

.doc-info p {
    font-size: 0.7rem;
    color: #6b7280;
    margin: 0.25rem 0 0;
}

.btn-download {
    background: #0d6e2e;
    color: white;
    padding: 0.4rem 0.8rem;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.75rem;
    display: inline-block;
}

.btn-download:hover {
    background: #0a5524;
}

.next-steps {
    list-style: none;
    padding: 0;
}

.next-steps li {
    padding: 0.5rem 0;
    color: #4b5563;
}

.award-footer {
    text-align: center;
    padding-top: 1rem;
    color: #6b7280;
    font-size: 0.85rem;
}

.footer-actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    margin-top: 1.5rem;
}

.btn-primary {
    display: inline-block;
    background: #0d6e2e;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    text-decoration: none;
}

.btn-primary:hover {
    background: #0a5524;
}

.btn-outline {
    display: inline-block;
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    text-decoration: none;
}

.btn-outline:hover {
    background: #0d6e2e;
    color: white;
}

@media (max-width: 768px) {
    .info-row {
        flex-direction: column;
        gap: 0.25rem;
    }
    .info-row .label {
        width: auto;
    }
    .footer-actions {
        flex-direction: column;
    }
    .document-item {
        flex-direction: column;
        text-align: center;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>