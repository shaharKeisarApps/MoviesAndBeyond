package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.local.database.entity.AccountDetailsEntity
import com.keisardev.moviesandbeyond.core.local.datastore.UserPreferencesDataStore
import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.error.NetworkError
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var userPreferencesDataStore: UserPreferencesDataStore
    private lateinit var fakeTmdbApi: FakeTmdbApi
    private lateinit var fakeAccountDetailsDao: FakeAccountDetailsDao
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        userPreferencesDataStore = FakeUserPreferencesDataStore()
        fakeTmdbApi = FakeTmdbApi()
        fakeAccountDetailsDao = FakeAccountDetailsDao()
        repository =
            UserRepositoryImpl(
                userPreferencesDataStore = userPreferencesDataStore,
                tmdbApi = fakeTmdbApi,
                accountDetailsDao = fakeAccountDetailsDao,
            )
    }

    // region userData

    @Test
    fun `userData emits default preferences initially`() = runTest {
        val userData = repository.userData.first()

        assertFalse(userData.useDynamicColor)
        assertFalse(userData.includeAdultResults)
        assertEquals(SelectedDarkMode.SYSTEM, userData.darkMode)
        assertFalse(userData.hideOnboarding)
    }

    // endregion

    // region setDynamicColorPreference

    @Test
    fun `setDynamicColorPreference updates user data`() = runTest {
        repository.setDynamicColorPreference(true)

        val userData = repository.userData.first()
        assertTrue(userData.useDynamicColor)
    }

    // endregion

    // region setAdultResultPreference

    @Test
    fun `setAdultResultPreference updates user data`() = runTest {
        repository.setAdultResultPreference(true)

        val userData = repository.userData.first()
        assertTrue(userData.includeAdultResults)
    }

    // endregion

    // region setDarkModePreference

    @Test
    fun `setDarkModePreference to DARK updates user data`() = runTest {
        repository.setDarkModePreference(SelectedDarkMode.DARK)

        val userData = repository.userData.first()
        assertEquals(SelectedDarkMode.DARK, userData.darkMode)
    }

    @Test
    fun `setDarkModePreference to LIGHT updates user data`() = runTest {
        repository.setDarkModePreference(SelectedDarkMode.LIGHT)

        val userData = repository.userData.first()
        assertEquals(SelectedDarkMode.LIGHT, userData.darkMode)
    }

    // endregion

    // region setSeedColorPreference

    @Test
    fun `setSeedColorPreference updates user data`() = runTest {
        repository.setSeedColorPreference(SeedColor.BLUE)

        val userData = repository.userData.first()
        assertEquals(SeedColor.BLUE, userData.seedColor)
    }

    // endregion

    // region setHideOnboarding

    @Test
    fun `setHideOnboarding updates user data`() = runTest {
        repository.setHideOnboarding(true)

        val userData = repository.userData.first()
        assertTrue(userData.hideOnboarding)
    }

    // endregion

    // region setUseLocalOnly

    @Test
    fun `setUseLocalOnly updates user data`() = runTest {
        repository.setUseLocalOnly(true)

        val userData = repository.userData.first()
        assertTrue(userData.useLocalOnly)
    }

    // endregion

    // region getAccountDetails

    @Test
    fun `getAccountDetails returns null when no account is stored`() = runTest {
        val details = repository.getAccountDetails()

        assertNull(details)
    }

    @Test
    fun `getAccountDetails returns mapped account when stored`() = runTest {
        fakeAccountDetailsDao.accountDetails =
            AccountDetailsEntity(
                id = 42,
                gravatarHash = "hash",
                includeAdult = false,
                iso6391 = "en",
                iso31661 = "US",
                name = "Test User",
                tmdbAvatarPath = "/avatar.jpg",
                username = "testuser",
            )

        val details = repository.getAccountDetails()

        assertNotNull(details)
        assertEquals(42, details!!.id)
        assertEquals("testuser", details.username)
        assertEquals("Test User", details.name)
    }

    // endregion

    // region updateAccountDetails

    @Test
    fun `updateAccountDetails success stores account and updates preferences`() = runTest {
        val result = repository.updateAccountDetails(accountId = 1)

        assertTrue(result is Result.Success)
        assertNotNull(fakeAccountDetailsDao.accountDetails)
        assertEquals(1, fakeAccountDetailsDao.accountDetails!!.id)
    }

    @Test
    fun `updateAccountDetails with IOException returns Result Error`() = runTest {
        fakeTmdbApi.exception = IOException("Network down")

        val result = repository.updateAccountDetails(accountId = 1)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is IOException)
    }

    @Test
    fun `updateAccountDetails with NetworkError returns Result Error with message`() = runTest {
        fakeTmdbApi.exception = NetworkError.Forbidden()

        val result = repository.updateAccountDetails(accountId = 1)

        assertTrue(result is Result.Error)
        assertEquals("Forbidden (403)", (result as Result.Error).message)
    }

    // endregion

    // region setCustomColorArgb

    @Test
    fun `setCustomColorArgb updates user data`() = runTest {
        val customColor = 0xFFFF0000L

        repository.setCustomColorArgb(customColor)

        val userData = repository.userData.first()
        assertEquals(customColor, userData.customColorArgb)
    }

    // endregion
}
