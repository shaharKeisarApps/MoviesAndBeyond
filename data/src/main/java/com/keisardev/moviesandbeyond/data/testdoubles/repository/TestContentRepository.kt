package com.keisardev.moviesandbeyond.data.testdoubles.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

val testContentItems: List<ContentItem> =
    listOf(
        ContentItem(id = 1, imagePath = "/path1.jpg", name = "Movie One", rating = 7.5),
        ContentItem(id = 2, imagePath = "/path2.jpg", name = "Movie Two", rating = 8.0),
    )

class TestContentRepository : ContentRepository {
    private var generateError = false

    private val _movieItems = MutableStateFlow(testContentItems)
    private val _tvItems = MutableStateFlow(testContentItems)

    override fun observeMovieItems(
        category: MovieListCategory,
        page: Int,
        refresh: Boolean,
    ): Flow<Result<List<ContentItem>>> =
        _movieItems.map { items ->
            if (generateError) Result.Error(RuntimeException("Movie fetch failed"))
            else Result.Success(items)
        }

    override fun observeTvShowItems(
        category: TvShowListCategory,
        page: Int,
        refresh: Boolean,
    ): Flow<Result<List<ContentItem>>> =
        _tvItems.map { items ->
            if (generateError) Result.Error(RuntimeException("TV fetch failed"))
            else Result.Success(items)
        }

    override suspend fun refreshMovieItems(
        category: MovieListCategory,
        page: Int,
    ): Result<List<ContentItem>> {
        return if (generateError) {
            Result.Error(RuntimeException("Movie refresh failed"))
        } else {
            Result.Success(testContentItems)
        }
    }

    override suspend fun refreshTvShowItems(
        category: TvShowListCategory,
        page: Int,
    ): Result<List<ContentItem>> {
        return if (generateError) {
            Result.Error(RuntimeException("TV refresh failed"))
        } else {
            Result.Success(testContentItems)
        }
    }

    override suspend fun clearCache() {
        // no-op for tests
    }

    fun generateError(value: Boolean) {
        generateError = value
    }
}
