<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp/common/navbar.jsp" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

<main class="content-wrapper">
    <div class="dashboard-header">
        <div>
            <h2>⚠️ Award Notice Error</h2>
        </div>
    </div>

    <c:choose>
        <c:when test="${param.error == 'not_winner'}">
            <div class="alert alert-warning">
                <strong>You cannot view this award notice.</strong>
                <p>This contract was awarded to a different supplier. Only the winning supplier can view the full award details.</p>
            </div>
        </c:when>
        <c:when test="${param.error == 'unauthorized'}">
            <div class="alert alert-warning">
                <strong>Unauthorized Access</strong>
                <p>You do not have permission to view this award notice.</p>
            </div>
        </c:when>
        <c:when test="${param.error == 'not_awarded'}">
            <div class="alert alert-info">
                <strong>Tender Not Yet Awarded</strong>
                <p>This tender is still under evaluation. The award notice will appear here once the contract is awarded.</p>
            </div>
        </c:when>
        <c:when test="${param.error == 'no_award'}">
            <div class="alert alert-info">
                <strong>Award Record Not Found</strong>
                <p>No award record exists for this tender. Please contact the procurement officer.</p>
            </div>
        </c:when>
        <c:when test="${param.error == 'no_tender'}">
            <div class="alert alert-error">
                <strong>Tender Not Found</strong>
                <p>The requested tender does not exist.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-warning">
                <strong>Access Restricted</strong>
                <p>You do not have permission to view this award notice.</p>
            </div>
        </c:otherwise>
    </c:choose>
    
    <div class="back-links">
        <a href="${pageContext.request.contextPath}/supplier/award-notices" class="btn btn-primary">Back to My Award Notices</a>
        <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn btn-secondary">Back to Dashboard</a>
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
    color: white;
}

.alert {
    padding: 1rem;
    border-radius: 12px;
    margin-bottom: 1rem;
}

.alert-warning {
    background: #fef3c7;
    color: #92400e;
    border-left: 4px solid #d97706;
}

.alert-info {
    background: #cffafe;
    color: #155e75;
    border-left: 4px solid #0891b2;
}

.alert-error {
    background: #fee2e2;
    color: #991b1b;
    border-left: 4px solid #dc2626;
}

.back-links {
    display: flex;
    gap: 1rem;
    justify-content: center;
    margin-top: 1rem;
}

.btn-primary {
    background: #0d6e2e;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    text-decoration: none;
}

.btn-primary:hover {
    background: #0a5524;
}

.btn-secondary {
    background: #6c757d;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 8px;
    text-decoration: none;
}

.btn-secondary:hover {
    background: #5a6268;
}
</style>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>