<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">

    <div class="awarded-header">
        <div>
            <h1>🏆 Official Award Notice</h1>
            <p class="subtitle">Contract Award Details</p>
        </div>
        <div class="award-date-badge">
            <span class="badge-label">Award Date</span>
            <span class="badge-value">${award.awardDate}</span>
        </div>
    </div>

    <!-- TENDER INFORMATION SECTION -->
    <div class="info-card">
        <div class="card-header">
            <h3>📋 Tender Information</h3>
        </div>
        <div class="info-grid">
            <div class="info-row">
                <span class="info-label">Reference Number:</span>
                <span class="info-value">${tender.referenceNo}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Tender Title:</span>
                <span class="info-value">${tender.title}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Category:</span>
                <span class="info-value">${tender.category}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Original Estimated Value:</span>
                <span class="info-value">M <fmt:formatNumber value="${tender.estimatedValue}" pattern="#,##0.00"/></span>
            </div>
            <div class="info-row">
                <span class="info-label">Closing Date:</span>
                <span class="info-value">${tender.closingDateTime.toString().substring(0, 16).replace('T', ' ')}</span>
            </div>
        </div>
    </div>

    <!-- WINNING SUPPLIER INFORMATION SECTION -->
    <div class="info-card">
        <div class="card-header">
            <h3>🏢 Winning Supplier Information</h3>
        </div>
        <div class="info-grid">
            <div class="info-row">
                <span class="info-label">Company Name:</span>
                <span class="info-value supplier-highlight">${supplier.fullName}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Registration Number:</span>
                <span class="info-value">${supplier.registrationNumber}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Physical Address:</span>
                <span class="info-value">${supplier.physicalAddress}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Contact Number:</span>
                <span class="info-value">${supplier.contactNumber}</span>
            </div>
            <div class="info-row">
                <span class="info-label">Email Address:</span>
                <span class="info-value">${supplier.email}</span>
            </div>
        </div>
    </div>

    <!-- AWARD DETAILS SECTION -->
    <div class="info-card">
        <div class="card-header">
            <h3>💰 Award Details</h3>
        </div>
        <div class="info-grid">
            <div class="info-row">
                <span class="info-label">Original Bid Amount:</span>
                <span class="info-value">M <fmt:formatNumber value="${winningBid.bidAmount}" pattern="#,##0.00"/></span>
            </div>
            <div class="info-row">
                <span class="info-label">Awarded Contract Value:</span>
                <span class="info-value award-amount">M <fmt:formatNumber value="${award.awardedValue}" pattern="#,##0.00"/></span>
            </div>
            <div class="info-row">
                <span class="info-label">Proposed Timeline:</span>
                <span class="info-value">${winningBid.proposedTimelineDays} days</span>
            </div>
            <div class="info-row">
                <span class="info-label">Award Date:</span>
                <span class="info-value">${award.awardDate}</span>
            </div>
            <div class="info-row full-width">
                <span class="info-label">Justification:</span>
                <p class="justification-text">${award.justification}</p>
            </div>
        </div>
    </div>

    <!-- DOCUMENTS SECTION -->
    <div class="documents-card">
        <div class="card-header">
            <h3>📎 Contract Documents</h3>
        </div>
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
                    <strong>Supplier Bid Document</strong>
                    <p>Technical proposal and supporting documents submitted by ${supplier.fullName}</p>
                </div>
                <a href="${pageContext.request.contextPath}/download?file=${winningBid.supportingDocPath}" class="btn-download" target="_blank">
                    Download PDF
                </a>
            </div>
        </div>
    </div>

    <!-- BACK BUTTON -->
    <div class="back-actions">
        <a href="${pageContext.request.contextPath}/officer/award" class="btn btn-secondary">
            ← Back to Award Management
        </a>
        <a href="${pageContext.request.contextPath}/officer/dashboard" class="btn btn-outline">
            Go to Dashboard
        </a>
    </div>

</main>

<style>
.awarded-header {
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

.awarded-header h1 {
    margin: 0 0 0.25rem 0;
    font-size: 1.5rem;
}

.awarded-header .subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 0.85rem;
}

.award-date-badge {
    background: rgba(255,255,255,0.2);
    padding: 0.5rem 1rem;
    border-radius: 12px;
    text-align: center;
}

.award-date-badge .badge-label {
    display: block;
    font-size: 0.7rem;
    opacity: 0.8;
}

.award-date-badge .badge-value {
    display: block;
    font-size: 0.9rem;
    font-weight: 600;
}

.info-card {
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
    margin: 0;
    color: #0d6e2e;
    font-size: 1rem;
}

.info-grid {
    padding: 1.5rem;
}

.info-row {
    display: flex;
    padding: 0.75rem 0;
    border-bottom: 1px solid #f0fdf4;
}

.info-label {
    width: 200px;
    font-weight: 600;
    color: #6b7280;
}

.info-value {
    flex: 1;
    color: #1f2937;
}

.supplier-highlight {
    font-size: 1rem;
    font-weight: 700;
    color: #0d6e2e;
}

.award-amount {
    font-size: 1.1rem;
    font-weight: 700;
    color: #0d6e2e;
}

.justification-text {
    margin: 0;
    padding: 0.75rem;
    background: #f0fdf4;
    border-radius: 8px;
    line-height: 1.6;
    color: #4b5563;
}

.full-width {
    flex-direction: column;
}

.full-width .info-label {
    width: auto;
    margin-bottom: 0.5rem;
}

.documents-card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    margin-bottom: 1.5rem;
    overflow: hidden;
    border: 1px solid #e8f5e9;
}

.documents-list {
    padding: 1.5rem;
}

.document-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: #f8faf8;
    border-radius: 12px;
    margin-bottom: 1rem;
}

.document-item:last-child {
    margin-bottom: 0;
}

.doc-icon {
    font-size: 1.8rem;
}

.doc-info {
    flex: 1;
}

.doc-info strong {
    display: block;
    font-size: 0.9rem;
    color: #1f2937;
}

.doc-info p {
    font-size: 0.75rem;
    color: #6b7280;
    margin: 0.25rem 0 0;
}

.btn-download {
    background: #0d6e2e;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    text-decoration: none;
    font-size: 0.8rem;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

.btn-download:hover {
    background: #0a5524;
}

.back-actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    margin-top: 1rem;
}

.btn {
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    text-decoration: none;
    display: inline-block;
}

.btn-secondary {
    background: #0d6e2e;
    color: white;
}

.btn-secondary:hover {
    background: #0a5524;
}

.btn-outline {
    background: transparent;
    border: 1px solid #0d6e2e;
    color: #0d6e2e;
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
    .info-label {
        width: auto;
    }
    .awarded-header {
        flex-direction: column;
        text-align: center;
    }
    .back-actions {
        flex-direction: column;
    }
    .back-actions .btn {
        text-align: center;
    }
    .document-item {
        flex-direction: column;
        text-align: center;
    }
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>