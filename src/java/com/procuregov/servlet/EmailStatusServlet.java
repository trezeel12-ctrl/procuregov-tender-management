package com.procuregov.servlet;

import com.procuregov.service.AsyncEmailService;
import com.procuregov.util.AuthUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class EmailStatusServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        if (!AuthUtil.requireOfficer(req, resp)) return;
        
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        String status = AsyncEmailService.getQueueStatus();
        
        out.println("{");
        out.println("  \"status\": \"active\",");
        out.println("  \"message\": \"Emails are being sent in background\",");
        out.println("  \"details\": \"" + status + "\"");
        out.println("}");
    }
}