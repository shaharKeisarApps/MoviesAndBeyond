package com.keisardev.moviesandbeyond.data.repository.impl

import androidx.datastore.core.DataStore
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesSerializer
import com.keisardev.moviesandbeyond.core.local.proto.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Creates a [UserPreferencesDataStore] backed by an in-memory [DataStore] for testing. The
 * underlying [UserPreferences] proto can be observed and modified through the returned instance.
 */
fun FakeUserPreferencesDataStore(): UserPreferencesDataStore {
    return UserPreferencesDataStore(InMemoryUserPreferencesDataStore())
}

/**
 * In-memory [DataStore] that holds a [UserPreferences] proto in a [MutableStateFlow]. No file I/O
 * is performed.
 */
private class InMemoryUserPreferencesDataStore : DataStore<UserPreferences> {
    private val state = MutableStateFlow(UserPreferencesSerializer.defaultValue)

    override val data: Flow<UserPreferences> = state

    override suspend fun updateData(
        transform: suspend (t: UserPreferences) -> UserPreferences
    ): UserPreferences {
        var result: UserPreferences? = null
        state.update { current ->
            val transformed = transform(current)
            result = transformed
            transformed
        }
        return result!!
    }
}
