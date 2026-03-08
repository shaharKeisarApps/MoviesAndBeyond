# ADR-006: Ktor Networking with Kotlinx Serialization

## Status

Accepted (supersedes dual networking stack decision)

## Context

The project previously maintained both Retrofit 3.0 (with Moshi) and Ktor side by side in `core:network` as a learning exercise. Over time, this dual stack added unnecessary complexity — all 15+ TMDB API endpoints used Retrofit while Ktor sat unused.

## Decision

Migrate fully to Ktor 3.4 with Kotlinx Serialization, removing Retrofit, Moshi, and OkHttp entirely.

- Ktor `HttpClient(Android)` handles all TMDB API calls.
- Kotlinx Serialization replaces Moshi for JSON deserialization via `ContentNegotiation` plugin.
- Error mapping uses `HttpResponseValidator` to convert HTTP status codes to typed `NetworkError` exceptions.
- Authentication and base URL are configured via `defaultRequest` plugin.

## Consequences

- Single networking stack reduces dependency count and APK size.
- Kotlinx Serialization is multiplatform-ready, easing a potential KMP migration.
- Ktor's coroutine-native API integrates naturally with the existing coroutine-based architecture.
- The `ktor-client-android` engine delegates to Android's `HttpURLConnection`, respecting `NetworkSecurityConfig` and system proxy settings.
