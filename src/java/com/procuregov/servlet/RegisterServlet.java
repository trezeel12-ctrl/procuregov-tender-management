package com.procuregov.servlet;

import com.procuregov.dao.UserDAO;
import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.User;
import com.procuregov.service.AuthService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Controller for supplier self-registration.
 * Validates input, delegates to AuthService, and redirects to login on success.
 */
public class RegisterServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService(new UserDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String address = req.getParameter("address");
        String contact = req.getParameter("contact");

        if (fullName == null || email == null || password == null || address == null || contact == null) {
            req.setAttribute("errorMsg", "All fields are mandatory.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
            return;
        }

        User supplier = new User();
        supplier.setFullName(fullName.trim());
        supplier.setEmail(email.trim().toLowerCase());
        supplier.setPasswordHash(password); // AuthService will hash it
        supplier.setPhysicalAddress(address.trim());
        supplier.setContactNumber(contact.trim());
        supplier.setRole("SUPPLIER");

        int userId = authService.registerSupplier(supplier);
        if (userId > 0) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login&success=1");
        } else {
            req.setAttribute("errorMsg", "Registration failed. Email may already exist.");
            req.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(req, resp);
        }
    }
}