@file:Suppress("MatchingDeclarationName")

package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize
import java.util.Locale

/**
 * Color-coded rating colors for visual quality indicators. These should match the theme's rating
 * colors.
 */
object RatingColors {
    /** Green - Excellent (8.0+) */
    val Excellent = Color(0xFF4CAF50)

    /** Amber - Good (6.0-7.9) */
    val Good = Color(0xFFFFC107)

    /** Orange - Average (4.0-5.9) */
    val Average = Color(0xFFFF9800)

    /** Red - Poor (<4.0) */
    val Poor = Color(0xFFF44336)

    /** Gold star color */
    val Star = Color(0xFFFFD700)
}

/** Get the rating color based on score. Uses industry-standard color coding for movie ratings. */
fun getRatingColor(score: Double): Color {
    return when {
        score >= 8.0 -> RatingColors.Excellent
        score >= 6.0 -> RatingColors.Good
        score >= 4.0 -> RatingColors.Average
        else -> RatingColors.Poor
    }
}

/**
 * Cinematic rating badge with color-coded background. Displays movie/TV rating with star icon and
 * score.
 *
 * Features:
 * - Color-coded background based on rating quality
 * - Animated scale-in when appearing
 * - Multiple size variants
 *
 * @param rating The rating score (0.0 - 10.0)
 * @param modifier Modifier for the badge
 * @param size Badge size variant (SMALL, MEDIUM, LARGE)
 * @param showAnimation Whether to animate on appearance
 */
@Composable
fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier,
    size: RatingBadgeSize = RatingBadgeSize.SMALL,
    showAnimation: Boolean = true
) {
    val backgroundColor = remember(rating) { getRatingColor(rating) }

    val scale by
        animateFloatAsState(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "rating_scale")

    val (iconSize, fontSize, horizontalPadding) =
        when (size) {
            RatingBadgeSize.SMALL -> Triple(14.dp, 11.sp, 6.dp)
            RatingBadgeSize.MEDIUM -> Triple(16.dp, 13.sp, 8.dp)
            RatingBadgeSize.LARGE -> Triple(20.dp, 15.sp, 10.dp)
        }

    Row(
        modifier =
            modifier
                .height(size.height)
                .graphicsLayer {
                    if (showAnimation) {
                        scaleX = scale
                        scaleY = scale
                    }
                }
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = Color.White)

            Text(
                text = String.format(Locale.getDefault(), "%.1f", rating),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White)
        }
}

/**
 * Compact rating badge showing just the score and star. For use on poster card overlays where space
 * is limited.
 */
@Composable
fun CompactRatingBadge(rating: Double, modifier: Modifier = Modifier) {
    val backgroundColor = remember(rating) { getRatingColor(rating) }

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor.copy(alpha = 0.9f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = Color.White)

            Text(
                text = String.format(Locale.getDefault(), "%.1f", rating),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White)
        }
}

/** Star rating display with filled/empty stars. Converts 10-point scale to 5-star display. */
@Composable
fun StarRating(rating: Double, modifier: Modifier = Modifier, maxStars: Int = 5) {
    val starCount = (rating / 2).coerceIn(0.0, maxStars.toDouble())

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(maxStars) { index ->
            val tint =
                if (index < starCount.toInt()) {
                    RatingColors.Star
                } else if (index < starCount) {
                    RatingColors.Star.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }

            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeSmall),
                tint = tint)
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun RatingBadgeExcellentPreview() {
    MaterialTheme { RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun RatingBadgeGoodPreview() {
    MaterialTheme { RatingBadge(rating = 7.2, size = RatingBadgeSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun RatingBadgeAveragePreview() {
    MaterialTheme { RatingBadge(rating = 5.5, size = RatingBadgeSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun RatingBadgePoorPreview() {
    MaterialTheme { RatingBadge(rating = 3.2, size = RatingBadgeSize.MEDIUM) }
}

@Preview(showBackground = true)
@Composable
private fun RatingBadgeSizesPreview() {
    MaterialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RatingBadge(rating = 8.5, size = RatingBadgeSize.SMALL)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.LARGE)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactRatingBadgePreview() {
    MaterialTheme { CompactRatingBadge(rating = 7.8) }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingPreview() {
    MaterialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StarRating(rating = 10.0) // 5 stars
            StarRating(rating = 7.0) // 3.5 stars
            StarRating(rating = 4.0) // 2 stars
        }
    }
}

// endregion
