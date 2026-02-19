# First-Login Flow Redesign: M3 Expressive Design Spec

## Overview

A ground-up redesign of the **OnboardingScreen** and **AuthScreen** into a single cohesive first-login experience. The flow should feel cinematic, premium, and delightful -- worthy of a movie discovery app. Both screens share visual DNA and transition smoothly between each other.

---

## Design Philosophy

- **Cinema-grade immersion**: Deep, dark backgrounds with luminous accents that feel like sitting in a theater before the show.
- **Content-forward**: Minimal chrome; the brand and CTAs take center stage.
- **Expressive motion**: Bouncy springs and staggered reveals create delight without slowing the user down.
- **Cohesive flow**: Onboarding and Auth feel like Act 1 and Act 2 of the same experience, not two separate screens.

---

## Screen 1: OnboardingScreen (Welcome)

### Layout Structure

```
+------------------------------------------+
|                                          |
|           (animated icon/logo)           |  <- Top ~35% of screen
|                                          |
|        M O V I E S  &  B E Y O N D      |  <- Display headline, centered
|                                          |
|     "Discover your next favorite story"  |  <- Subtitle, centered
|                                          |
|                                          |
|     +--------------------------------+   |
|     |       GET STARTED              |   |  <- Primary CTA, wide
|     +--------------------------------+   |
|                                          |
+------------------------------------------+
```

### Visual Hierarchy (Top to Bottom)

1. **Background**: Full-bleed `surface` (`MaterialTheme.colorScheme.surface`). No gradient on the background itself -- let the content breathe against the deep cinematic dark.

2. **Animated Icon Area** (top ~35%):
   - A centered `Icon` using a movie/film-reel icon from Material Icons (e.g., `Icons.Rounded.Movie` or `Icons.Rounded.Theaters`).
   - Rendered inside a large (96.dp) `primaryContainer` circle.
   - Icon tint: `onPrimaryContainer`.
   - Entry animation: scale from 0 to 1 with `Spring.DampingRatioLowBouncy` + `Spring.StiffnessLow` (overshoot bounce).
   - Delay: 0ms (first element to appear).

3. **App Name Headline**:
   - Text: "Movies & Beyond" (or string resource `app_name`)
   - Token: `MaterialTheme.typography.displaySmall` (36sp, SemiBold)
   - Color: `MaterialTheme.colorScheme.onSurface`
   - Entry animation: `fadeIn` + `slideInVertically` (from bottom, 24.dp offset) with expressive spring.
   - Delay: 150ms after icon.

4. **Subtitle**:
   - Text: "Discover your next favorite story" (update the `onboarding_text` string resource)
   - Token: `MaterialTheme.typography.bodyLarge` (16sp, Normal)
   - Color: `MaterialTheme.colorScheme.onSurfaceVariant`
   - Entry animation: `fadeIn` only, tween 400ms.
   - Delay: 300ms after icon.

5. **Get Started Button** (pinned to bottom area):
   - Component: `Button` (filled, primary)
   - Text token: `MaterialTheme.typography.labelLarge` (14sp, Medium)
   - Shape: `MaterialTheme.shapes.large` (16.dp radius) -- CinematicShapes.large
   - Height: 56.dp
   - Width: `fillMaxWidth` with 24.dp horizontal padding
   - Bottom padding: 48.dp from bottom edge (above nav bar safe area)
   - Entry animation: `fadeIn` + `slideInVertically` (from bottom, full offset) with tween 400ms, delay 450ms.
   - Press animation: scale to 0.96f with `Spring.DampingRatioMediumBouncy`.

### Color Mapping

| Element | Color Token |
|---------|------------|
| Screen background | `colorScheme.surface` |
| Icon circle background | `colorScheme.primaryContainer` |
| Icon tint | `colorScheme.onPrimaryContainer` |
| App name text | `colorScheme.onSurface` |
| Subtitle text | `colorScheme.onSurfaceVariant` |
| Button background | `colorScheme.primary` |
| Button text | `colorScheme.onPrimary` |

