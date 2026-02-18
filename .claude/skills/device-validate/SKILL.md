---
name: device-validate
description: >
  Device validation for MoviesAndBeyond. Extends the base KMP device-validate
  skill with project-specific screens, edge-to-edge architecture, HazeScaffold,
  Navigation 3 routes, and TMDB data models. Orchestrates M3 compliance,
  Compose patterns, product acceptance, runtime adb testing, and accessibility checks.
argument-hint: "[full|smoke|product|keyboard|design|accessibility|layout|code]"
version: "1.0.0"
updated: "2026-02-16"
extends: device-validate (claude-flow-kmp base)
---

# Device Validation — MoviesAndBeyond

> **Base skill**: This extends the generic KMP `device-validate` from `claude-flow-kmp`.
> Follow the same 4-phase structure (Code Analysis -> Product Acceptance -> Runtime Measurement -> Cross-Reference Report).
> This file provides **MoviesAndBeyond-specific configuration** that plugs into the base framework.

## Arguments

Parse `$ARGUMENTS` to determine scope:
- **`full`** (default): All 4 phases
- **`smoke`**: Quick smoke — render all tabs + details screen scroll + keyboard check
- **`product`**: Full product acceptance (Phase 2 all scenarios + Phase 3)
- **`keyboard`**: IME spacing only (Phase 1 insets review + Phase 3 V2)
- **`design`**: M3 compliance only (Phase 1a)
- **`accessibility`**: A11y only (Phase 1 accessibility + Phase 3 V4)
- **`layout`**: Layout spacing only (Phase 1 layout + Phase 3 V3)
- **`code`**: Code analysis only, no device (Phase 1)

---

## Project Context

- **Package**: `com.keisardev.moviesandbeyond`
- **Activity**: `.MainActivity`
- **Architecture**: HazeScaffold (outer) wraps Navigation 3 NavDisplay with FloatingNavigationBar
- **Edge-to-edge**: `enableEdgeToEdge()` + `WindowCompat.setDecorFitsSystemWindows(window, false)`
- **DI**: Hilt with `@HiltViewModel` ViewModels
- **State**: ViewModel + StateFlow + `collectAsStateWithLifecycle`
- **Navigation**: Navigation 3 with type-safe `NavKey` routes and developer-owned back stack

### Edge-to-Edge Inset Chain (Critical)

1. `MainActivity.kt` -> `enableEdgeToEdge()` + `WindowCompat.setDecorFitsSystemWindows(window, false)`
2. `MoviesAndBeyondApp.kt` -> outer `HazeScaffold` with `contentWindowInsets = WindowInsets.safeDrawing` -> produces paddingValues including status bar + bottom nav
3. `MoviesAndBeyondNav3.kt` line 91 -> `modifier = Modifier.padding(top = paddingValues.calculateTopPadding())` on NavDisplay -> pushes ALL screens down by status bar height
4. Each inner Scaffold has `contentWindowInsets = WindowInsets(0, 0, 0, 0)` (zeroed out to avoid double-consuming)
5. TopAppBars via `TopAppBarWithBackButton` may still consume status bar insets via their own `windowInsets` parameter (M3 default: `WindowInsets.safeDrawing`)

### Known Gotcha: TopAppBar Double Inset

M3 `TopAppBar` defaults to `windowInsets = TopAppBarDefaults.windowInsets` which equals `WindowInsets.safeDrawing`. Since NavDisplay already applies status bar padding, inner TopAppBars must set `windowInsets = WindowInsets(0, 0, 0, 0)` to avoid double padding. Exception: Details screen where backdrop goes edge-to-edge behind status bar.

---

## UI Interaction Strategy

### PREFERRED: Semantic Tapping (device-independent)

Use uiautomator XML dump to find elements by `text`, `content-desc`, or `resource-id`, then tap center of bounds.

```bash
# Dump UI hierarchy
adb shell uiautomator dump /sdcard/ui.xml
adb pull /sdcard/ui.xml /tmp/ui.xml

# Find element by text and tap center
python3 -c "
import xml.etree.ElementTree as ET, subprocess
tree = ET.parse('/tmp/ui.xml')
target_text = 'TARGET_TEXT'
for el in tree.iter('node'):
    if el.get('text') == target_text:
        bounds = el.get('bounds', '')
        parts = bounds.replace('[', ' ').replace(']', ' ').split()
        x = (int(parts[0]) + int(parts[2])) // 2
        y = (int(parts[1]) + int(parts[3])) // 2
        subprocess.run(['adb', 'shell', 'input', 'tap', str(x), str(y)])
        break
"
```

