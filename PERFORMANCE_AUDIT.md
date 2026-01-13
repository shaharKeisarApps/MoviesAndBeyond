# Performance Audit

> Audit Date: January 2026
> App: MoviesAndBeyond

## Goal

Achieve consistent **60fps (16ms frame budget)** throughout the app, especially during:
- List scrolling
- Shared element transitions
- Animation playback
- Image loading

---

## Current Architecture Assessment

### Strengths

1. **Optimized Image Loading (Landscapist)**
   - Memory cache: 128 MB
   - Disk cache: 256 MB
   - `TmdbListImage`: No crossfade for instant cached display
   - Shared ImageOptions instances for reuse

2. **Modern Architecture**
   - Hilt DI with singleton scoping
   - Repository pattern with clean data flow
   - StateFlow for UI state

3. **List Pagination**
   - Automatic append on scroll end
   - Efficient pagination detection

### Potential Issues

1. **Recomposition Efficiency**
   - Need to verify stable types usage
   - Lambda stability in list items
   - derivedStateOf usage

2. **Animation Performance**
   - Current spring animations may need profiling
   - graphicsLayer usage to be verified

3. **Image Sizing**
   - Large backdrop images may cause memory pressure
   - Need to verify proper sizing constraints

---

## Recomposition Analysis

### Areas to Investigate

#### 1. List Item Composables

```kotlin
// Potential issue: Lambda recreation
items(listSnacks) { snack ->
    SnackItem(
        snack = snack,
        onClick = { onItemClick(snack.id) }  // Lambda recreated each recomposition
    )
}

// Solution: Remember or hoist
items(
    items = listSnacks,
    key = { it.id }  // Keys help Compose track items
) { snack ->
    SnackItem(
        snack = snack,
        onClick = remember(snack.id) { { onItemClick(snack.id) } }
    )
}
```

#### 2. State Management

```kotlin
// Potential issue: Recomposition on every scroll position change
val scrollState = rememberScrollState()
val showButton = scrollState.value > 100  // Recomposes every scroll tick

// Solution: derivedStateOf
val showButton by remember {
    derivedStateOf { scrollState.value > 100 }
}
```

#### 3. ViewModel State

```kotlin
// Verify: StateFlow collection pattern
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// Check: Is state class stable?
@Stable  // or @Immutable
data class MoviesUiState(
    val movies: List<Movie>,
    val isLoading: Boolean,
    val error: String?
)
```

---

## Animation Performance Guidelines

### Current Implementation

The app uses spring animations in HazeScaffold:
- `Spring.DampingRatioLowBouncy`
- `Spring.StiffnessLow`
- `Spring.StiffnessMedium`

### Performance Checklist

#### ✅ DO: Use graphicsLayer for Transforms

```kotlin
// GOOD: Only draw pass, no layout recalculation
Modifier.graphicsLayer {
    scaleX = animatedScale
    scaleY = animatedScale
    alpha = animatedAlpha
    translationY = animatedOffset
}
```

#### ❌ DON'T: Animate Layout Properties

```kotlin
// BAD: Causes full layout pass every frame
Modifier
    .size(animatedSize.dp)      // Layout recalc
    .offset(x = animatedX.dp)   // Layout recalc
    .padding(animatedPadding)   // Layout recalc
```

#### Recommended Animation Patterns

```kotlin
// Scale animation (good)
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

Box(
    modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)
```

---

## Image Loading Performance

### Current Configuration

```kotlin
// MoviesAndBeyondApplication
ImageLoaderInitializer.initImageLoader(
    context = this,
    memoryCacheSize = 128 * 1024 * 1024,  // 128 MB
    diskCacheSize = 256 * 1024 * 1024     // 256 MB
)
```

### Image Components Analysis

| Component | Use Case | Crossfade | Performance |
|-----------|----------|-----------|-------------|
| TmdbListImage | List scrolling | ❌ No | ✅ Optimized |
| TmdbImage | Detail screens | ✅ Yes (350ms) | ✅ Good |
| PersonImage | Profile avatars | ✅ CircularReveal | ✅ Good |

### Recommendations

1. **Verify image sizing**
   ```kotlin
   // Check: Are images properly constrained?
   IntSize(500, 750)  // Current for list images
   ```

2. **Memory pressure during transitions**
   - Large backdrop images during shared element may spike memory
   - Consider preloading or size constraints

---

## List Performance Checklist

### Keys

