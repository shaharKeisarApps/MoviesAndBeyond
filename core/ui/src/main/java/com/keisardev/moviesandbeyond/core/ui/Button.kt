package com.keisardev.moviesandbeyond.core.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Triggers bounce animation and haptic feedback when the active state changes.
 *
 * @param isActive Current active state of the button
 * @param isAnimating Mutable state to control animation
 * @param previousActiveState Mutable state tracking the previous active state
 * @param view The Android View for haptic feedback
 */
@Composable
private fun BounceAnimationEffect(
    isActive: Boolean,
    isAnimating: MutableState<Boolean>,
    previousActiveState: MutableState<Boolean>,
    view: View
) {
    LaunchedEffect(isActive) {
        if (previousActiveState.value != isActive) {
            isAnimating.value = true
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            previousActiveState.value = isActive
        }
    }
}

/** Creates a spring animation spec for button animations. */
private fun buttonSpringSpec() =
    spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

/**
 * Library action button with delightful bounce animation and haptic feedback. Features:
 * - Scale bounce animation (1.0 -> 1.3 -> 1.0) when toggling favorites
 * - Color transition animation for the icon
 * - Haptic feedback on toggle
 * - M3 Expressive press feedback animation
 */
@Composable
fun LibraryActionButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    iconTint: Color = LocalContentColor.current,
    border: BorderStroke? = null,
    name: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    activeIconTint: Color = Color.Red,
    inactiveIconTint: Color = Color.Gray,
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    val isAnimating = remember { mutableStateOf(false) }
    val previousActiveState = remember { mutableStateOf(isActive) }

    BounceAnimationEffect(isActive, isAnimating, previousActiveState, view)

    val bounceScale by animateBounceScale(isAnimating)
    val pressScale by animatePressScale(isPressed)
    val animatedIconTint by animateIconColor(iconTint, isActive, activeIconTint, inactiveIconTint)

    LibraryActionButtonContent(
        modifier = modifier,
        colors = colors,
        border = border,
        name = name,
        icon = icon,
        iconTint = animatedIconTint,
        scale = pressScale * bounceScale,
        onClick = onClick,
        onPressChanged = { isPressed = it })
}

@Composable
private fun animateBounceScale(isAnimating: MutableState<Boolean>): State<Float> =
    animateFloatAsState(
        targetValue = if (isAnimating.value) 1.3f else 1f,
        animationSpec = buttonSpringSpec(),
        finishedListener = { isAnimating.value = false },
        label = "bounce_scale")

@Composable
private fun animatePressScale(isPressed: Boolean): State<Float> =
    animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = buttonSpringSpec(),
        label = "press_scale")

@Composable
private fun animateIconColor(
    iconTint: Color,
    isActive: Boolean,
    activeIconTint: Color,
    inactiveIconTint: Color
): State<Color> {
    val targetColor =
        when {
            iconTint != LocalContentColor.current -> iconTint
            isActive -> activeIconTint
            else -> inactiveIconTint
        }
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "icon_color")
}

@Composable
private fun LibraryActionButtonContent(
    modifier: Modifier,
    colors: ButtonColors,
    border: BorderStroke?,
    name: String,
    icon: ImageVector,
    iconTint: Color,
    scale: Float,
    onClick: () -> Unit,
    onPressChanged: (Boolean) -> Unit
) {
    Button(
        onClick = {},
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        border = border,
        interactionSource = remember { MutableInteractionSource() },
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPressChanged(true)
                            tryAwaitRelease()
                            onPressChanged(false)
                        },
                        onTap = { onClick() })
                }) {
            Icon(imageVector = icon, contentDescription = name, tint = iconTint)
            Spacer(Modifier.width(4.dp))
            Text(text = name, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
}

@Preview(showBackground = true)
@Composable
private fun LibraryActionButtonPreview() {
    LibraryActionButton(
        name = "Favorite", icon = Icons.Rounded.Favorite, onClick = {}, isActive = false)
}

@Preview(showBackground = true)
@Composable
private fun LibraryActionButtonActivePreview() {
    LibraryActionButton(
        name = "Remove from Favorites",
        icon = Icons.Rounded.Favorite,
        onClick = {},
        isActive = true)
}
