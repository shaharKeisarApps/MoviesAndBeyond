# Test Coverage Gap Analysis - Why Bugs Weren't Caught

**Date**: 2026-02-02
**Context**: Task #3 - Two critical UI bugs were not caught by existing tests

---

## Bugs That Escaped Testing

### Bug #1: Guest Mode - Favorites/Watchlist Not Accessible
**Symptom**: LibrarySection only rendered in LoggedInView, not LoggedOutView
**Impact**: Guest users couldn't access their locally saved favorites
**Severity**: Critical - Core feature completely broken for guest users

### Bug #2: TMDB Sync Not Running on Login
**Symptom**: Sync only scheduled via WorkManager (delayed), not run immediately
**Impact**: Authenticated users didn't see their TMDB favorites after login
**Severity**: Critical - Poor first-login experience

---

## Why Tests Didn't Catch These Bugs

### Root Cause Analysis

#### 1. **No UI/Navigation Tests** ❌
**Missing**: Tests that verify UI components are actually rendered and accessible

**What We Had**:
- ✅ Unit tests for ViewModel logic
- ✅ Unit tests for Repository logic
- ✅ Instrumentation tests for DAO queries

**What We Didn't Have**:
- ❌ UI tests verifying LibrarySection appears for guest users
- ❌ Navigation tests verifying "Favorites" link navigates correctly
- ❌ Compose UI tests (Robolectric or instrumentation)

**Example Missing Test**:
```kotlin
@Test
fun `guest user sees library section on You screen`() {
    composeTestRule.setContent {
        YouScreen(
            isLoggedIn = false,
            onNavigateToAuth = {},
            onLibraryItemClick = {}
        )
    }

    // Verify Library Section is visible
    composeTestRule.onNodeWithText("Your Library").assertExists()
    composeTestRule.onNodeWithText("Favorites").assertExists()
    composeTestRule.onNodeWithText("Watchlist").assertExists()
}
```

---

#### 2. **No Integration Tests** ❌
**Missing**: Tests that verify multiple components work together

**What We Had**:
- ✅ Unit tests for individual components in isolation
- ✅ Test doubles (TestLibraryRepository, TestAuthRepository)

**What We Didn't Have**:
- ❌ Integration tests verifying YouViewModel → LibraryRepository → DAO flow
- ❌ Tests verifying login triggers immediate sync
- ❌ Tests verifying guest mode end-to-end flow

**Example Missing Test**:
```kotlin
@Test
fun `login triggers immediate sync of TMDB favorites`() = runTest {
    // Setup: User has favorites on TMDB
    val remoteFavorites = listOf(
        NetworkFavoriteItem(id = 1, name = "Movie 1"),
        NetworkFavoriteItem(id = 2, name = "Movie 2")
    )
    fakeTmdbApi.setFavorites(remoteFavorites)

    // Action: User logs in
    authRepository.setAuthStatus(true)
    viewModel.getAccountDetails()

    // Wait for sync to complete
    advanceUntilIdle()

    // Verify: Local database has TMDB favorites
    val localFavorites = favoriteDao.getFavoriteMovies().first()
    assertEquals(2, localFavorites.size)
    assertEquals(SyncStatus.SYNCED, localFavorites[0].syncStatus)
}
```

---

#### 3. **Test Isolation vs. Real-World Scenarios** ❌
**Problem**: Tests used mocks/fakes that behaved differently from real implementation

**What We Had**:
- ✅ `TestLibraryRepository` with in-memory StateFlow
- ✅ `TestAuthRepository` with simple boolean flag

**What We Didn't Have**:
- ❌ Tests using real Room database (instrumentation)
- ❌ Tests verifying WorkManager scheduling
- ❌ Tests simulating user login flow

**Example of Divergence**:
```kotlin
// TestLibraryRepository (simplified, always works)
override suspend fun syncFavorites(): Result<Unit> {
    _favorites.value = testFavorites  // Instant, no network
    return Result.success(Unit)
}

// Real LibraryRepositoryImpl (complex, async)
override suspend fun syncFavorites(): Result<Unit> {
    val networkFavorites = tmdbApi.getFavorites()  // Network call
    favoriteDao.upsertFavoriteItems(...)  // Database write
    // Sync might fail, be delayed, etc.
    return Result.success(Unit)
}
```

**Why This Matters**: The test passes because the fake always works instantly. The real implementation has timing issues, network dependencies, and WorkManager delays.

---

#### 4. **No Behavioral/User Journey Tests** ❌
**Missing**: Tests that simulate actual user workflows

**Example User Journeys Not Tested**:
1. Guest adds favorite → navigates to "You" screen → sees favorite
2. User logs in → TMDB favorites appear immediately
3. Guest adds favorite → logs in → favorites merge correctly

