package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

/**
 * A TIVI-style floating navigation bar with proper Surface, elevation, and gradient border.
 *
 * This navigation bar provides a modern floating appearance with:
 * - Blur effect applied to the bar itself (not the surrounding area)
 * - Transparent padding around the bar for true floating effect
 * - Gradient border from surfaceVariant to surfaceVariant @ 0.3 alpha
 * - ExtraLarge rounded shape
 * - 80dp height (TIVI-style)
 * - Accessible selectableGroup modifier
 *
 * @param hazeState The haze state for blur effect
 * @param modifier The modifier to be applied to the navigation bar
 * @param content The navigation items to display in the bar
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun FloatingNavigationBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    // Padding wrapper - this area is TRANSPARENT (no blur)
    Box(
        modifier =
            modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Surface with blur - ONLY the bar itself has blur
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            border =
                BorderStroke(
                    width = 0.5.dp,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    colorScheme.outlineVariant,
                                    colorScheme.outlineVariant.copy(alpha = 0.3f),
                                )
                        ),
                ),
            modifier =
                Modifier.clip(MaterialTheme.shapes.extraLarge)
                    .hazeEffect(
                        state = hazeState,
                        style = CupertinoMaterials.regular(colorScheme.surface),
                    ),
        ) {
            Row(
                modifier =
                    Modifier.padding(horizontal = 8.dp)
                        .fillMaxWidth()
                        .height(80.dp)
                        .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}

/**
 * An animated navigation item with spring scale animation for the floating navigation bar.
 *
 * This item provides:
 * - Spring scale animation (0.95 -> 1.0) when selected (TIVI-style)
 * - Crossfade animation between selected and unselected icons
 * - Proper NavigationBarItem colors for selected/unselected states
 *
 * @param selected Whether this item is currently selected
 * @param onClick Callback when the item is clicked
 * @param selectedIcon The composable to display when the item is selected
 * @param unselectedIcon The composable to display when the item is not selected
 * @param label The label to display for the item
 * @param modifier The modifier to be applied to the item
 */
@Composable
fun RowScope.AnimatedNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: @Composable () -> Unit,
    unselectedIcon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by
        animateFloatAsState(
            targetValue = if (selected) 1f else 0.95f,
            animationSpec =
                spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioLowBouncy,
                ),
            label = "nav_item_scale",
        )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Crossfade(targetState = selected, label = "icon_crossfade") { isSelected ->
                if (isSelected) {
                    selectedIcon()
                } else {
                    unselectedIcon()
                }
            }
        },
        label = label,
        modifier =
            modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors =
            NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            ),
    )
}
