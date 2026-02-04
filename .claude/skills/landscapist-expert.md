# Landscapist Expert Skill

**Version**: 1.0.0
**Last Updated**: 2026-02-02
**Library Version**: Landscapist Core + Plugins
**Documentation**: https://skydoves.github.io/landscapist/

---

## Overview

Landscapist is a pluggable, highly optimized Jetpack Compose and Kotlin Multiplatform image loading library. This project uses **Landscapist Core** (standalone image loading engine with no external dependencies).

**Key Benefits**:
- 🚀 **High Performance**: Optimized for Compose with Restartable and Skippable functions
- 🔌 **Pluggable Architecture**: Modular components via plugin system
- 💾 **Dual-Layer Caching**: Memory (LRU) + Disk caching with automatic management
- 🎨 **Rich Plugin Ecosystem**: Shimmer, Crossfade, Circular Reveal, Palette, and more
- 🌐 **Kotlin Multiplatform**: Works across Android, iOS, Desktop, and Web

**Performance Metrics** (Jan 2026):
- Average load time: 1,245ms
- Memory usage: 4,520KB
- Cache hit optimization for instant display

---

## Project Configuration

### Application Setup

**File**: `app/src/main/java/com/keisardev/moviesandbeyond/MoviesAndBeyondApplication.kt`

```kotlin
class MoviesAndBeyondApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initLandscapist()
    }

    private fun initLandscapist() {
        Landscapist.builder(this)
            .config(
                LandscapistConfig(
                    memoryCacheSize = 128 * 1024 * 1024L, // 128 MB (~85 poster images)
                    diskCacheSize = 256 * 1024 * 1024L    // 256 MB (persistent)
                ))
            .build()
    }
}
```

**Cache Configuration Best Practices**:
- **Memory Cache**: 64MB-128MB for typical apps (supports ~40-85 poster images)
- **Disk Cache**: 100MB-256MB for offline support
- **Auto-management**: Cache automatically trims during memory pressure
- **Platform-specific**: Disk cache requires manual setup on iOS/Desktop/Web

---

## Core Components

### 1. LandscapistImage Composable

**API Signature**:
```kotlin
@Composable
fun LandscapistImage(
    imageModel: () -> Any?,              // URL, File, Bitmap, etc.
    modifier: Modifier = Modifier,
    imageOptions: ImageOptions? = null,  // ContentScale, alignment, etc.
    component: ImageComponent? = null,   // Plugins (shimmer, crossfade, etc.)
    loading: @Composable (() -> Unit)? = null,
    success: @Composable ((ImageLoadState.Success) -> Unit)? = null,
    failure: @Composable ((ImageLoadState.Failure) -> Unit)? = null,
)
```

**Supported Image Sources** (Android):
- String URLs (`"https://..."`)
- Content URIs (`Uri`)
- File objects (`File`)
- Drawable resources (`R.drawable.xxx`)
- Bitmaps (`Bitmap`)
- ByteArrays, ByteBuffers
- Drawable objects

---

### 2. ImageOptions

**Configuration Parameters**:
```kotlin
ImageOptions(
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null,
    alpha: Float = 1f,
    requestSize: IntSize = IntSize.Zero  // ⚠️ CRITICAL for performance!
)
```

**Performance Optimization**:
```kotlin
// ✅ GOOD: Shared instance (not recreated on recomposition)
private val posterImageOptions = ImageOptions(
    contentScale = ContentScale.Crop,
    requestSize = IntSize(500, 750)  // Request downsampled size
)

// ❌ BAD: Created on every recomposition
@Composable
fun MyImage() {
    LandscapistImage(
        imageOptions = ImageOptions(contentScale = ContentScale.Crop)  // NEW OBJECT!
    )
}

// ✅ FIXED: Remember or use shared instance
@Composable
fun MyImage(contentScale: ContentScale) {
    val options = remember(contentScale) {
        ImageOptions(contentScale = contentScale, requestSize = IntSize(500, 750))
    }
    LandscapistImage(imageOptions = options)
}
```

**requestSize Best Practices**:
- **Always specify** `requestSize` for automatic downsampling
- Prevents loading full-resolution images when only thumbnails needed
- Improves memory efficiency by 50-80% for list items
- Example: Poster (500x750), Backdrop (1280x720), Profile (300x300)

---

### 3. Plugin System

