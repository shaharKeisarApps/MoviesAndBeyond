# TMDB Movies App - Comprehensive Improvement Plan

## Orchestration Strategy

### Execution Model
```
ORCHESTRATOR (Claude Code)
├── Coordinates all tasks
├── Manages dependencies
├── Tracks progress
└── Spawns subagents for parallel work

SUBAGENTS (Parallel Execution)
├── Task 1: Build Logic Agent
├── Task 2: Theme Agent
├── Task 3: README/CI Agent
├── Task 4: Splash/Icon Agent
├── Task 5: Local User Agent
├── Task 6: Benchmark Agent
└── Task 7: Metro DI Agent (sequential - runs last)
```

### Task Dependency Graph
```
PARALLEL (Independent - can use subagents):
├── Task 1: Build Logic (type-safe convention plugins)
├── Task 2: Theme/Design (dynamic + seed color)
├── Task 3: README + GitHub + CI
├── Task 4: Splash + App Icon
├── Task 5: Local User Management
└── Task 6: Benchmark + Baseline Profiles

SEQUENTIAL (Run after all parallel tasks merged):
└── Task 7: Metro DI Migration (touches entire codebase)
```

### Worktree Strategy
Each task gets isolated worktree:
- Prevents conflicts
- Allows parallel development
- Clean PR history
- Easy rollback if needed

---

## Task Specifications

### Task 1: Build Logic - Type-Safe Convention Plugins

**Objective**: Consume convention plugins type-safely, not with string IDs

**Reference**: Insight App `/Users/shaharkeisar/Desktop/Projects/Repositories/KMP/MetroDITest`

**Current Problem**:
```kotlin
// BAD - String-based (current)
plugins {
    id("moviesandbeyond.android.application")
}
```

**Target Solution**:
```kotlin
// GOOD - Type-safe (like Insight)
plugins {
    alias(libs.plugins.moviesandbeyond.android.application)
}
```

**Implementation Steps**:
1. Study Insight app's `build-logic/` structure
2. Study how convention plugins are defined in version catalog
3. Update `libs.versions.toml` with plugin aliases
4. Update all module `build.gradle.kts` files
5. Verify type-safe consumption works
6. Build and test

**Worktree**: `../movies-worktree-plugins`
**Branch**: `feature/type-safe-plugins`

---

### Task 2: Theme/Design - Dynamic Theme + Seed Color

**Objective**: Dynamic theme default + seed color picker in settings

**Reference**: NoteNest Project

**Implementation Steps**:
1. Study NoteNest theme implementation
2. Update `Theme.kt` for dynamic color support
3. Create seed color options (palette)
4. Add color picker UI in settings screen
5. Persist selection with DataStore
6. Apply seed color throughout app
7. Test on Android 12+ (dynamic) and older (seed fallback)

**Worktree**: `../movies-worktree-theme`
**Branch**: `feature/dynamic-theme`

---

### Task 3: README + GitHub + CI/APK

**Objective**: Update docs, configure CI for APK releases

**Implementation Steps**:
1. Update README.md:
   - Add Store5 architecture section
   - Document offline-first approach
   - Add architecture diagram
2. Update screenshots with latest UI
3. Create `.github/workflows/release.yml`:
   - Trigger on push to main
   - Build release APK
   - Upload to GitHub Releases
   - Tag with version
4. Add download badge to README
5. Test CI workflow

**Worktree**: `../movies-worktree-readme`
**Branch**: `feature/readme-ci`

---

### Task 4: Splash Screen + App Icon

**Objective**: Update splash and icon using provided assets

**Source**: `/Users/shaharkeisar/Downloads/MoviesAndBeyond-icons`

**Implementation Steps**:
1. Inventory provided icon assets
2. Generate adaptive icon variants (if needed)
3. Update `res/mipmap-*` folders
4. Configure Splash Screen API (Android 12+)
5. Add backward-compatible splash for older versions
6. Update README with new app images
7. Test on various Android versions

**Worktree**: `../movies-worktree-splash`
**Branch**: `feature/splash-icon`

---

### Task 5: Local User Management

**Objective**: Local-only favorites/watchlist, fix indication, add animation

**Implementation Steps**:
1. Create local user data model
2. Create Room entities:
   - `FavoriteEntity`
   - `WatchlistEntity`
