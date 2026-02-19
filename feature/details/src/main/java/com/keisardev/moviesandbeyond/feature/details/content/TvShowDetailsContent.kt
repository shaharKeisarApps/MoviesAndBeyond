package com.keisardev.moviesandbeyond.feature.details.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.details.tv.TvDetails
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.keisardev.moviesandbeyond.feature.details.R

@Composable
internal fun TvShowDetailsContent(
    tvDetails: TvDetails,
    isFavorite: Boolean,
    isAddedToWatchList: Boolean,
    onFavoriteClick: (LibraryItem) -> Unit,
    onWatchlistClick: (LibraryItem) -> Unit,
    onSeeAllCastClick: () -> Unit,
    onCastClick: (String) -> Unit,
    onRecommendationClick: (String) -> Unit,
    onBackdropCollapse: (Boolean) -> Unit,
) {
    val libraryItem = remember(tvDetails.id) { tvDetails.asLibraryItem() }
    val stableFavoriteClick =
        remember(libraryItem, onFavoriteClick) { { onFavoriteClick(libraryItem) } }
    val stableWatchlistClick =
        remember(libraryItem, onWatchlistClick) { { onWatchlistClick(libraryItem) } }
    val stableRecommendationClick =
        remember(onRecommendationClick) {
            { id: String -> onRecommendationClick("${id},${MediaType.TV}") }
        }

    MediaDetailsContent(
        backdropPath = tvDetails.backdropPath,
        voteCount = tvDetails.voteCount,
        name = tvDetails.name,
        rating = tvDetails.rating,
        releaseYear = tvDetails.releaseYear,
        runtime = tvDetails.episodeRunTime,
        tagline = tvDetails.tagline,
        genres = tvDetails.genres,
        overview = tvDetails.overview,
        cast = tvDetails.credits.cast.take(10),
        recommendations = tvDetails.recommendations,
        isFavorite = isFavorite,
        isAddedToWatchList = isAddedToWatchList,
        onFavoriteClick = stableFavoriteClick,
        onWatchlistClick = stableWatchlistClick,
        onSeeAllCastClick = onSeeAllCastClick,
        onCastClick = onCastClick,
        onRecommendationClick = stableRecommendationClick,
        onBackdropCollapse = onBackdropCollapse) {
            TvDetailsSection(
                originalLanguage = tvDetails.originalLanguage,
                firstAirDate = tvDetails.firstAirDate,
                lastAirDate = tvDetails.lastAirDate,
                inProduction = tvDetails.inProduction,
                status = tvDetails.status,
                nextAirDate = tvDetails.nextEpisodeToAir?.airDate,
                numberOfEpisodes = tvDetails.numberOfEpisodes,
                numberOfSeasons = tvDetails.numberOfSeasons,
                networks = tvDetails.networks,
                productionCompanies = tvDetails.productionCompanies,
                productionCountries = tvDetails.productionCountries)
        }
}

@Composable
private fun TvDetailsSection(
    originalLanguage: String,
    firstAirDate: String,
    lastAirDate: String,
    inProduction: String,
    status: String,
    nextAirDate: String?,
    numberOfEpisodes: Int,
    numberOfSeasons: Int,
    networks: String,
    productionCompanies: String,
    productionCountries: String
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large)
                .padding(Spacing.md)) {
            DetailItem(
                fieldName = stringResource(id = R.string.original_language),
                value = originalLanguage)

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.first_air_date), value = firstAirDate)

            HorizontalDivider(color = dividerColor)

            DetailItem(fieldName = stringResource(id = R.string.last_air_date), value = lastAirDate)

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.in_production), value = inProduction)

            HorizontalDivider(color = dividerColor)

            DetailItem(fieldName = stringResource(id = R.string.status), value = status)

            nextAirDate?.let {
                HorizontalDivider(color = dividerColor)
                DetailItem(fieldName = stringResource(id = R.string.next_air_date), value = it)
            }

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.number_episodes),
                value = "$numberOfEpisodes")

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.number_seasons),
                value = "$numberOfSeasons")

            HorizontalDivider(color = dividerColor)

            DetailItem(fieldName = stringResource(id = R.string.networks), value = networks)

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.production_companies),
                value = productionCompanies)

            HorizontalDivider(color = dividerColor)

            DetailItem(
                fieldName = stringResource(id = R.string.production_countries),
                value = productionCountries)
        }
}
