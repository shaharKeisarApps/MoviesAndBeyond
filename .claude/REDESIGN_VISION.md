# TMDB App Redesign Vision

## Design Philosophy

**Vision Statement**: Transform movie discovery from browsing a database into exploring a cinematic universe.

**User Should Feel**:
- Excited to discover new content
- Immersed in beautiful movie artwork
- Confident in quality through visible ratings
- Delighted by fluid, polished interactions

**Design Principles**:
1. **Cinematic**: Let the content be the hero - large imagery, immersive feel
2. **Discoverable**: Make exploration intuitive and exciting
3. **Informative**: Surface quality signals (ratings) without tapping
4. **Fluid**: Smooth transitions, purposeful motion everywhere
5. **Premium**: Every detail polished, no rough edges

---

## Design System

### Color Palette

```kotlin
// =============================================================================
// CINEMATIC DARK THEME (Primary - Movies look best on dark)
// =============================================================================

object CinematicColors {
    // Brand - Deep cinematic blue with red accent
    val Primary = Color(0xFF3D5AFE)          // Electric blue for CTAs
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1A237E) // Deep blue container
    val OnPrimaryContainer = Color(0xFFB6C4FF)

    // Secondary - Warm accent for highlights
    val Secondary = Color(0xFFFF5252)         // Cinema red accent
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFF5C0011)
    val OnSecondaryContainer = Color(0xFFFFDAD6)

    // Tertiary - Gold for ratings
    val Tertiary = Color(0xFFFFD700)          // Gold for ratings/stars
    val OnTertiary = Color(0xFF000000)
    val TertiaryContainer = Color(0xFF4A3D00)
    val OnTertiaryContainer = Color(0xFFFFE082)

    // Surface Hierarchy (Dark-first for cinematic feel)
    val Background = Color(0xFF0A0A0F)        // Near black with blue tint
    val OnBackground = Color(0xFFE6E1E5)

    val Surface = Color(0xFF0F0F14)           // Slightly elevated
    val OnSurface = Color(0xFFE6E1E5)

    val SurfaceVariant = Color(0xFF1A1A22)    // Cards, elevated surfaces
    val OnSurfaceVariant = Color(0xFFCAC4D0)

    val SurfaceContainer = Color(0xFF141419)  // Container surfaces
    val SurfaceContainerHigh = Color(0xFF1E1E24) // Higher elevation
    val SurfaceContainerHighest = Color(0xFF28282F) // Highest elevation

    val SurfaceDim = Color(0xFF050508)        // Deepest layer
    val SurfaceBright = Color(0xFF38383F)     // Highlights

    // Semantic Colors
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)

    // Rating Colors (for visual quality indicators)
    val RatingExcellent = Color(0xFF4CAF50)   // Green (8.0+)
    val RatingGood = Color(0xFFFFC107)        // Amber (6.0-7.9)
    val RatingAverage = Color(0xFFFF9800)     // Orange (4.0-5.9)
    val RatingPoor = Color(0xFFF44336)        // Red (<4.0)

    // Star/Rating Gold
    val StarGold = Color(0xFFFFD700)
    val StarGoldDim = Color(0xFFB8860B)

    // Outline
    val Outline = Color(0xFF938F99)
    val OutlineVariant = Color(0xFF49454F)

    // Scrim for overlays
    val Scrim = Color(0xFF000000)
}

// Light theme (secondary, for light mode users)
object CinematicColorsLight {
    val Primary = Color(0xFF1A4BD2)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFDEE1FF)
    val OnPrimaryContainer = Color(0xFF00105C)

    val Background = Color(0xFFFFFBFF)
    val OnBackground = Color(0xFF1C1B1E)

    val Surface = Color(0xFFFFFBFF)
    val OnSurface = Color(0xFF1C1B1E)

    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454F)

    // ... (complete light palette)
}
```

### Typography Scale

```kotlin
object CinematicTypography {
    // Display - Hero titles, featured content names
    val DisplayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp
    )
    val DisplayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    )
    val DisplaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    )

    // Headlines - Section headers, movie titles on cards
    val HeadlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    )
    val HeadlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    )
    val HeadlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )

    // Titles - Card titles, list items
    val TitleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )
    val TitleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    )
    val TitleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )

    // Body - Descriptions, overviews
    val BodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    )
    val BodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    )
    val BodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    )

    // Labels - Chips, badges, metadata
    val LabelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )
    val LabelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
    val LabelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
}
```

### Spacing System (Enhanced)

