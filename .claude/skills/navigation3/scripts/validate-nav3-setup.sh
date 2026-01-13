#!/bin/bash
# Navigation 3 Setup Validation Script
# Checks for common Navigation 3 configuration issues

set -e

PROJECT_ROOT="${1:-.}"
ERRORS=0
WARNINGS=0

echo "=== Navigation 3 Setup Validation ==="
echo ""

# Check for Navigation 3 dependencies in libs.versions.toml
check_dependencies() {
    echo "Checking dependencies..."

    VERSIONS_FILE="$PROJECT_ROOT/gradle/libs.versions.toml"

    if [[ ! -f "$VERSIONS_FILE" ]]; then
        echo "  [ERROR] libs.versions.toml not found"
        ((ERRORS++))
        return
    fi

    # Check for navigation3 runtime
    if grep -q "navigation3-runtime\|navigation3.runtime" "$VERSIONS_FILE"; then
        echo "  [OK] navigation3-runtime dependency found"
    else
        echo "  [ERROR] Missing navigation3-runtime dependency"
        ((ERRORS++))
    fi

    # Check for navigation3 ui
    if grep -q "navigation3-ui\|navigation3.ui" "$VERSIONS_FILE"; then
        echo "  [OK] navigation3-ui dependency found"
    else
        echo "  [ERROR] Missing navigation3-ui dependency"
        ((ERRORS++))
    fi

    # Check for lifecycle-viewmodel-navigation3
    if grep -q "lifecycle-viewmodel-navigation3\|viewmodel.navigation3" "$VERSIONS_FILE"; then
        echo "  [OK] lifecycle-viewmodel-navigation3 dependency found"
    else
        echo "  [WARNING] Missing lifecycle-viewmodel-navigation3 - required for ViewModel scoping"
        ((WARNINGS++))
    fi

    # Check for kotlinx-serialization
    if grep -q "kotlinx-serialization\|serialization" "$VERSIONS_FILE"; then
        echo "  [OK] kotlinx-serialization found"
    else
        echo "  [WARNING] kotlinx-serialization may be needed for NavKey serialization"
        ((WARNINGS++))
    fi

    echo ""
}

# Check for NavKey usage
check_navkey_usage() {
    echo "Checking NavKey implementations..."

    # Find route files
    ROUTE_FILES=$(find "$PROJECT_ROOT" -name "*.kt" -type f \( -name "*Route*.kt" -o -name "*Routes.kt" \) 2>/dev/null | grep -v build || true)

    if [[ -z "$ROUTE_FILES" ]]; then
        echo "  [INFO] No route files found"
        return
    fi

    for file in $ROUTE_FILES; do
        if grep -q "@Serializable" "$file"; then
            if grep -q ": NavKey" "$file"; then
                echo "  [OK] $file - Routes implement NavKey"
            else
                echo "  [WARNING] $file - @Serializable found but no NavKey interface"
                ((WARNINGS++))
            fi
        fi
    done

    echo ""
}

# Check for ViewModel decorator usage
check_viewmodel_decorator() {
    echo "Checking ViewModel decorator usage..."

    # Find files with NavDisplay
    NAV_FILES=$(grep -rl "NavDisplay" "$PROJECT_ROOT" --include="*.kt" 2>/dev/null | grep -v build || true)

    if [[ -z "$NAV_FILES" ]]; then
        echo "  [INFO] No NavDisplay usage found"
        return
    fi

    for file in $NAV_FILES; do
        if grep -q "rememberViewModelStoreNavEntryDecorator" "$file"; then
            echo "  [OK] $file - ViewModel decorator present"
        else
            if grep -q "hiltViewModel\|viewModel" "$file"; then
                echo "  [ERROR] $file - Uses ViewModels but missing rememberViewModelStoreNavEntryDecorator()"
                echo "         This will cause ViewModels to be scoped to Activity instead of NavEntry!"
                ((ERRORS++))
            fi
        fi

        if grep -q "rememberSaveableStateHolderNavEntryDecorator" "$file"; then
            echo "  [OK] $file - SaveableState decorator present"
        else
            echo "  [WARNING] $file - Missing rememberSaveableStateHolderNavEntryDecorator()"
            ((WARNINGS++))
        fi
    done

    echo ""
}

# Check back stack type
check_backstack_type() {
    echo "Checking back stack implementation..."

    NAV_FILES=$(grep -rl "NavDisplay" "$PROJECT_ROOT" --include="*.kt" 2>/dev/null | grep -v build || true)

    for file in $NAV_FILES; do
        if grep -q "rememberNavBackStack\|mutableStateListOf" "$file"; then
            echo "  [OK] $file - Proper back stack type"
        elif grep -q "mutableListOf" "$file"; then
            echo "  [ERROR] $file - Using mutableListOf instead of mutableStateListOf"
            echo "         Back stack changes won't trigger recomposition!"
            ((ERRORS++))
        fi
    done

    echo ""
}

# Run all checks
check_dependencies
check_navkey_usage
check_viewmodel_decorator
check_backstack_type

# Summary
echo "=== Validation Summary ==="
echo "Errors: $ERRORS"
echo "Warnings: $WARNINGS"
echo ""

if [[ $ERRORS -gt 0 ]]; then
    echo "FAILED: Please fix the errors above"
    exit 1
elif [[ $WARNINGS -gt 0 ]]; then
    echo "PASSED with warnings"
    exit 0
else
    echo "PASSED: Navigation 3 setup looks good!"
    exit 0
fi