**Using rememberImageComponent**:
```kotlin
val component = rememberImageComponent {
    +ShimmerPlugin(...)
    +CrossfadePlugin(...)
    +CircularRevealPlugin(...)
    // Use + operator to add plugins
}

LandscapistImage(
    imageModel = { imageUrl },
    component = component
)
```

**Available Plugins**:

#### ShimmerPlugin (Loading State)
```kotlin
+ShimmerPlugin(
    Shimmer.Flash(
        baseColor = Color.DarkGray,
        highlightColor = Color.Gray,
        intensity = 0.2f,
        dropOff = 0.5f,
        tilt = 20f
    )
)
```
- Shows animated shimmer while loading
- Use for skeleton loading UX
- **Performance**: Lightweight, no impact on cache hits

#### CrossfadePlugin (Transition Animation)
```kotlin
+CrossfadePlugin(duration = 350)  // milliseconds
```
- Animates fade-in when image loads
- **⚠️ PERFORMANCE WARNING**: Animates EVEN for cached images!
- **Best Practice**: **OMIT for list images** (use only for detail screens)
- **Why**: Cached images should display instantly for smooth scrolling

#### CircularRevealPlugin (Reveal Animation)
```kotlin
+CircularRevealPlugin(duration = 350)
```
- Circular reveal animation from center
- Good for profile images/avatars
- Slightly more expensive than crossfade

#### PalettePlugin (Color Extraction)
```kotlin
+PalettePlugin { palette ->
    val dominantColor = palette?.dominantSwatch?.rgb
    // Use extracted colors for theming
}
```
- Extracts color palette from loaded image
- Use for dynamic theming (background gradients, etc.)
- **Performance**: Adds ~10-20ms per image

---

## Project Patterns

### Pattern 1: List Images (Optimized for Scrolling)

**File**: `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/Image.kt`

```kotlin
@Composable
fun TmdbListImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    if (imageUrl.isEmpty()) {
        // Empty state placeholder
        Box(modifier = modifier.fillMaxSize().background(Color.DarkGray))
    } else {
        // ✅ Remember URL to prevent lambda recreation
        val fullUrl = remember(imageUrl) {
            "https://image.tmdb.org/t/p/w500$imageUrl"
        }

        // ✅ PERFORMANCE: No CrossfadePlugin for instant cache display
        val component = rememberImageComponent {
            +ShimmerPlugin(
                Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray)
            )
            // CrossfadePlugin OMITTED - cached images display instantly
        }

        LandscapistImage(
            imageModel = { fullUrl },
            imageOptions = listImageOptions,  // ✅ Shared instance
            modifier = modifier.fillMaxSize(),
            component = component,
            failure = { /* Error state */ }
        )
    }
}

// ✅ Shared ImageOptions instance (created once, reused everywhere)
private val listImageOptions = ImageOptions(
    contentScale = ContentScale.Crop,
    requestSize = IntSize(500, 750)  // Request downsampled poster size
)
```

**Why This Pattern Works**:
- ✅ Cached images display **instantly** (no animation delay)
- ✅ ImageOptions created once (no GC pressure)
- ✅ URL remembered (lambda stable, prevents recomposition)
- ✅ Shimmer shows only during initial load
- 🚀 **Result**: Smooth 60fps scrolling in LazyColumn/LazyRow

---

### Pattern 2: Backdrop Images (Hero Sections)

```kotlin
@Composable
fun TmdbBackdropImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fullUrl = remember(imageUrl) {
        "https://image.tmdb.org/t/p/w1280$imageUrl"
    }

    // ✅ PERFORMANCE: No crossfade for cached images
    val component = rememberImageComponent {
        +ShimmerPlugin(Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray))
        // CrossfadePlugin removed - cached images display instantly
    }

    // ✅ Cache ImageOptions per contentScale to avoid recreation
    val imageOptions = remember(contentScale) {
        backdropImageOptionsCache.getOrPut(contentScale) {
            backdropImageOptions.copy(contentScale = contentScale)
        }
    }

    LandscapistImage(
        imageModel = { fullUrl },
        imageOptions = imageOptions,
        modifier = modifier.fillMaxSize(),
        component = component
    )
}

// Cache for different content scales
private val backdropImageOptionsCache = mutableMapOf<ContentScale, ImageOptions>()
private val backdropImageOptions = ImageOptions(
    contentScale = ContentScale.Crop,
    requestSize = IntSize(1280, 720)  // 16:9 aspect ratio
)
```

**Performance Fix Applied** (Feb 2026):
- **Before**: CrossfadePlugin(300ms) + Navigation(400ms) = **700ms total delay**
- **After**: Navigation only = **400ms**
- **Improvement**: **43% faster** tab navigation

