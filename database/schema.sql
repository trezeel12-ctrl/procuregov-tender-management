SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS procuregov
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE procuregov;

DROP TABLE IF EXISTS awards;
DROP TABLE IF EXISTS evaluation_scores;
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS tenders;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash CHAR(64) NOT NULL,
    role ENUM('SUPPLIER', 'OFFICER', 'EVALUATOR') NOT NULL,
    registration_number VARCHAR(20) UNIQUE NULL,
    physical_address TEXT NULL,
    contact_number VARCHAR(20) NULL,
    failed_login_attempts INT DEFAULT 0,
    last_failed_attempt TIMESTAMP NULL DEFAULT NULL,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_role (role),
    INDEX idx_email (email),
    INDEX idx_registration (registration_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tenders (
    tender_id INT AUTO_INCREMENT PRIMARY KEY,
    reference_no VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    category ENUM('Construction', 'Roads', 'Electrical', 'Plumbing', 'General Services') NOT NULL,
    description TEXT NOT NULL,
    estimated_value DECIMAL(12,2) NOT NULL,
    closing_datetime DATETIME NOT NULL,
    notice_file_path VARCHAR(255) NULL,
    status ENUM('DRAFT', 'OPEN', 'CLOSED', 'UNDER_EVALUATION', 'EVALUATED', 'AWARDED') DEFAULT 'DRAFT',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_closing (closing_datetime),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bids (
    bid_id INT AUTO_INCREMENT PRIMARY KEY,
    tender_id INT NOT NULL,
    supplier_id INT NOT NULL,
    bid_amount DECIMAL(12,2) NOT NULL,
    technical_statement VARCHAR(600) NOT NULL,
    proposed_timeline_days INT NOT NULL,
    supporting_doc_path VARCHAR(255) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tender_id) REFERENCES tenders(tender_id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    UNIQUE KEY unique_supplier_tender (tender_id, supplier_id),
    INDEX idx_tender (tender_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE evaluation_scores (
    score_id INT AUTO_INCREMENT PRIMARY KEY,
    bid_id INT NOT NULL,
    evaluator_id INT NOT NULL,
    price_score DECIMAL(5,2) NOT NULL,
    technical_score DECIMAL(5,2) NOT NULL,
    timeline_score DECIMAL(5,2) NOT NULL,
    weighted_total DECIMAL(6,2) NOT NULL,
    comments TEXT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (bid_id) REFERENCES bids(bid_id) ON DELETE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    UNIQUE KEY unique_evaluator_bid (bid_id, evaluator_id),
    INDEX idx_bid (bid_id),
    INDEX idx_evaluator (evaluator_id),
    INDEX idx_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE awards (
    award_id INT AUTO_INCREMENT PRIMARY KEY,
    tender_id INT UNIQUE NOT NULL,
    winning_bid_id INT NOT NULL,
    awarded_value DECIMAL(12,2) NOT NULL,
    justification TEXT NOT NULL,
    award_date DATE NOT NULL,
    awarded_by INT NOT NULL,
    notification_sent BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (tender_id) REFERENCES tenders(tender_id) ON DELETE RESTRICT,
    FOREIGN KEY (winning_bid_id) REFERENCES bids(bid_id) ON DELETE RESTRICT,
    FOREIGN KEY (awarded_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_award_date (award_date),
    INDEX idx_awarded_by (awarded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (full_name, email, password_hash, role, registration_number, physical_address, contact_number, failed_login_attempts, last_login) VALUES
('Kabelo Setho', 'kabelo@gmail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'OFFICER', 'OFF-001', 'Ministry of Public Works, Maseru', '+266 2231 0001', 0, NULL),
('Tankiso Khula', 'tankiso@gmail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'OFFICER', 'OFF-002', 'Ministry of Public Works, Maseru', '+266 2231 0002', 0, NULL),
('Sechaba Mohale', 'sechaba@gmail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'EVALUATOR', 'EVA-001', 'ICT Directorate, Maseru', '+266 2231 0003', 0, NULL),
('Kabelo Nthu', 'kabelo@gmail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'EVALUATOR', 'EVA-002', 'ICT Directorate, Maseru', '+266 2231 0004', 0, NULL);

