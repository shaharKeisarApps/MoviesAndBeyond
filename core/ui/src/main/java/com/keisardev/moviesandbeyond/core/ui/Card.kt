package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Card component for displaying movie/TV show posters in lists.
 * Uses [TmdbListImage] which is optimized for scrolling performance.
 *
 * This composable is designed to be stable/skippable when:
 * - posterPath is the same (String is stable)
 * - onItemClick is a stable lambda (remembered by caller)
 */
@Composable
fun MediaItemCard(
    posterPath: String,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit = {}
) {
    // Remember interaction source to avoid recreation on recomposition
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .size(width = 120.dp, height = 160.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            )
    ) {
        TmdbListImage(imageUrl = posterPath)
    }
}
