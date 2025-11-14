-- Create deliveries table
CREATE TABLE IF NOT EXISTS deliveries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    delivery_id VARCHAR(50) UNIQUE,
    exportation_id VARCHAR(50),
    carrier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    delivery_address TEXT,
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    delivery_date DATE,
    status VARCHAR(50),
    notes TEXT,
    shipping_method VARCHAR(50),
    shipping_cost DECIMAL(10,2),
    shipping_currency VARCHAR(3),
    reference_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exportation_id (exportation_id),
    INDEX idx_status (status),
    INDEX idx_delivery_date (delivery_date),
    INDEX idx_tracking_number (tracking_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
