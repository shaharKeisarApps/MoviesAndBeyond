package com.keisardev.moviesandbeyond.data.testdoubles

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItem

internal val tvMediaType = MediaType.TV.name.lowercase()
internal val movieMediaType = MediaType.MOVIE.name.lowercase()

val testLibraryItems: List<LibraryItem> =
    listOf(
        LibraryItem(id = 1, imagePath = "path", name = "name", mediaType = movieMediaType),
        LibraryItem(id = 2, imagePath = "path", name = "name", mediaType = tvMediaType),
    )