3. Create DAOs for CRUD operations
4. Create `LocalUserRepository`
5. Update UI to use local storage (not TMDB API)
6. Verify favorite indication works correctly
7. Implement delightful favorite animation:
   - Scale bounce (1.0 → 1.3 → 1.0)
   - Color transition
   - Optional particle effect
   - Haptic feedback
8. Test all flows

**Animation Spec**:
```kotlin
// Favorite toggle animation
val scale by animateFloatAsState(
    targetValue = if (isAnimating) 1.3f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)
// Color: unfavorite gray → favorite red/accent
// Duration: ~300-400ms total
// Haptic: performHapticFeedback on toggle
```

**Worktree**: `../movies-worktree-localuser`
**Branch**: `feature/local-user`

---

### Task 6: Benchmark + Baseline Profiles

**Objective**: Add benchmarks and baseline profiles like NowInAndroid

**Reference**: https://github.com/android/nowinandroid

**Implementation Steps**:
1. Fetch NowInAndroid benchmark module structure
2. Create `:benchmarks` module
3. Add macrobenchmark dependencies
4. Create benchmark tests:
   - StartupBenchmark
   - ScrollBenchmark (home screen)
   - NavigationBenchmark
5. Create `:baselineprofile` module
6. Generate baseline profiles
7. Configure CI for profile generation
8. Verify performance improvement

**Worktree**: `../movies-worktree-benchmarks`
**Branch**: `feature/benchmarks`

---

### Task 7: Metro DI Migration (SEQUENTIAL - LAST)

**Objective**: Replace Hilt with Metro DI

**Reference**: Insight App `/Users/shaharkeisar/Desktop/Projects/Repositories/KMP/MetroDITest`

**CRITICAL**: Run AFTER all other tasks merged to main

**Implementation Steps**:
1. Pull latest main (all other tasks merged)
2. Study Insight app Metro DI setup thoroughly
3. Add Metro dependencies
4. Create Metro component graph structure
5. Migrate incrementally:
   - Core/data modules first
   - Domain layer
   - Feature modules
   - App module last
6. Remove Hilt dependencies completely
7. Test all injection points
8. Verify app functionality

**Worktree**: `../movies-worktree-metro`
**Branch**: `feature/metro-di`

---

## Worktree Lifecycle

### Create
```bash
git worktree add -b <branch> <directory> main
cd <directory>
```

### Work
```bash
# Implement changes
./gradlew clean build  # Verify
git add -A
git commit -m "Task N: Description"
git push -u origin <branch>
```

### PR & Merge
```bash
# Create PR (GitHub CLI)
gh pr create --title "Task N: Title" --body "Description"

# Wait for CI to pass
# Review PR
# Merge via GitHub

# Or merge locally after CI:
gh pr merge --merge
```

### Cleanup
```bash
cd <original-repo>
git pull origin main
git worktree remove <directory>
git branch -d <branch>  # Safe if merged
```

---

## Autonomous Rules

### ORCHESTRATION
- Create MASTER_PLAN.md and TASK_PROGRESS.md first
- Track every step in progress tracker
- Use subagent pattern for parallel tasks
- Coordinate dependencies (Task 7 waits for 1-6)

### WORKTREES
- One task = one worktree
- Always branch from latest main
- Never merge without CI passing
- Always cleanup after merge

### CRITICAL
- **Task 7 (Metro DI) runs LAST** - depends on stable codebase
- **Wait for CI** before any merge
- **Delete worktrees** after successful merge
- **Pull main** before creating dependent worktrees

### DO
- Study reference projects thoroughly before implementing
- Test build after each major change
- Update progress tracker frequently
- Create detailed commit messages
- Verify CI status before and after merge

### DON'T
- Work on main branch directly
- Skip CI verification
- Leave worktrees after merge
- Start Task 7 before Tasks 1-6 complete
- Forget to update TASK_PROGRESS.md

---

## Final Deliverables

When ALL complete:

1. **Documents**
   - `MASTER_PLAN.md` - Complete orchestration plan
   - `TASK_PROGRESS.md` - All tasks marked complete

2. **Codebase**
   - Type-safe convention plugin consumption
   - Dynamic theme + seed color settings
   - Updated README with Store5 docs
   - CI releasing APKs on main merge
   - New splash screen and app icon
   - Local user with animated favorites
   - Benchmark + baseline profiles
   - Metro DI (Hilt removed)

3. **GitHub**
   - 7 PRs merged to main
   - CI green
   - APK in releases
   - All worktrees cleaned

4. **Verification**
   - App builds and runs correctly
   - All new features working
   - Performance improved
