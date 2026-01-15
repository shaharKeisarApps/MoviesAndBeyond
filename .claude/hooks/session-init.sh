#!/bin/bash
# Session Initialization Hook
#
# This hook runs at the start of each Claude Code session to:
# 1. Set up environment variables
# 2. Validate project state
# 3. Report current work status
#
# Exit codes:
#   0 - Success (continue session)
#   1 - Warning (non-blocking)
#   2 - Error (block session)

set -e

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
LOG_FILE="${REPO_ROOT}/.claude/session.log"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "$1" >> "$LOG_FILE" 2>/dev/null || true; }
info() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Initialize log
echo "=== Session Start: $(date) ===" >> "$LOG_FILE" 2>/dev/null || true

# ============================================
# Environment Setup
# ============================================

# Set project-specific environment
export GRADLE_OPTS="${GRADLE_OPTS:-} -Xmx2g"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home 2>/dev/null || echo '')}"

# Persist to CLAUDE_ENV_FILE if available
if [ -n "$CLAUDE_ENV_FILE" ]; then
    echo "export GRADLE_OPTS='$GRADLE_OPTS'" >> "$CLAUDE_ENV_FILE"
    [ -n "$JAVA_HOME" ] && echo "export JAVA_HOME='$JAVA_HOME'" >> "$CLAUDE_ENV_FILE"
fi

# ============================================
# Project State Validation
# ============================================

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    error "Not in a git repository"
    exit 2
fi

# Get current branch
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
log "Current branch: $BRANCH"

# Check for uncommitted changes
UNCOMMITTED=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
if [ "$UNCOMMITTED" -gt 0 ]; then
    warn "Found $UNCOMMITTED uncommitted changes"
    log "Uncommitted changes: $UNCOMMITTED"
fi

# Check if ahead/behind remote
if git rev-parse --abbrev-ref @{upstream} > /dev/null 2>&1; then
    AHEAD=$(git rev-list --count @{upstream}..HEAD 2>/dev/null || echo "0")
    BEHIND=$(git rev-list --count HEAD..@{upstream} 2>/dev/null || echo "0")

    if [ "$AHEAD" -gt 0 ]; then
        info "Branch is $AHEAD commits ahead of remote"
        log "Ahead of remote: $AHEAD"
    fi

    if [ "$BEHIND" -gt 0 ]; then
        warn "Branch is $BEHIND commits behind remote"
        log "Behind remote: $BEHIND"
    fi
fi

# ============================================
# Current Work Status
# ============================================

# Check for in-progress specs
IN_PROGRESS_SPEC="${REPO_ROOT}/.claude/specs/in-progress/CURRENT.md"
if [ -f "$IN_PROGRESS_SPEC" ]; then
    info "Found active work spec: .claude/specs/in-progress/CURRENT.md"
    log "Active spec found"
fi

# Check for pending PRs
if command -v gh &> /dev/null; then
    PR_COUNT=$(gh pr list --state open --limit 10 --json number 2>/dev/null | grep -c '"number"' || echo "0")
    if [ "$PR_COUNT" -gt 0 ]; then
        info "Found $PR_COUNT open pull requests"
        log "Open PRs: $PR_COUNT"
    fi
fi

# ============================================
# Quality Checks (Quick)
# ============================================

# Check if gradle wrapper exists
if [ ! -f "${REPO_ROOT}/gradlew" ]; then
    warn "Gradle wrapper not found"
    log "Missing gradlew"
fi

# Check for local.properties
if [ ! -f "${REPO_ROOT}/local.properties" ]; then
    warn "local.properties not found - API credentials may be missing"
    log "Missing local.properties"
fi

# ============================================
# Output Session Context
# ============================================

# Return session context for Claude
cat << EOF
{
  "session": {
    "branch": "$BRANCH",
    "uncommittedChanges": $UNCOMMITTED,
    "hasActiveSpec": $([ -f "$IN_PROGRESS_SPEC" ] && echo "true" || echo "false"),
    "openPRs": ${PR_COUNT:-0}
  },
  "recommendations": [
    $([ "$UNCOMMITTED" -gt 0 ] && echo '"Review uncommitted changes before starting new work",' || true)
    $([ "${BEHIND:-0}" -gt 0 ] && echo '"Consider pulling latest changes from remote",' || true)
    "null"
  ],
  "orchestration": {
    "specsPath": ".claude/specs/",
    "hooksPath": ".claude/hooks/",
    "skillsPath": ".claude/skills/",
    "agentsPath": ".claude/agents/",
    "orchestrationDoc": ".claude/ORCHESTRATION.md"
  }
}
EOF

# ============================================
# Final Status
# ============================================

info "Session initialized successfully"
log "Session init complete"

exit 0
