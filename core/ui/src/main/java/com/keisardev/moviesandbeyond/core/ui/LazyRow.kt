package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerRow
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun LazyRowContentSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    endReached: Boolean = false,
    itemsEmpty: Boolean = false,
    appendItems: () -> Unit = {},
    pagingEnabled: Boolean,
    rowContentPadding: PaddingValues = PaddingValues(0.dp),
    sectionHeaderContent: @Composable () -> Unit,
    rowContent: LazyListScope.() -> Unit,
) {
    val lazyRowState = rememberLazyListState()

    if (pagingEnabled) {
        val isAtEnd by remember {
            derivedStateOf {
                val layoutInfo = lazyRowState.layoutInfo
                if (layoutInfo.totalItemsCount == 0) {
                    false
                } else {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val viewportWidth =
                        layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset

                    val isLastItemReached = lastVisibleItem.index + 1 == layoutInfo.totalItemsCount
                    val isLastItemDisplayed =
                        lastVisibleItem.offset + lastVisibleItem.size <= viewportWidth

                    (isLastItemReached && isLastItemDisplayed)
                }
            }
        }

        // Fix: Include all dependencies in LaunchedEffect to avoid stale values
        LaunchedEffect(isAtEnd, isLoading, endReached) {
            if (isAtEnd && !isLoading && !endReached) {
                appendItems()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.headerSpacing)) {
        sectionHeaderContent()

        if (itemsEmpty && isLoading) {
            ShimmerRow(contentPadding = rowContentPadding, modifier = modifier.fillMaxWidth())
        } else {
            LazyRow(
                contentPadding = rowContentPadding,
                horizontalArrangement = Arrangement.spacedBy(Spacing.itemSpacing),
                state = lazyRowState,
                modifier = modifier.fillMaxWidth(),
                content = rowContent)
        }
    }
}
