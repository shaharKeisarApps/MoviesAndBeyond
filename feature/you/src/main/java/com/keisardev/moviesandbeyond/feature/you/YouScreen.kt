package com.keisardev.moviesandbeyond.feature.you

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.DARK
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.LIGHT
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.SYSTEM
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.ui.LocalThemePreviewState
import com.keisardev.moviesandbeyond.core.ui.PersonImage
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.image.LandscapistImage

@Composable
fun YouRoute(
    navigateToAuth: () -> Unit,
    navigateToLibraryItem: (String) -> Unit,
    viewModel: YouViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val libraryItemCounts by viewModel.libraryItemCounts.collectAsStateWithLifecycle()
    val callbacks =
        YouScreenCallbacks(
            onChangeTheme = viewModel::setDynamicColorPreference,
            onChangeDarkMode = viewModel::setDarkModePreference,
            onChangeCustomColorArgb = viewModel::setCustomColorArgb,
            onChangeIncludeAdult = viewModel::setAdultResultPreference,
            onChangeUseLocalOnly = viewModel::toggleUseLocalOnly,
            onNavigateToAuth = navigateToAuth,
            onLibraryItemClick = navigateToLibraryItem,
            onReloadAccountDetailsClick = viewModel::getAccountDetails,
            onRefresh = viewModel::onRefresh,
            onLogOutClick = viewModel::logOut,
            onErrorShown = viewModel::onErrorShown,
        )
    YouScreen(
        uiState = uiState,
        isLoggedIn = isLoggedIn,
        userSettings = userSettings,
        libraryItemCounts = libraryItemCounts,
        callbacks = callbacks,
    )
}

@Suppress("LongParameterList")
internal class YouScreenCallbacks(
    val onChangeTheme: (Boolean) -> Unit,
    val onChangeDarkMode: (SelectedDarkMode) -> Unit,
    val onChangeCustomColorArgb: (Long) -> Unit,
    val onChangeIncludeAdult: (Boolean) -> Unit,
    val onChangeUseLocalOnly: (Boolean) -> Unit,
    val onNavigateToAuth: () -> Unit,
    val onLibraryItemClick: (String) -> Unit,
    val onReloadAccountDetailsClick: () -> Unit,
    val onLogOutClick: () -> Unit,
    val onRefresh: () -> Unit,
    val onErrorShown: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YouScreen(
    uiState: YouUiState,
    isLoggedIn: Boolean?,
    userSettings: UserSettings?,
    libraryItemCounts: LibraryItemCounts,
    callbacks: YouScreenCallbacks,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            callbacks.onErrorShown()
        }
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSettingsDialog) {
        SettingsDialog(
            userSettings = userSettings,
            onChangeTheme = callbacks.onChangeTheme,
            onChangeDarkMode = callbacks.onChangeDarkMode,
            onChangeCustomColorArgb = callbacks.onChangeCustomColorArgb,
            onChangeIncludeAdult = callbacks.onChangeIncludeAdult,
            onChangeUseLocalOnly = callbacks.onChangeUseLocalOnly,
            onDismissRequest = { showSettingsDialog = !showSettingsDialog },
        )
    }

    var showAttributionInfoDialog by rememberSaveable { mutableStateOf(false) }
    if (showAttributionInfoDialog) {
        AttributionInfoDialog(
            onDismissRequest = { showAttributionInfoDialog = !showAttributionInfoDialog }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier.padding(paddingValues)
                    .pullToRefresh(
                        isRefreshing = uiState.isRefreshing,
                        state = pullToRefreshState,
                        onRefresh = callbacks.onRefresh,
                    )
        ) {
            YouScreenContent(
                uiState = uiState,
                isLoggedIn = isLoggedIn,
                libraryItemCounts = libraryItemCounts,
                callbacks = callbacks,
            )

            YouScreenActionButtons(
                userSettings = userSettings,
                onShowAttribution = { showAttributionInfoDialog = true },
                onShowSettings = { showSettingsDialog = true },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun YouScreenContent(
    uiState: YouUiState,
    isLoggedIn: Boolean?,
    libraryItemCounts: LibraryItemCounts,
    callbacks: YouScreenCallbacks,
) {
    Column(Modifier.fillMaxSize()) {
        isLoggedIn?.let {
            if (isLoggedIn) {
                uiState.accountDetails?.let {
                    LoggedInView(
                        accountDetails = it,
                        isLoggingOut = uiState.isLoggingOut,
                        libraryItemCounts = libraryItemCounts,
                        onLibraryItemClick = callbacks.onLibraryItemClick,
                        onLogOutClick = callbacks.onLogOutClick,
                    )
                }
                    ?: LoadAccountDetails(
                        isLoading = uiState.isLoading,
                        onReloadAccountDetailsClick = callbacks.onReloadAccountDetailsClick,
                    )
            } else {
                LoggedOutView(
                    libraryItemCounts = libraryItemCounts,
                    onNavigateToAuth = callbacks.onNavigateToAuth,
                    onLibraryItemClick = callbacks.onLibraryItemClick,
                )
            }
        }
    }
}

@Composable
private fun YouScreenActionButtons(
    userSettings: UserSettings?,
    onShowAttribution: () -> Unit,
    onShowSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(top = Spacing.xl, end = Spacing.lg)) {
        IconButton(onClick = onShowAttribution) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = stringResource(id = R.string.attribution_info),
            )
        }

        userSettings?.let {
            IconButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(id = R.string.settings_dialog_title),
                )
            }
        }
    }
}

