# Navigation 3 Migration Plan

## Current State
- **Navigation Version**: 2.9.6 (androidx.navigation.compose)
- **Total Screens**: 11
- **Feature Modules**: 6 (movies, tv, search, you, details, auth)
- **Navigation Patterns**: Bottom nav, nested graphs, shared ViewModels

## Target State
- **Navigation 3 Version**: 1.0.0 (stable)
- **Type-safe routes**: All routes as `@Serializable` data classes/objects implementing `NavKey`
- **Owned back stack**: Developer-controlled `NavBackStack`
- **Multiple backstacks**: `TopLevelBackStack` pattern for bottom navigation

## Migration Strategy

**Approach: Incremental Migration**

We will migrate incrementally to minimize risk:
1. Add Navigation 3 dependencies (keep Nav2 temporarily)
2. Create type-safe route definitions
3. Build test case to validate patterns
4. Migrate simple screens first
5. Migrate complex screens (nested graphs)
6. Implement bottom navigation pattern
7. Remove Navigation 2 dependencies

---

## Phase 1: Dependencies

### Add to `libs.versions.toml`

```toml
[versions]
navigation3 = "1.0.0"
lifecycle-viewmodel-navigation3 = "2.10.0"
# kotlinx-serialization already exists: "1.9.0"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycle-viewmodel-navigation3" }
```

### Add to `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.jetbrains.kotlin.serialization) // If not already present
}

