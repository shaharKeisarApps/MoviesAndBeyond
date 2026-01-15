---
name: viewmodel-testing-expert
description: Use when testing @HiltViewModel classes with StateFlow, Turbine, and MainDispatcherRule. Triggers on "ViewModel test", "StateFlow test", "Turbine", "MainDispatcherRule", "test coroutines", "WhileSubscribedOrRetained test".
category: testing
allowed-tools: Read, Edit, Write, Bash, Grep, Glob
---

# ViewModel Testing Expert Skill

## Overview

Testing patterns for `@HiltViewModel` classes using Turbine, MainDispatcherRule, and test doubles. Special handling for `WhileSubscribedOrRetained`.

## When to Use

- Writing unit tests for ViewModels
- Testing StateFlow emissions with Turbine
- Mocking repository responses (StoreReadResponse)
- Verifying pagination behavior
- Testing error handling flows

## Dependencies

```toml
# libs.versions.toml
[versions]
coroutines = "1.10.2"
turbine = "1.2.0"
kotest = "5.9.1"

[libraries]
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotest-assertions = { group = "io.kotest", name = "kotest-assertions-core", version.ref = "kotest" }
```

## WhileSubscribedOrRetained in Tests

The custom `WhileSubscribedOrRetained` automatically detects test environment:

```kotlin
private val isInTestEnvironment: Boolean by lazy {
    try { Looper.getMainLooper(); false }
    catch (_: RuntimeException) { true }
}
```

In tests (where Looper throws), `STOP` is emitted immediately without waiting for Choreographer. This means tests behave like standard `WhileSubscribed` - no special mocking needed.

## Core Patterns

### MainDispatcherRule (core/testing/)

```kotlin
// core/testing/src/main/java/.../MainDispatcherRule.kt
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### Basic ViewModel Test Structure

```kotlin
class MoviesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeContentRepository
    private lateinit var viewModel: MoviesViewModel

    @Before
    fun setup() {
        repository = FakeContentRepository()
        viewModel = MoviesViewModel(repository)
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel.nowPlayingMovies.test {
            val initial = awaitItem()
            assertThat(initial.isLoading).isTrue()
        }
    }
}
```

### Testing StateFlow Emissions with Turbine

```kotlin
@Test
fun `emits success when repository returns data`() = runTest {
    val movies = listOf(testMovie1, testMovie2)

    viewModel.nowPlayingMovies.test {
        // Skip initial Loading state
        skipItems(1)

        // Trigger data emission
        repository.emitData(movies)

        // Assert success state
        val success = awaitItem()
        assertThat(success.isLoading).isFalse()
        assertThat(success.items).isEqualTo(movies)
        assertThat(success.items).hasSize(2)
    }
}

@Test
fun `handles error from repository`() = runTest {
    viewModel.nowPlayingMovies.test {
        skipItems(1) // Skip initial Loading

        repository.emitError("Network error")

        val error = awaitItem()
        assertThat(error.isLoading).isFalse()
    }

    // Error message is exposed separately
    viewModel.errorMessage.test {
        assertThat(awaitItem()).isEqualTo("Network error")
    }
}
```

### Testing Pagination

```kotlin
@Test
fun `appendItems increments page and accumulates`() = runTest {
    val page1 = listOf(testMovie1)
    val page2 = listOf(testMovie2)

    viewModel.nowPlayingMovies.test {
        skipItems(1) // Loading

        // Page 1 data
        repository.emitData(page1)
        assertThat(awaitItem().items).hasSize(1)

        // Request page 2
        viewModel.appendItems(MovieListCategory.NOW_PLAYING)

        skipItems(1) // Loading for page 2

        // Page 2 data
        repository.emitData(page2)

        // Should accumulate
        assertThat(awaitItem().items).hasSize(2)
    }
}

@Test
fun `refresh resets page and clears accumulated`() = runTest {
    val initialMovies = listOf(testMovie1, testMovie2)
    val refreshedMovies = listOf(testMovie3)

    viewModel.nowPlayingMovies.test {
        skipItems(1)
        repository.emitData(initialMovies)
        assertThat(awaitItem().items).hasSize(2)

        // Trigger refresh
        viewModel.refresh(MovieListCategory.NOW_PLAYING)

        skipItems(1) // Loading after refresh

        // New data
        repository.emitData(refreshedMovies)

        // Should be reset, not accumulated
        assertThat(awaitItem().items).hasSize(1)
    }
}
```

### Testing isFromCache Behavior

```kotlin
@Test
fun `shows cache indicator when data is from cache`() = runTest {
    viewModel.nowPlayingMovies.test {
        skipItems(1)

        repository.emitData(testMovies, fromCache = true)

        val state = awaitItem()
        assertThat(state.isFromCache).isTrue()
    }
}

@Test
fun `hides cache indicator when data is from network`() = runTest {
    viewModel.nowPlayingMovies.test {
        skipItems(1)

        repository.emitData(testMovies, fromCache = false)

        val state = awaitItem()
        assertThat(state.isFromCache).isFalse()
    }
}
```

### Testing Error Dismissal

```kotlin
@Test
fun `onErrorShown clears error message`() = runTest {
    viewModel.nowPlayingMovies.test {
        skipItems(1)
        repository.emitError("Error")
        awaitItem()
    }

    viewModel.errorMessage.test {
        assertThat(awaitItem()).isEqualTo("Error")

        viewModel.onErrorShown()

        assertThat(awaitItem()).isNull()
    }
}
```

## Fake Repository Pattern

```kotlin
// data/testdoubles/repository/FakeContentRepository.kt
class FakeContentRepository : ContentRepository {

