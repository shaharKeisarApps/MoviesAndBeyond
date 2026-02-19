package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.animation.circular.CircularRevealPlugin
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

// Shared ImageOptions instances to avoid recreation on recomposition
private val listImageOptions =
    ImageOptions(contentScale = ContentScale.Crop, requestSize = IntSize(500, 750))

private val personImageOptions =
    ImageOptions(contentScale = ContentScale.Crop, requestSize = IntSize(300, 300))

private val backdropImageOptions =
    ImageOptions(contentScale = ContentScale.Crop, requestSize = IntSize(1280, 720))

private val profileImageOptions =
    ImageOptions(contentScale = ContentScale.Crop, requestSize = IntSize(185, 278))

// Cache for backdrop images with different content scales to avoid recreation
private val backdropImageOptionsCache = mutableMapOf<ContentScale, ImageOptions>()

/**
 * Reusable image component for person/cast profile images. Uses CircularRevealPlugin for animated
 * reveal effect.
 *
 * @param imageUrl The TMDB image path (without base URL)
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image container
 */
@Composable
fun PersonImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
        modifier = modifier,
    ) {
        if (imageUrl.isEmpty()) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Remember URL to prevent lambda recreation
            val fullUrl = remember(imageUrl) { "https://image.tmdb.org/t/p/w300$imageUrl" }

            LandscapistImage(
                imageModel = { fullUrl },
                modifier = Modifier.fillMaxSize(),
                imageOptions = personImageOptions,
                component = rememberImageComponent { +CircularRevealPlugin(duration = 350) },
            )
        }
    }
}

/**
 * Reusable TMDB image component with shimmer loading and crossfade animation. Use this for detail
 * screens, backdrops, and non-list images.
 *
 * @param width TMDB image width (e.g., 500, 780, 1280)
 * @param imageUrl The TMDB image path (without base URL)
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image container
 * @param contentScale How the image should be scaled
 * @param alpha Image transparency (0-1)
 */
@Composable
fun TmdbImage(
    width: Int,
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.FillBounds,
    alpha: Float = 1f,
) {
    Box(modifier = modifier.fillMaxSize().alpha(alpha)) {
        if (imageUrl.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_image_available),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LandscapistImage(
                imageModel = { "https://image.tmdb.org/t/p/w${width}$imageUrl" },
                imageOptions =
                    ImageOptions(
                        contentScale = contentScale,
                        contentDescription = contentDescription,
                        alpha = alpha,
                        // Request size hint for automatic downsampling and better cache efficiency
                        requestSize = IntSize(width, (width * 1.5).toInt()),
                    ),
                modifier = Modifier.fillMaxSize(),
                component =
                    rememberImageComponent {
                        +ShimmerPlugin(
                            Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray)
                        )
                        // CrossfadePlugin removed - cached images display instantly
                    },
                failure = {
                    Box(Modifier.matchParentSize()) {
                        Text(
                            text = stringResource(id = R.string.no_image_available),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                },
            )
        }
    }
}

/**
 * Optimized TMDB image component for scrolling lists (LazyRow, LazyColumn).
 *
 * Performance optimizations:
 * - Remembered URL string to prevent lambda recreation
 * - Shared ImageOptions instance (not recreated per recomposition)
 * - Static plugin configuration (stable, skippable)
 * - Short crossfade (200ms) - cache hits appear instant
 *
 * @param imageUrl The TMDB image path (without base URL)
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for the image container
 */
@Suppress("UnusedParameter") // contentDescription kept for API consistency and future accessibility
@Composable
fun TmdbListImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (imageUrl.isEmpty()) {
        Box(
            modifier =
                modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Text(
                text = stringResource(id = R.string.no_image_available),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    } else {
        // Remember the full URL to prevent lambda recreation on recomposition
        val fullUrl = remember(imageUrl) { "https://image.tmdb.org/t/p/w500$imageUrl" }

        // STABLE: Plugin component with shimmer only (no crossfade for instant cache display)
        val component = rememberImageComponent {
            +ShimmerPlugin(Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray))
            // No CrossfadePlugin - cached images display instantly without animation delay
        }

        LandscapistImage(
            imageModel = { fullUrl },
            imageOptions = listImageOptions,
            modifier = modifier.fillMaxSize(),
            component = component,
            failure = {
                Box(modifier = Modifier.matchParentSize().background(Color.DarkGray)) {
                    Text(
                        text = stringResource(id = R.string.no_image_available),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            },
        )
    }
}

/**
 * Optimized TMDB backdrop image component for backdrop cards and hero sections. Uses 16:9 aspect
 * ratio images at 1280px width.
 *
 * @param imageUrl The TMDB image path (without base URL)
 * @param modifier Modifier for the image container
 * @param contentScale How the image should be scaled (default: Crop)
 * @param contentDescription Accessibility description for the image
 */
@Suppress("UnusedParameter")
@Composable
fun TmdbBackdropImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    if (imageUrl.isEmpty()) {
        Box(
            modifier =
                modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Text(
                text = stringResource(id = R.string.no_image_available),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    } else {
        val fullUrl = remember(imageUrl) { "https://image.tmdb.org/t/p/w1280$imageUrl" }

        // PERFORMANCE: No crossfade for cached images - instant display
        val component = rememberImageComponent {
            +ShimmerPlugin(Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray))
            // CrossfadePlugin removed - cached images display instantly without flash/re-render
        }

        // PERFORMANCE: Cache ImageOptions per contentScale to avoid recreation on recomposition
        val imageOptions =
            remember(contentScale) {
                backdropImageOptionsCache.getOrPut(contentScale) {
                    backdropImageOptions.copy(contentScale = contentScale)
                }
            }

        LandscapistImage(
            imageModel = { fullUrl },
            imageOptions = imageOptions,
            modifier = modifier.fillMaxSize(),
            component = component,
            failure = {
                Box(modifier = Modifier.matchParentSize().background(Color.DarkGray)) {
                    Text(
                        text = stringResource(id = R.string.no_image_available),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            },
        )
    }
}

/**
 * TMDB profile image component for cast/crew circular avatars. Optimized for small profile images
 * with 185px width.
 *
 * @param imageUrl The TMDB image path (without base URL)
 * @param modifier Modifier for the image container
 * @param contentDescription Accessibility description for the image
 */
@Suppress("UnusedParameter")
@Composable
fun TmdbProfileImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (imageUrl.isEmpty()) {
        Box(
            modifier =
                modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
    } else {
        val fullUrl = remember(imageUrl) { "https://image.tmdb.org/t/p/w185$imageUrl" }

        val component = rememberImageComponent {
            +ShimmerPlugin(Shimmer.Flash(baseColor = Color.DarkGray, highlightColor = Color.Gray))
            +CircularRevealPlugin(duration = 250)
        }

        LandscapistImage(
            imageModel = { fullUrl },
            imageOptions = profileImageOptions,
            modifier = modifier.fillMaxSize(),
            component = component,
            failure = { Box(modifier = Modifier.matchParentSize().background(Color.DarkGray)) },
        )
    }
}
