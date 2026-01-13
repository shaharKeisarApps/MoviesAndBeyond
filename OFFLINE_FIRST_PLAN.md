# Offline-First Architecture Implementation Plan

## Executive Summary

This document outlines the implementation plan for migrating MoviesAndBeyond to an offline-first architecture using **Store5** for reactive caching and **WhileSubscribedOrRetained** for optimized Flow consumption in ViewModels.

---

## Phase 1: Current State Analysis

### Current Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CURRENT DATA FLOW                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│   Network (TMDB API via Retrofit)                                │
│              │                                                    │
│              ▼                                                    │
│   Repository (suspend functions, NetworkResponse wrapper)        │
│              │                                                    │
│              ▼                                                    │
│   ViewModel (MutableStateFlow, manual loading/error handling)    │
│              │                                                    │
│              ▼                                                    │
│   Composable UI                                                  │
│                                                                   │
│   PROBLEMS:                                                       │
│   - No offline support (network-only)                            │
│   - WhileSubscribed(5000L) causes memory leaks on config change  │
│   - Manual error handling duplicated across ViewModels           │
│   - No automatic cache invalidation/freshness                    │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### Current Repository Pattern Issues

**ContentRepositoryImpl.kt:**
```kotlin
// PROBLEM: Network-only, no caching
override suspend fun getMovieItems(page: Int, category: MovieListCategory): NetworkResponse<List<ContentItem>> {
    return try {
        val response = tmdbApi.getMovieLists(category = category.categoryName, page = page, region = accountDetailsDao.getRegionCode())
        NetworkResponse.Success(response.results.map(NetworkContentItem::asModel))
    } catch (e: IOException) {
        return NetworkResponse.Error()
    }
}
```

**DetailsRepositoryImpl.kt:**
```kotlin
// PROBLEM: Every detail request hits network, no local persistence
override suspend fun getMovieDetails(id: Int): NetworkResponse<MovieDetails> {
    return try {
        val response = tmdbApi.getMovieDetails(id).asModel()
        NetworkResponse.Success(response)
    } catch (e: IOException) {
        NetworkResponse.Error()
    }
}
```

### Current ViewModel Pattern Issues

**SearchViewModel.kt (Line 63-66):**
```kotlin
// PROBLEM: WhileSubscribed(5000L) can cause issues during configuration changes
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000L),  // <- PROBLEMATIC
    initialValue = emptyList()
)
```

**MoviesViewModel.kt:**
```kotlin
// PROBLEM: Manual state management, no reactive caching
private val _nowPlayingMovies = MutableStateFlow(ContentUiState(MovieListCategory.NOW_PLAYING))
val nowPlayingMovies = _nowPlayingMovies.asStateFlow()

// Manually handling loading, success, and error in every method
viewModelScope.launch {
    _nowPlayingMovies.update { it.copy(isLoading = true) }
    // ... fetch and update
}
```

### Existing Local Storage

The app already has Room database with:
- `FavoriteContentEntity` - User favorites
- `WatchlistContentEntity` - User watchlist
- `AccountDetailsEntity` - User account info

**Gap:** No entities for caching movie/TV content lists or details.

---

## Phase 2: Target Architecture

### Store5 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     TARGET OFFLINE-FIRST FLOW                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────────┐   │
│   │   Fetcher   │────▶│    Store    │◀────│   SourceOfTruth │   │
│   │  (Network)  │     │   (Cache)   │     │    (Room DB)    │   │
│   └─────────────┘     └─────────────┘     └─────────────────┘   │
│                              │                                    │
│                              ▼                                    │
│                       ┌─────────────┐                            │
│                       │StoreResponse│                            │
│                       │Loading/Data │                            │
│                       │/Error/NoNew │                            │
│                       └─────────────┘                            │
│                              │                                    │
│                              ▼                                    │
│                       ┌─────────────┐                            │
│                       │  Repository │                            │
│                       │ (Store API) │                            │
│                       └─────────────┘                            │
│                              │                                    │
│                              ▼                                    │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │                      ViewModel                           │   │
│   │  - Uses WhileSubscribedOrRetained                       │   │
│   │  - Handles StoreResponse states                         │   │
│   │  - Automatic loading/error handling                     │   │
│   └─────────────────────────────────────────────────────────┘   │
│                              │                                    │
│                              ▼                                    │
│                       ┌─────────────┐                            │
│                       │ Composable  │                            │
│                       │     UI      │                            │
│                       └─────────────┘                            │
│                                                                   │
│   BENEFITS:                                                       │
│   - Offline-first: Show cached data immediately                  │
│   - Automatic refresh in background                              │
│   - WhileSubscribedOrRetained prevents memory leaks              │
│   - Centralized error handling via StoreResponse                 │
│   - Freshness validation via Validator                           │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Phase 3: Store5 Benefits

