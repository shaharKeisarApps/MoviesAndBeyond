# MoviesAndBeyond Development Orchestration

This document defines the comprehensive development workflow for the MoviesAndBeyond project, ensuring consistent quality and proper coordination between Claude Code components.

---

## Overview

The orchestration system consists of:
- **17 Skills**: Domain-specific knowledge modules
- **5 Subagents**: Specialized AI agents for specific tasks
- **Hooks**: Automated validation and enforcement
- **Specs**: Formal specifications for features and tasks
- **Quality Gates**: Mandatory verification checkpoints

---

## Parallel-First Implementation Strategy

**CRITICAL:** This project enforces parallel execution by default for multi-task implementations.

### When to Use Parallel Subagents

Launch parallel subagents when ANY of these conditions apply:
- Multiple independent spec/feature implementations
- Database + Repository + ViewModel changes (different layers)
- Multiple feature modules being modified
- Exploration + implementation can happen concurrently

### Parallel Execution Patterns

**Pattern 1: Multi-Spec Execution**
```
User: "Execute pending specs: local-favorites, baseline-profiles"

Action: Launch TWO parallel subagents:
- Subagent 1: Implements local-favorites (data-layer-specialist)
- Subagent 2: Implements baseline-profiles (general implementation)
```

**Pattern 2: Multi-Layer Implementation**
```
User: "Add offline caching to movie details"

Action: Launch THREE parallel subagents:
- data-layer-specialist: Entity + DAO + Repository
- presentation-layer-specialist: ViewModel + UI state
- di-layer-specialist: Hilt module bindings
```

**Pattern 3: Exploration + Implementation**
```
User: "Research best approach and implement feature X"

Action: Launch TWO parallel subagents:
- technical-researcher: Investigate approaches
- Explore agent: Analyze existing codebase patterns
Then: Sequential implementation based on findings
```

### When NOT to Parallelize

Only run sequentially when:
- Output of one task is input to another
- Shared file modifications would conflict
- Dependency on build verification between steps

### Enforcement

The UserPromptSubmit hook reminds about parallel execution. For multi-task requests:
1. Identify independent subtasks
2. Route to appropriate specialist subagents
3. Launch ALL independent subagents in a SINGLE message
4. Aggregate results and verify

---

## Development Flow

### 1. Task Initiation

**Trigger:** User describes a feature, bug fix, or enhancement.

**Actions:**
1. Create spec in `.claude/specs/features/` (for features) or `.claude/specs/tasks/` (for tasks)
2. Break down into subtasks if complex
3. **Identify independent subtasks for parallel execution**
4. Identify relevant skills to activate
5. Assign to appropriate subagent if specialized work
6. **Launch parallel subagents for independent work**

**Example:**
```
User: "Add dark mode toggle to settings"

Action:
1. Create .claude/specs/features/dark-mode-toggle.md
2. Identify skills: compose-expert, material3-expressive
3. Plan UI, state, and persistence changes
```

**Parallel Example:**
```
User: "Execute pending specs: local-favorites, baseline-profiles"

Action: Launch parallel subagents in SINGLE message:
- Task(data-layer-specialist): "Implement local-favorites..."
- Task(general-purpose): "Implement baseline-profiles..."
```

### 2. Implementation

**Trigger:** Task assigned for implementation.

**Actions:**
1. Activate relevant skills via Skill tool
2. Follow CLAUDE.md architecture patterns
3. Write tests alongside code (TDD when appropriate)
4. Use TodoWrite to track progress

**Skills by Domain:**

| Domain | Skills |
|--------|--------|
| UI/Compose | compose-expert, compose-viewmodel-bridge |
| Navigation | navigation3 |
| DI | metro-expert |
| Networking | ktor-expert |
| Data/Caching | store5-expert, store5-room-bridge |
| Testing | testing-expert, viewmodel-testing-expert |
| Architecture | usecase-expert, circuit-expert |
| Build | gradle-expert, cicd-expert |
| Code Quality | quality-expert |
| Coroutines | coroutines-expert |
| Git | git-expert |
| Research | technical-researcher |

### 3. Self-Review

**Trigger:** Implementation complete.

**Actions:**
1. Invoke `code-reviewer` subagent
2. Address all Blockers (mandatory)
3. Address all Suggestions (recommended)
4. Document why Nitpicks are skipped (if any)

