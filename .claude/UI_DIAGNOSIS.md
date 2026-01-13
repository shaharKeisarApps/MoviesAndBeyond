# UI/UX Diagnosis Report - MoviesAndBeyond App

## Executive Summary
- **Total screens audited**: 11
- **Critical inconsistencies**: 4
- **Medium inconsistencies**: 6
- **Components already shared**: 5 (MediaItemCard, LazyRowContentSection, LazyVerticalContentGrid, ContentSectionHeader, TopAppBarWithBackButton)
- **Design token gaps**: No centralized spacing tokens
- **UX improvements identified**: 5

---

## Critical Inconsistency Findings

### 🔴 CRITICAL #1: Movies vs TV Feed Screen Padding

#### Movies FeedScreen (`feature/movies/FeedScreen.kt:86-89`)
```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    modifier = modifier.fillMaxSize().padding(WindowInsets.safeDrawing.asPaddingValues()),
    verticalArrangement = Arrangement.spacedBy(24.dp))
```
- Uses **16.dp** content padding on all sides
- Uses `WindowInsets.safeDrawing.asPaddingValues()` directly
- Does **NOT** use Scaffold

#### TV FeedScreen (`feature/tv/FeedScreen.kt:83-87`)
```kotlin
Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarState) }) { paddingValues ->
    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
        modifier = Modifier.fillMaxWidth().padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(24.dp))
```
- Uses **4.dp top, 8.dp bottom** - completely different!
- Uses Scaffold with paddingValues
- Different padding approach

**Impact**: Visual inconsistency between Movies and TV tabs - noticeable jump when switching tabs.

---

### 🔴 CRITICAL #2: `horizontalPadding` Defined Locally Multiple Times

The same `horizontalPadding = 8.dp` is defined locally in 5 different files:

| File | Line | Value |
|------|------|-------|
| `feature/movies/FeedScreen.kt` | 38 | 8.dp |
| `feature/movies/ItemsScreen.kt` | 32 | 8.dp |
| `feature/tv/FeedScreen.kt` | 37 | 8.dp |
| `feature/search/SearchScreen.kt` | 35 | 8.dp |
| `feature/details/DetailsScreen.kt` | 48 | 8.dp |

**AND** LibraryItemsScreen uses a different value inline:
- `feature/you/library_items/LibraryItemsScreen.kt:164`: `PaddingValues(horizontal = 10.dp)` - **Inconsistent!**

**Impact**: Maintenance nightmare and visual inconsistency.

---

### 🔴 CRITICAL #3: No Centralized Spacing Tokens

**Current State**: No `Spacing.kt` or `Dimens.kt` file exists.

**Hardcoded values found throughout codebase**:
- 2.dp (DetailsScreen)
- 4.dp (TV FeedScreen, LibraryItemsScreen, YouScreen)
- 6.dp (DetailsScreen)
- 8.dp (Movies FeedScreen, SearchScreen, LazyRow, YouScreen)
- 10.dp (SearchBar, YouScreen, LibraryItemsScreen)
- 12.dp (YouScreen, LazyRow)
- 16.dp (Movies FeedScreen)
- 24.dp (FeedScreens, YouScreen)
- 50.dp (DetailsScreen bottom sheet)

**Impact**: No design system, inconsistent spacing, hard to maintain.

---

### 🔴 CRITICAL #4: Scaffold Usage Inconsistency

| Screen | Uses Scaffold | Handles Insets |
|--------|---------------|----------------|
| Movies FeedScreen | ❌ No | WindowInsets manually |
| TV FeedScreen | ✅ Yes | Via paddingValues |
| SearchScreen | ✅ Yes | Via paddingValues |
| YouScreen | ✅ Yes | Via paddingValues |
| Items Screens | ✅ Yes | Via paddingValues |
| LibraryItemsScreen | ✅ Yes | Via paddingValues |
| DetailsScreen | ✅ Yes (BottomSheetScaffold) | Via paddingValues |

**Impact**: Inconsistent inset handling, potential overlapping with system UI.

---

## Medium Inconsistency Findings

### 🟠 MEDIUM #1: Section Spacing Between Header and Content

**LazyRow.kt:63**:
```kotlin
Column(verticalArrangement = Arrangement.spacedBy(12.dp))
```

**FeedScreens:89**:
```kotlin
verticalArrangement = Arrangement.spacedBy(24.dp)
```

The 12.dp is hardcoded in the shared component but 24.dp is used between sections.

---

### 🟠 MEDIUM #2: Loading State Implementation Varies

| Screen | Loading Implementation |
|--------|----------------------|
| FeedScreens | CircularProgressIndicator in Box |
| ItemsScreens | CircularProgressIndicator in Box |
| YouScreen | CircularProgressIndicator centered |
| DetailsScreen | CircularProgressIndicator with semantics |
| LazyRow | CircularProgressIndicator centered |

No shimmer loading anywhere - all use basic CircularProgressIndicator.

---

### 🟠 MEDIUM #3: Empty State Handling

| Screen | Has Empty State |
|--------|----------------|
| LibraryItemsScreen | ✅ Yes - "No items" text |
| SearchScreen | ❌ No - Just empty space |
| FeedScreens | ❌ No - Just shows loading |
| YouScreen | ✅ Partial - Shows reload button |

---

### 🟠 MEDIUM #4: Typography Inconsistencies

**Section Headers**:
- ContentSectionHeader: `MaterialTheme.typography.titleLarge`
- LibrarySection (YouScreen): `MaterialTheme.typography.titleLarge`

**Item Titles**:
- SearchSuggestionItem: No explicit style (defaults)
- LibraryItemOption: `fontSize = 18.sp` (hardcoded!)

