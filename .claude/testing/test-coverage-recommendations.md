# Test Coverage Recommendations

**Purpose:** Guide for implementing comprehensive automated tests
**Target Coverage:** 80% code coverage, 100% critical path coverage
**Priority:** Implement P1-P2 first (Compose UI + Integration tests)

---

## 📊 Current Test Coverage Status

### Existing Tests ✅
- **Unit Tests:** ViewModel logic (with fake repositories)
- **DAO Tests:** Room database operations (ContentDaoTest.kt)
- **Build Tests:** Compilation + ktlint + detekt

### Missing Tests ❌
- **Compose UI Tests:** Screen rendering and user interactions
- **Integration Tests:** ViewModel + Repository + Database
- **Screenshot Tests:** Visual regression detection
- **E2E Tests:** Full user flows
- **Edge-to-Edge Tests:** System UI configuration

---

## Priority 1: Compose UI Tests (High Impact)

### Rationale
- Would have caught Bug #1 (LibrarySection missing in guest mode)
- Tests actual user-facing UI, not just logic
- Fast to run (~1-2 seconds per test)
- No device/emulator needed (Robolectric)

### Implementation Plan

#### 1.1 YouScreen UI Tests

**File:** `feature/you/src/androidTest/java/com/keisardev/moviesandbeyond/feature/you/YouScreenTest.kt`

**Dependencies:**
```kotlin
// build.gradle.kts (:feature:you)
dependencies {
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
}
```

**Example Tests:**

```kotlin
package com.keisardev.moviesandbeyond.feature.you

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.model.user.UserSettings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YouScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUserSettings = UserSettings(
        theme = "Auto",
        darkMode = false,
        includeAdult = false,
        useLocalOnly = false
    )

    @Test
    fun guestMode_showsLibrarySection() {
        // GIVEN: User not logged in
        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = testUserSettings,
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}
            )
        }

        // THEN: LibrarySection should be visible
        composeTestRule
            .onNodeWithText("Your Library")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Favorites")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Watchlist")
            .assertIsDisplayed()
    }

    @Test
    fun guestMode_favoritesClickable() {
        // GIVEN: User not logged in
        var clickedItem: String? = null

        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = testUserSettings,
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = { item -> clickedItem = item },
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}
            )
        }

        // WHEN: User taps Favorites
        composeTestRule
            .onNodeWithText("Favorites")
            .performClick()

        // THEN: Callback triggered
        assert(clickedItem == "FAVORITE")
    }

    @Test
    fun authenticatedMode_showsAccountDetails() {
        // GIVEN: User logged in
        val accountDetails = AccountDetails(
            id = 123,
            username = "testuser",
            name = "Test User",
            avatarPath = "/avatar.jpg"
        )

        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(accountDetails = accountDetails),
                isLoggedIn = true,
                userSettings = testUserSettings,
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}
            )
        }

        // THEN: Account details visible
        composeTestRule
            .onNodeWithText("testuser")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Test User")
            .assertIsDisplayed()
    }

    @Test
    fun authenticatedMode_showsLibrarySection() {
        // GIVEN: User logged in
        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(
                    accountDetails = AccountDetails(
                        id = 123,
                        username = "testuser",
                        name = "Test User",
                        avatarPath = null
                    )
                ),
                isLoggedIn = true,
                userSettings = testUserSettings,
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}
            )
        }

        // THEN: LibrarySection visible for auth users too
        composeTestRule
            .onNodeWithText("Your Library")
            .assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        // GIVEN: Error state
        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(errorMessage = "Failed to load account"),
                isLoggedIn = true,
                userSettings = testUserSettings,
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}
            )
        }

        // THEN: Error message displayed
        composeTestRule
            .onNodeWithText("Failed to load account")
            .assertIsDisplayed()
    }
}
```

**Estimated Effort:** 3-4 hours
**Impact:** ✅ Would have caught Bug #1

---

#### 1.2 LibraryItemsScreen UI Tests

**File:** `feature/you/src/androidTest/java/com/keisardev/moviesandbeyond/feature/you/LibraryItemsScreenTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class LibraryItemsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun favoritesList_displaysItems() {
        val testItems = listOf(
            LibraryItem(
                id = 1,
                mediaType = "movie",
                imagePath = "/poster1.jpg",
                name = "Test Movie"
            )
        )

        composeTestRule.setContent {
            LibraryItemsScreen(
                libraryItemType = LibraryItemType.FAVORITE,
                movieItems = testItems,
                tvItems = emptyList(),
                onBackClick = {},
                navigateToDetails = {}
            )
        }

        composeTestRule
            .onNodeWithText("Test Movie")
            .assertIsDisplayed()
    }

    @Test
    fun emptyList_showsEmptyState() {
        composeTestRule.setContent {
            LibraryItemsScreen(
                libraryItemType = LibraryItemType.FAVORITE,
                movieItems = emptyList(),
                tvItems = emptyList(),
                onBackClick = {},
                navigateToDetails = {}
            )
        }

        composeTestRule
            .onNodeWithText("No favorites yet")
            .assertIsDisplayed()
    }
}
```

