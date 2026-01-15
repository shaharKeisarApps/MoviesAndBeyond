---
name: compose-viewmodel-bridge
description: Use when connecting ViewModel StateFlow to Compose UI. Triggers on "collectAsStateWithLifecycle", "stateInWhileSubscribed", "ViewModel state", "StateFlow to Compose", "configuration change resilience", "WhileSubscribedOrRetained".
category: presentation
allowed-tools: Read, Edit, Write, Grep, Glob, Bash
---

# Compose-ViewModel Bridge Skill

## Overview

Expert guidance for connecting ViewModel StateFlow to Compose UI using the project's custom `WhileSubscribedOrRetained` strategy for optimal configuration change handling.

## When to Use

- Exposing StateFlow from @HiltViewModel
- Collecting state in Composables with lifecycle awareness
- Implementing pagination with accumulated state
- Handling configuration changes without data loss
- Debugging state collection issues

## Quick Start

### ViewModel Pattern (CORRECT)

```kotlin
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    val movies: StateFlow<ContentUiState> = flow
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ContentUiState(category)
        )
}
```

### Compose Collection (CORRECT)

```kotlin
@Composable
fun MoviesScreen(viewModel: MoviesViewModel = hiltViewModel()) {
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    // Use movies...
}
```

## Critical Pattern: WhileSubscribedOrRetained

### Source Location
`data/src/main/java/com/keisardev/moviesandbeyond/data/coroutines/WhileSubscribedOrRetained.kt`

### Why NOT WhileSubscribed(5000)?

The standard `SharingStarted.WhileSubscribed(5000)` causes issues during configuration changes:
- **Restarts expensive operations**: Network calls re-trigger unnecessarily
- **UI flicker**: Loading states flash during rotation
- **Redundant requests**: Same data fetched multiple times
- **Memory waste**: Duplicate flows created during transition

### How WhileSubscribedOrRetained Works

```
[Subscription 0 -> 1]
        |
        v
    emit(START)  <-- Immediate, no delay
        |
[Subscription 1 -> 0]
        |
        v
    Wait for:
    1. Choreographer frame callback
    2. Handler message queue to clear
        |
        v
    emit(STOP)  <-- Only after UI stabilizes
```

This ensures that during configuration changes (rotation, theme toggle), the new Activity/Fragment has time to re-subscribe before the flow stops.

