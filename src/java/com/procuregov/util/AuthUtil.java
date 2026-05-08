package com.procuregov.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Reusable session validation and role-based access control utility.
 * Called at the start of every protected Servlet as required by Module 1.
 */
public final class AuthUtil {

    private AuthUtil() {}

    /**
     * Checks if a valid user session exists.
     * Redirects to login page if session is missing or expired.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @return true if a valid session exists, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean requireLogin(HttpServletRequest req,
                                        HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?error=session_expired");
            return false;
        }
        return true;
    }

    /**
     * Validates that the logged-in user possesses one of the allowed roles.
     * Redirects to login with Access Denied message on failure.
     * Per Module 1: unauthorised access must redirect to login with Access Denied.
     *
     * @param req          the HTTP request
     * @param resp         the HTTP response
     * @param allowedRoles one or more roles permitted to access the resource
     * @return true if user has a permitted role, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean checkRole(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     String... allowedRoles) throws IOException {
        if (!requireLogin(req, resp)) return false;
        String userRole = (String) req.getSession().getAttribute("userRole");
        for (String role : allowedRoles) {
            if (role.equals(userRole)) return true;
        }
        resp.sendRedirect(req.getContextPath() + "/auth?error=access_denied");
        return false;
    }

    /**
     * Alias for checkRole — used by servlets that call requireRole(...).
     * Identical behaviour to checkRole.
     *
     * @param req          the HTTP request
     * @param resp         the HTTP response
     * @param allowedRoles one or more roles permitted to access the resource
     * @return true if user has a permitted role, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean requireRole(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       String... allowedRoles) throws IOException {
        return checkRole(req, resp, allowedRoles);
    }

    /**
     * Restricts access to SUPPLIER role only.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @return true if user is a SUPPLIER, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean requireSupplier(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);

        if (session == null) {
            System.out.println("requireSupplier: Session is null");
            resp.sendRedirect(req.getContextPath() + "/auth?error=session_expired");
            return false;
        }

        Object userId = session.getAttribute("userId");
        if (userId == null) {
            System.out.println("requireSupplier: userId is null");
            resp.sendRedirect(req.getContextPath() + "/auth?error=not_logged_in");
            return false;
        }

        Object role = session.getAttribute("userRole");
        if (role == null) {
            System.out.println("requireSupplier: role is null");
            resp.sendRedirect(req.getContextPath() + "/auth?error=no_role");
            return false;
        }

        if (!"SUPPLIER".equalsIgnoreCase(role.toString())) {
            System.out.println("requireSupplier: role is " + role + ", not SUPPLIER");
            resp.sendRedirect(req.getContextPath() + "/auth?error=access_denied");
            return false;
        }

        return true;
    }

    /**
     * Restricts access to OFFICER role only.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @return true if user is an OFFICER, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean requireOfficer(HttpServletRequest req,
                                          HttpServletResponse resp) throws IOException {
        return checkRole(req, resp, AppConstants.ROLE_OFFICER);
    }

    /**
     * Restricts access to EVALUATOR or OFFICER roles.
     * Officers also participate in evaluation per the exam scenario.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @return true if user is an EVALUATOR or OFFICER, false if redirected
     * @throws IOException if redirect fails
     */
    public static boolean requireEvaluator(HttpServletRequest req,
                                            HttpServletResponse resp) throws IOException {
        return checkRole(req, resp, AppConstants.ROLE_EVALUATOR, AppConstants.ROLE_OFFICER);
    }

    /**
     * Returns the logged-in user's ID from the current session.
     *
     * @param req the HTTP request
     * @return userId as int, or -1 if session is invalid
     */
    public static int getSessionUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return -1;
        Object id = session.getAttribute("userId");
        return id == null ? -1 : (Integer) id;
    }

    /**
     * Returns the logged-in user's role from the current session.
     *
     * @param req the HTTP request
     * @return role string or null if session is invalid
     */
    public static String getSessionRole(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("userRole");
    }

    /**
     * Returns the logged-in user's full name from the current session.
     *
     * @param req the HTTP request
     * @return full name string or null if session is invalid
     */
    public static String getSessionUserName(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("userName");
    }
}