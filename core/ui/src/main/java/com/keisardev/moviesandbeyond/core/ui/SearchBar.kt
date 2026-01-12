package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Search bar with M3 Expressive animations. Features animated clear button with scale-in/out and
 * expressive press feedback.
 */
@Suppress("LongMethod") // Compose UI component with animation state - naturally longer
@Composable
fun MoviesAndBeyondSearchBar(
    value: String,
    onQueryChange: (String) -> Unit,
) {
    // Expressive press state for clear button
    var isClearPressed by remember { mutableStateOf(false) }

    val clearScale by
        animateFloatAsState(
            targetValue = if (isClearPressed) 0.8f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh),
            label = "clear_scale")

    TextField(
        value = value,
        onValueChange = onQueryChange,
        shape = RoundedCornerShape(12.dp),
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            // Animated visibility for clear button with expressive enter/exit
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter =
                    scaleIn(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium)) + fadeIn(),
                exit =
                    scaleOut(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessHigh)) + fadeOut()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.clear),
                        modifier =
                            Modifier.graphicsLayer {
                                    scaleX = clearScale
                                    scaleY = clearScale
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isClearPressed = true
                                            tryAwaitRelease()
                                            isClearPressed = false
                                        },
                                        onTap = { onQueryChange("") })
                                })
                }
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(10.dp))
}

@Composable
@Preview
private fun MoviesAndBeyondSearchBarPreview() {
    MoviesAndBeyondSearchBar(value = "", onQueryChange = {})
}
