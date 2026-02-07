package com.keisardev.moviesandbeyond.feature.you.library_items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.ui.LazyVerticalContentGrid
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.MediaType as SharedMediaType
import com.keisardev.moviesandbeyond.core.ui.SharedElementOrigin
import com.keisardev.moviesandbeyond.core.ui.SharedElementType
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.keisardev.moviesandbeyond.feature.you.R
import kotlinx.coroutines.launch

@Composable
fun LibraryItemsRoute(
    onBackClick: () -> Unit,
    navigateToDetails: (String) -> Unit,
    viewModel: LibraryItemsViewModel = hiltViewModel(),
    libraryItemType: String? = null
) {
    // For Navigation 3: Set the type from the route key
    libraryItemType?.let { viewModel.setLibraryItemType(it) }

    val movieItems by viewModel.movieItems.collectAsStateWithLifecycle()
    val tvItems by viewModel.tvItems.collectAsStateWithLifecycle()
    val libraryItemTypeValue by viewModel.libraryItemType.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LibraryItemsScreen(
        movieItems = movieItems,
        tvItems = tvItems,
        libraryItemType = libraryItemTypeValue,
        errorMessage = errorMessage,
        onDeleteItem = viewModel::deleteItem,
        onBackClick = onBackClick,
        onItemClick = navigateToDetails,
        onErrorShown = viewModel::onErrorShown)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun LibraryItemsScreen(
    movieItems: List<LibraryItem>,
    tvItems: List<LibraryItem>,
    libraryItemType: LibraryItemType?,
    errorMessage: String?,
    onDeleteItem: (LibraryItem) -> Unit,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onErrorShown: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    errorMessage?.let {
        scope.launch { snackbarHostState.showSnackbar(it) }
        onErrorShown()
    }

    val libraryItemTitle =
        when (libraryItemType) {
            LibraryItemType.FAVORITE -> stringResource(id = R.string.favorites)
            LibraryItemType.WATCHLIST -> stringResource(id = R.string.watchlist)
            else -> null
        }

    Scaffold(
        topBar = {
            TopAppBarWithBackButton(
                title = { libraryItemTitle?.let { Text(text = it) } }, onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                val libraryMediaTabs = LibraryMediaType.entries
                val pagerState = rememberPagerState(pageCount = { libraryMediaTabs.size })

                val selectedTabIndex by
                    remember(pagerState.currentPage) { mutableIntStateOf(pagerState.currentPage) }

                // Premium M3 PrimaryTabRow with surface container and icons
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier =
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurface) {
                        libraryMediaTabs.forEachIndexed { index, mediaTypeTab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                                icon = {
                                    Icon(
                                        imageVector =
                                            when (mediaTypeTab) {
                                                LibraryMediaType.MOVIE -> Icons.Rounded.Movie
                                                LibraryMediaType.TV -> Icons.Rounded.Tv
                                            },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp))
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = mediaTypeTab.displayName),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight =
                                            if (selectedTabIndex == index) FontWeight.SemiBold
                                            else FontWeight.Normal)
                                })
                        }
                    }

                Spacer(Modifier.height(Spacing.xs))

                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    Column(Modifier.fillMaxSize()) {
                        when (libraryMediaTabs[page]) {
                            LibraryMediaType.MOVIE -> {
                                LibraryContent(
                                    content = movieItems,
                                    libraryItemType = libraryItemType,
                                    onItemClick = onItemClick,
                                    onDeleteClick = onDeleteItem)
                            }

                            LibraryMediaType.TV -> {
                                LibraryContent(
                                    content = tvItems,
                                    libraryItemType = libraryItemType,
                                    onItemClick = onItemClick,
                                    onDeleteClick = onDeleteItem)
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun LibraryContent(
    content: List<LibraryItem>,
    libraryItemType: LibraryItemType?,
    onItemClick: (String) -> Unit,
    onDeleteClick: (LibraryItem) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        if (content.isEmpty()) {
            // Premium empty state with surface container and animations
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(100)
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter =
                    fadeIn(
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow)),
                modifier = Modifier.align(Alignment.Center)) {
                    Column(
                        modifier =
                            Modifier.padding(Spacing.xl)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = MaterialTheme.shapes.extraLarge)
                                .padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            // Icon with primary color accent and subtle pulse animation
                            val iconScale by
                                animateFloatAsState(
                                    targetValue = if (isVisible) 1f else 0.8f,
                                    animationSpec =
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow),
                                    label = "icon_scale")

                            Box(
                                modifier =
                                    Modifier.size(120.dp)
                                        .scale(iconScale)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = CircleShape),
                                contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector =
                                            if (libraryItemType == LibraryItemType.FAVORITE) {
                                                Icons.Rounded.Favorite
                                            } else {
                                                Icons.Rounded.Bookmark
                                            },
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimens.profileMediumSize),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }

                            Text(
                                text = stringResource(id = R.string.no_items),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)

                            Text(
                                text = stringResource(id = R.string.no_items_description),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center)
                        }
                }
        } else {
            LazyVerticalContentGrid(
                pagingEnabled = false,
                contentPadding =
                    PaddingValues(
                        start = Spacing.screenPadding,
                        end = Spacing.screenPadding,
                        top = Spacing.md,
                        bottom = Spacing.xl),
                itemSpacing = Spacing.md) {
                    items(items = content, key = { it.id }) { item ->
                        // Convert string media type to SharedMediaType for shared elements
                        val sharedMediaType =
                            when (item.mediaType.lowercase()) {
                                "movie" -> SharedMediaType.Movie
                                "tv" -> SharedMediaType.TvShow
                                else -> null
                            }

                        // Staggered entrance animation
                        val itemIndex = content.indexOf(item)
                        var isVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(itemIndex * 50L)
                            isVisible = true
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter =
                                fadeIn(
                                    animationSpec =
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow)) +
                                    scaleIn(
                                        initialScale = 0.8f,
                                        animationSpec =
                                            spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow)),
                            modifier = Modifier.animateItem()) {
                                LibraryItemCard(
                                    posterPath = item.imagePath,
                                    sharedElementKey =
                                        sharedMediaType?.let {
                                            MediaSharedElementKey(
                                                mediaId = item.id.toLong(),
                                                mediaType = it,
                                                origin = SharedElementOrigin.LIBRARY,
                                                elementType = SharedElementType.Image)
                                        },
                                    onItemClick = {
                                        onItemClick("${item.id},${item.mediaType.uppercase()}")
                                    },
                                    onDeleteClick = { onDeleteClick(item) })
                            }
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryItemCard(
    posterPath: String,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemVisible by remember { mutableStateOf(true) }
    val hapticFeedback = LocalHapticFeedback.current

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = true
                        false
                    }
                    SwipeToDismissBoxValue.Settled -> false
                }
            })

    LaunchedEffect(showDeleteDialog) {
        if (!showDeleteDialog && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                itemVisible = false
                onDeleteClick()
            },
            onDismiss = { showDeleteDialog = false })
    }

    AnimatedVisibility(
        visible = itemVisible,
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))) {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = { SwipeDeleteBackground(dismissState = dismissState) }) {
                    LibraryItemCardContent(
                        posterPath = posterPath,
                        sharedElementKey = sharedElementKey,
                        onItemClick = onItemClick,
                        onDeleteClick = { showDeleteDialog = true })
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDeleteBackground(dismissState: SwipeToDismissBoxState) {
    val backgroundColor by
        animateFloatAsState(
            targetValue =
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart -> 1f
                    else -> 0f
                },
            animationSpec = spring(),
            label = "background_alpha")

    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = backgroundColor),
                    shape = RoundedCornerShape(12.dp))
                .padding(horizontal = Spacing.md),
        contentAlignment =
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.delete),
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(Dimens.iconSizeMedium))
        }
}

@Composable
private fun LibraryItemCardContent(
    posterPath: String,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box {
        MediaItemCard(
            posterPath = posterPath, sharedElementKey = sharedElementKey, onItemClick = onItemClick)

        IconButton(
            onClick = onDeleteClick,
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer),
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(Spacing.xxs)
                    .size(32.dp)
                    .clip(CircleShape)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    modifier = Modifier.size(Dimens.iconSizeSmall))
            }
    }
}

/** Material 3 confirmation dialog for destructive delete action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.iconSize))

                    // Title
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold)

                    // Message
                    Text(
                        text = "Are you sure you want to remove this item from your library?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(id = R.string.cancel))
                            }

                            Spacer(Modifier.size(Spacing.xs))

                            TextButton(onClick = onConfirm) {
                                Text(
                                    stringResource(id = R.string.delete),
                                    color = MaterialTheme.colorScheme.error)
                            }
                        }
                }
        }
    }
}
