# Find Your MySQL Port

## Method 1: phpMyAdmin (EASIEST)

1. Go to phpMyAdmin (localhost:3309)
2. Click the "SQL" tab at top
3. Paste this and click "Go":

```sql
SHOW VARIABLES WHERE Variable_name = 'port';
```

4. It will show your MySQL port number!

---

## Method 2: Try Common Ports

Try updating hikari.properties with these common ports:

### Try Port 3306 (Default MySQL):
```properties
jdbcUrl=jdbc:mysql://localhost:3306/exportation_panelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### Try Port 8889 (MAMP):
```properties
jdbcUrl=jdbc:mysql://localhost:8889/exportation_panelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### Try Port 3307:
```properties
jdbcUrl=jdbc:mysql://localhost:3307/exportation_panelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## Method 3: Check MAMP/XAMPP

**MAMP:**
1. Open MAMP
2. Preferences → Ports
3. Look at "MySQL Port"

**XAMPP:**
1. Open XAMPP Control Panel
2. Look at MySQL port (usually 3306)

---

## After Finding the Port:

1. Edit: `src/main/resources/hikari.properties`
2. Change line 3:
   ```
   jdbcUrl=jdbc:mysql://localhost:CORRECT_PORT/exportation_panelera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```
3. Replace CORRECT_PORT with your actual MySQL port
4. Run your application again
