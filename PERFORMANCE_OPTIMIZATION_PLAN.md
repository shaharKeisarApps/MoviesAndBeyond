# Butter-Smooth Performance Plan

> Version: 1.0
> Date: January 2026
> App: MoviesAndBeyond

## Goal

Achieve **consistent 60fps (16ms frame budget)** throughout the app during:
- List scrolling (feeds, search results, grids)
- Shared element transitions
- Animation playback
- Image loading

---

## Measurement Strategy

### Tools

| Tool | Purpose | When to Use |
|------|---------|-------------|
| Android Studio Profiler | CPU/Memory analysis | During development |
| Compose Layout Inspector | Recomposition counts | Debug builds |
| GPU Rendering Profiler | Frame timing | Performance testing |
| Macrobenchmark | Automated metrics | CI/CD |

### Baseline Metrics to Establish

Before implementing shared elements, measure:

1. **Scroll Performance**
   - Movies feed: fps during fast scroll
   - Search results: fps during scroll
   - Details screen: fps while scrolling content

2. **Navigation Timing**
   - Cold start to first frame
   - Feed → Details transition time
   - Back navigation time

3. **Memory Baseline**
   - Idle memory usage
   - Peak memory during navigation cycle
   - Image cache hit rate

---

## Optimization Strategies

### 1. Recomposition Minimization

#### 1.1 Use Stable Types

```kotlin
// Mark data classes as stable
@Stable
data class MovieUiState(
    val movie: Movie?,
    val isLoading: Boolean,
    val error: String?
)

// Or immutable for truly immutable data
@Immutable
data class Movie(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val rating: Float
)
```

#### 1.2 Use derivedStateOf

```kotlin
// BAD: Recomposes on every scroll tick
val scrollState = rememberScrollState()
val showBackToTop = scrollState.value > 1000

// GOOD: Only recomposes when boolean changes
val showBackToTop by remember {
    derivedStateOf { scrollState.value > 1000 }
}
```

#### 1.3 Stabilize Lambdas

```kotlin
// BAD: Lambda recreated on each recomposition
LazyColumn {
    items(movies) { movie ->
        MovieCard(
            movie = movie,
            onClick = { onMovieClick(movie.id) }  // New lambda each time
        )
    }
}

// GOOD: Stable lambda
LazyColumn {
    items(
        items = movies,
        key = { it.id }
    ) { movie ->
        MovieCard(
            movie = movie,
            onClick = remember(movie.id) { { onMovieClick(movie.id) } }
        )
    }
}

// BETTER: Hoist to ViewModel
LazyColumn {
    items(
        items = movies,
        key = { it.id }
    ) { movie ->
        MovieCard(
            movie = movie,
            onClick = viewModel::onMovieClick  // Stable reference
        )
    }
}
```

#### 1.4 Scope State Reads

```kotlin
// BAD: Outer composable recomposes on every animation frame
@Composable
fun AnimatedCard() {
    val scale by animateFloatAsState(...)

    Card(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)  // Causes Card to recompose
    ) {
        // Complex content also recomposes
    }
}

// GOOD: Only graphicsLayer updates
@Composable
fun AnimatedCard() {
    val scale by animateFloatAsState(...)

    Card(
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Content doesn't recompose
    }
}
```

---

### 2. Animation Performance

#### 2.1 Always Use graphicsLayer for Transforms

```kotlin
// ✅ GOOD: Only draw pass
Modifier.graphicsLayer {
    scaleX = scale
    scaleY = scale
    alpha = alpha
    translationX = offsetX
    translationY = offsetY
    rotationZ = rotation
}

// ❌ BAD: Causes layout pass
Modifier
    .scale(scale)           // Layout
    .alpha(alpha)           // OK, but graphicsLayer is better
    .offset(x = offsetX)    // Layout
    .rotate(rotation)       // Layout
```

#### 2.2 Optimal Animation Specs

```kotlin
// Interactive press feedback (fast)
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
// ~200ms, bouncy

// Shared element transitions (smooth)
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)
// ~350ms, smooth

// Celebration/delight (expressive)
spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)
// ~500ms, very bouncy

// Enter/exit animations (crisp)
tween(durationMillis = 300, easing = FastOutSlowInEasing)
```

#### 2.3 Batch Related Animations

```kotlin
// Animate multiple properties together
val transition = updateTransition(targetState = isExpanded)

val scale by transition.animateFloat { if (it) 1f else 0.95f }
val alpha by transition.animateFloat { if (it) 1f else 0.6f }
val elevation by transition.animateDp { if (it) 8.dp else 2.dp }

Card(
    modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    },
    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
)
```

