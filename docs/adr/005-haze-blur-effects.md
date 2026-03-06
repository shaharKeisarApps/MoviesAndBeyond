# ADR-005: Haze for Frosted-Glass Blur Effects

## Status

Accepted

## Context

The app's navigation bar benefits from a frosted-glass blur effect to create visual depth. Android's built-in blur APIs (`RenderEffect.createBlurEffect`) require API 31+ and are not Compose-friendly.

## Decision

Use the Haze library (`dev.chrisbanes.haze`) for cross-version blur effects in Compose.

- `HazeScaffold` wraps the app shell and applies blur to the bottom navigation bar.
- The blur source is the main content area; the navigation bar is the blur target.
- In landscape/rail mode, the blur is not applied since the NavigationRail uses a solid background.

## Consequences

- Consistent frosted-glass effect across API levels without platform-specific branching.
- Haze integrates directly with Compose modifiers, keeping the blur declarative.
- The library adds a runtime dependency but avoids custom `RenderNode` management.
- Blur intensity and tint are configurable per-surface.
