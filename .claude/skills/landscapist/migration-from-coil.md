# Migration from Coil to Landscapist

This guide provides step-by-step instructions for migrating from Coil to Landscapist using the `LandscapistImage` composable with the standalone `landscapist-core` engine.

## Table of Contents

- [Overview](#overview)
- [Step 1: Update Dependencies](#step-1-update-dependencies)
- [Step 2: Update Application Class](#step-2-update-application-class)
- [Step 3: Migrate Image Components](#step-3-migrate-image-components)
- [Step 4: Migrate AsyncImage Usages](#step-4-migrate-asyncimage-usages)
- [Step 5: Add Enhanced Features](#step-5-add-enhanced-features)
- [API Mapping Reference](#api-mapping-reference)
- [Troubleshooting](#troubleshooting)

---

## Overview

### Current Coil Usage in Project

| File | Component | Purpose |
|------|-----------|---------|
| `core/ui/Image.kt` | `SubcomposeAsyncImage` | TMDB image loading with loading indicator |
| `feature/you/YouScreen.kt` | `AsyncImage` | User avatar with SVG support |
| `MoviesAndBeyondApplication.kt` | `ImageLoaderFactory` | Global image loader configuration |

### Migration Benefits

- **Lighter weight**: ~312KB vs ~460KB (Coil)
- **Built-in shimmer effects**: No custom implementation needed
- **Rich animations**: Crossfade and circular reveal out of the box
- **Palette extraction**: Built-in color extraction for dynamic theming
- **KMP ready**: Same API works on Android, iOS, Desktop, Web
- **Better recomposition**: Restartable/Skippable composables

---

## Step 1: Update Dependencies

### Remove Coil Dependencies

```diff
# gradle/libs.versions.toml

[versions]
- coil = "2.7.0"
+ landscapist = "2.4.7"

[libraries]
- coil-kt-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
- coil-kt-svg = { group = "io.coil-kt", name = "coil-svg", version.ref = "coil" }

+ # Landscapist core + image composable
+ landscapist-core = { module = "com.github.skydoves:landscapist-core", version.ref = "landscapist" }
+ landscapist-image = { module = "com.github.skydoves:landscapist-image", version.ref = "landscapist" }

+ # Optional plugins
+ landscapist-animation = { module = "com.github.skydoves:landscapist-animation", version.ref = "landscapist" }
+ landscapist-placeholder = { module = "com.github.skydoves:landscapist-placeholder", version.ref = "landscapist" }
+ landscapist-palette = { module = "com.github.skydoves:landscapist-palette", version.ref = "landscapist" }
```

### Update Module Build Files

```diff
# core/ui/build.gradle.kts

dependencies {
-   implementation(libs.coil.kt.compose)
+   implementation(libs.landscapist.core)
+   implementation(libs.landscapist.image)
+   implementation(libs.landscapist.animation)
+   implementation(libs.landscapist.placeholder)
}
```

```diff
# feature/you/build.gradle.kts

dependencies {
-   implementation(libs.coil.kt.compose)
-   implementation(libs.coil.kt.svg)
+   // Uses core:ui which includes Landscapist
}
```

```diff
# app/build.gradle.kts

dependencies {
-   implementation(libs.coil.kt.compose)
+   implementation(libs.landscapist.core)
+   implementation(libs.landscapist.image)
}
```

---

## Step 2: Update Application Class

### Before (Coil)

```kotlin
// MoviesAndBeyondApplication.kt
package com.keisardev.moviesandbeyond

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MoviesAndBeyondApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .apply {
                setWorkerFactory(workerFactory)
                if (BuildConfig.DEBUG) {
                    setMinimumLoggingLevel(android.util.Log.DEBUG)
                } else {
                    setMinimumLoggingLevel(android.util.Log.ERROR)
                }
            }
            .build()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).crossfade(true).build()
    }
}
```

### After (Landscapist)

```kotlin
// MoviesAndBeyondApplication.kt
package com.keisardev.moviesandbeyond

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.skydoves.landscapist.Landscapist
import com.skydoves.landscapist.LandscapistConfig
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MoviesAndBeyondApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .apply {
                setWorkerFactory(workerFactory)
                if (BuildConfig.DEBUG) {
                    setMinimumLoggingLevel(android.util.Log.DEBUG)
                } else {
                    setMinimumLoggingLevel(android.util.Log.ERROR)
                }
            }
            .build()

    override fun onCreate() {
        super.onCreate()

        // Optional: Initialize Landscapist with custom config
        Landscapist.builder(this)
            .config(LandscapistConfig(
                memoryCacheSize = 64 * 1024 * 1024L,  // 64 MB
                diskCacheSize = 100 * 1024 * 1024L   // 100 MB
            ))
            .build()
    }
}
```

**Note:** Landscapist initialization is optional - it will use sensible defaults if not configured.

---

## Step 3: Migrate Image Components

### TmdbImage Component

#### Before (Coil)

```kotlin
// core/ui/src/main/java/.../core/ui/Image.kt
package com.keisardev.moviesandbeyond.core.ui

import coil.compose.SubcomposeAsyncImage

@Composable
fun TmdbImage(
    width: Int,
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
    alpha: Float = 1f
) {
    Box(modifier = modifier.fillMaxSize().alpha(alpha)) {
        if (imageUrl.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_image_available),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            SubcomposeAsyncImage(
                model = "https://image.tmdb.org/t/p/w${width}${imageUrl}",
                contentDescription = null,
                contentScale = contentScale,
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier.align(Alignment.Center).size(20.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

#### After (Landscapist)

```kotlin
// core/ui/src/main/java/.../core/ui/Image.kt
package com.keisardev.moviesandbeyond.core.ui

import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun TmdbImage(
    width: Int,
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
    alpha: Float = 1f
) {
    Box(modifier = modifier.fillMaxSize().alpha(alpha)) {
        if (imageUrl.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_image_available),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LandscapistImage(
                imageModel = { "https://image.tmdb.org/t/p/w${width}${imageUrl}" },
                imageOptions = ImageOptions(contentScale = contentScale),
                modifier = Modifier.fillMaxSize(),
                component = rememberImageComponent {
                    +ShimmerPlugin(
                        Shimmer.Flash(
                            baseColor = Color.DarkGray,
                            highlightColor = Color.Gray,
                            durationMillis = 800
                        )
                    )
                    +CrossfadePlugin(duration = 350)
                },
                failure = {
                    Box(Modifier.matchParentSize()) {
                        Text(
                            text = stringResource(id = R.string.no_image_available),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            )
        }
    }
}
```

### PersonImage Component

#### Before

```kotlin
@Composable
fun PersonImage(imageUrl: String, modifier: Modifier = Modifier) {
    Surface(color = Color.LightGray, shape = CircleShape, modifier = modifier) {
        if (imageUrl.isEmpty()) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            TmdbImage(width = 300, imageUrl = imageUrl)
        }
    }
}
```

#### After (with circular reveal animation)

```kotlin
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin

@Composable
fun PersonImage(imageUrl: String, modifier: Modifier = Modifier) {
    Surface(color = Color.LightGray, shape = CircleShape, modifier = modifier) {
        if (imageUrl.isEmpty()) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LandscapistImage(
                imageModel = { "https://image.tmdb.org/t/p/w300${imageUrl}" },
                modifier = Modifier.fillMaxSize(),
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                component = rememberImageComponent {
                    +CircularRevealPlugin(duration = 350)
                }
            )
        }
    }
}
```

---

## Step 4: Migrate AsyncImage Usages

### YouScreen Avatar

#### Before (Coil with SVG)

```kotlin
// feature/you/src/main/java/.../feature/you/YouScreen.kt
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(avatarUrl)
        .decoderFactory(SvgDecoder.Factory())
        .build(),
    contentDescription = "User avatar",
    modifier = Modifier.size(80.dp)
)
```

#### After (Landscapist)

```kotlin
// feature/you/src/main/java/.../feature/you/YouScreen.kt
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin

LandscapistImage(
    imageModel = { avatarUrl },
    modifier = Modifier.size(80.dp),
    imageOptions = ImageOptions(contentScale = ContentScale.Crop),
    component = rememberImageComponent {
        +CircularRevealPlugin(duration = 300)
    }
)
```

**Note:** For SVG support, the `landscapist-core` engine supports SVG out of the box for network URLs. For local SVG files, you may need additional configuration.

---

## Step 5: Add Enhanced Features

### Add Palette Extraction for Movie Details

```kotlin
// feature/details/src/main/java/.../DetailsScreen.kt
import com.skydoves.landscapist.palette.PalettePlugin

@Composable
fun MovieDetailHeader(backdropPath: String) {
    var dominantColor by remember { mutableStateOf(Color.Transparent) }

    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        LandscapistImage(
            imageModel = { "https://image.tmdb.org/t/p/w1280${backdropPath}" },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            component = rememberImageComponent {
                +ShimmerPlugin(Shimmer.Fade(Color.DarkGray, Color.Gray))
                +PalettePlugin { palette ->
                    palette.dominantSwatch?.let {
                        dominantColor = Color(it.rgb)
                    }
                }
            }
        )

        // Gradient overlay using extracted color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            dominantColor.copy(alpha = 0.9f)
                        )
                    )
                )
        )
    }
}
```

### Add Zoomable to Detail Images

```kotlin
// feature/details - for full-screen image view
import com.skydoves.landscapist.zoomable.ZoomablePlugin
import com.skydoves.landscapist.zoomable.rememberZoomableState

@Composable
fun FullScreenImage(imageUrl: String, onDismiss: () -> Unit) {
    val zoomState = rememberZoomableState()

    Dialog(onDismissRequest = onDismiss) {
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier.fillMaxSize(),
            component = rememberImageComponent {
                +ZoomablePlugin(zoomState)
            }
        )
    }
}
```

---

## API Mapping Reference

| Coil | Landscapist |
|------|-------------|
| `AsyncImage` | `LandscapistImage` |
| `SubcomposeAsyncImage` | `LandscapistImage` with `loading`/`failure` params |
| `rememberAsyncImagePainter` | Use `LandscapistImage` directly |
| `ImageRequest.Builder` | `imageModel = { url }` |
| `contentScale` | `imageOptions = ImageOptions(contentScale = ...)` |
| `placeholder()` | `PlaceholderPlugin.Loading()` |
| `error()` | `PlaceholderPlugin.Failure()` or `failure = {}` |
| `crossfade(true)` | `CrossfadePlugin(duration = 350)` |
| `ImageLoader.Builder` | `Landscapist.builder().config(...)` |
| `LocalImageLoader` | `Landscapist.getInstance()` |
| `ImageLoaderFactory` | Not needed - configure in Application |

### Content Scale Mapping

```kotlin
// Both use the same ContentScale values
ContentScale.Crop
ContentScale.Fit
ContentScale.FillBounds
ContentScale.FillWidth
ContentScale.FillHeight
ContentScale.Inside
ContentScale.None
```

### Loading State Mapping

| Coil State | Landscapist State |
|------------|-------------------|
| `AsyncImagePainter.State.Loading` | `ImageLoadState.Loading` |
| `AsyncImagePainter.State.Success` | `ImageLoadState.Success` |
| `AsyncImagePainter.State.Error` | `ImageLoadState.Failure` |
| `AsyncImagePainter.State.Empty` | `ImageLoadState.None` |

---

## Troubleshooting

### Common Issues

#### 1. Image Not Loading

**Cause:** Missing internet permission or incorrect URL

**Solution:** Verify `AndroidManifest.xml` has:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

#### 2. Memory Issues

**Solution:** Configure cache sizes in Application:
```kotlin
Landscapist.builder(this)
    .config(LandscapistConfig(
        memoryCacheSize = 32 * 1024 * 1024L,  // 32 MB
        diskCacheSize = 50 * 1024 * 1024L     // 50 MB
    ))
    .build()
```

#### 3. ProGuard/R8 Issues

**Solution:** Add to `proguard-rules.pro`:
```proguard
-keep class com.skydoves.landscapist.** { *; }
```

#### 4. Shimmer Not Showing

**Cause:** Missing placeholder dependency

**Solution:**
```kotlin
implementation("com.github.skydoves:landscapist-placeholder:2.4.7")
```

#### 5. Crossfade Not Working

**Cause:** Missing animation dependency

**Solution:**
```kotlin
implementation("com.github.skydoves:landscapist-animation:2.4.7")
```

### Testing Migration

After migration, verify:

1. **Images load correctly** in all screens
2. **Loading states** show shimmer/placeholder
3. **Error states** display fallback UI
4. **Animations** work (crossfade, circular reveal)
5. **Memory usage** is stable during scrolling
6. **Cache** works (images load faster on revisit)

### Rollback Plan

If issues arise, revert changes by:

1. Restore original `libs.versions.toml` entries
2. Restore original `Image.kt` implementation
3. Restore `ImageLoaderFactory` in Application class
4. Remove Landscapist dependencies

Keep original files backed up until migration is verified in production.
