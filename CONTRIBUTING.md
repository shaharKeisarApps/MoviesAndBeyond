# Contributing to MoviesAndBeyond

Thanks for your interest in contributing! This document covers the basics to get you started.

## Getting Started

1. Fork the repository
2. Clone your fork and create a feature branch from `dev`
3. Set up `local.properties` with your TMDB API credentials (see [README](README.md#setup))
4. Open the project in Android Studio and sync Gradle

## Development Workflow

1. Create a branch: `git checkout -b feature/your-feature` (from `dev`)
2. Make your changes
3. Run the quality checks before committing:
   ```bash
   ./gradlew spotlessApply   # Auto-format code
   ./gradlew detekt          # Static analysis
   ./gradlew lintDebug       # Lint checks
   ./gradlew test            # Unit tests
   ```
4. Commit with a clear message describing the change
5. Push your branch and open a Pull Request against `dev`

## Code Quality

The project enforces code quality with:

- **Spotless + ktfmt** for consistent formatting (Google style, 100-char line width)
- **Detekt** for static analysis
- **Android Lint** for Android-specific checks

CI runs all of these automatically on every PR. Please make sure they pass locally before pushing.

## Architecture Guidelines

- Feature modules depend on `core:ui` and `data` (via repository interfaces)
- Feature modules should **not** depend on `core:network` or `core:local` directly
- New ViewModels use `@HiltViewModel` with constructor injection
- Expose UI state via `StateFlow`
- Add KDoc to new public interfaces and key classes

## Module Structure

When adding a new feature module:

1. Create the module under `feature/`
2. Apply the convention plugin: `id("moviesandbeyond.android.feature")`
3. Register the module in `settings.gradle.kts`
4. Add navigation extensions and wire them in `MoviesAndBeyondNavigation.kt`

## Reporting Issues

Use the [issue templates](.github/ISSUE_TEMPLATE/) for bug reports and feature requests.

## License

By contributing, you agree that your contributions will be licensed under the same terms as the project.