### Why Store5?

1. **Offline-First by Design**
   - Shows cached data immediately
   - Fetches fresh data in background
   - Gracefully handles network failures

2. **Reactive Streams**
   - Emits `StoreResponse` with Loading/Data/Error states
   - Automatic state transitions
   - Subscribers get real-time updates

3. **Memory + Disk Cache**
   - In-memory cache for fast access
   - SourceOfTruth (Room) for persistence
   - Configurable cache policies

4. **Freshness Validation**
   - `Validator` decides if data is stale
   - Auto-triggers fetch when stale
   - Configurable TTL

5. **De-duplication**
   - Multiple requests for same key share single network call
   - Prevents redundant API calls

---

## Phase 4: WhileSubscribedOrRetained Benefits

### Why Not WhileSubscribed(5000)?

**Problem:** During configuration changes (rotation, dark mode toggle), the 5-second timeout can:
- Restart expensive operations
- Cause UI flicker
- Create unnecessary network requests

### WhileSubscribedOrRetained Solution

```kotlin
object WhileSubscribedOrRetained : SharingStarted {
    private val handler = Handler(Looper.getMainLooper())

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        subscriptionCount
            .transformLatest { count ->
                if (count > 0) {
                    emit(SharingCommand.START)
                } else {
                    val posted = CompletableDeferred<Unit>()
                    // Wait for frame + handler queue to clear
                    Choreographer.getInstance().postFrameCallback {
                        handler.postAtFrontOfQueue {
                            handler.post {
                                posted.complete(Unit)
                            }
                        }
                    }
                    posted.await()
                    emit(SharingCommand.STOP)
                }
            }
            .dropWhile { it != SharingCommand.START }
            .distinctUntilChanged()
}
```

**Benefits:**
- Keeps Flow alive during configuration changes
- Stops Flow only when truly no subscribers
- Prevents UI flicker and redundant fetches

---

## Phase 5: Implementation Plan

### Sprint 1: Foundation (Dependencies & Utilities)

#### 5.1.1 Add Store5 Dependency

**File:** `gradle/libs.versions.toml`
```toml
[versions]
store5 = "5.1.0-alpha03"

[libraries]
store5 = { group = "org.mobilenativefoundation.store", name = "store5", version.ref = "store5" }
```

**File:** `data/build.gradle.kts`
```kotlin
dependencies {
    implementation(libs.store5)
    // ... existing deps
}
```

