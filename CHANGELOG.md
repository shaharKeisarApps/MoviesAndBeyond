# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-03-05

### Added
- Release signing configuration with environment variable support
- AAB (Android App Bundle) output in CI release workflow
- Hardened ProGuard/R8 rules for Room, Moshi, Kotlin Serialization, Retrofit, and Ktor
- Centralized versioning in `gradle.properties`
- This changelog

### Changed
- Bumped version from 0.0.2 to 1.0.0
- Migrated to AGP 9.0.1
- Updated release workflow to extract version from `gradle.properties`
- Updated minimum API level requirement in release notes to API 28

## [0.0.2] - 2025-12-15

### Added
- Adaptive layout with NavigationRail for landscape and tablets
- WindowSizeClass support
- Edge-to-edge inset handling for compact and rail modes
- UI landscape specialist agent

### Changed
- Feed top padding adjustments for navigation bar clearance

## [0.0.1] - 2025-10-01

### Added
- Initial release
- Browse trending and popular movies and TV shows
- Multi-search across movies, TV shows, and people
- Rich detail screens with cast, crew, ratings, and metadata
- TMDB account integration for watchlists and favorites
- Customizable Material 3 theming with dynamic colors
- Offline support via Store5 caching
- Background sync with WorkManager
- Haze frosted-glass blur effects
- Baseline profiles and macrobenchmarks
- CI pipeline with GitHub Actions

[1.0.0]: https://github.com/shaharKeisarApps/MoviesAndBeyond/compare/v0.0.2...v1.0.0
[0.0.2]: https://github.com/shaharKeisarApps/MoviesAndBeyond/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/shaharKeisarApps/MoviesAndBeyond/releases/tag/v0.0.1
