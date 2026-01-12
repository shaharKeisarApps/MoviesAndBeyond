# Navigation Audit Report

## Current State

### Navigation Version
- **Library**: `androidx.navigation:navigation-compose:2.9.6`
- **Hilt Navigation**: `androidx.hilt:hilt-navigation-compose:1.3.0`

### Project Structure

```
app/
├── ui/
│   ├── MoviesAndBeyondApp.kt          # Main app composable with bottom nav
│   ├── OnboardingScreen.kt            # Onboarding flow
│   └── navigation/
│       ├── MoviesAndBeyondNavigation.kt    # Main NavHost
│       └── MoviesAndBeyondDestination.kt   # Bottom nav destinations enum

feature/
├── movies/
│   └── MoviesNavigation.kt            # Nested graph: Feed, Items
├── tv/
│   └── TvShowsNavigation.kt           # Nested graph: Feed, Items
├── search/
│   └── SearchNavigation.kt            # Simple screen
├── you/
│   └── YouNavigation.kt               # Nested graph: You, LibraryItems
├── details/
│   └── DetailsNavigation.kt           # Nested graph: Details, Credits
└── auth/
    └── AuthNavigation.kt              # Simple screen
```

### Screen Inventory

| Module | Screen | Route | Arguments | Complexity |
|--------|--------|-------|-----------|------------|
| app | Onboarding | `onboarding` | None | Simple |
| movies | Feed | `movies_feed` | None | Simple |
| movies | Items | `movies_items/{category}` | category: String | Medium |
| tv | Feed | `tv_shows_feed` | None | Simple |
| tv | Items | `tv_shows_items/{category}` | category: String | Medium |
| search | Search | `search` | None | Simple |
| you | You | `you` | None | Simple |
| you | LibraryItems | `library_items/{type}` | type: String | Medium |
| details | Details | `details` (nested in `details/{id}`) | id: String | Complex |
| details | Credits | `credits` (nested in `details/{id}`) | None (uses parent) | Complex |
| auth | Auth | `auth` | None | Simple |

**Total: 11 unique screens**

### Navigation Patterns Used

#### 1. Bottom Navigation (4 tabs)
- **Movies** - `MoviesAndBeyondDestination.MOVIES`
- **TV Shows** - `MoviesAndBeyondDestination.TV_SHOWS`
- **Search** - `MoviesAndBeyondDestination.SEARCH`
- **You** - `MoviesAndBeyondDestination.YOU`

Uses `NavOptions` with:
- `popUpTo(startDestination)` with `saveState = true`
- `launchSingleTop = true`
- `restoreState = true`

#### 2. Nested Navigation Graphs
Used in: Movies, TV Shows, Details, You modules

Pattern:
```kotlin
navigation(
    route = "parent_route",
    startDestination = "child_route"
) {
    composable(route = "child_route") { ... }
    composable(route = "another_child") { ... }
}
```

#### 3. Parent Entry for Shared ViewModels
Used to share ViewModel across nested screens:
```kotlin
val parentEntry = remember(backStackEntry) {
    navController.getBackStackEntry(parentRoute)
}
val viewModel = hiltViewModel<ViewModel>(parentEntry)
```

#### 4. String-Based Routes with Arguments
Pattern: `"route/{argument}"`
Used in: Movies, TV Shows, Details, You modules

#### 5. NavController Extension Functions
Each feature module exports:
- `NavGraphBuilder.xyzScreen()` - NavGraph builder extension
- `NavController.navigateToXyz()` - Navigation extension

### Special Considerations

1. **Haze Effect on Bottom Bar**: Uses `dev.chrisbanes.haze` for blur effect
2. **Conditional Onboarding**: StartDestination changes based on `hideOnboarding` flag
3. **Details Navigation**: Complex nested structure with `{id}` in parent route
4. **Hierarchy Detection**: Uses `NavDestination.hierarchy` for bottom bar selection

### Dependencies Graph

```
app
├── feature:movies
├── feature:tv
├── feature:search
├── feature:you
├── feature:details
├── feature:auth
└── core:ui (HazeScaffold)

feature:movies → core:ui
feature:tv → core:ui
feature:details → core:ui
feature:you → core:ui
feature:search → core:ui
feature:auth → core:ui
```

### Migration Complexity Assessment

| Component | Complexity | Reason |
|-----------|------------|--------|
| Basic routes (auth, search) | Low | Simple composable routes |
| Feed screens | Low | No arguments, straightforward |
| Items screens | Medium | Route arguments to convert |
| Details module | High | Nested graph with shared ViewModel |
| Bottom navigation | High | Multiple backstacks pattern |
| Onboarding flow | Medium | Conditional start destination |

### Risks Identified

1. **Shared ViewModel Pattern**: Navigation 3 doesn't have `getBackStackEntry()` - need alternative approach
2. **Nested Graph Flattening**: Will need to restructure details navigation
3. **Bottom Nav State**: Must implement `TopLevelBackStack` pattern
4. **Route Matching for Bottom Bar**: Currently uses string matching on route names
