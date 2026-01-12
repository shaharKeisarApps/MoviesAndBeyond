# Navigation 3 Examples

Complete code examples for common Navigation 3 patterns.

## Pattern 1: Basic Navigation Setup

The foundational pattern for Navigation 3.

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// Define navigation keys
@Serializable
data object Home : NavKey

@Serializable
data class Product(val id: String) : NavKey

@Composable
fun BasicNavigationExample() {
    // Create and own the back stack
    val backStack = remember { mutableStateListOf<Any>(Home) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(onProductClick = { backStack.add(Product(it)) })
            }
            entry<Product> { key ->
                ProductScreen(productId = key.id)
            }
        }
    )
}
```

---

## Pattern 2: Saveable Back Stack (Process Death Survival)

Use `rememberNavBackStack` for automatic state persistence:

```kotlin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data class Detail(val id: String) : NavKey

@Composable
fun SaveableNavigationExample() {
    // Automatically persists across config changes and process death
    val backStack = rememberNavBackStack(Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(onNavigate = { backStack.add(Detail(it)) })
            }
            entry<Detail> { key ->
                DetailScreen(id = key.id)
            }
        }
    )
}
```

---

## Pattern 3: Modular Navigation with Hilt

For large codebases with feature modules contributing navigation destinations independently.

```kotlin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.multibindings.IntoSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Navigator class - shared across modules
@ActivityRetainedScoped
class Navigator(startDestination: NavKey) {
    private val _backStack = MutableStateFlow<List<NavKey>>(listOf(startDestination))
    val backStack: StateFlow<List<NavKey>> = _backStack

    fun goTo(destination: NavKey) {
        _backStack.value = _backStack.value + destination
    }

    fun goBack(): Boolean {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
            return true
        }
        return false
    }

    fun popUpTo(destination: NavKey, inclusive: Boolean = false) {
        val currentStack = _backStack.value.toMutableList()
        val index = currentStack.lastIndexOf(destination)
        if (index >= 0) {
            val removeCount = currentStack.size - index - (if (inclusive) 0 else 1)
            repeat(removeCount) { currentStack.removeAt(currentStack.size - 1) }
            _backStack.value = currentStack
        }
    }
}

// Common module - provides Navigator
@Module
@InstallIn(ActivityRetainedComponent::class)
object CommonModule {
    @Provides
    @ActivityRetainedScoped
    fun provideNavigator(): Navigator = Navigator(startDestination = Home)
}

// Type alias for modular entry providers
typealias EntryProviderInstaller = (Navigator) -> NavEntry<NavKey>

// Feature Module - Home
@Module
@InstallIn(ActivityRetainedComponent::class)
object HomeModule {
    @Provides
    @IntoSet
    fun provideHomeEntry(navigator: Navigator): EntryProviderInstaller = {
        NavEntry(Home) {
            HomeScreen(onNavigate = { navigator.goTo(it) })
        }
    }
}

// Main Activity assembles all modules
@AndroidEntryPoint
class ModularActivity : ComponentActivity() {
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var entryProviders: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val backStack by navigator.backStack.collectAsState()

            NavDisplay(
                backStack = backStack.toMutableStateList(),
                onBack = { navigator.goBack() },
                entryProvider = { key ->
                    entryProviders.firstNotNullOf { installer ->
                        runCatching { installer(navigator) }
                            .takeIf { it.isSuccess && it.getOrNull()?.key == key }
                            ?.getOrNull()
                    }
                }
            )
        }
    }
}
```

---

## Pattern 4: Multiple Back Stacks (Bottom Navigation)

Each tab maintains its own independent back stack.

```kotlin
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.material3.*
import kotlinx.serialization.Serializable

// Define tab keys
@Serializable data object HomeTab : NavKey
@Serializable data object SearchTab : NavKey
@Serializable data object ProfileTab : NavKey
@Serializable data class ArticleDetail(val articleId: String) : NavKey

// Custom back stack manager for multiple top-level destinations
class TopLevelBackStack<T : Any>(startKey: T) {
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack: SnapshotStateList<T> = topLevelStacks[startKey]!!

    fun addTopLevel(key: T) {
        if (key == topLevelKey) return

        if (!topLevelStacks.contains(key)) {
            topLevelStacks[key] = mutableStateListOf(key)
        }

        topLevelKey = key
        backStack.clear()
        backStack.addAll(topLevelStacks[key]!!)
    }

    fun add(key: T) {
        backStack.add(key)
        topLevelStacks[topLevelKey]!!.add(key)
    }

    fun removeLast(): T? {
        if (backStack.size <= 1) return null
        val removed = backStack.removeLastOrNull()
        topLevelStacks[topLevelKey]!!.removeLastOrNull()
        return removed
    }
}

@Composable
fun BottomNavigationExample() {
    val navBackStack = remember { TopLevelBackStack(HomeTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    selected = navBackStack.topLevelKey == HomeTab,
                    onClick = { navBackStack.addTopLevel(HomeTab) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Search") },
                    selected = navBackStack.topLevelKey == SearchTab,
                    onClick = { navBackStack.addTopLevel(SearchTab) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") },
                    selected = navBackStack.topLevelKey == ProfileTab,
                    onClick = { navBackStack.addTopLevel(ProfileTab) }
                )
            }
        }
    ) { padding ->
        NavDisplay(
            modifier = Modifier.padding(padding),
            backStack = navBackStack.backStack,
            onBack = { navBackStack.removeLast() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<HomeTab> {
                    HomeScreen(onArticleClick = { navBackStack.add(ArticleDetail(it)) })
                }
                entry<SearchTab> { SearchScreen() }
                entry<ProfileTab> { ProfileScreen() }
                entry<ArticleDetail> { key -> ArticleScreen(articleId = key.articleId) }
            }
        )
    }
}
```

---

## Pattern 5: Conditional Navigation (Authentication Flow)

Redirect users to login when accessing protected routes.

```kotlin
import kotlinx.serialization.Serializable

