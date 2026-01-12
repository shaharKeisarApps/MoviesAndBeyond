# Landscapist Image Loading Skill

## Overview

This skill provides expert guidance for implementing and migrating to Landscapist, a highly optimized, pluggable Jetpack Compose and Kotlin Multiplatform image loading library. The primary composable is `LandscapistImage`, which uses the standalone `landscapist-core` engine - a lightweight, cross-platform solution with no external dependencies.

## When to Use

- **Migrating from Coil** to Landscapist for enhanced plugin support
- **Implementing image loading** with shimmer effects, crossfade animations, or circular reveals
- **Adding palette extraction** to extract colors from images
- **Implementing zoomable images** with pinch/pan gestures
- **Optimizing image loading performance** with Restartable/Skippable composables
- **Building KMP apps** that need cross-platform image loading (Android, iOS, Desktop, Web)

## Key Advantages

| Feature | Description |
|---------|-------------|
| **Lightweight** | ~312 KB (vs Coil ~460KB, Glide ~689KB) |
| **KMP Support** | Works on Android, iOS, Desktop, Web |
| **Built-in Caching** | Memory + Disk caching with LRU eviction |
| **Shimmer Loading** | Built-in `ShimmerPlugin` with multiple styles |
| **Animations** | `CrossfadePlugin`, `CircularRevealPlugin` |
| **Palette Extraction** | Built-in `PalettePlugin` for dynamic theming |
| **Blur Transformation** | Built-in `BlurTransformationPlugin` |
| **Zoomable Images** | Built-in `ZoomablePlugin` for pinch/pan |
| **Progressive Loading** | Low-res previews while downloading |
| **Performance** | Restartable/Skippable composables |

## Quick Start

### Dependencies

```kotlin
// libs.versions.toml
[versions]
landscapist = "2.8.2"

[libraries]
# Core engine + Image composable
landscapist-core = { module = "com.github.skydoves:landscapist-core", version.ref = "landscapist" }
landscapist-image = { module = "com.github.skydoves:landscapist-image", version.ref = "landscapist" }

# Plugins (optional)
landscapist-animation = { module = "com.github.skydoves:landscapist-animation", version.ref = "landscapist" }
landscapist-placeholder = { module = "com.github.skydoves:landscapist-placeholder", version.ref = "landscapist" }
landscapist-palette = { module = "com.github.skydoves:landscapist-palette", version.ref = "landscapist" }
landscapist-transformation = { module = "com.github.skydoves:landscapist-transformation", version.ref = "landscapist" }
landscapist-zoomable = { module = "com.github.skydoves:landscapist-zoomable", version.ref = "landscapist" }
```

### Basic Usage

```kotlin
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import com.skydoves.landscapist.crossfade.CrossfadePlugin

// Simple image loading
LandscapistImage(
    imageModel = { imageUrl },
    imageOptions = ImageOptions(
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )
)

// With shimmer and crossfade
LandscapistImage(
    imageModel = { imageUrl },
    component = rememberImageComponent {
        +ShimmerPlugin(
            Shimmer.Flash(
                baseColor = Color.White,
                highlightColor = Color.LightGray
            )
        )
        +CrossfadePlugin(duration = 550)
    }
)
```

## Documentation Structure

- **[reference.md](reference.md)** - Complete API documentation
- **[examples.md](examples.md)** - Usage patterns and real-world examples
- **[migration-from-coil.md](migration-from-coil.md)** - Step-by-step Coil to Landscapist migration

## Available Modules

| Module | Description |
|--------|-------------|
| `landscapist-core` | Standalone KMP image loading engine with caching |
| `landscapist-image` | `LandscapistImage` composable with full plugin support |
| `landscapist-animation` | CrossfadePlugin, CircularRevealPlugin |
| `landscapist-placeholder` | ShimmerPlugin, PlaceholderPlugin, ThumbnailPlugin |
| `landscapist-transformation` | BlurTransformationPlugin |
| `landscapist-palette` | PalettePlugin for color extraction |
| `landscapist-zoomable` | ZoomablePlugin for pinch/pan gestures |

## Project-Specific Context

This project uses **Landscapist 2.8.2** with `LandscapistImage`:

- `core/ui/src/main/java/.../core/ui/Image.kt` - `TmdbImage` and `PersonImage` composables
- `feature/you/src/main/java/.../feature/you/YouScreen.kt` - TMDB logo display

### Best Practices Applied

1. **Size hints via `requestSize`** - Enables automatic downsampling for better cache efficiency
2. **Content descriptions** - For accessibility support
3. **Plugin combinations** - ShimmerPlugin + CrossfadePlugin for loading UX
4. **CircularRevealPlugin** - For person/avatar images

## Helper Scripts

- **[scripts/find-coil-usage.sh](scripts/find-coil-usage.sh)** - Find all Coil usages in codebase
- **[scripts/check-landscapist-version.sh](scripts/check-landscapist-version.sh)** - Check latest Landscapist version

## References

- [Landscapist GitHub](https://github.com/skydoves/landscapist)
- [Official Documentation](https://skydoves.github.io/landscapist/)
- [Landscapist Core KMP Guide](https://skydoves.github.io/landscapist/landscapist/landscapist-core/)
