-- ============================================================
-- QUICK SETUP - Admin User Creation
-- ============================================================
-- Just copy and paste this entire file into MySQL Workbench or
-- your MySQL client and execute it!
-- ============================================================

-- Connect to your database
USE exportation_panelera;

-- Show current users (before)
SELECT '=== BEFORE: Current users ===' AS '';
SELECT id, username, is_active, created_at FROM users;

-- Create the admin user
-- Username: admin
-- Password: admin123 (change this later for production!)
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',  -- This is BCrypt hash for "admin123"
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE
    password_hash = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',
    is_active = 1,
    updated_at = NOW();

-- Show result
SELECT '=== SUCCESS! Admin user created ===' AS '';
SELECT id, username, is_active, created_at FROM users WHERE username = 'admin';

-- Display login credentials
SELECT '' AS '';
SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║     🎉 SETUP COMPLETE!                ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  Username: admin                       ║' AS '';
SELECT '║  Password: admin123                    ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  ⚠️  IMPORTANT:                        ║' AS '';
SELECT '║  Change password for production!       ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';