**Command:**
```
Review the changes for [task name]
```

### 4. Quality Gates

**Trigger:** Review complete.

**Actions:**
1. Run all quality checks:
   ```bash
   ./gradlew clean assembleDebug test check
   ```
2. Verify all gates pass (see `.claude/specs/tasks/task-completion-gates.md`)

**Gates:**
1. Implementation Complete
2. Code Quality (spotless, detekt, lint)
3. Testing Complete
4. Code Review Passed
5. Integration Verified
6. PR Ready

### 5. PR Creation

**Trigger:** All gates pass.

**Actions:**
1. Create PR with standard template
2. Include complete test plan
3. Link to spec and related issues

**PR Template:**
```markdown
## Summary
- [Main change]
- [Secondary change]

## Test Plan
- [ ] Unit tests pass: `./gradlew :module:test`
- [ ] Build compiles: `./gradlew assembleDebug`
- [ ] [Feature-specific test 1]
- [ ] [Feature-specific test 2]

## Related
- Closes #[issue]
- Spec: `.claude/specs/features/[spec-name].md`
```

### 6. Test Plan Execution

**Trigger:** PR created.

**Actions:**
1. Execute each test plan item
2. Use `test-plan-validator` subagent for automation
3. Check off completed items in PR description
4. Document any failures

**Command:**
```
Verify test plan for PR #N
```

### 7. Merge Approval

**Trigger:** Test plan 100% complete.

**Actions:**
1. Verify all CI checks pass
2. Run pre-merge hook: `.claude/hooks/pre-pr-merge.sh <PR_NUMBER>`
3. Get explicit user approval
4. Merge to main

---

## Quality Enforcement

### Hooks Configuration

| Hook | Type | Purpose |
|------|------|---------|
| pre-pr-merge.sh | Bash | Validate test plan completion |
| (Future) pre-commit | Bash | Run spotlessCheck |
| (Future) pre-push | Bash | Run test + detekt |

### Blocking Conditions

A task/PR is **BLOCKED** if:
- Any test plan item is unchecked
- CI checks are failing
- Code review has unaddressed Blockers
- Quality gates fail (spotless, detekt, lint)
- Integration tests fail

### Mandatory Steps

These steps **CANNOT be skipped**:
1. Code review (even for small changes)
2. Test plan execution
3. Quality gate verification
4. User approval for merge

---

## Layer-Based Agent Routing

### Routing Rules

When a task involves specific architectural layers, route to the appropriate specialist:

| Task Involves | Route To | Triggers |
|---------------|----------|----------|
| Room entities, DAOs, Store5, Retrofit, Hilt modules | **data-layer-specialist** | "repository", "Room", "Store5", "cache", "API", "DAO", "Fetcher", "SourceOfTruth", "StoreReadResponse" |
| @HiltViewModel, StateFlow, stateInWhileSubscribed, Compose UI | **presentation-layer-specialist** | "ViewModel", "screen", "UI state", "StateFlow", "navigation", "WhileSubscribedOrRetained" |
| @Module, @Provides, @Binds, scoping, DI setup | **di-layer-specialist** | "Hilt", "@Module", "@Provides", "@Binds", "injection", "dependency", "StoreModule", "SingletonComponent" |

### Skill Composition by Layer

| Layer | Primary Skills | Secondary Skills |
|-------|---------------|------------------|
| Data | store5-room-bridge, store5-expert | coroutines-expert |
| Presentation | compose-viewmodel-bridge, compose-expert | navigation3, compose-expert |
| DI | (patterns in CLAUDE.md) | - |
| Testing | viewmodel-testing-expert, testing-expert | coroutines-expert |
| Cross-Layer | usecase-expert | - |

### Critical Patterns to Enforce

#### ViewModel StateFlow Pattern

**ALWAYS use the project's custom `stateInWhileSubscribed()`:**
```kotlin
// ✅ CORRECT - Uses WhileSubscribedOrRetained
val movies: StateFlow<ContentUiState> = flow
    .stateInWhileSubscribed(
        scope = viewModelScope,
        initialValue = ContentUiState(category)
    )
```

**NEVER use standard WhileSubscribed with timeout:**
```kotlin
// ❌ WRONG - Causes issues during configuration changes
val movies: StateFlow<ContentUiState> = flow
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContentUiState(category)
    )
```

