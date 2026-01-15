#!/bin/bash
# Pre-PR Merge Validation Hook
# Validates all test plan items are checked before PR merge approval
#
# Usage: ./pre-pr-merge.sh <PR_NUMBER> [--strict]
#
# Exit codes:
#   0 - All validations pass
#   1 - General error
#   2 - Validation failed (blocks merge)

set -e

PR_NUMBER="${1:-}"
STRICT_MODE="${2:-}"
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_error() { echo -e "${RED}ERROR: $1${NC}" >&2; }
log_success() { echo -e "${GREEN}SUCCESS: $1${NC}"; }
log_warn() { echo -e "${YELLOW}WARNING: $1${NC}"; }
log_info() { echo "INFO: $1"; }

# Check if gh CLI is available
if ! command -v gh &> /dev/null; then
    log_error "GitHub CLI (gh) is not installed. Please install it: https://cli.github.com/"
    exit 1
fi

# Validate PR number provided
if [ -z "$PR_NUMBER" ]; then
    log_error "PR number required. Usage: $0 <PR_NUMBER>"
    exit 1
fi

log_info "Validating test plan for PR #$PR_NUMBER..."

# Fetch PR body
PR_BODY=$(gh pr view "$PR_NUMBER" --json body --jq '.body' 2>/dev/null)
if [ -z "$PR_BODY" ]; then
    log_error "Could not fetch PR #$PR_NUMBER. Ensure you're authenticated with 'gh auth login'"
    exit 1
fi

# Extract test plan section
# Look for common test plan headers
TEST_PLAN=$(echo "$PR_BODY" | sed -n '/## Test [Pp]lan/,/^## /p' | head -n -1)
if [ -z "$TEST_PLAN" ]; then
    # Try alternative headers
    TEST_PLAN=$(echo "$PR_BODY" | sed -n '/### Testing/,/^## /p' | head -n -1)
fi

if [ -z "$TEST_PLAN" ]; then
    log_warn "No test plan section found in PR description"
    if [ "$STRICT_MODE" == "--strict" ]; then
        log_error "Strict mode: Test plan is required"
        exit 2
    fi
    log_info "Skipping test plan validation (no test plan found)"
    exit 0
fi

# Count checklist items
TOTAL_ITEMS=$(echo "$TEST_PLAN" | grep -c '\- \[.\]' || echo "0")
CHECKED_ITEMS=$(echo "$TEST_PLAN" | grep -c '\- \[x\]' || echo "0")
UNCHECKED_ITEMS=$(echo "$TEST_PLAN" | grep -c '\- \[ \]' || echo "0")

log_info "Test plan items: $TOTAL_ITEMS total, $CHECKED_ITEMS checked, $UNCHECKED_ITEMS unchecked"

# Validate all items are checked
if [ "$UNCHECKED_ITEMS" -gt 0 ]; then
    log_error "$UNCHECKED_ITEMS test plan items are unchecked"
    echo ""
    echo "Unchecked items:"
    echo "$TEST_PLAN" | grep '\- \[ \]' | while read -r line; do
        echo "  - $line"
    done
    echo ""
    log_error "All test plan items must be verified before merge"
    echo ""
    echo "To fix this:"
    echo "1. Execute each unchecked test item"
    echo "2. Update the PR description to mark items as checked [x]"
    echo "3. Or use the test-plan-validator subagent to automate verification"
    exit 2
fi

# Additional validations (optional)

# Check if CI passed (if available)
CI_STATUS=$(gh pr checks "$PR_NUMBER" --json state --jq '.[].state' 2>/dev/null | sort -u || echo "")
if echo "$CI_STATUS" | grep -q "FAILURE"; then
    log_error "CI checks are failing. Fix CI before merging."
    exit 2
fi

if echo "$CI_STATUS" | grep -q "PENDING"; then
    log_warn "CI checks are still running. Wait for completion before merging."
    if [ "$STRICT_MODE" == "--strict" ]; then
        exit 2
    fi
fi

# All validations passed
log_success "All $CHECKED_ITEMS test plan items are verified"
log_success "PR #$PR_NUMBER is ready for merge"
exit 0
