<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov - Supplier Registration</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-header">
            <h1>Supplier Registration</h1>
            <p>Ministry of Public Works, Kingdom of Lesotho</p>
        </div>

        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-error">⚠️ ${sessionScope.errorMsg}</div>
            <c:remove var="errorMsg" scope="session"/>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label for="fullName">Company / Individual Name</label>
                <input type="text" id="fullName" name="fullName" required 
                       placeholder="Enter company name">
            </div>
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" required 
                       placeholder="Enter your email">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" required minlength="6" 
                       placeholder="Minimum 6 characters">
            </div>
            <div class="form-group">
                <label for="address">Physical Address</label>
                <textarea id="address" name="address" required rows="2" 
                          placeholder="Enter full physical address"></textarea>
            </div>
            <div class="form-group">
                <label for="contact">Contact Number</label>
                <input type="tel" id="contact" name="contact" required 
                       placeholder="Enter your phone numbers">
            </div>
            <button type="submit" class="btn btn-primary">Register Account</button>
        </form>

        <div class="auth-footer">
            <p>Already have an account? <a href="${pageContext.request.contextPath}/auth">Login here</a></p>
        </div>
    </div>
</body>
</html>