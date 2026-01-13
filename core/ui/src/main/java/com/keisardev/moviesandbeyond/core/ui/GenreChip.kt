package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

/**
 * Genre chip with selectable state and expressive press animation. Used for genre filtering and
 * display in movie/TV details.
 *
 * @param text The genre name to display
 * @param modifier Modifier for the chip
 * @param selected Whether the chip is selected
 * @param onClick Callback when chip is clicked
 */
@Suppress("LongMethod")
@Composable
fun GenreChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh),
            label = "chip_scale")

    val backgroundColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            animationSpec = spring(),
            label = "chip_bg")

    val contentColor by
        animateColorAsState(
            targetValue =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            animationSpec = spring(),
            label = "chip_content")

    val chipShape = RoundedCornerShape(8.dp)

    Box(
        modifier =
            modifier
                .height(Dimens.chipHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(chipShape)
                .background(backgroundColor)
                .then(
                    if (onClick != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = { onClick() })
                        }
                    } else {
                        Modifier
                    })
                .padding(horizontal = Spacing.sm),
        contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor)
        }
}

/**
 * Outlined genre chip variant for detail screens. Shows genre with subtle border instead of filled
 * background.
 */
@Composable
fun OutlinedGenreChip(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.92f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh),
            label = "outlined_chip_scale")

    val chipShape = RoundedCornerShape(8.dp)

    Box(
        modifier =
            modifier
                .height(Dimens.chipHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(chipShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = chipShape)
                .then(
                    if (onClick != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                },
                                onTap = { onClick() })
                        }
                    } else {
                        Modifier
                    })
                .padding(horizontal = Spacing.sm),
        contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
}

/** Row of genre chips with flow layout. Automatically wraps to next line if needed. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreChipRow(
    genres: List<String>,
    modifier: Modifier = Modifier,
    onGenreClick: ((String) -> Unit)? = null
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.chipSpacing),
        verticalArrangement = Arrangement.spacedBy(Spacing.chipSpacing)) {
            genres.forEach { genre ->
                OutlinedGenreChip(text = genre, onClick = onGenreClick?.let { { it(genre) } })
            }
        }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun GenreChipPreview() {
    MaterialTheme { GenreChip(text = "Action") }
}

@Preview(showBackground = true)
@Composable
private fun GenreChipSelectedPreview() {
    MaterialTheme { GenreChip(text = "Action", selected = true) }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedGenreChipPreview() {
    MaterialTheme { OutlinedGenreChip(text = "Science Fiction") }
}

@Preview(showBackground = true)
@Composable
private fun GenreChipRowPreview() {
    MaterialTheme {
        GenreChipRow(genres = listOf("Action", "Adventure", "Sci-Fi", "Drama", "Thriller"))
    }
}

// endregion
