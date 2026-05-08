<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<footer class="app-footer">
    <div class="footer-content">
        <p>© <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy"/> Ministry of Public Works, Kingdom of Lesotho</p>
        <p class="footer-subtitle">ProcureGov Tender Management System | Secure & Transparent Procurement</p>
    </div>
</footer>