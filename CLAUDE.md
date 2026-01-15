# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MoviesAndBeyond is an Android app that provides movie, TV show, and person information from TMDB API. The app is built using modern Android development practices with Kotlin, Jetpack Compose, and Hilt dependency injection.

## Build and Development Commands

### Building the App
```bash
# Build the project (assembles all variants)
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build artifacts
./gradlew clean
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run unit tests for a specific module (e.g., data module)
./gradlew :data:test

# Run instrumentation tests on connected devices
./gradlew connectedAndroidTest

# Run instrumentation tests for debug variant only
./gradlew connectedDebugAndroidTest
```

### Code Quality

The project uses Spotless with ktfmt for code formatting and Detekt for static analysis.

```bash
# Check code formatting
./gradlew spotlessCheck

# Auto-format code with ktfmt (Google style, 100 char line width)
./gradlew spotlessApply

# Run static analysis with Detekt
./gradlew detekt

# Run lint checks on all variants
./gradlew lint

# Run lint and apply safe fixes
./gradlew lintFix

# Run lint on specific variant
./gradlew lintDebug
./gradlew lintRelease

# Run all verification tasks (includes checks, tests, lint, detekt)
./gradlew check
```

**Code Quality Configuration:**
- Spotless config: `build.gradle.kts` (root)
- Detekt config: `config/detekt/detekt.yml`
- Detekt baseline: `config/detekt/baseline.xml`
- ktfmt settings: Google style, 100 char max width, 4-space indentation

**CI/CD Integration:**
The GitHub Actions workflow (`.github/workflows/build.yml`) runs code quality checks on every push and PR:
1. Code Quality job: Spotless check + Detekt analysis (runs first, fails fast)
2. Build and Test job: Compilation + unit tests (requires code quality to pass)
3. Android Test job: Instrumentation tests on emulator (requires build to pass)

### Compiling Kotlin
```bash
# Compile debug Kotlin sources for app module
./gradlew :app:compileDebugKotlin

# Compile debug sources (includes all compilation tasks)
./gradlew compileDebugSources

# Check for Kotlin Gradle Plugin configuration errors
./gradlew checkKotlinGradlePluginConfigurationErrors
```

## Architecture

### Multi-Module Structure
The project uses a hybrid modularization approach combining feature-based and layer-based modules:

**Core Modules (Layer-based):**
- `core:local` - Local data storage (Room database, DataStore, SharedPreferences, SessionManager)
- `core:model` - Domain models shared across modules
- `core:network` - Network layer (Retrofit, Ktor, TMDB API interface)
- `core:testing` - Shared testing utilities (MainDispatcherRule)
- `core:ui` - Reusable UI components and Compose utilities

**Data Module:**
- `data` - Repository implementations, data layer abstraction, test doubles

**Feature Modules (Feature-based):**
- `feature:auth` - TMDB account authentication
- `feature:details` - Movie/TV/Person details screens
- `feature:movies` - Movies feed and listings
- `feature:search` - Multi-search functionality
- `feature:tv` - TV shows feed and listings
- `feature:you` - User profile and library

**Sync Module:**
- `sync` - Background synchronization with WorkManager

**App Module:**
- `app` - Application entry point, navigation, theme, main activity

### Build Logic Convention Plugins
Custom Gradle convention plugins are defined in `build-logic/convention/` to standardize module configurations:
- `moviesandbeyond.android.application` - Application module configuration
- `moviesandbeyond.android.application.compose` - Application Compose setup
- `moviesandbeyond.android.library` - Library module configuration
- `moviesandbeyond.android.library.compose` - Library Compose setup
- `moviesandbeyond.android.hilt` - Hilt dependency injection setup
- `moviesandbeyond.android.feature` - Feature module configuration (includes library, compose, hilt, and core:ui)

### Dependency Injection with Hilt
The project uses Hilt (Dagger) for dependency injection:
- Repositories are bound in `data/di/RepositoryModule.kt` at `SingletonComponent` scope
- Network components are provided in `core/network/di/NetworkModule.kt`
- Database components are provided in `core/local/di/DatabaseModule.kt`
- Each feature module has Hilt integration via the `moviesandbeyond.android.feature` convention plugin

