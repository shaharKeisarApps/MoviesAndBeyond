# ADR-002: Store5 for Offline-First Caching

## Status

Accepted

## Context

The app needs to display content when offline and minimize redundant network requests. We evaluated plain Room caching, manual cache layers, and Store5.

## Decision

Use Store5 (alpha) with Fetcher + SourceOfTruth backed by Room. Content repositories expose `StoreReadResponse<T>` flows that emit loading, data, and error states.

Key patterns:
- `Fetcher` wraps TMDB API calls.
- `SourceOfTruth` reads/writes Room DAOs.
- Consumers observe `StoreReadResponse` to render loading, cached, and fresh states.

## Consequences

- Offline-first behavior is built-in without manual cache orchestration.
- `StoreReadResponse` unifies loading/cached/error states for the UI layer.
- Store5 is still in alpha, so API surface may change on upgrades.
- The data module owns all Store instances; feature modules remain decoupled from caching details.
