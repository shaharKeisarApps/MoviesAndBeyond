# Shared Element Transitions Skill

## Overview

Implementing shared element and shared bounds transitions in Jetpack Compose for seamless visual continuity between screens. Based on official Android documentation and Jetsnack patterns.

## When to Use

- List to detail transitions (movie card → detail screen)
- Image zoom transitions
- Card expansion animations
- Tab content transitions
- Any screen-to-screen content continuity

## Dependencies

```kotlin
// libs.versions.toml
[versions]
compose-animation = "1.7.0"  // Or match your Compose BOM

[libraries]
animation = { module = "androidx.compose.animation:animation", version.ref = "compose-animation" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version = "2.8.0" }
```

---

## Core Concepts

### SharedTransitionLayout

The root container that enables shared element tracking. Must wrap all content participating in shared transitions.

```kotlin
SharedTransitionLayout {
    // All shared element content goes here
    NavHost(...) { }
    // OR
    AnimatedContent(...) { }
}
```

### SharedTransitionScope

Provides the scope needed to use shared element modifiers. Accessed via `this@SharedTransitionLayout`.

### AnimatedVisibilityScope

Required for all shared element modifiers. Provides enter/exit animation context. Obtained from:
- `AnimatedContent`: `this@AnimatedContent`
- `NavHost composable`: `this@composable`
- `AnimatedVisibility`: `this@AnimatedVisibility`

---

## sharedElement vs sharedBounds

| Aspect | sharedElement() | sharedBounds() |
|--------|-----------------|----------------|
| **Use Case** | Same content moves (images, icons) | Container transforms (cards → full screen) |
| **Content** | Only target rendered during transition | Both contents visible with cross-fade |
| **Enter/Exit** | Automatic | Required parameters |
| **Best For** | Hero images, avatars | Card expansions, layout changes |

---

## Implementation Patterns

### Pattern 1: Navigation Compose Integration

```kotlin
@Composable
fun AppNavigation() {
    SharedTransitionLayout {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "list"
        ) {
            composable("list") {
                ListScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onItemClick = { id -> navController.navigate("detail/$id") }
                )
            }

            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable

                DetailScreen(
                    id = id,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
```

### Pattern 2: List Screen with Shared Elements

```kotlin
@Composable
fun ListScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemClick: (Int) -> Unit
) {
    with(sharedTransitionScope) {
        LazyColumn {
            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(index) }
                        .padding(16.dp)
                ) {
                    // Shared image
                    Image(
                        painter = painterResource(item.image),
                        contentDescription = null,
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = "image-$index"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Shared title
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = "title-$index"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    )
                }
            }
        }
    }
}
```

### Pattern 3: Detail Screen with Shared Elements

```kotlin
@Composable
fun DetailScreen(
    id: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    val item = items[id]

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Shared image (expanded)
            Image(
                painter = painterResource(item.image),
                contentDescription = null,
                modifier = Modifier
                    .sharedElement(
                        state = rememberSharedContentState(key = "image-$id"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Shared title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(key = "title-$id"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Non-shared content with enter animation
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.animateEnterExit(
                        enter = fadeIn() + slideInVertically { it / 4 },
                        exit = fadeOut() + slideOutVertically { it / 4 }
                    )
                )
            }
        }
    }
}
```

### Pattern 4: Card Expansion with sharedBounds

```kotlin
@Composable
fun ExpandableCard(
    item: Item,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        val shape = if (isExpanded) {
            RoundedCornerShape(0.dp)
        } else {
            RoundedCornerShape(16.dp)
        }

        Box(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "card-${item.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds,
                    clipInOverlayDuringTransition = OverlayClip(shape)
                )
                .background(MaterialTheme.colorScheme.surface, shape)
                .clip(shape)
                .clickable { onToggle() }
        ) {
            if (isExpanded) {
                ExpandedContent(item, animatedVisibilityScope)
            } else {
                CollapsedContent(item, animatedVisibilityScope)
            }
        }
    }
}
```

### Pattern 5: Unique Key Pattern (Jetsnack Style)

```kotlin
// Define key types for type safety
data class MovieSharedElementKey(
    val movieId: Long,
    val origin: String,  // "feed", "search", "favorites"
    val type: SharedElementType
)

enum class SharedElementType {
    Image,
    Title,
    Rating,
    Card,
    Background
}

// Usage
@Composable
fun MovieCard(
    movie: Movie,
    origin: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = MovieSharedElementKey(
                            movieId = movie.id,
                            origin = origin,
                            type = SharedElementType.Card
                        )
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clickable { onClick() }
        ) {
            Image(
                // ...
                modifier = Modifier.sharedElement(
                    state = rememberSharedContentState(
                        key = MovieSharedElementKey(
                            movieId = movie.id,
                            origin = origin,
                            type = SharedElementType.Image
                        )
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        }
    }
}
```

