-- Create Database
CREATE DATABASE IF NOT EXISTS home_insurance;
USE home_insurance;

-- To show Databases
show databases;

-- To show Tables in the tables from the database selected
show tables;

-- Users Table
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,            -- Updated as per feedback
    email VARCHAR(254) UNIQUE NOT NULL,          -- Updated as per feedback
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NULL,                   -- Added audit field
    updated_by VARCHAR(100)                      -- Added audit field
);

-- Customers Table (Risk Factor included)(i added it thought of useful for review claims)
CREATE TABLE customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    age INT NOT NULL,
    phone_number VARCHAR(25) NOT NULL,           -- Updated as per feedback
    address TEXT NOT NULL,
    risk_factor ENUM('LOW','MEDIUM','HIGH'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NULL,                   -- Added audit field
    updated_by VARCHAR(100),                     -- Added audit field
    FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE                        -- Added as per feedback
);


-- Properties Table (Mandatory for Policy Mgmt)
CREATE TABLE properties (
    property_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    property_type ENUM('APARTMENT','HOUSE') NOT NULL,
    built_up_area INT NOT NULL,
    construction_year INT NOT NULL,
    property_value DECIMAL(15,2) NOT NULL,       -- Updated as per feedback

    -- Structured address (as suggested)
    building_no VARCHAR(50),
    street VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(10),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NULL,                   -- Added audit field
    updated_by VARCHAR(100),                     -- Added audit field

    FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE CASCADE                        -- Added as per feedback
);


-- Policies Table -- (Linked to Property, not Customer)
CREATE TABLE policies (
    policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_number VARCHAR(50) UNIQUE NOT NULL,   -- Added as per feedback
    property_id BIGINT NOT NULL,
    policy_type VARCHAR(50) NOT NULL,
    premium_amount DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(12,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE','EXPIRED','CANCELLED'),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NULL,                   -- Added audit field
    updated_by VARCHAR(100),                     -- Added audit field

    FOREIGN KEY (property_id)
        REFERENCES properties(property_id)
        ON DELETE CASCADE                        -- Added as per feedback
);


-- Claims Table (Admin Review)
CREATE TABLE claims (
    claim_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_number VARCHAR(50) UNIQUE NOT NULL,    -- Added as per feedback
    policy_id BIGINT NOT NULL,
    incident_type VARCHAR(100) NOT NULL,
    incident_date DATE NOT NULL,
    estimated_loss DECIMAL(12,2) NOT NULL,
    claim_status ENUM('SUBMITTED','IN_REVIEW','APPROVED','REJECTED'),
    admin_remarks TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NULL,                   -- Added audit field
    updated_by VARCHAR(100),                     -- Added audit field

    FOREIGN KEY (policy_id)
        REFERENCES policies(policy_id)
        ON DELETE CASCADE                        -- Added as per feedback
);


-- Payments Table (Added as per feedback to track premium payments)
CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(30),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (policy_id)
        REFERENCES policies(policy_id)
        ON DELETE CASCADE
);

