package com.keisardev.moviesandbeyond.feature.you

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
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
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.DARK
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.LIGHT
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.SYSTEM
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.ui.PersonImage
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.image.LandscapistImage

@Composable
fun YouRoute(
    navigateToAuth: () -> Unit,
    navigateToLibraryItem: (String) -> Unit,
    viewModel: YouViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val libraryItemCounts by viewModel.libraryItemCounts.collectAsStateWithLifecycle()
    val callbacks =
        YouScreenCallbacks(
            onChangeTheme = viewModel::setDynamicColorPreference,
            onChangeDarkMode = viewModel::setDarkModePreference,
            onChangeSeedColor = viewModel::setSeedColorPreference,
            onChangeCustomColorArgb = viewModel::setCustomColorArgb,
            onChangeIncludeAdult = viewModel::setAdultResultPreference,
            onChangeUseLocalOnly = viewModel::toggleUseLocalOnly,
            onNavigateToAuth = navigateToAuth,
            onLibraryItemClick = navigateToLibraryItem,
            onReloadAccountDetailsClick = viewModel::getAccountDetails,
            onRefresh = viewModel::onRefresh,
            onLogOutClick = viewModel::logOut,
            onErrorShown = viewModel::onErrorShown)
    YouScreen(
        uiState = uiState,
        isLoggedIn = isLoggedIn,
        userSettings = userSettings,
        libraryItemCounts = libraryItemCounts,
        callbacks = callbacks)
}

@Suppress("LongParameterList")
internal class YouScreenCallbacks(
    val onChangeTheme: (Boolean) -> Unit,
    val onChangeDarkMode: (SelectedDarkMode) -> Unit,
    val onChangeSeedColor: (SeedColor) -> Unit,
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
            onChangeSeedColor = callbacks.onChangeSeedColor,
            onChangeCustomColorArgb = callbacks.onChangeCustomColorArgb,
            onChangeIncludeAdult = callbacks.onChangeIncludeAdult,
            onChangeUseLocalOnly = callbacks.onChangeUseLocalOnly,
            onDismissRequest = { showSettingsDialog = !showSettingsDialog })
    }

    var showAttributionInfoDialog by rememberSaveable { mutableStateOf(false) }
    if (showAttributionInfoDialog) {
        AttributionInfoDialog(
            onDismissRequest = { showAttributionInfoDialog = !showAttributionInfoDialog })
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
                        onRefresh = callbacks.onRefresh)) {
                YouScreenContent(
                    uiState = uiState,
                    isLoggedIn = isLoggedIn,
                    libraryItemCounts = libraryItemCounts,
                    callbacks = callbacks)

                YouScreenActionButtons(
                    userSettings = userSettings,
                    onShowAttribution = { showAttributionInfoDialog = true },
                    onShowSettings = { showSettingsDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd))
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
                        onLogOutClick = callbacks.onLogOutClick)
                }
                    ?: LoadAccountDetails(
                        isLoading = uiState.isLoading,
                        onReloadAccountDetailsClick = callbacks.onReloadAccountDetailsClick)
            } else {
                LoggedOutView(
                    libraryItemCounts = libraryItemCounts,
                    onNavigateToAuth = callbacks.onNavigateToAuth,
                    onLibraryItemClick = callbacks.onLibraryItemClick)
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
                contentDescription = stringResource(id = R.string.attribution_info))
        }

        userSettings?.let {
            IconButton(onClick = onShowSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(id = R.string.settings_dialog_title))
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
    onLogOutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenPadding, vertical = Spacing.lg)) {
            // Profile header section with premium surface
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier =
                    Modifier.fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = MaterialTheme.shapes.extraLarge)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraLarge)
                        .padding(Spacing.lg)) {
                    PersonImage(
                        imageUrl = accountDetails.avatar ?: "",
                        modifier = Modifier.size(Dimens.profileLargeSize))
                    Text(
                        text = accountDetails.username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = accountDetails.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            // Library section
            LibrarySection(
                libraryItemCounts = libraryItemCounts, onLibraryItemClick = onLibraryItemClick)

            // Logout section
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = Spacing.md),
                    color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = onLogOutClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large) {
                        Text(
                            text = stringResource(id = R.string.log_out),
                            style = MaterialTheme.typography.labelLarge)
                    }
            }
        }
}

