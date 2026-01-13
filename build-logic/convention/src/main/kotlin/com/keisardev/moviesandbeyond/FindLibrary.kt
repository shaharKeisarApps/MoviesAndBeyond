package com.keisardev.moviesandbeyond

import org.gradle.api.Project

internal fun Project.findLibrary(alias: String) = versionCatalog.findLibrary(alias).get()

internal fun Project.findVersion(alias: String) = versionCatalog.findVersion(alias).get().toString()
