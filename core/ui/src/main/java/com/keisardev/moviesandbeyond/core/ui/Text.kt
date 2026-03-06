package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Text composable that supports clickable [AnnotatedString] annotations (e.g., hyperlinks).
 *
 * @param attributionString The annotated string containing clickable spans
 * @param onClick Callback with the character offset that was tapped
 */
@Composable
fun AnnotatedClickableText(
    attributionString: AnnotatedString,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator =
        Modifier.pointerInput(onClick) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    onClick(layoutResult.getOffsetForPosition(pos))
                }
            }
        }

    Text(
        text = attributionString,
        onTextLayout = { layoutResult.value = it },
        modifier = modifier.then(pressIndicator),
    )
}

/** Single-line text with animated fade-in/out visibility. Used for collapsing top-bar titles. */
@Composable
fun AnimatedText(
    text: String,
    visible: Boolean,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
) {
    AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}
