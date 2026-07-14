-- Create Database
CREATE DATABASE IF NOT EXISTS pocketdepthai_db;
USE pocketdepthai_db;

-- Create Patients Table
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Tooth Measurements Table
CREATE TABLE IF NOT EXISTS tooth_measurements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(255) NOT NULL,
    tooth_number INT NOT NULL,
    values_json VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_name) REFERENCES patients(name) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create Users Table (Clinical Staff / Doctors)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create AI Training Data Table (Stochastic Gradient Descent training set)
CREATE TABLE IF NOT EXISTS ai_training_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mean_ppd FLOAT NOT NULL,
    mean_cal FLOAT NOT NULL,
    bop_percentage FLOAT NOT NULL,
    deep_pockets_count INT NOT NULL,
    diagnosis_class INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
