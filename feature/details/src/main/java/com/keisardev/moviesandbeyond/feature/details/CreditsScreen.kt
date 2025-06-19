package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.details.people.Credits
import com.keisardev.moviesandbeyond.core.ui.PersonImage
import com.keisardev.moviesandbeyond.core.ui.TopAppBarWithBackButton
import com.keisardev.moviesandbeyond.core.ui.noRippleClickable
import com.keisardev.moviesandbeyond.ui.navigation.NavManager
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey


// CreditsRoute is assumed to be called from NavDisplay when the key is CreditsKey(itemId, itemType)
@Composable
internal fun CreditsRoute(
    // onItemClick: (String) -> Unit, // Removed
    // onBackClick: () -> Unit, // Removed
    viewModel: DetailsViewModel // ViewModel provides the data based on itemId/itemType from CreditsKey
) {
    val details by viewModel.contentDetailsUiState.collectAsStateWithLifecycle()

    CreditsScreen(
        details = details
        // onItemClick and onBackClick are handled directly now
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsScreen(
    details: ContentDetailUiState
    // onItemClick: (String) -> Unit, // Removed
    // onBackClick: () -> Unit // Removed
) {
    Scaffold(
        topBar = {
            TopAppBarWithBackButton(
                title = {
                    Text(
                        text = stringResource(id = R.string.credits),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onBackClick = { NavManager.navigateUp() } // Use NavManager
            )
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            when (details) {
                is ContentDetailUiState.Movie -> {
                    CreditsLazyColumn(
                        credits = details.data.credits
                        // onItemClick is handled in CreditsLazyColumn
                    )
                }

                is ContentDetailUiState.TV -> {
                    CreditsLazyColumn(
                        credits = details.data.credits
                        // onItemClick is handled in CreditsLazyColumn
                    )
                }

                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreditsLazyColumn(
    credits: Credits
    // onItemClick: (String) -> Unit // Removed, handled in CreditsItem
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    ) {
        stickyHeader {
            CategoryHeader(stringResource(id = R.string.cast))
        }
        items(
            items = credits.cast,
            key = { it.id }
        ) {
            CreditsItem(
                name = it.name,
                role = it.character,
                imagePath = it.profilePath,
                onItemClick = {
                    NavManager.navigateTo(DetailsKey(itemId = it.id.toString(), itemType = MediaType.PERSON.name)) // Use NavManager
                }
            )
        }

        if (credits.crew.isNotEmpty()) {
            item {
                CategoryHeader(text = stringResource(id = R.string.crew))
            }

            val crewListByDepartment = credits.crew.groupBy { it.department }
            crewListByDepartment.forEach { mapEntry ->
                stickyHeader {
                    CategoryHeader(text = mapEntry.key)
                }
                items(
                    items = mapEntry.value,
                    key = { it.creditId }
                ) {
                    CreditsItem(
                        name = it.name,
                        role = it.job,
                        imagePath = it.profilePath,
                        onItemClick = {
                            NavManager.navigateTo(DetailsKey(itemId = it.id.toString(), itemType = MediaType.PERSON.name)) // Use NavManager
                        }
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
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable { onItemClick() }
            .padding(horizontal = horizontalPadding, vertical = 6.dp)
    ) {
        PersonImage(
            imageUrl = imagePath,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(2.dp))
            Text(text = role)
        }
    }
}

@Composable
private fun CategoryHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(10.dp)
    )
}