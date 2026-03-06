# ADR-006: Dual Networking Stack (Retrofit + Ktor)

## Status

Accepted

## Context

This project is a learning lab for modern Android technologies. Both Retrofit and Ktor are widely used HTTP clients in the Kotlin ecosystem, each with different design philosophies.

## Decision

Maintain both Retrofit 3.0 and Ktor 3.x side by side in `core:network`.

- Retrofit handles the primary TMDB API calls with Moshi for JSON deserialization.
- Ktor is available as an alternative client to explore its coroutine-native, multiplatform-ready API.
- Both clients share the same base URL and authentication token from `local.properties`.

## Consequences

- The project demonstrates proficiency with both networking libraries.
- Repository implementations can choose either client depending on the use case.
- The dual stack adds some dependency weight but keeps the learning scope broad.
- If the project migrates to KMP in the future, Ktor provides a head start on multiplatform networking.
