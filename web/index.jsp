<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Force session invalidation if ?reset=1 is in URL
    if ("1".equals(request.getParameter("reset"))) {
        HttpSession sess = request.getSession(false);
        if (sess != null) {
            sess.invalidate();
        }
        // Clear cookies
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie c : cookies) {
                if ("JSESSIONID".equals(c.getName())) {
                    c.setMaxAge(0);
                    c.setPath(request.getContextPath());
                    response.addCookie(c);
                    break;
                }
            }
        }
    }
    
    // No-cache headers
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="refresh" content="0;url=${pageContext.request.contextPath}/auth?action=login">
    <title>ProcureGov - Redirecting...</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .redirect-wrapper {
            display: flex; flex-direction: column; align-items: center; justify-content: center;
            min-height: 100vh; background: var(--bg-page); text-align: center; padding: 2rem;
        }
        .spinner {
            width: 48px; height: 48px; border: 4px solid #e5e7eb; border-top: 4px solid var(--primary);
            border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 1.25rem;
        }
    </style>
</head>
<body>
    <div class="redirect-wrapper">
        <div class="spinner"></div>
        <h1 style="color: var(--primary); margin-bottom: 0.5rem;">ProcureGov</h1>
        <p style="color: var(--text-muted);">Ministry of Public Works, Kingdom of Lesotho</p>
        <p style="margin-top: 1rem; font-size: 0.9rem;">
            <c:choose>
                <c:when test="${param.reset == '1'}">Session reset. Redirecting to login...</c:when>
                <c:otherwise>Redirecting to login...</c:otherwise>
            </c:choose>
        </p>
        <p style="font-size: 0.8rem; color: #6b7280; margin-top: 0.5rem;">
            <a href="${pageContext.request.contextPath}/?reset=1">🔄 Force reset session</a>
        </p>
    </div>

    <c:redirect url="/auth?action=login"/>
</body>
</html>