**What We Had**:
- ✅ Tests for individual actions (add favorite, remove favorite)
- ✅ Tests for state changes (markedFavorite = true/false)

**What We Didn't Have**:
- ❌ Multi-step workflow tests
- ❌ Tests verifying UI reflects backend state
- ❌ Tests simulating real user behavior

---

## Test Coverage Recommendations

### Priority 1: Compose UI Tests (High Impact)

**Purpose**: Verify UI components render correctly and respond to state changes

**Technology**: Compose Testing (androidx.compose.ui.test)

**Implementation**:
```kotlin
// feature/you/src/androidTest/java/YouScreenTest.kt

@RunWith(AndroidJUnit4::class)
class YouScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun guestUserSeesLibrarySection() {
        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = null,
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                // ... other params
            )
        }

        // Verify guest user sees library section
        composeTestRule.onNodeWithText("Your Library").assertExists()
        composeTestRule.onNodeWithText("Favorites").assertExists()
        composeTestRule.onNodeWithText("Watchlist").assertExists()
    }

    @Test
    fun clickingFavoritesNavigatesToFavoritesList() {
        var navigatedTo: String? = null

        composeTestRule.setContent {
            YouScreen(
                isLoggedIn = false,
                onLibraryItemClick = { type -> navigatedTo = type },
                // ...
            )
        }

        // Click Favorites
        composeTestRule.onNodeWithText("Favorites").performClick()

        // Verify navigation
        assertEquals("FAVORITE", navigatedTo)
    }

    @Test
    fun loggedInUserSeesAccountDetails() {
        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(
                    accountDetails = AccountDetails(
                        username = "testuser",
                        name = "Test User"
                    )
                ),
                isLoggedIn = true,
                // ...
            )
        }

        // Verify account details visible
        composeTestRule.onNodeWithText("testuser").assertExists()
        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("Your Library").assertExists()
    }
}
```

**Coverage**:
- ✅ Verifies UI components actually render
- ✅ Verifies guest/authenticated states show correct views
- ✅ Verifies navigation callbacks work
- ✅ Catches UI structure bugs like Bug #1

**Effort**: Medium (2-4 hours to set up, 30 min per test)

---

### Priority 2: Integration Tests (High Impact)

**Purpose**: Verify multiple layers work together (ViewModel → Repository → DAO)

**Technology**: Hilt + Robolectric or Android instrumentation tests with real dependencies

**Implementation**:
```kotlin
// feature/you/src/androidTest/java/YouViewModelIntegrationTest.kt

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class YouViewModelIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var libraryRepository: LibraryRepository  // Real implementation

    @Inject
    lateinit var authRepository: AuthRepository  // Real implementation

    @Inject
    lateinit var database: MoviesAndBeyondDatabase  // Real Room database

    lateinit var viewModel: YouViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = YouViewModel(authRepository, libraryRepository, ...)
    }

    @Test
    fun loginTriggersSyncAndLoadsFavorites() = runTest {
        // Setup: Populate fake TMDB API with favorites
        // (This requires injecting a fake network module)

        // Action: Trigger login
        authRepository.login("testuser", "password")
        viewModel.getAccountDetails()

        // Wait for sync
        advanceUntilIdle()

        // Verify: Database has TMDB favorites
        val favorites = database.favoriteContentDao().getFavoriteMovies().first()
        assertTrue(favorites.isNotEmpty())
        assertEquals(SyncStatus.SYNCED, favorites[0].syncStatus)
    }

    @Test
    fun guestFavoritesVisibleInLibrary() = runTest {
        // Setup: Guest mode
        authRepository.logout()

        // Action: Add favorite as guest
        val item = LibraryItem(id = 1, name = "Movie", mediaType = MediaType.MOVIE, ...)
        libraryRepository.addOrRemoveFavorite(item, isAuthenticated = false)

        // Verify: Item appears in favorites list
        val favorites = libraryRepository.favoriteMovies.first()
        assertEquals(1, favorites.size)
        assertEquals(SyncStatus.LOCAL_ONLY, favorites[0].syncStatus)
    }
}
```

**Coverage**:
- ✅ Verifies real Room database queries work
- ✅ Verifies sync actually runs on login
- ✅ Verifies end-to-end data flow
- ✅ Catches both Bug #1 and Bug #2

**Effort**: High (4-8 hours to set up Hilt test infrastructure, 1 hour per test)

---

### Priority 3: Screenshot Tests (Medium Impact)

**Purpose**: Catch visual regressions and missing UI elements

**Technology**: Android Screenshot Testing (Paparazzi, Roborazzi, or Shot)

