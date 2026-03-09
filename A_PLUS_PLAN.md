# A+ Achievement Plan — MoviesAndBeyond

> **Goal**: Elevate project from A (90/100) to A+ (96+/100)
> **Strategy**: Claude Code Agent Teams with parallel work streams
> **Reference**: [Agent Teams Docs](https://code.claude.com/docs/en/agent-teams)

---

## Team Architecture

```
┌───────────────────────────────────────────────────────┐
│                   TEAM LEAD (you)                     │
│  Orchestrates phases, reviews merges, validates       │
├───────────┬───────────┬───────────┬───────────────────┤
│ Agent 1   │ Agent 2   │ Agent 3   │ Agent 4           │
│ data-     │ compose-  │ viewmodel-│ build-polish      │
│ layer     │ stability │ fixes     │                   │
│           │           │           │                   │
│ R1,R3,R4  │ R2        │ R5,R6,R7  │ R9,R10,R11        │
│           │           │ R8        │                   │
└───────────┴───────────┴───────────┴───────────────────┘
```

---

## Phase Overview

| Phase | Items | Agents | Est. Impact | Gate |
|-------|-------|--------|-------------|------|
| **Phase 2A** | R4, R5, R6, R7, R9, R10, R11 | 3 parallel | +3 pts | CI + device |
| **Phase 2B** | R2 (ImmutableList) | 1 focused | +1.5 pts | CI + device |
| **Phase 2C** | R3 (LibraryRepo split) | 1 focused | +1 pt | CI + device + tests |
| **Phase 2D** | R1, R8 (Tests) | 2 parallel | +4 pts | CI (all tests green) |
| **Merge** | PR to dev → main | Lead | — | Full CI + on-device |

**Total projected**: +9.5 pts → 90 + 9.5 ≈ 96+ (A+)

---

## Phase 2A: Quick Wins (Parallel, ~30 min)

Three agents work simultaneously on independent, low-risk fixes.

### Agent: viewmodel-fixes

**Items**: R5 (DetailsViewModel parallel queries), R6 (AuthViewModel init), R7 (YouViewModel side effect)

```
Prompt for agent (subagent_type: refactor-expert):

Fix 3 ViewModel issues in MoviesAndBeyond:

1. **DetailsViewModel parallel queries** (`feature/details/.../DetailsViewModel.kt`):
   - In `handleMovieDetailsResponse` and `handleTvDetailsResponse`, the two
     `libraryRepository.itemInFavoritesExists()` and `itemInWatchlistExists()`
     calls are sequential inside `_uiState.update {}`.
   - Fix: Move queries OUTSIDE the update block. Use `coroutineScope { }` with
     two `async { }` blocks, await both, THEN call `_uiState.update` with results.
   - Run existing test: `./gradlew :feature:details:test`

2. **AuthViewModel init pattern** (`feature/auth/.../AuthViewModel.kt`):
   - Replace the `init { launch { first() } }` pattern for `_hideOnboarding`.
   - Fix: Derive as a proper StateFlow:
     ```kotlin
     val hideOnboarding: StateFlow<Boolean?> =
         userRepository.userData
             .map { it.hideOnboarding }
             .stateInWhileSubscribed(scope = viewModelScope, initialValue = null)
     ```
   - Remove the `_hideOnboarding` MutableStateFlow and the init block.
   - Run existing test: `./gradlew :feature:auth:test`

3. **YouViewModel side effect** (`feature/you/.../YouViewModel.kt`):
   - The `onEach { if (isLoggedIn) getAccountDetails() }` fires on every
     re-subscription.
   - Fix: Remove `onEach` from the flow chain. Add a separate collection in init:
     ```kotlin
     init {
         viewModelScope.launch {
             authRepository.isLoggedIn
                 .distinctUntilChanged()
                 .filter { it }
                 .collect { getAccountDetails() }
         }
     }
     ```
   - Keep the existing `isLoggedIn` StateFlow without `onEach`.
   - Run existing test: `./gradlew :feature:you:test`

After all 3 fixes, run: `./gradlew spotlessApply && ./gradlew spotlessCheck detekt lintDebug test`
```

### Agent: build-polish

**Items**: R9 (version catalog bundles), R10 (README versions), R11 (RatingBadge colors)

```
Prompt for agent (subagent_type: refactor-expert):

Fix 3 build/polish issues in MoviesAndBeyond:

1. **Version catalog bundles** (`gradle/libs.versions.toml`):
   - The `[bundles]` section is empty. Add bundles for commonly co-used deps:
     ```toml
     [bundles]
     ktor = ["ktor-client-core", "ktor-client-okhttp", "ktor-client-content-negotiation", "ktor-serialization-kotlinx-json", "ktor-client-auth"]
     landscapist = ["landscapist-coil3", "landscapist-placeholder", "landscapist-animation"]
     compose-testing = ["compose-ui-test-junit4", "compose-ui-test-manifest"]
     ```
   - Update module build.gradle.kts files that use these deps to use the bundles.
   - Verify: `./gradlew assembleDebug`

2. **README version drift** (`README.md`):
   - Read `gradle/libs.versions.toml` for actual versions.
   - Update ALL stale versions in README.md to match. Key ones:
     Kotlin, Compose BOM, Hilt, Store5, Landscapist, Navigation, Nav3, Haze,
     Material Kolor, colorpicker, WorkManager, Serialization, Spotless, ktfmt, AGP, Screenshot Tests.

3. **RatingBadge content text colors** (`core/ui/.../RatingBadge.kt`):
   - In `getRatingContentColor()` (lines 68-73): replace `Color(0xFF1C1B1F)`
     with `MaterialTheme.colorScheme.onSurface` and `Color.White` with
     `MaterialTheme.colorScheme.inverseOnSurface` (or appropriate theme token).
   - NOTE: Keep `RatingColors` object colors (green/amber/orange/red/gold) as-is —
     these are semantic rating colors that should NOT change with theme.
   - Run: `./gradlew spotlessApply && ./gradlew spotlessCheck detekt lintDebug`
```

### Agent: error-handling-fix

**Item**: R4 (inconsistent error handling in LibraryRepositoryImpl)

```
Prompt for agent (subagent_type: data-layer-specialist):

Fix inconsistent error handling in LibraryRepositoryImpl:

File: `data/src/main/java/com/keisardev/moviesandbeyond/data/repository/impl/LibraryRepositoryImpl.kt`

Issues:
- `addOrRemoveFavorite()` (line 116-118): catches `IOException` then immediately
  re-throws it — the catch block is pointless and misleading.
- `addOrRemoveFromWatchlist()` (line 167-169): same problem.

These methods are called from DetailsViewModel which wraps them in try/catch,
so the thrown exceptions ARE caught upstream. But the pattern is inconsistent
with other repos that return `Result<T>`.

Fix approach:
- Since these methods are called from DetailsViewModel's `addOrRemoveFavorite()`
  and `addOrRemoveFromWatchlist()` which already have try/catch blocks,
  simply REMOVE the misleading `catch (e: IOException) { throw e }` blocks.
- The outer try/catch in the same methods handles the real error cases.
- Do NOT change the method signatures — that would require updating all callers.

Also update the TestLibraryRepository to match if needed.

Run: `./gradlew spotlessApply && ./gradlew :data:test :feature:details:test`
```

### Phase 2A Gate
```bash
# After all 3 agents complete:
./gradlew spotlessCheck detekt lintDebug test
./gradlew installDebug
# On-device: verify all tabs, details screen, auth flow, library actions
```

### Git Handling for Phase 2A
```bash
git checkout -b feature/a-plus-quick-wins dev
# ... all agent work ...
git add -A && git commit -m "fix: ViewModel patterns, error handling, build polish"
./gradlew spotlessCheck detekt lintDebug test  # gate
# on-device validation
git checkout dev && git merge feature/a-plus-quick-wins
git push origin dev
git branch -d feature/a-plus-quick-wins
```

---

## Phase 2B: ImmutableList Migration (~20 min)

Single focused agent — this change touches many files and must be consistent.

### Agent: compose-stability

```
Prompt for agent (subagent_type: refactor-expert):

Migrate List<T> to ImmutableList<T> for Compose stability in MoviesAndBeyond.

Step 1: Add dependency
- In `gradle/libs.versions.toml`:
  - Add version: `kotlinx-collections-immutable = "0.3.8"`
  - Add library: `kotlinx-collections-immutable = { group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable", version.ref = "kotlinx-collections-immutable" }`
- In `core/ui/build.gradle.kts`: add `api(libs.kotlinx.collections.immutable)`
  (api so features inherit it)

Step 2: Update UI state data classes
Replace `List<T>` with `ImmutableList<T>` in these state classes:
- `feature/movies/.../MoviesViewModel.kt` → `ContentUiState.items`
- `feature/tv/.../TvShowsViewModel.kt` → `ContentUiState.items`
- `feature/search/.../SearchViewModel.kt` → `searchSuggestions` StateFlow type
- `feature/you/.../library_items/LibraryItemsViewModel.kt` → items StateFlows
- Update `emptyList()` defaults to `persistentListOf()`
- Update accumulation: `.toImmutableList()` or `.toPersistentList()` where lists are built

Step 3: Update composable parameters
Replace `List<T>` params with `ImmutableList<T>` in:
- `feature/movies/.../FeedScreen.kt` composable params
- `feature/tv/.../FeedScreen.kt` composable params
- `feature/search/.../SearchScreen.kt` composable params
- `feature/you/.../library_items/LibraryItemsScreen.kt` composable params
- `core/ui/.../HeroCarousel.kt` composable params

Step 4: Update model classes (if needed for domain → UI mapping)
- Check if `MovieDetails.recommendations`, `TvDetails.recommendations` should use
  ImmutableList. If they're domain models, keep as List and convert at the
  ViewModel/mapping layer.

Import: `kotlinx.collections.immutable.ImmutableList`, `kotlinx.collections.immutable.persistentListOf`, `kotlinx.collections.immutable.toImmutableList`

Run after each file: `./gradlew :feature:movies:compileDebugKotlin` (fast check)
Run at end: `./gradlew spotlessApply && ./gradlew spotlessCheck detekt lintDebug test`
```

### Phase 2B Gate
```bash
./gradlew spotlessCheck detekt lintDebug test
./gradlew installDebug
# On-device: all tabs render, scroll performance, pagination works
```

### Git Handling for Phase 2B
```bash
git checkout -b feature/immutable-list-migration dev
# ... agent work ...
git add -A && git commit -m "perf: migrate List to ImmutableList for Compose stability"
./gradlew spotlessCheck detekt lintDebug test  # gate
# on-device validation
git checkout dev && git merge feature/immutable-list-migration
git push origin dev
git branch -d feature/immutable-list-migration
```

---

## Phase 2C: LibraryRepositoryImpl DRY Refactor (~30 min)

Single focused agent — reducing duplication with Kotlin higher-order functions.

**Design decision (GDE-validated):** The original plan to extract `FavoriteSyncManager`
and `WatchlistSyncManager` was REJECTED — it would create two nearly-identical classes.
The real code smell is **duplication**, not SRP. The existing interfaces
(`Synchronizer`, `UserLibrarySyncOperations`, `LibraryTaskSyncOperation`) are already
well-designed. Fix: reduce duplication with shared private helpers.

### Agent: data-refactor

```
Prompt for agent (subagent_type: refactor-expert):

Refactor LibraryRepositoryImpl (517 lines) to reduce duplication using Kotlin
higher-order functions. Do NOT extract separate manager classes.

File: `data/src/main/java/com/keisardev/moviesandbeyond/data/repository/impl/LibraryRepositoryImpl.kt`

The problem is DUPLICATION, not SRP:
- addOrRemoveFavorite ≈ addOrRemoveFromWatchlist (same logic, different DAO)
- syncFavorites ≈ syncWatchlist (same logic, different DAO)

Fix:
1. Create private helper `addOrRemoveCollectionItem()` with DAO operations as lambdas:
   - checkExists, markForDeletion, deleteItem, insertItem params
   - createTask lambda for LibraryTask creation
   - Both addOrRemoveFavorite and addOrRemoveFromWatchlist delegate to it

2. Create private helper `syncCollection()` with DAO operations as lambdas:
   - fetchItems, getItem, syncItems params matching the DAO-specific calls
   - LibraryItemType param to distinguish favorites vs watchlist
   - Both syncFavorites and syncWatchlist delegate to it

3. Remove the pointless `catch (e: IOException) { throw e }` in addOrRemoveFavorite
   (line 116-118) and addOrRemoveFromWatchlist (line 167-169).

4. Do NOT change LibraryRepository interface or any external callers.
5. Do NOT create new classes or files — keep everything in LibraryRepositoryImpl.

Target: 517 lines → ~350 lines, zero behavior change.

Run: `./gradlew spotlessApply && ./gradlew spotlessCheck detekt lintDebug :data:test :feature:details:test :feature:you:test`
```

### Phase 2C Gate
```bash
./gradlew spotlessCheck detekt lintDebug test
./gradlew installDebug
# On-device: add/remove favorites, add/remove watchlist, verify library counts
```

### Git Handling for Phase 2C
```bash
git checkout -b refactor/library-repository-split dev
# ... agent work ...
git add -A && git commit -m "refactor: extract sync managers from LibraryRepositoryImpl"
./gradlew spotlessCheck detekt lintDebug test  # gate
# on-device validation
git checkout dev && git merge refactor/library-repository-split
git push origin dev
git branch -d refactor/library-repository-split
```

---

## Phase 2D: Repository + ViewModel Tests (~45 min)

Two agents working in parallel — tests are independent of each other.

### Agent 1: repo-tests (subagent_type: general-purpose)

```
Prompt:

Write comprehensive unit tests for ALL 6 repository implementations in MoviesAndBeyond.

Test doubles and fixtures already exist in:
- `data/src/main/java/.../data/testdoubles/` (TestItemDetails, TestLibraryItems, TestSearchResults)
- Network/DAO mocking will use fakes or Mockito

Create these test files in `data/src/test/java/com/keisardev/moviesandbeyond/data/repository/impl/`:

1. **AuthRepositoryImplTest.kt** (~10 tests):
   - login success → returns Result.Success
   - login with wrong credentials → returns Result.Error(NetworkError.Unauthorized)
   - login IOException → returns Result.Error
   - logout success
   - logout error
   - isLoggedIn flow reflects session state

2. **ContentRepositoryImplTest.kt** (~12 tests):
   - observeMovieItems emits Loading then Success
   - observeMovieItems emits Error on network failure
   - observeTvShowItems same pattern
   - Store5 caching behavior: second call returns cached data
   - Pagination: different pages return different data

3. **DetailsRepositoryImplTest.kt** (~8 tests):
   - getMovieDetails success
   - getMovieDetails not found → Result.Error(NetworkError.NotFound)
   - getTvShowDetails success/error
   - getPersonDetails success/error

4. **LibraryRepositoryImplTest.kt** (~20 tests):
   - addOrRemoveFavorite adds when not present
   - addOrRemoveFavorite removes when present
   - addOrRemoveFromWatchlist same pattern
   - itemInFavoritesExists returns true/false
   - itemInWatchlistExists returns true/false
   - syncLocalItemsWithTmdb syncs correctly
   - observeFavorites/observeWatchlist emit updates
   - Error cases for sync operations

5. **SearchRepositoryImplTest.kt** (~6 tests):
   - getSearchSuggestions returns results
   - getSearchSuggestions empty query → empty list
   - getSearchSuggestions network error
   - includeAdult parameter passed correctly

6. **UserRepositoryImplTest.kt** (~6 tests):
   - userData flow emits preferences
   - setHideOnboarding updates DataStore
   - setDarkMode updates DataStore
   - updateAccountDetails success/error

For each test:
- Use JUnit4 + kotlinx.coroutines.test (runTest, TestDispatcher)
- Use MainDispatcherRule from core:testing
- Create fake DAOs/API clients or use Mockito where simpler
- Follow AAA pattern (Arrange, Act, Assert)
- Use Turbine for Flow testing where appropriate

Run after each file: `./gradlew :data:test`
Run at end: `./gradlew spotlessApply && ./gradlew spotlessCheck detekt :data:test`
```

### Agent 2: mainactivity-tests (subagent_type: general-purpose)

```
Prompt:

Write unit tests for MainActivityViewModel in MoviesAndBeyond.

File to test: `app/src/main/java/com/keisardev/moviesandbeyond/MainActivityViewModel.kt`

Create: `app/src/test/java/com/keisardev/moviesandbeyond/MainActivityViewModelTest.kt`

Dependencies to mock/fake:
- `UserRepository` → use `TestUserRepository` from `data/testdoubles/repository/`
- `AuthRepository` → use `TestAuthRepository` from `data/testdoubles/repository/`
- `SyncScheduler` → create a simple fake or use Mockito

Tests (~8 cases):
1. Initial uiState is Loading
2. uiState transitions to Success when userData emits
3. Success state contains correct useDynamicColor from preferences
4. Success state contains correct darkMode from preferences
5. Success state contains correct hideOnboarding from preferences
6. executeLibrarySyncWork calls syncScheduler when logged in
7. executeLibrarySyncWork does NOT call syncScheduler when logged out
8. Success state contains correct customColorArgb from preferences

Use:
- JUnit4 + runTest + TestDispatcher
- MainDispatcherRule from core:testing
- Turbine for StateFlow testing

Run: `./gradlew spotlessApply && ./gradlew :app:testDebugUnitTest`
```

### Phase 2D Gate
```bash
./gradlew spotlessCheck detekt lintDebug test
# Verify JaCoCo coverage thresholds pass (data: 80%, app: 70%)
```

### Git Handling for Phase 2D
```bash
git checkout -b test/repository-and-viewmodel-tests dev
# ... both agents work ...
git add -A && git commit -m "test: add repository implementation and MainActivityViewModel tests"
./gradlew spotlessCheck detekt lintDebug test  # gate
git checkout dev && git merge test/repository-and-viewmodel-tests
git push origin dev
git branch -d test/repository-and-viewmodel-tests
```

---

## Final Merge to Main

After all phases are on `dev` and CI is green:

```bash
# Ensure dev is fully up to date
git checkout dev && git pull origin dev

# Full CI verification
./gradlew clean spotlessCheck detekt lintDebug test assembleDebug

# On-device final validation
./gradlew installDebug
# Test: all tabs, details, search, auth, favorites, watchlist, rotation, back nav

# Create PR: dev → main
gh pr create --base main --head dev --title "Project diagnosis: A+ quality improvements" --body "$(cat <<'EOF'
## Summary
- Fix ViewModel patterns (parallel queries, init races, side effects)
- Migrate to ImmutableList for Compose stability
- Refactor LibraryRepositoryImpl (SRP, extract sync managers)
- Add comprehensive repository tests (6 impl test files)
- Add MainActivityViewModel tests
- Fix inconsistent error handling
- Add version catalog bundles
- Update README with current dependency versions
- Polish RatingBadge theme colors

## Test Plan
- [ ] All unit tests pass (`./gradlew test`)
- [ ] CI pipeline green (spotless, detekt, lint, test)
- [ ] On-device: Movies tab loads, scroll, pagination
- [ ] On-device: TV Shows tab loads
- [ ] On-device: Search with debounce works
- [ ] On-device: Details screen with favorites/watchlist
- [ ] On-device: You tab with library items
- [ ] On-device: Auth flow
- [ ] On-device: Rotation / config change survives
- [ ] On-device: Back navigation works correctly
- [ ] Screenshot tests pass

🤖 Generated with [claude-flow](https://github.com/ruvnet/claude-flow)
EOF
)"

# After CI passes on PR:
# Merge PR via GitHub
```

---

## Execution Command (Claude Code Agent Teams)

To execute this plan, use the following orchestration in Claude Code:

```
Create a team called "a-plus-push" with these tasks:

Phase 2A (parallel):
  Task 1: "Fix ViewModel patterns (R5, R6, R7)" → refactor-expert agent
  Task 2: "Fix build polish (R9, R10, R11)" → refactor-expert agent
  Task 3: "Fix error handling (R4)" → data-layer-specialist agent
  Gate: CI + on-device validation

Phase 2B (after 2A merges):
  Task 4: "ImmutableList migration (R2)" → refactor-expert agent
  Gate: CI + on-device validation

Phase 2C (after 2B merges):
  Task 5: "LibraryRepo split (R3)" → refactor-expert agent
  Gate: CI + on-device validation

Phase 2D (after 2C merges):
  Task 6: "Repository tests (R1)" → general-purpose agent
  Task 7: "MainActivityVM tests (R8)" → general-purpose agent
  Gate: CI (all tests green)

Final: PR dev → main
```

---

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| ImmutableList breaks compilation | Phase 2B is isolated; compile after each file |
| LibraryRepo refactor breaks sync | Phase 2C runs existing tests + on-device favorites test |
| Test flakiness | Use `runTest` + `TestDispatcher`, no real I/O |
| Detekt violations from new code | Run `spotlessApply` + `detekt` after each agent |
| Merge conflicts between phases | Sequential phases, each merges to dev before next starts |

---

## Success Criteria

- [ ] All 11 remaining items (R1-R11) addressed
- [ ] `./gradlew spotlessCheck detekt lintDebug test` passes
- [ ] On-device validation: all screens, all flows
- [ ] PR from dev → main merged with green CI
- [ ] PROJECT_DIAGNOSIS.md updated with final A+ scores