### Navigation Tab Labels

Bottom navigation items have labels: `"Movies"`, `"TV Shows"`, `"Search"`, `"You"`.

### Screen Element Identifiers

**Movies Feed**: Section headers (Now Playing, Popular, etc.), movie poster cards
**TV Feed**: Same structure as Movies
**Search**: Search text field, result cards
**You**: Profile card, Favorites/Watchlist library items, Settings/Info icons
**Details (Movie/TV)**: Backdrop image, back button (content-desc: "Back"), title, rating, cast row, recommendations
**Details (Person)**: Back button, person image, name, biography
**Credits**: Back button, full cast list
**Auth**: WebView for TMDB auth
**Library Items (Favorites/Watchlist)**: Back button, grid of saved items

---

## Critical Files

| Screen | File | Key Concerns |
|--------|------|--------------|
| Movies Feed | `feature/movies/.../FeedScreen.kt` | LazyColumn sections, poster cards, pull-to-refresh |
| Movies Items | `feature/movies/.../ItemsScreen.kt` | Category drill-down, back button, TopAppBar insets |
| TV Feed | `feature/tv/.../FeedScreen.kt` | Same as Movies |
| TV Items | `feature/tv/.../ItemsScreen.kt` | Same as Movies Items |
| Search | `feature/search/.../SearchScreen.kt` | Search field, results, keyboard handling |
| You | `feature/you/.../YouScreen.kt` | Account card, settings dialog, library items |
| Details | `feature/details/.../DetailsScreen.kt` | Backdrop collapse, TopAppBar overlay, edge-to-edge |
| Media Content | `feature/details/.../MediaDetailsContent.kt` | BackdropImageSection parallax, gradient scrim |
| Person Content | `feature/details/.../PersonDetailsContent.kt` | TopAppBar with back, person info |
| Credits | `feature/details/.../CreditsScreen.kt` | Full cast list, TopAppBar |
| Auth | `feature/auth/.../AuthScreen.kt` | WebView, TopAppBar |
| Library Items | `feature/you/.../LibraryItemsScreen.kt` | Back button, content grid |
| TopAppBar | `core/ui/.../TopAppBar.kt` | `TopAppBarWithBackButton` - shared across screens |
| App Shell | `app/.../MoviesAndBeyondApp.kt` | HazeScaffold, bottom nav, inset chain |
| Navigation | `app/.../MoviesAndBeyondNav3.kt` | NavDisplay, route entries, top padding |
| Routes | `app/.../Routes.kt` | Type-safe NavKey routes |
| Theme | `core/ui/.../theme/` | M3 theme, colors, typography, spacing |

---

## Phase 1: Code Analysis (Subagents in Parallel)

### 1a. Material Design 3 Compliance
**Subagent**: `material-design-expert`
**Prompt**: Review MoviesAndBeyond's Compose UI for M3 compliance:
- Correct M3 component usage (HazeScaffold, Scaffold, TopAppBar, BottomSheetScaffold)
- Proper color tokens (no hardcoded colors except semantic red for favorites)
- Typography scale (bodyLarge, titleMedium, headlineMedium, etc.)
- Spacing consistency (Spacing.xs/sm/md/lg/xl tokens)
- Edge-to-edge and window insets (see Known Gotcha above)
- Dark mode support (MaterialTheme.colorScheme throughout)
- FloatingNavigationBar blur effect correct

Files: All composable files in `feature/` and `core/ui/`.

### 1b. Compose Pattern Analysis
**Subagent**: `code-reviewer`
**Prompt**: Review Compose best practices:
- `Modifier` parameter on all public composables
- State hoisting (state in ViewModels, not composables)
- Recomposition stability (State classes stable)
- LazyColumn/LazyRow key usage
- `collectAsStateWithLifecycle` usage (not `collectAsState`)
- No side effects in composition scope
- Proper remember/derivedStateOf usage