### Navigation Pattern
Navigation follows type-safe Compose Navigation patterns:
- Each feature module exports navigation functions (e.g., `navigateToMovies()`, `navigateToSearch()`)
- Navigation is centralized in `app/ui/navigation/MoviesAndBeyondNavigation.kt`
- Bottom bar destinations are defined in `app/ui/navigation/MoviesAndBeyondDestination.kt`
- Navigation state is preserved with `saveState` and `restoreState`

### UI Architecture
- **Compose-first**: All UI is built with Jetpack Compose
- **Material3**: Uses Material Design 3 components
- **Haze effect**: Custom blur/frosted glass effects for bottom bar using `dev.chrisbanes.haze` library
- **ViewModel pattern**: Each feature has ViewModels for state management
- **State hoisting**: UI state is hoisted to ViewModels, composables are stateless

### Data Flow
```
Network (TMDB API via Retrofit/Ktor)
    ↓
Repository Layer (data module)
    ↓
ViewModel (feature modules)
    ↓
Composable UI (feature modules)
```

Local data is persisted via:
- Room database for favorites and watchlist content
- DataStore (Proto) for user preferences
- SharedPreferences for session management (SessionManager)

## Setup Requirements

### API Configuration
The app requires TMDB API credentials. Create `local.properties` in the project root:
```properties
ACCESS_TOKEN=your_tmdb_access_token
BASE_URL=https://api.themoviedb.org/3/
```

