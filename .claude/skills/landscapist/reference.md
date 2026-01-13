# Landscapist API Reference

## Table of Contents

- [Landscapist Core](#landscapist-core)
- [LandscapistImage](#landscapistimage)
- [ImageOptions](#imageoptions)
- [ImageLoadState](#imageloadstate)
- [Plugin System](#plugin-system)
- [Animation Plugins](#animation-plugins)
- [Placeholder Plugins](#placeholder-plugins)
- [Transformation Plugins](#transformation-plugins)
- [Palette Plugin](#palette-plugin)
- [Zoomable Plugin](#zoomable-plugin)

---

## Landscapist Core

The standalone Kotlin Multiplatform image loading engine with no external dependencies.

### Key Features

- **Automatic Caching**: Memory and disk caching handled automatically
- **Plugin System**: Modular plugins for shimmer, crossfade, palette extraction
- **KMP Support**: Works across Android, iOS, Desktop, and Web
- **Size Optimization**: Use `requestSize` in `ImageOptions` to enable automatic downsampling

### Dependencies

```kotlin
// In libs.versions.toml
landscapist = "2.8.2"

// In build.gradle.kts
dependencies {
    implementation("com.github.skydoves:landscapist-core:$version")
    implementation("com.github.skydoves:landscapist-image:$version")
    implementation("com.github.skydoves:landscapist-animation:$version")
    implementation("com.github.skydoves:landscapist-placeholder:$version")
    implementation("com.github.skydoves:landscapist-palette:$version")
}
```

### Package Structure

| Module | Description |
|--------|-------------|
| `landscapist-core` | Core engine and utilities |
| `landscapist-image` | `LandscapistImage` composable |
| `landscapist-animation` | `CircularRevealPlugin` |
| `landscapist-placeholder` | `ShimmerPlugin`, `PlaceholderPlugin` |
| `landscapist-palette` | `PalettePlugin` for color extraction |

---

## LandscapistImage

The primary composable for loading and displaying images with full plugin support.

### Signature

```kotlin
@Composable
fun LandscapistImage(
    imageModel: () -> Any?,
    modifier: Modifier = Modifier,
    landscapist: Landscapist = Landscapist.getInstance(),
    imageOptions: ImageOptions = ImageOptions(),
    component: ImageComponent = rememberImageComponent {},
    onImageStateChanged: (ImageLoadState) -> Unit = {},
    previewPlaceholder: Painter? = null,
    loading: @Composable (BoxScope.(ImageLoadState.Loading) -> Unit)? = null,
    success: @Composable (BoxScope.(ImageLoadState.Success, Painter) -> Unit)? = null,
    failure: @Composable (BoxScope.(ImageLoadState.Failure) -> Unit)? = null
)
```

### Parameters

| Parameter | Description |
|-----------|-------------|
| `imageModel` | Lambda returning image source (URL, File, Uri, Resource, ByteArray, etc.) |
| `modifier` | Compose modifier for the image container |
| `landscapist` | Landscapist instance (optional, uses singleton by default) |
| `imageOptions` | Configuration for content scale, alignment, alpha, etc. |
| `component` | Plugin container for shimmer, crossfade, palette, etc. |
| `onImageStateChanged` | Callback for state changes (Loading, Success, Failure) |
| `previewPlaceholder` | Painter to display in Android Studio preview |
| `loading` | Custom loading composable slot |
| `success` | Custom success composable slot |
| `failure` | Custom failure composable slot |

### Supported Model Types

**Android:**
- Network URLs (String)
- Content URIs
- Files
- Drawable resources (Int)
- Bitmaps
- ByteArrays
- Drawables

**iOS:**
- Network URLs
- Local file paths

**Desktop:**
- Network URLs
- File paths

**Web (Wasm):**
- Network URLs only

### Basic Example

```kotlin
LandscapistImage(
    imageModel = { "https://example.com/image.jpg" },
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    imageOptions = ImageOptions(
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )
)
```

### With All Options

```kotlin
LandscapistImage(
    imageModel = { "https://example.com/image.jpg" },
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f),
    imageOptions = ImageOptions(
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        alpha = 1f,
        colorFilter = null
    ),
    component = rememberImageComponent {
        +ShimmerPlugin(Shimmer.Flash(Color.White, Color.LightGray))
        +CrossfadePlugin(duration = 550)
    },
    onImageStateChanged = { state ->
        when (state) {
            is ImageLoadState.Loading -> { /* loading */ }
            is ImageLoadState.Success -> {
                val dataSource = state.dataSource // MEMORY, DISK, NETWORK
            }
            is ImageLoadState.Failure -> { /* error */ }
            ImageLoadState.None -> { /* initial */ }
        }
    },
    loading = {
        Box(Modifier.matchParentSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    },
    failure = {
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error"
            )
            Text("Failed to load")
        }
    }
)
```

---

## ImageOptions

Configuration for image display properties.

```kotlin
data class ImageOptions(
    val contentScale: ContentScale = ContentScale.Fit,
    val alignment: Alignment = Alignment.Center,
    val contentDescription: String? = null,
    val alpha: Float = 1f,
    val colorFilter: ColorFilter? = null,
    val requestSize: IntSize = IntSize.Zero
)
```

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `contentScale` | ContentScale | Fit | How image scales (Crop, Fit, FillBounds, etc.) |
| `alignment` | Alignment | Center | Image alignment within container |
| `contentDescription` | String? | null | Accessibility description |
| `alpha` | Float | 1f | Image transparency (0-1) |
| `colorFilter` | ColorFilter? | null | Color filter to apply |
| `requestSize` | IntSize | Zero | Override image request dimensions |

### ContentScale Options

| Value | Description |
|-------|-------------|
| `ContentScale.Crop` | Scale to fill, cropping if necessary |
| `ContentScale.Fit` | Scale to fit within bounds |
| `ContentScale.FillBounds` | Stretch to fill exactly |
| `ContentScale.FillWidth` | Scale to fill width |
| `ContentScale.FillHeight` | Scale to fill height |
| `ContentScale.Inside` | Scale down only if larger |
| `ContentScale.None` | No scaling |

---

## ImageLoadState

Track loading states and image source.

```kotlin
sealed class ImageLoadState {
    object None : ImageLoadState()
    object Loading : ImageLoadState()
    data class Success(
        val painter: Painter,
        val dataSource: DataSource
    ) : ImageLoadState()
    data class Failure(
        val throwable: Throwable?
    ) : ImageLoadState()
}
```

### DataSource

| Value | Description |
|-------|-------------|
| `MEMORY` | Loaded from memory cache |
| `DISK` | Loaded from disk cache |
| `NETWORK` | Downloaded from network |
| `UNKNOWN` | Source unknown |

---

## Plugin System

### rememberImageComponent

Create a plugin container:

```kotlin
val component = rememberImageComponent {
    +ShimmerPlugin(...)
    +CrossfadePlugin(...)
    +PalettePlugin(...)
}

LandscapistImage(
    imageModel = { url },
    component = component
)
```

### Plugin Composition

Plugins are applied in order. Common combinations:

```kotlin
// Loading with animation
rememberImageComponent {
    +ShimmerPlugin(Shimmer.Flash(...))
    +CrossfadePlugin(duration = 550)
}

// Palette extraction with blur
rememberImageComponent {
    +PalettePlugin { palette -> /* use colors */ }
    +BlurTransformationPlugin(radius = 10)
}

// Thumbnail with crossfade
rememberImageComponent {
    +ThumbnailPlugin(IntSize(30, 30))
    +CrossfadePlugin(duration = 350)
}
```

---

## Animation Plugins

**Dependency:** `landscapist-animation`

### CrossfadePlugin

Smooth fade transition when image loads.

```kotlin
CrossfadePlugin(
    duration: Int = 550  // milliseconds
)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +CrossfadePlugin(duration = 700)
    }
)
```

### CircularRevealPlugin

Circular reveal animation from center.

```kotlin
CircularRevealPlugin(
    duration: Int = 350  // milliseconds
)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    modifier = Modifier.size(100.dp).clip(CircleShape),
    component = rememberImageComponent {
        +CircularRevealPlugin(duration = 500)
    }
)
```

---

## Placeholder Plugins

**Dependency:** `landscapist-placeholder`

### ShimmerPlugin

Animated shimmer effect during loading.

```kotlin
ShimmerPlugin(
    shimmer: Shimmer
)
```

**Shimmer Types:**

```kotlin
// Flash effect (most common)
Shimmer.Flash(
    baseColor: Color = Color.White,
    highlightColor: Color = Color.LightGray,
    intensity: Float = 0f,
    dropOff: Float = 0.5f,
    tilt: Float = 20f,
    durationMillis: Int = 650
)

// Fade effect
Shimmer.Fade(
    baseColor: Color = Color.White,
    highlightColor: Color = Color.LightGray,
    durationMillis: Int = 650
)

// Resonate effect
Shimmer.Resonate(
    baseColor: Color = Color.White,
    highlightColor: Color = Color.LightGray,
    durationMillis: Int = 650
)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +ShimmerPlugin(
            Shimmer.Flash(
                baseColor = MaterialTheme.colorScheme.surface,
                highlightColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
)
```

### PlaceholderPlugin

Static placeholders for loading and failure states.

```kotlin
PlaceholderPlugin.Loading(source: Any)  // ImageBitmap, ImageVector, or Painter
PlaceholderPlugin.Failure(source: Any)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +PlaceholderPlugin.Loading(painterResource(R.drawable.placeholder))
        +PlaceholderPlugin.Failure(painterResource(R.drawable.error))
    }
)
```

### ThumbnailPlugin

Pre-load small thumbnail while fetching full image.

```kotlin
ThumbnailPlugin(
    requestSize: IntSize = IntSize(30, 30)
)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +ThumbnailPlugin(IntSize(50, 50))
        +CrossfadePlugin(duration = 350)
    }
)
```

---

## Transformation Plugins

**Dependency:** `landscapist-transformation`

### BlurTransformationPlugin

Apply blur effect to images.

```kotlin
BlurTransformationPlugin(
    radius: Int = 10,  // blur radius (1-25)
    sampling: Int = 1  // downsampling factor
)
```

**Usage:**

```kotlin
LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +BlurTransformationPlugin(radius = 15, sampling = 2)
    }
)
```

---

## Palette Plugin

**Dependency:** `landscapist-palette`

Extract color palette from images.

```kotlin
PalettePlugin(
    imageModel: Any? = null,
    useCache: Boolean = true,
    paletteLoadedListener: (Palette) -> Unit
)
```

### Palette Object

```kotlin
palette.dominantSwatch?.rgb      // Most prominent color
palette.vibrantSwatch?.rgb       // Vibrant color
palette.mutedSwatch?.rgb         // Muted color
palette.lightVibrantSwatch?.rgb  // Light vibrant
palette.lightMutedSwatch?.rgb    // Light muted
palette.darkVibrantSwatch?.rgb   // Dark vibrant
palette.darkMutedSwatch?.rgb     // Dark muted
```

**Usage:**

```kotlin
var dominantColor by remember { mutableStateOf(Color.Transparent) }

LandscapistImage(
    imageModel = { url },
    component = rememberImageComponent {
        +PalettePlugin { palette ->
            palette.dominantSwatch?.let {
                dominantColor = Color(it.rgb)
            }
        }
    }
)

// Use dominantColor for dynamic theming
Box(modifier = Modifier.background(dominantColor)) {
    // content
}
```

---

## Zoomable Plugin

**Dependency:** `landscapist-zoomable`

Enable pinch-to-zoom and pan gestures.

```kotlin
ZoomablePlugin(
    zoomableState: ZoomableState = rememberZoomableState()
)
```

### ZoomableState

```kotlin
val zoomState = rememberZoomableState(
    maxScale: Float = 5f,
    minScale: Float = 1f,
    doubleTapScale: Float = 2.5f
)

// Programmatic control
zoomState.reset()
zoomState.setScale(2f)
```

**Usage:**

```kotlin
val zoomState = rememberZoomableState()

LandscapistImage(
    imageModel = { highResUrl },
    modifier = Modifier.fillMaxSize(),
    component = rememberImageComponent {
        +ZoomablePlugin(zoomState)
    }
)
```

---

## Event Listeners

For programmatic image loading with the core engine:

```kotlin
class LoggingEventListener : EventListener {
    override fun onStart(request: ImageRequest) {
        Log.d("Landscapist", "Started: ${request.model}")
    }

    override fun onSuccess(request: ImageRequest, result: ImageResult.Success) {
        Log.d("Landscapist", "Success from ${result.dataSource}")
    }

    override fun onFailure(request: ImageRequest, reason: Throwable) {
        Log.e("Landscapist", "Failed: ${reason.message}")
    }

    override fun onCancel(request: ImageRequest) {
        Log.d("Landscapist", "Cancelled: ${request.tag}")
    }
}
```

---

## Request Management

```kotlin
// Cancel by tag
landscapist.requestManager.cancelRequests(tag = "profile-images")

// Cancel all
landscapist.requestManager.cancelAll()

// Check active requests
val hasActive = landscapist.requestManager.hasActiveRequests()
```

---

## Cache Management

```kotlin
// Clear memory cache
landscapist.memoryCache?.clear()

// Trim memory cache
landscapist.memoryCache?.trimToSize(32 * 1024 * 1024L)

// Clear disk cache (Android)
landscapist.diskCache?.clear()
```