    private val responseFlow = MutableSharedFlow<StoreReadResponse<List<ContentItem>>>()

    suspend fun emitLoading() {
        responseFlow.emit(StoreReadResponse.Loading(ResponseOrigin.Fetcher))
    }

    suspend fun emitData(items: List<ContentItem>, fromCache: Boolean = false) {
        val origin = if (fromCache) ResponseOrigin.Cache else ResponseOrigin.Fetcher
        responseFlow.emit(StoreReadResponse.Data(items, origin))
    }

    suspend fun emitError(message: String) {
        responseFlow.emit(StoreReadResponse.Error.Message(message, ResponseOrigin.Fetcher))
    }

    suspend fun emitNoNewData() {
        responseFlow.emit(StoreReadResponse.NoNewData(ResponseOrigin.Fetcher))
    }

    override fun observeMovieItems(
        category: MovieListCategory,
        page: Int
    ): Flow<StoreReadResponse<List<ContentItem>>> = responseFlow

    override suspend fun refreshMovies(category: MovieListCategory) {
        // No-op for tests
    }
}
```

## Test Fixtures

```kotlin
// data/testdoubles/TestFixtures.kt
object TestFixtures {

    val testMovie1 = ContentItem(
        id = 1,
        title = "Test Movie 1",
        posterPath = "/poster1.jpg",
        overview = "Overview 1",
        voteAverage = 8.5,
        releaseDate = "2024-01-15"
    )

    val testMovie2 = ContentItem(
        id = 2,
        title = "Test Movie 2",
        posterPath = "/poster2.jpg",
        overview = "Overview 2",
        voteAverage = 7.5,
        releaseDate = "2024-02-20"
    )

    val testMovie3 = ContentItem(
        id = 3,
        title = "Test Movie 3",
        posterPath = "/poster3.jpg",
        overview = "Overview 3",
        voteAverage = 9.0,
        releaseDate = "2024-03-25"
    )

    val testMovies = listOf(testMovie1, testMovie2)
}
```

## Dispatcher Selection

### UnconfinedTestDispatcher (Default)

- Executes coroutines eagerly
- Best for most ViewModel tests
- State changes visible immediately

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule() // Uses UnconfinedTestDispatcher
```

### StandardTestDispatcher

- Requires manual advancement
- Better for testing timing-sensitive code
- More control over execution order

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

@Test
fun `timing sensitive test`() = runTest {
    viewModel.triggerAction()
    advanceUntilIdle() // Manually advance
    // Assert state
}
```

## Turbine Best Practices

### Use `skipItems()` for Known Emissions

```kotlin
viewModel.state.test {
    skipItems(1) // Skip initial state
    // Focus on what we're testing
}
```

### Use `awaitItem()` for Specific Assertions

```kotlin
val state = awaitItem()
assertThat(state.isLoading).isFalse()
assertThat(state.items).hasSize(5)
```

### Use `cancelAndIgnoreRemainingEvents()` to End Early

```kotlin
viewModel.state.test {
    awaitItem() // Get first emission
    // Don't care about rest
    cancelAndIgnoreRemainingEvents()
}
```

### Use `expectNoEvents()` When No Emission Expected

```kotlin
viewModel.state.test {
    expectNoEvents()
}
```

## Anti-Patterns

### WRONG: Accessing .value directly without Turbine

```kotlin
// Don't do this - misses state transitions
@Test
fun `bad test`() = runTest {
    viewModel.triggerAction()
    assertThat(viewModel.state.value.isLoading).isFalse() // Race condition!
}
```

### WRONG: Forgetting MainDispatcherRule

```kotlin
// Don't do this - Dispatchers.Main won't work
class BadTest {
    // Missing: @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `will fail`() = runTest {
        // Crashes: Module with the Main dispatcher had failed to initialize
    }
}
```

### WRONG: Not handling all StateFlow emissions

```kotlin
// Don't do this - test may timeout
viewModel.state.test {
    // If there are 3 emissions but we only handle 2, test fails
    awaitItem()
    awaitItem()
    // Missing: awaitItem() or cancelAndIgnoreRemainingEvents()
}
```

## Running Tests

```bash
# Run all ViewModel tests
./gradlew :feature:movies:testDebugUnitTest --tests "*ViewModelTest"

# Run specific test class
./gradlew :feature:movies:testDebugUnitTest --tests "MoviesViewModelTest"

# Run with coverage
./gradlew :feature:movies:testDebugUnitTest jacocoTestReport
```

## Related Skills

- **testing-expert**: General testing philosophy
- **compose-viewmodel-bridge**: ViewModel patterns being tested
- **store5-room-bridge**: Repository patterns for fakes

## Files Reference

- `core/testing/MainDispatcherRule.kt` - Dispatcher rule
- `data/testdoubles/repository/` - Fake repositories
- `data/testdoubles/TestFixtures.kt` - Test data
- `feature/*/test/` - ViewModel tests

See: [reference.md](reference.md) | [examples.md](examples.md)
