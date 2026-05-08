package com.procuregov.servlet;

import com.procuregov.util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * Secure file download handler for tender notices and bid documents.
 * 
 * Exam Compliance:
 * - Module 2: "tender notice PDF must be stored on the server filesystem and served back 
 *   through a dedicated download Servlet — not by exposing the file path directly"
 * - Module 5: All files stored outside WAR directory for security
 * - Authentication required before downloading any file
 * - Prevents directory traversal attacks
 */
public class FileDownloadServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(FileDownloadServlet.class.getName());
    private String uploadBasePath;

    @Override
    public void init() throws ServletException {
        // Get upload path from web.xml context-param (Module 2 requirement: store outside WAR)
        uploadBasePath = getServletContext().getInitParameter("upload.base.path");
        if (uploadBasePath == null || uploadBasePath.isEmpty()) {
            logger.severe("[FileDownloadServlet.init] upload.base.path not configured in web.xml");
            // Fallback for development only - DO NOT use in production
            uploadBasePath = System.getProperty("catalina.base") + File.separator + "uploads";
        }
        logger.info("[FileDownloadServlet.init] Upload base path: " + uploadBasePath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Module 1: Authentication required for all file downloads
        if (!AuthUtil.requireLogin(req, resp)) {
            logger.warning("[FileDownloadServlet] Unauthenticated download attempt");
            return;
        }
        
        // Get file parameter (supports both "file" and "path" for compatibility)
        String filePath = req.getParameter("file");
        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = req.getParameter("path");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            logger.warning("[FileDownloadServlet] Missing file parameter");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file parameter");
            return;
        }
        
        // Normalize file path (remove any leading slashes or backslashes)
        String normalizedPath = filePath.trim().replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        
        logger.info("[FileDownloadServlet] Requested file: " + normalizedPath);
        
        // Security: Prevent directory traversal attacks
        if (normalizedPath.contains("..")) {
            logger.warning("[FileDownloadServlet] Directory traversal attempt: " + normalizedPath);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file path");
            return;
        }
        
        // Build the full file path
        File baseDir = new File(uploadBasePath);
        File file = new File(baseDir, normalizedPath);
        
        // Security: Ensure the file path is within the upload directory
        String canonicalFile = file.getCanonicalPath();
        String canonicalBase = baseDir.getCanonicalPath();
        if (!canonicalFile.startsWith(canonicalBase)) {
            logger.warning("[FileDownloadServlet] Path traversal attempt: " + canonicalFile);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Check if file exists and is readable
        if (!file.exists() || !file.isFile()) {
            logger.warning("[FileDownloadServlet] File not found: " + canonicalFile);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }
        
        if (!file.canRead()) {
            logger.warning("[FileDownloadServlet] File not readable: " + canonicalFile);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "File cannot be read");
            return;
        }
        
        // Determine MIME type based on file extension
        String mimeType = getMimeType(file.getName());
        resp.setContentType(mimeType);
        
        // Set Content-Disposition header (inline for PDF, attachment for others)
        String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("%28", "(")
                .replaceAll("%29", ")");
        
        if ("application/pdf".equals(mimeType)) {
            resp.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        } else {
            resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        }
        
        resp.setContentLengthLong(file.length());
        
        // Stream the file to the client
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            int totalBytes = 0;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            logger.info("[FileDownloadServlet] Successfully downloaded: " + normalizedPath + 
                       " (" + totalBytes + " bytes)");
            os.flush();
            
        } catch (IOException e) {
            logger.severe("[FileDownloadServlet] Error streaming file: " + e.getMessage());
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading file");
            }
        }
    }
    
    /**
     * Determines MIME type based on file extension.
     * @param fileName the name of the file
     * @return the MIME type string
     */
    private String getMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        
        String lowerName = fileName.toLowerCase();
        
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Handles HEAD requests to check file existence without downloading.
     * @param req the HTTP request
     * @param resp the HTTP response
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        if (!AuthUtil.requireLogin(req, resp)) {
            return;
        }
        
        String filePath = req.getParameter("file");
        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = req.getParameter("path");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String normalizedPath = filePath.trim().replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        
        if (normalizedPath.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        File baseDir = new File(uploadBasePath);
        File file = new File(baseDir, normalizedPath);
        
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String mimeType = getMimeType(file.getName());
        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}