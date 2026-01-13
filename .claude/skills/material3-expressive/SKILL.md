# Material 3 Expressive Skill

## Overview

Patterns for implementing Material 3 Expressive design in Compose apps. M3 Expressive is an expansion of Material Design 3 with research-backed updates to theming, components, motion, typography, and more - designed to help create engaging products that users love.

## When to Use

- Upgrading to M3 Expressive alpha
- Adding expressive motion and animations
- Implementing new M3 Expressive components
- Creating delightful micro-interactions
- Enhancing user feedback with expressive motion

## Dependencies

```kotlin
// libs.versions.toml
[versions]
material3-expressive = "1.5.0-alpha11"  // Latest as of Dec 2025

[libraries]
material3 = { module = "androidx.compose.material3:material3", version.ref = "material3-expressive" }
```

## Opt-In Requirements

```kotlin
// File-level opt-in
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

// Or function-level
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyExpressiveComponent() { }
```

---

## MotionScheme API

### Accessing Motion Schemes

```kotlin
// Two available schemes
val standardMotion = MotionScheme.standard()
val expressiveMotion = MotionScheme.expressive()

// Access via CompositionLocal
val currentMotion = LocalMotionScheme.current
```

### When to Use Each Scheme

| Use Case | Recommended Scheme |
|----------|-------------------|
| General navigation | Standard |
| Form interactions | Standard |
| Success celebrations | Expressive |
| Favorites/likes | Expressive |
| Achievements | Expressive |
| Onboarding | Expressive |
| Error feedback | Standard |

---

## Implementation Patterns

### Pattern 1: Expressive Button States

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    Button(
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
        content = content
    )
}
```

### Pattern 2: Expressive Card Interactions

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> 8.dp
            else -> 4.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_elevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_scale"
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        content = content
    )
}
```

### Pattern 3: Expressive FAB Animation

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    val fabScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (expanded) 0f else 45f
                }
            )
        },
        text = { Text(text) },
        modifier = modifier.graphicsLayer {
            scaleX = fabScale
            scaleY = fabScale
        }
    )
}
```

### Pattern 4: Expressive List Item Interactions

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveListItem(
    headlineContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "list_item_bg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "list_item_scale"
    )

    ListItem(
        headlineContent = headlineContent,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(backgroundColor)
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingContent
    )
}
```

### Pattern 5: Success Celebration Animation

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CelebrationAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "celebration_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isVisible) 0f else -180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "celebration_rotation"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Added to favorites",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
        )
    }
}
```

### Pattern 6: Expressive Loading Indicator

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )

    CircularProgressIndicator(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    )
}
```

---

## Color & Typography

### Expressive Color Usage

```kotlin
// Use surface container variants for depth hierarchy
MaterialTheme.colorScheme.surfaceContainerLowest  // Background
MaterialTheme.colorScheme.surfaceContainerLow     // Cards
MaterialTheme.colorScheme.surfaceContainer        // Elevated cards
MaterialTheme.colorScheme.surfaceContainerHigh    // Selected states
MaterialTheme.colorScheme.surfaceContainerHighest // Pressed states

// Primary for emphasis
MaterialTheme.colorScheme.primary                 // Action items
MaterialTheme.colorScheme.primaryContainer        // Selected items
```

### Typography Animation

```kotlin
@Composable
fun AnimatedHeadline(
    text: String,
    isEmphasized: Boolean,
    modifier: Modifier = Modifier
) {
    val textStyle by animateValueAsState(
        targetValue = if (isEmphasized) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.titleLarge
        },
        typeConverter = TwoWayConverter(
            convertToVector = { /* ... */ },
            convertFromVector = { /* ... */ }
        ),
        label = "text_style"
    )

    // Simpler approach - animate size
    val fontSize by animateFloatAsState(
        targetValue = if (isEmphasized) 28f else 22f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "font_size"
    )

    Text(
        text = text,
        fontSize = fontSize.sp,
        modifier = modifier
    )
}
```

---

## Performance Considerations

### DO: Use graphicsLayer for Transforms

```kotlin
// GOOD: Only draw pass, no layout recalculation
Modifier.graphicsLayer {
    scaleX = animatedScale
    scaleY = animatedScale
    alpha = animatedAlpha
    rotationZ = animatedRotation
}
```

### DON'T: Animate Layout Properties Directly

```kotlin
// BAD: Causes full layout pass on every frame
Modifier.size(animatedSize.dp)

// GOOD: Use scale instead
Modifier.graphicsLayer {
    val scaleFactor = animatedSize / originalSize
    scaleX = scaleFactor
    scaleY = scaleFactor
}
```

### Animation Performance Tips

1. **Keep durations reasonable**: 200-400ms for most interactions
2. **Use spring specs**: More natural than tween for interactive elements
3. **Batch animations**: Animate related properties together
4. **Profile on low-end devices**: Ensure 60fps on budget phones
5. **Avoid simultaneous complex animations**: Stagger if needed

---

## Common Imports

```kotlin
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
```

---

## Testing Expressive Components

```kotlin
@Test
fun testExpressiveButtonScale() {
    composeTestRule.setContent {
        ExpressiveButton(onClick = {}) {
            Text("Test")
        }
    }

    // Verify initial state
    composeTestRule.onNodeWithText("Test")
        .assertExists()

    // Simulate press
    composeTestRule.onNodeWithText("Test")
        .performTouchInput {
            down(center)
            advanceEventTime(100)
        }

    // Verify animation started (would need custom assertion)
}
```