### Implementation

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
object WhileSubscribedOrRetained : SharingStarted {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    /** Detects unit test environment where Android framework unavailable */
    private val isInTestEnvironment: Boolean by lazy {
        try {
            Looper.getMainLooper()
            false
        } catch (_: RuntimeException) {
            true
        }
    }

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        subscriptionCount
            .transformLatest { count ->
                if (count > 0) {
                    emit(SharingCommand.START)
                } else {
                    if (!isInTestEnvironment) {
                        val posted = CompletableDeferred<Unit>()
                        Choreographer.getInstance().postFrameCallback {
                            handler.postAtFrontOfQueue { handler.post { posted.complete(Unit) } }
                        }
                        posted.await()
                    }
                    emit(SharingCommand.STOP)
                }
            }
            .dropWhile { it != SharingCommand.START }
            .distinctUntilChanged()
}
```

### Extension Function (Use This!)

```kotlin
fun <T> Flow<T>.stateInWhileSubscribed(
    scope: CoroutineScope,
    initialValue: T,
): StateFlow<T> = stateIn(
    scope = scope,
    started = WhileSubscribedOrRetained,
    initialValue = initialValue,
)
```

## Project-Specific Patterns

### Pattern 1: Simple State (DetailsViewModel)

```kotlin
// From: feature/details/src/main/java/.../DetailsViewModel.kt
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val detailsRepository: DetailsRepository
) : ViewModel() {

    private val idDetailsString =
        savedStateHandle.getStateFlow(key = idNavigationArgument, initialValue = "")

    val contentDetailsUiState: StateFlow<ContentDetailUiState> =
        idDetailsString
            .mapLatest { detailsString ->
                // Transform navigation argument to UI state
                detailsString.takeIf { it.isNotEmpty() }?.let {
                    val (id, mediaType) = parseDetails(detailsString)
                    when (mediaType) {
                        MediaType.MOVIE -> fetchMovieDetails(id)
                        MediaType.TV -> fetchTvDetails(id)
                        MediaType.PERSON -> fetchPersonDetails(id)
                        else -> ContentDetailUiState.Empty
                    }
                } ?: ContentDetailUiState.Empty
            }
            .stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = ContentDetailUiState.Loading
            )
}
```

### Pattern 2: Multi-Category Pagination (MoviesViewModel)

```kotlin
// From: feature/movies/src/main/java/.../MoviesViewModel.kt
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    // Page tracking per category
    private val _nowPlayingPage = MutableStateFlow(1)
    private val _popularPage = MutableStateFlow(1)

    // Accumulated items for pagination
    private val _nowPlayingAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())
    private val _popularAccumulated = MutableStateFlow<List<ContentItem>>(emptyList())

    // Error state (separate flow for snackbar handling)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Now Playing with offline-first support
    val nowPlayingMovies: StateFlow<ContentUiState> =
        createContentFlow(
            category = MovieListCategory.NOW_PLAYING,
            pageFlow = _nowPlayingPage,
            accumulatedFlow = _nowPlayingAccumulated
        ).stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ContentUiState(MovieListCategory.NOW_PLAYING)
        )

    private fun createContentFlow(
        category: MovieListCategory,
        pageFlow: MutableStateFlow<Int>,
        accumulatedFlow: MutableStateFlow<List<ContentItem>>
    ) = pageFlow
        .flatMapLatest { page ->
            contentRepository.observeMovieItems(category = category, page = page)
                .map { response -> handleStoreResponse(response, category, page, accumulatedFlow) }
        }
        .combine(accumulatedFlow) { currentState, accumulated ->
            currentState.copy(items = accumulated)
        }

    fun appendItems(category: MovieListCategory) {
        when (category) {
            MovieListCategory.NOW_PLAYING -> _nowPlayingPage.update { it + 1 }
            MovieListCategory.POPULAR -> _popularPage.update { it + 1 }
            // ... other categories
        }
    }

    fun refresh(category: MovieListCategory) {
        viewModelScope.launch {
            when (category) {
                MovieListCategory.NOW_PLAYING -> {
                    _nowPlayingAccumulated.value = emptyList()
                    _nowPlayingPage.value = 1
                }
                // ... other categories
            }
        }
    }
}
```

### Pattern 3: Compose Screen Collection

```kotlin
// From: feature/movies/src/main/java/.../FeedScreen.kt
@Composable
fun FeedRoute(
    navigateToDetails: (String) -> Unit,
    navigateToItems: (String) -> Unit,
    viewModel: MoviesViewModel,
    modifier: Modifier = Modifier
) {
    // Collect ALL StateFlows with lifecycle awareness
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsStateWithLifecycle()
    val popularMovies by viewModel.popularMovies.collectAsStateWithLifecycle()
    val topRatedMovies by viewModel.topRatedMovies.collectAsStateWithLifecycle()
    val upcomingMovies by viewModel.upcomingMovies.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    FeedScreen(
        nowPlayingMovies = nowPlayingMovies,
        popularMovies = popularMovies,
        topRatedMovies = topRatedMovies,
        upcomingMovies = upcomingMovies,
        errorMessage = errorMessage,
        appendItems = viewModel::appendItems,
        onItemClick = navigateToDetails,
        onSeeAllClick = navigateToItems,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier
    )
}
```

### Pattern 4: Error Handling with Snackbar

```kotlin
@Composable
internal fun FeedScreen(
    // ... state params
    errorMessage: String?,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error as snackbar then clear
    errorMessage?.let { message ->
        scope.launch { snackbarState.showSnackbar(message) }
        onErrorShown()
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarState) }) { padding ->
        // ... content
    }
}
```

## Anti-Patterns

### WRONG: Using WhileSubscribed with timeout

```kotlin
// DON'T DO THIS - causes issues during config changes
val data: StateFlow<UiState> = flow
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // BAD!
        initialValue = UiState()
    )
