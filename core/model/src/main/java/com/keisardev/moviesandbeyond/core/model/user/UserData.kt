package com.keisardev.moviesandbeyond.core.model.user

import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode

data class UserData(
    val useDynamicColor: Boolean,
    val includeAdultResults: Boolean,
    val darkMode: SelectedDarkMode,
    val hideOnboarding: Boolean,
    val seedColor: SeedColor
)
