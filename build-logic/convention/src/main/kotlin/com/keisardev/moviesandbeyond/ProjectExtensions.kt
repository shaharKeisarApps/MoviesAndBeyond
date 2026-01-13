package com.keisardev.moviesandbeyond

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

internal val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.findPlugin(alias: String): Provider<PluginDependency> =
    versionCatalog.findPlugin(alias).get()

internal fun Project.pluginId(alias: String): String = findPlugin(alias).get().pluginId
