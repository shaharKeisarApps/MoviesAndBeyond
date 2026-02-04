# Performance Benchmarking Task - Parallel Worktree

**Date**: 2026-02-04
**Status**: 📋 Queued for Parallel Execution
**Priority**: High
**Worktree**: To be created

---

## User Request

> "in a worktree, use claude-flow, and Compose runtime expert + Android expert, to think how to build benchmarks, and baseline profiles + using perfetto, in order to verify that app performance are Elite!. make sure to save that in memory in order to handle that as a parallel, and beside our current work on this branch and phases"

---

## Scope

### 1. Worktree Setup
- Create separate git worktree for performance work
- Branch: `feature/performance-benchmarking`
- Parallel to main branch work (favorites bug fix, shared elements)

### 2. Benchmarking Strategy
- Design comprehensive performance testing strategy
- Define "Elite" performance metrics:
  - App startup time: < 300ms (cold), < 150ms (warm)
  - Frame time: < 16ms (60 FPS target)
  - Jank-free scrolling in feed screens
  - Smooth shared element transitions (no frame drops)
  - Memory usage: < 150MB baseline
  - APK size optimization

### 3. Jetpack Macrobenchmark Implementation
- Create `:benchmark` module
- Implement macrobenchmark tests:
  - Startup benchmarks (cold, warm, hot)
  - Scroll performance benchmarks (feed screens, detail scrolling)
  - Navigation benchmarks (tab switching, screen transitions)
  - Shared element transition benchmarks
  - Search performance benchmarks
  - Database query benchmarks (favorites, watchlist)

### 4. Baseline Profiles
- Generate baseline profiles using Jetpack Baseline Profile Gradle plugin
- Profile types:
  - Startup profile (MainActivity, navigation setup)
  - Runtime profile (scroll performance, image loading)
  - User journey profiles (common user flows)
- Verify profile effectiveness with before/after metrics

### 5. Perfetto Integration
- Set up Perfetto trace collection
- Create trace analysis documentation
- Identify performance bottlenecks:
  - Recomposition hotspots
  - Layout/measure performance
  - Image loading pipeline
  - Network request overhead
  - Database query performance

### 6. Compose Runtime Profiling
- Track recomposition metrics
- Identify unnecessary recompositions
- Measure remember/derivedStateOf effectiveness
- Profile LazyColumn/LazyRow performance
- Analyze StateFlow collection patterns

---

## Tools & Expertise Required

### Libraries
- `androidx.benchmark:benchmark-macro-junit4`
- `androidx.profileinstaller:profileinstaller`
- `androidx.tracing:tracing-perfetto`
- `androidx.tracing:tracing-perfetto-binary`

### Expertise
- **Compose Runtime Expert**: Recomposition analysis, state management profiling
- **Android Performance Expert**: Macrobenchmark, baseline profiles, Perfetto
- **claude-flow**: Multi-agent coordination for parallel analysis