dependencies {
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
```

### Checklist
- [ ] Add Navigation 3 versions to libs.versions.toml
- [ ] Add Navigation 3 libraries to libs.versions.toml
- [ ] Add dependencies to app module
- [ ] Add serialization plugin if needed
- [ ] Verify build compiles

---

## Phase 2: Route Definitions

### New Route Structure

Create `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/Routes.kt`:

```kotlin
package com.keisardev.moviesandbeyond.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Onboarding
@Serializable
data object OnboardingRoute : NavKey

// Movies
@Serializable
data object MoviesFeedRoute : NavKey

@Serializable
data class MoviesItemsRoute(val category: String) : NavKey

// TV Shows
@Serializable
data object TvShowsFeedRoute : NavKey

@Serializable
data class TvShowsItemsRoute(val category: String) : NavKey

// Search
@Serializable
data object SearchRoute : NavKey

// You / Profile
@Serializable
data object YouRoute : NavKey

@Serializable
data class LibraryItemsRoute(val type: String) : NavKey

// Details (flattened from nested graph)
@Serializable
data class DetailsRoute(val id: String) : NavKey

@Serializable
data class CreditsRoute(val id: String) : NavKey  // Now carries its own id

// Auth
@Serializable
data object AuthRoute : NavKey

// Top-level tabs for bottom navigation
@Serializable
sealed class TopLevelRoute : NavKey {
    @Serializable
    data object Movies : TopLevelRoute()

    @Serializable
    data object TvShows : TopLevelRoute()

    @Serializable
    data object Search : TopLevelRoute()

    @Serializable
    data object You : TopLevelRoute()
}
```

### Route Mapping

| Old Route | New Route |
|-----------|-----------|
| `"onboarding"` | `OnboardingRoute` |
| `"movies"` | `TopLevelRoute.Movies` |
| `"movies_feed"` | `MoviesFeedRoute` |
| `"movies_items/{category}"` | `MoviesItemsRoute(category)` |
| `"tv_shows"` | `TopLevelRoute.TvShows` |
| `"tv_shows_feed"` | `TvShowsFeedRoute` |
| `"tv_shows_items/{category}"` | `TvShowsItemsRoute(category)` |
| `"search"` | `SearchRoute` / `TopLevelRoute.Search` |
| `"you"` | `YouRoute` / `TopLevelRoute.You` |
| `"library_items/{type}"` | `LibraryItemsRoute(type)` |
| `"details/{id}"` | `DetailsRoute(id)` |
| `"credits"` | `CreditsRoute(id)` |
| `"auth"` | `AuthRoute` |

### Checklist
- [ ] Create Routes.kt file
- [ ] Define all route data classes/objects
- [ ] Ensure all routes implement NavKey
- [ ] Add @Serializable annotations
- [ ] Handle nested graph flattening (Details → CreditsRoute now has id)

---

## Phase 3: Test Case

Before migrating main app, create isolated test to validate:

1. Basic navigation works
2. Arguments are passed correctly
3. ViewModel integration works with Hilt
4. Back stack behaves correctly

### Test Location
`app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/Nav3TestActivity.kt`

### Checklist
- [ ] Create test activity with basic Nav3 setup
- [ ] Test navigation between 2-3 screens
- [ ] Test argument passing
- [ ] Test back navigation
- [ ] Test ViewModel with assisted injection
- [ ] Verify all works before proceeding

---

## Phase 4: Core Navigation Infrastructure

### 4.1 Create TopLevelBackStack

`app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/TopLevelBackStack.kt`:

```kotlin
class TopLevelBackStack(startKey: NavKey) {
    private var topLevelStacks: LinkedHashMap<NavKey, SnapshotStateList<NavKey>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack: SnapshotStateList<NavKey> get() = topLevelStacks[topLevelKey]!!

    fun addTopLevel(key: NavKey) {
        if (key == topLevelKey) return
        if (!topLevelStacks.contains(key)) {
            topLevelStacks[key] = mutableStateListOf(key)
        }
        topLevelKey = key
    }

    fun add(key: NavKey) {
        backStack.add(key)
        topLevelStacks[topLevelKey]!!.add(key)
    }

    fun removeLast(): NavKey? {
        if (backStack.size <= 1) return null
        val removed = backStack.removeLastOrNull()
        topLevelStacks[topLevelKey]!!.removeLastOrNull()
        return removed
    }
}
```

### 4.2 Create NavigationState

Handle conditional onboarding:

```kotlin
class NavigationState(
    startRoute: NavKey,
    private val onboardingRoute: NavKey,
    val showOnboarding: Boolean
) {
    private val actualStart = if (showOnboarding) onboardingRoute else startRoute
    val topLevelBackStack = TopLevelBackStack(actualStart)

    var hasCompletedOnboarding by mutableStateOf(!showOnboarding)
        private set

    fun completeOnboarding() {
        hasCompletedOnboarding = true
        // Navigate to main content
    }
}
```

### Checklist
- [ ] Create TopLevelBackStack class
- [ ] Create NavigationState class
- [ ] Handle onboarding flow
- [ ] Test back stack operations

---

## Phase 5: Screen-by-Screen Migration

### Order of Migration (simplest to complex)

#### Batch 1: Simple Screens (No Arguments)
1. **AuthRoute** - Simple screen, no args
2. **SearchRoute** - Simple screen, no args

#### Batch 2: Feed Screens
3. **MoviesFeedRoute** - No args, entry point for Movies tab
4. **TvShowsFeedRoute** - No args, entry point for TV tab
5. **YouRoute** - No args, entry point for You tab

#### Batch 3: Screens with Arguments
6. **MoviesItemsRoute** - category argument
7. **TvShowsItemsRoute** - category argument
8. **LibraryItemsRoute** - type argument

#### Batch 4: Complex Screens
9. **DetailsRoute** - Requires ViewModel refactoring
10. **CreditsRoute** - Now needs own id argument
11. **OnboardingRoute** - Conditional flow

### Migration Template per Screen

```kotlin
// In entryProvider
entry<RouteType> { key ->
    val viewModel = hiltViewModel<ScreenViewModel>()
    ScreenRoute(
        // pass key.argument if needed
        viewModel = viewModel,
        onNavigate = { destination -> backStack.add(destination) },
        onBack = { backStack.removeLastOrNull() }
    )
}
```

### Checklist
- [ ] Migrate AuthRoute
- [ ] Migrate SearchRoute
- [ ] Migrate MoviesFeedRoute
- [ ] Migrate TvShowsFeedRoute
- [ ] Migrate YouRoute
- [ ] Migrate MoviesItemsRoute
- [ ] Migrate TvShowsItemsRoute
- [ ] Migrate LibraryItemsRoute
- [ ] Migrate DetailsRoute (refactor ViewModel)
- [ ] Migrate CreditsRoute (add id argument)
- [ ] Migrate OnboardingRoute

---

## Phase 6: ViewModel Refactoring

### Details Module Challenge

**Current Pattern:**
```kotlin
// Parent entry holds shared ViewModel
val parentEntry = navController.getBackStackEntry(detailsNavigationRouteWithArg)
val viewModel = hiltViewModel<DetailsViewModel>(parentEntry)
```

**Navigation 3 Solution:**

Option A: Pass ID to each screen, create separate ViewModels
```kotlin
@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted val navKey: DetailsRoute,
    private val repository: DetailsRepository
) : ViewModel() {
    // ...

    @AssistedFactory
    interface Factory {
        fun create(navKey: DetailsRoute): DetailsViewModel
    }
}
```

Option B: Shared state via repository/use-case (recommended)
- Move shared state to repository level
- Each screen gets its own ViewModel instance
- Data is cached in repository

### Checklist
- [ ] Decide on ViewModel sharing strategy
- [ ] Refactor DetailsViewModel for assisted injection
- [ ] Update CreditsRoute to receive id directly
- [ ] Test ViewModel scoping with entry decorators

---

## Phase 7: Bottom Navigation Integration

### Update MoviesAndBeyondApp.kt

```kotlin
@Composable
fun MoviesAndBeyondApp(hideOnboarding: Boolean) {
    val navigationState = remember {
        NavigationState(
            startRoute = TopLevelRoute.Movies,
            onboardingRoute = OnboardingRoute,
            showOnboarding = !hideOnboarding
        )
    }

    val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }

    // Map enum to route
    fun destinationToRoute(dest: MoviesAndBeyondDestination): NavKey = when (dest) {
        MoviesAndBeyondDestination.MOVIES -> TopLevelRoute.Movies
        MoviesAndBeyondDestination.TV_SHOWS -> TopLevelRoute.TvShows
        MoviesAndBeyondDestination.SEARCH -> TopLevelRoute.Search
        MoviesAndBeyondDestination.YOU -> TopLevelRoute.You
    }

    HazeScaffold(
        bottomBar = {
            if (shouldShowBottomBar(navigationState)) {
                MoviesAndBeyondNavigationBar(
                    destinations = bottomBarDestinations,
                    selectedDestination = routeToDestination(navigationState.topLevelBackStack.topLevelKey),
                    onNavigateToDestination = { dest ->
                        navigationState.topLevelBackStack.addTopLevel(destinationToRoute(dest))
                    }
                )
            }
        }
    ) { padding ->
        NavDisplay(
            modifier = Modifier.padding(padding),
            backStack = navigationState.topLevelBackStack.backStack,
            onBack = { navigationState.topLevelBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                // Route to appropriate screen
            }
        )
    }
}
```

### Checklist
- [ ] Create route-to-destination mapping
- [ ] Update bottom bar selection logic
- [ ] Implement NavDisplay with all entries
- [ ] Test tab switching preserves state
- [ ] Test nested navigation within tabs

---

## Phase 8: Cleanup

### Remove Navigation 2 Dependencies

1. Remove from `libs.versions.toml`:
```toml
# Remove
androidx-navigation-compose = "2.9.6"
```

2. Remove from feature module `build.gradle.kts` files

3. Delete old navigation files:
- Feature module `*Navigation.kt` files (after migrating logic)

### Checklist
- [ ] Remove Navigation 2 version
- [ ] Remove Navigation 2 library
- [ ] Remove old navigation extension functions
- [ ] Remove NavController references
- [ ] Clean up unused imports
- [ ] Run `./gradlew clean build`

---

## Rollback Strategy

If migration fails at any point:

1. **Git Branch**: Create feature branch `feature/navigation3-migration` before starting
2. **Incremental Commits**: Commit after each successful phase
3. **Keep Nav2**: Don't remove Nav2 dependencies until fully migrated and tested
4. **Rollback Command**: `git checkout main -- .` to restore all files

### Recovery Steps
1. `git stash` current changes
2. `git checkout main`
3. Analyze failure
4. Create new branch with learnings

---

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Shared ViewModel pattern breaks | High | High | Use assisted injection or shared repository |
| Bottom nav state loss | Medium | High | Implement TopLevelBackStack correctly |
| Build failures | Medium | Medium | Incremental migration, test each phase |
| Runtime crashes | Medium | High | Test case validation before main migration |
| Deep links break | Low | Medium | Not currently used, skip for now |
| Animation changes | Low | Low | Nav3 has similar animation API |

---

## Success Criteria

- [ ] All screens navigable as before
- [ ] Bottom navigation preserves state per tab
- [ ] Back button works correctly
- [ ] Arguments passed correctly to all screens
- [ ] ViewModels scoped correctly
- [ ] `./gradlew clean build` passes
- [ ] No Navigation 2 imports in codebase
- [ ] App runs without crashes

---

## Timeline Estimate

| Phase | Status |
|-------|--------|
| Phase 1: Dependencies | ✅ Completed |
| Phase 2: Route Definitions | ✅ Completed |
| Phase 3: Test Case | ✅ Completed |
| Phase 4: Core Infrastructure | ✅ Completed |
| Phase 5: Screen Migration | ✅ Completed |
| Phase 6: ViewModel Refactoring | ✅ Completed (using hiltViewModel directly) |
| Phase 7: Bottom Nav Integration | ✅ Completed |
| Phase 8: Cleanup | In Progress |

## Migration Summary

### Files Created
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/Routes.kt` - Type-safe route definitions
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/TopLevelBackStack.kt` - Multi-stack navigation manager
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/MoviesAndBeyondNav3.kt` - Nav3 navigation component
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/Nav3Test.kt` - Test implementation

### Files Modified
- `gradle/libs.versions.toml` - Added Navigation 3 dependencies
- `app/build.gradle.kts` - Added Nav3 and serialization plugin
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/MoviesAndBeyondApp.kt` - Updated to use Nav3
- Feature module screen files - Changed `internal` to `public` visibility for Route composables