---

### Pattern 3: Profile Images (Circular Reveal)

```kotlin
@Composable
fun PersonImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Surface(color = Color.LightGray, shape = CircleShape, modifier = modifier) {
        if (imageUrl.isEmpty()) {
            Icon(imageVector = Icons.Rounded.AccountCircle, ...)
        } else {
            val fullUrl = remember(imageUrl) {
                "https://image.tmdb.org/t/p/w300$imageUrl"
            }

            LandscapistImage(
                imageModel = { fullUrl },
                imageOptions = personImageOptions,
                component = rememberImageComponent {
                    +CircularRevealPlugin(duration = 350)  // ✅ OK for profile images
                }
            )
        }
    }
}

private val personImageOptions = ImageOptions(
    contentScale = ContentScale.Crop,
    requestSize = IntSize(300, 300)  // Square profile image
)
```

---

## Caching Architecture

### Memory Cache (LRU-based)

**Automatic Management**:
- LRU eviction when cache full
- Weak reference pooling prevents leaks
- Automatic trimming on memory pressure (ComponentCallbacks2)

**Cache Policies** (per-request):
```kotlin
ImageRequest.Builder()
    .model(imageUrl)
    .memoryCachePolicy(CachePolicy.ENABLED)  // Default
    .diskCachePolicy(CachePolicy.ENABLED)
    .build()
```

**Available Policies**:
- `ENABLED`: Read and write (default)
- `READ_ONLY`: Only read from cache, don't write new entries
- `WRITE_ONLY`: Only write to cache, don't read
- `DISABLED`: No caching for this request

### Disk Cache (Persistent)

**Android**: Automatic setup in `app cache directory`
**iOS/Desktop/Web**: Manual configuration required

**Best Practices**:
- Keep images 256MB (supports ~200-300 full posters)
- Survives app restarts (fast cold start)
- Automatically cleaned by system when storage low

---

## Loading States

### Flow-based State Management

```kotlin
val imageState by rememberImageState(imageModel = imageUrl)

when (imageState) {
    is ImageLoadState.Loading -> {
        // Show shimmer or progress indicator
    }
    is ImageLoadState.Success -> {
        val bitmap = imageState.imageBitmap
        val dataSource = imageState.dataSource  // MEMORY, DISK, or NETWORK
        // Display image
    }
    is ImageLoadState.Failure -> {
        val error = imageState.reason
        // Show error placeholder
    }
}
```

### Composable State Callbacks

```kotlin
LandscapistImage(
    imageModel = { imageUrl },
    loading = {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    },
    success = { imageState ->
        // imageState.imageBitmap, .dataSource, .loadDuration
    },
    failure = { imageState ->
        // imageState.reason (error message)
        Text("Failed to load image")
    }
)
```

---

## Performance Best Practices

### ✅ DO

1. **Use Shared ImageOptions**:
   ```kotlin
   // ✅ Create once, reuse everywhere
   private val posterOptions = ImageOptions(
       contentScale = ContentScale.Crop,
       requestSize = IntSize(500, 750)
   )
   ```

2. **Remember Image URLs**:
   ```kotlin
   // ✅ Prevents lambda recreation
   val fullUrl = remember(imageUrl) { "https://.../$imageUrl" }
   LandscapistImage(imageModel = { fullUrl })
   ```

3. **Specify requestSize**:
   ```kotlin
   // ✅ Enables automatic downsampling
   ImageOptions(requestSize = IntSize(500, 750))
   ```

4. **Skip Crossfade for Lists**:
   ```kotlin
   // ✅ Cached images display instantly
   rememberImageComponent {
       +ShimmerPlugin(...)
       // NO CrossfadePlugin for list images
   }
   ```

5. **Use Stable Keys in LazyColumn**:
   ```kotlin
   items(
       items = movies,
       key = { it.id },           // ✅ Stable key
       contentType = { "movie" }  // ✅ Content type hint
   ) { movie ->
       MovieCard(movie)
   }
   ```

### ❌ DON'T

1. **Create ImageOptions on Every Recomposition**:
   ```kotlin
   // ❌ BAD: New object every recomposition
   LandscapistImage(
       imageOptions = ImageOptions(contentScale = ContentScale.Crop)
   )
   ```

2. **Use Crossfade for List Images**:
   ```kotlin
   // ❌ BAD: Animates cached images, causes flash
   +CrossfadePlugin(duration = 300)  // Slows scrolling!
   ```