@Composable
private fun LoggedInView(
    accountDetails: AccountDetails,
    isLoggingOut: Boolean,
    libraryItemCounts: LibraryItemCounts,
    onLibraryItemClick: (String) -> Unit,
    onLogOutClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenPadding, vertical = Spacing.lg),
    ) {
        // Profile header section with premium surface
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier =
                Modifier.fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    .padding(Spacing.lg),
        ) {
            PersonImage(
                imageUrl = accountDetails.avatar ?: "",
                modifier = Modifier.size(Dimens.profileLargeSize),
            )
            Text(
                text = accountDetails.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = accountDetails.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Library section
        LibrarySection(
            libraryItemCounts = libraryItemCounts,
            onLibraryItemClick = onLibraryItemClick,
        )

        // Logout section
        if (isLoggingOut) {
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = Spacing.md),
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            OutlinedButton(
                onClick = onLogOutClick,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = stringResource(id = R.string.log_out),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun LoggedOutView(
    libraryItemCounts: LibraryItemCounts,
    onNavigateToAuth: () -> Unit,
    onLibraryItemClick: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenPadding, vertical = Spacing.lg),
    ) {
        // Welcome card with premium surface
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier =
                Modifier.fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                    .padding(Spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSizeLarge),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(id = R.string.log_in_welcome),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(id = R.string.log_in_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onNavigateToAuth,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = stringResource(id = R.string.log_in),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = Spacing.sm),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // Guest mode library section
        LibrarySection(
            libraryItemCounts = libraryItemCounts,
            onLibraryItemClick = onLibraryItemClick,
        )
    }
}

@Composable
private fun LoadAccountDetails(isLoading: Boolean, onReloadAccountDetailsClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(Spacing.screenPadding),
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(Spacing.xl),
            ) {
                Text(
                    text = stringResource(id = R.string.reload_account_details),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onReloadAccountDetailsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = stringResource(id = R.string.reload_account_details),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(
    libraryItemCounts: LibraryItemCounts,
    onLibraryItemClick: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(id = R.string.your_library),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Spacing.xs),
        )
        LibraryItemOption(
            optionName = stringResource(id = R.string.favorites),
            itemCount = libraryItemCounts.favoritesCount,
            onClick = { onLibraryItemClick(LibraryItemType.FAVORITE.name) },
        )
        LibraryItemOption(
            optionName = stringResource(id = R.string.watchlist),
            itemCount = libraryItemCounts.watchlistCount,
            onClick = { onLibraryItemClick(LibraryItemType.WATCHLIST.name) },
        )
    }
}

