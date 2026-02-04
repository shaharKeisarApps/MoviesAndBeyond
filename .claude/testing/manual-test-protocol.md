# Manual Testing Protocol

**Purpose:** Comprehensive manual testing checklist for bug fixes and features
**Target Time:** 15-20 minutes per test run
**When to Run:** Before marking tasks complete, before creating PRs

---

## 🚀 Quick Start

```bash
# Run build verification script (handles build + install + displays checklist)
bash .claude/scripts/verify-fixes.sh
```

---

## 📋 Core Test Scenarios

### Test A: Guest Mode Favorites (Priority 1)

**Duration:** 5 minutes
**Tests:** Guest user local favorites persistence

**Steps:**
1. [ ] If logged in, log out (You screen → Log Out)
2. [ ] Navigate to Movies tab
3. [ ] Tap any movie poster
4. [ ] Tap heart icon to add to favorites
5. [ ] **Verify**: Toast shows "Added to favorites"
6. [ ] Navigate to You tab
7. [ ] **Verify**: "Your Library" section visible below login prompt
8. [ ] Tap "Favorites" in library section
9. [ ] **Verify**: Movie appears in favorites list with poster
10. [ ] Press back to return to You screen
11. [ ] Close app completely (swipe away from recents)
12. [ ] Reopen app
13. [ ] Navigate to You → Favorites
14. [ ] **Verify**: Favorite persists after app restart

**Expected Behavior:**
- ✅ Library section visible in guest mode
- ✅ Favorites accessible without authentication
- ✅ Local favorites persist across app restarts
- ✅ UI shows correct poster images

**Bug #1 Fixed:** LibrarySection now present in LoggedOutView

---

### Test B: TMDB Sync After Login (Priority 1)

**Duration:** 5 minutes
**Tests:** Authenticated user TMDB favorites sync

**Prerequisites:**
- Have a TMDB account with at least 3 favorites on tmdb.org

**Steps:**
1. [ ] If logged in, log out
2. [ ] Navigate to You tab
3. [ ] Tap "Log In" button
4. [ ] Complete TMDB authentication flow
5. [ ] **Verify**: Redirected back to You screen
6. [ ] **Verify**: Account details appear (username, avatar)
7. [ ] Wait 2-3 seconds (allow sync to complete)
8. [ ] Tap "Favorites" in library section
9. [ ] **Verify**: TMDB favorites appear in list
10. [ ] Count number of items
11. [ ] Open tmdb.org in browser
12. [ ] Navigate to your favorites
13. [ ] **Verify**: Same number of favorites in app and website
14. [ ] **Verify**: Same movies/shows appear in both
15. [ ] Close app and reopen
16. [ ] **Verify**: Favorites still visible (persisted)

**Expected Behavior:**
- ✅ Sync triggers immediately after login (within 3 seconds)
- ✅ All TMDB favorites appear in app
- ✅ Favorites persist locally (offline access)
- ✅ No duplicate entries

**Bug #2 Fixed:** Immediate sync now called in getAccountDetails()

---

### Test C: Edge-to-Edge Status Bar (Priority 1)

**Duration:** 2 minutes
**Tests:** Edge-to-edge rendering and status bar transparency

**Steps:**
1. [ ] Open app (fresh launch)
2. [ ] **Verify**: Status bar background matches Movies feed background
3. [ ] **Verify**: Status bar icons visible (not blending into background)
4. [ ] Navigate to TV tab
5. [ ] **Verify**: Status bar background adapts to new screen color
6. [ ] Navigate to Search tab
7. [ ] **Verify**: Status bar background adapts
8. [ ] Navigate to You tab
9. [ ] **Verify**: Status bar background adapts
10. [ ] Open Settings (if theme customization enabled)
11. [ ] Change theme (Light → Dark or vice versa)
12. [ ] **Verify**: Status bar adapts to new theme
13. [ ] **Verify**: No harsh contrast or wrong colors

**Expected Behavior:**
- ✅ Status bar transparent (shows app background)
- ✅ Status bar color adapts to each screen
- ✅ Status bar icons have proper contrast
- ✅ No jarring color mismatches