### Spacing

| Gap | Value |
|-----|-------|
| Top of screen to icon center | ~35% of screen height (use `weight` or fixed 200.dp top spacer) |
| Icon to app name | 24.dp |
| App name to subtitle | 8.dp |
| Bottom of subtitle to button (flex) | `Spacer(weight(1f))` |
| Button bottom padding | 48.dp |
| Button horizontal padding | 24.dp |

---

## Screen 2: AuthScreen (Sign In)

### Layout Structure

```
+------------------------------------------+
|  <-- (back arrow)                        |
|                                          |
|         Welcome back                     |  <- Headline
|  Sign in to your TMDB account to         |
|  unlock favorites, watchlists & more     |  <- Body description
|                                          |
|  +------------------------------------+  |
|  | Username                           |  |  <- OutlinedTextField
|  +------------------------------------+  |
|                                          |
|  +------------------------------------+  |
|  | Password                       [o] |  |  <- OutlinedTextField + toggle
|  +------------------------------------+  |
|                                          |
|  +------------------------------------+  |
|  |           SIGN IN                  |  |  <- Primary Button (filled)
|  +------------------------------------+  |
|                                          |
|  -------------- or ---------------       |  <- Divider with text
|                                          |
|  +------------------------------------+  |
|  |    CONTINUE WITHOUT SIGN IN        |  |  <- Outlined/Tonal Button
|  +------------------------------------+  |
|                                          |
|  Don't have an account? Sign Up          |  <- Annotated text link
|                                          |
+------------------------------------------+
```

### Visual Hierarchy (Top to Bottom)

1. **Top App Bar**:
   - Keep existing `TopAppBarWithBackButton` (transparent, flat).
   - Uses `CinematicElevation.topBar` (0.dp).

2. **Welcome Headline**:
   - Text: "Welcome back" (new string: `auth_welcome_title`)
   - Token: `MaterialTheme.typography.headlineMedium` (28sp, SemiBold)
   - Color: `MaterialTheme.colorScheme.onSurface`
   - Entry animation: `fadeIn` + `slideInVertically` from bottom (16.dp), delay 0ms, spring.

3. **Description**:
   - Text: "Sign in to your TMDB account to unlock favorites, watchlists & more" (new string: `auth_description`)
   - Token: `MaterialTheme.typography.bodyMedium` (14sp, Normal)
   - Color: `MaterialTheme.colorScheme.onSurfaceVariant`
   - Entry animation: `fadeIn`, delay 100ms.
   - Top spacing: 8.dp below headline.

4. **Username Field**:
   - Component: `OutlinedTextField`
   - Shape: `MaterialTheme.shapes.medium` (12.dp) -- CinematicShapes.medium
   - Placeholder token: `MaterialTheme.typography.bodyLarge`
   - Placeholder color: `MaterialTheme.colorScheme.onSurfaceVariant`
   - Outline color: uses default M3 OutlinedTextField colors (outline/primary when focused)
   - Entry animation: `fadeIn` + `slideInVertically` (8.dp from bottom), delay 150ms.
   - Top spacing: 32.dp below description.

5. **Password Field**:
   - Same styling as Username field.
   - Trailing icon (visibility toggle): `MaterialTheme.colorScheme.onSurfaceVariant`.
   - Entry animation: `fadeIn` + `slideInVertically` (8.dp), delay 200ms.
   - Top spacing: 16.dp below username.

6. **Sign In Button** (primary action):
   - Component: `Button` (filled, primary)
   - Text token: `MaterialTheme.typography.labelLarge`
   - Shape: `MaterialTheme.shapes.large` (16.dp) -- consistent with onboarding CTA
   - Height: 56.dp
   - Width: `fillMaxWidth`
   - Entry animation: `fadeIn` + `slideInVertically`, delay 300ms.
   - Press animation: scale to 0.96f with `Spring.DampingRatioMediumBouncy`.
   - Loading state: Replace text with `CircularProgressIndicator` (existing behavior), but use `MaterialTheme.colorScheme.onPrimary` tint.
   - Top spacing: 24.dp below password.

