<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar">
    <div class="navbar-brand">
        <a href="${pageContext.request.contextPath}/"> ProcureGov</a>
    </div>

    <c:if test="${not empty sessionScope.user}">
        <ul class="navbar-links">
            <c:choose>
                <c:when test="${sessionScope.userRole == 'SUPPLIER'}">
                    <li><a href="${pageContext.request.contextPath}/supplier/dashboard">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/supplier/bids">My Bids</a></li>
                    <li><a href="${pageContext.request.contextPath}/supplier/award-notices">Award Notices</a></li>
                </c:when>
                <c:when test="${sessionScope.userRole == 'OFFICER'}">
                    <li><a href="${pageContext.request.contextPath}/officer/dashboard">Dashboard</a></li>
                    <li><a href="${pageContext.request.contextPath}/officer/tender?action=list">Tenders</a></li>
                    <li><a href="${pageContext.request.contextPath}/officer/award">Awards</a></li>
                    <li><a href="${pageContext.request.contextPath}/officer/awarded-tenders">Awarded Tenders</a></li>
                </c:when>
                <c:when test="${sessionScope.userRole == 'EVALUATOR'}">
                    <li><a href="${pageContext.request.contextPath}/evaluator/dashboard">Dashboard</a></li>
                </c:when>
            </c:choose>
        </ul>

        <div class="navbar-user">
            <span class="user-info">
                <strong>${sessionScope.user.fullName}</strong> 
                <span class="role-badge">${sessionScope.userRole}</span>
            </span>
            <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn-logout">Logout</a>
        </div>
    </c:if>
</nav>