package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake [SessionManager] backed by an in-memory field, avoiding the need for real SharedPreferences
 * in unit tests.
 *
 * Note: We cannot extend [SessionManager] directly because it is a concrete class with
 * SharedPreferences in its constructor. Instead, we create a duck-type compatible wrapper that
 * implements the same surface used by AuthRepositoryImpl.
 */
class FakeSessionManager {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn

    private var sessionId: String? = null

    fun storeSessionId(sessionId: String) {
        this.sessionId = sessionId
        _isLoggedIn.value = true
    }

    fun getSessionId(): String? = sessionId

    fun deleteSessionId() {
        sessionId = null
        _isLoggedIn.value = false
    }
}
