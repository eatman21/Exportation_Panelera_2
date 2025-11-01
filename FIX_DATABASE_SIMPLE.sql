-- ============================================================
-- SIMPLE DATABASE FIX (Alternative Method)
-- ============================================================
-- If the other script has issues, use this simpler version
-- This recreates the users table from scratch
-- ============================================================

USE exportation_panelera;

-- Backup: Show existing users (if any)
SELECT '=== Existing users (BACKUP - write these down!) ===' AS '';
SELECT * FROM users;

-- Drop and recreate the users table
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert admin user
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',  -- Password: admin123
    1,
    NOW()
);

-- Verify
SELECT '=== ✅ SUCCESS! Table recreated and admin user added ===' AS '';
SELECT id, username, LEFT(password_hash, 30) as hash_preview, is_active, created_at
FROM users;

SELECT '' AS '';
SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║     ✅ READY TO LOGIN!                 ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  Username: admin                       ║' AS '';
SELECT '║  Password: admin123                    ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';
