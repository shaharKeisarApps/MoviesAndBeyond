package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// Test route definitions
@Serializable data object TestHomeRoute : NavKey

@Serializable data class TestDetailRoute(val itemId: String) : NavKey

@Serializable data object TestSettingsRoute : NavKey

/**
 * Simple Navigation 3 test composable to validate the library works correctly. This can be used as
 * a temporary entry point for testing before full migration.
 */
@Composable
fun Nav3TestScreen() {
    // Create the back stack - we own this completely
    val backStack = remember { mutableStateListOf<NavKey>(TestHomeRoute) }

    Scaffold { padding ->
        NavDisplay(
            modifier = Modifier.padding(padding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider =
                entryProvider {
                    entry<TestHomeRoute> {
                        TestHomeScreen(
                            onNavigateToDetail = { id -> backStack.add(TestDetailRoute(id)) },
                            onNavigateToSettings = { backStack.add(TestSettingsRoute) },
                        )
                    }

                    entry<TestDetailRoute> { key ->
                        TestDetailScreen(
                            itemId = key.itemId,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<TestSettingsRoute> {
                        TestSettingsScreen(onBack = { backStack.removeLastOrNull() })
                    }
                },
        )
    }
}

@Composable
private fun TestHomeScreen(onNavigateToDetail: (String) -> Unit, onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Navigation 3 Test - Home", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { onNavigateToDetail("item-123") },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Go to Detail (with argument)")
        }

        Button(onClick = { onNavigateToSettings() }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Go to Settings")
        }
    }
}

@Composable
private fun TestDetailScreen(itemId: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Detail Screen", style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Item ID: $itemId",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go Back") }
    }
}

@Composable
private fun TestSettingsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Settings Screen", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Go Back") }
    }
}