```kotlin
object CinematicSpacing {
    // Base scale
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
    val huge = 80.dp

    // Semantic aliases
    val screenPaddingHorizontal = md    // 16.dp - generous screen edges
    val screenPaddingVertical = sm      // 12.dp
    val sectionSpacing = xl             // 32.dp - breathing room between sections
    val itemSpacing = sm                // 12.dp - items in rows
    val cardSpacing = md                // 16.dp - cards in grids
    val headerContentSpacing = md       // 16.dp - header to content
    val inlineSpacing = xs              // 8.dp - inline elements
    val chipSpacing = xs                // 8.dp - between chips
}
```

### Shape System

```kotlin
object CinematicShapes {
    // Rounded corners scale
    val extraSmall = RoundedCornerShape(4.dp)   // Chips, badges
    val small = RoundedCornerShape(8.dp)        // Small cards, inputs
    val medium = RoundedCornerShape(12.dp)      // Standard cards
    val large = RoundedCornerShape(16.dp)       // Featured cards
    val extraLarge = RoundedCornerShape(24.dp)  // Bottom sheets, modals
    val full = RoundedCornerShape(50)           // Pills, circular buttons

    // Specific components
    val posterCard = RoundedCornerShape(12.dp)
    val backdropCard = RoundedCornerShape(16.dp)
    val heroCard = RoundedCornerShape(20.dp)
    val chip = RoundedCornerShape(8.dp)
    val button = RoundedCornerShape(12.dp)
    val searchBar = RoundedCornerShape(28.dp)   // Pill-shaped
    val bottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}
```

### Elevation System

```kotlin
object CinematicElevation {
    val none = 0.dp
    val extraLow = 1.dp
    val low = 2.dp
    val medium = 4.dp
    val high = 8.dp
    val extraHigh = 12.dp
    val max = 16.dp

    // Component-specific
    val card = medium           // 4.dp
    val cardPressed = low       // 2.dp
    val cardHovered = high      // 8.dp
    val fab = high              // 8.dp
    val topBar = none           // 0.dp (transparent)
    val bottomBar = none        // 0.dp (blurred)
    val dialog = extraHigh      // 12.dp
}
```

---

## Component Library

### 1. MediaHeroCarousel

**Purpose**: Featured content showcase at top of feed screens

**Specs**:
- Height: 55% of viewport (min 300.dp, max 400.dp)
- HorizontalPager with 5 featured items
- Auto-advancement: 5 seconds per page
- Gradient overlay: Bottom 50%, Black 0% → 80% alpha
- Page indicators: Pill-shaped, bottom center

**Content**:
- Backdrop image (16:9, full-bleed)
- Title (DisplaySmall)
- Tagline (BodyMedium, if available)
- Rating badge with star
- Genre chips (2-3 max)
- CTAs: "Details" (primary), "Add to List" (secondary icon)

**Animations**:
- Page transition: Scale (0.85 → 1.0) + Fade (0.5 → 1.0)
- Indicator: Width animate (8.dp → 24.dp for selected)
- Auto-advancement with pause on touch

### 2. MediaPosterCard

**Purpose**: Standard movie/TV poster card for rows

**Sizes**:
- Small: 100.dp × 150.dp (compact rows)
- Medium: 120.dp × 180.dp (standard rows)
- Large: 140.dp × 210.dp (featured rows)

**Specs**:
- Image: 2:3 aspect ratio poster
- Corner radius: 12.dp
- Elevation: 4.dp (2.dp pressed)
- Rating badge: Top-right corner, optional

**Content**:
- Poster image with shimmer loading
- Optional: Rating badge overlay
- Optional: Title below (TitleSmall)

**Animations**:
- Press: Scale to 0.96, elevation to 2.dp (spring, medium bouncy)
- Shared element: Bounds transform to detail

### 3. MediaBackdropCard

**Purpose**: Wide cards for "Trending" or "Featured" sections

**Size**: Full row width - 32.dp padding, 180.dp height

**Specs**:
- Image: 16:9 backdrop
- Corner radius: 16.dp
- Gradient overlay: Left 60%, Black 0% → 70% alpha

**Content**:
- Backdrop image
- Title (TitleLarge, left-aligned)
- Year • Rating (BodyMedium)
- Genre chips below

**Animations**:
- Press: Scale to 0.98 (spring)
- Parallax: Subtle image shift on scroll

### 4. MediaInfoCard

**Purpose**: Poster + text info for search results, lists

**Size**: Full width, ~100.dp height

**Specs**:
- Layout: Row (poster left, info right)
- Poster: 60.dp × 90.dp (small)
- Corner radius: 8.dp

**Content**:
- Poster thumbnail
- Title (TitleMedium)
- Year • Type badge • Runtime (BodySmall)
- Rating with star (LabelMedium)
- Overview excerpt (BodySmall, 2 lines max)

### 5. SectionRow

