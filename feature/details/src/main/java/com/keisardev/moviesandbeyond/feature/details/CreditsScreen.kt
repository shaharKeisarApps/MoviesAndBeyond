package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.details.people.Credits
import com.keisardev.moviesandbeyond.core.ui.PersonImage
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import com.keisardev.moviesandbeyond.core.ui.noRippleClickable
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

@Composable
fun CreditsRoute(
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DetailsViewModel,
    detailsId: String? = null,
) {
    // For Navigation 3: Set the ID from the route key
    detailsId?.let { viewModel.setDetailsId(it) }

    val details by viewModel.contentDetailsUiState.collectAsStateWithLifecycle()

    CreditsScreen(details = details, onItemClick = onItemClick, onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsScreen(
    details: ContentDetailUiState,
    onItemClick: (String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBarWithBackButton(
                title = {
                    Text(
                        text = stringResource(id = R.string.credits),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                onBackClick = onBackClick,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (details) {
                is ContentDetailUiState.Movie -> {
                    CreditsLazyColumn(credits = details.data.credits, onItemClick = onItemClick)
                }

                is ContentDetailUiState.TV -> {
                    CreditsLazyColumn(credits = details.data.credits, onItemClick = onItemClick)
                }

                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreditsLazyColumn(credits: Credits, onItemClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xxxs)) {
        stickyHeader { CategoryHeader(stringResource(id = R.string.cast)) }
        items(items = credits.cast, key = { it.id }) { castMember ->
            val stableClick =
                remember(castMember.id) { { onItemClick("${castMember.id},${MediaType.PERSON}") } }
            CreditsItem(
                name = castMember.name,
                role = castMember.character,
                imagePath = castMember.profilePath,
                onItemClick = stableClick,
            )
        }

        if (credits.crew.isNotEmpty()) {
            item { CategoryHeader(text = stringResource(id = R.string.crew)) }

            val crewListByDepartment = credits.crew.groupBy { it.department }
            crewListByDepartment.forEach { mapEntry ->
                stickyHeader { CategoryHeader(text = mapEntry.key) }
                items(items = mapEntry.value, key = { it.creditId }) { crewMember ->
                    val stableClick =
                        remember(crewMember.id) {
                            { onItemClick("${crewMember.id},${MediaType.PERSON}") }
                        }
                    CreditsItem(
                        name = crewMember.name,
                        role = crewMember.job,
                        imagePath = crewMember.profilePath,
                        onItemClick = stableClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditsItem(
    name: String,
    role: String,
    imagePath: String,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .noRippleClickable { onItemClick() }
                .padding(horizontal = Spacing.screenPadding, vertical = Spacing.sm),
    ) {
        PersonImage(imageUrl = imagePath, modifier = Modifier.size(Dimens.personAvatarSize))

        Spacer(Modifier.width(Spacing.sm))

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(Spacing.xxxs))
            Text(
                text = role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(Spacing.sm),
    )
}
