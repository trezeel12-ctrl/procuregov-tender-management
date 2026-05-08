<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov - Access Denied</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="error-page">
    <div class="error-container">
        <div class="error-icon">🔒</div>
        <h1>Access Denied</h1>
        <p>You do not have permission to view this page.</p>
        <p class="error-context">Unauthorized access attempts are logged for security compliance.</p>
        <p class="error-context">Ministry of Public Works, Kingdom of Lesotho</p>
        <a href="${pageContext.request.contextPath}/auth" class="btn btn-primary">Return to Login</a>
    </div>
</body>
</html>