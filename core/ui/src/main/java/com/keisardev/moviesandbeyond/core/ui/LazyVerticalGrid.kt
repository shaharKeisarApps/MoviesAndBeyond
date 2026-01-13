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
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerGridCard
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun LazyVerticalContentGrid(
    isLoading: Boolean = false,
    endReached: Boolean = false,
    itemsEmpty: Boolean = false,
    appendItems: () -> Unit = {},
    pagingEnabled: Boolean,
    contentPadding: PaddingValues,
    content: LazyGridScope.() -> Unit
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

        val shouldAppend = isAtBottom && !isLoading && !endReached
        LaunchedEffect(isAtBottom) { if (shouldAppend) appendItems() }
    }

    if (itemsEmpty && isLoading) {
        // Shimmer loading grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(Dimens.gridColumns),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
            userScrollEnabled = false,
            modifier = Modifier.fillMaxWidth()) {
                items(6) { ShimmerGridCard() }
            }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(Dimens.gridColumns),
            contentPadding = contentPadding,
            state = lazyGridState,
            horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
            modifier = Modifier.fillMaxSize(),
            content = content)
    }
}
