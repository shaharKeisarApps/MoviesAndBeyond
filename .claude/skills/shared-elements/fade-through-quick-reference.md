# Fade Through Quick Reference

**One-Page Reference for Material 3 Fade Through Pattern with Shared Elements**

---

## 🎯 When to Use

- ✅ List → Detail transitions (movies, photos, products)
- ✅ Image-heavy content where visual should be the hero
- ✅ Replacing aggressive slides that compete with shared elements
- ❌ Navigation drawer/tabs (use slide instead)
- ❌ Settings screens (no prominent image)

---

## 📊 Pattern Overview

```
Timeline: ~600ms total
┌────────────────────────────────────────────────────┐
│ Exit (250ms)  │ Gap (50ms) │ Enter (300ms)       │
│ Screen fades  │ Only poster│ Screen fades in     │
│ out quickly   │ visible    │ smoothly            │
│ (Accelerate)  │ (Hero!)    │ (Decelerate)        │
└────────────────────────────────────────────────────┘
        │                                    │
        └──── Shared Element Morph ─────────┘
              (Spring physics, ~400-500ms)
```

---

## 🚀 Implementation (Copy-Paste Ready)

### Material 3 Easing Curves (Define Once)

```kotlin
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

private val EmphasizedDecelerateEasing: Easing =
    CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f) // Smooth enter

private val EmphasizedAccelerateEasing: Easing =
    CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f) // Quick exit
```

### Timing Constants

```kotlin
private const val FADE_THROUGH_EXIT_DURATION = 250  // Fade out
private const val FADE_THROUGH_ENTER_DURATION = 300 // Fade in
private const val FADE_THROUGH_GAP = 50 // Hero moment gap
```

### Navigation 3 Implementation

```kotlin
entry<DetailsRoute>(
    metadata = NavDisplay.transitionSpec {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_THROUGH_ENTER_DURATION,
                delayMillis = FADE_THROUGH_GAP,
                easing = EmphasizedDecelerateEasing
            )
        ).togetherWith(
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                    easing = EmphasizedAccelerateEasing
                )
            )
        )
    } +
    NavDisplay.popTransitionSpec {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_THROUGH_ENTER_DURATION,
                delayMillis = FADE_THROUGH_GAP,
                easing = EmphasizedDecelerateEasing
            )
        ).togetherWith(
            fadeOut(
                animationSpec = tween(
                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                    easing = EmphasizedAccelerateEasing
                )
            )
        )
    }
) { route ->
    DetailsScreen(...)
}
```

### Circuit Implementation

```kotlin
AnimatedContent(
    targetState = backStack.last(),
    transitionSpec = {
        if (targetState.isDetailsScreen()) {
            fadeIn(
                animationSpec = tween(
                    durationMillis = FADE_THROUGH_ENTER_DURATION,
                    delayMillis = FADE_THROUGH_GAP,
                    easing = EmphasizedDecelerateEasing
                )
            ).togetherWith(
                fadeOut(
                    animationSpec = tween(
                        durationMillis = FADE_THROUGH_EXIT_DURATION,
                        easing = EmphasizedAccelerateEasing
                    )
                )
            )
        } else {
            // Other transitions
        }
    }
) { record ->
    CompositionLocalProvider(
        LocalAnimatedVisibilityScope provides this@AnimatedContent
    ) {
        content(record)
    }
}
```

---

## 🎨 Fine-Tuning

### Too Slow?

```kotlin
private const val FADE_THROUGH_EXIT_DURATION = 200  // ↓ from 250ms
private const val FADE_THROUGH_ENTER_DURATION = 250 // ↓ from 300ms
private const val FADE_THROUGH_GAP = 30             // ↓ from 50ms
```

### Too Fast?

```kotlin
private const val FADE_THROUGH_EXIT_DURATION = 300  // ↑ from 250ms
private const val FADE_THROUGH_ENTER_DURATION = 350 // ↑ from 300ms
private const val FADE_THROUGH_GAP = 75             // ↑ from 50ms
```

### Gap Not Noticeable?

```kotlin
private const val FADE_THROUGH_GAP = 75-100 // Increase (max 100ms)
```

---

## ✅ Testing Checklist

- [ ] List fades out smoothly (250ms, no harsh cuts)
- [ ] Brief moment where only poster is visible (50ms gap)
- [ ] Details fades in gently (300ms, soft cushion)
- [ ] Poster morphs continuously with spring physics
- [ ] Total transition feels ~500-600ms (not too fast/slow)
- [ ] Back navigation symmetric and smooth
- [ ] Predictive back gesture works (swipe from left edge)
- [ ] 60fps throughout (no dropped frames)

---

## ⚠️ Common Mistakes

1. ❌ Using `FastOutSlowInEasing` (legacy, not Material 3)
2. ❌ Gap > 100ms (feels sluggish)
3. ❌ Adding slide/scale (defeats fade through purpose)
4. ❌ Using `tween` for shared element (let spring handle it)
5. ❌ Exit slower than enter (violates M3 principle)

---

## 🎓 Why This Works

**Problem:** Slides compete with shared element morphs (two motions to track)

**Solution:** Fade Through
- Zero spatial motion (only opacity)
- 50ms gap where **only** shared element visible
- Users track one motion: the poster morph
- Feels effortless and premium

**Production Examples:**
- Google Photos: Grid → Photo
- Play Store: Card → Detail
- YouTube: Thumbnail → Player
- Netflix: Title → Detail

---

## 📚 Full Documentation

See: `.claude/skills/shared-elements/skill.md`
Section: "Material 3 Fade Through Pattern"