**Bug #3 Fixed:** Transparent status bar configured, edge-to-edge enabled

---

### Test D: Shared Element Transitions (Priority 1)

**Duration:** 3 minutes
**Tests:** Fade Through transitions with shared element morphs

**Steps:**
1. [ ] Navigate to Movies feed
2. [ ] Scroll to see multiple movie posters
3. [ ] Tap any movie poster
4. [ ] **Verify**: List screen fades out smoothly (no harsh cuts)
5. [ ] **Verify**: Brief moment where only poster is visible (50ms gap)
6. [ ] **Verify**: Details screen fades in smoothly
7. [ ] **Verify**: Poster morphs from list position to detail header
8. [ ] **Verify**: Transition feels ~500-600ms (not too fast/slow)
9. [ ] Press device back button
10. [ ] **Verify**: Details fades out quickly (~250ms)
11. [ ] **Verify**: List fades in smoothly (~300ms)
12. [ ] **Verify**: Poster morphs back to original position
13. [ ] Tap another movie poster
14. [ ] Swipe from left edge (predictive back gesture)
15. [ ] **Verify**: Details screen follows finger horizontally
16. [ ] **Verify**: Poster tracks smoothly back to list
17. [ ] Release gesture
18. [ ] **Verify**: Transition completes smoothly
19. [ ] Test from different entry points:
    - [ ] TV Shows feed → TV details
    - [ ] Search results → details
    - [ ] Favorites → details

**Expected Behavior:**
- ✅ Fade through pattern: fade out → gap → fade in
- ✅ Poster morph is the visual anchor
- ✅ No competing spatial motion (slides)
- ✅ Smooth at 60fps
- ✅ Predictive back works
- ✅ Consistent across all entry points

**Feature Added:** Material 3 Fade Through pattern with emphasized easing

---

## 🔧 Additional Verification Tests

### Test E: Performance & Responsiveness (Priority 2)

**Duration:** 3 minutes

1. [ ] Navigate to Movies feed
2. [ ] Scroll rapidly up and down
3. [ ] **Verify**: Smooth scrolling at 60fps
4. [ ] **Verify**: Images load progressively (shimmer → image)
5. [ ] **Verify**: No jank or stuttering
6. [ ] Navigate between tabs rapidly
7. [ ] **Verify**: Tab switches feel instant
8. [ ] **Verify**: No memory warnings or crashes
9. [ ] Open Details screen for 10 different movies/shows
10. [ ] **Verify**: App remains responsive
11. [ ] **Verify**: Memory usage stable (check in Android Studio Profiler if available)

---

### Test F: Offline Functionality (Priority 2)

**Duration:** 4 minutes

1. [ ] With internet ON, add 3 items to favorites
2. [ ] Enable Airplane Mode
3. [ ] Navigate to You → Favorites
4. [ ] **Verify**: Favorites visible offline
5. [ ] Tap a favorite to open details
6. [ ] **Verify**: Cached details load (if available)
7. [ ] Add a new item to favorites
8. [ ] **Verify**: Item added locally
9. [ ] Disable Airplane Mode
10. [ ] Wait 10 seconds
11. [ ] **Verify**: Local favorites sync to TMDB (if auth user)
12. [ ] Check tmdb.org
13. [ ] **Verify**: Offline-added favorite now appears online

---

### Test G: Navigation Flows (Priority 2)

**Duration:** 3 minutes

1. [ ] Test all tab switches: Movies ↔ TV ↔ Search ↔ You
2. [ ] **Verify**: Tab state preserved (scroll position, content)
3. [ ] From Movies feed, navigate: Feed → Items → Detail → Credits
4. [ ] Press back 3 times
5. [ ] **Verify**: Returns to Movies feed
6. [ ] **Verify**: Feed scroll position preserved
7. [ ] Test deep link (if applicable)
8. [ ] **Verify**: Deep link navigation works

---

### Test H: Theme & Appearance (Priority 3)