@Composable
private fun LoggedOutView(
    libraryItemCounts: LibraryItemCounts,
    onNavigateToAuth: () -> Unit,
    onLibraryItemClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenPadding, vertical = Spacing.lg)) {
            // Welcome card with premium surface
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.extraLarge)
                        .padding(Spacing.xl)) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.iconSizeLarge),
                        tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(id = R.string.log_in_welcome),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = stringResource(id = R.string.log_in_description),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = onNavigateToAuth,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large) {
                            Text(
                                text = stringResource(id = R.string.log_in),
                                style = MaterialTheme.typography.labelLarge)
                        }
                }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant)

            // Guest mode library section
            LibrarySection(
                libraryItemCounts = libraryItemCounts, onLibraryItemClick = onLibraryItemClick)
        }
}

@Composable
private fun LoadAccountDetails(isLoading: Boolean, onReloadAccountDetailsClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(Spacing.screenPadding)) {
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
                                shape = MaterialTheme.shapes.extraLarge)
                            .padding(Spacing.xl)) {
                        Text(
                            text = stringResource(id = R.string.reload_account_details),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                        Button(
                            onClick = onReloadAccountDetailsClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large) {
                                Text(
                                    text = stringResource(id = R.string.reload_account_details),
                                    style = MaterialTheme.typography.labelLarge)
                            }
                    }
            }
        }
}

@Composable
private fun LibrarySection(
    libraryItemCounts: LibraryItemCounts,
    onLibraryItemClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.your_library),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.xs))
            LibraryItemOption(
                optionName = stringResource(id = R.string.favorites),
                itemCount = libraryItemCounts.favoritesCount,
                onClick = { onLibraryItemClick(LibraryItemType.FAVORITE.name) })
            LibraryItemOption(
                optionName = stringResource(id = R.string.watchlist),
                itemCount = libraryItemCounts.watchlistCount,
                onClick = { onLibraryItemClick(LibraryItemType.WATCHLIST.name) })
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
                    shape = MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
            Text(
                text = optionName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    if (itemCount > 0) {
                        Text(
                            text = itemCount.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier.background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape)
                                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs))
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.iconSizeSmall))
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
                    style = MaterialTheme.typography.labelLarge)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxWidth()) {
                    LandscapistImage(
                        imageModel = { R.drawable.tmdb_logo },
                        imageOptions =
                            ImageOptions(
                                contentScale = ContentScale.Fit,
                                contentDescription =
                                    stringResource(id = R.string.tmdb_logo_description)),
                        modifier = Modifier.size(100.dp))
                    Text(
                        text = stringResource(id = R.string.attribution_text),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
        },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp)
}

@Composable
private fun SettingsDialog(
    userSettings: UserSettings?,
    onChangeTheme: (Boolean) -> Unit,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
    onChangeSeedColor: (SeedColor) -> Unit,
    onChangeCustomColorArgb: (Long) -> Unit,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    userSettings?.let {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = stringResource(R.string.settings_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SettingsPanel(
                        settings = userSettings,
                        onChangeTheme = onChangeTheme,
                        onChangeDarkMode = onChangeDarkMode,
                        onChangeSeedColor = onChangeSeedColor,
                        onChangeCustomColorArgb = onChangeCustomColorArgb,
                        onChangeIncludeAdult = onChangeIncludeAdult,
                        onChangeUseLocalOnly = onChangeUseLocalOnly)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest, shape = MaterialTheme.shapes.medium) {
                    Text(
                        text = stringResource(R.string.settings_dialog_dismiss_text),
                        style = MaterialTheme.typography.labelLarge)
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp)
    }
}

