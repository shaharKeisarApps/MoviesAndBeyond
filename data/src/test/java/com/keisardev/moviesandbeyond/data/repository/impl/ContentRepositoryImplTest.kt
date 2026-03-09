package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import com.keisardev.moviesandbeyond.data.store.MovieContentKey
import com.keisardev.moviesandbeyond.data.store.TvContentKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

@OptIn(ExperimentalStoreApi::class)
class ContentRepositoryImplTest {

    private lateinit var fakeCachedContentDao: FakeCachedContentDao
    private lateinit var fakeMovieStore: FakeStore<MovieContentKey>
    private lateinit var fakeTvStore: FakeStore<TvContentKey>
    private lateinit var repository: ContentRepositoryImpl

    @Before
    fun setUp() {
        fakeCachedContentDao = FakeCachedContentDao()
        fakeMovieStore = FakeStore()
        fakeTvStore = FakeStore()
        repository =
            ContentRepositoryImpl(
                cachedContentDao = fakeCachedContentDao,
                movieContentStore = fakeMovieStore,
                tvContentStore = fakeTvStore,
            )
    }

    // region clearCache

    @Test
    fun `clearCache delegates to cachedContentDao deleteAll`() = runTest {
        repository.clearCache()

        assertTrue(fakeCachedContentDao.deleteAllCalled)
    }

    // endregion

    // region observeMovieItems

    @Test
    fun `observeMovieItems emits Success when store has data`() = runTest {
        val contentItems = listOf(ContentItem(id = 1, imagePath = "/poster.jpg", name = "Movie 1"))
        fakeMovieStore.responseToEmit =
            StoreReadResponse.Data(value = contentItems, origin = StoreReadResponseOrigin.Fetcher())

        val result =
            repository
                .observeMovieItems(category = MovieListCategory.POPULAR, page = 1, refresh = true)
                .first()

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals("Movie 1", result.data[0].name)
    }

    @Test
    fun `observeMovieItems emits Loading when store is loading`() = runTest {
        fakeMovieStore.responseToEmit = StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher())

        val result =
            repository
                .observeMovieItems(
                    category = MovieListCategory.NOW_PLAYING,
                    page = 1,
                    refresh = true,
                )
                .first()

        assertTrue(result is Result.Loading)
    }

    @Test
    fun `observeMovieItems emits Error when store errors`() = runTest {
        fakeMovieStore.responseToEmit =
            StoreReadResponse.Error.Exception(
                error = RuntimeException("Store error"),
                origin = StoreReadResponseOrigin.Fetcher(),
            )

        val result =
            repository
                .observeMovieItems(category = MovieListCategory.TOP_RATED, page = 1, refresh = true)
                .first()

        assertTrue(result is Result.Error)
        assertEquals("Store error", (result as Result.Error).message)
    }

    // endregion

    // region observeTvShowItems

    @Test
    fun `observeTvShowItems emits Success when store has data`() = runTest {
        val contentItems =
            listOf(ContentItem(id = 2, imagePath = "/poster.jpg", name = "TV Show 1"))
        fakeTvStore.responseToEmit =
            StoreReadResponse.Data(value = contentItems, origin = StoreReadResponseOrigin.Fetcher())

        val result =
            repository
                .observeTvShowItems(category = TvShowListCategory.POPULAR, page = 1, refresh = true)
                .first()

        assertTrue(result is Result.Success)
        assertEquals("TV Show 1", (result as Result.Success).data[0].name)
    }

    @Test
    fun `observeTvShowItems emits Error on store error`() = runTest {
        fakeTvStore.responseToEmit =
            StoreReadResponse.Error.Message(
                message = "Failed to load",
                origin = StoreReadResponseOrigin.SourceOfTruth,
            )

        val result =
            repository
                .observeTvShowItems(
                    category = TvShowListCategory.AIRING_TODAY,
                    page = 1,
                    refresh = false,
                )
                .first()

        assertTrue(result is Result.Error)
    }

    // endregion
}

/**
 * A minimal fake [Store] implementation that emits a single configurable response. This avoids the
 * complexity of real Store5 Fetcher/SourceOfTruth wiring in unit tests.
 */
@OptIn(ExperimentalStoreApi::class)
class FakeStore<Key : Any> : Store<Key, List<ContentItem>> {
    var responseToEmit: StoreReadResponse<List<ContentItem>> =
        StoreReadResponse.Data(value = emptyList(), origin = StoreReadResponseOrigin.Cache)

    override fun stream(
        request: StoreReadRequest<Key>
    ): Flow<StoreReadResponse<List<ContentItem>>> = flowOf(responseToEmit)

    override suspend fun clear(key: Key) {
        // no-op for tests
    }

    override suspend fun clear() {
        // no-op for tests
    }
}
