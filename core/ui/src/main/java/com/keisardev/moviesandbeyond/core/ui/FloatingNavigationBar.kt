package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * A TIVI-style floating navigation bar with a transparent background and gradient border.
 *
 * This navigation bar provides a modern floating appearance with:
 * - Transparent background
 * - Gradient border from surfaceVariant to surfaceVariant @ 0.3 alpha
 * - ExtraLarge rounded shape
 * - 80dp height
 *
 * @param modifier The modifier to be applied to the navigation bar
 * @param content The navigation items to display in the bar
 */
@Composable
fun FloatingNavigationBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier =
            modifier
                .padding(horizontal = 8.dp)
                .height(80.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(Color.Transparent)
                .border(
                    width = 0.5.dp,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    colorScheme.surfaceVariant,
                                    colorScheme.surfaceVariant.copy(alpha = 0.3f))),
                    shape = MaterialTheme.shapes.extraLarge),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        content = content)
}

/**
 * An animated navigation item with spring scale animation for the floating navigation bar.
 *
 * This item provides:
 * - Spring scale animation (1.0 -> 1.1) when selected
 * - Crossfade animation between selected and unselected icons
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
    modifier: Modifier = Modifier
) {
    val scale by
        animateFloatAsState(
            targetValue = if (selected) 1.1f else 1f,
            animationSpec =
                spring(
                    stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "nav_item_scale")

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier =
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }) {
                    Crossfade(targetState = selected, label = "icon_crossfade") { isSelected ->
                        if (isSelected) {
                            selectedIcon()
                        } else {
                            unselectedIcon()
                        }
                    }
                }
        },
        label = label,
        modifier = modifier)
}
