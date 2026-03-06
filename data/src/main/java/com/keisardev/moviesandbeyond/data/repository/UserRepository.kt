package com.keisardev.moviesandbeyond.data.repository

import com.keisardev.moviesandbeyond.core.model.Result
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import com.keisardev.moviesandbeyond.core.model.user.UserData
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user preferences and TMDB account details.
 *
 * Persists theme, content filter, and onboarding preferences via DataStore, and manages the TMDB
 * account profile stored in the local Room database.
 */
interface UserRepository {
    /** Reactive stream of the user's preferences (theme, filters, onboarding state). */
    val userData: Flow<UserData>

    /** Returns the locally cached TMDB account details, or `null` if not logged in. */
    suspend fun getAccountDetails(): AccountDetails?

    /** Toggles Material You dynamic color theming. */
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    /** Toggles whether adult content appears in search results. */
    suspend fun setAdultResultPreference(includeAdultResults: Boolean)

    /** Sets the dark mode preference (System, Light, or Dark). */
    suspend fun setDarkModePreference(selectedDarkMode: SelectedDarkMode)

    /** Sets the color seed used for the app's Material 3 theme. */
    suspend fun setSeedColorPreference(seedColor: SeedColor)

    /**
     * Fetches fresh account details from TMDB and updates the local cache.
     *
     * @param accountId The TMDB account ID to refresh
     */
    suspend fun updateAccountDetails(accountId: Int): Result<Unit>

    /** Marks the onboarding flow as completed so it is not shown again. */
    suspend fun setHideOnboarding(hideOnboarding: Boolean)

    /** Toggles local-only mode where the app works without network authentication. */
    suspend fun setUseLocalOnly(useLocalOnly: Boolean)

    /** Sets a custom ARGB color value for the app's theme seed color. */
    suspend fun setCustomColorArgb(colorArgb: Long)
}