**Purpose**: Horizontal scrolling content section

**Specs**:
- Header: Title (TitleLarge) + "See All" button
- Content: LazyRow with configurable card type
- Padding: 16.dp horizontal screen edges
- Item spacing: 12.dp

**Variants**:
- PosterRow: MediaPosterCard items
- BackdropRow: MediaBackdropCard items
- PersonRow: PersonCard items

### 6. RatingBadge

**Purpose**: Visual quality indicator

**Sizes**:
- Small: 32.dp pill (compact)
- Medium: 40.dp pill (standard)
- Large: 48.dp pill (featured)

**Specs**:
- Background: Color-coded (green/amber/orange/red based on score)
- Icon: Star (filled)
- Text: Score (1 decimal)
- Corner radius: Full (pill)

**Color Logic**:
- 8.0+ = Green (Excellent)
- 6.0-7.9 = Amber (Good)
- 4.0-5.9 = Orange (Average)
- < 4.0 = Red (Poor)

### 7. GenreChip

**Purpose**: Filterable genre tags

**Specs**:
- Height: 32.dp
- Padding: 12.dp horizontal
- Corner radius: 8.dp
- Background: SurfaceVariant (unselected), Primary (selected)

**States**:
- Default: SurfaceVariant bg, OnSurfaceVariant text
- Selected: Primary bg, OnPrimary text
- Press: Scale 0.95

### 8. PersonCard

**Purpose**: Cast/crew display

**Size**: 80.dp × 120.dp

**Specs**:
- Image: Circular, 64.dp diameter
- Corner radius: Full (circle)
- Below: Name (LabelMedium), Role (LabelSmall)

### 9. StateViews

**EmptyState**:
- Icon: 80.dp, OnSurfaceVariant
- Title: HeadlineSmall
- Subtitle: BodyMedium, OnSurfaceVariant
- CTA button: Primary, optional

**ErrorState**:
- Icon: Warning, Error color
- Title + message
- Retry button

**LoadingState**:
- Shimmer grid/row matching content layout

---

## Screen Redesigns

### Home Screen (Movies/TV Feed)

**Layout** (top to bottom):
1. **Hero Carousel** (55% viewport)
   - 5 featured items with auto-advancement
   - Full-bleed backdrop images
   - Title, rating, genre chips overlay
   - Page indicators at bottom

2. **Trending Section**
   - "Trending Now" header with See All
   - BackdropCard row (large, immersive)
   - Shows top 5 trending

3. **Content Sections** (4x)
   - "Now Playing" / "Airing Today" - Large poster cards
   - "Popular" - Medium poster cards with ratings
   - "Top Rated" - Numbered list style (1, 2, 3...)
   - "Upcoming" / "On Air" - Medium cards with release dates

**Visual Hierarchy**:
- Hero: 55% viewport, demands attention
- Trending: Full-width backdrops, secondary importance
- Sections: Standard rows, tertiary importance

**Interactions**:
- Pull-to-refresh with custom indicator
- Scroll-aware system bars (immersive in hero)
- Snap scrolling in hero carousel
- Smooth horizontal scroll in sections

### Details Screen

**Layout**:
1. **Immersive Hero** (60% viewport)
   - Full-bleed backdrop
   - Gradient overlay (bottom 60%)
   - Back button: Floating, top-left
   - Poster: Floating, overlapping hero/content transition
   - Title: DisplaySmall over gradient
   - Year • Runtime • Rating badge

2. **Action Bar**
   - Play Trailer (Primary, large)
   - Add to Favorites (Icon button)
   - Add to Watchlist (Icon button)
   - Share (Icon button)

3. **Genre Chips**
   - Horizontal row of GenreChips

4. **Content Tabs**
   - Overview (default)
   - Cast & Crew
   - Similar
   - (Reviews if available)

5. **Overview Tab**
   - Synopsis (BodyLarge, expandable)
   - Additional details (release date, budget, etc.)

6. **Cast Tab**
   - Horizontal PersonCard row
   - "See Full Cast" link

7. **Similar Tab**
   - 3-column poster grid
   - RatingBadge on each

**Scroll Behavior**:
- Backdrop parallax (0.5x scroll speed)
- Backdrop collapse to 100.dp on scroll
- Title animates to app bar on collapse
- System bars become visible on scroll

### Search Screen

**Layout**:
1. **Search Bar** (sticky top)
   - Pill-shaped, full width - 32.dp padding
   - Animated search icon
   - Animated clear button
   - Placeholder: Animated suggestions

2. **Empty State** (no query)
   - "Trending Searches" section
   - "Browse by Genre" chips grid
   - Recent searches (if any)

