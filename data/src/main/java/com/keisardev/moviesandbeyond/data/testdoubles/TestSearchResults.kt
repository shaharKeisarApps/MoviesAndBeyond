package com.keisardev.moviesandbeyond.data.testdoubles

import com.keisardev.moviesandbeyond.core.model.SearchItem

val testSearchResults: List<SearchItem> = listOf(
    SearchItem(
        id = 1,
        imagePath = "path",
        name = "name",
        mediaType = movieMediaType
    ),
    SearchItem(
        id = 2,
        imagePath = "path",
        name = "name",
        mediaType = movieMediaType
    ),
    SearchItem(
        id = 3,
        imagePath = "path",
        name = "name",
        mediaType = movieMediaType
    ),
    SearchItem(
        id = 4,
        imagePath = "path",
        name = "name",
        mediaType = movieMediaType
    )
)