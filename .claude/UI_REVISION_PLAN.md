# UI Revision Plan - MoviesAndBeyond App

## Design System Definition

### Spacing Tokens (to create in `core/ui/theme/Spacing.kt`)
```kotlin
object Spacing {
    val xxs = 2.dp   // minimal spacing (e.g., text gaps)
    val xs = 4.dp    // small gaps, section top padding
    val sm = 8.dp    // standard item spacing, horizontal padding
    val md = 12.dp   // section internal spacing
    val lg = 16.dp   // screen edge padding (NOT USED - standardize on sm)
    val xl = 24.dp   // section-to-section spacing
    val xxl = 32.dp  // large spacing

    // Semantic aliases
    val screenPadding = sm        // 8.dp - horizontal screen edges
    val sectionSpacing = xl       // 24.dp - between content sections
    val itemSpacing = sm          // 8.dp - between list items
    val headerSpacing = md        // 12.dp - header to content
}
```

### Size Tokens (to create in `core/ui/theme/Dimens.kt`)
```kotlin
object Dimens {
    // Cards
    val cardWidth = 120.dp
    val cardHeight = 160.dp
    val featuredCardHeight = 200.dp

    // Loading
    val loadingIndicatorWidth = 110.dp

    // Profile
    val avatarSize = 64.dp
    val iconSize = 48.dp

    // Touch targets
    val minTouchTarget = 48.dp
}
```

---

## Implementation Sprints

### Sprint 1: Design System Foundation
**Goal**: Create centralized design tokens

**Tasks**:
1. Create `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/theme/Spacing.kt`
2. Create `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/theme/Dimens.kt`

**Effort**: S (30 minutes)

---

### Sprint 2: Fix Critical Padding Issues
**Goal**: Unify padding across Movies and TV feed screens

**Tasks**:
1. **Fix Movies FeedScreen** - Wrap with Scaffold and use consistent padding
   - Change `PaddingValues(16.dp)` → `PaddingValues(top = Spacing.xs, bottom = Spacing.sm)`
   - Add Scaffold wrapper
   - Remove manual WindowInsets handling

2. **Verify TV FeedScreen** - Ensure it uses same padding tokens

3. **Fix LibraryItemsScreen** - Change `10.dp` → `Spacing.sm` (8.dp)

4. **Replace all hardcoded padding**:
   - Remove local `horizontalPadding = 8.dp` definitions
   - Use `Spacing.screenPadding` instead

**Files to modify**:
- `feature/movies/FeedScreen.kt`
- `feature/movies/ItemsScreen.kt`
- `feature/tv/FeedScreen.kt`
- `feature/tv/ItemsScreen.kt`
- `feature/search/SearchScreen.kt`
- `feature/details/DetailsScreen.kt`
- `feature/you/library_items/LibraryItemsScreen.kt`
- `core/ui/LazyRow.kt`

**Effort**: M (1-2 hours)

---

### Sprint 3: Add Shimmer Loading Component
**Goal**: Replace CircularProgressIndicator with shimmer loading cards

**Tasks**:
1. Create `ShimmerCard.kt` - Single card shimmer placeholder
2. Create `ShimmerRow.kt` - Row of shimmer cards for horizontal lists
3. Create `ShimmerGrid.kt` - Grid of shimmer cards for vertical grids
4. Update `LazyRowContentSection` to use ShimmerRow when loading
5. Update feed screens to show shimmer on initial load

**New files**:
- `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/loading/ShimmerCard.kt`

**Effort**: M (1-2 hours)

---

### Sprint 4: Add Featured Carousel Component
**Goal**: Add carousel for featured content (Now Playing / Airing Today)

**Tasks**:
1. Create `FeaturedCarousel.kt` using HorizontalPager
   - Larger cards (200.dp height)
   - Page indicators
   - Auto-scroll option
   - Smooth snapping

2. Update Movies FeedScreen:
   - Replace first section (Now Playing) with carousel

3. Update TV FeedScreen:
   - Replace first section (Airing Today) with carousel

**New files**:
- `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/carousel/FeaturedCarousel.kt`

**Effort**: L (2-3 hours)

---

### Sprint 5: UX Enhancements
**Goal**: Polish and additional UX improvements

**Tasks**:
1. Add empty state component
2. Add pull-to-refresh to Movies and TV feed screens
3. Unify grid columns (decide on 2 or 3)
4. Add list item animations using `animateItem()`
5. Fix hardcoded font size in YouScreen (18.sp → MaterialTheme.typography)

**Effort**: M (1-2 hours)

---

## File Structure After Revision

```
core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/
├── theme/
│   ├── Spacing.kt          # NEW - Spacing tokens
│   └── Dimens.kt           # NEW - Size tokens
├── loading/
│   └── ShimmerCard.kt      # NEW - Shimmer loading
├── carousel/
│   └── FeaturedCarousel.kt # NEW - Featured carousel
├── Card.kt                 # EXISTING - MediaItemCard
├── LazyRow.kt              # MODIFY - Use Spacing tokens
├── LazyVerticalGrid.kt     # MODIFY - Use Spacing tokens
├── ContentSectionHeader.kt # EXISTING
└── ...
```

---

## Execution Order

1. **Sprint 1**: Design System Foundation (prerequisite for all other sprints)
2. **Sprint 2**: Fix Critical Padding Issues (highest priority)
3. **Sprint 3**: Add Shimmer Loading
4. **Sprint 4**: Add Featured Carousel
5. **Sprint 5**: UX Enhancements

Each sprint must pass `./gradlew clean build` before proceeding.

---

## Risk Mitigation

### Breaking Changes
- Padding changes may affect scroll positions
- Test on multiple screen sizes

### Performance
- Shimmer animations should use `graphicsLayer` for 60fps
- Carousel auto-scroll should pause when not visible

### Backward Compatibility
- Keep existing component APIs stable
- Add new parameters with defaults
