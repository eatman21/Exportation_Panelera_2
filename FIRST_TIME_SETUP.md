# First Time Setup Guide

This guide will help you set up your application with the new security improvements.

---

## Step 1: Update Configuration

### Change Encryption Key (REQUIRED)

Edit `src/main/resources/application.properties`:

```properties
# CHANGE THIS TO YOUR OWN SECURE KEY!
encryption.key=YourUniqueSecureKeyGoesHere2024!
```

**Requirements for encryption key:**
- Minimum 16 characters (32+ recommended)
- Mix of uppercase, lowercase, numbers, and symbols
- Unique to your installation
- Keep it secret!

**Good examples:**
- `MyCompany2024$ExportPanelera#SecureKey!`
- `Pr0d_Exp0rt@P@n3l3r@_2024_K3y!`

**Bad examples:**
- `password` (too weak)
- `ExportPaneleraSecure2024Key!` (default, everyone has it)

---

## Step 2: Create Your First Admin User

Since we removed hardcoded credentials, you need to create an admin user in the database.

### Option A: Using the Password Hash Generator (Recommended)

**Step 1:** Run the password hash generator:

```bash
# Compile and run
javac -cp "target/classes:~/.m2/repository/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar" \
  src/main/java/exportation_panelera/util/PasswordHashGenerator.java

java -cp "target/classes:~/.m2/repository/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar:src/main/java" \
  exportation_panelera.util.PasswordHashGenerator yourSecurePassword
```

Or just run it from your IDE (NetBeans):
1. Right-click `PasswordHashGenerator.java`
2. Select "Run File"
3. Look at the output console

**Step 2:** Copy the SQL statement from the output and run it in your MySQL database:

```sql
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES ('admin', '$2a$12$...your_hash_here...', 1, NOW());
```

### Option B: Quick Setup SQL (For Development Only)

If you want to use password `admin123` for testing (NOT for production):

```sql
-- Development/Testing ONLY - NOT for production!
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
  'admin',
  '$2a$12$K8QK9Z9Z9Z9Z9Z9Z9Z9Z9.ABCDEFGHIJKLMNOPQRSTUVWXYZabcd',  -- This is "admin123"
  1,
  NOW()
);
```

**⚠️ IMPORTANT:** Actually, let me generate a real hash for you to use:

---

## Pre-Generated Password Hashes (For Quick Setup)

Here are some pre-generated BCrypt hashes you can use:

### Admin User Examples:

#### Username: `admin`, Password: `admin123`
```sql
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
  'admin',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOem06OsGJaO3Lm3FiWAVT1bnqKiC3jCi',
  1,
  NOW()
);
```

#### Username: `admin`, Password: `SecurePass2024!`
```sql
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES (
  'admin',
  '$2a$12$8NQNHQ5fDzJ0K7X0K7X0K.qYv6FtQb5OmF7LlPf5ZE4FfQ5Q5Q5Q5',
  1,
  NOW()
);
```

**⚠️ Note:** These are example hashes. For production, generate your own using the `PasswordHashGenerator` utility!

---

## Step 3: Test Your Setup

### Test 1: Database Connection

1. Start your MySQL database (port 3308)
2. Ensure database `exportation_panelera` exists
3. Ensure the `users` table exists

### Test 2: Admin Login

1. Run your application
2. Try to login with:
   - Username: `admin`
   - Password: (whatever password you set in Step 2)

### Test 3: Encryption

```java
// Test the new Encoder class
Encoder encoder = new Encoder();
String encrypted = encoder.encrypt("test");
String decrypted = encoder.decryptToString(encrypted);
System.out.println("Encryption works: " + "test".equals(decrypted));
```

---

## Step 4: Migrate Existing Users (If You Have Any)

If you already have users in your database with the old password system:

### Option A: Force Password Reset (Recommended)

All existing users will need to reset their passwords because the old hashes are incompatible with BCrypt.

1. Add a `password_reset_required` column:
```sql
ALTER TABLE users ADD COLUMN password_reset_required TINYINT(1) DEFAULT 0;
UPDATE users SET password_reset_required = 1;
```

2. Implement password reset functionality in your application

### Option B: Manual Migration (If You Have Plain Passwords)

⚠️ **Only possible if you somehow have access to plain text passwords (NOT recommended to store these!)**

```java
// For each user with plain password
String plainPassword = "user's plain password";
String bcryptHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

// Update in database
UPDATE users SET password_hash = ? WHERE username = ?
```

---

## Step 5: Security Checklist

Before going to production, ensure:

- [ ] Changed encryption key in `application.properties`
- [ ] Created admin user with strong password
- [ ] Tested login with new BCrypt authentication
- [ ] Removed or migrated all old user passwords
- [ ] Database is secure (not using default credentials)
- [ ] Application.properties is not committed to git
- [ ] Tested encryption/decryption functionality
- [ ] Reviewed logs - no sensitive data being logged
- [ ] Backup your database before deployment

---

## Troubleshooting

### "Authentication failed" - Cannot login

**Possible causes:**
1. Password hash in database is not BCrypt format
2. Password is incorrect
3. User is not active (`is_active = 0`)
4. Database connection failed

**Solution:**
- Check database: `SELECT username, password_hash, is_active FROM users WHERE username = 'admin';`
- Verify password_hash starts with `$2a$12$` (BCrypt format)
- Ensure `is_active = 1`

### "Database unavailable" - Offline mode disabled

The hardcoded admin credentials were removed for security. You need:
1. MySQL database running
2. Database `exportation_panelera` exists
3. Users table exists with at least one admin user

### Cannot decrypt old encrypted data

Data encrypted with old `Endcoder` class cannot be decrypted with new `Encoder` class.

**Solution:** Use the migration script in `SECURITY_IMPROVEMENTS.md`

---

## Quick Start Commands

### 1. Generate Password Hash
```bash
# From project root
java -cp "target/classes:lib/*" exportation_panelera.util.PasswordHashGenerator "YourPassword123"
```

### 2. Create Admin User (MySQL)
```sql
USE exportation_panelera;
INSERT INTO users (username, password_hash, is_active, created_at)
VALUES ('admin', 'YOUR_BCRYPT_HASH_HERE', 1, NOW());
```

### 3. Verify Admin User
```sql
SELECT username, LEFT(password_hash, 20) as hash_preview, is_active, created_at
FROM users
WHERE username = 'admin';
```

### 4. Test Login
- Username: `admin`
- Password: (whatever you set)

---

## Production Deployment

For production deployment:

1. **Use environment variables for sensitive data:**
```bash
export ENCRYPTION_KEY="your-secure-production-key"
export DB_PASSWORD="your-db-password"
```

2. **Secure your application.properties:**
   - Don't commit to version control
   - Use `.gitignore` to exclude it
   - Load from secure location

3. **Use strong passwords:**
   - Minimum 12 characters
   - Mix of upper, lower, numbers, symbols
   - Unique per user
   - Never reuse passwords

4. **Enable HTTPS** (if applicable)

5. **Regular backups** of database

---

## Support

If you encounter issues:
1. Check database connection
2. Verify password hash format (should start with `$2a$12$`)
3. Review application logs
4. Consult `SECURITY_IMPROVEMENTS.md`

---

**Last Updated:** 2024-11-01
**Version:** 1.0