// Marker interface for routes requiring authentication
interface RequiresLogin

@Serializable data object Home : NavKey
@Serializable data object Login : NavKey
@Serializable data object Profile : NavKey, RequiresLogin
@Serializable data object Settings : NavKey, RequiresLogin

// Auth-aware back stack
class AuthAwareBackStack<T : Any>(
    startRoute: T,
    private val loginRoute: T
) {
    var isLoggedIn by mutableStateOf(false)
        private set

    private var onLoginSuccessRoute: T? = null
    val backStack = mutableStateListOf(startRoute)

    fun add(route: T) {
        if (route is RequiresLogin && !isLoggedIn) {
            onLoginSuccessRoute = route
            backStack.add(loginRoute)
        } else {
            backStack.add(route)
        }
    }

    fun removeLast() {
        backStack.removeLastOrNull()
    }

    fun login(username: String, password: String) {
        if (username.isNotEmpty() && password.isNotEmpty()) {
            isLoggedIn = true
            onLoginSuccessRoute?.let {
                backStack.remove(loginRoute)
                backStack.add(it)
                onLoginSuccessRoute = null
            }
        }
    }

    fun logout() {
        isLoggedIn = false
        backStack.clear()
        backStack.add(backStack.first())
    }
}

@Composable
fun AuthNavigationExample() {
    val navBackStack = remember { AuthAwareBackStack(startRoute = Home, loginRoute = Login) }

    NavDisplay(
        backStack = navBackStack.backStack,
        onBack = { navBackStack.removeLast() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onProfileClick = { navBackStack.add(Profile) },
                    onSettingsClick = { navBackStack.add(Settings) },
                    isLoggedIn = navBackStack.isLoggedIn,
                    onLogout = { navBackStack.logout() }
                )
            }
            entry<Login> {
                LoginScreen(onLogin = { user, pass -> navBackStack.login(user, pass) })
            }
            entry<Profile> { ProfileScreen() }
            entry<Settings> { SettingsScreen() }
        }
    )
}
```

---

## Pattern 6: ViewModel Integration with Hilt Assisted Injection

Pass navigation arguments to ViewModels using assisted injection.

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetail(val productId: String, val source: String) : NavKey

@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
class ProductDetailViewModel @AssistedInject constructor(
    @Assisted val navKey: ProductDetail,
    private val productRepository: ProductRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    var product by mutableStateOf<Product?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        loadProduct()
        analyticsService.logScreenView("product_detail", mapOf("source" to navKey.source))
    }

    private fun loadProduct() {
        viewModelScope.launch {
            isLoading = true
            try {
                product = productRepository.getProduct(navKey.productId)
            } finally {
                isLoading = false
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: ProductDetail): ProductDetailViewModel
    }
}

// Usage in NavDisplay
@AndroidEntryPoint
class ProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(ProductList)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<ProductList> {
                        ProductListScreen(
                            onProductClick = { productId ->
                                backStack.add(ProductDetail(productId, source = "list"))
                            }
                        )
                    }
                    entry<ProductDetail> { navKey ->
                        val viewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory>(
                            creationCallback = { factory -> factory.create(navKey) }
                        )
                        ProductDetailScreen(viewModel = viewModel)
                    }
                }
            )
        }
    }
}
```

---

## Pattern 7: Navigation Animations

Customize transitions with global defaults and per-destination overrides.

```kotlin
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@Serializable data object ScreenA : NavKey
@Serializable data object ScreenB : NavKey
@Serializable data object ScreenC : NavKey

@Composable
fun AnimatedNavigationExample() {
    val backStack = rememberNavBackStack(ScreenA)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        // Global animation for forward navigation
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            ).togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(500)
                )
            )
        },
        // Global animation for back navigation
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ).togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            )
        },
        entryProvider = entryProvider {
            entry<ScreenA> {
                Column {
                    Text("Screen A - Horizontal Slide")
                    Button(onClick = { backStack.add(ScreenB) }) { Text("Go to B") }
                    Button(onClick = { backStack.add(ScreenC) }) { Text("Go to C (Vertical)") }
                }
            }
            entry<ScreenB> {
                Text("Screen B - Uses global horizontal animation")
            }
            // Override animation for specific destination
            entry<ScreenC>(
                metadata = NavDisplay.transitionSpec {
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(700)
                    ).togetherWith(ExitTransition.KeepUntilTransitionsFinished)
                }
            ) {
                Text("Screen C - Custom vertical slide animation")
            }
        }
    )
}
```

---

## This Project's Implementation

See the actual implementation in this codebase:

- **Routes**: `app/src/main/java/.../ui/navigation/Routes.kt`
- **Navigation State**: `app/src/main/java/.../ui/navigation/TopLevelBackStack.kt`
- **NavDisplay**: `app/src/main/java/.../ui/navigation/MoviesAndBeyondNav3.kt`
- **Main App**: `app/src/main/java/.../ui/MoviesAndBeyondApp.kt`
