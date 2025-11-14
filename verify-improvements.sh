#!/bin/bash

# Verification Script for Exportation Panelera Improvements
# This script checks that all improvements are in place

echo "======================================"
echo "  Verifying Project Improvements"
echo "======================================"
echo ""

ERRORS=0

# Function to check file exists
check_file() {
    if [ -f "$1" ]; then
        echo "✓ $1"
        return 0
    else
        echo "✗ MISSING: $1"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

# Function to check directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo "✓ $1/"
        return 0
    else
        echo "✗ MISSING: $1/"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

echo "Checking New Security Features..."
check_file "src/main/java/exportation_panelera/security/RateLimiter.java"
check_file "src/main/java/exportation_panelera/security/PasswordPolicy.java"
check_file "src/main/java/exportation_panelera/security/SessionManager.java"
echo ""

echo "Checking Service Layer..."
check_file "src/main/java/exportation_panelera/service/AuthenticationService.java"
echo ""

echo "Checking Exception Classes..."
check_file "src/main/java/exportation_panelera/exception/AuthenticationException.java"
check_file "src/main/java/exportation_panelera/exception/DatabaseException.java"
check_file "src/main/java/exportation_panelera/exception/RateLimitException.java"
check_file "src/main/java/exportation_panelera/exception/ValidationException.java"
echo ""

echo "Checking Database Migrations..."
check_file "src/main/resources/db/migration/V1__create_users_table.sql"
check_file "src/main/resources/db/migration/V2__create_exportations_table.sql"
check_file "src/main/resources/db/migration/V3__create_deliveries_table.sql"
echo ""

echo "Checking Documentation..."
check_file "IMPROVEMENTS_SUMMARY.md"
check_file "NETBEANS_VERIFICATION_GUIDE.md"
echo ""

echo "Checking Legacy Code Removed..."
if [ -d "src/exportation_panelera" ]; then
    echo "✗ WARNING: Legacy code still exists in src/exportation_panelera/"
    ERRORS=$((ERRORS + 1))
else
    echo "✓ Legacy code removed (src/exportation_panelera/)"
fi
echo ""

echo "Checking Main Entry Point..."
check_file "src/main/java/exportation_panelera/Exportation_Panelera.java"
echo ""

echo "Checking POM Updated..."
if grep -q "flyway" pom.xml; then
    echo "✓ Flyway dependency in pom.xml"
else
    echo "✗ Flyway dependency missing in pom.xml"
    ERRORS=$((ERRORS + 1))
fi

if grep -q "slf4j.version>2.0" pom.xml; then
    echo "✓ SLF4J updated to 2.0.x"
else
    echo "✗ SLF4J not updated"
    ERRORS=$((ERRORS + 1))
fi
echo ""

echo "Checking .gitignore Enhanced..."
if grep -q "application.properties" .gitignore; then
    echo "✓ .gitignore protects sensitive files"
else
    echo "✗ .gitignore not enhanced"
    ERRORS=$((ERRORS + 1))
fi
echo ""

echo "======================================"
if [ $ERRORS -eq 0 ]; then
    echo "  ✓ ALL CHECKS PASSED!"
    echo "======================================"
    echo ""
    echo "Your project is ready to use in NetBeans!"
    echo ""
    echo "Next steps:"
    echo "  1. Open project in NetBeans"
    echo "  2. Read NETBEANS_VERIFICATION_GUIDE.md"
    echo "  3. Run 'Clean and Build'"
    echo ""
    exit 0
else
    echo "  ✗ FAILED: $ERRORS error(s) found"
    echo "======================================"
    echo ""
    echo "Some files are missing. Please check:"
    echo "  1. Did you pull the latest changes?"
    echo "  2. Are you in the correct directory?"
    echo "  3. Run: git pull origin main"
    echo ""
    exit 1
fi
