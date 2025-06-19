package com.keisardev.moviesandbeyond.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
// Assuming NavigationKeys.kt will be created in this package with OnboardingKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.OnboardingKey

object NavManager {

    private val _backStack = MutableStateFlow<List<Any>>(listOf(OnboardingKey))
    val backStack: StateFlow<List<Any>> = _backStack.asStateFlow()

    fun navigateTo(key: Any) {
        _backStack.update { currentStack ->
            // Basic navigation, consider singleTop behavior if needed by checking if last() == key
            // For singleTop: if (currentStack.isNotEmpty() && currentStack.last() == key) return currentStack
            currentStack + key
        }
    }

    fun navigateUp(): Boolean {
        if (_backStack.value.size <= 1) {
            return false // Cannot navigate up from the last or only screen
        }
        _backStack.update { currentStack ->
            currentStack.dropLast(1)
        }
        return true
    }

    fun popBackStackTo(key: Any, inclusive: Boolean): Boolean {
        var keyFound = false
        var newStack: List<Any> = emptyList()

        _backStack.update { currentStack ->
            val lastIndexOfKey = currentStack.lastIndexOf(key)
            if (lastIndexOfKey == -1) {
                keyFound = false
                return@update currentStack // Key not found, return current stack
            }
            keyFound = true
            newStack = if (inclusive) {
                currentStack.subList(0, lastIndexOfKey + 1)
            } else {
                if (lastIndexOfKey + 1 < currentStack.size) {
                    currentStack.subList(0, lastIndexOfKey + 1) // Keep the key itself
                } else {
                    // This case means the key is the last element, and inclusive=false means pop it too.
                    // However, the typical behavior is to keep the key screen unless it's also popped.
                    // For simplicity, if not inclusive and it's the target, it means we want to go *to* it,
                    // effectively making it the new top. If it's the only item, it stays.
                    currentStack.subList(0, lastIndexOfKey + 1)
                }
            }
            // Ensure stack is not empty, if it would be, keep the key itself
            if (newStack.isEmpty() && keyFound) {
                 listOf(key)
            } else {
                newStack
            }
        }
        return keyFound
    }

    fun replaceAll(key: Any) {
        _backStack.value = listOf(key)
    }
}
