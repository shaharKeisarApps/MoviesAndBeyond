# ADR-003: Hilt for Dependency Injection

## Status

Accepted

## Context

The multi-module architecture requires a DI framework that supports scoped component hierarchies and integrates with Android lifecycle components (ViewModel, WorkManager).

## Decision

Use Hilt (Dagger) for dependency injection across the project.

- Repository interfaces are bound via `@Binds` in `data/di/RepositoryModule.kt` at `SingletonComponent` scope.
- Network and database providers live in their respective core modules.
- Feature modules get Hilt integration automatically through the `moviesandbeyond.android.feature` convention plugin.
- ViewModels use `@HiltViewModel` with constructor injection.

## Consequences

- Compile-time DI validation catches wiring errors early.
- Convention plugins remove per-module Hilt boilerplate.
- Repository implementations can be swapped for test doubles without changing feature modules.
- Adding new bindings follows a consistent pattern: interface in `data/repository/`, implementation in `data/repository/impl/`, binding in `data/di/`.