**Estimated Effort:** 2-3 hours

---

### Total P1 Effort: ~6-7 hours
### Total P1 Tests: 7-8 tests
### Impact: Would catch UI rendering bugs, navigation issues

---

## Priority 2: Integration Tests (Medium-High Impact)

### Rationale
- Would have caught Bug #2 (sync not triggering after login)
- Tests real integration between ViewModel, Repository, and Database
- Can use fake API responses
- Moderate speed (~2-5 seconds per test)

### Implementation Plan

#### 2.1 YouViewModel Integration Tests

**File:** `feature/you/src/androidTest/java/com/keisardev/moviesandbeyond/feature/you/YouViewModelIntegrationTest.kt`

**Dependencies:**
```kotlin
// build.gradle.kts (:feature:you)
dependencies {
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("app.cash.turbine:turbine:1.0.0")
}
```

**Example Tests:**

```kotlin
@RunWith(AndroidJUnit4::class)
class YouViewModelIntegrationTest {

    private lateinit var database: MoviesAndBeyondDatabase
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userRepository: FakeUserRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MoviesAndBeyondDatabase::class.java
        ).build()

        libraryRepository = LibraryRepositoryImpl(
            favoriteContentDao = database.favoriteContentDao(),
            watchlistContentDao = database.watchlistContentDao(),
            tmdbApi = FakeTmdbApi(),
            accountDetailsDao = database.accountDetailsDao()
        )

        authRepository = FakeAuthRepository()
        userRepository = FakeUserRepository()
    }

    @Test
    fun login_triggersImmediateSync() = runTest {
        // GIVEN: ViewModel initialized
        val viewModel = YouViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            libraryRepository = libraryRepository
        )

        // WHEN: User logs in
        authRepository.setLoggedIn(true)
        advanceUntilIdle()

        // Simulate account details loaded
        viewModel.getAccountDetails()
        advanceUntilIdle()

        // THEN: Favorites synced
        val favorites = database.favoriteContentDao()
            .getFavoriteMovies()
            .first()

        assertTrue(favorites.isNotEmpty())
        assertEquals(SyncStatus.SYNCED, favorites[0].syncStatus)
    }

    @Test
    fun guestMode_localFavoritesPersist() = runTest {
        // GIVEN: Guest user
        authRepository.setLoggedIn(false)

        // WHEN: User adds favorite locally
        val testItem = FavoriteContentEntity(
            mediaId = 123,
            mediaType = "movie",
            imagePath = "/test.jpg",
            name = "Test Movie",
            syncStatus = SyncStatus.LOCAL_ONLY
        )
        database.favoriteContentDao().insertFavoriteItem(testItem)

        // THEN: Favorite visible in repository
        val favorites = libraryRepository.favoriteMovies.first()
        assertTrue(favorites.any { it.id == 123 })
    }

    @After
    fun tearDown() {
        database.close()
    }
}
```

**Estimated Effort:** 4-5 hours
**Impact:** ✅ Would have caught Bug #2

---

### Total P2 Effort: ~4-5 hours
### Total P2 Tests: 3-4 tests
### Impact: Would catch sync issues, data flow bugs

---

## Priority 3: Screenshot Tests (Medium Impact)

### Rationale
- Visual regression detection
- Catches layout/styling issues
- Fast to run
- Great for CI/CD

### Implementation Plan

#### 3.1 Paparazzi Setup

**Dependencies:**
```kotlin
// build.gradle.kts (root)
plugins {
    id("app.cash.paparazzi") version "1.3.1" apply false
}

// build.gradle.kts (:feature:you)
plugins {
    id("app.cash.paparazzi")
}
```

**Example Tests:**

```kotlin
class YouScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.DayNight"
    )

    @Test
    fun guestMode_screenshot() {
        paparazzi.snapshot {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = testUserSettings,
                // ... callbacks
            )
        }
    }

    @Test
    fun authenticatedMode_screenshot() {
        paparazzi.snapshot {
            YouScreen(
                uiState = YouUiState(
                    accountDetails = testAccountDetails
                ),
                isLoggedIn = true,
                userSettings = testUserSettings,
                // ... callbacks
            )
        }
    }
}
```