7. **"or" Divider**:
   - Keep existing `HorizontalDivider` + "or" text pattern.
   - Divider color: `MaterialTheme.colorScheme.outlineVariant`
   - "or" text token: `MaterialTheme.typography.labelMedium`
   - "or" text color: `MaterialTheme.colorScheme.onSurfaceVariant`
   - Top spacing: 16.dp.

8. **Continue Without Sign In Button** (secondary action):
   - Component: `FilledTonalButton` (NOT a second filled Button -- must be visually distinct)
   - Text token: `MaterialTheme.typography.labelLarge`
   - Shape: `MaterialTheme.shapes.large` (16.dp)
   - Height: 56.dp
   - Width: `fillMaxWidth`
   - Colors: `secondaryContainer` background, `onSecondaryContainer` text (M3 tonal button defaults)
   - Top spacing: 16.dp below divider.

9. **Sign Up Link**:
   - Text: "Don't have an account? **Sign Up**"
   - Base token: `MaterialTheme.typography.bodySmall`
   - Base color: `MaterialTheme.colorScheme.onSurfaceVariant`
   - "Sign Up" color: `MaterialTheme.colorScheme.primary` with `FontWeight.Bold`
   - Top spacing: 16.dp.
   - Bottom spacing: 24.dp (for scroll padding).

### Color Mapping

| Element | Color Token |
|---------|------------|
| Screen background | `colorScheme.surface` (via Scaffold) |
| Headline text | `colorScheme.onSurface` |
| Description text | `colorScheme.onSurfaceVariant` |
| Text field outline (unfocused) | `colorScheme.outline` |
| Text field outline (focused) | `colorScheme.primary` |
| Text field shape | `shapes.medium` (12.dp) |
| Placeholder text | `colorScheme.onSurfaceVariant` |
| Sign In button bg | `colorScheme.primary` |
| Sign In button text | `colorScheme.onPrimary` |
| Continue button bg | `colorScheme.secondaryContainer` |
| Continue button text | `colorScheme.onSecondaryContainer` |
| Divider | `colorScheme.outlineVariant` |
| "or" label | `colorScheme.onSurfaceVariant` |
| Sign Up link | `colorScheme.primary` |
| Error snackbar | Default M3 snackbar colors |
| Visibility icon | `colorScheme.onSurfaceVariant` |

### Spacing Summary

| Gap | Value |
|-----|-------|
| Top bar to headline | via `paddingValues` from Scaffold |
| Headline to description | 8.dp |
| Description to username field | 32.dp |
| Username to password | 16.dp |
| Password to Sign In button | 24.dp |
| Sign In button to divider | 16.dp |
| Divider to Continue button | 16.dp |
| Continue button to Sign Up link | 16.dp |
| Sign Up link bottom padding | 24.dp |
| Horizontal content padding | 24.dp |

---

## Shape System Usage

| Component | Shape Token | Value |
|-----------|------------|-------|
| Get Started button | `MaterialTheme.shapes.large` | 16.dp rounded |
| Sign In button | `MaterialTheme.shapes.large` | 16.dp rounded |
| Continue button | `MaterialTheme.shapes.large` | 16.dp rounded |
| Text fields | `MaterialTheme.shapes.medium` | 12.dp rounded |
| Icon circle (onboarding) | `CircleShape` | Fully circular |

---

## Elevation / TonalElevation

| Component | TonalElevation | Notes |
|-----------|---------------|-------|
| Onboarding screen | `CinematicElevation.none` (0.dp) | Flat background |
| Auth Scaffold | `CinematicElevation.none` (0.dp) | Flat background |
| Top app bar | `CinematicElevation.topBar` (0.dp) | Transparent/flat |
| Text fields | `CinematicElevation.none` (0.dp) | Outlined style, no elevation |
| Buttons | `CinematicElevation.none` (0.dp) | Filled style, no elevation |
| Snackbar | `CinematicElevation.snackbar` (8.dp) | Default M3 snackbar |

