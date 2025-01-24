package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.model.user.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val userData: Flow<UserData>

    suspend fun getAccountDetails(): AccountDetails?

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    suspend fun setAdultResultPreference(includeAdultResults: Boolean)

    suspend fun setDarkModePreference(selectedDarkMode: com.keisardev.moviesandbeyond.core.model.SelectedDarkMode)

    suspend fun updateAccountDetails(accountId: Int): com.keisardev.moviesandbeyond.core.model.NetworkResponse<Unit>

    suspend fun setHideOnboarding(hideOnboarding: Boolean)
}