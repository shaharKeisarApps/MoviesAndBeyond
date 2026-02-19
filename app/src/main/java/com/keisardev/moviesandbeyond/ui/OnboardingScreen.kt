package com.keisardev.moviesandbeyond.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.R
import kotlinx.coroutines.delay

const val onboardingNavigationRoute = "onboarding"

@Composable
fun OnboardingScreen(navigateToAuth: () -> Unit) {
    var iconVisible by rememberSaveable { mutableStateOf(false) }
    var titleVisible by rememberSaveable { mutableStateOf(false) }
    var subtitleVisible by rememberSaveable { mutableStateOf(false) }
    var buttonVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        iconVisible = true
        delay(200)
        titleVisible = true
        delay(150)
        subtitleVisible = true
        delay(200)
        buttonVisible = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    ) {
        Spacer(Modifier.height(200.dp))
        OnboardingIcon(visible = iconVisible)
        Spacer(Modifier.height(24.dp))
        OnboardingTitle(visible = titleVisible)
        Spacer(Modifier.height(8.dp))
        OnboardingSubtitle(visible = subtitleVisible)
        Spacer(Modifier.weight(1f))
        OnboardingGetStartedButton(visible = buttonVisible, onClick = navigateToAuth)
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun OnboardingIcon(visible: Boolean) {
    // Gentle floating animation after icon appears
    val infiniteTransition = rememberInfiniteTransition(label = "icon_float")
    val floatOffset by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 2000),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "icon_float_offset",
        )

    AnimatedVisibility(
        visible = visible,
        enter =
            scaleIn(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    )
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.graphicsLayer { translationY = floatOffset }
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Movie,
                contentDescription = stringResource(id = R.string.app_name),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun OnboardingTitle(visible: Boolean) {
    val density = LocalDensity.current
    val titleOffsetPx = with(density) { 24.dp.roundToPx() }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
            ) +
                slideInVertically(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    initialOffsetY = { titleOffsetPx },
                ),
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OnboardingSubtitle(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun OnboardingGetStartedButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    initialOffsetY = { it / 2 },
                ),
    ) {
        OnboardingButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun OnboardingButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "button_press_scale",
        )

    Button(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        interactionSource = interactionSource,
        modifier =
            modifier.height(56.dp).graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Text(
            text = stringResource(id = R.string.get_started),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(navigateToAuth = {})
}