**Implementation**:
```kotlin
@RunWith(PaparazziTestRunner::class)
class YouScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun youScreenGuestMode() {
        paparazzi.snapshot {
            YouScreen(
                isLoggedIn = false,
                // ... params
            )
        }
    }

    @Test
    fun youScreenAuthenticatedMode() {
        paparazzi.snapshot {
            YouScreen(
                isLoggedIn = true,
                accountDetails = AccountDetails(...),
                // ... params
            )
        }
    }
}
```

**Coverage**:
- ✅ Visual regression detection
- ✅ Catches missing UI elements
- ✅ Great for PR reviews

**Effort**: Medium (3-5 hours to set up, 15 min per test)

---

### Priority 4: End-to-End Tests (Lower Priority)

**Purpose**: Test full user flows across multiple screens

**Technology**: Espresso or Compose UI Test with full app

**Implementation**:
```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class FavoritesE2ETest {

    @get:Rule
    val activityRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun guestCanAddAndViewFavorite() {
        // 1. Navigate to Movies screen
        // 2. Click on a movie
        // 3. Click "Add to favorites"
        // 4. Navigate to "You" screen
        // 5. Click "Favorites"
        // 6. Verify movie appears in list
    }
}
```

**Coverage**:
- ✅ Full user journey verification
- ✅ Catches navigation bugs
- ✅ Realistic testing

**Effort**: Very High (8-12 hours to set up, 2-3 hours per test)

**Tradeoff**: Slow to run, brittle, hard to maintain

---

## Recommended Test Strategy

### Test Pyramid for This Project

```
        /\
       /E2E\          (Few) - 5% of tests
      /------\
     /Screen-\        (Some) - 15% of tests
    /shot Tests\
   /------------\
  / Integration \     (More) - 30% of tests
 /    Tests      \
/------------------\
/   Unit Tests     \  (Most) - 50% of tests
/--------------------\
```

### Immediate Actions (This PR)

1. ✅ **Add Compose UI tests for YouScreen** (Priority 1)
   - `guestUserSeesLibrarySection()`
   - `loggedInUserSeesLibrarySection()`
   - `clickingFavoritesNavigates()`

   **Effort**: 2 hours
   **Impact**: Would have caught Bug #1

2. ✅ **Add integration test for sync on login** (Priority 2)
   - `loginTriggersImmediateSync()`

   **Effort**: 4 hours (includes Hilt setup)
   **Impact**: Would have caught Bug #2

### Future Improvements (Next Sprint)

3. **Add screenshot tests** (Priority 3)
   - Set up Paparazzi or Roborazzi
   - Snapshot all major screens

   **Effort**: 1 day
   **Impact**: Prevents visual regressions

4. **Add E2E tests for critical paths** (Priority 4)
   - Guest favorites flow
   - Login and sync flow
   - Cross-device sync (if applicable)

   **Effort**: 2-3 days
   **Impact**: Full confidence in user journeys

---

## Specific Test Additions for This Bug Fix

### Test 1: Guest Mode Library Access
**File**: `feature/you/src/androidTest/java/YouScreenComposeTest.kt`

```kotlin
@Test
fun guestUserCanAccessLibrarySection() {
    composeTestRule.setContent {
        YouScreen(
            uiState = YouUiState(),
            isLoggedIn = false,
            userSettings = UserSettings(...),
            onLibraryItemClick = { /* verify called */ },
            // ... other params
        )
    }

    // Verify library section visible
    composeTestRule.onNodeWithText("Your Library").assertIsDisplayed()
    composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    composeTestRule.onNodeWithText("Watchlist").assertIsDisplayed()

    // Verify clickable
    composeTestRule.onNodeWithText("Favorites").assertHasClickAction()
}
```

**Why This Catches Bug #1**: Directly verifies LibrarySection is rendered for guest users.

---

### Test 2: Login Triggers Sync
**File**: `feature/you/src/test/java/YouViewModelIntegrationTest.kt`

```kotlin
@Test
fun getAccountDetailsTriggersSync() = runTest {
    // Setup: Mock repositories with sync tracking
    val syncCalled = atomic(false)
    val libraryRepository = object : LibraryRepository by TestLibraryRepository() {
        override suspend fun syncFavorites(): Result<Unit> {
            syncCalled.value = true
            return Result.success(Unit)
        }
    }

    val viewModel = YouViewModel(
        authRepository = testAuthRepository,
        userRepository = testUserRepository,
        libraryRepository = libraryRepository
    )

    // Action: Get account details
    testUserRepository.setAccountDetails(testAccountDetails)
    viewModel.getAccountDetails()

    // Wait for async sync
    advanceUntilIdle()

    // Verify: Sync was called
    assertTrue(syncCalled.value)
}
```