### Key Architectural Changes
1. **NavController → NavigationState**: Application now owns its navigation state
2. **String routes → Type-safe NavKey**: All routes are now @Serializable data classes/objects
3. **Single back stack → Multiple back stacks**: TopLevelBackStack manages independent stacks per tab
4. **NavHost → NavDisplay**: Using Navigation 3's declarative NavDisplay with entryProvider
5. **NavGraphBuilder extensions → Direct composable references**: Feature screens called directly

### Known Limitations
1. DetailsViewModel still uses SavedStateHandle - may need refactoring for assisted injection
2. Shared ViewModel pattern between Details and Credits screens needs review
3. Deep links not yet implemented (were not present in original)

## Rollback Strategy

If issues are discovered after the migration:

### Quick Rollback
1. The old Navigation 2 files are preserved:
   - `MoviesAndBeyondNavigation.kt` (renamed but present)
   - Feature module `*Navigation.kt` extensions

2. To rollback:
   ```bash
   # Revert MoviesAndBeyondApp.kt to use Nav2
   git checkout HEAD~1 -- app/src/main/java/com/keisardev/moviesandbeyond/ui/MoviesAndBeyondApp.kt
   ```

### Full Rollback via Git
```bash
# Create a rollback branch
git checkout -b rollback/nav3-migration

# Revert all changes
git revert HEAD

# Or reset to before migration
git reset --hard <commit-before-migration>
```

### Files That Can Be Safely Removed After Validation
Once the migration is confirmed stable:
- `app/src/main/java/com/keisardev/moviesandbeyond/ui/navigation/MoviesAndBeyondNavigation.kt` (old Nav2)
- Feature module `*Navigation.kt` files (old NavGraphBuilder extensions)
- Can remove Navigation 2 dependency from `libs.versions.toml`

## Breaking Changes

### For Developers
1. **NavController no longer used**: Navigation state is now managed via `NavigationState` class
2. **Feature screen visibility**: Route composables changed from `internal` to `public`
3. **Route type changes**: String routes replaced with type-safe `@Serializable` data classes
4. **ViewModel scoping**: May need adjustment for screens requiring shared ViewModels

### For Users
- No user-facing changes expected
- Navigation behavior should be identical to before
