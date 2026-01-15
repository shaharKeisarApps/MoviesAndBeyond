#!/bin/bash
# Validates that ViewModels use WhileSubscribedOrRetained instead of WhileSubscribed(5000)
# Run from project root: ./.claude/skills/compose-viewmodel-bridge/scripts/validate-state-strategy.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=============================================="
echo "  StateFlow Strategy Validation"
echo "=============================================="
echo ""

ERRORS=0
WARNINGS=0

# Check for WhileSubscribed with timeout (anti-pattern)
echo "Checking for WhileSubscribed(5000) anti-pattern..."
BAD_PATTERN=$(grep -rn "SharingStarted\.WhileSubscribed\s*(" --include="*.kt" feature/ app/ 2>/dev/null || true)
if [ -n "$BAD_PATTERN" ]; then
    echo -e "${RED}ERROR: Found SharingStarted.WhileSubscribed with timeout (use stateInWhileSubscribed instead):${NC}"
    echo "$BAD_PATTERN"
    ((ERRORS++))
else
    echo -e "${GREEN}OK: No SharingStarted.WhileSubscribed(timeout) found${NC}"
fi

echo ""

# Check for stateIn without WhileSubscribedOrRetained
echo "Checking for raw stateIn() usage..."
RAW_STATEIN=$(grep -rn "\.stateIn\s*(" --include="*.kt" feature/ app/ 2>/dev/null | grep -v "stateInWhileSubscribed" | grep -v "test" | grep -v "Test" || true)
if [ -n "$RAW_STATEIN" ]; then
    echo -e "${YELLOW}WARNING: Found raw stateIn() calls - consider using stateInWhileSubscribed:${NC}"
    echo "$RAW_STATEIN"
    ((WARNINGS++))
else
    echo -e "${GREEN}OK: All stateIn() calls use stateInWhileSubscribed${NC}"
fi

echo ""

# Check for SharingStarted.Eagerly/Lazily
echo "Checking for Eagerly/Lazily usage..."
EAGER_LAZY=$(grep -rn "SharingStarted\.\(Eagerly\|Lazily\)" --include="*.kt" feature/ app/ 2>/dev/null || true)
if [ -n "$EAGER_LAZY" ]; then
    echo -e "${YELLOW}WARNING: Found SharingStarted.Eagerly/Lazily - ensure this is intentional:${NC}"
    echo "$EAGER_LAZY"
    ((WARNINGS++))
else
    echo -e "${GREEN}OK: No Eagerly/Lazily sharing strategies found${NC}"
fi

echo ""

# Verify stateInWhileSubscribed is being used
echo "Verifying stateInWhileSubscribed usage..."
GOOD_USAGE=$(grep -rn "stateInWhileSubscribed" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$GOOD_USAGE" ]; then
    COUNT=$(echo "$GOOD_USAGE" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT usages of stateInWhileSubscribed${NC}"
else
    echo -e "${YELLOW}WARNING: No stateInWhileSubscribed usage found in feature modules${NC}"
    ((WARNINGS++))
fi

echo ""

# Check that WhileSubscribedOrRetained.kt exists
echo "Verifying WhileSubscribedOrRetained implementation exists..."
if [ -f "data/src/main/java/com/keisardev/moviesandbeyond/data/coroutines/WhileSubscribedOrRetained.kt" ]; then
    echo -e "${GREEN}OK: WhileSubscribedOrRetained.kt found${NC}"
else
    echo -e "${RED}ERROR: WhileSubscribedOrRetained.kt not found at expected location${NC}"
    ((ERRORS++))
fi

echo ""
echo "=============================================="
echo "  Summary"
echo "=============================================="
echo -e "Errors: ${RED}$ERRORS${NC}"
echo -e "Warnings: ${YELLOW}$WARNINGS${NC}"

if [ $ERRORS -gt 0 ]; then
    echo -e "\n${RED}FAILED: Please fix the errors above${NC}"
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo -e "\n${YELLOW}PASSED with warnings${NC}"
    exit 0
else
    echo -e "\n${GREEN}PASSED: All checks passed${NC}"
    exit 0
fi
