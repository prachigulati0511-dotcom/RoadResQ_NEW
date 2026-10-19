-- RoadResQ Relational Database Schema (MySQL 8.0 & H2 compatible)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    license_plate VARCHAR(30) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    color VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS provider_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    service_types VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    is_online BOOLEAN DEFAULT TRUE,
    rating DOUBLE DEFAULT 4.9,
    total_reviews INT DEFAULT 18,
    active_vehicle_plate VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS emergency_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    assigned_provider_id BIGINT,
    service_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    address VARCHAR(255) NOT NULL,
    vehicle_info VARCHAR(255),
    problem_description TEXT,
    photo_url VARCHAR(255),
    base_fee DECIMAL(10,2) NOT NULL,
    travel_fee DECIMAL(10,2) NOT NULL,
    hazard_fee DECIMAL(10,2) DEFAULT 0.00,
    total_estimated_cost DECIMAL(10,2) NOT NULL,
    revised_quote DECIMAL(10,2),
    revised_quote_reason VARCHAR(255),
    quote_status VARCHAR(30) DEFAULT 'ORIGINAL',
    consent_contacts BOOLEAN DEFAULT FALSE,
    cancel_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (assigned_provider_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(100),
    relationship VARCHAR(50) NOT NULL,
    notify_by_default BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rating_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    stars INT NOT NULL,
    tags VARCHAR(255),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES emergency_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (provider_id) REFERENCES users(id)
);