@Composable
private fun SettingsPanel(
    settings: UserSettings,
    onChangeTheme: (Boolean) -> Unit,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
    onChangeSeedColor: (SeedColor) -> Unit,
    onChangeCustomColorArgb: (Long) -> Unit,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
) {
    if (supportsDynamicColorTheme()) {
        SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_theme))
        Column(Modifier.selectableGroup()) {
            SettingsDialogChooserRow(
                text = stringResource(id = R.string.settings_dialog_theme_default),
                selected = !settings.useDynamicColor,
                onClick = { onChangeTheme(false) })
            SettingsDialogChooserRow(
                text = stringResource(id = R.string.settings_dialog_theme_dynamic),
                selected = settings.useDynamicColor,
                onClick = { onChangeTheme(true) })
        }
    }

    // Show seed color picker when dynamic color is disabled or not supported
    if (!settings.useDynamicColor || !supportsDynamicColorTheme()) {
        SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_seed_color))
        SeedColorPicker(
            selectedColor = settings.seedColor,
            customColorArgb = settings.customColorArgb,
            onColorSelected = onChangeSeedColor,
            onCustomColorChanged = onChangeCustomColorArgb,
            modifier = Modifier.padding(vertical = 8.dp))
    }

    SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_dark_mode))
    Column(Modifier.selectableGroup()) {
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_default),
            selected = settings.darkMode == SYSTEM,
            onClick = { onChangeDarkMode(SYSTEM) })
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_yes),
            selected = settings.darkMode == DARK,
            onClick = { onChangeDarkMode(DARK) })
        SettingsDialogChooserRow(
            text = stringResource(id = R.string.settings_dialog_dark_no),
            selected = settings.darkMode == LIGHT,
            onClick = { onChangeDarkMode(LIGHT) })
    }
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_adult))
            Switch(checked = settings.includeAdultResults, onCheckedChange = onChangeIncludeAdult)
        }
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                SettingsDialogSectionTitle(
                    text = stringResource(id = R.string.settings_dialog_local_only))
                Text(
                    text = stringResource(id = R.string.settings_dialog_local_only_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = settings.useLocalOnly, onCheckedChange = onChangeUseLocalOnly)
        }
}

