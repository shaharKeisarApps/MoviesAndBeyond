package com.keisardev.moviesandbeyond.core.network.model.details

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class NetworkGenre(val id: Int, val name: String)