### 1c. Accessibility Review
**Subagent**: `code-reviewer`
**Prompt**: Review accessibility:
- All Icon() calls have contentDescription
- Touch targets >= 48dp
- Navigation items have labels
- Back buttons have content-desc "Back"
- Screen titles as headings

### 1d. Inset Architecture Review
**Subagent**: `code-reviewer`
**Prompt**: Review the full inset chain:
- HazeScaffold contentWindowInsets = WindowInsets.safeDrawing
- NavDisplay top padding from paddingValues
- Inner Scaffolds all have contentWindowInsets = WindowInsets(0, 0, 0, 0)
- TopAppBars NOT consuming safeDrawing (except Details edge-to-edge case)
- No duplicate windowInsetsPadding modifiers

---

## Phase 2: Product Acceptance Testing

### P1: Tab Navigation
Navigate to each tab (Movies, TV, Search, You) and verify:
- Screen renders without crash
- Content loads (or shows loading indicator)
- Bottom nav remains visible and interactive
- Status bar content not overlapping

### P2: Details Flow
- Tap a movie poster -> Details screen opens
- Backdrop image visible, scrolls with parallax
- Back button works (content-desc "Back")
- Cast section scrollable, "View All" navigates to Credits
- Credits screen shows full cast with back button

### P3: Library Flow (requires login)
- Navigate to You -> Login -> Authenticate via WebView
- Return to You screen with account details
- Favorites/Watchlist counts shown
- Tap Favorites -> Library Items screen with correct items
- Back button returns to You screen

### P4: Search Flow
- Navigate to Search tab
- Tap search field -> keyboard opens
- Type query -> results appear
- Tap result -> Details screen opens
- Back returns to search with query preserved

### P5: Settings Dialog
- Navigate to You -> Tap Settings icon
- Dialog opens with theme, dark mode, seed color options
- Toggle switches work
- Dismiss dialog

---

## Phase 3: Runtime Technical Validation

### V1: All Screens Render
Launch app and navigate to all 4 tabs, take screenshots.

### V2: Keyboard Spacing
Navigate to Search -> tap search field -> measure gap between field and keyboard.
**Threshold**: <= 24dp gap.

### V3: Layout Spacing
Per screen: dump UI, find views >500px wide AND >300px tall with no text/content-desc.
**Threshold**: 0 unexplained empty views.

### V4: Accessibility Scan
Per screen: find `clickable=true` nodes missing `content-desc` AND `text`.
**Threshold**: 0 missing labels.

### V5: Touch Target Size
Parse all `clickable=true` bounds, check >= 48dp.
**Threshold**: All targets >= 48x48dp.

### V6: Inset Verification (Project-Specific)
For each screen with TopAppBar:
- Dump UI -> find TopAppBar bounds -> verify top matches expected position
- Expected: TopAppBar top at ~status_bar_height (not 0, not 2x status bar)
- Details screen: TopAppBar at status_bar_height, backdrop at 0 (edge-to-edge)

---

## Phase 4: Cross-Reference Report

Use the standard report template from the base skill. Add project-specific sections:

### Inset Health Check
| Screen | TopAppBar Top (px) | Expected | Status |
|--------|-------------------|----------|--------|
| Movies Feed | - | no TopAppBar | N/A |
| Movies Items | X | status_bar | PASS/FAIL |
| Details (Movie) | X | status_bar | PASS/FAIL |
| Person Details | X | status_bar | PASS/FAIL |
| Credits | X | status_bar | PASS/FAIL |
| Auth | X | status_bar | PASS/FAIL |
| Library Items | X | status_bar | PASS/FAIL |

### Edge-to-Edge Verification
| Check | Expected | Actual | Status |
|-------|----------|--------|--------|
| Details backdrop behind status bar | Yes | | |
| Bottom nav floating over content | Yes | | |
| Status bar scrim on details | Gradient visible | | |

---

## Important Notes

- Use `keyevent 111` (ESCAPE) to dismiss keyboard, NOT `keyevent 4` (BACK) — BACK triggers Navigator back
- Always `sleep 1.5-2s` after navigation before uiautomator dump (animation must complete)
- FloatingNavigationBar overlays content — it does NOT use system navigation bar insets
- Details screen is unique: backdrop goes edge-to-edge, TopAppBar sits over it with transparent background
- PersonDetailsContent has its own inner Scaffold with TopAppBar (different from Movie/TV details)
