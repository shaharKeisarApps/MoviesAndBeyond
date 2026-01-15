---
spec_version: 1.0
feature_name: Baseline Profile Release Integration
description: Integrate baseline profile generation into release builds for improved app startup performance
status: PLANNING
priority: P2
owner: performance-team
created: 2026-01-15
updated: 2026-01-15
---

# Feature: Baseline Profile Release Integration

## Overview

Integrate the existing benchmarks module with the release build process to automatically generate and include baseline profiles in release APKs/AABs.

---

## Current State

### Existing Setup (PR #60)
- `benchmarks` module exists with Macrobenchmark tests
- Baseline profile generation capability available
- Not integrated with release builds
- Manual profile generation required

### What's Missing
- Automatic profile generation during release builds
- CI/CD integration for profile generation
- Profile inclusion in release artifacts
- Performance monitoring baseline

---

## Target State

```
Release Build Process:
1. ./gradlew assembleRelease
2. Automatically generates baseline profile
3. Includes profile in APK/AAB
4. App starts faster on first install
```

---

## Technical Implementation

### 1. App Module Configuration

Add baseline profile configuration to `app/build.gradle.kts`:

```kotlin
plugins {
    id("moviesandbeyond.android.application")
    id("moviesandbeyond.android.application.compose")
    id("moviesandbeyond.android.hilt")
    id("androidx.baselineprofile")  // ADD THIS
}

android {
    // ... existing config ...

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            // ADD: Enable baseline profile for release
            baselineProfile.automaticGenerationDuringBuild = true
        }
    }
}

dependencies {
    // ... existing dependencies ...

    // ADD: Baseline profile dependency
    baselineProfile(projects.benchmarks)
}
```

### 2. Benchmarks Module Configuration

Update `benchmarks/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")  // ADD THIS
}

android {
    namespace = "com.movies.benchmarks"
    compileSdk = 36

    defaultConfig {
        minSdk = 28  // Macrobenchmark requires API 28+
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    // ADD: Test options for baseline profile
    testOptions.managedDevices {
        devices {
            create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6Api34") {
                device = "Pixel 6"
                apiLevel = 34
                systemImageSource = "aosp-atd"
            }
        }
    }
}

baselineProfile {
    // Use managed device for consistent profile generation
    managedDevices += "pixel6Api34"

    // Don't use connected devices by default (CI-friendly)
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.profileinstaller)
}
```

### 3. Baseline Profile Generator

Create `benchmarks/src/main/java/com/movies/benchmarks/BaselineProfileGenerator.kt`:

```kotlin
package com.movies.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "com.movies.moviesandbeyond",
            maxIterations = 10,
            includeInStartupProfile = true,
            profileBlock = {
                // Cold start
                startActivityAndWait()

                // Navigate to Movies tab (already default)
                // Wait for content to load
                device.waitForIdle()

                // Navigate to TV Shows
                device.findObject(By.text("TV Shows")).click()
                device.waitForIdle()

                // Navigate to Search
                device.findObject(By.text("Search")).click()
                device.waitForIdle()

                // Navigate to You/Profile
                device.findObject(By.text("You")).click()
                device.waitForIdle()

                // Navigate back to Movies
                device.findObject(By.text("Movies")).click()
                device.waitForIdle()

                // Open a movie detail (if visible)
                device.findObject(By.res("movie_card")).click()
                device.waitForIdle()

                // Go back
                device.pressBack()
                device.waitForIdle()
            }
        )
    }
}
```

### 4. Startup Profile Generator

Create `benchmarks/src/main/java/com/movies/benchmarks/StartupProfileGenerator.kt`:

```kotlin
package com.movies.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = "com.movies.moviesandbeyond",
            maxIterations = 5,
            includeInStartupProfile = true,
            profileBlock = {
                // Minimal startup path - just launch and wait for first frame
                startActivityAndWait()

                // Wait for initial content load
                device.waitForIdle()
            }
        )
    }
}
```

### 5. CI/CD Integration

Update `.github/workflows/build.yml`:

