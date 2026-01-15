package com.keisardev.moviesandbeyond.core.local.datastore

import androidx.datastore.core.DataStore
import com.keisardev.moviesandbeyond.core.local.proto.DarkMode
import com.keisardev.moviesandbeyond.core.local.proto.UserPreferences
import com.keisardev.moviesandbeyond.core.local.proto.copy
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SeedColor.Companion.DEFAULT_CUSTOM_COLOR_ARGB
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.UserData
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class UserPreferencesDataStore
@Inject
constructor(private val userPreferences: DataStore<UserPreferences>) {
    val userData =
        userPreferences.data.map {
            UserData(
                useDynamicColor = it.useDynamicColor,
                includeAdultResults = it.includeAdultResults,
                darkMode =
                    when (it.darkMode) {
                        null,
                        DarkMode.UNRECOGNIZED,
                        DarkMode.DARK_MODE_SYSTEM -> SelectedDarkMode.SYSTEM

                        DarkMode.DARK_MODE_DARK -> SelectedDarkMode.DARK
                        DarkMode.DARK_MODE_LIGHT -> SelectedDarkMode.LIGHT
                    },
                hideOnboarding = it.hideOnboarding,
                seedColor = SeedColor.fromName(it.seedColor),
                useLocalOnly = it.useLocalOnly,
                customColorArgb =
                    if (it.customColorArgb != 0L) it.customColorArgb else DEFAULT_CUSTOM_COLOR_ARGB)
        }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData { it.copy { this.useDynamicColor = useDynamicColor } }
    }

    suspend fun setAdultResultPreference(includeAdultResults: Boolean) {
        userPreferences.updateData { it.copy { this.includeAdultResults = includeAdultResults } }
    }

    suspend fun setDarkModePreference(selectedDarkMode: SelectedDarkMode) {
        userPreferences.updateData {
            it.copy {
                this.darkMode =
                    when (selectedDarkMode) {
                        SelectedDarkMode.SYSTEM -> DarkMode.DARK_MODE_SYSTEM
                        SelectedDarkMode.DARK -> DarkMode.DARK_MODE_DARK
                        SelectedDarkMode.LIGHT -> DarkMode.DARK_MODE_LIGHT
                    }
            }
        }
    }

    suspend fun setHideOnboarding(hideOnboarding: Boolean) {
        userPreferences.updateData { it.copy { this.hideOnboarding = hideOnboarding } }
    }

    suspend fun setSeedColorPreference(seedColor: SeedColor) {
        userPreferences.updateData { it.copy { this.seedColor = seedColor.name } }
    }

    suspend fun setUseLocalOnlyPreference(useLocalOnly: Boolean) {
        userPreferences.updateData { it.copy { this.useLocalOnly = useLocalOnly } }
    }

    suspend fun setCustomColorArgb(colorArgb: Long) {
        userPreferences.updateData { it.copy { this.customColorArgb = colorArgb } }
    }
}
