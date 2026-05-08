# ProcureGov - Tender Management System

**Version:** 1.0
**Last Updated:** 2026-05-08

ProcureGov is a comprehensive, web-based Tender Management System designed for the Ministry of Public Works, Kingdom of Lesotho. It digitizes the complete tender lifecycle—from creation and submission to evaluation and contract awarding—replacing manual, paper-based processes with an efficient, secure digital workflow.

## Table of Contents

- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Predefined User Credentials](#predefined-user-credentials)
- [Installation Guide](#installation-guide)
  - [Prerequisites](#prerequisites)
  - [Automated Deployment (Recommended)](#automated-deployment-recommended)
  - [Manual Deployment](#manual-deployment)
- [Project Structure](#project-structure)
- [Troubleshooting Guide](#troubleshooting-guide)
- [Additional Notes](#additional-notes)

## Project Overview

### 1. Purpose
The system provides a centralized platform for procurement officers, evaluators, and suppliers to manage public tenders. It ensures transparency, reduces administrative overhead, and enforces security through role-based access and session management.

### 2. Key Features
- **SHA-256 Password Hashing** for secure user authentication.
- **Role-Based Access Control** (Procurement Officer, Evaluator, Supplier).
- **Complete Tender Lifecycle Management** (Create, Publish, Close, Award).
- **Supplier Bid Submission** with multi-file document upload.
- **Automated Bid Evaluation** with a configurable scoring system.
- **Contract Award Management** with digital documentation.
- **Email Notification System** for award results.
- **Professional Green-themed User Interface**.
- **Session Management** with timeout protection.
- **File Upload Validation** (Tenders: 5MB max, Bids: 10MB max).
- **Account Lockout** after 3 failed login attempts (15 minutes).

### 3. Target Audience
- Procurement officers and government tendering departments.
- Bid evaluators and technical committees.
- Registered suppliers and contractors.
- System administrators managing public procurement platforms.

## Technology Stack

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Frontend** | HTML5, CSS3, JavaScript, JSP | User interface, role-based views. |
| **Backend** | Java Servlets (v8+), JSP | Business logic, request handling, session management. |
| **Database** | MySQL (via XAMPP) | Persistent data storage. |
| **Web Server** | Apache Tomcat 8.5+ (via XAMPP) | Servlet container and application deployment. |
| **Security** | SHA-256, Session-based Auth, RBAC | Authentication and access control. |
| **Libraries** | MySQL Connector/J, JavaMail API, FileUpload API | Database connectivity, email, file handling. |

## System Architecture
[ CLIENT BROWSER (Chrome, Firefox, Edge) ]
|
| HTTP/HTTPS
V
[ APACHE TOMCAT (PORT 8080) ]
(Web Server / Servlet Container)
|
V
[ JSP / SERVLET LAYER ]
(Controllers, Views, Session Management)
|
V
[ BUSINESS LOGIC LAYER ]
(TenderService, BidService, EvaluationService, EmailService)
|
V
[ DAO LAYER ]
(Data Access Objects, JDBC Connectivity)
|
| JDBC
V
[ MYSQL DATABASE (PORT 3306) ]
(Users, Tenders, Bids, Evaluation_Scores, Awards)


## Predefined User Credentials

**Password for all pre-defined accounts:** `123456`

| Role | Email | Company / Designation |
| :--- | :--- | :--- |
| **PROCUREMENT OFFICER** | `kabelo@gmail.com` | Ministry of Public Works |
| **PROCUREMENT OFFICER** | `tankiso@gmail.com` | Ministry of Public Works |
| **EVALUATOR** | `sechaba@gmail.com` | ICT Directorate |
| **EVALUATOR** | `kabelo@gmail.com` (Note: same email, different role & ID) | ICT Directorate |

> **Permissions:**
> - **Procurement Officer:** Create, publish, close tenders, manage awards.
> - **Evaluator:** Evaluate bids, assign scores.
> - **Supplier:** Submit bids, view tenders, check award status.

## Installation Guide

### Prerequisites

- **XAMPP** (with MySQL and Tomcat)
- **MySQL** 5.7 or higher
- **Apache Tomcat** 8.5 or higher
- **JDK** 8 or higher (Java Development Kit)
- A modern web browser (Chrome, Firefox, Edge)

### Automated Deployment (Recommended)

1. **Locate the automated script:** `run.bat` in the project root directory.
2. **Run as Administrator:** Right-click on `run.bat` and select **"Run as Administrator"**.
3. The script will automatically:
   - Check the MySQL connection.
   - Create and populate the database.
   - Deploy the WAR file to Tomcat.
   - Start the Tomcat server.
   - Open the application in your browser.

> **Note:** If you see a "MySQL is not running" error, proceed to the manual deployment method below.

### Manual Deployment

#### Step 1: Set Up the Database
1. Open XAMPP Control Panel. Click **Start** next to **MySQL**.
2. Open your browser and go to: `http://localhost/phpmyadmin`
3. Click the **Import** tab.
4. Click **Choose File** and select: `database/schema.sql`
5. Click **Go** at the bottom. You should see: *"Import has been successfully finished"*.

#### Step 2: Deploy the WAR File
1. Locate the WAR file: `dist/procuregov.war`
2. **Copy** the WAR file to: `C:\XAMPP\tomcat\webapps\`
3. Tomcat will auto-deploy the application within a few seconds if it is running.

#### Step 3: Start Tomcat
1. Open XAMPP Control Panel.
2. Click **Start** next to **Tomcat**. Verify it shows **"Running"** in green.

#### Step 4: Access the Application
1. Open a web browser.
2. Navigate to: `http://localhost:8080/procuregov/`
3. Click **Sign In** and use the credentials from the [Predefined User Credentials](#predefined-user-credentials) section.

## Project Structure
ProcureGov/
│
├── database/ # SQL schema file
│ └── schema.sql
│
├── nbproject/ # NetBeans project files
│
├── src/ # Java source code
│ └── com/procuregov/
│ ├── model/ # User, Tender, Bid, Award, EvaluationScore
│ ├── dao/ # Data Access Objects & implementations
│ ├── service/ # TenderService, BidService, EvaluationService, EmailService
│ ├── servlet/ # AuthServlet, TenderServlet, BidServlet, EvaluationServlet
│ └── util/ # DBConnectionPool, PasswordHasher, AuthUtil, FileUploader
│
├── test/ # Unit tests
│
├── upload/ # Uploaded documents
│ ├── tenders/ # Tender document uploads
│ └── bids/ # Bid document uploads
│
├── web/ # Web application root
│ ├── WEB-INF/
│ │ ├── lib/ # Required JAR files
│ │ ├── jsp/ # All JSP files organized by role
│ │ └── web.xml # Deployment descriptor
│ ├── css/ # Stylesheets
│ └── index.jsp # Welcome page
│
├── build/ # Compiled classes and built artifacts
├── dist/ # WAR file location
└── README.md # This file

## Troubleshooting Guide

| Issue | Solution |
| :--- | :--- |
| **MySQL is not running or accessible** | - Open XAMPP Control Panel as Administrator and start MySQL.<br>- If port 3306 is in use, change MySQL port to 3307 in `my.ini`.<br>- Try the [manual deployment method](#manual-deployment). |
| **Port 8080 already in use** | - Stop other Tomcat instances: `taskkill /F /IM java.exe`<br>- Or change Tomcat port in `conf/server.xml` from 8080 to 8081, then access: `http://localhost:8081/procuregov/` |
| **Database connection error** | - Verify MySQL is running in XAMPP.<br>- Check `context.xml` at `C:\xampp\tomcat\conf\Catalina\localhost\`.<br>- Confirm database name is `procuregov` and the port (3306 or 3307) matches. |
| **File upload fails** | - Check upload directories exist: `uploads/tenders/` and `uploads/bids/`.<br>- Verify folder write permissions.<br>- Check file size: Tenders < 5MB, Bids < 10MB.<br>- Allowed formats: `.pdf`, `.doc`, `.docx`, `.jpg`, `.png`. |
| **Login fails** | - Check that CAPS LOCK is off.<br>- Default password is `123456`.<br>- After 3 failed attempts, the account locks for 15 minutes.<br>- Clear browser cache and cookies. |
| **JSP compilation errors** | - Clean and build the project in NetBeans.<br>- Verify all JAR files are in `WEB-INF/lib/`.<br>- Delete and redeploy the WAR file.<br>- Restart Tomcat completely. |
| **Email notifications not sending** | - Check your internet connection.<br>- The email service uses configured SMTP (informational only; the system works without it).<br>- Check your spam/junk folder. |

## Additional Notes

- The automated `run.bat` script **must be run as Administrator**.
- If MySQL is on port 3307, modify `run.bat` by adding `--port=3307` to the MySQL commands.
- Default Tomcat port is `8080`. Ensure no other service uses this port.
- All uploaded files are stored in the `uploads` directory.
- The database name is `procuregov`.
- The system uses **connection pooling** for database efficiency.
- For production deployment, change default passwords and configure a secure SMTP server for email notifications.
