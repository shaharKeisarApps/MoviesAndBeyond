package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Mutable state holder for live theme color preview without persisting to DataStore. */
class ThemePreviewState {
    var previewColorArgb: Long? by mutableStateOf(null)
}

/** CompositionLocal providing [ThemePreviewState] for live color preview from the color picker. */
val LocalThemePreviewState =
    staticCompositionLocalOf<ThemePreviewState> { error("No ThemePreviewState provided") }
