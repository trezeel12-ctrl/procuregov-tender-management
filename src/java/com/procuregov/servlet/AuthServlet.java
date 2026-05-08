package com.procuregov.servlet;

import com.procuregov.dao.UserDAOImpl;
import com.procuregov.model.User;
import com.procuregov.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Handles authentication (login/logout),
 * session management, and role-based redirection.
 * 
 * Module 1 Compliance:
 * - SHA-256 password hashing
 * - Failed login attempts tracked in database
 * - 3 failed attempts = 15 minute lockout
 * - Session management with role-based redirects
 */
public class AuthServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService(new UserDAOImpl());
        logger.info("AuthServlet initialized");
    }

    /**
     * Handles GET requests:
     * - Show login page
     * - Handle logout
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("logout".equalsIgnoreCase(action)) {
            handleLogout(request, response);
        } else {
            // DO NOT CLEAR THE ERROR MESSAGE HERE - REMOVED THE LINE
            // The error message is already removed in the JSP after display
            request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp")
                   .forward(request, response);
        }
    }

    /**
     * Handles POST requests:
     * - Login processing
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("login".equalsIgnoreCase(action)) {
            handleLogin(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/auth");
        }
    }

    /**
     * Handles login logic with database-based failure tracking.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        HttpSession session = request.getSession();

        // Validate input
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            session.setAttribute("errorMsg", "Please enter both email and password.");
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // First, check if user exists and get their lock status
        User existingUser = authService.getUserByEmail(email);

        if (existingUser != null && authService.isAccountLocked(existingUser)) {
            long minutesLeft = authService.getRemainingLockoutMinutes(existingUser);
            String lockMessage = "Account is temporarily locked due to multiple failed attempts. " +
                                 "Please try again in " + minutesLeft + " minute(s).";
            session.setAttribute("errorMsg", lockMessage);
            logger.info("Login attempt on locked account: " + email);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Attempt authentication
        User user = authService.authenticate(email, password);

        if (user != null) {
            // Successful login
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userName", user.getFullName());
            session.removeAttribute("errorMsg");

            logger.info("Successful login for: " + email + " (Role: " + user.getRole() + ")");

            // Role-based redirection
            switch (user.getRole()) {
                case "SUPPLIER":
                    response.sendRedirect(request.getContextPath() + "/supplier/dashboard");
                    break;
                case "OFFICER":
                    response.sendRedirect(request.getContextPath() + "/officer/dashboard");
                    break;
                case "EVALUATOR":
                    response.sendRedirect(request.getContextPath() + "/evaluator/dashboard");
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/auth");
                    break;
            }
        } else {
            // Failed login - get updated user to show remaining attempts
            User failedUser = authService.getUserByEmail(email);

            if (failedUser != null) {
                int failedAttempts = failedUser.getFailedLoginAttempts();
                int maxAttempts = authService.getMaxFailedAttempts();
                int attemptsRemaining = Math.max(0, maxAttempts - failedAttempts);

                if (failedAttempts < maxAttempts) {
                    session.setAttribute("errorMsg", 
                        "Invalid email or password. " + attemptsRemaining + " attempt(s) remaining before account lock.");
                } else {
                    session.setAttribute("errorMsg", 
                        "Account has been locked for " + authService.getLockoutDurationMinutes() + 
                        " minutes due to too many failed attempts.");
                }
                logger.info("Failed login for: " + email + " (Attempt " + failedAttempts + "/" + maxAttempts + ")");
            } else {
                session.setAttribute("errorMsg", "Invalid email or password.");
                logger.info("Failed login attempt with non-existent email: " + email);
            }

            response.sendRedirect(request.getContextPath() + "/auth");
        }
    }

    /**
     * Handles logout:
     * - Invalidates session
     * - Redirects to login page
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String userEmail = session.getAttribute("email") != null ? 
                              session.getAttribute("email").toString() : "unknown";
            logger.info("Logout for user: " + userEmail);
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/auth");
    }
}