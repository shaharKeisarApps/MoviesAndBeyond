package com.keisardev.moviesandbeyond.feature.you

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun YouRoute(
    navigateToAuth: () -> Unit,
    navigateToLibraryItem: (String) -> Unit,
    viewModel: YouViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    YouScreen(
        uiState = uiState,
        isLoggedIn = isLoggedIn,
        userSettings = userSettings,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun YouScreen(
    uiState: YouUiState,
    isLoggedIn: Boolean?,
    userSettings: UserSettings?,
    onChangeTheme: (Boolean) -> Unit,
    onChangeDarkMode: (SelectedDarkMode) -> Unit,
    onChangeSeedColor: (SeedColor) -> Unit,
    onChangeCustomColorArgb: (Long) -> Unit,
    onChangeIncludeAdult: (Boolean) -> Unit,
    onChangeUseLocalOnly: (Boolean) -> Unit,
    onNavigateToAuth: () -> Unit,
    onLibraryItemClick: (String) -> Unit,
    onReloadAccountDetailsClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onRefresh: () -> Unit,
    onErrorShown: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let {
        scope.launch { snackbarHostState.showSnackbar(it) }
        onErrorShown()
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSettingsDialog) {
        SettingsDialog(
            userSettings = userSettings,
            onChangeTheme = onChangeTheme,
            onChangeDarkMode = onChangeDarkMode,
            onChangeSeedColor = onChangeSeedColor,
            onChangeCustomColorArgb = onChangeCustomColorArgb,
            onChangeIncludeAdult = onChangeIncludeAdult,
            onChangeUseLocalOnly = onChangeUseLocalOnly,
            onDismissRequest = { showSettingsDialog = !showSettingsDialog })
    }

    var showAttributionInfoDialog by rememberSaveable { mutableStateOf(false) }
    if (showAttributionInfoDialog) {
        AttributionInfoDialog(
            onDismissRequest = { showAttributionInfoDialog = !showAttributionInfoDialog })
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(
            modifier =
                Modifier.padding(paddingValues)
                    .pullToRefresh(
                        isRefreshing = uiState.isRefreshing,
                        state = pullToRefreshState,
                        onRefresh = onRefresh)) {
                // Floating action buttons in top-right corner
                Row(
                    modifier =
                        Modifier.align(Alignment.TopEnd)
                            .padding(top = Spacing.md, end = Spacing.sm)) {
                        IconButton(onClick = { showAttributionInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = stringResource(id = R.string.attribution_info))
                        }

                        userSettings?.let {
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription =
                                        stringResource(id = R.string.settings_dialog_title))
                            }
                        }
                    }

                Column(Modifier.fillMaxSize()) {
                    isLoggedIn?.let {
                        if (isLoggedIn) {
                            uiState.accountDetails?.let {
                                LoggedInView(
                                    accountDetails = it,
                                    isLoggingOut = uiState.isLoggingOut,
                                    onLibraryItemClick = onLibraryItemClick,
                                    onLogOutClick = onLogOutClick)
                            }
                                ?: LoadAccountDetails(
                                    isLoading = uiState.isLoading,
                                    onReloadAccountDetailsClick = onReloadAccountDetailsClick)
                        } else {
                            LoggedOutView(onNavigateToAuth = onNavigateToAuth)
                        }
                    }
                }

                /*  PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                if (pullToRefreshState.isRefreshing) {
                    LaunchedEffect(true) { onRefresh() }
                }

                LaunchedEffect(uiState.isRefreshing) {
                    if (uiState.isRefreshing) {
                        pullToRefreshState.startRefresh()
                    } else {
                        pullToRefreshState.endRefresh()
                    }
                }*/
            }
    }
}

@Composable
private fun LoggedInView(
    accountDetails: AccountDetails,
    isLoggingOut: Boolean,
    onLibraryItemClick: (String) -> Unit,
    onLogOutClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.headerSpacing),
        modifier =
            Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
            PersonImage(imageUrl = accountDetails.avatar ?: "", modifier = Modifier.size(64.dp))
            Text(
                text = accountDetails.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(
                text = accountDetails.name,
                style = MaterialTheme.typography.titleMedium,
            )
            LibrarySection(onLibraryItemClick = onLibraryItemClick)

            if (isLoggingOut) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onLogOutClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.log_out))
                }
            }
        }
}

