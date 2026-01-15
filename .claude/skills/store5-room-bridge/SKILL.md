---
name: store5-room-bridge
description: Use when implementing offline-first data pipelines with Store5 + Room. Triggers on "StoreReadResponse", "SourceOfTruth", "Fetcher", "cache", "entity mapping", "Room DAO Flow", "offline-first".
category: data-layer
allowed-tools: Read, Edit, Write, Grep, Glob, Bash
---

# Store5 + Room Bridge Skill

## Overview

Expert guidance for implementing offline-first data pipelines using Store5 with Room as SourceOfTruth. This skill documents patterns from MoviesAndBeyond's production implementation.

## When to Use

- Implementing repository with cache-first pattern
- Setting up Store5 builder with Room DAO
- Handling StoreReadResponse in ViewModel
- Configuring pagination with Store5
- Designing Room entities for Store5 caching
- Debugging offline-first data flow issues

## Quick Start

### Store Builder Pattern

```kotlin
@Singleton
class MovieContentStoreFactory @Inject constructor(
    private val api: TmdbApi,
    private val cachedContentDao: CachedContentDao
) {
    fun create(): Store<MovieContentKey, List<ContentItem>> =
        StoreBuilder.from(
            fetcher = Fetcher.of { key: MovieContentKey ->
                api.getMovieLists(key.category.categoryName, key.page)
                    .results
                    .map(NetworkContentItem::asModel)
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { key ->
                    cachedContentDao.observeByCategoryAndPage(key.toCategoryString(), key.page)
                        .map { entities -> entities.map { it.toContentItem() }.takeIf { it.isNotEmpty() } }
                },
                writer = { key, items ->
                    cachedContentDao.deleteByCategoryAndPage(key.toCategoryString(), key.page)
                    cachedContentDao.insertAll(items.toCachedEntities(key.toCategoryString(), key.page))
                },
                delete = { key -> cachedContentDao.deleteByCategoryAndPage(key.toCategoryString(), key.page) },
                deleteAll = { cachedContentDao.deleteAll() }
            )
        )
        .cachePolicy(MemoryPolicy.builder<MovieContentKey, List<ContentItem>>().setMaxSize(100).build())
        .build()
}
```

## Core Patterns

### 1. Store Key Design

**Location:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/ContentStoreKey.kt`

```kotlin
/**
 * Key for movie content Store. Combines category and page for unique identification.
 */
data class MovieContentKey(
    val category: MovieListCategory,
    val page: Int
) {
    /** Returns the category string used for database storage. */
    fun toCategoryString(): String = "movie_${category.categoryName}"
}

/**
 * Key for TV show content Store. Combines category and page for unique identification.
 */
data class TvContentKey(
    val category: TvShowListCategory,
    val page: Int
) {
    fun toCategoryString(): String = "tv_${category.categoryName}"
}
```

**Key Design Principles:**
- Include all parameters that uniquely identify the data
- Add a method to generate a stable string for database keys
- Keep keys immutable (data class)
- Consider pagination as part of the key

### 2. Room Entity Design for Store5

**Location:** `core/local/src/main/java/.../database/entity/CachedContentEntity.kt`

```kotlin
@Entity(
    tableName = "cached_content",
    primaryKeys = ["content_id", "category"],  // Composite key for deduplication
    indices = [
        Index(value = ["category"]),           // Query optimization
        Index(value = ["fetched_at"])          // Freshness queries
    ]
)
data class CachedContentEntity(
    @ColumnInfo(name = "content_id") val contentId: Int,
    val category: String,
    val page: Int,
    val position: Int,                         // For ordering within page
    @ColumnInfo(name = "image_path") val imagePath: String,
    val name: String,
    @ColumnInfo(name = "backdrop_path") val backdropPath: String?,
    val rating: Double?,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    val overview: String?,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long = System.currentTimeMillis()
)
```

**Entity Design Decisions:**
| Field | Purpose |
|-------|---------|
| Composite PK (`content_id`, `category`) | Same content can appear in multiple categories |
| `page` | Tracks which page this item was fetched from |
| `position` | Preserves API order within page (critical!) |
| `fetched_at` | Enables freshness validation and cleanup |
| Indices on `category`, `fetched_at` | Optimize common queries |

### 3. Room DAO for Store5

**Location:** `core/local/src/main/java/.../database/dao/CachedContentDao.kt`

```kotlin
@Dao
interface CachedContentDao {