---

### 3. List Performance

#### 3.1 Always Provide Keys

```kotlin
// ✅ CRITICAL
LazyColumn {
    items(
        items = movies,
        key = { movie -> movie.id }  // ALWAYS provide
    ) { movie ->
        MovieCard(movie)
    }
}
```

#### 3.2 Use contentType for Mixed Lists

```kotlin
LazyColumn {
    items(
        items = feedItems,
        key = { it.id },
        contentType = { item ->
            when (item) {
                is FeedItem.Header -> "header"
                is FeedItem.MovieCard -> "movie"
                is FeedItem.TvCard -> "tv"
                is FeedItem.Ad -> "ad"
            }
        }
    ) { item ->
        when (item) {
            is FeedItem.Header -> HeaderItem(item)
            is FeedItem.MovieCard -> MovieCard(item)
            is FeedItem.TvCard -> TvCard(item)
            is FeedItem.Ad -> AdItem(item)
        }
    }
}
```

#### 3.3 Keep Item Composables Light

```kotlin
// ❌ BAD: Heavy computation in item
items(movies) { movie ->
    val processedRating = calculateComplexRating(movie)  // Expensive!
    MovieCard(movie, processedRating)
}

// ✅ GOOD: Pre-compute in ViewModel
// ViewModel
val processedMovies = movies.map { movie ->
    MovieWithRating(movie, calculateComplexRating(movie))
}

// Composable
items(processedMovies) { movie ->
    MovieCard(movie)
}
```

#### 3.4 Avoid Nested Scrolling Issues

```kotlin
// ⚠️ CAUTION: LazyRow inside LazyColumn
LazyColumn {
    items(sections) { section ->
        LazyRow {  // Nested lazy
            items(section.movies) { movie ->
                // This is OK but be careful with shared elements
            }
        }
    }
}

// Alternative for small fixed lists
LazyColumn {
    items(sections) { section ->
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            section.movies.forEach { movie ->
                MovieCard(movie)
            }
        }
    }
}
```

---

### 4. Image Optimization

#### 4.1 Current Configuration (Already Good)

```kotlin
// MoviesAndBeyondApplication - already optimized
ImageLoaderInitializer.initImageLoader(
    context = this,
    memoryCacheSize = 128 * 1024 * 1024,  // 128 MB
    diskCacheSize = 256 * 1024 * 1024     // 256 MB
)
```

#### 4.2 Image Sizing Strategy

```kotlin
// For list items (thumbnail)
ImageRequest.Builder(context)
    .data(posterUrl.replace("original", "w500"))  // Smaller size
    .size(500, 750)  // Constrain
    .build()

// For detail backdrop
ImageRequest.Builder(context)
    .data(backdropUrl.replace("original", "w1280"))  // Larger for detail
    .size(1280, 720)
    .build()

// For shared elements
ImageRequest.Builder(context)
    .data(url)
    .crossfade(false)  // Disable crossfade for shared elements
    .build()
```

#### 4.3 Preload for Transitions

```kotlin
// Preload detail image when user hovers/presses list item
LaunchedEffect(isPressed) {
    if (isPressed) {
        // Preload the larger image
        imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(movie.backdropUrl)
                .size(1280, 720)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
        )
    }
}
```

---

### 5. Shared Element Performance

#### 5.1 Keep Shared Content Simple

```kotlin
// ❌ BAD: Complex layout as shared element
Box(
    modifier = Modifier.sharedBounds(...)
) {
    Column {
        Image(...)
        Row {
            Text(...)
            Rating(...)
        }
        FlowRow { genres.forEach { GenreChip(it) } }
    }
}

// ✅ GOOD: Simple shared element
Image(
    modifier = Modifier
        .sharedElement(...)
        .aspectRatio(2f / 3f)
)

// Animate non-shared content separately
Column(
    modifier = Modifier.animateEnterExit(...)
) {
    Title(...)
    Rating(...)
    Genres(...)
}
```

#### 5.2 Transition Duration

```kotlin
// Optimal: 300-400ms
// < 200ms: Feels rushed, hard to follow
// > 500ms: Feels sluggish

// Use spring for natural feel
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)
```

#### 5.3 Fallback Strategy

```kotlin
// Graceful degradation if shared elements cause issues
val useSharedElements = remember {
    // Check device capability or feature flag
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

if (useSharedElements && sharedTransitionScope != null) {
    // Use shared elements
} else {
    // Standard transition
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 4 }
    ) {
        Content()
    }
}
```

---

### 6. Memory Management

