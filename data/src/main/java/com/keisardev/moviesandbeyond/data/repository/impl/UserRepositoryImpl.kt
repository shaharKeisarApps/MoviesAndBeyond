package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.model.user.UserData
import com.keisardev.moviesandbeyond.core.network.error.NetworkError
import com.keisardev.moviesandbeyond.core.network.ktor.TmdbApi
import com.keisardev.moviesandbeyond.data.model.asEntity
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class UserRepositoryImpl
@Inject
constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val tmdbApi: TmdbApi,
    private val accountDetailsDao: AccountDetailsDao,
) : UserRepository {
    override val userData: Flow<UserData> = userPreferencesDataStore.userData

    override suspend fun getAccountDetails(): AccountDetails? =
        accountDetailsDao.getAccountDetails()?.asModel()

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferencesDataStore.setDynamicColorPreference(useDynamicColor)
    }

    override suspend fun setAdultResultPreference(includeAdultResults: Boolean) {
        userPreferencesDataStore.setAdultResultPreference(includeAdultResults)
    }

    override suspend fun setDarkModePreference(selectedDarkMode: SelectedDarkMode) {
        userPreferencesDataStore.setDarkModePreference(selectedDarkMode)
    }

    override suspend fun setSeedColorPreference(seedColor: SeedColor) {
        userPreferencesDataStore.setSeedColorPreference(seedColor)
    }

    override suspend fun updateAccountDetails(accountId: Int): Result<Unit> {
        return try {
            val accountDetails = tmdbApi.getAccountDetailsWithId(accountId).asEntity()
            accountDetailsDao.addAccountDetails(accountDetails)
            userPreferencesDataStore.setAdultResultPreference(accountDetails.includeAdult)
            Result.Success(Unit)
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: NetworkError) {
            Result.Error(e, e.message)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun setHideOnboarding(hideOnboarding: Boolean) {
        userPreferencesDataStore.setHideOnboarding(hideOnboarding)
    }

    override suspend fun setUseLocalOnly(useLocalOnly: Boolean) {
        userPreferencesDataStore.setUseLocalOnlyPreference(useLocalOnly)
    }

    override suspend fun setCustomColorArgb(colorArgb: Long) {
        userPreferencesDataStore.setCustomColorArgb(colorArgb)
    }
}