    // Reader: MUST return Flow for reactive SourceOfTruth
    @Query("""
        SELECT * FROM cached_content
        WHERE category = :category
        ORDER BY page ASC, position ASC
    """)
    fun observeByCategory(category: String): Flow<List<CachedContentEntity>>

    // Reader with pagination support
    @Query("""
        SELECT * FROM cached_content
        WHERE category = :category AND page = :page
        ORDER BY position ASC
    """)
    fun observeByCategoryAndPage(category: String, page: Int): Flow<List<CachedContentEntity>>

    // Writer: Suspend for background execution
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedContentEntity>)

    // Delete: For cache invalidation (per page)
    @Query("DELETE FROM cached_content WHERE category = :category AND page = :page")
    suspend fun deleteByCategoryAndPage(category: String, page: Int)

    // Delete: For full category invalidation
    @Query("DELETE FROM cached_content WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    // Delete all: For full cache clear
    @Query("DELETE FROM cached_content")
    suspend fun deleteAll()

    // Freshness check
    @Query("SELECT MAX(fetched_at) FROM cached_content WHERE category = :category")
    suspend fun getLatestFetchTime(category: String): Long?

    // Cleanup old data
    @Query("DELETE FROM cached_content WHERE fetched_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
```

**DAO Requirements for Store5:**
- **Reader MUST return Flow**: Room DAO returns `Flow<List<T>>` for reactive updates
- **Return null/empty for no data**: SourceOfTruth reader should return `null` when cache is empty
- **Writer is suspend**: Background execution for database operations
- **Delete operations match keys**: Same granularity as Store keys

### 4. Entity Mapping Bridge

**Location:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/ContentEntityMapper.kt`

```kotlin
// Entity -> Domain Model
fun CachedContentEntity.toContentItem(): ContentItem =
    ContentItem(
        id = contentId,
        imagePath = imagePath,
        name = name,
        backdropPath = backdropPath,
        rating = rating,
        releaseDate = releaseDate,
        overview = overview
    )

// List convenience extension
fun List<CachedContentEntity>.toContentItems(): List<ContentItem> =
    map { it.toContentItem() }

// Domain Model -> Entity (for single item)
fun ContentItem.toCachedEntity(
    category: String,
    page: Int,
    position: Int
): CachedContentEntity =
    CachedContentEntity(
        contentId = id,
        category = category,
        page = page,
        position = position,
        imagePath = imagePath,
        name = name,
        backdropPath = backdropPath,
        rating = rating,
        releaseDate = releaseDate,
        overview = overview,
        fetchedAt = System.currentTimeMillis()
    )

// List -> Entities with proper positioning
fun List<ContentItem>.toCachedEntities(
    category: String,
    page: Int
): List<CachedContentEntity> =
    mapIndexed { index, item ->
        item.toCachedEntity(category, page, index)
    }
```

**Mapping Best Practices:**
- Keep mapping functions as extension functions for fluency
- Position is calculated from index (critical for order preservation)
- Always set `fetchedAt` during write
- Provide both single-item and list variants

### 5. StoreReadResponse Handling (All 4 States)

**Location:** `data/src/main/java/com/keisardev/moviesandbeyond/data/store/StoreResponseExtensions.kt`

```kotlin
/**
 * StoreReadResponse has 4 primary states that MUST all be handled:
 * 1. Loading - Data is being fetched
 * 2. Data - Data available (check origin for cache vs network)
 * 3. Error - Fetch failed (Exception or Message)
 * 4. NoNewData - Cache is still valid, no new data from network
 */

// Check if response has data
val <T> StoreReadResponse<T>.hasData: Boolean
    get() = this is StoreReadResponse.Data

// Extract data safely
fun <T> StoreReadResponse<T>.dataOrNull(): T? =
    (this as? StoreReadResponse.Data)?.value

// Check data origin
val <T> StoreReadResponse<T>.isFromCache: Boolean
    get() = this is StoreReadResponse.Data &&
        (origin is StoreReadResponseOrigin.Cache ||
         origin is StoreReadResponseOrigin.SourceOfTruth)

val <T> StoreReadResponse<T>.isFromNetwork: Boolean
    get() = this is StoreReadResponse.Data &&
        origin is StoreReadResponseOrigin.Fetcher

// Check loading state
val <T> StoreReadResponse<T>.isLoading: Boolean
    get() = this is StoreReadResponse.Loading

// Check error state
val <T> StoreReadResponse<T>.isError: Boolean
    get() = this is StoreReadResponse.Error

// Extract error message
fun <T> StoreReadResponse<T>.errorMessageOrNull(): String? = when (this) {
    is StoreReadResponse.Error.Exception -> error.message
    is StoreReadResponse.Error.Message -> message
    else -> null
}

// Convert to NetworkResponse (for legacy compatibility)
fun <T> StoreReadResponse<T>.toNetworkResponse(): NetworkResponse<T> = when (this) {
    is StoreReadResponse.Data -> NetworkResponse.Success(value)
    is StoreReadResponse.Error.Exception -> NetworkResponse.Error(error.message)
    is StoreReadResponse.Error.Message -> NetworkResponse.Error(message)
    is StoreReadResponse.Error.Custom<*> -> NetworkResponse.Error("Custom error")
    is StoreReadResponse.Loading -> NetworkResponse.Error("Loading")
    is StoreReadResponse.NoNewData -> NetworkResponse.Error("No new data")
    is StoreReadResponse.Initial -> NetworkResponse.Error("Initial state")
}
```

### 6. Repository Implementation

**Location:** `data/src/main/java/.../repository/impl/ContentRepositoryImpl.kt`

```kotlin
internal class ContentRepositoryImpl @Inject constructor(
    private val movieContentStore: Store<MovieContentKey, List<ContentItem>>,
    private val tvContentStore: Store<TvContentKey, List<ContentItem>>,
    private val cachedContentDao: CachedContentDao
) : ContentRepository {

    // Observe with cache-first, then refresh
    override fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean
    ): Flow<StoreReadResponse<List<ContentItem>>> =
        movieContentStore.stream(
            StoreReadRequest.cached(
                key = MovieContentKey(category, page),
                refresh = refresh  // true = fetch fresh after serving cache
            )
        )

    // Force fresh fetch (skip cache)
    override suspend fun refreshMovieItems(
        category: MovieListCategory,
        page: Int
    ): List<ContentItem> =
        movieContentStore
            .stream(StoreReadRequest.fresh(MovieContentKey(category, page)))
            .filterIsInstance<StoreReadResponse.Data<List<ContentItem>>>()
            .first()
            .value

    // Clear all cached data
    override suspend fun clearCache() {
        cachedContentDao.deleteAll()
    }
}
```

**StoreReadRequest Types:**
| Request Type | Behavior |
|--------------|----------|
| `cached(key, refresh=false)` | Return cache only, no network |
| `cached(key, refresh=true)` | Return cache, then fetch network |
| `fresh(key)` | Skip cache, fetch from network |

### 7. ViewModel StoreReadResponse Handling

**Location:** `feature/movies/src/main/java/.../MoviesViewModel.kt`

```kotlin
private fun handleStoreResponse(
    response: StoreReadResponse<List<ContentItem>>,
    category: MovieListCategory,
    page: Int,
    accumulatedFlow: MutableStateFlow<List<ContentItem>>
): ContentUiState {
    return when (response) {
        // State 1: Initial/Loading - show loading, keep existing data
        is StoreReadResponse.Initial,
        is StoreReadResponse.Loading -> {
            ContentUiState(
                items = accumulatedFlow.value,  // Keep showing existing data!
                isLoading = true,
                endReached = false,
                page = page,
                category = category,
                isFromCache = false
            )
        }

        // State 2: Data - accumulate for pagination
        is StoreReadResponse.Data -> {
            val newItems = response.value
            // Accumulate items for pagination (page 1 replaces, others append)
            if (newItems.isNotEmpty()) {
                accumulatedFlow.update { current ->
                    if (page == 1) newItems
                    else (current + newItems).distinctBy { it.id }
                }
            }
            ContentUiState(
                items = accumulatedFlow.value,
                isLoading = false,
                endReached = newItems.isEmpty(),
                page = page,
                category = category,
                isFromCache = response.isFromCache  // Track data staleness
            )
        }

        // State 3: Error - show error, keep existing data
        is StoreReadResponse.Error -> {
            _errorMessage.update { response.errorMessageOrNull() }
            ContentUiState(
                items = accumulatedFlow.value,  // Don't clear on error!
                isLoading = false,
                endReached = false,
                page = page,
                category = category,
                isFromCache = false
            )
        }

        // State 4: NoNewData - cache is still valid
        is StoreReadResponse.NoNewData -> {
            ContentUiState(
                items = accumulatedFlow.value,
                isLoading = false,
                endReached = false,
                page = page,
                category = category,
                isFromCache = true
            )
        }
    }
}
```

**Critical UX Patterns:**
- **Never clear data on loading**: Show existing data with loading indicator
- **Never clear data on error**: Keep stale data visible, show error separately
- **Track `isFromCache`**: UI can indicate stale data to user
- **Use `distinctBy` for pagination**: Prevent duplicates across pages

## Pagination Pattern

### Accumulation with Deduplication

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaginatedViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _page = MutableStateFlow(1)
    private val _accumulated = MutableStateFlow<List<ContentItem>>(emptyList())

    val items: StateFlow<ContentUiState> =
        _page.flatMapLatest { page ->
            contentRepository.observeItems(page)
                .map { response -> handleResponse(response, page) }
        }
        .combine(_accumulated) { state, items -> state.copy(items = items) }
        .stateInWhileSubscribed(viewModelScope, ContentUiState())

    fun loadMore() {
        _page.update { it + 1 }
    }

    fun refresh() {
        viewModelScope.launch {
            _accumulated.value = emptyList()
            _page.value = 1
        }
    }
}
```

## Data Flow Diagram

```
                          Store5 Data Flow
    ============================================================

    [UI Request]
         |
         v
    +----------------+
    | Repository     | observeItems(category, page)
    +----------------+
         |
         v
    +----------------+    StoreReadRequest.cached(key, refresh=true)
    | Store5         |
    +----------------+
         |
         +---> [MemoryCache] --> if hit, emit Data(origin=Cache)
         |
         +---> [SourceOfTruth (Room)] --> if hit, emit Data(origin=SourceOfTruth)
         |
         +---> [Fetcher (Network)] --> emit Data(origin=Fetcher)
                    |
                    v
              [SourceOfTruth Writer] --> persist to Room
                    |
                    v
              [Room emits update] --> SourceOfTruth reader re-emits

    ============================================================
    Emission Sequence (with cached + refresh):
    1. Loading (if no cache)
    2. Data(origin=SourceOfTruth) (if cache exists)
    3. Data(origin=Fetcher) (after network completes)
       OR Error (if network fails)
    ============================================================
```

## Common Gotchas

### 1. DAO Flow must handle empty state
```kotlin
// WRONG: Returns null which Store5 interprets as "no data"
fun observe(): Flow<List<Entity>?>

// RIGHT: Return empty list, let Store5 handle null
sourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        dao.observe(key).map { entities ->
            entities.takeIf { it.isNotEmpty() }  // Returns null if empty
        }
    }
)
```

### 2. Composite keys for shared content
```kotlin
// WRONG: Single ID as key - same movie in different categories overwrites
@Entity(primaryKeys = ["content_id"])

