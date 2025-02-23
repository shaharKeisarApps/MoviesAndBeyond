package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.model.NetworkResponse
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.model.user.UserData
import com.keisardev.moviesandbeyond.core.network.retrofit.TmdbApi
import com.keisardev.moviesandbeyond.data.model.asEntity
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val tmdbApi: TmdbApi,
    private val accountDetailsDao: AccountDetailsDao,
) : UserRepository {
    override val userData: Flow<UserData> = userPreferencesDataStore.userData

    override suspend fun getAccountDetails(): AccountDetails? = accountDetailsDao
        .getAccountDetails()?.asModel()

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferencesDataStore.setDynamicColorPreference(useDynamicColor)
    }

    override suspend fun setAdultResultPreference(includeAdultResults: Boolean) {
        userPreferencesDataStore.setAdultResultPreference(includeAdultResults)
    }

    override suspend fun setDarkModePreference(selectedDarkMode: SelectedDarkMode) {
        userPreferencesDataStore.setDarkModePreference(selectedDarkMode)
    }

    override suspend fun updateAccountDetails(accountId: Int): NetworkResponse<Unit> {
        return try {
            val accountDetails = tmdbApi.getAccountDetailsWithId(accountId).asEntity()
            accountDetailsDao.addAccountDetails(accountDetails)
            userPreferencesDataStore.setAdultResultPreference(accountDetails.includeAdult)

            NetworkResponse.Success(Unit)
        } catch (e: IOException) {
            NetworkResponse.Error()
        } catch (e: HttpException) {
            NetworkResponse.Error()
        }
    }

    override suspend fun setHideOnboarding(hideOnboarding: Boolean) {
        userPreferencesDataStore.setHideOnboarding(hideOnboarding)
    }
}