### Tools
- Android Studio Profiler
- Perfetto UI (https://ui.perfetto.dev)
- Jetpack Macrobenchmark library
- Baseline Profile Gradle plugin
- Compose Runtime Metrics (androidx.compose.runtime:runtime-tracing)

---

## Deliverables

### 1. `:benchmark` Module
```
benchmark/
├── build.gradle.kts
├── src/
│   └── main/
│       └── java/
│           └── com/keisardev/moviesandbeyond/benchmark/
│               ├── StartupBenchmark.kt
│               ├── ScrollBenchmark.kt
│               ├── NavigationBenchmark.kt
│               ├── SharedElementBenchmark.kt
│               └── SearchBenchmark.kt
```

### 2. Baseline Profile Rules
```
app/src/main/baseline-prof.txt
- Startup optimizations
- Hot path optimizations
- Image loading pipeline
- Navigation transitions
```

### 3. Perfetto Trace Analysis Documentation
```
.claude/performance/
├── perfetto-setup.md
├── trace-analysis-guide.md
├── performance-metrics.md
└── optimization-recommendations.md
```

### 4. Performance Regression Test Suite
- CI/CD integration for benchmark tests
- Performance regression detection
- Automated baseline profile generation
- Performance dashboard

### 5. Performance Metrics Report
```
.claude/performance/metrics-report.md
- Baseline metrics (before optimization)
- Target metrics (Elite performance)
- Current metrics (after optimization)
- Comparison with industry benchmarks
```

---

## Elite Performance Targets

### Startup Performance
- Cold startup: < 300ms (TTID - Time To Initial Display)
- Warm startup: < 150ms
- Hot startup: < 100ms
- First frame: < 50ms after launch

### Runtime Performance
- Frame time: < 16ms (60 FPS)
- 99th percentile frame time: < 20ms
- Jank count: 0 frames > 32ms per session
- Scroll performance: 60 FPS sustained

### Shared Element Transitions
- Transition duration: 350-400ms
- Frame drops: 0
- Animation smoothness: 100% (no stuttering)

### Memory
- Baseline memory: < 150MB
- Peak memory: < 250MB
- Memory leaks: 0
- Image cache: Efficient (128MB memory + 256MB disk)

### Network & Database
- API response handling: < 100ms
- Database queries: < 10ms
- Cache hit rate: > 80%

---

## Implementation Plan

### Phase 1: Setup & Baseline (1-2 hours)
1. Create worktree for parallel development
2. Set up `:benchmark` module
3. Add benchmark dependencies
4. Run initial benchmarks (establish baseline)

### Phase 2: Macrobenchmark Tests (2-3 hours)
1. Implement startup benchmarks
2. Implement scroll benchmarks
3. Implement navigation benchmarks
4. Implement shared element benchmarks
5. Run tests and collect metrics

### Phase 3: Baseline Profiles (1-2 hours)
1. Generate startup profile
2. Generate runtime profile
3. Generate user journey profiles
4. Measure profile effectiveness

### Phase 4: Perfetto Integration (2-3 hours)
1. Set up Perfetto trace collection
2. Capture traces for key user flows
3. Analyze traces for bottlenecks
4. Document findings and recommendations

### Phase 5: Optimization & Verification (3-4 hours)
1. Implement performance optimizations based on findings
2. Re-run benchmarks to verify improvements
3. Create performance regression test suite
4. Document best practices

### Total Estimated Time: 9-14 hours

---

## CI/CD Integration

### GitHub Actions Workflow
```yaml
name: Performance Benchmarks

on:
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM

jobs:
  benchmark:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Macrobenchmarks
        run: ./gradlew :benchmark:connectedBenchmarkAndroidTest
      - name: Upload Results
        uses: actions/upload-artifact@v4
        with:
          name: benchmark-results
          path: benchmark/build/outputs/
      - name: Check Performance Regression
        run: ./scripts/check-performance-regression.sh
```

---

## Success Criteria

- ✅ All macrobenchmark tests passing
- ✅ Baseline profiles generated and effective (>20% improvement)
- ✅ Perfetto traces captured and analyzed
- ✅ Elite performance targets met
- ✅ Performance regression tests in CI/CD
- ✅ Documentation complete

---

## Dependencies

**Blocked by**: None (can start immediately in parallel)

**Blocks**: None (parallel work to main branch)

---

## Notes

- This work will be done in a **separate worktree** to avoid interfering with main branch work
- Use **claude-flow** for multi-agent coordination (Compose expert + Android expert)
- Results will be saved to `.claude/performance/` directory
- Performance metrics will be tracked over time
- This establishes the foundation for ongoing performance monitoring

---

**Status**: 📋 Queued for execution after current main branch work is committed and pushed

**Next Steps**:
1. Create worktree: `git worktree add ../MoviesAndBeyond-performance feature/performance-benchmarking`
2. Invoke claude-flow swarm with Compose Runtime expert + Android Performance expert
3. Execute phases 1-5 above
4. Merge results back to main when complete