Both screens are intentionally flat (0.dp tonalElevation). The visual hierarchy comes from color contrast and typography scale, not elevation. This is a deliberate design choice: onboarding/auth should feel like an immersive full-screen experience, not a card-based layout.

---

## Motion / Animation Spec

### Guiding Principles
- Use **M3 Expressive springs** for interactive elements (buttons, icon).
- Use **tween** for content reveals (text fading in).
- **Stagger** content appearance top-to-bottom, 100-150ms intervals.
- Keep total reveal under 600ms so the screen feels snappy, not slow.

### OnboardingScreen Animations

| Element | Animation | Spec | Delay |
|---------|-----------|------|-------|
| Icon circle | `scaleIn` | `Spring.DampingRatioLowBouncy`, `Spring.StiffnessLow` | 0ms |
| App name | `fadeIn` + `slideInVertically(offset = -24.dp)` | spring, `DampingRatioNoBouncy`, `StiffnessMediumLow` | 150ms |
| Subtitle | `fadeIn` | tween 400ms | 300ms |
| Get Started button | `fadeIn` + `slideInVertically(offset = fullHeight)` | tween 400ms | 450ms |

### AuthScreen Animations

| Element | Animation | Spec | Delay |
|---------|-----------|------|-------|
| Headline | `fadeIn` + `slideInVertically(offset = 16.dp)` | spring, `DampingRatioNoBouncy`, `StiffnessMediumLow` | 0ms |
| Description | `fadeIn` | tween 300ms | 100ms |
| Username field | `fadeIn` + `slideInVertically(offset = 8.dp)` | tween 300ms | 150ms |
| Password field | `fadeIn` + `slideInVertically(offset = 8.dp)` | tween 300ms | 200ms |
| Sign In button | `fadeIn` + `slideInVertically(offset = 8.dp)` | tween 300ms | 300ms |
| Divider + Continue + Sign Up | `fadeIn` | tween 300ms | 400ms |