```

### WRONG: Using Eagerly/Lazily without consideration

```kotlin
// DON'T DO THIS - leaks resources, never stops
val data: StateFlow<UiState> = flow
    .stateIn(viewModelScope, SharingStarted.Eagerly, UiState())
```

### WRONG: Collecting without lifecycle awareness

```kotlin
// DON'T DO THIS - continues collecting when in background
@Composable
fun BadScreen(viewModel: ViewModel) {
    val data by viewModel.data.collectAsState() // Missing lifecycle!
}
```

### WRONG: Fetching in init block

```kotlin
// DON'T DO THIS - triggers on ViewModel creation
@HiltViewModel
class BadViewModel @Inject constructor(repo: Repository) : ViewModel() {
    init {
        viewModelScope.launch {
            // This triggers immediately, not when UI subscribes
            repo.fetchData()
        }
    }
}
```

## Testing Considerations

### WhileSubscribedOrRetained Auto-Detection

The strategy automatically detects test environments:

```kotlin
private val isInTestEnvironment: Boolean by lazy {
    try {
        Looper.getMainLooper()  // Throws in unit tests
        false
    } catch (_: RuntimeException) {
        true
    }
}
```

In tests (where Looper throws), `STOP` is emitted immediately without waiting for Choreographer. Tests behave like standard `WhileSubscribed`.

### ViewModel Test Pattern

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val repository = TestSearchRepository()
    private lateinit var viewModel: SearchViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = SearchViewModel(searchRepository = repository)
    }

    @Test
    fun `test state collection`() = runTest {
        // Collect StateFlow in background
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.searchSuggestions.collect()
        }

        // Trigger action
        viewModel.changeSearchQuery("test")
        advanceUntilIdle()

        // Assert state
        assertEquals(expectedResults, viewModel.searchSuggestions.value)

        collectJob.cancel()
    }
}
```

### MainDispatcherRule

```kotlin
// From: core/testing/src/main/java/.../MainDispatcherRule.kt
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}
```

## Validation Commands

Run these to verify correct usage:

```bash
# Check for WhileSubscribed(5000) anti-pattern
./scripts/validate-state-strategy.sh

# Verify collectAsStateWithLifecycle usage
./scripts/validate-compose-collection.sh
```

## UI State Classes

### Standard Pattern

```kotlin
data class ContentUiState(
    val items: List<ContentItem>,
    val isLoading: Boolean,
    val endReached: Boolean,
    val page: Int,
    val category: MovieListCategory,
    val isFromCache: Boolean = false  // For stale data indicator
) {
    // Secondary constructor for initial state
    constructor(category: MovieListCategory) : this(
        items = emptyList(),
        isLoading = true,
        endReached = false,
        page = 1,
        category = category,
        isFromCache = false
    )
}
```

### Sealed Interface Pattern

```kotlin
sealed interface ContentDetailUiState {
    data object Loading : ContentDetailUiState
    data object Empty : ContentDetailUiState
    data class Movie(val data: MovieDetails) : ContentDetailUiState
    data class TV(val data: TvDetails) : ContentDetailUiState
    data class Person(val data: PersonDetails) : ContentDetailUiState
}
```

## Related Skills

- **store5-expert**: For StoreReadResponse handling
- **store5-room-bridge**: For offline-first data pipelines
- **viewmodel-testing-expert**: For ViewModel testing with Turbine
- **coroutines-expert**: For advanced Flow patterns

## Files Reference

| File | Purpose |
|------|---------|
| `data/src/.../coroutines/WhileSubscribedOrRetained.kt` | Custom SharingStarted strategy |
| `feature/movies/src/.../MoviesViewModel.kt` | Multi-category pagination reference |
| `feature/tv/src/.../TvShowsViewModel.kt` | TV shows implementation |
| `feature/details/src/.../DetailsViewModel.kt` | Simple state transformation |
| `feature/movies/src/.../FeedScreen.kt` | Compose collection reference |
| `core/testing/src/.../MainDispatcherRule.kt` | Test dispatcher rule |
