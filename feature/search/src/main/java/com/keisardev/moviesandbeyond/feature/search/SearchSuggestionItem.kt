package com.keisardev.moviesandbeyond.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.keisardev.moviesandbeyond.core.ui.MediaItemCard
import com.keisardev.moviesandbeyond.core.ui.MediaSharedElementKey
import com.keisardev.moviesandbeyond.core.ui.noRippleClickable
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

/** Search result item with poster and title. Displays content in a card with consistent spacing. */
@Composable
internal fun SearchSuggestionItem(
    name: String,
    imagePath: String,
    sharedElementKey: MediaSharedElementKey? = null,
    onItemClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier.noRippleClickable { onItemClick() },
    ) {
        MediaItemCard(
            posterPath = imagePath,
            sharedElementKey = sharedElementKey,
            onItemClick = onItemClick,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.xxs),
        )
    }
}
