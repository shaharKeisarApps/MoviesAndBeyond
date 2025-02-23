package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    suspend fun login(
        username: String,
        password: String
    ): NetworkResponse<Unit>

    suspend fun logout(accountId: Int): NetworkResponse<Unit>
}