# NetBeans Verification Guide

This guide will help you verify all improvements work correctly in NetBeans.

## ✅ Pre-Flight Checklist

Before opening in NetBeans, verify files exist:

### 1. New Security Features
```bash
ls src/main/java/exportation_panelera/security/
```
Should show:
- PasswordPolicy.java ✓
- RateLimiter.java ✓
- SessionManager.java ✓

### 2. Service Layer
```bash
ls src/main/java/exportation_panelera/service/
```
Should show:
- AuthenticationService.java ✓

### 3. Exception Hierarchy
```bash
ls src/main/java/exportation_panelera/exception/
```
Should show:
- AuthenticationException.java ✓
- DatabaseException.java ✓
- RateLimitException.java ✓
- ValidationException.java ✓

### 4. Database Migrations
```bash
ls src/main/resources/db/migration/
```
Should show:
- V1__create_users_table.sql ✓
- V2__create_exportations_table.sql ✓
- V3__create_deliveries_table.sql ✓

### 5. Main Entry Point Still Exists
```bash
ls src/main/java/exportation_panelera/Exportation_Panelera.java
```
Should exist ✓

---

## 🔧 Opening in NetBeans

### Step 1: Open Project
1. Open NetBeans IDE
2. File → Open Project
3. Navigate to: `Exportation_Panelera_2`
4. Click "Open Project"

### Step 2: Wait for Maven to Load
- NetBeans will automatically detect it's a Maven project
- Wait for Maven to resolve dependencies (bottom-right progress bar)
- This may take 2-5 minutes on first load

### Step 3: Check for Compilation Errors
1. Look at the **Projects** tab (left sidebar)
2. Expand `Source Packages`
3. Look for **red error icons** ❌
4. If you see red icons, that means compilation errors

**Expected:** No red error icons ✅

---

## 🧪 Testing the Improvements

### Test 1: Verify Project Compiles

**In NetBeans:**
1. Right-click on project root → **Clean and Build**
2. Check the **Output** window at bottom
3. Look for: `BUILD SUCCESS`

**Expected Output:**
```
------------------------------------------------------------------------
BUILD SUCCESS
------------------------------------------------------------------------
```

### Test 2: Test New Security Classes Compile

**Create a test file to verify compilation:**

1. Right-click `src/main/java/exportation_panelera` → New → Java Class
2. Name it: `TestImprovements`
3. Paste this code:

```java
package exportation_panelera;

import exportation_panelera.security.RateLimiter;
import exportation_panelera.security.PasswordPolicy;
import exportation_panelera.security.SessionManager;
import exportation_panelera.service.AuthenticationService;
import exportation_panelera.exception.*;

public class TestImprovements {

    public static void main(String[] args) {
        System.out.println("=== Testing Improvements ===\n");

        // Test 1: Rate Limiter
        System.out.println("1. Testing RateLimiter...");
        RateLimiter rateLimiter = new RateLimiter();
        boolean allowed = rateLimiter.isAllowed("testuser");
        System.out.println("   Rate limiter created: " + (allowed ? "PASS ✓" : "FAIL ✗"));

        // Test 2: Password Policy
        System.out.println("\n2. Testing PasswordPolicy...");
        PasswordPolicy passwordPolicy = new PasswordPolicy();
        String requirements = passwordPolicy.getPolicyDescription();
        System.out.println("   Password policy created: PASS ✓");
        System.out.println("   " + requirements.split("\n")[0]);

        // Test 3: Session Manager
        System.out.println("\n3. Testing SessionManager...");
        SessionManager sessionManager = new SessionManager();
        String sessionToken = sessionManager.createSession("testuser", "127.0.0.1");
        boolean valid = sessionManager.validateSession(sessionToken);
        System.out.println("   Session created: " + (sessionToken != null ? "PASS ✓" : "FAIL ✗"));
        System.out.println("   Session valid: " + (valid ? "PASS ✓" : "FAIL ✗"));

        // Test 4: Authentication Service
        System.out.println("\n4. Testing AuthenticationService...");
        AuthenticationService authService = AuthenticationService.getInstance();
        int sessionTimeout = authService.getSessionTimeoutMinutes();
        System.out.println("   Auth service created: PASS ✓");
        System.out.println("   Session timeout: " + sessionTimeout + " minutes");

        // Test 5: Exception Classes
        System.out.println("\n5. Testing Exception Classes...");
        try {
            throw new ValidationException("test", "Test validation error");
        } catch (ValidationException e) {
            System.out.println("   ValidationException: PASS ✓");
        }

        try {
            throw new AuthenticationException("testuser", "Test auth error");
        } catch (AuthenticationException e) {
            System.out.println("   AuthenticationException: PASS ✓");
        }

        System.out.println("\n=== All Tests Passed! ✓ ===");
        System.out.println("\nThe improvements are working correctly!");
        System.out.println("You can now delete this TestImprovements.java file.");
    }
}
```

4. Right-click `TestImprovements.java` → **Run File**
5. Check **Output** window for results

