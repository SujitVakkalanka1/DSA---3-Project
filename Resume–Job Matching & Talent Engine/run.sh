#!/bin/bash
# ====================================================================
#   Résumé–Job Matching & Talent-Marketplace Engine
#   Course: Data Structures and Algorithms - 3 (25CS2103E)
#   KL University, Hyderabad | Team 14 | Section 10
# ====================================================================

echo "===================================================================="
echo "  Résumé–Job Matching & Talent-Marketplace Engine"
echo "  Course: DSA-3 (25CS2103E) | KL University | Team 14"
echo "===================================================================="
echo ""

if ! command -v javac &> /dev/null; then
    echo "[ERROR] 'javac' could not be found. Please install JDK 17+."
    exit 1
fi

echo "[1/3] Creating output directory..."
mkdir -p bin

echo "[2/3] Compiling Java source files..."
javac -encoding UTF-8 -d bin src/model/*.java src/dsa/*.java src/storage/*.java src/server/*.java src/Main.java
if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed."
    exit 1
fi

echo "[3/3] Starting Talent Engine HTTP Server on http://localhost:8080..."
echo "Press Ctrl+C to stop the server."
echo "===================================================================="
echo ""
java -cp bin Main
