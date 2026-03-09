package com.keisardev.moviesandbeyond.core.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * A function that installs entry providers into an [EntryProviderScope]. Each feature module
 * contributes one of these via Hilt multibinding.
 */
typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit
