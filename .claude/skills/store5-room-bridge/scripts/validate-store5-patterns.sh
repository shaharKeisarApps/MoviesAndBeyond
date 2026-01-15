#!/bin/bash
# Validates Store5 implementation patterns
# Run from project root: ./.claude/skills/store5-room-bridge/scripts/validate-store5-patterns.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=============================================="
echo "  Store5 Pattern Validation"
echo "=============================================="
echo ""

ERRORS=0
WARNINGS=0

# Check for StoreReadResponse handling completeness
echo "Checking StoreReadResponse exhaustive handling..."
STORE_RESPONSE_HANDLERS=$(grep -rn "when.*StoreReadResponse" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$STORE_RESPONSE_HANDLERS" ]; then
    # Check if all states are handled
    MISSING_INITIAL=$(echo "$STORE_RESPONSE_HANDLERS" | grep -v "Initial" | head -5 || true)
    MISSING_LOADING=$(echo "$STORE_RESPONSE_HANDLERS" | grep -v "Loading" | head -5 || true)
    MISSING_NONEWDATA=$(echo "$STORE_RESPONSE_HANDLERS" | grep -v "NoNewData" | head -5 || true)

    echo -e "${GREEN}OK: Found StoreReadResponse handlers${NC}"
else
    echo -e "${YELLOW}WARNING: No StoreReadResponse when expressions found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for proper Store builder patterns
echo "Checking Store builder patterns..."
STORE_BUILDERS=$(grep -rn "StoreBuilder.from" --include="*.kt" data/ 2>/dev/null || true)
if [ -n "$STORE_BUILDERS" ]; then
    COUNT=$(echo "$STORE_BUILDERS" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT Store builders${NC}"

    # Check for SourceOfTruth
    SOT_COUNT=$(grep -rn "SourceOfTruth.of" --include="*.kt" data/ 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}   - SourceOfTruth definitions: $SOT_COUNT${NC}"

    # Check for Fetcher
    FETCHER_COUNT=$(grep -rn "Fetcher.of" --include="*.kt" data/ 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}   - Fetcher definitions: $FETCHER_COUNT${NC}"
else
    echo -e "${RED}ERROR: No StoreBuilder.from() found in data module${NC}"
    ((ERRORS++))
fi

echo ""

# Check for StoreResponseExtensions usage
echo "Checking StoreResponseExtensions usage..."
EXTENSIONS_USAGE=$(grep -rn "isFromCache\|isFromNetwork\|errorMessageOrNull\|dataOrNull" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$EXTENSIONS_USAGE" ]; then
    COUNT=$(echo "$EXTENSIONS_USAGE" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT usages of StoreResponse extensions${NC}"
else
    echo -e "${YELLOW}WARNING: StoreResponseExtensions not being used in feature modules${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for StoreReadRequest patterns
echo "Checking StoreReadRequest patterns..."
CACHED_REQUESTS=$(grep -rn "StoreReadRequest.cached" --include="*.kt" data/ 2>/dev/null | wc -l | tr -d ' ')
FRESH_REQUESTS=$(grep -rn "StoreReadRequest.fresh" --include="*.kt" data/ 2>/dev/null | wc -l | tr -d ' ')
echo "   - cached() requests: $CACHED_REQUESTS"
echo "   - fresh() requests: $FRESH_REQUESTS"

if [ "$CACHED_REQUESTS" -gt 0 ]; then
    echo -e "${GREEN}OK: Using cached requests for offline-first${NC}"
else
    echo -e "${YELLOW}WARNING: No cached() requests found - consider offline-first pattern${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for proper DAO Flow return types
echo "Checking Room DAO Flow return types..."
DAO_FLOWS=$(grep -rn "fun observe.*: Flow<" --include="*.kt" core/local/ 2>/dev/null || true)
if [ -n "$DAO_FLOWS" ]; then
    COUNT=$(echo "$DAO_FLOWS" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT Flow-returning DAO methods${NC}"
else
    echo -e "${YELLOW}WARNING: No Flow-returning DAO methods found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for entity mapping functions
echo "Checking entity mapping functions..."
ENTITY_MAPPERS=$(grep -rn "fun.*\.to.*Entity\|fun.*Entity\.to" --include="*.kt" data/src/ 2>/dev/null || true)
if [ -n "$ENTITY_MAPPERS" ]; then
    COUNT=$(echo "$ENTITY_MAPPERS" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT entity mapping functions${NC}"
else
    echo -e "${YELLOW}WARNING: No entity mapping functions found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for distinctBy in pagination
echo "Checking pagination deduplication..."
DISTINCT_BY=$(grep -rn "distinctBy" --include="*.kt" feature/ 2>/dev/null || true)
if [ -n "$DISTINCT_BY" ]; then
    COUNT=$(echo "$DISTINCT_BY" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT distinctBy usages for deduplication${NC}"
else
    echo -e "${YELLOW}WARNING: No distinctBy found - check pagination for duplicates${NC}"
    ((WARNINGS++))
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