3. **Results State**
   - Filter chips: All | Movies | TV Shows | People
   - Results grid: 2 columns MediaInfoCard
   - Infinite scroll with loading indicator

### Library Screen (Favorites/Watchlist)

**Layout**:
1. **Header**
   - Title: "My Library"
   - Tab row: Favorites | Watchlist

2. **Filter Bar**
   - Type chips: All | Movies | TV Shows
   - Sort dropdown: Date Added | Rating | Title

3. **Content Grid**
   - 2 columns of MediaPosterCard with RatingBadge
   - Long-press for quick actions (remove)

4. **Empty State**
   - Friendly illustration
   - "Start building your collection"
   - "Explore" CTA button

---

## Motion & Animation Strategy

### Principles
1. **Purposeful**: Every animation communicates something
2. **Quick**: 200-300ms for most, 400ms max for complex
3. **Natural**: Spring physics where appropriate
4. **Consistent**: Same patterns everywhere

### Transition Specs

**Screen Transitions**:
- Navigation: Fade (200ms) + Slide (300ms, spring)
- Shared element: 400ms, FastOutSlowIn

**Component Animations**:
- Card press: Scale 0.96, 150ms spring (stiffness: Medium, damping: MediumBouncy)
- Button press: Scale 0.95, 100ms spring
- Chip press: Scale 0.92, 100ms
- Rating appear: Scale 0 → 1, 300ms spring + overshoot

**Carousel**:
- Page transition: 400ms, spring
- Indicator: 200ms width animate
- Auto-advance: 5s delay, 400ms transition

**Loading**:
- Shimmer: 1000ms, linear, infinite
- Content fade-in: 300ms, FastOutSlowIn

**Micro-interactions**:
- Favorite toggle: Heart scale 1 → 1.3 → 1, with particle burst
- Add to list: Checkmark draw animation, 300ms
- Pull-to-refresh: Custom spinner with cinema reel animation

---

## Implementation Sprints

### Sprint 1: Design Foundation (S - 2 hours)
- [ ] Create `CinematicColors.kt` with full palette
- [ ] Create `CinematicTypography.kt` with scale
- [ ] Update `Spacing.kt` with new values
- [ ] Create `CinematicShapes.kt` with tokens
- [ ] Create `CinematicElevation.kt` with tokens
- [ ] Update `Theme.kt` to use new system
- [ ] Verify dark theme looks cinematic

### Sprint 2: Core Components (M - 3 hours)
- [ ] MediaPosterCard (3 sizes, rating badge, animations)
- [ ] MediaBackdropCard (gradient, info overlay)
- [ ] RatingBadge (color-coded, sizes)
- [ ] GenreChip (selectable states)
- [ ] SectionRow (header + content pattern)
- [ ] Update ShimmerCard for new sizes
- [ ] PersonCard for cast sections

### Sprint 3: Home Screen Redesign (L - 4 hours)
- [ ] MediaHeroCarousel component
- [ ] Hero backdrop with gradient
- [ ] Auto-advancement with indicators
- [ ] Rebuild FeedScreen with hero + sections
- [ ] Trending section with backdrop cards
- [ ] Section rows with varied card sizes
- [ ] Pull-to-refresh

### Sprint 4: Details Screen Redesign (L - 4 hours)
- [ ] Immersive hero area (60% viewport)
- [ ] Parallax backdrop scroll
- [ ] Floating poster with shadow
- [ ] Action bar with trailer button
- [ ] Tab navigation (Overview/Cast/Similar)
- [ ] Cast section with PersonCards
- [ ] Similar content grid

### Sprint 5: Remaining Screens (M - 3 hours)
- [ ] Search: Empty state with trending
- [ ] Search: Filter chips
- [ ] Search: Rich result cards
- [ ] Library: Tab bar + filters
- [ ] Library: Grid with actions
- [ ] Library: Empty states
- [ ] Profile: Visual refresh

### Sprint 6: Polish & Delight (M - 3 hours)
- [ ] Shared element transitions tuned
- [ ] All micro-interactions verified
- [ ] Haptic feedback on key actions
- [ ] Loading state polish
- [ ] Error state polish
- [ ] Performance optimization
- [ ] Final visual QA pass

---

## Success Metrics

After redesign, the app should:

1. **Visual Impact**: First screen makes user say "wow"
2. **Content Showcase**: Movie artwork displayed prominently
3. **Quality Signals**: Can assess movie quality without tapping
4. **Smooth Flow**: All transitions buttery smooth
5. **Delight Moments**: Multiple micro-interactions that surprise
6. **Consistency**: Every screen feels part of same system
7. **Performance**: No jank, instant response

**Quality Bar**: Would a user screenshot this and share it? If yes, we succeeded.
