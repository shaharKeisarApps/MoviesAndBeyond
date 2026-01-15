# Task Completion Quality Gates

This document defines the mandatory verification steps required before any task can be marked as complete in the MoviesAndBeyond project.

---

## Overview

Every task progresses through 6 quality gates. A task cannot be marked complete until ALL applicable gates pass.

---

## Gate 1: Implementation Complete

**Purpose:** Ensure code is written and functional.

### Checklist
- [ ] All required code written
- [ ] Code compiles without errors: `./gradlew compileDebugKotlin`
- [ ] Follows project architecture (see CLAUDE.md)
- [ ] Follows Clean Architecture boundaries
- [ ] No circular dependencies introduced

### Verification Command
```bash
./gradlew :module:compileDebugKotlin
```

### Blocking Conditions
- Compilation errors
- Architecture violations
- Missing required functionality

---

## Gate 2: Code Quality

**Purpose:** Ensure code meets quality standards.

### Checklist
- [ ] Code formatted: `./gradlew spotlessApply`
- [ ] Formatting verified: `./gradlew spotlessCheck`
- [ ] Static analysis clean: `./gradlew detekt`
- [ ] Lint checks pass: `./gradlew lint`
- [ ] No new warnings introduced

### Verification Commands
```bash
# Format code
./gradlew spotlessApply

# Verify all quality checks
./gradlew spotlessCheck detekt lint
```

### Blocking Conditions
- Spotless formatting violations
- Detekt rule violations
- High-severity lint errors

---

## Gate 3: Testing Complete

**Purpose:** Ensure code is properly tested.

### Checklist
- [ ] Unit tests written for new code
- [ ] Existing tests still pass: `./gradlew test`
- [ ] Edge cases covered
- [ ] Error paths tested
- [ ] Test coverage maintained or improved

### Verification Commands
```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :module:testDebugUnitTest
```

### Test Requirements by Change Type

| Change Type | Test Requirement |
|-------------|------------------|
| New feature | Unit tests for all public functions |
| Bug fix | Regression test proving fix |
| Refactor | All existing tests must pass |
| UI change | Screenshot test update (if applicable) |

### Blocking Conditions
- Any test failures
- Missing tests for new public APIs
- Test coverage decrease > 5%

---

## Gate 4: Code Review

**Purpose:** Ensure code quality through peer/AI review.

### Checklist
- [ ] Self-review using `code-reviewer` subagent
- [ ] All Blockers addressed
- [ ] All Suggestions addressed (or documented why not)
- [ ] Nitpicks addressed where reasonable

### Process

1. **Invoke code-reviewer:**
   ```
   Review the code changes for this task
   ```

2. **Address findings by severity:**
   - **Blockers**: Must fix before proceeding
   - **Suggestions**: Should fix or document reason for not fixing
   - **Nitpicks**: Optional but encouraged

3. **Re-review if significant changes made**

### Blocking Conditions
- Any unaddressed Blockers
- Code reviewer verdict: "Request Changes"

---

## Gate 5: Integration Verification

**Purpose:** Ensure changes integrate correctly with the full system.

### Checklist
- [ ] Full debug build passes: `./gradlew assembleDebug`
- [ ] All project tests pass: `./gradlew test`
- [ ] All code quality checks pass: `./gradlew check`
- [ ] No regressions in existing functionality
- [ ] Changes work with dependent modules

### Verification Commands
```bash
# Complete verification suite
./gradlew clean assembleDebug test check
```

### Blocking Conditions
- Build failure
- Any test failure
- Quality check failure

---

## Gate 6: PR Ready

**Purpose:** Ensure PR is complete and ready for review/merge.

### Checklist
- [ ] PR created with descriptive title
- [ ] PR description includes:
  - [ ] Summary of changes (1-3 bullet points)
  - [ ] Test plan with checkboxes
  - [ ] Related issue/task links
- [ ] All test plan items executed and checked
- [ ] All CI checks pass
- [ ] No unresolved review comments

### PR Description Template

```markdown
## Summary
- [Bullet 1: Main change]
- [Bullet 2: Secondary change]
- [Bullet 3: Additional notes]

## Test Plan
- [ ] Unit tests pass: `./gradlew :module:test`
- [ ] Build compiles: `./gradlew assembleDebug`
- [ ] [Feature-specific test 1]
- [ ] [Feature-specific test 2]

## Related
- Closes #[issue_number]
- Related to #[related_issue]
```

### Blocking Conditions
- Incomplete PR description
- Unchecked test plan items
- Failing CI checks
- Unresolved review comments

---

## Gate Summary

```
Gate 1: Implementation ─┐
                        │
Gate 2: Code Quality ───┼──► All 6 gates must pass
                        │
Gate 3: Testing ────────┤
                        │
Gate 4: Code Review ────┤
                        │
Gate 5: Integration ────┤
                        │
Gate 6: PR Ready ───────┘
                        │
                        ▼
                TASK COMPLETE
```

---

## Quick Reference: Full Verification

Run all gates in sequence:

```bash
# Gate 1: Implementation
./gradlew compileDebugKotlin

# Gate 2: Code Quality
./gradlew spotlessApply spotlessCheck detekt lint

# Gate 3: Testing
./gradlew test

# Gate 5: Integration (includes 1-3)
./gradlew clean assembleDebug test check

# Gate 4 & 6: Manual steps
# - Run code-reviewer subagent
# - Create PR with test plan
# - Execute and check off test plan items
```

---

## Exceptions

### When Gates Can Be Skipped

| Scenario | Skippable Gates |
|----------|-----------------|
| Documentation-only changes | Gate 3 (Testing) |
| CI/CD configuration | Gate 3 (Testing), Gate 4 (if trivial) |
| Dependency updates | Gate 3 (if no API changes) |
| Emergency hotfixes | None - all gates required |

**Note:** All exceptions must be documented in the PR description.

---

## Enforcement

These gates are enforced through:
1. **Hooks**: Pre-commit and pre-push hooks run automated checks
2. **Subagents**: code-reviewer and test-plan-validator enforce manual checks
3. **CI/CD**: GitHub Actions enforce all automated gates
4. **PR Process**: Test plan must be 100% checked before merge

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-15 | Initial version |