**Why This Catches Bug #2**: Verifies that `getAccountDetails()` triggers `syncFavorites()`.

---

## Code Coverage Tools

### Current Coverage (Estimate)
- **Unit Tests**: ~40% code coverage
- **Integration Tests**: ~0% (none exist)
- **UI Tests**: ~0% (none exist)
- **Overall**: ~40%

### Recommended Tools

#### 1. **JaCoCo** (Java Code Coverage)
**Setup**:
```gradle
// app/build.gradle.kts
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // Exclude generated code
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude("**/R.class", "**/BuildConfig.*", "**/*_Factory.*")
        }
    }))
}
```

**Usage**:
```bash
./gradlew testDebugUnitTest jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

**Benefits**:
- ✅ Line and branch coverage metrics
- ✅ HTML reports highlighting uncovered code
- ✅ Can enforce minimum coverage thresholds

---

#### 2. **Kover** (Kotlin-specific coverage)
**Setup**:
```gradle
// build.gradle.kts (root)
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

kover {
    filters {
        excludes {
            classes("*Fragment", "*Activity", "*_Factory")
        }
    }
}
```

**Usage**:
```bash
./gradlew koverHtmlReport
open build/reports/kover/html/index.html
```

**Benefits**:
- ✅ Better Kotlin support than JaCoCo
- ✅ Inline code coverage in IDE
- ✅ Compose-aware coverage

---

#### 3. **SonarQube** (Advanced analysis)
**Benefits**:
- ✅ Code quality metrics
- ✅ Test coverage tracking over time
- ✅ Code smells and technical debt tracking
- ✅ Integration with CI/CD

**Setup**: Requires server (SonarCloud or self-hosted)

---

## CI/CD Integration

### GitHub Actions Workflow
```yaml
name: Test Coverage

on: [pull_request]

jobs:
  test-coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'

      - name: Run tests with coverage
        run: ./gradlew testDebugUnitTest koverHtmlReport

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./build/reports/kover/report.xml

      - name: Comment PR with coverage
        uses: codecov/codecov-action@v3
        with:
          token: ${{ secrets.CODECOV_TOKEN }}
```

**Benefits**:
- ✅ Automatic coverage reports on PRs
- ✅ Coverage trends over time
- ✅ Prevents coverage regressions

---

## Summary: What Would Have Prevented These Bugs

### Bug #1 (Guest Library Access)
**Would Have Been Caught By**:
- ✅ Compose UI test verifying LibrarySection rendered for guest users
- ✅ Screenshot test showing guest mode UI
- ✅ E2E test simulating guest workflow

**Not Caught By**:
- ❌ Unit tests (test logic, not UI rendering)
- ❌ DAO tests (test database, not UI)

---

### Bug #2 (Sync on Login)
**Would Have Been Caught By**:
- ✅ Integration test verifying `getAccountDetails()` calls `syncFavorites()`
- ✅ E2E test verifying TMDB favorites appear after login
- ✅ Unit test with spy/mock tracking sync calls

**Not Caught By**:
- ❌ Pure ViewModel unit tests (didn't test login → sync flow)
- ❌ Repository unit tests (didn't test ViewModel usage)

---

## Lessons Learned

### 1. Unit Tests Are Necessary But Not Sufficient
**Lesson**: Unit tests verify components work in isolation, but not that they work together or are actually used.

**Action**: Add integration and UI tests for critical paths.

---

### 2. Test Doubles Can Hide Integration Issues
**Lesson**: `TestLibraryRepository` worked instantly; real implementation has async/timing issues.

**Action**: Use real implementations in integration tests when possible.

---

### 3. UI Tests Are Critical for User-Facing Features
**Lesson**: No test verified the UI actually rendered the LibrarySection for guest users.

**Action**: Add Compose UI tests for all major screens and user journeys.

---

### 4. Test Coverage Metrics Don't Show Integration Gaps
**Lesson**: 100% code coverage doesn't mean integration works or UI renders correctly.

**Action**: Track different types of coverage: unit, integration, UI, E2E.

---

## Immediate Next Steps

1. **Install Kover** for coverage tracking
   ```bash
   ./gradlew koverHtmlReport
   ```

2. **Add Compose UI tests** for YouScreen (2 hours)

3. **Add integration test** for login sync (4 hours)

4. **Set up CI coverage reporting** (1 hour)

5. **Document testing strategy** in CLAUDE.md

**Total Effort**: 1 day

**ROI**: Would have prevented both critical bugs + prevents future regressions

---

**Report Generated**: 2026-02-02
**Next Review**: After implementing Priority 1 & 2 tests