// RIGHT: Composite key allows same content in multiple categories
@Entity(primaryKeys = ["content_id", "category"])
```

### 3. Position field for ordering
```kotlin
// WRONG: Order is lost when items are inserted
fun toCachedEntities(items: List<Item>) = items.map { it.toEntity() }

// RIGHT: Preserve position from API response
fun toCachedEntities(items: List<Item>) = items.mapIndexed { index, item ->
    item.toEntity(position = index)
}
```

### 4. Handle all 4 StoreReadResponse states
```kotlin
// WRONG: Missing states cause crashes or unexpected behavior
when (response) {
    is StoreReadResponse.Data -> handleData()
    is StoreReadResponse.Error -> handleError()
}

// RIGHT: Exhaustive handling
when (response) {
    is StoreReadResponse.Initial,
    is StoreReadResponse.Loading -> handleLoading()
    is StoreReadResponse.Data -> handleData()
    is StoreReadResponse.Error -> handleError()
    is StoreReadResponse.NoNewData -> handleNoNewData()
}
```

### 5. Use `distinctBy` for pagination
```kotlin
// WRONG: Duplicates appear when same item is on multiple pages
accumulatedFlow.update { current -> current + newItems }

// RIGHT: Deduplicate by ID
accumulatedFlow.update { current ->
    (current + newItems).distinctBy { it.id }
}
```

## Validation Commands

```bash
# Verify Store5 patterns are correctly implemented
./scripts/validate-store5-patterns.sh