Obtain your access token from [TMDB API](https://api.themoviedb.org/).

### Build Configuration
- **Kotlin Version**: 2.3.0
- **Java Version**: 17
- **Compile SDK**: 36
- **Min SDK**: Set in convention plugins (check `AndroidApplicationConventionPlugin`)
- **Target SDK**: 36

## Key Dependencies
- **Jetpack Compose BOM**: 2025.12.01
- **Hilt**: 2.57.2
- **Room**: 2.8.4
- **Retrofit**: 3.0.0 (with Moshi converter)
- **Ktor**: 3.3.3
- **Kotlin Serialization**: 1.9.0
- **Navigation Compose**: 2.9.6
- **Coil**: 2.7.0 (image loading)
- **Haze**: 1.7.1 (blur effects)
- **WorkManager**: 2.11.0
- **DataStore**: 1.2.0
- **Code Quality**:
  - **Spotless**: 7.0.3 (code formatting)
  - **ktfmt**: 0.51 (Kotlin formatter by Facebook)
  - **Detekt**: 1.23.8 (static analysis)

## Testing Approach
- Feature modules can use test doubles from `data/testdoubles/` package
- Test repositories (e.g., `TestAuthRepository`, `TestDetailsRepository`) are available in the data module
- Shared testing utilities are in `core:testing`
- Use `MainDispatcherRule` for coroutine testing

## Module Dependencies
When creating or modifying features:
- Feature modules should depend on `core:ui` (automatically included via `moviesandbeyond.android.feature`)
- Feature modules should use the data layer through repository interfaces
- Feature modules should not directly depend on `core:network` or `core:local`
- Use type-safe project accessors: `projects.core.ui`, `projects.data`, etc.

## Common Patterns

### Adding a New Feature Module
1. Create module in `feature/` directory
2. Apply the feature convention plugin in `build.gradle.kts`:
   ```kotlin
   plugins {
       id("moviesandbeyond.android.feature")
   }
   ```
3. Add module to `settings.gradle.kts`: `include(":feature:yourfeature")`
4. Create navigation extension functions (e.g., `navigateToYourFeature()`)
5. Register navigation in `MoviesAndBeyondNavigation.kt`

### ViewModel Pattern
ViewModels should:
- Inject repositories via constructor (Hilt `@Inject constructor`)
- Expose UI state via `StateFlow` or `State`
- Use `lifecycle-runtime-compose` for state collection in Composables
- Handle business logic and data transformation

### Repository Pattern
Repositories follow interface-implementation separation:
- Interface in `data/repository/`
- Implementation in `data/repository/impl/`
- Test doubles in `data/testdoubles/repository/`
- Binding in `data/di/RepositoryModule.kt`

---

## Claude Code Orchestration

This project uses an advanced Claude Code setup with skills, subagents, hooks, and task orchestration.

### Directory Structure
```
.claude/
├── agents/                     # Subagent definitions
│   ├── code-reviewer.md        # Code quality review
│   ├── test-plan-validator.md  # PR test plan validation
│   ├── bug-fixer.md            # Bug investigation and fixing
│   ├── refactor-expert.md      # Code refactoring
│   └── release-manager.md      # Release preparation
├── hooks/                      # Automation hooks
│   └── pre-pr-merge.sh         # Test plan validation before merge
├── skills/                     # Domain expertise skills
│   ├── compose-expert/         # Jetpack Compose patterns
│   ├── coroutines-expert/      # Kotlin coroutines
│   ├── gradle-expert/          # Gradle/build configuration
│   ├── testing-expert/         # Testing patterns
│   ├── store5-expert/          # Store5 caching
│   └── ...                     # See full list below
├── specs/                      # Feature and task specifications
│   ├── features/               # Feature specifications
│   └── tasks/                  # Task definitions and gates
├── settings.json               # Hooks and workflow configuration
└── ORCHESTRATION.md            # Full orchestration documentation
```

### Available Skills (17 total)

| Skill | Domain |
|-------|--------|
| `circuit-expert` | Circuit UI framework |
| `metro-expert` | Metro dependency injection |
| `compose-expert` | Jetpack Compose patterns |
| `coroutines-expert` | Kotlin coroutines & Flow |
| `gradle-expert` | Build configuration |
| `testing-expert` | Unit and integration testing |
| `quality-expert` | Code quality (Detekt, Spotless) |
| `store5-expert` | Store5 caching patterns |
| `ktor-expert` | Ktor HTTP client |
| `usecase-expert` | Use case/interactor patterns |
| `git-expert` | Git workflows |
| `cicd-expert` | CI/CD with GitHub Actions |
| `technical-researcher` | Technical research |
| `navigation3` | Navigation 3 patterns |
| `material3-expressive` | Material 3 expressive |
| `landscapist` | Image loading |
| `shared-elements` | Shared element transitions |

### Subagent Usage

| Subagent | Trigger | Purpose |
|----------|---------|---------|
| `code-reviewer` | "review code", "check PR" | Pre-commit code review |
| `test-plan-validator` | "verify test plan", "ready to merge" | PR test plan validation |
| `bug-fixer` | "bug in", "crash", "doesn't work" | Bug investigation |
| `refactor-expert` | "refactor", "migrate to" | Safe code transformations |
| `release-manager` | "prepare release", "version bump" | Release preparation |

### Task Orchestrator Integration

The project uses Task Orchestrator MCP plugin for feature development:

```
1. Create plan file: .claude/specs/features/[feature-name].md
2. Run: coordinate_feature_development with my [feature-name].md plan file
3. Execute: Ask "What's next?" for automatic task routing
```

See `.claude/ORCHESTRATION.md` for full workflow documentation.

### Quality Gates

All tasks must pass through 6 quality gates before completion:
1. **Implementation Complete**: Code compiles
2. **Code Quality**: spotlessCheck, detekt, lint pass
3. **Testing Complete**: Unit tests pass
4. **Code Review**: code-reviewer subagent approved
5. **Integration Verified**: Full build passes
6. **PR Ready**: Test plan 100% checked

See `.claude/specs/tasks/task-completion-gates.md` for details.

### PR Test Plan Enforcement

Before merging any PR, verify test plan is complete:
```bash
.claude/hooks/pre-pr-merge.sh <PR_NUMBER>
```

All test plan items must be checked `[x]` before merge is allowed
