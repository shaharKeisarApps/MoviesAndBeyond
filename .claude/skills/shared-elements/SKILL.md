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

## Common Pitfalls

1. **Mismatched keys**: Ensure exact key match between source and destination
2. **Missing AnimatedVisibilityScope**: Always pass the correct scope
3. **Incorrect SharedTransitionLayout placement**: Must wrap all participating content
4. **Large images**: Resize/sample before transition for performance
5. **View interop**: No support for AndroidView, Dialog, ModalBottomSheet

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
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateEnterExit
import androidx.compose.animation.sharedElement
import androidx.compose.animation.sharedBounds
import androidx.compose.animation.rememberSharedContentState
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.OverlayClip
```