# Check for offline-first compliance
./scripts/validate-offline-first.sh
```

## Testing Store5 + Room

```kotlin
class ContentRepositoryTest {
    private lateinit var database: MoviesDatabase
    private lateinit var store: Store<MovieContentKey, List<ContentItem>>

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context,
            MoviesDatabase::class.java
        ).allowMainThreadQueries().build()

        store = StoreBuilder.from(
            fetcher = Fetcher.of { FakeApi.getMovies() },
            sourceOfTruth = SourceOfTruth.of(
                reader = { database.contentDao().observeAll().map { it.takeIf { it.isNotEmpty() } } },
                writer = { _, data -> database.contentDao().insertAll(data) },
                delete = { database.contentDao().deleteAll() },
                deleteAll = { database.contentDao().deleteAll() }
            )
        ).build()
    }

    @Test
    fun `serves cached data then network`() = runTest {
        // Pre-seed cache
        database.contentDao().insertAll(listOf(testEntity))

        store.stream(StoreReadRequest.cached(key, refresh = true)).test {
            // First emission: cached data
            val cached = awaitItem()
            assertThat(cached).isInstanceOf(StoreReadResponse.Data::class.java)
            assertThat((cached as StoreReadResponse.Data).origin)
                .isEqualTo(StoreReadResponseOrigin.SourceOfTruth)

            // Second emission: network data
            val fresh = awaitItem()
            assertThat((fresh as StoreReadResponse.Data).origin)
                .isEqualTo(StoreReadResponseOrigin.Fetcher)
        }
    }

    @Test
    fun `handles network error with cached data`() = runTest {
        // Pre-seed cache
        database.contentDao().insertAll(listOf(testEntity))

        // Configure API to fail
        fakeApi.shouldFail = true

        store.stream(StoreReadRequest.cached(key, refresh = true)).test {
            // First: cached data
            val cached = awaitItem()
            assertThat(cached).isInstanceOf(StoreReadResponse.Data::class.java)

            // Second: error from network
            val error = awaitItem()
            assertThat(error).isInstanceOf(StoreReadResponse.Error::class.java)

            // Data should still be accessible from cache!
        }
    }
}
```

## Related Skills

- **store5-expert**: For advanced Store5 configuration and validators
- **compose-viewmodel-bridge**: For ViewModel StateFlow patterns
- **coroutines-expert**: For Flow operators and error handling
- **testing-expert**: For comprehensive test patterns

## Files Reference

| File | Purpose |
|------|---------|
| `data/src/.../store/MovieContentStore.kt` | Movie Store5 factory |
| `data/src/.../store/TvContentStore.kt` | TV Store5 factory |
| `data/src/.../store/ContentStoreKey.kt` | Store key definitions |
| `data/src/.../store/ContentEntityMapper.kt` | Entity mapping functions |
| `data/src/.../store/StoreResponseExtensions.kt` | Response utility extensions |
| `data/src/.../repository/impl/ContentRepositoryImpl.kt` | Repository implementation |
| `core/local/src/.../database/dao/CachedContentDao.kt` | Room DAO |
| `core/local/src/.../database/entity/CachedContentEntity.kt` | Room entity |
| `feature/movies/src/.../MoviesViewModel.kt` | ViewModel with Store5 handling |
