# Landscapist Usage Examples

## Table of Contents

- [Basic Image Loading](#basic-image-loading)
- [Loading States](#loading-states)
- [Shimmer Effects](#shimmer-effects)
- [Animations](#animations)
- [Placeholders](#placeholders)
- [Palette Extraction](#palette-extraction)
- [Transformations](#transformations)
- [Zoomable Images](#zoomable-images)
- [TMDB-Specific Patterns](#tmdb-specific-patterns)
- [Performance Patterns](#performance-patterns)
- [Compose Preview Support](#compose-preview-support)

---

## Basic Image Loading

### Simple URL Loading

```kotlin
import com.skydoves.landscapist.image.LandscapistImage

@Composable
fun SimpleImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
```

### With Content Scale and Alignment

```kotlin
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.image.LandscapistImage

@Composable
fun CroppedImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}
```

### From Resource

```kotlin
@Composable
fun ResourceImage() {
    LandscapistImage(
        imageModel = { R.drawable.placeholder },
        modifier = Modifier.size(100.dp)
    )
}
```

---

## Loading States

### Custom Loading and Error States

```kotlin
@Composable
fun ImageWithStates(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.fillMaxWidth().height(200.dp),
        loading = {
            Box(Modifier.matchParentSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    strokeCap = StrokeCap.Round
                )
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Failed to load",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
```

### State Callback for Analytics

```kotlin
@Composable
fun TrackedImage(imageUrl: String, analytics: Analytics) {
    LandscapistImage(
        imageModel = { imageUrl },
        onImageStateChanged = { state ->
            when (state) {
                is ImageLoadState.Loading -> {
                    analytics.trackEvent("image_loading_started")
                }
                is ImageLoadState.Success -> {
                    analytics.trackEvent("image_loaded", mapOf(
                        "source" to state.dataSource.name
                    ))
                }
                is ImageLoadState.Failure -> {
                    analytics.trackEvent("image_failed", mapOf(
                        "error" to (state.throwable?.message ?: "unknown")
                    ))
                }
                ImageLoadState.None -> { /* initial state */ }
            }
        }
    )
}
```

---

## Shimmer Effects

### Flash Shimmer (Most Common)

```kotlin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun ShimmerImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.fillMaxWidth().height(200.dp),
        component = rememberImageComponent {
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = MaterialTheme.colorScheme.surface,
                    highlightColor = MaterialTheme.colorScheme.surfaceVariant,
                    durationMillis = 800
                )
            )
        }
    )
}
```

### Fade Shimmer

```kotlin
@Composable
fun FadeShimmerImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +ShimmerPlugin(
                Shimmer.Fade(
                    baseColor = Color(0xFFEEEEEE),
                    highlightColor = Color(0xFFF5F5F5),
                    durationMillis = 1000
                )
            )
        }
    )
}
```

### Themed Shimmer

```kotlin
@Composable
fun ThemedShimmerImage(imageUrl: String) {
    val shimmerBase = MaterialTheme.colorScheme.surfaceContainerLow
    val shimmerHighlight = MaterialTheme.colorScheme.surfaceContainerHigh

    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = shimmerBase,
                    highlightColor = shimmerHighlight
                )
            )
        }
    )
}
```

---

## Animations

### Crossfade Animation

```kotlin
import com.skydoves.landscapist.animation.crossfade.CrossfadePlugin

@Composable
fun CrossfadeImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +CrossfadePlugin(duration = 550)
        }
    )
}
```

### Circular Reveal Animation

```kotlin
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin

@Composable
fun CircularRevealImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.size(150.dp).clip(CircleShape),
        component = rememberImageComponent {
            +CircularRevealPlugin(duration = 400)
        }
    )
}
```

### Combined Shimmer + Crossfade

```kotlin
@Composable
fun PolishedImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.fillMaxWidth().height(200.dp),
        component = rememberImageComponent {
            +ShimmerPlugin(
                Shimmer.Flash(
                    baseColor = Color.LightGray,
                    highlightColor = Color.White
                )
            )
            +CrossfadePlugin(duration = 700)
        }
    )
}
```

---

## Placeholders

### Static Placeholders

```kotlin
import com.skydoves.landscapist.placeholder.placeholder.PlaceholderPlugin

@Composable
fun PlaceholderImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +PlaceholderPlugin.Loading(
                painterResource(R.drawable.placeholder_loading)
            )
            +PlaceholderPlugin.Failure(
                painterResource(R.drawable.placeholder_error)
            )
        }
    )
}
```

### Thumbnail Preloading

```kotlin
import com.skydoves.landscapist.placeholder.thumbnail.ThumbnailPlugin

@Composable
fun ThumbnailImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +ThumbnailPlugin(IntSize(30, 30))
            +CrossfadePlugin(duration = 350)
        }
    )
}
```

### Vector Placeholder

```kotlin
@Composable
fun VectorPlaceholderImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        component = rememberImageComponent {
            +PlaceholderPlugin.Loading(
                rememberVectorPainter(Icons.Default.Image)
            )
            +PlaceholderPlugin.Failure(
                rememberVectorPainter(Icons.Default.BrokenImage)
            )
        }
    )
}
```

---

## Palette Extraction

### Extract Dominant Color

```kotlin
import com.skydoves.landscapist.palette.PalettePlugin

@Composable
fun DynamicThemedCard(imageUrl: String) {
    var backgroundColor by remember { mutableStateOf(Color.Transparent) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            component = rememberImageComponent {
                +PalettePlugin { palette ->
                    palette.dominantSwatch?.let {
                        backgroundColor = Color(it.rgb)
                    }
                }
                +CrossfadePlugin(duration = 350)
            }
        )
    }
}
```

### Full Palette Usage

```kotlin
@Composable
fun PaletteShowcase(imageUrl: String) {
    var palette by remember { mutableStateOf<Palette?>(null) }

    Column {
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            component = rememberImageComponent {
                +PalettePlugin { palette = it }
            }
        )

        palette?.let { p ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                p.dominantSwatch?.rgb?.let {
                    ColorSwatch(Color(it), "Dominant")
                }
                p.vibrantSwatch?.rgb?.let {
                    ColorSwatch(Color(it), "Vibrant")
                }
                p.mutedSwatch?.rgb?.let {
                    ColorSwatch(Color(it), "Muted")
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color, CircleShape)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
```

---

## Transformations

### Blur Effect

```kotlin
import com.skydoves.landscapist.transformation.blur.BlurTransformationPlugin

@Composable
fun BlurredBackground(imageUrl: String) {
    Box {
        // Blurred background
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier.fillMaxSize(),
            component = rememberImageComponent {
                +BlurTransformationPlugin(radius = 25, sampling = 4)
            },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
        )

        // Sharp foreground
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(16.dp)),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
        )
    }
}
```

### Progressive Blur Overlay

```kotlin
@Composable
fun GradientBlurImage(imageUrl: String) {
    Box {
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop)
        )

        // Bottom gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
    }
}
```

---

## Zoomable Images

### Basic Zoomable

```kotlin
import com.skydoves.landscapist.zoomable.ZoomablePlugin

@Composable
fun ZoomableImage(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.fillMaxSize(),
        component = rememberImageComponent {
            +ZoomablePlugin()
        }
    )
}
```

### Zoomable with Controls

```kotlin
import com.skydoves.landscapist.zoomable.rememberZoomableState

@Composable
fun ZoomableImageWithControls(imageUrl: String) {
    val zoomState = rememberZoomableState(
        maxScale = 5f,
        minScale = 1f,
        doubleTapScale = 2.5f
    )

    Column {
        LandscapistImage(
            imageModel = { imageUrl },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            component = rememberImageComponent {
                +ZoomablePlugin(zoomState)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { zoomState.reset() }) {
                Text("Reset")
            }
            Button(onClick = { zoomState.setScale(2f) }) {
                Text("Zoom 2x")
            }
        }
    }
}
```

---

## TMDB-Specific Patterns

### TMDB Image with Width Parameter

```kotlin
@Composable
fun TmdbImage(
    width: Int,
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds
) {
    if (imageUrl.isEmpty()) {
        Box(modifier = modifier) {
            Text(
                text = "No Image",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        LandscapistImage(
            imageModel = { "https://image.tmdb.org/t/p/w${width}${imageUrl}" },
            modifier = modifier,
            imageOptions = ImageOptions(contentScale = contentScale),
            component = rememberImageComponent {
                +ShimmerPlugin(
                    Shimmer.Flash(
                        baseColor = Color.DarkGray,
                        highlightColor = Color.Gray
                    )
                )
                +CrossfadePlugin(duration = 350)
            }
        )
    }
}
```

### Movie Poster Card

```kotlin
@Composable
fun MoviePosterCard(
    posterPath: String,
    title: String,
    onClick: () -> Unit
) {
    var dominantColor by remember { mutableStateOf(Color.DarkGray) }

    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = dominantColor.copy(alpha = 0.15f)
        )
    ) {
        Column {
            LandscapistImage(
                imageModel = { "https://image.tmdb.org/t/p/w342$posterPath" },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                component = rememberImageComponent {
                    +ShimmerPlugin(Shimmer.Flash(Color.Gray, Color.LightGray))
                    +CrossfadePlugin(duration = 300)
                    +PalettePlugin { palette ->
                        palette.dominantSwatch?.let {
                            dominantColor = Color(it.rgb)
                        }
                    }
                }
            )

            Text(
                text = title,
                modifier = Modifier.padding(8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

### Person Avatar

```kotlin
@Composable
fun PersonAvatar(
    profilePath: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.LightGray,
        shape = CircleShape,
        modifier = modifier
    ) {
        if (profilePath.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = name,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LandscapistImage(
                imageModel = { "https://image.tmdb.org/t/p/w185$profilePath" },
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

### Backdrop with Gradient

```kotlin
@Composable
fun MovieBackdrop(
    backdropPath: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LandscapistImage(
            imageModel = { "https://image.tmdb.org/t/p/w1280$backdropPath" },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            component = rememberImageComponent {
                +ShimmerPlugin(Shimmer.Fade(Color.DarkGray, Color.Gray))
            }
        )

        // Gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
    }
}
```

---

## Performance Patterns

### Lazy List Optimization

```kotlin
@Composable
fun MovieGrid(movies: List<Movie>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = movies,
            key = { it.id }
        ) { movie ->
            // Component is remembered per item
            val component = rememberImageComponent {
                +ShimmerPlugin(Shimmer.Flash(Color.Gray, Color.LightGray))
                +CrossfadePlugin(duration = 250)
            }

            LandscapistImage(
                imageModel = { movie.posterUrl },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                component = component,
                imageOptions = ImageOptions(contentScale = ContentScale.Crop)
            )
        }
    }
}
```

### Request Size Optimization

```kotlin
@Composable
fun OptimizedThumbnail(imageUrl: String) {
    LandscapistImage(
        imageModel = { imageUrl },
        modifier = Modifier.size(100.dp),
        // Request smaller image to save memory
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            requestSize = IntSize(100, 100)
        )
    )
}
```

---

## Compose Preview Support

### Preview with Placeholder

```kotlin
@Preview
@Composable
fun MoviePosterPreview() {
    LandscapistImage(
        imageModel = { "" }, // Won't load in preview
        modifier = Modifier
            .width(150.dp)
            .aspectRatio(2f / 3f),
        previewPlaceholder = painterResource(R.drawable.sample_poster)
    )
}
```

### Preview with Drawable Resource

```kotlin
@Preview(showBackground = true)
@Composable
fun AvatarPreview() {
    PersonAvatar(
        profilePath = null, // Uses fallback icon
        name = "John Doe",
        modifier = Modifier.size(80.dp)
    )
}
```