**Estimated Effort:** 2-3 hours
**Impact:** Visual regression prevention

---

## Priority 4: Edge-to-Edge Tests (Low-Medium Impact)

### Implementation Plan

**File:** `app/src/androidTest/java/com/keisardev/moviesandbeyond/EdgeToEdgeTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class EdgeToEdgeTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun statusBar_isTransparent() {
        activityRule.scenario.onActivity { activity ->
            val statusBarColor = activity.window.statusBarColor
            assertEquals(Color.TRANSPARENT, statusBarColor)
        }
    }

    @Test
    fun navigationBar_isTransparent() {
        activityRule.scenario.onActivity { activity ->
            val navBarColor = activity.window.navigationBarColor
            assertEquals(Color.TRANSPARENT, navBarColor)
        }
    }

    @Test
    fun decorFitsSystemWindows_isFalse() {
        activityRule.scenario.onActivity { activity ->
            val decorView = activity.window.decorView
            assertFalse(decorView.fitsSystemWindows)
        }
    }
}
```

**Estimated Effort:** 1-2 hours
**Impact:** ✅ Would have caught Bug #3

---

## Priority 5: E2E Tests (Low Priority)

### Rationale
- Tests complete user flows
- Catches integration issues between features
- Slow to run (~30-60 seconds per test)
- Brittle (UI changes break tests)

### Recommendation
**Defer to future:** Implement after P1-P4 are complete

**Example Flow to Test:**
```
Login → Browse Movies → Add Favorite → Navigate to You → Verify Favorite
```

**Estimated Effort:** 8-10 hours
**Defer until:** Q2 2026

---

## 📊 Implementation Roadmap

### Phase 1: Critical Tests (Week 1-2)
- ✅ Priority 1: Compose UI Tests (6-7 hours)
- ✅ Priority 2: Integration Tests (4-5 hours)
- ✅ Build verification script (complete)
- ✅ Manual test protocol (complete)

**Total Effort:** ~11-12 hours
**Expected Coverage:** 60-70% critical paths

### Phase 2: Quality Improvements (Week 3-4)
- Priority 3: Screenshot Tests (2-3 hours)
- Priority 4: Edge-to-Edge Tests (1-2 hours)
- CI/CD integration (2 hours)

**Total Effort:** ~5-7 hours
**Expected Coverage:** 75-85% critical paths

### Phase 3: Comprehensive Coverage (Future)
- Priority 5: E2E Tests (8-10 hours)
- Performance Tests (3-4 hours)
- Accessibility Tests (2-3 hours)

**Total Effort:** ~13-17 hours
**Expected Coverage:** 90%+ critical paths

---

## 🚀 Quick Start for Developers

### To Add Compose UI Test

1. Create test file in `feature/[module]/src/androidTest/java/`
2. Add dependencies (ui-test-junit4, ui-test-manifest)
3. Use `createComposeRule()` to set up test
4. Use semantic matchers: `onNodeWithText`, `onNodeWithContentDescription`
5. Assert with `assertIsDisplayed()`, `assertExists()`, etc.
6. Run: `./gradlew :feature:[module]:connectedDebugAndroidTest`

### To Add Integration Test

1. Create test file in `src/androidTest/java/`
2. Use in-memory Room database: `Room.inMemoryDatabaseBuilder()`
3. Inject real DAOs, fake repositories
4. Use `runTest` for coroutines
5. Use Turbine for Flow assertions
6. Run: `./gradlew connectedDebugAndroidTest`

### To Add Screenshot Test

1. Add Paparazzi plugin
2. Create test in `src/test/java/`
3. Use `paparazzi.snapshot { }`
4. Run: `./gradlew recordPaparazziDebug`
5. Verify: `./gradlew verifyPaparazziDebug`

---

## 📚 Resources

### Official Documentation
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Hilt Testing](https://developer.android.com/training/dependency-injection/hilt-testing)
- [Room Testing](https://developer.android.com/training/data-storage/room/testing-db)
- [Paparazzi](https://github.com/cashapp/paparazzi)

### Code Examples
- [Now in Android](https://github.com/android/nowinandroid) - Comprehensive test examples
- [Jetsnack](https://github.com/android/compose-samples/tree/main/Jetsnack) - Compose UI tests

---

**Last Updated:** 2026-02-04
**Version:** 1.0
**Maintained By:** MoviesAndBeyond Team
