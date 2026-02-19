package com.keisardev.moviesandbeyond.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.ui.AnnotatedClickableText
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import kotlinx.coroutines.delay

@Composable
fun AuthRoute(onBackClick: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        uiState = uiState,
        hideOnboarding = viewModel.hideOnboarding,
        onBackClick = onBackClick,
        onLogInClick = viewModel::logIn,
        onContinueWithoutSignInClick = viewModel::setHideOnboarding,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AuthScreen(
    uiState: AuthUiState,
    hideOnboarding: Boolean?,
    onBackClick: () -> Unit,
    onLogInClick: () -> Unit,
    onContinueWithoutSignInClick: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onErrorShown: () -> Unit,
) {
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onBackClick()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    var headlineVisible by rememberSaveable { mutableStateOf(false) }
    var descriptionVisible by rememberSaveable { mutableStateOf(false) }
    var usernameVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var signInButtonVisible by rememberSaveable { mutableStateOf(false) }
    var bottomSectionVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headlineVisible = true
        delay(100)
        descriptionVisible = true
        delay(50)
        usernameVisible = true
        delay(50)
        passwordVisible = true
        delay(100)
        signInButtonVisible = true
        delay(100)
        bottomSectionVisible = true
    }

    Scaffold(
        topBar = { TopAppBarWithBackButton(onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        AuthScreenContent(
            uiState = uiState,
            hideOnboarding = hideOnboarding,
            headlineVisible = headlineVisible,
            descriptionVisible = descriptionVisible,
            usernameVisible = usernameVisible,
            passwordVisible = passwordVisible,
            signInButtonVisible = signInButtonVisible,
            bottomSectionVisible = bottomSectionVisible,
            onLogInClick = onLogInClick,
            onContinueWithoutSignInClick = onContinueWithoutSignInClick,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun AuthScreenContent(
    uiState: AuthUiState,
    hideOnboarding: Boolean?,
    headlineVisible: Boolean,
    descriptionVisible: Boolean,
    usernameVisible: Boolean,
    passwordVisible: Boolean,
    signInButtonVisible: Boolean,
    bottomSectionVisible: Boolean,
    onLogInClick: () -> Unit,
    onContinueWithoutSignInClick: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        AuthWelcomeHeader(
            headlineVisible = headlineVisible,
            descriptionVisible = descriptionVisible,
        )
        Spacer(Modifier.height(32.dp))
        AuthFormFields(
            uiState = uiState,
            usernameVisible = usernameVisible,
            passwordVisible = passwordVisible,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
        )
        Spacer(Modifier.height(24.dp))
        AuthSignInSection(
            isLoading = uiState.isLoading,
            visible = signInButtonVisible,
            onLogInClick = {
                onLogInClick()
                focusManager.clearFocus()
            },
        )
        AuthBottomSection(
            visible = bottomSectionVisible,
            hideOnboarding = hideOnboarding,
            onContinueWithoutSignInClick = onContinueWithoutSignInClick,
        )
    }
}

@Composable
private fun AuthWelcomeHeader(headlineVisible: Boolean, descriptionVisible: Boolean) {
    val density = LocalDensity.current
    val headlineOffsetPx = with(density) { 16.dp.roundToPx() }

    AnimatedVisibility(
        visible = headlineVisible,
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
                    initialOffsetY = { headlineOffsetPx },
                ),
    ) {
        Text(
            text = stringResource(id = R.string.auth_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    Spacer(Modifier.height(8.dp))

    AnimatedVisibility(
        visible = descriptionVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
    ) {
        Text(
            text = stringResource(id = R.string.auth_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthFormFields(
    uiState: AuthUiState,
    usernameVisible: Boolean,
    passwordVisible: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    val density = LocalDensity.current
    val smallOffsetPx = with(density) { 8.dp.roundToPx() }

    AnimatedVisibility(
        visible = usernameVisible,
        enter =
            fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { smallOffsetPx },
                ),
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChange,
            placeholder = { Text(stringResource(id = R.string.username)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(Modifier.height(16.dp))

    AuthPasswordField(
        password = uiState.password,
        visible = passwordVisible,
        smallOffsetPx = smallOffsetPx,
        onPasswordChange = onPasswordChange,
    )
}

@Composable
private fun AuthPasswordField(
    password: String,
    visible: Boolean,
    smallOffsetPx: Int,
    onPasswordChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var passwordFieldVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { smallOffsetPx },
                ),
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text(stringResource(id = R.string.password)) },
            singleLine = true,
            visualTransformation =
                if (passwordFieldVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            trailingIcon = {
                IconButton(onClick = { passwordFieldVisible = !passwordFieldVisible }) {
                    Icon(
                        imageVector =
                            if (passwordFieldVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                        contentDescription =
                            stringResource(
                                id =
                                    if (passwordFieldVisible) R.string.hide_password
                                    else R.string.show_password
                            ),
                    )
                }
            },
            keyboardOptions =
                KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AuthSignInSection(isLoading: Boolean, visible: Boolean, onLogInClick: () -> Unit) {
    val density = LocalDensity.current
    val smallOffsetPx = with(density) { 8.dp.roundToPx() }

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
                    initialOffsetY = { smallOffsetPx * 2 },
                ),
    ) {
        if (isLoading) {
            val authIndicatorDescription =
                stringResource(id = R.string.auth_circular_progress_indicator)
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = authIndicatorDescription }
            )
        } else {
            AuthPrimaryButton(onClick = onLogInClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.sign_in),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun AuthBottomSection(
    visible: Boolean,
    hideOnboarding: Boolean?,
    onContinueWithoutSignInClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            hideOnboarding?.let {
                if (!it) {
                    Spacer(Modifier.height(16.dp))
                    AuthOrDivider()
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onContinueWithoutSignInClick,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.continue_without_sign_in),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            AuthSignUpLink()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthOrDivider() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(id = R.string.or),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthSignUpLink() {
    val uriHandler = LocalUriHandler.current
    val signUpAnnotatedClickableText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
            append(stringResource(id = R.string.no_account))
        }
        append(" ")
        pushStringAnnotation(tag = "URL", annotation = "https://www.themoviedb.org/signup")
        withStyle(
            style =
                SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        ) {
            append(stringResource(id = R.string.sign_up))
        }
        pop()
    }

    AnnotatedClickableText(
        attributionString = signUpAnnotatedClickableText,
        onClick = { offset ->
            signUpAnnotatedClickableText
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { uriHandler.openUri(it.item) }
        },
    )
}

@Composable
private fun AuthPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
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
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AuthScreen(
        uiState = AuthUiState(),
        hideOnboarding = false,
        onBackClick = {},
        onLogInClick = {},
        onContinueWithoutSignInClick = {},
        onUsernameChange = {},
        onPasswordChange = {},
        onErrorShown = {},
    )
}