#### 5.1.2 Implement WhileSubscribedOrRetained

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/coroutines/WhileSubscribedOrRetained.kt`

---

### Sprint 2: Database Entities for Content Caching

#### 5.2.1 Create Content Cache Entities

**File:** `core/local/src/main/java/com/keisardev/moviesandbeyond/core/local/database/entity/CachedContentEntity.kt`
```kotlin
@Entity(
    tableName = "cached_content",
    primaryKeys = ["id", "category"],
    indices = [Index("category"), Index("fetchedAt")]
)
data class CachedContentEntity(
    val id: Int,
    val category: String,           // "movie_now_playing", "movie_popular", "tv_airing_today", etc.
    val imagePath: String,
    val name: String,
    val backdropPath: String?,
    val rating: Double?,
    val releaseDate: String?,
    val overview: String?,
    val page: Int,
    val fetchedAt: Long = System.currentTimeMillis()
)
```

**File:** `core/local/src/main/java/com/keisardev/moviesandbeyond/core/local/database/entity/CachedDetailsEntity.kt`
```kotlin
@Entity(tableName = "cached_movie_details")
data class CachedMovieDetailsEntity(
    @PrimaryKey val id: Int,
    val json: String,               // Serialized MovieDetails
    val fetchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_tv_details")
data class CachedTvDetailsEntity(
    @PrimaryKey val id: Int,
    val json: String,               // Serialized TvDetails
    val fetchedAt: Long = System.currentTimeMillis()
)
```

#### 5.2.2 Create DAOs

**File:** `core/local/src/main/java/com/keisardev/moviesandbeyond/core/local/database/dao/CachedContentDao.kt`
```kotlin
@Dao
interface CachedContentDao {
    @Query("SELECT * FROM cached_content WHERE category = :category ORDER BY page, id")
    fun observeByCategory(category: String): Flow<List<CachedContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedContentEntity>)

    @Query("DELETE FROM cached_content WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("DELETE FROM cached_content WHERE category = :category AND page = :page")
    suspend fun deleteByPage(category: String, page: Int)
}
```

---

### Sprint 3: Create Stores

#### 5.3.1 Content Store Keys

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/ContentStoreKey.kt`
```kotlin
data class MovieContentKey(
    val category: MovieListCategory,
    val page: Int
)

data class TvContentKey(
    val category: TvShowListCategory,
    val page: Int
)

data class DetailsKey(
    val id: Int,
    val mediaType: MediaType
)
```

#### 5.3.2 Movie Content Store

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/MovieContentStore.kt`
```kotlin
@Singleton
class MovieContentStoreFactory @Inject constructor(
    private val api: TmdbApi,
    private val dao: CachedContentDao,
    private val accountDetailsDao: AccountDetailsDao
) {
    fun create(): Store<MovieContentKey, List<ContentItem>> = StoreBuilder.from(
        fetcher = Fetcher.of { key: MovieContentKey ->
            api.getMovieLists(
                category = key.category.categoryName,
                page = key.page,
                region = accountDetailsDao.getRegionCode()
            ).results.map(NetworkContentItem::asModel)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key ->
                dao.observeByCategory("movie_${key.category.categoryName}")
                    .map { entities ->
                        entities.filter { it.page == key.page }
                            .map { it.toContentItem() }
                            .takeIf { it.isNotEmpty() }
                    }
            },
            writer = { key, items ->
                dao.insertAll(items.map { it.toCachedEntity("movie_${key.category.categoryName}", key.page) })
            },
            delete = { key ->
                dao.deleteByPage("movie_${key.category.categoryName}", key.page)
            },
            deleteAll = {
                // Delete all movie categories
            }
        )
    ).validator(
        Validator.by { items: List<ContentItem>? ->
            // Data is fresh for 30 minutes
            items != null
        }
    ).build()
}
```

#### 5.3.3 TV Show Content Store

Similar pattern to Movie Content Store.

#### 5.3.4 Details Store

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/DetailsStore.kt`
```kotlin
@Singleton
class MovieDetailsStoreFactory @Inject constructor(
    private val api: TmdbApi,
    private val dao: CachedMovieDetailsDao
) {
    fun create(): Store<Int, MovieDetails> = StoreBuilder.from(
        fetcher = Fetcher.of { id: Int ->
            api.getMovieDetails(id).asModel()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id ->
                dao.observeById(id).map { entity ->
                    entity?.let { Json.decodeFromString<MovieDetails>(it.json) }
                }
            },
            writer = { id, details ->
                dao.insert(CachedMovieDetailsEntity(
                    id = id,
                    json = Json.encodeToString(details),
                    fetchedAt = System.currentTimeMillis()
                ))
            },
            delete = { id -> dao.deleteById(id) },
            deleteAll = { dao.deleteAll() }
        )
    ).cachePolicy(
        MemoryPolicy.builder<Int, MovieDetails>()
            .setMaxSize(50)
            .setExpireAfterWrite(30.minutes)
            .build()
    ).build()
}
```

---

### Sprint 4: Migrate Repositories

#### 5.4.1 Update ContentRepository Interface

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/repository/ContentRepository.kt`
```kotlin
interface ContentRepository {
    // Old API (kept for compatibility)
    suspend fun getMovieItems(page: Int, category: MovieListCategory): NetworkResponse<List<ContentItem>>
    suspend fun getTvShowItems(page: Int, category: TvShowListCategory): NetworkResponse<List<ContentItem>>

