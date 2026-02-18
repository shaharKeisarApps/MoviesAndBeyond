package com.keisardev.moviesandbeyond.feature.details.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.details.MovieDetails
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing
import com.keisardev.moviesandbeyond.feature.details.R

@Composable
internal fun MovieDetailsContent(
    movieDetails: MovieDetails,
    isFavorite: Boolean,
    isAddedToWatchList: Boolean,
    onFavoriteClick: (LibraryItem) -> Unit,
    onWatchlistClick: (LibraryItem) -> Unit,
    onCastClick: (String) -> Unit,
    onRecommendationClick: (String) -> Unit,
    onSeeAllCastClick: () -> Unit,
    onBackdropCollapse: (Boolean) -> Unit,
) {
    MediaDetailsContent(
        backdropPath = movieDetails.backdropPath,
        voteCount = movieDetails.voteCount,
        name = movieDetails.title,
        rating = movieDetails.rating,
        releaseYear = movieDetails.releaseYear,
        runtime = movieDetails.runtime,
        tagline = movieDetails.tagline,
        genres = movieDetails.genres,
        overview = movieDetails.overview,
        cast = movieDetails.credits.cast.take(10),
        recommendations = movieDetails.recommendations,
        isFavorite = isFavorite,
        isAddedToWatchList = isAddedToWatchList,
        onFavoriteClick = { onFavoriteClick(movieDetails.asLibraryItem()) },
        onWatchlistClick = { onWatchlistClick(movieDetails.asLibraryItem()) },
        onSeeAllCastClick = onSeeAllCastClick,
        onCastClick = onCastClick,
        onRecommendationClick = { id -> onRecommendationClick("${id},${MediaType.MOVIE}") },
        onBackdropCollapse = onBackdropCollapse) {
            MovieDetailsSection(
                releaseDate = movieDetails.releaseDate,
                originalLanguage = movieDetails.originalLanguage,
                productionCompanies = movieDetails.productionCompanies,
                productionCountries = movieDetails.productionCountries,
                budget = movieDetails.budget,
                revenue = movieDetails.revenue)
        }
}

@Composable
private fun MovieDetailsSection(
    releaseDate: String,
    originalLanguage: String,
    productionCompanies: String,
    productionCountries: String,
    budget: String,
    revenue: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large)
                .padding(Spacing.md)) {
            DetailItem(fieldName = stringResource(id = R.string.release_date), value = releaseDate)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItem(
                fieldName = stringResource(id = R.string.original_language),
                value = originalLanguage)

            if (budget != "0" && budget.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                DetailItem(fieldName = stringResource(id = R.string.budget), value = "$${budget}")
            }

            if (revenue != "0" && revenue.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                DetailItem(fieldName = stringResource(id = R.string.revenue), value = "$${revenue}")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItem(
                fieldName = stringResource(id = R.string.production_companies),
                value = productionCompanies)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            DetailItem(
                fieldName = stringResource(id = R.string.production_countries),
                value = productionCountries)
        }
}