/**
 * A composable that displays seed color options with an inline HSV color picker (NoteNest style).
 *
 * Features:
 * - Horizontal row of preset color options
 * - Custom color option as a radio button row (NoteNest pattern)
 * - Inline HSV color picker using AnimatedVisibility
 * - Live theme preview as user picks colors
 * - Submit/Cancel buttons to confirm or revert
 *
 * @param selectedColor The currently selected seed color
 * @param customColorArgb The custom color ARGB value
 * @param onColorSelected Callback invoked when a preset color is selected
 * @param onCustomColorChanged Callback invoked when the custom color is changed
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun SeedColorPicker(
    selectedColor: SeedColor,
    customColorArgb: Long,
    onColorSelected: (SeedColor) -> Unit,
    onCustomColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    val colorPickerController = rememberColorPickerController()
    // Store original color to restore on cancel
    val originalColorArgb = remember(showColorPicker) { customColorArgb }
    val isCustomSelected = selectedColor == SeedColor.CUSTOM

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Preset colors row (excluding CUSTOM)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)) {
                items(SeedColor.entries.filter { it != SeedColor.CUSTOM }) { seedColor ->
                    val isSelected = seedColor == selectedColor
                    Box(
                        modifier =
                            Modifier.size(48.dp)
                                .clip(CircleShape)
                                .background(Color(seedColor.argb))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = CircleShape)
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape)
                                    })
                                .clickable {
                                    showColorPicker = false
                                    onColorSelected(seedColor)
                                },
                        contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription =
                                        stringResource(id = R.string.seed_color_selected),
                                    tint = getContrastColor(Color(seedColor.argb)),
                                    modifier = Modifier.size(24.dp))
                            }
                        }
                }
            }

        // Custom color radio button row (NoteNest pattern)
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        color =
                            if (isCustomSelected)
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent)
                    .selectable(
                        selected = isCustomSelected,
                        role = Role.RadioButton,
                        onClick = {
                            onColorSelected(SeedColor.CUSTOM)
                            showColorPicker = true
                        })
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isCustomSelected, onClick = null)
                Spacer(Modifier.size(Spacing.xs))
                Text(
                    text = stringResource(id = R.string.custom_color_picker),
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        if (isCustomSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.weight(1f))
                // Color box preview with rounded corners
                Box(
                    modifier =
                        Modifier.size(32.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(color = Color(customColorArgb))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.small)
                            .clickable {
                                onColorSelected(SeedColor.CUSTOM)
                                showColorPicker = true
                            })
            }

        // Inline color picker (NoteNest style - appears below when Custom is selected)
        AnimatedVisibility(visible = showColorPicker && isCustomSelected) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large)
                        .padding(Spacing.md)) {
                    Text(
                        text = stringResource(id = R.string.pick_a_color),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)

                    HsvColorPicker(
                        modifier =
                            Modifier.fillMaxWidth().height(240.dp).padding(horizontal = Spacing.xs),
                        controller = colorPickerController,
                        onColorChanged = { colorEnvelope ->
                            // Live preview: update theme in real-time as user picks
                            onCustomColorChanged(colorEnvelope.color.value.toLong())
                        },
                        initialColor = Color(customColorArgb))

                    // Submit and Cancel buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Button(
                                onClick = { showColorPicker = false },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium) {
                                    Text(
                                        text = stringResource(id = R.string.submit),
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            TextButton(
                                onClick = {
                                    // Restore original color on cancel
                                    onCustomColorChanged(originalColorArgb)
                                    showColorPicker = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium) {
                                    Text(
                                        text = stringResource(id = R.string.cancel),
                                        style = MaterialTheme.typography.labelLarge)
                                }
                        }
                }
        }
    }
}

/** Returns a contrasting color (black or white) based on the luminance of the input color. */
private fun getContrastColor(color: Color): Color {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return if (luminance > 0.5) Color.Black else Color.White
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs))
}

@Composable
private fun SettingsDialogChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .background(
                color =
                    if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    else Color.Transparent)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface)
        }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicColorTheme() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
                errorMessage = null),
        isLoggedIn = true,
        userSettings =
            UserSettings(
                useDynamicColor = true,
                includeAdultResults = false,
                darkMode = SYSTEM,
                seedColor = SeedColor.DEFAULT,
                useLocalOnly = false,
                customColorArgb = SeedColor.DEFAULT_CUSTOM_COLOR_ARGB),
        libraryItemCounts = LibraryItemCounts(favoritesCount = 5, watchlistCount = 3),
        callbacks =
            YouScreenCallbacks(
                onChangeTheme = {},
                onChangeDarkMode = {},
                onChangeSeedColor = {},
                onChangeCustomColorArgb = {},
                onChangeIncludeAdult = {},
                onChangeUseLocalOnly = {},
                onNavigateToAuth = {},
                onLibraryItemClick = {},
                onReloadAccountDetailsClick = {},
                onRefresh = {},
                onLogOutClick = {},
                onErrorShown = {}))
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
                seedColor = SeedColor.BLUE,
                useLocalOnly = false,
                customColorArgb = SeedColor.DEFAULT_CUSTOM_COLOR_ARGB),
        onChangeTheme = {},
        onChangeDarkMode = {},
        onChangeSeedColor = {},
        onChangeCustomColorArgb = {},
        onChangeIncludeAdult = {},
        onChangeUseLocalOnly = {}) {}
}

@Preview(showBackground = true)
@Composable
private fun SeedColorPickerPreview() {
    SeedColorPicker(
        selectedColor = SeedColor.BLUE,
        customColorArgb = SeedColor.DEFAULT_CUSTOM_COLOR_ARGB,
        onColorSelected = {},
        onCustomColorChanged = {})
}