    // New Store5 API
    fun observeMovieItems(category: MovieListCategory, page: Int): Flow<StoreResponse<List<ContentItem>>>
    fun observeTvShowItems(category: TvShowListCategory, page: Int): Flow<StoreResponse<List<ContentItem>>>
    suspend fun refreshMovieItems(category: MovieListCategory, page: Int)
    suspend fun refreshTvShowItems(category: TvShowListCategory, page: Int)
}
```

#### 5.4.2 Update ContentRepositoryImpl

**File:** `data/src/main/java/com/keisardev/moviesandbeyond/data/repository/impl/ContentRepositoryImpl.kt`
```kotlin
internal class ContentRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val accountDetailsDao: AccountDetailsDao,
    private val movieContentStore: Store<MovieContentKey, List<ContentItem>>,
    private val tvContentStore: Store<TvContentKey, List<ContentItem>>
) : ContentRepository {

    // Legacy API (can be removed after migration)
    override suspend fun getMovieItems(page: Int, category: MovieListCategory): NetworkResponse<List<ContentItem>> {
        // ... existing implementation
    }

    // New Store5 API
    override fun observeMovieItems(category: MovieListCategory, page: Int): Flow<StoreResponse<List<ContentItem>>> =
        movieContentStore.stream(
            StoreReadRequest.cached(
                key = MovieContentKey(category, page),
                refresh = true
            )
        )

    override suspend fun refreshMovieItems(category: MovieListCategory, page: Int) {
        movieContentStore.fresh(MovieContentKey(category, page))
    }

    // Similar for TV shows...
}
```

---

### Sprint 5: Update ViewModels

#### 5.5.1 Update MoviesViewModel

**File:** `feature/movies/src/main/java/com/keisardev/moviesandbeyond/feature/movies/MoviesViewModel.kt`
```kotlin
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _nowPlayingPage = MutableStateFlow(1)

    val nowPlayingMovies: StateFlow<MovieListUiState> = _nowPlayingPage
        .flatMapLatest { page ->
            contentRepository.observeMovieItems(MovieListCategory.NOW_PLAYING, page)
        }
        .map { response -> response.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribedOrRetained,  // <-- Key change!
            initialValue = MovieListUiState.Loading
        )

    // Similar for other categories...

    fun loadNextPage(category: MovieListCategory) {
        when (category) {
            MovieListCategory.NOW_PLAYING -> _nowPlayingPage.update { it + 1 }
            // ...
        }
    }

    fun refresh(category: MovieListCategory) {
        viewModelScope.launch {
            when (category) {
                MovieListCategory.NOW_PLAYING ->
                    contentRepository.refreshMovieItems(category, _nowPlayingPage.value)
                // ...
            }
        }
    }
}

sealed interface MovieListUiState {
    data object Loading : MovieListUiState
    data class Success(
        val items: List<ContentItem>,
        val isRefreshing: Boolean = false,
        val isFromCache: Boolean = false
    ) : MovieListUiState
    data class Error(val message: String, val cachedItems: List<ContentItem>? = null) : MovieListUiState
}

fun StoreResponse<List<ContentItem>>.toUiState(): MovieListUiState = when (this) {
    is StoreResponse.Loading -> MovieListUiState.Loading
    is StoreResponse.Data -> MovieListUiState.Success(
        items = value,
        isFromCache = origin == StoreResponse.Origin.Cache
    )
    is StoreResponse.Error.Exception -> MovieListUiState.Error(error.message ?: "Unknown error")
    is StoreResponse.Error.Message -> MovieListUiState.Error(message)
    is StoreResponse.NoNewData -> MovieListUiState.Loading // Keep current state
}
```

#### 5.5.2 Update SearchViewModel

Replace `SharingStarted.WhileSubscribed(5000L)` with `WhileSubscribedOrRetained`.

#### 5.5.3 Update DetailsViewModel

Replace `SharingStarted.WhileSubscribed(5000L)` with `WhileSubscribedOrRetained`.

---

## Phase 6: File Structure

```
data/
├── src/main/java/com/keisardev/moviesandbeyond/data/
│   ├── coroutines/
│   │   └── WhileSubscribedOrRetained.kt          [NEW]
│   ├── store/
│   │   ├── ContentStoreKey.kt                     [NEW]
│   │   ├── MovieContentStore.kt                   [NEW]
│   │   ├── TvContentStore.kt                      [NEW]
│   │   ├── MovieDetailsStore.kt                   [NEW]
│   │   ├── TvDetailsStore.kt                      [NEW]
│   │   └── StoreResponseExtensions.kt             [NEW]
│   ├── repository/
│   │   ├── ContentRepository.kt                   [MODIFY]
│   │   ├── DetailsRepository.kt                   [MODIFY]
│   │   └── impl/
│   │       ├── ContentRepositoryImpl.kt           [MODIFY]
│   │       └── DetailsRepositoryImpl.kt           [MODIFY]
│   └── di/
│       ├── RepositoryModule.kt                    [MODIFY]
│       └── StoreModule.kt                         [NEW]

