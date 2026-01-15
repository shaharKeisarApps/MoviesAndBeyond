#!/bin/bash
# Validates offline-first data pipeline compliance
# Run from project root: ./.claude/skills/store5-room-bridge/scripts/validate-offline-first.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=============================================="
echo "  Offline-First Compliance Validation"
echo "=============================================="
echo ""

ERRORS=0
WARNINGS=0

# Check for Room database setup
echo "Checking Room database setup..."
DATABASE_FILE=$(find core/local -name "*Database.kt" 2>/dev/null | head -1)
if [ -n "$DATABASE_FILE" ]; then
    echo -e "${GREEN}OK: Found Room database: $DATABASE_FILE${NC}"
else
    echo -e "${RED}ERROR: No Room database found in core/local${NC}"
    ((ERRORS++))
fi

echo ""

# Check for cached content entity
echo "Checking cached content entity..."
CACHED_ENTITY=$(find core/local -name "*CachedContent*.kt" 2>/dev/null | head -1)
if [ -n "$CACHED_ENTITY" ]; then
    echo -e "${GREEN}OK: Found cached entity: $CACHED_ENTITY${NC}"

    # Check for composite primary key
    COMPOSITE_KEY=$(grep -l "primaryKeys.*=" "$CACHED_ENTITY" 2>/dev/null || true)
    if [ -n "$COMPOSITE_KEY" ]; then
        echo -e "${GREEN}   - Has composite primary key${NC}"
    else
        echo -e "${YELLOW}WARNING: No composite primary key found - may have deduplication issues${NC}"
        ((WARNINGS++))
    fi

    # Check for fetchedAt timestamp
    FETCHED_AT=$(grep -l "fetched_at\|fetchedAt" "$CACHED_ENTITY" 2>/dev/null || true)
    if [ -n "$FETCHED_AT" ]; then
        echo -e "${GREEN}   - Has fetchedAt timestamp for freshness${NC}"
    else
        echo -e "${YELLOW}WARNING: No fetchedAt field - cannot track data freshness${NC}"
        ((WARNINGS++))
    fi

    # Check for position field
    POSITION=$(grep -l "position" "$CACHED_ENTITY" 2>/dev/null || true)
    if [ -n "$POSITION" ]; then
        echo -e "${GREEN}   - Has position field for ordering${NC}"
    else
        echo -e "${YELLOW}WARNING: No position field - API order may be lost${NC}"
        ((WARNINGS++))
    fi
else
    echo -e "${RED}ERROR: No cached content entity found${NC}"
    ((ERRORS++))
fi

echo ""

# Check for DAO with Flow
echo "Checking DAO reactive patterns..."
DAO_FILES=$(find core/local -name "*Dao.kt" 2>/dev/null)
if [ -n "$DAO_FILES" ]; then
    FLOW_DAOS=$(grep -l "Flow<" $DAO_FILES 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $FLOW_DAOS DAOs with Flow return types${NC}"

    # Check for suspend functions
    SUSPEND_COUNT=$(grep -rn "suspend fun" $DAO_FILES 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}   - Suspend functions: $SUSPEND_COUNT${NC}"
else
    echo -e "${RED}ERROR: No DAO files found${NC}"
    ((ERRORS++))
fi

echo ""

# Check for Store5 integration
echo "Checking Store5 integration..."
STORE_FILES=$(find data -name "*Store*.kt" 2>/dev/null | grep -v "test" || true)
if [ -n "$STORE_FILES" ]; then
    echo -e "${GREEN}OK: Found Store files${NC}"
    echo "$STORE_FILES" | while read -r file; do
        echo "   - $(basename $file)"
    done
else
    echo -e "${RED}ERROR: No Store files found in data module${NC}"
    ((ERRORS++))
fi

echo ""

# Check for Repository offline support
echo "Checking Repository offline-first methods..."
REPO_IMPL=$(find data -name "*RepositoryImpl.kt" 2>/dev/null)
if [ -n "$REPO_IMPL" ]; then
    OBSERVE_METHODS=$(grep -rn "fun observe" $REPO_IMPL 2>/dev/null | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $OBSERVE_METHODS observe() methods${NC}"

    # Check for deprecated network-only methods
    DEPRECATED=$(grep -rn "@Deprecated" $REPO_IMPL 2>/dev/null | wc -l | tr -d ' ')
    if [ "$DEPRECATED" -gt 0 ]; then
        echo -e "${GREEN}   - $DEPRECATED legacy methods marked @Deprecated${NC}"
    fi
else
    echo -e "${YELLOW}WARNING: No RepositoryImpl files found${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for error handling in ViewModels
echo "Checking error handling patterns..."
ERROR_FLOWS=$(grep -rn "_errorMessage\|errorMessage" --include="*.kt" feature/ 2>/dev/null | wc -l | tr -d ' ')
if [ "$ERROR_FLOWS" -gt 0 ]; then
    echo -e "${GREEN}OK: Found error message handling in $ERROR_FLOWS locations${NC}"
else
    echo -e "${YELLOW}WARNING: No error message flows found - errors may not be displayed${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for stateInWhileSubscribed with Store5
echo "Checking stateInWhileSubscribed + Store5 integration..."
INTEGRATED=$(grep -rln "stateInWhileSubscribed" --include="*.kt" feature/ 2>/dev/null | while read -r file; do
    if grep -q "StoreReadResponse\|observeMovie\|observeTv" "$file" 2>/dev/null; then
        echo "$file"
    fi
done)
if [ -n "$INTEGRATED" ]; then
    COUNT=$(echo "$INTEGRATED" | wc -l | tr -d ' ')
    echo -e "${GREEN}OK: Found $COUNT ViewModels with Store5 + stateInWhileSubscribed${NC}"
else
    echo -e "${YELLOW}WARNING: No ViewModels found using both Store5 and stateInWhileSubscribed${NC}"
    ((WARNINGS++))
fi

echo ""

# Check for proper data persistence
echo "Checking data persistence..."
INSERT_METHODS=$(grep -rn "@Insert\|@Upsert" --include="*.kt" core/local/ 2>/dev/null | wc -l | tr -d ' ')
DELETE_METHODS=$(grep -rn "@Query.*DELETE" --include="*.kt" core/local/ 2>/dev/null | wc -l | tr -d ' ')
echo "   - Insert/Upsert methods: $INSERT_METHODS"
echo "   - Delete queries: $DELETE_METHODS"

if [ "$INSERT_METHODS" -gt 0 ] && [ "$DELETE_METHODS" -gt 0 ]; then
    echo -e "${GREEN}OK: Has both insert and delete operations for cache management${NC}"
else
    echo -e "${YELLOW}WARNING: Missing insert or delete operations${NC}"
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
