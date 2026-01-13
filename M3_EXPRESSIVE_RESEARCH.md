# Material 3 Expressive Research

> Research conducted: January 2026
> Latest alpha version: **1.5.0-alpha11** (December 17, 2025)

## Overview

Material 3 Expressive is an expansion of the Material Design 3 system - a set of new features, updated components, and design tactics for creating emotionally impactful UX. It was unveiled at Google I/O 2025 as Material Design's latest evolution.

## Key Sources

- [Android Developers Blog - What's New in Jetpack Compose](https://android-developers.googleblog.com/2025/05/whats-new-in-jetpack-compose.html)
- [Compose Material 3 Releases](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [M3 Expressive Medium Article](https://navczydev.medium.com/express-yourself-designing-with-material-3-expressive-in-compose-215818c18e91)

---

## Latest Alpha Version Details

### Version: 1.5.0-alpha11 (December 17, 2025)

**New Components Added:**
- `ExpandedFullScreenContainedSearchBar` - Full-screen search bar variant
- Multi-aspect Carousels using lazy grids
- Material Expressive List Items with interactions and segmented styling
- Expressive Menu Updates:
  - Toggleable menu items
  - Selectable menu items
  - Menu groups
  - Menu popup with new defaults in `MenuDefaults`

**Stable APIs (from 1.4.0):**
- Multi-browse and uncontained carousel APIs
- `contentPadding` and `horizontalSpacing` parameters for `FilterChip` and `ElevatedFilterChip`

---

## M3 Expressive Components Catalog

### New Expressive Components

| Component | Description | Status |
|-----------|-------------|--------|
| Loading Indicator | Expressive loading animations | Alpha |
| Split Button | Button with split action area | Alpha |
| FAB Menu | Floating action button with menu | Alpha |
| Button Groups | Connected/standard button groupings | Alpha |
| Toolbars | Contextual toolbars | Alpha |
| Expressive List Items | Interactive list items with segmented styling | Alpha |
| HorizontalCenteredHeroCarousel | Centered hero carousel | Alpha |
| VerticalDragHandle | Drag handle component | Alpha |
| SecureTextField | Password entry with visibility toggle | Alpha |
| TimePickerDialog | Unified time picker | Alpha |

### Enhanced Existing Components

- **Buttons**: Press scale animation, ripple enhancement, expressive color tokens
- **Cards**: Hover/press elevation changes, content scale on interaction
- **FAB**: Extended FAB with expand/collapse, icon morph animation
- **Menus**: New toggleable/selectable items, groups, popup variants
- **Search Bars**: Separated collapsed/expanded states

---

## MotionScheme API

### Overview

Material 3 components now use `MotionScheme` for defining motion characteristics. Two schemes are available:

```kotlin
// Access via companion object
MotionScheme.standard()   // Standard motion personality
MotionScheme.expressive() // Expressive motion personality
```

### CompositionLocal Access

```kotlin
// LocalMotionScheme for accessing from CompositionLocalConsumerModifierNodes
MotionTheme.LocalMotionScheme
```

### Motion Token Migration

Components have migrated from spring animations to motion-scheme-based animations:
- **Bottom Sheet**: Uses `MotionScheme`'s fast-effect for hiding/collapsing, default-spatial for expanding
- **All components**: Motion tokens replace hardcoded spring values

### Expressive Motion Characteristics

| Motion Type | Standard | Expressive |
|-------------|----------|------------|
| Duration | Moderate | Longer, more pronounced |
| Easing | Smooth | Bouncy, playful |
| Overshoot | Minimal | More pronounced |
| Personality | Professional | Delightful |

---

## Usage Requirements

### Dependency

```kotlin
// libs.versions.toml
[versions]
material3-expressive = "1.5.0-alpha11"

[libraries]
material3 = { module = "androidx.compose.material3:material3", version.ref = "material3-expressive" }
```

### Opt-In Annotation

```kotlin
// Required for experimental APIs
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyExpressiveComponent() {
    // Use expressive components here
}
```

### Theme Setup

```kotlin
MaterialTheme(
    colorScheme = dynamicColorScheme ?: lightColorScheme,
    typography = Typography,
    // MotionScheme is automatically applied
) {
    // Content
}
```

---

## Expressive Motion Principles

### Core Principles

1. **Emotional Impact**: Motion should evoke positive emotions
2. **Predictability**: Users should understand what will happen
3. **Continuity**: Smooth transitions maintain context
4. **Personality**: Motion expresses brand character

### Implementation Guidelines

```kotlin
// Standard motion for most interactions
MotionScheme.standard()

// Expressive motion for:
// - Celebrations (success states)
// - Delightful moments (favorites, likes)
// - Brand expression (splash, onboarding)
// - User achievements
MotionScheme.expressive()
```

### Duration Guidelines

| Interaction Type | Duration Range |
|------------------|----------------|
| Micro-interactions | 100-200ms |
| Standard transitions | 200-400ms |
| Expressive reveals | 300-500ms |
| Complex animations | 400-700ms |

---

## Migration Considerations

### From Stable M3 to M3 Expressive

1. **Update dependency** to alpha channel (1.5.0-alpha+)
2. **Add opt-in annotations** where using expressive APIs
3. **Review component replacements**:
   - Standard buttons → Expressive buttons with press states
   - Standard cards → Cards with interaction animations
   - Static FAB → FAB with menu capability
4. **Test motion schemes** - verify animations feel appropriate
5. **Performance testing** - ensure 60fps maintained

### Breaking Changes

- M3 Expressive APIs were moved from 1.4.0-alpha to 1.5.0-alpha track
- Not present in stable 1.4.x releases
- Build errors if attempting to use with stable version

---

## Relevant for MoviesAndBeyond

### High-Priority Components

1. **Expressive List Items** - For movie/TV show lists with interactive feedback
2. **FAB Menu** - For quick actions (add to favorites, watchlist)
3. **Loading Indicators** - For async content loading
4. **Button Groups** - For filter selections
5. **Carousels** - For featured content sections

### Motion Opportunities

1. **Feed screens**: Expressive list item animations
2. **Details screen**: Hero image reveal, rating animations
3. **Search**: Search bar expand/collapse transitions
4. **Favorites/Watchlist**: Celebration animations on add
5. **Bottom navigation**: Tab switch animations

### Current State vs Target

| Area | Current | Target (M3 Expressive) |
|------|---------|------------------------|
| Motion Scheme | Not using | `MotionScheme.expressive()` for key moments |
| List Items | Standard | Expressive with segmented styling |
| FAB | None | FAB Menu for quick actions |
| Loading | Shimmer only | Expressive loading indicators |
| Cards | Static | Interactive with elevation/scale |

---

## References

### Official Documentation
- [Material 3 Expressive Design Guidelines](https://m3.material.io)
- [Compose Material 3 API Reference](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary)

### Sample Apps
- [Material 3 Expressive Catalog](https://github.com/meticha/material-3-expressive-catalog)
- [Androidify Sample](https://android-developers.googleblog.com/2025/05/androidify-building-delightful-ui-with-compose.html)

### Articles
- [Express Yourself: Designing with Material 3 Expressive](https://navczydev.medium.com/express-yourself-designing-with-material-3-expressive-in-compose-215818c18e91)
- [Compose Material 3 Expressive Overview](https://zoewave.medium.com/compose-material-3-expressive-89f4147df5b8)