@Composable
private fun LibraryItemOption(optionName: String, itemCount: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.large,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        Text(
            text = optionName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (itemCount > 0) {
                Text(
                    text = itemCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                            )
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeSmall),
            )
        }
    }
}

@Composable
private fun AttributionInfoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest, shape = MaterialTheme.shapes.medium) {
                Text(
                    text = stringResource(R.string.settings_dialog_dismiss_text),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                LandscapistImage(
                    imageModel = { R.drawable.tmdb_logo },
                    imageOptions =
                        ImageOptions(
                            contentScale = ContentScale.Fit,
                            contentDescription = stringResource(id = R.string.tmdb_logo_description),
                        ),
                    modifier = Modifier.size(100.dp),
                )
                Text(
                    text = stringResource(id = R.string.attribution_text),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
    )
}

@Composable
private fun SettingsDialog(
    userSettings: UserSettings?,
    onChangeTheme: (Boolean) -> Unit,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
    onChangeCustomColorArgb: (Long) -> Unit,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    userSettings?.let {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = stringResource(R.string.settings_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SettingsPanel(
                        settings = userSettings,
                        onChangeTheme = onChangeTheme,
                        onChangeDarkMode = onChangeDarkMode,
                        onChangeCustomColorArgb = onChangeCustomColorArgb,
                        onChangeIncludeAdult = onChangeIncludeAdult,
                        onChangeUseLocalOnly = onChangeUseLocalOnly,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest, shape = MaterialTheme.shapes.medium) {
                    Text(
                        text = stringResource(R.string.settings_dialog_dismiss_text),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
        )
    }
}

@Composable
private fun SettingsPanel(
    settings: UserSettings,
    onChangeTheme: (Boolean) -> Unit,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
    onChangeCustomColorArgb: (Long) -> Unit,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val isSeedColorMode = !settings.useDynamicColor

    SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_theme))
    Column(Modifier.selectableGroup()) {
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_theme_dynamic),
            selected = settings.useDynamicColor,
            onClick = {
                onChangeTheme(true)
                showColorPicker = false
            },
        )
        SeedColorChooserRow(
            selected = isSeedColorMode,
            customColorArgb = settings.customColorArgb,
            onClick = {
                onChangeTheme(false)
                showColorPicker = true
            },
            onSwatchClick = {
                onChangeTheme(false)
                showColorPicker = !showColorPicker
            },
        )
    }

    // Inline HSV color picker — appears when Seed Color is active
    AnimatedVisibility(visible = isSeedColorMode && showColorPicker) {
        InlineColorPicker(
            customColorArgb = settings.customColorArgb,
            onCustomColorChanged = onChangeCustomColorArgb,
            onDismiss = { showColorPicker = false },
        )
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    DarkModeSection(darkMode = settings.darkMode, onChangeDarkMode = onChangeDarkMode)
    Spacer(modifier = Modifier.height(Spacing.sm))
    SettingsToggles(
        includeAdultResults = settings.includeAdultResults,
        useLocalOnly = settings.useLocalOnly,
        onChangeIncludeAdult = onChangeIncludeAdult,
        onChangeUseLocalOnly = onChangeUseLocalOnly,
    )
}

/** Dark mode radio group: System / Dark / Light. */
@Composable
private fun DarkModeSection(
    darkMode: SelectedDarkMode,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
) {
    SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_dark_mode))
    Column(Modifier.selectableGroup()) {
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_default),
            selected = darkMode == SYSTEM,
            onClick = { onChangeDarkMode(SYSTEM) },
        )
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_yes),
            selected = darkMode == DARK,
            onClick = { onChangeDarkMode(DARK) },
        )
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_no),
            selected = darkMode == LIGHT,
            onClick = { onChangeDarkMode(LIGHT) },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** Adult results and local-only toggle switches. */