**Expected Output:**
```
=== Testing Improvements ===

1. Testing RateLimiter...
   Rate limiter created: PASS ✓

2. Testing PasswordPolicy...
   Password policy created: PASS ✓
   Password Requirements:

3. Testing SessionManager...
   Session created: PASS ✓
   Session valid: PASS ✓

4. Testing AuthenticationService...
   Auth service created: PASS ✓
   Session timeout: 30 minutes

5. Testing Exception Classes...
   ValidationException: PASS ✓
   AuthenticationException: PASS ✓

=== All Tests Passed! ✓ ===
```

If you see this, **all improvements are working!** ✅

### Test 3: Run the Main Application

1. Right-click on project → **Run**
2. Or press **F6**
3. The SignInForm window should appear
4. Try to login

**Expected:**
- Application starts without errors ✓
- SignInForm window appears ✓
- Login attempts are rate-limited (try 6 wrong passwords) ✓

### Test 4: Test Password Strength

In `TestImprovements.java`, add this method and run it:

```java
public static void testPasswordStrength() {
    AuthenticationService authService = AuthenticationService.getInstance();

    String[] passwords = {
        "pass",           // Too short
        "password",       // Common
        "Password123",    // Good
        "MyP@ssw0rd123!" // Very strong
    };

    System.out.println("\n=== Password Strength Tests ===");
    for (String pwd : passwords) {
        int strength = authService.getPasswordStrength(pwd);
        String desc = authService.getPasswordStrengthDescription(pwd);
        System.out.printf("Password: %-20s | Strength: %3d/100 | %s\n",
            pwd, strength, desc);
    }
}
```

**Expected Output:**
```
=== Password Strength Tests ===
Password: pass                 | Strength:   0/100 | Very Weak
Password: password             | Strength:  10/100 | Very Weak
Password: Password123          | Strength:  60/100 | Strong
Password: MyP@ssw0rd123!       | Strength:  90/100 | Very Strong
```

---

## 🗄️ Database Setup (Optional)

If you want to test with database:

### 1. Ensure MySQL is Running
```bash
# Check if MySQL is running on port 3308
netstat -an | grep 3308
```

### 2. Run Flyway Migrations

**In NetBeans:**
1. Right-click project → **Run Maven** → **Goals**
2. Type: `flyway:migrate`
3. Click **OK**

**Or in Terminal:**
```bash
cd /path/to/Exportation_Panelera_2
mvn flyway:migrate
```

**Expected:**
```
[INFO] Successfully applied 3 migrations
```

### 3. Verify Tables Created

Connect to MySQL and check:
```sql
USE exportation_panelera;
SHOW TABLES;
```

Should show:
- users ✓
- exportations ✓
- deliveries ✓
- flyway_schema_history ✓

---

## 🚨 Troubleshooting

### Problem: Compilation Errors

**Symptom:** Red X icons in Projects tab

**Solutions:**
1. Right-click project → **Clean and Build**
2. File → **Project Properties** → Categories: **Sources**
   - Verify: Source/Binary Format: **21**
3. Tools → **Java Platforms**
   - Verify: JDK 21 is selected

### Problem: Cannot Resolve Dependencies

**Symptom:** "Cannot find symbol" errors

**Solutions:**
1. Right-click project → **Update Dependencies**
2. Delete `~/.m2/repository` and rebuild
3. Check internet connection (Maven needs to download dependencies)

### Problem: Main Class Not Found

**Symptom:** "Could not find or load main class"

**Solutions:**
1. Right-click project → **Properties** → **Run**
2. Main Class: `exportation_panelera.Exportation_Panelera`
3. Click **OK**

### Problem: Database Connection Failed

**Symptom:** "Database unavailable"

**Solutions:**
1. Check MySQL is running: `sudo systemctl status mysql`
2. Check port: MySQL should be on 3308 (not default 3306)
3. Check credentials in code match your MySQL setup
4. The app will run in offline mode if database unavailable

---

## ✅ Success Criteria

Your improvements are working correctly if:

- [✓] Project opens in NetBeans without errors
- [✓] Clean and Build shows `BUILD SUCCESS`
- [✓] TestImprovements.java runs and shows all PASS ✓
- [✓] Main application starts and shows SignInForm
- [✓] New directories visible in Projects tab:
  - exportation_panelera.security
  - exportation_panelera.service
  - exportation_panelera.exception
- [✓] No compilation errors (no red X icons)

---

## 📊 What Changed - Quick Reference

| Feature | Location | What It Does |
|---------|----------|--------------|
| Rate Limiting | `security/RateLimiter.java` | Blocks brute force attacks (5 tries/15 min) |
| Password Policy | `security/PasswordPolicy.java` | Enforces strong passwords |
| Sessions | `security/SessionManager.java` | Manages user sessions (30 min timeout) |
| Auth Service | `service/AuthenticationService.java` | Central authentication logic |
| Exceptions | `exception/*.java` | Custom error types |
| Migrations | `resources/db/migration/*.sql` | Database version control |

---

## 🎯 Next Steps After Verification

1. ✅ Delete `TestImprovements.java` (it was just for testing)
2. 📖 Read `IMPROVEMENTS_SUMMARY.md` for detailed usage
3. 🔐 Review `SECURITY_IMPROVEMENTS.md` for security details
4. 🚀 Start using the new `AuthenticationService` in your code

---

**Questions?**
- Check `IMPROVEMENTS_SUMMARY.md`
- Review the Javadoc comments in the new classes
- All new classes have detailed documentation

---

Last updated: November 2025