#### 6.1 Avoid Memory Leaks

```kotlin
// ❌ BAD: Capturing context in lambda
val context = LocalContext.current
LaunchedEffect(Unit) {
    someFlow.collect {
        doSomethingWith(context)  // Context captured
    }
}

// ✅ GOOD: Use applicationContext or handle properly
val context = LocalContext.current.applicationContext
LaunchedEffect(Unit) {
    someFlow.collect {
        doSomethingWith(context)
    }
}
```

#### 6.2 Clean Up Resources

```kotlin
// In ViewModel
override fun onCleared() {
    super.onCleared()
    // Cancel any ongoing operations
    job?.cancel()
}
```

---

## Screen-Specific Optimizations

### Movies/TV Feed Screens

**Current Issues**: None identified (well-optimized)

**Optimizations Applied**:
- TmdbListImage without crossfade
- LazyRow with pagination
- Shimmer loading

**To Add**:
- [ ] Verify keys in all list items
- [ ] Add contentType if mixed content
- [ ] Profile recomposition counts

### Details Screen

**Potential Issues**:
- Large backdrop image during transition
- Multiple sections loading simultaneously

**Optimizations**:
- [ ] Preload backdrop on list item press
- [ ] Stagger content animations
- [ ] Use simpler shared element (image only)
- [ ] Lazy load cast/recommendations

### Search Screen

**Potential Issues**:
- Rapid recomposition during typing
- Grid re-layout on results change

**Optimizations**:
- [ ] Debounce search query (already done?)
- [ ] AnimatedContent for results transitions
- [ ] Keys for grid items

---

## Testing Checklist

### Profiling Tasks

- [ ] Profile Movies feed scroll - target: 60fps
- [ ] Profile TV feed scroll - target: 60fps
- [ ] Profile Search results scroll - target: 60fps
- [ ] Profile Details scroll - target: 60fps
- [ ] Profile Feed → Details transition - target: 60fps
- [ ] Measure recomposition counts in feeds
- [ ] Memory test: navigate 10 cycles

### Device Testing

- [ ] High-end device (Pixel 8 Pro or equivalent)
- [ ] Mid-range device (Pixel 6a or equivalent)
- [ ] Low-end device (Pixel 3a or equivalent)
- [ ] Tablet (if supported)

### Automated Tests

```kotlin
// Macrobenchmark example
@LargeTest
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollMoviesFeed() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        }
    ) {
        val feedList = device.findObject(By.res("movies_feed_list"))
        feedList.setGestureMargin(device.displayWidth / 5)

        repeat(5) {
            feedList.scroll(Direction.DOWN, 1f)
            device.waitForIdle()
        }
    }
}
```

---

## Monitoring & Alerts

### Metrics to Track

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| P50 frame time | < 8ms | > 12ms |
| P95 frame time | < 16ms | > 20ms |
| P99 frame time | < 20ms | > 32ms |
| Jank rate | < 1% | > 3% |
| Cold start | < 500ms | > 1000ms |

### Firebase Performance Integration

```kotlin
// Track custom traces
val trace = Firebase.performance.newTrace("shared_element_transition")
trace.start()
// ... transition
trace.stop()
```

---

## Implementation Checklist

### Phase 1: Baseline
- [ ] Profile current performance
- [ ] Document baseline metrics
- [ ] Identify hot spots

### Phase 2: Quick Wins
- [ ] Add keys to all lazy lists
- [ ] Verify stable types for state
- [ ] Check lambda stability

### Phase 3: Animation Optimization
- [ ] Use graphicsLayer everywhere
- [ ] Optimize spring specs
- [ ] Profile shared elements

### Phase 4: Image Optimization
- [ ] Verify sizing constraints
- [ ] Test preloading strategy
- [ ] Profile memory during navigation

### Phase 5: Validation
- [ ] Profile on low-end device
- [ ] Run benchmark tests
- [ ] Fix any regressions

---

## Quick Reference

### Good Patterns

```kotlin
// Stable lambda
onClick = remember(id) { { viewModel.onClick(id) } }

// derivedStateOf
val show by remember { derivedStateOf { scroll > 100 } }

// graphicsLayer
Modifier.graphicsLayer { scaleX = scale; scaleY = scale }

// Keys in lists
items(list, key = { it.id }) { ... }
```

### Bad Patterns

```kotlin
// Unstable lambda
onClick = { viewModel.onClick(id) }

// Direct state read causing recomposition
val show = scrollState.value > 100

// Layout-triggering modifiers
Modifier.scale(scale).offset(x = offset)

// Missing keys
items(list) { ... }
```