### Pattern 6: CompositionLocal for Deep Hierarchies

```kotlin
// Define CompositionLocals
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Provide at navigation level
@Composable
fun AppNavigation() {
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            NavHost(...) {
                composable("screen") {
                    CompositionLocalProvider(
                        LocalAnimatedVisibilityScope provides this@composable
                    ) {
                        ScreenContent()
                    }
                }
            }
        }
    }
}

// Use anywhere in hierarchy
@Composable
fun DeepNestedComponent(movie: Movie) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: error("No SharedTransitionScope provided")
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        ?: error("No AnimatedVisibilityScope provided")

    with(sharedTransitionScope) {
        Image(
            modifier = Modifier.sharedElement(
                state = rememberSharedContentState(key = "image-${movie.id}"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        )
    }
}
```

### Pattern 7: Keeping Elements on Top (Bottom Bar)

```kotlin
@Composable
fun MainScreen(
    sharedTransitionScope: SharedTransitionScope
) {
    with(sharedTransitionScope) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(
                            zIndexInOverlay = 1f
                        )
                        .animateEnterExit(
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it }
                        )
                ) {
                    // Navigation items
                }
            }
        ) { padding ->
            // Content
        }
    }
}
```

---

## Integration with Predictive Back

```kotlin
@Composable
fun SharedElementWithPredictiveBack() {
    SharedTransitionLayout {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "home",
            // Predictive back animations work automatically with shared elements
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            // composable definitions...
        }
    }
}
```

---

## Performance Best Practices

### DO

```kotlin
// 1. Use simple content in shared elements
Image(
    modifier = Modifier
        .sharedElement(...)
        .size(100.dp)  // Fixed size
)

// 2. Remember shared content state
val state = rememberSharedContentState(key = "my-key")

// 3. Use unique, type-safe keys
data class MyKey(val id: Long, val type: Type)

// 4. Keep transition durations reasonable (300-400ms)
```

### DON'T

```kotlin
// 1. Complex layouts in shared elements
Box(
    modifier = Modifier.sharedElement(...)
) {
    Column {
        Row { /* Many children */ }
        LazyColumn { /* Heavy list */ }
    }
}

// 2. Shared elements in nested scrolling
LazyColumn {
    items(items) {
        LazyRow {  // Nested!
            items(subItems) { subItem ->
                Image(modifier = Modifier.sharedElement(...))  // Problematic
            }
        }
    }
}

// 3. String keys prone to typos
Modifier.sharedElement(rememberSharedContentState(key = "image_123"))  // Risky
Modifier.sharedElement(rememberSharedContentState(key = "image-123"))  // Mismatch!
```

---

## Material 3 Fade Through Pattern (RECOMMENDED for Image-Heavy Content)

### Overview

**Fade Through** is Material 3's flagship pattern for transitions where the shared element (image/card) should be the absolute hero. Instead of sliding or scaling screens, content fades sequentially with a brief gap where **only the shared element is visible**.

**Best For:**
- List → Detail transitions (movie/photo/product cards)
- Image galleries
- Content where the visual element is more important than the screen
- Replacing aggressive horizontal/vertical slides that compete with shared elements

**Production Examples:**
- Google Photos: Grid → Photo detail
- Play Store: App card → App detail
- YouTube: Thumbnail → Video player
- Netflix: Title card → Detail page

### Why Fade Through Works

**Problem with Slides:**
- Spatial translation (slide in/out) competes with shared element's position/scale morph
- Users have to track two motions simultaneously (slide + morph)
- Aggressive motion can feel harsh or "slammy"

**Fade Through Solution:**
- Zero spatial motion - only opacity changes
- Brief gap (50ms) where **only shared element is visible** (hero moment)
- Users track one motion: the shared element morph
- Feels effortless and premium

### Implementation: Navigation 3

