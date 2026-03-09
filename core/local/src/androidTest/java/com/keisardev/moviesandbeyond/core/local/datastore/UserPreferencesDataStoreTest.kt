package com.keisardev.moviesandbeyond.core.local.datastore

import androidx.datastore.core.DataStoreFactory
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesDataStoreTest {
    private lateinit var userPreferencesDataStore: UserPreferencesDataStore

    @get:Rule val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setUp() {
        userPreferencesDataStore =
            UserPreferencesDataStore(
                DataStoreFactory.create(
                    serializer = UserPreferencesSerializer,
                    produceFile = { tmpFolder.root.resolve("user_prefs_test.pb") },
                )
            )
    }

    @Test
    fun hideOnboardingDefaultIsFalse() = runTest {
        TestCase.assertEquals(userPreferencesDataStore.userData.first().hideOnboarding, false)
    }

    @Test
    fun hideOnboardingWhenSetTrue() = runTest {
        userPreferencesDataStore.setHideOnboarding(true)
        TestCase.assertEquals(userPreferencesDataStore.userData.first().hideOnboarding, true)
    }

    @Test
    fun darkModeDefaultIsDark() = runTest {
        TestCase.assertEquals(
            userPreferencesDataStore.userData.first().darkMode,
            com.keisardev.moviesandbeyond.core.model.SelectedDarkMode.DARK,
        )
    }
}
