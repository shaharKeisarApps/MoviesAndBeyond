# ADR-001: Multi-Module Architecture

## Status

Accepted

## Context

As the app grew beyond a single-module structure, build times increased and module boundaries became unclear. We needed an architecture that enforces separation of concerns and scales with new features.

## Decision

Adopt a hybrid modularization approach combining feature-based and layer-based modules:

- **Core modules** (`core:local`, `core:network`, `core:model`, `core:ui`, `core:testing`) provide shared infrastructure.
- **Feature modules** (`feature:movies`, `feature:tv`, `feature:search`, `feature:details`, `feature:auth`, `feature:you`) encapsulate screens and ViewModels.
- **Data module** owns repository interfaces, implementations, and test doubles.
- **Custom Gradle convention plugins** (`build-logic/`) standardize module configuration.

Feature modules depend on `core:ui` and `data` but never on `core:network` or `core:local` directly.

## Consequences

- Clear dependency boundaries reduce accidental coupling.
- Convention plugins eliminate boilerplate in `build.gradle.kts` files.
- Adding a new feature is a matter of creating a module and applying the `moviesandbeyond.android.feature` plugin.
- Build parallelism improves because independent modules compile concurrently.