```kotlin
// Step 1: Define Material 3 Emphasized Easing Curves
private val EmphasizedDecelerateEasing: Easing =
    CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // Smooth, elastic deceleration

private val EmphasizedAccelerateEasing: Easing =
    CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f) // Quick, confident acceleration

// Step 2: Define Timing Constants
private const val FADE_THROUGH_EXIT_DURATION = 250 // Fade out (quick exit)
private const val FADE_THROUGH_ENTER_DURATION = 300 // Fade in (smooth enter)
private const val FADE_THROUGH_GAP = 50 // Gap where only shared element visible

// Step 3: Implement Fade Through Transition
entry<DetailsRoute>(
    metadata = NavDisplay.transitionSpec {
        // Details fades IN with delay (creates the gap)
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_THROUGH_ENTER_DURATION,
                delayMillis = FADE_THROUGH_GAP, // Brief pause for hero moment
                easing = EmphasizedDecelerateEasing
            )
        ).togetherWith(
            // List fades OUT quickly
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                    easing = EmphasizedAccelerateEasing
                )
            )
        )
    } +
    NavDisplay.popTransitionSpec {
        // Back navigation: symmetric fade
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_THROUGH_ENTER_DURATION,
                delayMillis = FADE_THROUGH_GAP,
                easing = EmphasizedDecelerateEasing
            )
        ).togetherWith(
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                    easing = EmphasizedAccelerateEasing
                )
            )
        )
    } +
    NavDisplay.predictivePopTransitionSpec {
        // Gesture-driven: Linear easing for immediate response
        scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(300, easing = LinearEasing)
        ).togetherWith(
            fadeOut(tween(300, easing = LinearEasing)) +
            scaleOut(
                targetScale = 0.85f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        )
    }
) { route ->
    DetailsScreen(...)
}
```

### Implementation: Circuit Navigation

```kotlin
// Step 1: Same easing curves as above

// Step 2: Define transition in Circuit NavDecoration
@Composable
fun AppNavDecoration(
    backStack: ImmutableList<Record>,
    content: @Composable (Record) -> Unit
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            AnimatedContent(
                targetState = backStack.last(),
                transitionSpec = {
                    // Check if navigating to Details screen
                    if (targetState.isDetailsScreen()) {
                        // Fade Through pattern
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = FADE_THROUGH_ENTER_DURATION,
                                delayMillis = FADE_THROUGH_GAP,
                                easing = EmphasizedDecelerateEasing
                            )
                        ).togetherWith(
                            fadeOut(
                                animationSpec = tween(
                                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                                    easing = EmphasizedAccelerateEasing
                                )
                            )
                        )
                    } else {
                        // Default slide for other screens
                        slideInHorizontally(...).togetherWith(slideOutHorizontally(...))
                    }
                },
                label = "circuit-navigation"
            ) { record ->
                CompositionLocalProvider(
                    LocalAnimatedVisibilityScope provides this@AnimatedContent
                ) {
                    content(record)
                }
            }
        }
    }
}
```

### Motion Choreography Breakdown

**Total Duration: ~600ms**

```
Timeline:
0ms ────────────────────────────────────────────────────────────────> 600ms
     [Exit: 250ms]      [Gap: 50ms]        [Enter: 300ms]
     List fades out →   Only poster →      Details fades in
     (Accelerate)       (Hero moment)      (Decelerate)

Shared Element:
0ms ────────────────────────────────────────────────────────────────> 600ms
     [Morph: ~400-500ms with spring physics damping]
     Poster scales + moves with organic spring motion
```

**Key Insight:** The longer screen fade duration (600ms total) allows the shared element morph (400-500ms) to happen **within** the fade, never competing.

### Material 3 Emphasized Easing Explained

**Why These Curves Matter:**

```kotlin
// EmphasizedAccelerateEasing (0.3, 0.0, 0.8, 0.15)
// For exits - quick and decisive
// Control points create steep acceleration curve
// Content leaves confidently, doesn't linger

// EmphasizedDecelerateEasing (0.05, 0.7, 0.1, 1.0)
// For enters - smooth and cushioned
// Control points create gentle deceleration
// Content arrives with soft, elastic "landing"
```

**Comparison to Legacy Curves:**
- `FastOutSlowInEasing`: Generic cubic-bezier, not optimized for M3 motion
- `LinearEasing`: Mechanical, no personality
- `EaseInOut`: Symmetric, doesn't distinguish enter vs exit

**Material 3 Principle:** Exits should be faster (250ms) than enters (300ms). Content leaving doesn't need savoring; new content should arrive smoothly.

### The Magic 50ms Gap

