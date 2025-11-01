-- ============================================================
-- Setup Admin User for Exportacion Panelera
-- ============================================================
-- This script creates an initial admin user with BCrypt-hashed password
-- Run this AFTER the users table has been created
--
-- IMPORTANT: Change the password hash before running in production!
-- ============================================================

USE exportation_panelera;

-- Check if admin user already exists
SELECT 'Checking for existing admin user...' AS '';
SELECT COUNT(*) as existing_admin_count FROM users WHERE username = 'admin';

-- Delete existing admin if you want to recreate (OPTIONAL - comment out if not needed)
-- DELETE FROM users WHERE username = 'admin';

-- Create admin user with BCrypt-hashed password
-- Default password: "admin123" (FOR DEVELOPMENT/TESTING ONLY!)
-- BCrypt hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi

INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',  -- Password: "admin123"
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    is_active = 1,
    updated_at = NOW();

-- Verify the admin user was created
SELECT 'Admin user created successfully!' AS '';
SELECT
    id,
    username,
    LEFT(password_hash, 30) as password_hash_preview,
    is_active,
    created_at
FROM users
WHERE username = 'admin';

SELECT '' AS '';
SELECT '============================================' AS '';
SELECT 'IMPORTANT: Login credentials' AS '';
SELECT '============================================' AS '';
SELECT 'Username: admin' AS '';
SELECT 'Password: admin123' AS '';
SELECT '' AS '';
SELECT 'WARNING: Change this password immediately after first login!' AS '';
SELECT 'This is a default password for setup only.' AS '';
SELECT '============================================' AS '';

-- ============================================================
-- OPTIONAL: Create additional users
-- ============================================================

-- Example: Create a regular user
-- Uncomment and modify as needed
/*
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
    'user1',
    '$2a$12$YourBCryptHashHere',  -- Generate using PasswordHashGenerator
    1,
    NOW()
);
*/

-- ============================================================
-- PRODUCTION NOTES:
-- ============================================================
-- For production deployment:
-- 1. Generate a strong password using PasswordHashGenerator utility
-- 2. Replace the password_hash above with your generated hash
-- 3. Never use default passwords like "admin123" in production
-- 4. Consider adding additional security measures:
--    - Multi-factor authentication
--    - Password expiration policies
--    - Account lockout policies
-- ============================================================
