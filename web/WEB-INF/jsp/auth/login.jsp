<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-header">
            <h1>ProcureGov</h1>
            <p>Ministry of Public Works, Kingdom of Lesotho</p>
        </div>

        <c:if test="${param.error == 'access_denied'}">
            <div class="alert alert-error">⚠️ Access Denied. Please log in with authorized credentials.</div>
        </c:if>
        
        <c:if test="${param.success == '1'}">
            <div class="alert alert-success">✅ Registration successful. Please log in.</div>
        </c:if>

        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-error">⚠️ ${sessionScope.errorMsg}</div>
            <c:remove var="errorMsg" scope="session"/>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth" method="post">
            <input type="hidden" name="action" value="login">
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" required 
                       placeholder="Enter your email">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required 
                       placeholder="Enter your password">
            </div>
            <button type="submit" class="btn btn-primary">Login</button>
        </form>

        <div class="auth-footer">
            <p>Are you a supplier? <a href="${pageContext.request.contextPath}/register">Register here</a></p>
        </div>
    </div>
</body>
</html>