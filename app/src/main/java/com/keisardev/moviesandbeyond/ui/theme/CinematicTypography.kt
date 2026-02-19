package com.keisardev.moviesandbeyond.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Cinematic typography scale for immersive movie discovery experience. Based on Material Design 3
 * type scale with cinematic adjustments.
 */
object CinematicTypography {
    // ==========================================================================
    // DISPLAY - Hero titles, featured content names
    // ==========================================================================

    val DisplayLarge =
        TextStyle(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.25).sp,
        )

    val DisplayMedium =
        TextStyle(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
        )

    val DisplaySmall =
        TextStyle(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )

    // ==========================================================================
    // HEADLINE - Section headers, movie titles on cards
    // ==========================================================================

    val HeadlineLarge =
        TextStyle(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )

    val HeadlineMedium =
        TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.sp,
        )

    val HeadlineSmall =
        TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        )

    // ==========================================================================
    // TITLE - Card titles, list items
    // ==========================================================================

    val TitleLarge =
        TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp,
        )

    val TitleMedium =
        TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.15.sp,
        )

    val TitleSmall =
        TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        )

    // ==========================================================================
    // BODY - Descriptions, overviews
    // ==========================================================================

    val BodyLarge =
        TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp,
        )

    val BodyMedium =
        TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp,
        )

    val BodySmall =
        TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp,
        )

    // ==========================================================================
    // LABEL - Chips, badges, metadata
    // ==========================================================================

    val LabelLarge =
        TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        )

    val LabelMedium =
        TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )

    val LabelSmall =
        TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )
}

/** Material3 Typography instance using cinematic scale. */
val CinematicM3Typography =
    Typography(
        displayLarge = CinematicTypography.DisplayLarge,
        displayMedium = CinematicTypography.DisplayMedium,
        displaySmall = CinematicTypography.DisplaySmall,
        headlineLarge = CinematicTypography.HeadlineLarge,
        headlineMedium = CinematicTypography.HeadlineMedium,
        headlineSmall = CinematicTypography.HeadlineSmall,
        titleLarge = CinematicTypography.TitleLarge,
        titleMedium = CinematicTypography.TitleMedium,
        titleSmall = CinematicTypography.TitleSmall,
        bodyLarge = CinematicTypography.BodyLarge,
        bodyMedium = CinematicTypography.BodyMedium,
        bodySmall = CinematicTypography.BodySmall,
        labelLarge = CinematicTypography.LabelLarge,
        labelMedium = CinematicTypography.LabelMedium,
        labelSmall = CinematicTypography.LabelSmall,
    )