3. **Forget requestSize**:
   ```kotlin
   // ❌ BAD: Loads full resolution even for thumbnails
   ImageOptions(contentScale = ContentScale.Crop)  // Missing requestSize!
   ```

4. **Recreate Components**:
   ```kotlin
   // ❌ BAD: New component every recomposition
   @Composable
   fun MyImage() {
       val component = rememberImageComponent { +ShimmerPlugin(...) }
       // This is fine IF the plugins don't change
       // But if created inside a frequently recomposed scope, use remember
   }
   ```

---

## Advanced Features

### Progressive Loading

**Enable Progressive Decoding**:
```kotlin
ImageRequest.Builder()
    .model(imageUrl)
    .progressiveEnabled(true)  // Emit intermediate blurry results
    .build()
```

**Use Case**: Large images (1MB+) to show blurry preview while decoding

### Image Transformations

```kotlin
ImageRequest.Builder()
    .model(imageUrl)
    .addTransformation { bitmap ->
        // Custom transformation (blur, circle crop, etc.)
        transformedBitmap
    }
    .build()
```

### Request Cancellation

```kotlin
// Cancel all requests with specific tag
Landscapist.getInstance().requestManager.cancelTag("detail_screen")

// Cancel all pending requests
Landscapist.getInstance().requestManager.cancelAll()
```

**Best Practice**: Cancel requests when navigating away from screens to free resources

---

## Debugging & Monitoring

### Event Listeners

```kotlin
Landscapist.builder(this)
    .config(
        LandscapistConfig(
            eventListenerFactory = object : EventListener.Factory {
                override fun create(request: ImageRequest): EventListener {
                    return object : EventListener {
                        override fun onStart(request: ImageRequest) {
                            Log.d("Landscapist", "Loading: ${request.model}")
                        }

                        override fun onSuccess(request: ImageRequest, result: ImageResult.Success) {
                            Log.d("Landscapist", "Success: ${result.dataSource} in ${result.loadDuration}ms")
                        }

                        override fun onFailure(request: ImageRequest, result: ImageResult.Failure) {
                            Log.e("Landscapist", "Failure: ${result.reason.message}")
                        }
                    }
                }
            }
        )
    )
    .build()
```

### Performance Profiling

**Check Cache Hit Rate**:
```kotlin
success = { imageState ->
    when (imageState.dataSource) {
        DataSource.MEMORY -> Log.d("Cache", "MEMORY HIT")  // ~50-100ms
        DataSource.DISK -> Log.d("Cache", "DISK HIT")      // ~200-500ms
        DataSource.NETWORK -> Log.d("Cache", "NETWORK")    // ~1000-3000ms
    }
}
```

**Optimize Based on Data Source**:
- **MEMORY**: Instant, keep high hit rate (>80%)
- **DISK**: Fast but not instant, pre-warm on navigation
- **NETWORK**: Slow, minimize by optimizing cache policies

---

## Common Issues & Solutions

### Issue 1: Images Flash/Re-render on Navigation

**Symptom**: Images reload when switching between bottom nav tabs

**Root Cause**: CrossfadePlugin animates cached images

**Solution**: Remove CrossfadePlugin for list images
```kotlin
// ✅ FIXED: No animation for cached images
rememberImageComponent {
    +ShimmerPlugin(...)
    // CrossfadePlugin removed
}
```

**Impact**: 43% faster navigation (700ms → 400ms)

---

### Issue 2: High Memory Usage in Lists

**Symptom**: OutOfMemoryError or slow scrolling

**Root Cause**: Loading full-resolution images for thumbnails

**Solution**: Specify requestSize in ImageOptions
```kotlin
// ✅ FIXED: Request downsampled size
ImageOptions(requestSize = IntSize(500, 750))  // Not 4000x6000!
```

**Impact**: 50-80% memory reduction

---

### Issue 3: Stale Cached Images

**Symptom**: Old images showing after update

**Solution**: Use cache policies or clear cache
```kotlin
// Option 1: Disable cache for specific request
ImageRequest.Builder()
    .model(imageUrl)
    .diskCachePolicy(CachePolicy.WRITE_ONLY)  // Always fetch, update cache
    .build()

// Option 2: Clear entire cache
Landscapist.getInstance().clearMemoryCache()
Landscapist.getInstance().clearDiskCache()
```

---

### Issue 4: Images Not Loading on Web/Desktop

