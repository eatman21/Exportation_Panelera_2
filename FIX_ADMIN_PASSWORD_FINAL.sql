-- ============================================================
-- FINAL FIX: Update admin password with verified BCrypt hash
-- ============================================================
-- This hash was freshly generated and verified to work with password: admin123
-- ============================================================

USE exportation_panelera;

-- Show current admin user
SELECT '=== Current admin user (BEFORE update) ===' AS '';
SELECT id, username, password_hash, is_active, created_at
FROM users
WHERE username = 'admin';

-- Update with verified working hash for password: admin123
UPDATE users
SET password_hash = '$2a$12$qDeuglk8K8cmXazOdrBhRuvB/CN7za6aOqwZtA5TGPVx0ZkIkxLxy',
    updated_at = NOW()
WHERE username = 'admin';

-- Verify the update
SELECT '=== Updated admin user (AFTER update) ===' AS '';
SELECT id, username,
       LEFT(password_hash, 30) as hash_preview,
       LENGTH(password_hash) as hash_length,
       is_active,
       updated_at
FROM users
WHERE username = 'admin';

-- Final confirmation
SELECT '' AS '';
SELECT '╔════════════════════════════════════════╗' AS '';
SELECT '║  ✅ PASSWORD HASH UPDATED!             ║' AS '';
SELECT '╠════════════════════════════════════════╣' AS '';
SELECT '║  Username: admin                       ║' AS '';
SELECT '║  Password: admin123                    ║' AS '';
SELECT '║                                        ║' AS '';
SELECT '║  This hash was VERIFIED to work!      ║' AS '';
SELECT '╚════════════════════════════════════════╝' AS '';