```kotlin
// ✅ Verify keys are provided
LazyColumn {
    items(
        items = movies,
        key = { it.id }  // CRITICAL for performance
    ) { movie ->
        MovieItem(movie)
    }
}
```

### contentType

```kotlin
// For mixed lists
LazyColumn {
    items(
        items = mixedList,
        key = { it.id },
        contentType = { item ->
            when (item) {
                is Header -> "header"
                is Movie -> "movie"
                is Ad -> "ad"
            }
        }
    ) { item ->
        // ...
    }
}
```

### Item Composable Weight

```kotlin
// ❌ Avoid heavy computation in items
items(movies) { movie ->
    val processedData = expensiveComputation(movie)  // BAD
    MovieItem(processedData)
}

// ✅ Move computation to ViewModel/Repository
items(processedMovies) { movie ->
    MovieItem(movie)
}
```

---

## Shared Element Performance

### Pre-Transition Preparation

```kotlin
// For large images, consider:
// 1. Size constraints
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(url)
        .size(500, 750)  // Constrain size
        .crossfade(false)  // Disable for shared element
        .build()
)

// 2. Simple shared content
// Keep shared elements simple - avoid nested layouts
```

### Transition Duration

```kotlin
// Optimal range: 300-400ms
// Too fast (< 200ms): Jarring
// Too slow (> 500ms): Sluggish

spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)
// Results in ~350ms animation
```

---

## Scroll Performance

### Current Lazy Components

1. **LazyColumn** - Movies/TV feeds, Details content
2. **LazyRow** - Category sections (via LazyRowContentSection)
3. **LazyVerticalGrid** - Items screens, Search results

### Optimization Checklist

- [ ] All lists have `key` parameter
- [ ] No lambda recreation in item lambdas
- [ ] No heavy computation in item composables
- [ ] Images properly sized
- [ ] Pagination loads appropriate page size

---

## Memory Considerations

### Current Image Cache

- **Memory**: 128 MB (reasonable)
- **Disk**: 256 MB (reasonable)

### Potential Memory Issues

1. **Navigation stack with large images**
   - Each screen retains state
   - Large backdrop images accumulate

2. **Shared element during transition**
   - Source and destination images in memory
   - Consider size constraints

### Recommendations

1. Clear unnecessary image caches on low memory
2. Use appropriate image sizes per context
3. Consider composition local for shared image sizing

---

## Profiling Recommendations

### Tools to Use

1. **Android Studio Profiler**
   - CPU profiler for frame timing
   - Memory profiler for image caching

2. **Compose Layout Inspector**
   - Recomposition counts
   - Component tree analysis

3. **GPU Rendering Profiler**
   - Frame timing visualization
   - Identify jank sources

4. **Macrobenchmark**
   - Startup time
   - Frame timing during scroll

### Key Metrics to Measure

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| Frame time | < 16ms | GPU Profiler |
| Recomposition count | Minimal | Layout Inspector |
| Memory (images) | Stable | Memory Profiler |
| List scroll FPS | 60fps | Profiler |
| Transition FPS | 60fps | Profiler |

---

## Performance Testing Checklist

### Pre-Launch Checks

- [ ] Profile on release build (not debug)
- [ ] Test on low-end device
- [ ] Measure cold start time
- [ ] Verify 60fps scroll in all lists
- [ ] Test shared element transitions
- [ ] Monitor memory during navigation cycles
- [ ] Test with slow/no network

### Specific Scenarios

1. **Feed Screen Scroll**
   - Fast scroll through entire feed
   - Verify no dropped frames

2. **Search Results**
   - Rapid typing
   - Scroll results quickly

3. **Navigation Cycle**
   - List → Details → Back (repeat 10x)
   - Monitor memory growth

4. **Shared Element (Future)**
   - Transition to details
   - Verify smooth animation
   - No frame drops

---

## Current Performance Status

### Estimated (Needs Profiling)

| Area | Status | Confidence |
|------|--------|------------|
| List scrolling | 🟢 Good | Medium |
| Image loading | 🟢 Good | High |
| Memory management | 🟢 Good | Medium |
| Animation | 🟡 Unknown | Low |
| Recomposition | 🟡 Unknown | Low |

### Action Items

1. **Profile existing animations** - Verify 60fps
2. **Measure recomposition counts** - Identify hot spots
3. **Establish baseline metrics** - Before adding shared elements
4. **Test on low-end device** - Pixel 3a or equivalent