**Neuroscience Basis:**
- Human visual processing groups motion within ~50-100ms windows
- **< 50ms**: Brain treats as simultaneous (no separation)
- **50-100ms**: Brain notices distinct pause (focus moment)
- **> 100ms**: Feels like jarring delay (too slow)

**Result:** 50ms is the "Goldilocks zone"
- Users subconsciously notice the poster is "alone" for a moment
- Eye naturally focuses on the morphing element
- Creates a subtle "reveal" feeling
- Premium apps use 30-75ms range (test to find sweet spot)

### Fine-Tuning Guide

**If transition feels too slow:**
```kotlin
private const val FADE_THROUGH_EXIT_DURATION = 200 // Reduce from 250ms
private const val FADE_THROUGH_ENTER_DURATION = 250 // Reduce from 300ms
private const val FADE_THROUGH_GAP = 30 // Reduce from 50ms
```

**If transition feels too fast/rushed:**
```kotlin
private const val FADE_THROUGH_EXIT_DURATION = 300 // Increase from 250ms
private const val FADE_THROUGH_ENTER_DURATION = 350 // Increase from 300ms
private const val FADE_THROUGH_GAP = 75 // Increase from 50ms
```

**If gap isn't noticeable enough:**
- Increase `FADE_THROUGH_GAP` to 75-100ms
- Ensure shared element has distinct visual (poster with clear subject)
- Test with different background colors (gap more visible on light backgrounds)

### Production Best Practices

**DO:**
- ✅ Use emphasized easing for screen transitions
- ✅ Keep gap subtle (30-75ms range)
- ✅ Let shared element use spring physics (automatic)
- ✅ Test on various poster positions (top/bottom of list)
- ✅ Ensure 60fps throughout transition

**DON'T:**
- ❌ Use LinearEasing or FastOutSlowInEasing (not Material 3 compliant)
- ❌ Make gap too long (>100ms feels slow)
- ❌ Add spatial translation (slide/scale) - defeats the purpose
- ❌ Use tween for shared element (let spring physics handle it)
- ❌ Skip predictive back implementation

### When NOT to Use Fade Through

**Use Slide Instead:**
- Navigation drawer/sidebar transitions
- Tab switching (horizontal slide expected)
- Settings/form screens (no prominent image)

**Use Container Transform Instead:**
- When card itself should expand to become screen
- Premium apps with time for complex implementation
- When you want maximum "wow" factor

**Use Scale + Fade Instead:**
- Content that benefits from "zooming in" metaphor
- Maps (zoom into location)
- Diagrams that expand

### Alternative: Soft Slide (20% Distance)

If you need to preserve spatial navigation cues but want smoother motion:

```kotlin
slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth / 5 }, // 20% travel (not 100%)
    animationSpec = tween(350, easing = EmphasizedDecelerateEasing)
) + fadeIn(tween(300, easing = EmphasizedDecelerateEasing))
```

**Result:** Gentle "page turn" instead of "screen slam"

---

## Common Pitfalls

1. **Mismatched keys**: Ensure exact key match between source and destination
2. **Missing AnimatedVisibilityScope**: Always pass the correct scope
3. **Incorrect SharedTransitionLayout placement**: Must wrap all participating content
4. **Large images**: Resize/sample before transition for performance
5. **View interop**: No support for AndroidView, Dialog, ModalBottomSheet
6. **Using wrong easing curves**: Always use Material 3 emphasized easing, not legacy curves
7. **Gap too long**: Keep fade through gap under 100ms to avoid sluggish feel

---

## Debugging Tips

```kotlin
// Check if match found
val sharedContentState = rememberSharedContentState(key = "my-key")
val isMatched = sharedContentState.isMatchFound

// Log for debugging
LaunchedEffect(sharedContentState.isMatchFound) {
    Log.d("SharedElement", "Match found: ${sharedContentState.isMatchFound}")
}
```

---

## Limitations

1. **No View interoperability** - Pure Compose only
2. **ContentScale not animated** - Snaps to end value
3. **No automatic shape clipping animation** - Use clipInOverlayDuringTransition
4. **No cross-activity transitions** - Same Activity only

---

## Common Imports

```kotlin
// Core shared element APIs
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateEnterExit
import androidx.compose.animation.sharedElement
import androidx.compose.animation.sharedBounds
import androidx.compose.animation.rememberSharedContentState
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.OverlayClip

// Screen transitions (Fade Through pattern)
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

// Material 3 Emphasized Easing (IMPORTANT)
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

// Define Material 3 curves
private val EmphasizedDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
```