**Rationale:** `WhileSubscribedOrRetained` waits for Choreographer frame + Handler queue before stopping, preventing issues during device rotation and configuration changes.

#### StoreReadResponse Handling

Always handle all 4 states:
```kotlin
when (response) {
    is StoreReadResponse.Loading -> // Show loading state
    is StoreReadResponse.Data -> // Update UI with data, check isFromCache
    is StoreReadResponse.Error -> // Surface error to user
    is StoreReadResponse.NoNewData -> // Handle stale cache scenario
}
```

#### Pagination Accumulation

Use `flatMapLatest` + `combine` pattern:
```kotlin
val items: StateFlow<ContentUiState> =
    _page.flatMapLatest { page ->
        repository.observeItems(category, page)
            .map { response -> handleResponse(response, page) }
    }
    .combine(_accumulated) { state, items -> state.copy(items = items) }
    .stateInWhileSubscribed(viewModelScope, ContentUiState())
```

---

## Subagent Reference

### Layer Specialists

#### data-layer-specialist
**When to use:** Repository implementation, Store5 configuration, Room entities/DAOs, API integration
**Trigger phrases:** "repository", "Room", "Store5", "cache", "Fetcher", "SourceOfTruth"
**Output:** Data layer code following offline-first patterns

#### presentation-layer-specialist
**When to use:** ViewModel creation, Compose screens, StateFlow management, Navigation 3 routes
**Trigger phrases:** "ViewModel", "screen", "UI state", "stateInWhileSubscribed"
**Output:** Presentation code with proper lifecycle handling

#### di-layer-specialist
**When to use:** Hilt module configuration, dependency bindings, scoping decisions
**Trigger phrases:** "Hilt", "@Module", "@Provides", "@Binds", "injection"
**Output:** DI configuration following project patterns

### General Purpose

### code-reviewer
**When to use:** Before any PR/commit
**Trigger phrases:** "review code", "check code", "best practices"
**Output:** Review report with Blockers/Suggestions/Nitpicks

### test-plan-validator
**When to use:** Before PR merge
**Trigger phrases:** "verify test plan", "ready to merge"
**Output:** Test execution report with pass/fail status

### bug-fixer
**When to use:** When errors/crashes are reported
**Trigger phrases:** "bug in", "crash", "doesn't work", "fix issue"
**Output:** Root cause analysis and fix

### refactor-expert
**When to use:** When migrating or restructuring code
**Trigger phrases:** "migrate to", "refactor", "convert", "move to"
**Output:** Safe transformation plan and execution

### release-manager
**When to use:** For release preparation
**Trigger phrases:** "prepare release", "create version", "tag release"
**Output:** Changelog, version bump, release notes

---

## Skill Activation Guide

### Automatic Triggers

Skills activate automatically based on context:

```
User mentions "Compose" → compose-expert activates
User mentions "coroutines" → coroutines-expert activates
User mentions "Store5" → store5-expert activates
```

### Manual Invocation

Force skill activation:
```
Use the compose-expert skill to help with this UI
```

### Skill Composition

Chain multiple skills:
```
Use compose-expert for the UI and testing-expert to write tests
```

---

## Spec-Driven Development

### When to Create Specs

Create a spec for:
- New features
- Significant refactors
- Architecture changes
- Multi-phase tasks

### Spec Locations

```
.claude/specs/
├── features/     # Feature specifications
├── tasks/        # Task-specific specs
├── plans/        # Pending architectural decisions
└── in-progress/  # Current active work
```

### Spec Template

See `.claude/specs/features/local-favorites-dual-user.md` for reference.

---

## Task Orchestrator Integration

The project uses the Task Orchestrator MCP plugin for advanced workflow management. This provides structured feature development, dependency tracking, and specialist routing.

### Architecture Overview

**Four-Tier Hybrid System:**

| Tier | Purpose | Token Cost | When to Use |
|------|---------|-----------|-------------|
| Direct Tools | Single operations | 50-100 tokens | Simple queries |
| Skills | Coordination (2-5 tools) | 300-600 tokens | "What's next?", status updates |
| Subagents | Complex implementation | 1,500-3,000 tokens | Feature creation, debugging |
| Hooks | Side effects/automation | 0 tokens | Git commits, notifications |

### Specialist Routing

