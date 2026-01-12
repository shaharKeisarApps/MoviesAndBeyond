package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

/**
 * Section header with expressive "See All" arrow animation. The arrow scales down and translates
 * right on press for delightful feedback.
 */
@Composable
fun ContentSectionHeader(
    sectionName: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Expressive press state for arrow
    var isArrowPressed by remember { mutableStateOf(false) }

    val arrowScale by
        animateFloatAsState(
            targetValue = if (isArrowPressed) 0.85f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh),
            label = "arrow_scale")

    val arrowTranslateX by
        animateFloatAsState(
            targetValue = if (isArrowPressed) 8f else 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh),
            label = "arrow_translate")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = sectionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold)

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = stringResource(id = R.string.see_all),
                modifier =
                    Modifier.graphicsLayer {
                            scaleX = arrowScale
                            scaleY = arrowScale
                            translationX = arrowTranslateX
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isArrowPressed = true
                                    tryAwaitRelease()
                                    isArrowPressed = false
                                },
                                onTap = { onSeeAllClick() })
                        })
        }
}
