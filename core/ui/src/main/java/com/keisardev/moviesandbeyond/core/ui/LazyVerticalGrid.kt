package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerGridCard
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens

/**
 * A lazy vertical grid optimized for content display with equal sizing and spacing.
 *
 * Uses [GridCells.Adaptive] for responsive layouts that maintain equal item widths across different
 * screen sizes. Items automatically adjust to fill available space while respecting the minimum
 * cell width.
 *
 * Features:
 * - Adaptive columns for equal sizing
 * - Consistent spacing between items
 * - Pagination support with automatic loading
 * - Shimmer loading state
 *
 * @param isLoading Whether content is currently loading
 * @param endReached Whether all pages have been loaded
 * @param itemsEmpty Whether the items list is empty
 * @param appendItems Callback to load more items
 * @param pagingEnabled Whether pagination is enabled
 * @param contentPadding Padding around the grid content
 * @param minCellWidth Minimum width for adaptive grid cells (default: poster medium width)
 * @param itemSpacing Spacing between grid items (default: grid item spacing)
 * @param content The grid content scope
 */
@Composable
fun LazyVerticalContentGrid(
    isLoading: Boolean = false,
    endReached: Boolean = false,
    itemsEmpty: Boolean = false,
    appendItems: () -> Unit = {},
    pagingEnabled: Boolean,
    contentPadding: PaddingValues,
    minCellWidth: Dp = Dimens.gridCellMinWidth,
    itemSpacing: Dp = Dimens.gridItemSpacing,
    content: LazyGridScope.() -> Unit,
) {
    val lazyGridState = rememberLazyGridState()

    if (pagingEnabled) {
        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = lazyGridState.layoutInfo
                if (layoutInfo.totalItemsCount == 0) {
                    false
                } else {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val viewPortHeight =
                        layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset

                    val isLastItemReached = lastVisibleItem.index + 1 == layoutInfo.totalItemsCount
                    val isLastItemDisplayed =
                        lastVisibleItem.offset.y + lastVisibleItem.size.height <= viewPortHeight

                    (isLastItemReached && isLastItemDisplayed)
                }
            }
        }

        // Fix: Include all dependencies in LaunchedEffect to avoid stale values
        LaunchedEffect(isAtBottom, isLoading, endReached) {
            if (isAtBottom && !isLoading && !endReached) {
                appendItems()
            }
        }
    }

    if (itemsEmpty && isLoading) {
        // Shimmer loading grid with adaptive columns
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellWidth),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(6) { ShimmerGridCard() }
        }
    } else {
        // Content grid with adaptive columns for equal sizing
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellWidth),
            contentPadding = contentPadding,
            state = lazyGridState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

/**
 * Lazy vertical grid with fixed number of columns. Use when you need a specific column count
 * regardless of screen size.
 *
 * @param columns Number of columns in the grid
 * @param isLoading Whether content is currently loading
 * @param endReached Whether all pages have been loaded
 * @param itemsEmpty Whether the items list is empty
 * @param appendItems Callback to load more items
 * @param pagingEnabled Whether pagination is enabled
 * @param contentPadding Padding around the grid content
 * @param itemSpacing Spacing between grid items
 * @param content The grid content scope
 */
@Composable
fun LazyVerticalContentGridFixed(
    columns: Int = Dimens.gridColumns,
    isLoading: Boolean = false,
    endReached: Boolean = false,
    itemsEmpty: Boolean = false,
    appendItems: () -> Unit = {},
    pagingEnabled: Boolean,
    contentPadding: PaddingValues,
    itemSpacing: Dp = Dimens.gridItemSpacing,
    content: LazyGridScope.() -> Unit,
) {
    val lazyGridState = rememberLazyGridState()

    if (pagingEnabled) {
        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = lazyGridState.layoutInfo
                if (layoutInfo.totalItemsCount == 0) {
                    false
                } else {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val viewPortHeight =
                        layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset

                    val isLastItemReached = lastVisibleItem.index + 1 == layoutInfo.totalItemsCount
                    val isLastItemDisplayed =
                        lastVisibleItem.offset.y + lastVisibleItem.size.height <= viewPortHeight

                    (isLastItemReached && isLastItemDisplayed)
                }
            }
        }

        LaunchedEffect(isAtBottom, isLoading, endReached) {
            if (isAtBottom && !isLoading && !endReached) {
                appendItems()
            }
        }
    }

    if (itemsEmpty && isLoading) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(columns * 2) { ShimmerGridCard() }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = contentPadding,
            state = lazyGridState,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}