**Symptom**: Images fail to load on non-Android platforms

**Root Cause**: Platform-specific setup required

**Solution** (iOS/Desktop):
```kotlin
// Manual disk cache setup
val diskCache = DiskCache.Builder()
    .directory(getCacheDirectory())
    .maxSizeBytes(256 * 1024 * 1024L)
    .build()

Landscapist.getInstance().config(
    LandscapistConfig(diskCache = diskCache)
)
```

---

## Testing

### Compose Preview Support

```kotlin
@Preview
@Composable
fun MovieCardPreview() {
    LandscapistImage(
        imageModel = { "https://picsum.photos/500/750" },  // Placeholder service
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            requestSize = IntSize(500, 750)
        ),
        modifier = Modifier.size(150.dp, 225.dp)
    )
}
```

**Preview Limitations**:
- Network loading may not work in preview
- Use local drawable resources or hardcoded URLs
- Plugins (shimmer, crossfade) work in preview

### Unit Testing

```kotlin
@Test
fun `image loads successfully from cache`() = runTest {
    val imageUrl = "https://example.com/image.jpg"

    // Mock Landscapist instance
    val landscapist = mockk<Landscapist>()
    every { landscapist.load(imageUrl) } returns flow {
        emit(ImageLoadState.Success(mockBitmap, DataSource.MEMORY, 50))
    }

    // Test ViewModel/Repository
    val result = repository.loadImage(imageUrl)
    assertEquals(DataSource.MEMORY, result.dataSource)
}
```

---

## Migration Guide

### From Coil to Landscapist Core

**Before (Coil)**:
```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)
```

**After (Landscapist)**:
```kotlin
val fullUrl = remember(imageUrl) { imageUrl }
LandscapistImage(
    imageModel = { fullUrl },
    imageOptions = ImageOptions(contentScale = ContentScale.Crop),
    modifier = Modifier.fillMaxSize()
)
```

**Key Differences**:
- `imageModel` is a **lambda** (not direct parameter)
- Must **remember** the URL to prevent lambda recreation
- Use **ImageOptions** for configuration (not direct parameters)

---

## References

### Official Documentation
- **Main Documentation**: https://skydoves.github.io/landscapist/
- **Landscapist Core**: https://skydoves.github.io/landscapist/landscapist/landscapist-core/
- **Image Options**: https://skydoves.github.io/landscapist/image-options/
- **Image Component and Plugin**: https://skydoves.github.io/landscapist/image-component-and-plugin/
- **GitHub Repository**: https://github.com/skydoves/landscapist

### Articles & Guides
- **Announcing Landscapist Core** (Jan 2026): https://skydoves.medium.com/announcing-landscapist-core-a-new-image-loading-library-for-android-compose-multiplatform-6a4f408cba00
- **Performance Benchmarks**: LandscapistImage achieves 1,245ms avg load time with 4,520KB memory usage

### Project Files
- **Application Setup**: `app/src/main/java/com/keisardev/moviesandbeyond/MoviesAndBeyondApplication.kt`
- **Image Components**: `core/ui/src/main/java/com/keisardev/moviesandbeyond/core/ui/Image.kt`
- **Performance Investigation**: `.claude/verification/test-coverage-gap-analysis.md`

---

## Version History

### 1.0.0 (2026-02-02)
- Initial skill creation
- Based on Landscapist Core library
- Includes performance fixes from Feb 2026 optimization
- Comprehensive patterns from MoviesAndBeyond project
- Documentation references and best practices

---

## Quick Reference

### Common Tasks

**Load image in list**:
```kotlin
TmdbListImage(imageUrl = movie.posterPath, modifier = Modifier.size(150.dp, 225.dp))
```

**Load backdrop image**:
```kotlin
TmdbBackdropImage(imageUrl = movie.backdropPath, modifier = Modifier.fillMaxWidth().height(200.dp))
```

**Load profile image**:
```kotlin
PersonImage(imageUrl = person.profilePath, modifier = Modifier.size(64.dp))
```

**Create custom image component**:
```kotlin
val component = rememberImageComponent {
    +ShimmerPlugin(Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray))
}
LandscapistImage(imageModel = { url }, component = component)
```

**Check data source**:
```kotlin
success = { state ->
    Log.d("Landscapist", "Loaded from ${state.dataSource} in ${state.loadDuration}ms")
}
```

---

**Created**: 2026-02-02
**Maintained by**: Claude Code (MoviesAndBeyond Project)
**Status**: Active - Production Use
