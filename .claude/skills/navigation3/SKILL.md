# Navigation 3 Migration & Implementation Skill

## Overview

This skill provides patterns and guidance for implementing and migrating to Jetpack Navigation 3 in Android Compose applications. Navigation 3 is a Compose-first navigation library that gives developers full control over the back stack and navigation state.

## When to Use

- Migrating from Navigation 2 to Navigation 3
- Implementing new navigation in Compose apps
- Setting up type-safe navigation with arguments
- Implementing multi-stack navigation (bottom nav)
- Adding navigation animations
- Integrating ViewModels with Hilt

## Quick Start

### 1. Add Dependencies

```toml
# libs.versions.toml
[versions]
navigation3 = "1.0.0"
lifecycle-viewmodel-navigation3 = "2.10.0"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycle-viewmodel-navigation3" }
```

### 2. Define Routes

```kotlin
@Serializable
data object Home : NavKey

@Serializable
data class ProductDetail(val productId: String) : NavKey
```

### 3. Set Up NavDisplay

```kotlin
val backStack = rememberNavBackStack(Home)

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()  // REQUIRED for ViewModels!
    ),
    entryProvider = entryProvider {
        entry<Home> { HomeScreen(onNavigate = { backStack.add(it) }) }
        entry<ProductDetail> { key -> DetailScreen(productId = key.productId) }
    }
)
```

## Documentation

| Document | Description |
|----------|-------------|
| [reference.md](./reference.md) | Detailed API documentation - NavDisplay, entryDecorators, NavKey, ViewModel integration |
| [examples.md](./examples.md) | 7 complete code patterns - Basic, Saveable, Modular/Hilt, Bottom Nav, Auth Flow, ViewModels, Animations |
| [scripts/validate-nav3-setup.sh](./scripts/validate-nav3-setup.sh) | Validates Navigation 3 configuration - checks dependencies, decorators, back stack types |

## Critical: Entry Decorators

**Always include both decorators when using ViewModels:**

```kotlin
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),  // UI state persistence
    rememberViewModelStoreNavEntryDecorator()         // ViewModel scoping per NavEntry
)
```

**Without `rememberViewModelStoreNavEntryDecorator()`:**
- ViewModels are scoped to Activity, not NavEntry
- All screens share the same ViewModel instance
- Navigation shows stale/same data (the bug we fixed!)

## Navigation 2 vs Navigation 3

| Aspect | Navigation 2 | Navigation 3 |
|--------|--------------|--------------|
| Routes | String-based | Type-safe `@Serializable` data classes |
| Back stack | NavController internal | Developer-owned `SnapshotStateList` |
| NavHost | `NavHost` + `NavGraphBuilder` | `NavDisplay` + `entryProvider` |
| ViewModel scoping | Via NavBackStackEntry | Via `rememberViewModelStoreNavEntryDecorator()` |
| Arguments | SavedStateHandle auto-populated | Manual via route key or assisted injection |

## Migration Checklist

- [ ] Add Navigation 3 dependencies
- [ ] Add Kotlin serialization plugin
- [ ] Convert string routes to `@Serializable` data classes implementing `NavKey`
- [ ] Replace `NavHost` with `NavDisplay`
- [ ] Create developer-owned back stack
- [ ] Add **both** entry decorators
- [ ] Update navigation calls: `navigate()` → `backStack.add()`
- [ ] Update ViewModel arg passing (assisted injection or manual ID injection)

## Common Pitfalls

### 1. Forgetting NavKey Interface
```kotlin
// WRONG
@Serializable data class Detail(val id: String)

// CORRECT
@Serializable data class Detail(val id: String) : NavKey
```

### 2. Wrong Back Stack Type
```kotlin
// WRONG - won't trigger recomposition
val backStack = remember { mutableListOf<Any>(Home) }

// CORRECT
val backStack = remember { mutableStateListOf<Any>(Home) }
```

### 3. Missing ViewModel Decorator
```kotlin
// WRONG - ViewModels won't be scoped correctly
entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator())

// CORRECT
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),
    rememberViewModelStoreNavEntryDecorator()
)
```

## This Project's Implementation

| File | Purpose |
|------|---------|
| `app/.../navigation/Routes.kt` | Type-safe route definitions |
| `app/.../navigation/TopLevelBackStack.kt` | Multi-stack manager for bottom nav |
| `app/.../navigation/MoviesAndBeyondNav3.kt` | Main NavDisplay with all entries |
| `app/.../MoviesAndBeyondApp.kt` | App scaffold with bottom navigation |

## References

- [Official Navigation 3 Documentation](https://developer.android.com/guide/navigation/navigation-3)
- [Navigation 3 Recipes Repository](https://github.com/android/nav3-recipes)
- [Migration Guide](https://developer.android.com/guide/navigation/navigation-3/migration-guide)
