package com.keisardev.moviesandbeyond.core.network.model.details.tv

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class NetworkBroadcastNetwork(val name: String)
