package com.keisardev.moviesandbeyond.core.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Library action button with M3 Expressive press feedback animation. Features scale and subtle
 * rotation for delightful interaction feedback.
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
) {
    // Expressive press feedback state
    var isPressed by remember { mutableStateOf(false) }

    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium),
            label = "button_scale")

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
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
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
    LibraryActionButton(name = "Favorite", icon = Icons.Rounded.Favorite, onClick = {})
}
