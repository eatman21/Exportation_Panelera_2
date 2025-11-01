-- ============================================================
-- FIX DATABASE SCHEMA
-- ============================================================
-- This script fixes the users table to work with BCrypt hashing
-- Run this in MySQL Workbench or any MySQL client
-- ============================================================

USE exportation_panelera;

-- Step 1: Show current table structure
SELECT '=== Current users table structure ===' AS '';
DESCRIBE users;

-- Step 2: Check if password_hash column exists
SELECT '=== Checking for password_hash column ===' AS '';
SELECT COUNT(*) as has_password_hash_column
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'exportation_panelera'
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'password_hash';

-- Step 3: Add password_hash column if it doesn't exist
-- (This will fail silently if column already exists - that's OK)
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER username;',
        'SELECT "password_hash column already exists" AS message;'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'exportation_panelera'
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'password_hash'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 4: If there's an old 'password' column, we need to handle it
-- Check if old password column exists
SELECT '=== Checking for old password column ===' AS '';
SELECT COUNT(*) as has_old_password_column
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'exportation_panelera'
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'password';

-- Step 5: Make password_hash NOT NULL after we add data
-- (We'll do this later after adding the admin user)

-- Step 6: Add other missing columns if needed
-- Add is_active if it doesn't exist
ALTER TABLE users
ADD COLUMN IF NOT EXISTS is_active TINYINT(1) DEFAULT 1;

-- Add created_at if it doesn't exist
ALTER TABLE users
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at if it doesn't exist
ALTER TABLE users
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Add last_login if it doesn't exist
ALTER TABLE users
ADD COLUMN IF NOT EXISTS last_login TIMESTAMP NULL;

-- Step 7: Show updated table structure
SELECT '=== Updated users table structure ===' AS '';
DESCRIBE users;

-- Step 8: Clean up any test data and insert admin user
-- Delete existing admin if present
DELETE FROM users WHERE username = 'admin';

-- Insert new admin user with BCrypt hash
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',  -- Password: admin123
    1,
    NOW()
);

-- Step 9: Verify
SELECT '=== SUCCESS! Admin user created ===' AS '';
SELECT id, username, LEFT(password_hash, 30) as password_hash_preview, is_active, created_at
FROM users
WHERE username = 'admin';

-- Step 10: Show login credentials
SELECT '' AS '';
SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║     ✅ DATABASE FIXED!                 ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  Username: admin                       ║' AS '';
SELECT '║  Password: admin123                    ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  The users table now has:              ║' AS '';
SELECT '║  - password_hash column (BCrypt)       ║' AS '';
SELECT '║  - is_active column                    ║' AS '';
SELECT '║  - created_at, updated_at columns      ║' AS '';
SELECT '║  - last_login column                   ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';

-- Done!
SELECT '=== All users in database ===' AS '';
SELECT id, username, is_active, created_at FROM users;
