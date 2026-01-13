# Navigation 3 API Reference

Detailed API documentation for Jetpack Navigation 3.

## Core Components

### NavDisplay

The main composable that renders navigation destinations.

```kotlin
@Composable
fun NavDisplay(
    backStack: List<Any>,
    onBack: () -> Unit,
    entryProvider: (key: Any) -> NavEntry<*>,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator> = emptyList(),
    transitionSpec: AnimatedContentTransitionScope<NavEntry<*>>.() -> ContentTransform = { ... },
    popTransitionSpec: AnimatedContentTransitionScope<NavEntry<*>>.() -> ContentTransform = { ... }
)
```

**Parameters:**
- `backStack` - Developer-owned list of navigation keys (use `SnapshotStateList` or `NavBackStack`)
- `onBack` - Callback for system back press
- `entryProvider` - Lambda that maps keys to `NavEntry` composables
- `entryDecorators` - List of decorators for state management (ViewModels, saveable state)
- `transitionSpec` - Animation for forward navigation
- `popTransitionSpec` - Animation for back navigation

### NavEntry

Wrapper for destination composables.

```kotlin
class NavEntry<T : Any>(
    val key: T,
    val metadata: Map<String, Any> = emptyMap(),
    val content: @Composable () -> Unit
)
```

### NavKey Interface

All navigation keys should implement this interface:

```kotlin
interface NavKey
```

**Usage:**
```kotlin
@Serializable
data class ProductDetail(val productId: String) : NavKey

@Serializable
data object Home : NavKey
```

---

## Entry Decorators

Entry decorators add behavior to NavEntries. **CRITICAL: Always include both for proper functionality.**

### rememberSaveableStateHolderNavEntryDecorator()

Preserves UI state (scroll position, text input, etc.) across navigation.

```kotlin
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
```

### rememberViewModelStoreNavEntryDecorator()

**REQUIRED for ViewModels.** Scopes ViewModels to individual NavEntries instead of the Activity.

```kotlin
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
```

**Dependency:** `implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")`

### Correct Usage

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()  // Required for ViewModels!
    ),
    entryProvider = entryProvider { ... }
)
```

**Without `rememberViewModelStoreNavEntryDecorator()`:**
- ViewModels are scoped to Activity
- All screens share the same ViewModel instance
- Navigation between screens shows stale/same data

---

## Back Stack Types

### SnapshotStateList (Basic)

Manual state management, triggers recomposition:

```kotlin
val backStack = remember { mutableStateListOf<NavKey>(Home) }
```

### NavBackStack (Recommended)

Automatic persistence across configuration changes and process death:

```kotlin
val backStack = rememberNavBackStack(Home)
```

**Requires:** Routes must implement `NavKey` and be `@Serializable`

---

## Entry Provider DSL

Type-safe way to define routes:

```kotlin
entryProvider {
    entry<Home> {
        HomeScreen()
    }
    entry<ProductDetail> { key ->
        ProductDetailScreen(productId = key.productId)
    }
}
```

**Benefits:**
- Type-safe access to route parameters via `key`
- Compile-time verification of route types
- Cleaner syntax than manual `when` expressions

---

## ViewModel Integration

### Option 1: Standard hiltViewModel (Simple cases)

Works when ViewModel gets data from repository, not route args:

```kotlin
entry<ProductList> {
    val viewModel = hiltViewModel<ProductListViewModel>()
    ProductListScreen(viewModel = viewModel)
}
```

### Option 2: Manual ID Injection (Current app pattern)

For ViewModels that need route arguments via SavedStateHandle:

```kotlin
// In ViewModel
fun setDetailsId(id: String) {
    if (savedStateHandle.get<String>(ID_KEY).isNullOrEmpty()) {
        savedStateHandle[ID_KEY] = id
    }
}

// In NavDisplay entry
entry<DetailsRoute> { key ->
    val viewModel = hiltViewModel<DetailsViewModel>()
    DetailsScreen(
        viewModel = viewModel,
        detailsId = key.id  // Pass ID to composable
    )
}

// In Screen composable
@Composable
fun DetailsScreen(viewModel: DetailsViewModel, detailsId: String? = null) {
    detailsId?.let { viewModel.setDetailsId(it) }
    // ...
}
```

### Option 3: Assisted Injection (Recommended for Nav3)

Official pattern for passing route args to ViewModels:

```kotlin
@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    @Assisted val navKey: ProductDetail,
    private val repository: ProductRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: ProductDetail): ProductDetailViewModel
    }
}

// Usage
entry<ProductDetail> { navKey ->
    val viewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    )
    ProductDetailScreen(viewModel = viewModel)
}
```

---

## Navigation Operations

### Navigate Forward

```kotlin
backStack.add(ProductDetail(productId = "123"))
```

### Navigate Back

```kotlin
backStack.removeLastOrNull()
```

### Pop to Specific Route

```kotlin
// Remove all entries after target
val index = backStack.indexOfLast { it is Home }
if (index >= 0) {
    while (backStack.size > index + 1) {
        backStack.removeLastOrNull()
    }
}
```

### Clear and Navigate

```kotlin
backStack.clear()
backStack.add(Home)
```

---

## Animations

### Global Transitions

```kotlin
NavDisplay(
    transitionSpec = {
        slideInHorizontally(initialOffsetX = { it }) togetherWith
        slideOutHorizontally(targetOffsetX = { -it })
    },
    popTransitionSpec = {
        slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })
    },
    // ...
)
```

### Per-Destination Override

```kotlin
entry<ModalScreen>(
    metadata = NavDisplay.transitionSpec {
        slideInVertically(initialOffsetY = { it }) togetherWith
        ExitTransition.KeepUntilTransitionsFinished
    }
) {
    ModalContent()
}
```

---

## Dependencies

```toml
[versions]
navigation3 = "1.0.0"
lifecycle-viewmodel-navigation3 = "2.10.0"
kotlinx-serialization = "1.9.0"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }
androidx-navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycle-viewmodel-navigation3" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

**Requirements:**
- Compile SDK: 36
- Min SDK: 23

---

## Common Import Statements

```kotlin
// Core
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

// ViewModel support
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.hilt.navigation.compose.hiltViewModel

// Serialization
import kotlinx.serialization.Serializable
```
