# UI/UX Transformation Plan: Material 3 Expressive

> Version: 1.0
> Date: January 2026
> App: MoviesAndBeyond

## Vision

Transform MoviesAndBeyond into a **delightful, butter-smooth experience** using Material 3 Expressive design language with seamless shared element transitions, creating a premium feel that users love.

## Key Pillars

| Pillar | Description | Impact |
|--------|-------------|--------|
| **Edge-to-Edge** | Immersive full-screen experience | ✅ Already done |
| **Predictive Back** | Gesture-driven navigation with previews | Critical |
| **Shared Elements** | Seamless content continuity | Critical |
| **Expressive Motion** | Delightful micro-interactions | High |
| **Butter Smooth** | 60fps throughout | Critical |

---

## Sprint 1: Foundation (Predictive Back + Navigation Setup)

### 1.1 Enable Predictive Back

**Priority**: P0 (Critical)
**Effort**: Medium
**Files**: `AndroidManifest.xml`, Navigation setup

#### Implementation Steps

1. **Update AndroidManifest.xml**
```xml
<application
    android:enableOnBackInvokedCallback="true"
    ... >
```

2. **Update activity-compose dependency** (if needed)
```kotlin
// libs.versions.toml
activity-compose = "1.9.0"  // or latest
```

3. **Navigation already uses Navigation 3** - predictive back should work automatically with proper setup

#### Verification
- [ ] Back gesture shows preview animation
- [ ] Back-to-home animation works
- [ ] Cross-activity transitions smooth

---

### 1.2 Setup SharedTransitionLayout

**Priority**: P0 (Critical)
**Effort**: Medium
**Files**: `app/ui/navigation/MoviesAndBeyondNav3.kt` or equivalent

#### Implementation Steps

1. **Wrap navigation with SharedTransitionLayout**
```kotlin
@Composable
fun MoviesAndBeyondApp() {
    SharedTransitionLayout {
        // Provide SharedTransitionScope via CompositionLocal
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            MoviesAndBeyondNavigation()
        }
    }
}
```

2. **Create CompositionLocals**
```kotlin
// core/ui/SharedElementScopes.kt
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
```

3. **Propagate AnimatedVisibilityScope in NavHost composables**
```kotlin
composable<DetailsRoute> { entry ->
    CompositionLocalProvider(
        LocalAnimatedVisibilityScope provides this@composable
    ) {
        DetailsScreen(...)
    }
}
```

#### Verification
- [ ] SharedTransitionScope available throughout app
- [ ] AnimatedVisibilityScope available in each screen
- [ ] No runtime errors

---

## Sprint 2: Shared Element Transitions

### 2.1 Define Shared Element Keys

**Priority**: P0 (Critical)
**Effort**: Small
**Files**: `core/ui/SharedElementKeys.kt`

#### Implementation

```kotlin
// core/ui/SharedElementKeys.kt
package com.MoviesAndBeyond.core.ui

/**
 * Type-safe shared element keys to prevent key mismatches
 */
data class MediaSharedElementKey(
    val mediaId: Long,
    val mediaType: MediaType,
    val origin: String,  // "movies_feed", "tv_feed", "search", "library"
    val elementType: SharedElementType
)

enum class MediaType {
    Movie,
    TvShow,
    Person
}

enum class SharedElementType {
    Image,
    Title,
    Card,
    Rating,
    Backdrop
}

// Helper extension
fun MediaSharedElementKey.toKey(): Any = this
```

---

### 2.2 Movies Feed → Details Transition

**Priority**: P0 (Critical)
**Effort**: Large
**Files**:
- `feature/movies/ui/FeedScreen.kt`
- `feature/movies/ui/components/MovieCard.kt` (or equivalent)
- `feature/details/ui/DetailsScreen.kt`

#### Source Screen (Feed)

```kotlin
@Composable
fun MovieCard(
    movie: Movie,
    origin: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Card(
                modifier = modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(
                            key = MediaSharedElementKey(
                                mediaId = movie.id,
                                mediaType = MediaType.Movie,
                                origin = origin,
                                elementType = SharedElementType.Card
                            )
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds
                    )
                    .clickable { onClick() }
            ) {
                TmdbListImage(
                    imageUrl = movie.posterPath,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = MediaSharedElementKey(
                                    mediaId = movie.id,
                                    mediaType = MediaType.Movie,
                                    origin = origin,
                                    elementType = SharedElementType.Image
                                )
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .aspectRatio(2f / 3f)
                )
            }
        }
    } else {
        // Fallback without shared elements
        Card(
            modifier = modifier.clickable { onClick() }
        ) {
            TmdbListImage(
                imageUrl = movie.posterPath,
                modifier = Modifier.aspectRatio(2f / 3f)
            )
        }
    }
}
```

#### Destination Screen (Details)