| Specialist | Model | Domain |
|------------|-------|--------|
| Feature Architect | Opus | Complex feature design, PRD analysis |
| Planning Specialist | Sonnet | Task breakdown, dependencies |
| Implementation Specialist | Haiku | General implementation (default) |
| Senior Engineer | Sonnet | Debugging, architecture challenges |

### Available Skills

```
task-orchestrator:feature-orchestration    # Feature lifecycle management
task-orchestrator:task-orchestration       # Dependency-aware task execution
task-orchestrator:dependency-orchestration # Critical path analysis
task-orchestrator:status-progression       # Status workflow navigation
task-orchestrator:dependency-analysis      # Blocker detection
task-orchestrator:backend-implementation   # Backend/Kotlin tasks
task-orchestrator:testing-implementation   # Testing tasks
task-orchestrator:documentation-implementation # Docs tasks
```

### Main Workflow: coordinate_feature_development

The recommended flow for new features:

**Step 1: Create Plan File**
```markdown
# Feature: [Name]

## Summary
[1-2 sentence overview]

## Requirements
- [Requirement 1]
- [Requirement 2]

## Technical Considerations
- [Architecture notes]
```

**Step 2: Run Orchestration**
```
Run coordinate_feature_development with my [feature-name].md plan file
```

**Phase 1: Feature Architecture (Opus)**
- Analyzes requirements
- Creates feature with templates
- Returns feature ID

**Phase 2: Task Breakdown (Sonnet)**
- Decomposes into 5-8 focused tasks
- Applies task templates
- Establishes dependency chains
- Tags for specialist routing

**Step 3: Execute Tasks**
```
What's next?
```
This triggers automatic skill-based coordination for the next available task.

### Token Efficiency

Task Orchestrator uses summary-based context passing:
- Each completed task generates ~400-token summary
- Subsequent tasks read only relevant summaries
- Result: 97% token reduction for complex projects
- Scales to 100+ tasks (vs. 12-15 with traditional approach)

### Integration Pattern

```
1. Create plan.md with requirements
2. Run coordinate_feature_development → Feature ID + Tasks created
3. Ask "What's next?" → Skill routes to appropriate specialist
4. Specialist completes task → Summary generated
5. Repeat step 3 until all tasks complete
6. Final verification → Feature marked complete
```

---

## Metrics and Tracking

### Per-Task Metrics

Track for each task:
- Time to completion
- Gates passed/failed
- Review iterations
- Test coverage delta

### Quality Indicators

Monitor:
- Build success rate
- Test pass rate
- Code quality score (detekt)
- PR merge time

---

## Emergency Procedures

### Hotfix Process

For critical production issues:
1. Create hotfix branch from main
2. Implement minimal fix
3. Run quick verification: `./gradlew assembleDebug test`
4. Skip full gate process (document exception)
5. Merge with expedited review
6. Follow up with full gates on next release

### Rollback Process

If a merge causes issues:
1. Identify problematic commit
2. Create revert PR
3. Execute minimal test plan
4. Merge revert immediately
5. Investigate root cause

---

## Continuous Improvement

### Feedback Loop

After each sprint/milestone:
1. Review blocked PRs
2. Identify common gate failures
3. Update specs/hooks as needed
4. Document lessons learned

### Orchestration Updates

This document evolves with the project:
- Add new skills as created
- Update hooks as workflow matures
- Refine gates based on experience

---

## Quick Reference

### Commands

```bash
# Full verification
./gradlew clean assembleDebug test check

# Code quality only
./gradlew spotlessApply spotlessCheck detekt lint

# Test specific module
./gradlew :feature:movies:testDebugUnitTest

# Pre-merge validation
.claude/hooks/pre-pr-merge.sh <PR_NUMBER>
```

### Subagent Invocations

```
Review the code for [task]              → code-reviewer
Verify test plan for PR #N              → test-plan-validator
Fix the bug in [area]                   → bug-fixer
Refactor [component] to use [pattern]   → refactor-expert
Prepare release for version X.Y.Z       → release-manager
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.2 | 2026-01-15 | Added Parallel-First Implementation Strategy, updated hooks for parallel enforcement, added parallel execution patterns |
| 1.1 | 2026-01-15 | Added layer-based agent routing, 3 layer specialists, critical patterns (stateInWhileSubscribed, StoreReadResponse), new bridge skills |
| 1.0 | 2026-01-15 | Initial orchestration document |