### Button Press Animation (Both Screens)

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    label = "button_press_scale"
)
```

Applied via `Modifier.graphicsLayer { scaleX = scale; scaleY = scale }` for performance (draw-only, no relayout).

### Screen Transition (Onboarding -> Auth)

Use the default `NavHost` animation. No custom shared-element transition needed for this phase. The staggered content reveal on the Auth screen provides a natural sense of entrance.

---

## Component Mapping

| UI Element | M3 Component | Notes |
|-----------|-------------|-------|
| Get Started button | `Button` (filled) | Primary emphasis |
| Sign In button | `Button` (filled) | Primary emphasis |
| Continue Without Sign In | `FilledTonalButton` | Secondary emphasis, visually distinct |
| Username input | `OutlinedTextField` | Standard M3 outlined variant |
| Password input | `OutlinedTextField` | With trailing visibility toggle |
| Visibility toggle | `IconButton` + `Icon` | Standard M3 icon button |
| Back navigation | `TopAppBarWithBackButton` | Existing component, keep as-is |
| Divider | `HorizontalDivider` | Standard M3 divider |
| Sign Up link | `AnnotatedClickableText` | Existing custom component |
| Loading indicator | `CircularProgressIndicator` | M3 default, replaces Sign In button content |
| Snackbar | `SnackbarHost` | M3 default for error messages |
| Onboarding icon | `Icon` in `Surface(shape=CircleShape)` | Large decorative element |

---

## Typography Token Assignment

### OnboardingScreen

| Text Element | Typography Token | Weight Override | Notes |
|-------------|-----------------|----------------|-------|
| App name | `displaySmall` | None (SemiBold from token) | 36sp, the hero text |
| Subtitle | `bodyLarge` | None (Normal from token) | 16sp |
| Button text | `labelLarge` | None (Medium from token) | 14sp, all-caps if string resource is uppercase |

### AuthScreen

| Text Element | Typography Token | Weight Override | Notes |
|-------------|-----------------|----------------|-------|
| "Welcome back" | `headlineMedium` | None (SemiBold from token) | 28sp |
| Description | `bodyMedium` | None (Normal from token) | 14sp |
| Field placeholder | `bodyLarge` | None | 16sp |
| Sign In button text | `labelLarge` | None | 14sp |
| Continue button text | `labelLarge` | None | 14sp |
| "or" label | `labelMedium` | None | 12sp |
| "Don't have an account?" | `bodySmall` | None | 12sp |
| "Sign Up" | `bodySmall` | Bold override | 12sp, primary color |

---

## String Resource Changes

### New Strings Needed

| Key | Value | File |
|-----|-------|------|
| `auth_welcome_title` | "Welcome back" | `feature/auth/src/main/res/values/strings.xml` |
| `auth_description` | "Sign in to your TMDB account to unlock favorites, watchlists & more" | `feature/auth/src/main/res/values/strings.xml` |

### Updated Strings

| Key | Current Value | New Value | File |
|-----|--------------|-----------|------|
| `onboarding_text` | "Experience TMDB on your mobile" | "Discover your next favorite story" | `app/src/main/res/values/strings.xml` |

### Unchanged Strings

- `app_name`: "Movies And Beyond" (keep as-is)
- `get_started`: "GET STARTED" (keep as-is)
- `sign_in`: "SIGN IN" (keep as-is)
- `continue_without_sign_in`: "CONTINUE WITHOUT SIGN IN" (keep as-is)
- `no_account`: "Don't have an account?" (keep as-is)
- `sign_up`: "Sign Up" (keep as-is)

---

## Accessibility Requirements

1. **All icons must have `contentDescription`**: The onboarding icon should have `contentDescription = "Movies And Beyond logo"` or similar.
2. **Button labels are self-describing**: "GET STARTED", "SIGN IN" are clear.
3. **Password visibility toggle**: Keep existing `show_password` / `hide_password` content descriptions.
4. **Loading state**: Keep existing `auth_circular_progress_indicator` semantic description.
5. **Minimum touch targets**: All buttons are 56.dp tall, exceeding the 48.dp minimum.
6. **Color contrast**: All text tokens against their backgrounds exceed WCAG AA 4.5:1 ratio (verified by the cinematic palette design).

---

## Dark/Light Theme Considerations

- Both screens use M3 color tokens throughout, so they automatically adapt to light/dark/dynamic color.
- No hardcoded colors anywhere.
- The onboarding icon uses `primaryContainer`/`onPrimaryContainer` which shift appropriately in light mode.
- The `FilledTonalButton` uses `secondaryContainer`/`onSecondaryContainer` which are theme-aware.

---

## Implementation Notes

1. **OnboardingScreen is in `app` module** -- it does NOT have access to Hilt ViewModel. Keep it stateless.
2. **AuthScreen is in `feature:auth`** -- it uses `hiltViewModel()`. Keep the existing ViewModel integration pattern.
3. **Do NOT change the navigation route or ViewModel APIs** -- only the UI composables and their internal structure.
4. **Use `rememberSaveable` for animation trigger state** so configuration changes don't re-trigger entry animations.
5. **The `hideOnboarding` null/boolean logic must be preserved** -- it controls whether the "Continue Without Sign In" option appears.
6. **Replace the hardcoded `RoundedCornerShape(20.dp)` on text fields** with `MaterialTheme.shapes.medium` for consistency.
7. **Replace `Button` for "Continue Without Sign In" with `FilledTonalButton`** to create clear visual hierarchy between primary and secondary actions.
8. **Horizontal padding should increase from 12.dp to 24.dp** on the auth screen for better visual breathing room.