```kotlin
@Composable
fun DetailsScreen(
    mediaId: Long,
    mediaType: MediaType,
    origin: String,
    // ...
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Column {
                // Shared backdrop/poster
                TmdbImage(
                    imageUrl = media.backdropPath ?: media.posterPath,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(
                                key = MediaSharedElementKey(
                                    mediaId = mediaId,
                                    mediaType = mediaType,
                                    origin = origin,
                                    elementType = SharedElementType.Image
                                )
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )

                // Non-shared content with enter animation
                Column(
                    modifier = Modifier
                        .animateEnterExit(
                            enter = fadeIn(tween(300, delayMillis = 150)) +
                                    slideInVertically(tween(300, delayMillis = 150)) { it / 4 },
                            exit = fadeOut(tween(150))
                        )
                ) {
                    // Title, rating, description, etc.
                }
            }
        }
    }
}
```

---

### 2.3 TV Feed → Details Transition

**Priority**: P0 (Critical)
**Effort**: Medium
**Files**: Same pattern as Movies

#### Implementation
Same pattern as 2.2, using `MediaType.TvShow`

---

### 2.4 Search → Details Transition

**Priority**: P0 (Critical)
**Effort**: Medium
**Files**: `feature/search/ui/SearchScreen.kt`, `feature/search/ui/components/SearchResultItem.kt`

#### Implementation
Same pattern with `origin = "search"`

---

### 2.5 Library → Details Transition

**Priority**: P1 (High)
**Effort**: Medium
**Files**: `feature/you/ui/LibraryItemsScreen.kt`

#### Implementation
Same pattern with `origin = "library"`

---

### 2.6 Pass Origin Through Navigation

**Priority**: P0 (Critical)
**Effort**: Small
**Files**: Route definitions

```kotlin
// Routes.kt
@Serializable
data class DetailsRoute(
    val mediaId: Long,
    val mediaType: String,
    val origin: String  // Add origin parameter
)

// Navigation call
navController.navigate(
    DetailsRoute(
        mediaId = movie.id,
        mediaType = "movie",
        origin = "movies_feed"
    )
)
```

---

## Sprint 3: M3 Expressive Components

### 3.1 Upgrade to M3 Expressive Alpha

**Priority**: P1 (High)
**Effort**: Small
**Files**: `libs.versions.toml`, `build.gradle.kts`

```kotlin
// libs.versions.toml
[versions]
material3 = "1.5.0-alpha11"

// Note: This is alpha - consider stability trade-offs
```

---

### 3.2 Expressive List Items

**Priority**: P1 (High)
**Effort**: Medium
**Files**: All list item components

#### Implementation Pattern

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveMediaCard(
    media: Media,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(),
        label = "card_elevation"
    )

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
    ) {
        // Content
    }
}
```

---

### 3.3 Expressive FAB for Favorites

**Priority**: P2 (Medium)
**Effort**: Medium
**Files**: `feature/details/ui/DetailsScreen.kt`

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FavoritesFab(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fab_scale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isFavorite) 0f else -30f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "icon_rotation"
    )

    FloatingActionButton(
        onClick = onToggle,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            modifier = Modifier.graphicsLayer {
                rotationZ = iconRotation
            }
        )
    }
}
```

---

### 3.4 Celebration Animation

**Priority**: P2 (Medium)
**Effort**: Medium
**Files**: `core/ui/CelebrationAnimation.kt`

```kotlin
@Composable
fun FavoriteCelebration(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
    }
}
```

---

## Sprint 4: Micro-Interactions & Animations

### 4.1 List Item Appear Animations

**Priority**: P1 (High)
**Effort**: Medium
**Files**: All LazyColumn/LazyRow implementations

```kotlin
LazyColumn {
    itemsIndexed(
        items = movies,
        key = { _, movie -> movie.id }
    ) { index, movie ->
        val animatedModifier = Modifier.animateItem(
            fadeInSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50  // Staggered
            ),
            fadeOutSpec = tween(durationMillis = 150)
        )

        MovieCard(
            movie = movie,
            modifier = animatedModifier
        )
    }
}
```

---

### 4.2 Content Stagger Animation (Details Screen)

**Priority**: P2 (Medium)
**Effort**: Medium
**Files**: `feature/details/ui/DetailsScreen.kt`

```kotlin
@Composable
fun StaggeredContent(
    isVisible: Boolean,
    content: @Composable () -> Unit,
    delayMillis: Int
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(300, delayMillis = delayMillis)
        ) + slideInVertically(
            animationSpec = tween(300, delayMillis = delayMillis)
        ) { it / 4 },
        exit = fadeOut(tween(150))
    ) {
        content()
    }
}

// Usage
Column {
    StaggeredContent(isVisible = true, delayMillis = 0) { Title() }
    StaggeredContent(isVisible = true, delayMillis = 50) { Rating() }
    StaggeredContent(isVisible = true, delayMillis = 100) { Genres() }
    StaggeredContent(isVisible = true, delayMillis = 150) { Description() }
}
```

---

