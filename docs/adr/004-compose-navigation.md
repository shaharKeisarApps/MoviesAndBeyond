# ADR-004: Type-Safe Compose Navigation

## Status

Accepted

## Context

The app has multiple screens across feature modules. Navigation needs to be type-safe, support deep linking, and keep feature modules decoupled from each other.

## Decision

Use Jetpack Compose Navigation with type-safe route objects and Navigation 3 for developer-owned back stack management.

- Each feature module exports `NavGraphBuilder` extension functions for registering its screens.
- Each feature module exports `NavController.navigateTo*()` extension functions for type-safe navigation.
- Navigation is assembled centrally in `app/ui/navigation/MoviesAndBeyondNavigation.kt`.
- Bottom bar destinations are defined as an enum in `MoviesAndBeyondDestination.kt`.
- `saveState` and `restoreState` preserve navigation state across tab switches.

## Consequences

- Feature modules define their own routes without knowing about other features.
- The app module is the only place that wires all navigation together.
- Type-safe routes eliminate stringly-typed navigation errors at compile time.
- Navigation 3 is still at 1.0.0, so the API may evolve.