```yaml
name: Build and Test

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  # ... existing jobs ...

  generate-baseline-profile:
    name: Generate Baseline Profile
    runs-on: macos-latest
    needs: build
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/gradle-build-action@v3

      - name: Accept Android Licenses
        run: yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

      - name: Generate Baseline Profile
        run: ./gradlew :app:generateBaselineProfile

      - name: Upload Baseline Profile
        uses: actions/upload-artifact@v4
        with:
          name: baseline-profile
          path: app/src/main/baselineProfiles/

  release-build:
    name: Release Build with Profile
    runs-on: ubuntu-latest
    needs: generate-baseline-profile
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Download Baseline Profile
        uses: actions/download-artifact@v4
        with:
          name: baseline-profile
          path: app/src/main/baselineProfiles/

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build Release APK
        run: ./gradlew assembleRelease

      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: release-apk
          path: app/build/outputs/apk/release/*.apk
```

### 6. Version Catalog Updates

Add to `gradle/libs.versions.toml`:

```toml
[versions]
# ... existing versions ...
benchmark-macro = "1.2.3"
profileinstaller = "1.3.1"

[libraries]
# ... existing libraries ...
androidx-benchmark-macro-junit4 = { group = "androidx.benchmark", name = "benchmark-macro-junit4", version.ref = "benchmark-macro" }
androidx-profileinstaller = { group = "androidx.profileinstaller", name = "profileinstaller", version.ref = "profileinstaller" }

[plugins]
# ... existing plugins ...
baselineprofile = { id = "androidx.baselineprofile", version = "1.2.3" }
```

### 7. App ProfileInstaller Dependency

Add to `app/build.gradle.kts` dependencies:

```kotlin
dependencies {
    // ... existing dependencies ...

    // Profile installer for baseline profile support
    implementation(libs.androidx.profileinstaller)
}
```

---

## Implementation Tasks

### Phase 1: Configuration
- [ ] Add baselineprofile plugin to project
- [ ] Update benchmarks module configuration
- [ ] Add ProfileInstaller dependency to app

### Phase 2: Profile Generators
- [ ] Create BaselineProfileGenerator
- [ ] Create StartupProfileGenerator
- [ ] Test profile generation locally

### Phase 3: Build Integration
- [ ] Configure automatic generation for release builds
- [ ] Verify profiles included in APK/AAB
- [ ] Test release build with profiles

### Phase 4: CI/CD
- [ ] Add profile generation job to workflow
- [ ] Configure artifact storage
- [ ] Test full CI pipeline

### Phase 5: Validation
- [ ] Benchmark startup time before/after
- [ ] Document performance improvements
- [ ] Monitor production metrics

---

## Success Criteria

| Metric | Baseline | Target |
|--------|----------|--------|
| Cold start time | Measure | -15% minimum |
| TTFD (Time to First Draw) | Measure | -10% minimum |
| Profile size | N/A | < 500KB |
| CI pipeline time | Current | +5 min max |

---

## Testing Plan

### Local Testing
```bash
# Generate profile
./gradlew :benchmarks:pixel6Api34Setup
./gradlew :benchmarks:generateBaselineProfile

# Verify profile created
ls app/src/main/baselineProfiles/

# Build release with profile
./gradlew assembleRelease

# Verify profile in APK
unzip -l app/build/outputs/apk/release/app-release.apk | grep baseline
```

### Benchmarking
```bash
# Run startup benchmark
./gradlew :benchmarks:connectedBenchmarkAndroidTest

# Compare with/without profile
# (Requires two builds for comparison)
```

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| CI time increase | Medium | Run only on main branch |
| Profile staleness | Low | Regenerate on major releases |
| Device compatibility | Low | Use ATD system image |
| APK size increase | Low | Profiles compress well (~100KB) |

---

## Related

- PR #60: Initial benchmarks module
- [Android Baseline Profiles Documentation](https://developer.android.com/topic/performance/baselineprofiles)
- [Macrobenchmark Guide](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-15 | Initial spec |
