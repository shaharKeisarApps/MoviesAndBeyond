package com.keisardev.moviesandbeyond

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.keisardev.moviesandbeyond.MainActivityUiState.Loading
import com.keisardev.moviesandbeyond.MainActivityUiState.Success
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.ui.MoviesAndBeyondApp
import com.keisardev.moviesandbeyond.ui.theme.MoviesAndBeyondTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate() per Android 12+ guidelines
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        var uiState: MainActivityUiState by mutableStateOf(Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.onEach { uiState = it }.collect {}
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                Loading -> true
                is Success -> false
            }
        }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)
            val dynamicColor = shouldUseDynamicColor(uiState)
            val seedColor = getSeedColor(uiState)

            MoviesAndBeyondTheme(
                darkTheme = darkTheme, dynamicColor = dynamicColor, seedColor = seedColor) {
                    if (uiState is Success) {
                        MoviesAndBeyondApp(hideOnboarding = (uiState as Success).hideOnboarding)
                    }
                }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        Loading -> isSystemInDarkTheme()
        is Success ->
            when (uiState.darkMode) {
                SelectedDarkMode.SYSTEM -> isSystemInDarkTheme()
                SelectedDarkMode.DARK -> true
                SelectedDarkMode.LIGHT -> false
            }
    }
}

@Composable
private fun shouldUseDynamicColor(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        Loading -> false
        is Success -> uiState.useDynamicColor
    }
}

private fun getSeedColor(uiState: MainActivityUiState): SeedColor {
    return when (uiState) {
        Loading -> SeedColor.DEFAULT
        is Success -> uiState.seedColor
    }
}
