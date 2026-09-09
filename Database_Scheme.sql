-- Create Database
CREATE DATABASE IF NOT EXISTS home_insurance;
USE home_insurance;

-- Users Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers Table (Risk Factor included)(i added it thought of useful for review claims)
CREATE TABLE customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    age INT NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    risk_factor ENUM('LOW','MEDIUM','HIGH'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- Properties Table (Mandatory for Policy Mgmt)
CREATE TABLE properties (
    property_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    property_type ENUM('APARTMENT','HOUSE') NOT NULL,
    built_up_area INT NOT NULL,
    construction_year INT NOT NULL,
    property_value DECIMAL(12,2) NOT NULL,
    property_address TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);


-- Policies Table -- (Linked to Property, not Customer)
CREATE TABLE policies (
    policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL,
    policy_type VARCHAR(50) NOT NULL,
    premium_amount DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(12,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE','EXPIRED','CANCELLED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(property_id)
);


-- Claims Table (Admin Review)
CREATE TABLE claims (
    claim_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    incident_type VARCHAR(100) NOT NULL,
    incident_date DATE NOT NULL,
    estimated_loss DECIMAL(12,2) NOT NULL,
    claim_status ENUM('SUBMITTED','IN_REVIEW','APPROVED','REJECTED'),
    admin_remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (policy_id) REFERENCES policies(policy_id)
);