@Composable
private fun LoggedOutView(onNavigateToAuth: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier.fillMaxWidth(0.6f).padding(horizontal = 12.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp))
                Text(
                    text = stringResource(id = R.string.log_in_description),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onNavigateToAuth, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.log_in))
                }
            }
    }
}

@Composable
private fun LoadAccountDetails(isLoading: Boolean, onReloadAccountDetailsClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onReloadAccountDetailsClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.reload_account_details))
            }
        }
    }
}

@Composable
private fun LibrarySection(onLibraryItemClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.your_library),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        LibraryItemOption(
            optionName = stringResource(id = R.string.favorites),
            onClick = { onLibraryItemClick(LibraryItemType.FAVORITE.name) })
        LibraryItemOption(
            optionName = stringResource(id = R.string.watchlist),
            onClick = { onLibraryItemClick(LibraryItemType.WATCHLIST.name) })
    }
}

@Composable
private fun LibraryItemOption(optionName: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth().clickable(onClick = onClick).height(Dimens.listItemMinHeight)) {
            Text(text = optionName, style = MaterialTheme.typography.bodyLarge)
        }
}

@Composable
private fun AttributionInfoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Text(
                text = stringResource(R.string.settings_dialog_dismiss_text),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp).clickable { onDismissRequest() },
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                    LandscapistImage(
                        imageModel = { R.drawable.tmdb_logo },
                        imageOptions =
                            ImageOptions(
                                contentScale = ContentScale.Fit,
                                contentDescription =
                                    stringResource(id = R.string.tmdb_logo_description)),
                        modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.attribution_text),
                        textAlign = TextAlign.Center)
                }
        })
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
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                HorizontalDivider()
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                Text(
                    text = stringResource(R.string.settings_dialog_dismiss_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp).clickable { onDismissRequest() },
                )
            },
        )
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
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
        SettingsDialogSectionTitle(text = stringResource(id = R.string.settings_dialog_adult))
        Switch(checked = settings.includeAdultResults, onCheckedChange = onChangeIncludeAdult)
    }
    Column {
        Row(
            modifier = Modifier.fillMaxSize(),
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
}

/**
 * A composable that displays a horizontal row of seed color options for theme customization,
 * including preset colors and a custom color option with HSV picker.
 *
 * @param selectedColor The currently selected seed color
 * @param customColorArgb The custom color ARGB value
 * @param onColorSelected Callback invoked when a preset color is selected
 * @param onCustomColorChanged Callback invoked when the custom color is changed
 * @param modifier Modifier to be applied to the LazyRow
 */
@Composable
fun SeedColorPicker(
    selectedColor: SeedColor,
    customColorArgb: Long,
    onColorSelected: (SeedColor) -> Unit,
    onCustomColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPickerDialog by remember { mutableStateOf(false) }

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            initialColor = customColorArgb,
            onColorConfirmed = { newColor ->
                onCustomColorChanged(newColor)
                onColorSelected(SeedColor.CUSTOM)
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false })
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)) {
            // Preset colors (excluding CUSTOM)
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
                            .clickable { onColorSelected(seedColor) },
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

            // Custom color option
            item {
                val isCustomSelected = selectedColor == SeedColor.CUSTOM
                Box(
                    modifier =
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(Color(customColorArgb))
                            .then(
                                if (isCustomSelected) {
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
                            .clickable { showColorPickerDialog = true },
                    contentAlignment = Alignment.Center) {
                        if (isCustomSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription =
                                    stringResource(id = R.string.seed_color_selected),
                                tint = getContrastColor(Color(customColorArgb)),
                                modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription =
                                    stringResource(id = R.string.custom_color_picker),
                                tint = getContrastColor(Color(customColorArgb)),
                                modifier = Modifier.size(24.dp))
                        }
                    }
            }
        }
}

@Composable
private fun CustomColorPickerDialog(
    initialColor: Long,
    onColorConfirmed: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colorPickerController = rememberColorPickerController()
    var selectedColorArgb by remember { mutableLongStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.custom_color_picker)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HsvColorPicker(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        controller = colorPickerController,
                        onColorChanged = { colorEnvelope ->
                            selectedColorArgb = colorEnvelope.color.value.toLong()
                        })

                    // Color preview
                    Box(
                        modifier =
                            Modifier.size(64.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColorArgb))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape))
                }
        },
        confirmButton = {
            TextButton(onClick = { onColorConfirmed(selectedColorArgb) }) {
                Text(stringResource(id = R.string.submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        })
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
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsDialogChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(text)
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
        onErrorShown = {})
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
