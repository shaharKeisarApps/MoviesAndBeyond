#!/bin/bash
set -e

echo "════════════════════════════════════════════════════════════════"
echo "  🔧 MoviesAndBeyond - Build & Install Verification Script"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Step 1: Run code quality checks
echo "${BLUE}Step 1:${NC} Running code quality checks..."
echo "─────────────────────────────────────────────────────────────────"

echo "  📋 Running Spotless check..."
./gradlew spotlessCheck || {
    echo "${RED}❌ Spotless check failed${NC}"
    echo "   Fix with: ./gradlew spotlessApply"
    exit 1
}
echo "${GREEN}  ✅ Spotless check passed${NC}"

echo ""
echo "  🔍 Running Detekt..."
./gradlew detekt || {
    echo "${YELLOW}⚠️  Detekt found issues${NC}"
    echo "   Review: build/reports/detekt/detekt.html"
    # Don't fail build, just warn
}
echo "${GREEN}  ✅ Detekt completed${NC}"

echo ""

# Step 2: Run unit tests
echo "${BLUE}Step 2:${NC} Running unit tests..."
echo "─────────────────────────────────────────────────────────────────"
./gradlew test || {
    echo "${RED}❌ Unit tests failed${NC}"
    exit 1
}
echo "${GREEN}✅ All unit tests passed${NC}"
echo ""

# Step 3: Build APK
echo "${BLUE}Step 3:${NC} Building debug APK..."
echo "─────────────────────────────────────────────────────────────────"
./gradlew assembleDebug || {
    echo "${RED}❌ APK build failed${NC}"
    exit 1
}

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "${RED}❌ APK not found at $APK_PATH${NC}"
    exit 1
fi

# Get APK info
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
APK_TIME=$(date -r "$APK_PATH" "+%Y-%m-%d %H:%M:%S")

echo "${GREEN}✅ APK built successfully${NC}"
echo "   📦 Size: $APK_SIZE"
echo "   🕐 Built: $APK_TIME"
echo "   📁 Location: $APK_PATH"
echo ""

# Step 4: Check for connected device
echo "${BLUE}Step 4:${NC} Checking for connected device..."
echo "─────────────────────────────────────────────────────────────────"

if ! command -v adb &> /dev/null; then
    echo "${YELLOW}⚠️  adb not found${NC}"
    echo "   Install Android SDK Platform Tools to enable auto-install"
    echo ""
    echo "${GREEN}✅ APK ready for manual installation${NC}"
    echo ""
    echo "Manual Install Instructions:"
    echo "  1. Copy APK to device: $APK_PATH"
    echo "  2. Open Files app on device"
    echo "  3. Tap APK file to install"
    echo ""
    exit 0
fi

DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "${YELLOW}⚠️  No devices connected${NC}"
    echo ""
    echo "${GREEN}✅ APK ready for manual installation${NC}"
    echo ""
    echo "To install APK:"
    echo "  1. Connect device via USB or start emulator"
    echo "  2. Enable USB debugging"
    echo "  3. Run: adb install -r $APK_PATH"
    echo ""
    exit 0
fi

echo "${GREEN}✅ Found $DEVICES connected device(s)${NC}"
echo ""

# Step 5: Install APK
echo "${BLUE}Step 5:${NC} Installing APK..."
echo "─────────────────────────────────────────────────────────────────"

adb install -r "$APK_PATH" || {
    echo "${RED}❌ APK installation failed${NC}"
    echo "   Try: adb uninstall com.keisardev.moviesandbeyond"
    echo "   Then: adb install $APK_PATH"
    exit 1
}

echo "${GREEN}✅ APK installed successfully${NC}"
echo ""

# Step 6: Launch app (optional)
echo "${BLUE}Step 6:${NC} Launching app..."
echo "─────────────────────────────────────────────────────────────────"

adb shell am start -n com.keisardev.moviesandbeyond/.MainActivity || {
    echo "${YELLOW}⚠️  Could not launch app automatically${NC}"
}

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  ${GREEN}✅ BUILD & INSTALL COMPLETE${NC}"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📋 ${BLUE}Manual Test Checklist:${NC}"
echo ""
echo "  ${YELLOW}Test A: Guest Mode Favorites (5 min)${NC}"
echo "    1. If logged in, log out"
echo "    2. Navigate to Movies → tap a movie"
echo "    3. Tap 'Add to favorites'"
echo "    4. Navigate to 'You' screen"
echo "    5. ✓ Verify: 'Your Library' section visible"
echo "    6. Tap 'Favorites'"
echo "    7. ✓ Verify: Movie appears in list"
echo "    8. Restart app"
echo "    9. ✓ Verify: Favorite persists"
echo ""
echo "  ${YELLOW}Test B: TMDB Sync After Login (5 min)${NC}"
echo "    1. Log in with TMDB account"
echo "    2. ✓ Verify: Account details appear"
echo "    3. Wait 2-3 seconds"
echo "    4. Navigate to 'You' → 'Favorites'"
echo "    5. ✓ Verify: TMDB favorites appear"
echo "    6. Compare with TMDB website"
echo "    7. ✓ Verify: Same favorites"
echo ""
echo "  ${YELLOW}Test C: Edge-to-Edge Status Bar (2 min)${NC}"
echo "    1. Open app"
echo "    2. ✓ Verify: Status bar background matches screen"
echo "    3. Navigate between tabs"
echo "    4. ✓ Verify: Status bar adapts to theme"
echo ""
echo "  ${YELLOW}Test D: Shared Element Transitions (3 min)${NC}"
echo "    1. Navigate to Movies feed"
echo "    2. Tap any movie poster"
echo "    3. ✓ Verify: Smooth fade through transition"
echo "    4. ✓ Verify: Poster morphs as focal point"
echo "    5. Press back"
echo "    6. ✓ Verify: Reverse transition smooth"
echo "    7. Swipe from left edge (predictive back)"
echo "    8. ✓ Verify: Gesture follows finger"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo ""