core/local/
├── src/main/java/com/keisardev/moviesandbeyond/core/local/database/
│   ├── entity/
│   │   ├── CachedContentEntity.kt                 [NEW]
│   │   ├── CachedMovieDetailsEntity.kt            [NEW]
│   │   └── CachedTvDetailsEntity.kt               [NEW]
│   ├── dao/
│   │   ├── CachedContentDao.kt                    [NEW]
│   │   ├── CachedMovieDetailsDao.kt               [NEW]
│   │   └── CachedTvDetailsDao.kt                  [NEW]
│   └── MoviesAndBeyondDatabase.kt                 [MODIFY - add new DAOs]

feature/movies/
├── src/main/java/com/keisardev/moviesandbeyond/feature/movies/
│   └── MoviesViewModel.kt                         [MODIFY]

feature/tv/
├── src/main/java/com/keisardev/moviesandbeyond/feature/tv/
│   └── TvShowsViewModel.kt                        [MODIFY]

feature/search/
├── src/main/java/com/keisardev/moviesandbeyond/feature/search/
│   └── SearchViewModel.kt                         [MODIFY]

feature/details/
├── src/main/java/com/keisardev/moviesandbeyond/feature/details/
│   └── DetailsViewModel.kt                        [MODIFY]
```

---

## Phase 7: Testing Strategy

### Unit Tests

1. **Store Tests**
   - Test Fetcher returns correct data
   - Test SourceOfTruth reads/writes correctly
   - Test cache invalidation

2. **Repository Tests**
   - Test `observeMovieItems` emits cache then network
   - Test `observeMovieItems` emits cache on network error
   - Test `refreshMovieItems` forces network fetch

3. **ViewModel Tests**
   - Test `movieListUiState` emits Loading then Success
   - Test WhileSubscribedOrRetained behavior

### Integration Tests

1. **Offline Mode Test**
   - Enable airplane mode
   - Verify cached data displays
   - Verify error state for uncached data

2. **Cache Persistence Test**
   - Fetch data, kill app
   - Relaunch, verify data loads from cache

---

## Phase 8: Migration Strategy

### Gradual Migration

1. **Week 1:** Add Store5 dependency, implement WhileSubscribedOrRetained, create database entities
2. **Week 2:** Create Stores, update repositories with new APIs (keep old APIs)
3. **Week 3:** Migrate MoviesViewModel and TvShowsViewModel
4. **Week 4:** Migrate DetailsViewModel and SearchViewModel
5. **Week 5:** Remove legacy APIs, full testing

### Rollback Plan

Keep legacy `NetworkResponse`-based APIs until all ViewModels are migrated and tested.

---

## Phase 9: Success Criteria

- [ ] App works in airplane mode with previously fetched data
- [ ] Data persists across app restarts
- [ ] Fresh data is fetched when online
- [ ] No UI flicker during configuration changes
- [ ] `./gradlew clean build` succeeds
- [ ] All existing tests pass
- [ ] New Store/Repository tests added

---

## References

- [Store5 GitHub](https://github.com/MobileNativeFoundation/Store)
- [Store5 Documentation](https://mobilenativefoundation.github.io/Store/)
- [WhileSubscribedOrRetained Article](https://blog.p-y.wtf/whilesubscribed5000)
- [ChatGPT-Android Reference](https://github.com/skydoves/chatgpt-android)