---

### 🟠 MEDIUM #5: Card Sizes Inconsistent

| Location | Card Size |
|----------|-----------|
| MediaItemCard | 120.dp x 160.dp |
| FeedScreen row height | 160.dp (matches) |
| ItemsScreen card height | 160.dp (matches) |
| PersonDetailsContent | 140.dp x 200.dp (different!) |

---

### 🟠 MEDIUM #6: Grid Column Configuration

| Screen | Grid Columns |
|--------|-------------|
| SearchScreen | GridCells.Fixed(2) |
| LazyVerticalContentGrid | GridCells.Fixed(3) |

Different column counts for similar content displays.

---

## Shared Components Already Implemented

### ✅ Well-Designed Components

1. **MediaItemCard** (`core/ui/Card.kt`)
   - Supports shared element transitions
   - Expressive press animations
   - Configurable via `sharedElementKey`

2. **LazyRowContentSection** (`core/ui/LazyRow.kt`)
   - Paging support
   - Loading states
   - Section header slot

3. **ContentSectionHeader** (`core/ui/ContentSectionHeader.kt`)
   - Expressive arrow animation
   - Consistent styling

4. **TopAppBarWithBackButton** (`core/ui/TopAppBar.kt`)
   - Reused across all detail screens

5. **LazyVerticalContentGrid** (`core/ui/LazyVerticalGrid.kt`)
   - Paging support
   - Consistent grid layout

---

## Missing UX Patterns

### 1. Featured Content Carousel
**Opportunity**:
- Add HorizontalPager carousel for "Now Playing" or trending content
- Auto-scroll with indicators
- Larger cards for featured items

**Screens that would benefit**:
- Movies FeedScreen (Now Playing section)
- TV FeedScreen (Airing Today section)

### 2. Shimmer Loading States
**Current**: CircularProgressIndicator everywhere
**Should be**: Shimmer placeholder cards matching content shape

**Screens affected**: All list/grid screens

### 3. Unified Empty States
**Current**: Inconsistent or missing
**Should be**: Friendly illustration + message + action button

**Screens affected**: Search, Library, potential error states

### 4. Pull-to-Refresh
**Current**: Only on YouScreen
**Should be**: All feed screens

**Screens that need it**: Movies FeedScreen, TV FeedScreen, Search

### 5. List Item Animations
**Current**: No entry animations
**Should be**: `animateItem()` on LazyColumn/LazyRow items

---

## Screen-by-Screen Summary

### Movies FeedScreen
| Issue | Severity |
|-------|----------|
| Uses 16.dp padding vs TV's 4.dp/8.dp | 🔴 Critical |
| No Scaffold wrapper | 🔴 Critical |
| No shimmer loading | 🟠 Medium |
| No featured carousel | 🟡 Low |

### TV FeedScreen
| Issue | Severity |
|-------|----------|
| Different padding than Movies | 🔴 Critical |
| No shimmer loading | 🟠 Medium |
| No featured carousel | 🟡 Low |

### SearchScreen
| Issue | Severity |
|-------|----------|
| 2-column grid vs 3-column elsewhere | 🟠 Medium |
| No empty state | 🟠 Medium |
| Grid spacing different (8.dp horizontal, 4.dp vertical) | 🟡 Low |

### YouScreen
| Issue | Severity |
|-------|----------|
| Hardcoded spacing (10.dp, 4.dp, 12.dp, 8.dp) | 🟠 Medium |
| 18.sp hardcoded font size | 🟠 Medium |
| Good: Has pull-to-refresh | ✅ |

### LibraryItemsScreen
| Issue | Severity |
|-------|----------|
| Uses 10.dp horizontal padding (others use 8.dp) | 🔴 Critical |
| Good: Has empty state | ✅ |
| Good: Uses HorizontalPager for tabs | ✅ |

### DetailsScreen
| Issue | Severity |
|-------|----------|
| Good: Uses shared horizontalPadding constant | ✅ |
| 50.dp bottom sheet spacer hardcoded | 🟡 Low |

### ItemsScreens (Movies/TV)
| Issue | Severity |
|-------|----------|
| Uses correct 8.dp horizontal padding | ✅ |
| Good: Consistent grid layout | ✅ |

---

## Design Token Gaps

### Spacing (Needs Creation)
```kotlin
object Spacing {
    val xxs = 2.dp   // minimal spacing
    val xs = 4.dp    // small gaps
    val sm = 8.dp    // standard item spacing
    val md = 12.dp   // section internal spacing
    val lg = 16.dp   // screen padding
    val xl = 24.dp   // section spacing
    val xxl = 32.dp  // large spacing
}
```

### Sizes (Needs Creation)
```kotlin
object Dimens {
    val cardWidth = 120.dp
    val cardHeight = 160.dp
    val loadingIndicatorSize = 110.dp
    val avatarSize = 64.dp
    val iconSize = 48.dp
}
```

---

## Priority Action Items

| Priority | Item | Effort |
|----------|------|--------|
| P0 | Fix Movies FeedScreen padding to match TV | S |
| P0 | Create Spacing.kt with centralized tokens | S |
| P0 | Fix LibraryItemsScreen horizontal padding (10.dp → 8.dp) | XS |
| P1 | Replace all hardcoded padding with Spacing tokens | M |
| P1 | Add shimmer loading components | M |
| P1 | Unify grid column configuration | S |
| P2 | Add featured content carousel | L |
| P2 | Add unified empty state component | M |
| P2 | Add pull-to-refresh to feed screens | M |
| P3 | Add list item animations | S |