### 4.3 Search Bar Animation

**Priority**: P2 (Medium)
**Effort**: Medium
**Files**: `feature/search/ui/SearchScreen.kt`

Consider using M3 Expressive `ExpandedFullScreenContainedSearchBar` when available.

---

### 4.4 Tab Indicator Animation

**Priority**: P3 (Low)
**Effort**: Small
**Files**: `feature/you/ui/LibraryItemsScreen.kt`

```kotlin
TabRow(
    selectedTabIndex = selectedTab,
    indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier
                .tabIndicatorOffset(tabPositions[selectedTab])
                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
            color = MaterialTheme.colorScheme.primary
        )
    }
) {
    // Tabs
}
```

---

## Sprint 5: Polish & Delight

### 5.1 Bottom Bar Hide on Scroll

**Priority**: P3 (Low)
**Effort**: Small
**Files**: `app/ui/MoviesAndBeyondApp.kt`

```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

// Or custom scroll detection
val listState = rememberLazyListState()
val isScrollingUp by remember {
    derivedStateOf {
        listState.firstVisibleItemIndex > 0 &&
        listState.firstVisibleItemScrollOffset > 0
    }
}

AnimatedVisibility(
    visible = !isScrollingUp,
    enter = slideInVertically { it },
    exit = slideOutVertically { it }
) {
    BottomNavigationBar()
}
```

---

### 5.2 Haptic Feedback

**Priority**: P3 (Low)
**Effort**: Small
**Files**: Interactive components

```kotlin
val haptic = LocalHapticFeedback.current

Button(
    onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onAction()
    }
)
```

---

## Implementation Priority Matrix

| Improvement | Impact | Effort | Priority | Sprint |
|-------------|--------|--------|----------|--------|
| Predictive Back | High | M | P0 | 1 |
| SharedTransitionLayout | Critical | M | P0 | 1 |
| Shared Element Keys | Critical | S | P0 | 2 |
| Movies→Details Shared | Critical | L | P0 | 2 |
| TV→Details Shared | Critical | M | P0 | 2 |
| Search→Details Shared | Critical | M | P0 | 2 |
| Library→Details Shared | High | M | P1 | 2 |
| M3 Expressive Upgrade | High | S | P1 | 3 |
| Expressive List Items | High | M | P1 | 3 |
| List Item Appear Anim | High | M | P1 | 4 |
| Expressive FAB | Medium | M | P2 | 3 |
| Celebration Animation | Medium | M | P2 | 3 |
| Content Stagger Anim | Medium | M | P2 | 4 |
| Search Bar Animation | Medium | M | P2 | 4 |
| Tab Indicator | Low | S | P3 | 5 |
| Bottom Bar Hide | Low | S | P3 | 5 |
| Haptics | Low | S | P3 | 5 |

---

## Success Metrics

### Visual Quality
- [ ] Consistent M3 Expressive look throughout
- [ ] All screen transitions have shared elements
- [ ] Delightful micro-interactions on interactive elements
- [ ] Professional polish comparable to top-tier apps

### Performance
- [ ] 60fps during all animations
- [ ] No jank during list scrolling
- [ ] Smooth shared element transitions
- [ ] < 16ms frame times consistently

### User Experience
- [ ] Predictive back feels native
- [ ] Shared elements create visual continuity
- [ ] Press feedback on all interactive elements
- [ ] Overall "premium" feel

---

## Risk Mitigation

### M3 Expressive Alpha Stability
- **Risk**: Alpha APIs may change
- **Mitigation**: Isolate expressive components, have fallbacks ready

### Shared Element Performance
- **Risk**: Complex shared elements cause jank
- **Mitigation**: Keep shared content simple, profile on low-end devices

### Navigation 3 Compatibility
- **Risk**: SharedTransitionLayout with Navigation 3 edge cases
- **Mitigation**: Thorough testing, fallback to non-shared if issues

---

## Testing Plan

### Manual Testing
1. Each screen pair with shared elements
2. Predictive back on all screens
3. Fast scrolling in all lists
4. Low-end device testing

### Automated Testing
1. Compose UI tests for animations
2. Benchmark tests for frame timing
3. Memory leak tests for navigation cycles

---

## Files to Create/Modify Summary

### New Files
- `core/ui/SharedElementKeys.kt`
- `core/ui/SharedElementScopes.kt`
- `core/ui/CelebrationAnimation.kt`
- `core/ui/ExpressiveComponents.kt`

### Modified Files
- `app/ui/navigation/MoviesAndBeyondNav3.kt`
- `feature/movies/ui/FeedScreen.kt`
- `feature/movies/ui/components/MovieCard.kt`
- `feature/tv/ui/FeedScreen.kt`
- `feature/search/ui/SearchScreen.kt`
- `feature/details/ui/DetailsScreen.kt`
- `feature/you/ui/LibraryItemsScreen.kt`
- `AndroidManifest.xml`
- `libs.versions.toml`