@Composable
private fun SettingsToggles(
    includeAdultResults: Boolean,
    useLocalOnly: Boolean,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_adult))
            Switch(checked = includeAdultResults, onCheckedChange = onChangeIncludeAdult)
        }
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SettingsDialogSectionTitle(
                    text = stringResource(id = R.string.settings_dialog_local_only)
                )
                Text(
                    text = stringResource(id = R.string.settings_dialog_local_only_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = useLocalOnly, onCheckedChange = onChangeUseLocalOnly)
        }
    }
}

/** "Seed Color" radio row with a color swatch preview on the right side. */
@Composable
private fun SeedColorChooserRow(
    selected: Boolean,
    customColorArgb: Long,
    onClick: () -> Unit,
    onSwatchClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(
                color =
                    if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else Color.Transparent
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = stringResource(id = R.string.settings_dialog_theme_seed),
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier =
                Modifier.size(28.dp)
                    .clip(CircleShape)
                    .background(Color(customColorArgb))
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onSwatchClick)
        )
    }
}

/** Inline HSV color picker with live preview via [LocalThemePreviewState]. */
@Composable
private fun InlineColorPicker(
    customColorArgb: Long,
    onCustomColorChanged: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val colorPickerController = rememberColorPickerController()
    val themePreview = LocalThemePreviewState.current

    // Clear preview when this composable leaves composition (dialog dismissed, etc.)
    DisposableEffect(Unit) { onDispose { themePreview.previewColorArgb = null } }

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = Spacing.xs)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = MaterialTheme.shapes.large,
                )
                .padding(Spacing.md),
    ) {
        Text(
            text = stringResource(id = R.string.pick_a_color),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        HsvColorPicker(
            modifier = Modifier.fillMaxWidth().height(240.dp).padding(horizontal = Spacing.xs),
            controller = colorPickerController,
            onColorChanged = { colorEnvelope ->
                // Live preview only — no DataStore write
                themePreview.previewColorArgb =
                    colorEnvelope.color.toArgb().toLong() and 0xFFFFFFFFL
            },
            initialColor = Color(customColorArgb),
        )

        ColorPickerActions(
            onSubmit = {
                themePreview.previewColorArgb?.let { onCustomColorChanged(it) }
                themePreview.previewColorArgb = null
                onDismiss()
            },
            onCancel = {
                themePreview.previewColorArgb = null
                onDismiss()
            },
        )
    }
}

/** Submit and Cancel buttons for the color picker. */
@Composable
private fun ColorPickerActions(onSubmit: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(id = R.string.submit),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(id = R.string.cancel),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs),
    )
}

@Composable
private fun SettingsDialogChooserRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(
                color =
                    if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else Color.Transparent
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YouScreenPreview() {
    YouScreen(
        uiState =
            YouUiState(
                accountDetails =
                    AccountDetails(
                        id = 1,
                        name = "John Doe",
                        username = "johndoe",
                        avatar = "",
                        includeAdult = false,
                        gravatar = "null",
                        iso6391 = "null",
                        iso31661 = "null",
                    ),
                isLoading = false,
                isRefreshing = false,
                isLoggingOut = false,
                errorMessage = null,
            ),
        isLoggedIn = true,
        userSettings =
            UserSettings(
                useDynamicColor = true,
                includeAdultResults = false,
                darkMode = SYSTEM,
                useLocalOnly = false,
                customColorArgb = 0xFF6750A4,
            ),
        libraryItemCounts = LibraryItemCounts(favoritesCount = 5, watchlistCount = 3),
        callbacks =
            YouScreenCallbacks(
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {},
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsDialogPreview() {
    SettingsDialog(
        userSettings =
            UserSettings(
                useDynamicColor = false,
                includeAdultResults = true,
                darkMode = SYSTEM,
                useLocalOnly = false,
                customColorArgb = 0xFF1976D2,
            ),
        onChangeTheme = {},
        onChangeDarkMode = {},
        onChangeCustomColorArgb = {},
        onChangeIncludeAdult = {},
        onChangeUseLocalOnly = {},
    ) {}
}
