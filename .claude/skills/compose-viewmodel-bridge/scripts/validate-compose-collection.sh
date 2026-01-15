#!/bin/bash
# Validates that Compose screens use collectAsStateWithLifecycle instead of collectAsState
# Run from project root: ./.claude/skills/compose-viewmodel-bridge/scripts/validate-compose-collection.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=============================================="
echo "  Compose State Collection Validation"
echo "=============================================="
echo ""

ERRORS=0
WARNINGS=0

# Check for collectAsState without lifecycle (anti-pattern)
echo "Checking for collectAsState() without lifecycle awareness..."
BAD_COLLECT=$(grep -rn "\.collectAsState\(\)" --include="*.kt" feature/ app/ 2>/dev/null | grep -v "collectAsStateWithLifecycle" || true)
if [ -n "$BAD_COLLECT" ]; then
    echo -e "${RED}ERROR: Found collectAsState() without lifecycle awareness:${NC}"
    echo "$BAD_COLLECT"
    echo ""
    echo "Replace with: collectAsStateWithLifecycle()"
    ((ERRORS++))
else
    echo -e "${GREEN}OK: No lifecycle-unaware collectAsState() found${NC}"
fi

echo ""

# Verify collectAsStateWithLifecycle is being used
echo "Verifying collectAsStateWithLifecycle usage..."
GOOD_COLLECT=$(grep -rn "collectAsStateWithLifecycle" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$GOOD_COLLECT" ]; then
    COUNT=$(echo "$GOOD_COLLECT" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT usages of collectAsStateWithLifecycle${NC}"
else
    echo -e "${YELLOW}WARNING: No collectAsStateWithLifecycle usage found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for proper import
echo "Checking for lifecycle-compose import..."
IMPORT_CHECK=$(grep -rn "import androidx.lifecycle.compose.collectAsStateWithLifecycle" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$IMPORT_CHECK" ]; then
    COUNT=$(echo "$IMPORT_CHECK" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found lifecycle-compose import in $COUNT files${NC}"
else
    echo -e "${YELLOW}WARNING: lifecycle-compose import not found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for ViewModels being collected in Composables
echo "Checking for ViewModel state collection patterns..."
VIEWMODEL_COLLECT=$(grep -rn "viewModel\.\w*\.\(collectAsStateWithLifecycle\|collectAsState\)" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$VIEWMODEL_COLLECT" ]; then
    echo "ViewModel state collection found:"
    echo "$VIEWMODEL_COLLECT" | head -10
    if [ $(echo "$VIEWMODEL_COLLECT" | wc -l) -gt 10 ]; then
        echo "... and more"
    fi
else
    echo -e "${YELLOW}NOTE: No direct ViewModel state collection found (may use different patterns)${NC}"
fi

echo ""

# Check for direct Flow collection in Composables (potential issue)
echo "Checking for direct Flow.collect() in Composables..."
FLOW_COLLECT=$(grep -rn "\.collect\s*{" --include="*.kt" feature/ 2>/dev/null | grep -i "screen\|route\|composable" || true)
if [ -n "$FLOW_COLLECT" ]; then
    echo -e "${YELLOW}WARNING: Found potential direct Flow.collect() in Composables:${NC}"
    echo "$FLOW_COLLECT" | head -5
    echo ""
    echo "Consider using collectAsStateWithLifecycle() instead"
    ((WARNINGS++))
else
    echo -e "${GREEN}OK: No direct Flow.collect() in screen files${NC}"
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