**Duration:** 2 minutes

1. [ ] Test Light theme
2. [ ] **Verify**: All text readable, proper contrast
3. [ ] Switch to Dark theme
4. [ ] **Verify**: All text readable, proper contrast
5. [ ] **Verify**: Images have proper contrast
6. [ ] **Verify**: Bottom navigation bar matches theme
7. [ ] **Verify**: Status bar adapts to theme

---

## 🐛 Regression Testing Checklist

Run this checklist to ensure no existing features broke:

### Core Features
- [ ] Movies feed loads and displays correctly
- [ ] TV shows feed loads and displays correctly
- [ ] Search functionality works
- [ ] Movie/TV details load correctly
- [ ] Cast/crew information displays
- [ ] Add to favorites works
- [ ] Add to watchlist works
- [ ] Remove from favorites works
- [ ] Remove from watchlist works
- [ ] Login/logout works
- [ ] User settings save correctly
- [ ] Theme switching works
- [ ] Navigation between screens works

### UI Components
- [ ] Bottom navigation bar visible and functional
- [ ] Top app bars display correctly
- [ ] Loading states show properly (shimmer effect)
- [ ] Error states display with retry options
- [ ] Empty states show appropriate messages
- [ ] Dialogs and modals work correctly

### Performance
- [ ] App launches in <2 seconds (cold start)
- [ ] Scrolling is smooth (60fps)
- [ ] Images load progressively
- [ ] No ANR (Application Not Responding) errors
- [ ] No crashes during normal usage

---

## 📊 Test Results Documentation

### Template for Test Run Report

```markdown
# Test Run Report

**Date:** YYYY-MM-DD
**APK Version:** [APK timestamp]
**Tester:** [Name]
**Device:** [Model, Android version]
**Build:** Debug/Release

## Test Results

### Test A: Guest Mode Favorites
- [ ] PASS / [ ] FAIL
- Issues: [List any issues found]
- Notes: [Additional observations]

### Test B: TMDB Sync
- [ ] PASS / [ ] FAIL
- Issues: [List any issues found]
- Sync Time: [How long it took]

### Test C: Edge-to-Edge
- [ ] PASS / [ ] FAIL
- Issues: [List any issues found]

### Test D: Shared Elements
- [ ] PASS / [ ] FAIL
- Issues: [List any issues found]
- FPS: [Estimated frame rate]

## Overall Assessment
- [ ] Ready for merge
- [ ] Needs fixes (see issues above)

## Screenshots
[Attach screenshots if issues found]
```

---

## 🚨 Known Issues & Workarounds

Document any known issues here:

### Issue #1: [Title]
**Status:** Open/In Progress/Fixed
**Workaround:** [Steps to work around]
**Tracked:** [Link to issue/ticket]

---

## 📝 Notes for Testers

### Best Practices
1. **Fresh Install:** For major changes, uninstall app before testing
2. **Clear Data:** Reset app data between test runs if needed
3. **Multiple Devices:** Test on different screen sizes if possible
4. **Network Conditions:** Test on WiFi, 4G, and offline
5. **Take Screenshots:** Capture any visual issues
6. **Note Timing:** Record slow operations (>1 second)

### Common Issues
- **Login fails:** Check TMDB API status
- **Images not loading:** Check network connection
- **Sync not working:** Wait 5 seconds, check logs
- **Crash on startup:** Clear app data and retry

### Reporting Bugs
When reporting bugs, include:
- [ ] Steps to reproduce
- [ ] Expected behavior
- [ ] Actual behavior
- [ ] Screenshots/video
- [ ] Device info
- [ ] Logcat output (if available)

---

## 🔄 Continuous Improvement

### After Each Test Run
1. Update this protocol with new test cases
2. Remove obsolete test cases
3. Refine steps based on feedback
4. Document new edge cases discovered

### Metrics to Track
- Average test run time
- Pass rate per test
- Most common failures
- Time to fix issues found

---

**Last Updated:** 2026-02-04
**Version:** 1.0
**Maintained By:** MoviesAndBeyond Team
