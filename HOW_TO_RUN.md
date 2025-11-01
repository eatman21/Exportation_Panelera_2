# How to Run Exportation Panelera - Quick Guide

## ✅ Fixed Issue

**Problem**: The application was showing the Delivery Management form directly instead of the SignIn form.

**Cause**: The `DeliveryManagementForm.java` had its own `main()` method that was being executed.

**Solution**:
1. ✅ Commented out the standalone `main()` method in `DeliveryManagementForm.java`
2. ✅ Created proper main entry point in `Exportation_Panelera.java`
3. ✅ Added NetBeans configuration file (`nbactions.xml`) to run the correct main class

---

## 🚀 How to Run the Application

### Method 1: NetBeans (Recommended)

1. **Open the project** in NetBeans
2. **Clean and Build** the project:
   - Right-click on the project → **Clean and Build**
   - Or press: `Shift + F11`
3. **Run the project**:
   - Right-click on the project → **Run**
   - Or press: `F6`
4. **Expected Result**: The **SignIn form** should appear first!

### Method 2: Command Line (Maven)

```bash
# Navigate to project directory
cd /Users/mac/NetBeansProjects/Exportation_Panelera

# Clean and compile
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="exportation_panelera.Exportation_Panelera"
```

### Method 3: Run JAR File

```bash
# Build the JAR with dependencies
mvn clean package

# Run the JAR
java -jar target/exportation-panelera-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## 🔐 Default Login Credentials

When the database is unavailable (offline mode):

- **Username**: `admin`
- **Password**: `admin123`

---

## 📋 Expected Application Flow

```
1. Application Starts
   ↓
2. Database Connection Pool Initializes (HikariCP)
   ↓
3. SignIn Form Appears ← YOU SHOULD SEE THIS FIRST!
   ↓
4. Enter Credentials (admin/admin123)
   ↓
5. Click "Login" or press Enter
   ↓
6. MainView Opens with Navigation:
   - Exportation Management
   - Delivery Management
   - Sign Out
```

---

## ⚠️ Important Notes

### If You Still See Delivery Management Form First

This can happen if:

1. **Wrong file is selected in NetBeans**:
   - Close all open files
   - Right-click project → **Clean and Build**
   - Right-click project → **Run** (NOT run file)

2. **Running a specific file instead of the project**:
   - Don't right-click on `DeliveryManagementForm.java` and select "Run File"
   - Always run the entire **project** from the project root

3. **NetBeans cache issue**:
   ```bash
   # Close NetBeans
   # Delete NetBeans cache
   rm -rf ~/Library/Caches/NetBeans/
   # Reopen NetBeans
   ```

### Main Class Configuration

The correct main class is configured in multiple places:

1. **pom.xml** (line 91):
   ```xml
   <mainClass>exportation_panelera.Exportation_Panelera</mainClass>
   ```

2. **nbactions.xml** (NetBeans):
   ```xml
   <exec.args>... exportation_panelera.Exportation_Panelera ...</exec.args>
   ```

---

## 🐛 Troubleshooting

### Issue: Still showing wrong form

**Solution**:
1. In NetBeans, go to: **Project Properties** (right-click project)
2. Select **Run** category
3. Verify **Main Class** is: `exportation_panelera.Exportation_Panelera`
4. Click **OK**
5. **Clean and Build** again
6. **Run** the project

### Issue: "Class not found" error

**Solution**:
```bash
mvn clean compile
```

### Issue: Database connection errors

**Solution**:
- The application will run in **offline mode** if database is unavailable
- Use default credentials: `admin` / `admin123`
- Check `src/main/resources/hikari.properties` for database settings

---

## 📁 Project Structure

```
Exportation_Panelera/
├── src/
│   └── exportation_panelera/
│       └── Exportation_Panelera.java  ← MAIN ENTRY POINT ✓
└── src/main/java/exportation_panelera/View/
    ├── SignInForm.java                ← Should appear FIRST
    ├── MainView.java                  ← Then this
    ├── DeliveryManagementForm.java    ← main() commented out ✓
    └── ExportationDelivery.java
```

---

## ✅ Verification Checklist

After running, you should see:

- ✅ Console shows: "Initializing database connection pool..."
- ✅ Console shows: "Starting Exportation Panelera application..."
- ✅ A window titled "Sign In" appears
- ✅ Window contains username and password fields
- ✅ Window has buttons: "Login", "Cancel", "Create Account"

If you see the Delivery Management table instead, follow the troubleshooting steps above.

---

## 🆘 Still Having Issues?

1. Close NetBeans completely
2. Run from command line:
   ```bash
   cd /Users/mac/NetBeansProjects/Exportation_Panelera
   mvn clean package
   java -jar target/exportation-panelera-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
3. Check the console output for any errors

If the command line works but NetBeans doesn't, the issue is with NetBeans configuration.

---

**Last Updated**: October 31, 2024
**Version**: 2.0 with HikariCP, Unit Tests, and i18n
