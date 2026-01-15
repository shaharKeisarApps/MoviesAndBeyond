---
spec_version: 1.0
feature_name: Metro DI Migration
description: Migrate from Hilt to Metro DI for improved compile-time safety and KMP support
status: PLANNING
priority: P2
owner: architecture-team
created: 2026-01-15
updated: 2026-01-15
---

# Task: Metro DI Migration (Task 7)

## Overview

Migrate the MoviesAndBeyond project from Hilt (Dagger) to Metro DI for:
- **Compile-time safety**: All DI errors caught at build time
- **KMP support**: Native multiplatform dependency injection
- **Simplified syntax**: Less boilerplate than Dagger/Hilt
- **Better IDE support**: Improved autocomplete and navigation

---

## Current State Analysis

### Existing Hilt Setup

**Root Application:**
```kotlin
// app/src/main/java/com/movies/MoviesAndBeyondApplication.kt
@HiltAndroidApp
class MoviesAndBeyondApplication : Application()
```

**Repository Module:**
```kotlin
// data/di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    // ... other bindings
}
```

**Feature ViewModels:**
```kotlin
// feature/movies/ui/MoviesViewModel.kt
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val moviesRepository: MoviesRepository
) : ViewModel()
```

### Files to Migrate

| Category | Files | Complexity |
|----------|-------|------------|
| Application | 1 | Medium |
| DI Modules | 4 | High |
| ViewModels | 8 | Medium |
| Convention Plugins | 1 | Medium |

---

## Metro DI Target Architecture

### AppGraph Definition

```kotlin
// app/src/main/java/com/movies/di/AppGraph.kt
@DependencyGraph
interface AppGraph {
    // Single entry point for all app dependencies

    // Repositories (from data module)
    val authRepository: AuthRepository
    val moviesRepository: MoviesRepository
    val tvRepository: TvRepository
    val detailsRepository: DetailsRepository
    val searchRepository: SearchRepository
    val youRepository: YouRepository

    // Network components
    val tmdbApi: TmdbApi
    val ktorClient: HttpClient

    // Database components
    val database: MoviesDatabase
    val favoritesDao: FavoritesDao
    val watchlistDao: WatchlistDao

    // DataStore
    val userPreferences: DataStore<UserPreferences>

    companion object {
        fun create(application: Application): AppGraph {
            return createAppGraph(application)
        }
    }
}
```

### Repository Bindings with Metro

```kotlin
// data/di/RepositoryBindings.kt
@ContributesBinding(AppGraph::class)
@SingleIn(AppGraph::class)
class AuthRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val sessionManager: SessionManager
) : AuthRepository {
    // Implementation
}
```

### Feature Module Integration

```kotlin
// feature/movies/ui/MoviesPresenter.kt
class MoviesPresenter @AssistedInject constructor(
    private val moviesRepository: MoviesRepository,
    @Assisted private val navigator: Navigator
) {
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): MoviesPresenter
    }
}
```

---

## Migration Plan

### Phase 1: Core Setup (Foundation)

**Tasks:**
- [ ] Add Metro dependencies to version catalog
- [ ] Create `AppGraph` interface in app module
- [ ] Configure KSP for Metro code generation
- [ ] Update `moviesandbeyond.android.hilt` convention plugin to Metro

**Version Catalog Additions:**
```toml
# gradle/libs.versions.toml
[versions]
metro = "0.3.0"  # Use latest stable

[libraries]
metro-runtime = { group = "com.zachklipp.metro", name = "metro-runtime", version.ref = "metro" }
metro-compiler = { group = "com.zachklipp.metro", name = "metro-compiler", version.ref = "metro" }

[plugins]
metro = { id = "com.zachklipp.metro", version.ref = "metro" }
```

**Convention Plugin Update:**
```kotlin
// build-logic/convention/src/main/kotlin/AndroidMetroConventionPlugin.kt
class AndroidMetroConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                "implementation"(libs.findLibrary("metro.runtime").get())
                "ksp"(libs.findLibrary("metro.compiler").get())
            }
        }
    }
}
```

### Phase 2: Network Module Migration

**Tasks:**
- [ ] Migrate `NetworkModule.kt` providers to Metro
- [ ] Update TmdbApi provider
- [ ] Update HttpClient provider
- [ ] Verify network calls still work

**Before (Hilt):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideTmdbApi(client: HttpClient): TmdbApi = TmdbApi(client)
}
```

**After (Metro):**
```kotlin
@ContributesTo(AppGraph::class)
interface NetworkBindings {
    @Provides
    @SingleIn(AppGraph::class)
    fun provideTmdbApi(client: HttpClient): TmdbApi = TmdbApi(client)

    @Provides
    @SingleIn(AppGraph::class)
    fun provideHttpClient(config: NetworkConfig): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
```

### Phase 3: Database Module Migration

**Tasks:**
- [ ] Migrate `DatabaseModule.kt` to Metro
- [ ] Update Room database provider
- [ ] Update DAO providers
- [ ] Verify local storage works

**After (Metro):**
```kotlin
@ContributesTo(AppGraph::class)
interface DatabaseBindings {
    @Provides
    @SingleIn(AppGraph::class)
    fun provideDatabase(application: Application): MoviesDatabase {
        return Room.databaseBuilder(
            application,
            MoviesDatabase::class.java,
            "movies_database"
        ).build()
    }

    @Provides
    fun provideFavoritesDao(database: MoviesDatabase): FavoritesDao {
        return database.favoritesDao()
    }
}
```

### Phase 4: Repository Bindings Migration

**Tasks:**
- [ ] Convert all `@Binds` to `@ContributesBinding`
- [ ] Update all repository implementations
- [ ] Verify repository injection

**Before (Hilt):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
```

**After (Metro):**
```kotlin
@ContributesBinding(AppGraph::class)
@SingleIn(AppGraph::class)
class AuthRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val sessionManager: SessionManager
) : AuthRepository
```

### Phase 5: Feature Module Migration (Parallel)

Each feature module can be migrated independently:

**Tasks per feature:**
- [ ] feature:auth - Convert AuthViewModel
- [ ] feature:movies - Convert MoviesViewModel
- [ ] feature:tv - Convert TvViewModel
- [ ] feature:details - Convert DetailsViewModel
- [ ] feature:search - Convert SearchViewModel
- [ ] feature:you - Convert YouViewModel

**ViewModel Migration Pattern:**
```kotlin
// Before (Hilt)
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MoviesRepository
) : ViewModel()

// After (Metro with Circuit)
class MoviesPresenter @AssistedInject constructor(
    private val repository: MoviesRepository,
    @Assisted private val navigator: Navigator
) {
    @AssistedFactory
    @ContributesBinding(AppGraph::class)
    interface Factory {
        fun create(navigator: Navigator): MoviesPresenter
    }
}
```

### Phase 6: Application Entry Point

**Tasks:**
- [ ] Remove `@HiltAndroidApp`
- [ ] Create AppGraph in Application class
- [ ] Wire graph to activity/fragments

```kotlin
// app/src/main/java/com/movies/MoviesAndBeyondApplication.kt
class MoviesAndBeyondApplication : Application() {
    val appGraph: AppGraph by lazy { AppGraph.create(this) }
}
```

### Phase 7: Cleanup

**Tasks:**
- [ ] Remove all Hilt dependencies from version catalog
- [ ] Remove Hilt Gradle plugin
- [ ] Delete `moviesandbeyond.android.hilt` convention plugin
- [ ] Update CLAUDE.md documentation
- [ ] Run full test suite
- [ ] Verify build times improved

---

## Success Criteria

| Metric | Current (Hilt) | Target (Metro) |
|--------|----------------|----------------|
| All modules compile | Yes | Yes |
| All tests pass | Yes | Yes |
| No Hilt imports | N/A | 0 remaining |
| Build time (clean) | Baseline | Improved or same |
| Incremental build | Baseline | Improved |

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Missing Metro feature | Medium | High | Check Metro docs first, may need workaround |
| Learning curve | Low | Medium | Refer to metro-expert skill |
| Build time regression | Low | Medium | Benchmark before/after |
| Test compatibility | Low | Low | Tests use interfaces, not DI |

---

## Dependencies

### External
- Metro DI library (stable version)
- KSP (required for Metro)

### Internal
- All feature modules depend on this migration
- Convention plugins need update
- CI/CD config may need KSP setup

---

## Testing Strategy

### Unit Tests
- All existing tests should pass (they use interfaces)
- No new tests needed for DI itself

### Integration Tests
- Verify graph creation works
- Verify all dependencies resolve
- Verify scoping works correctly

### Manual Testing
- Full app flow through all features
- Verify no runtime DI errors
- Check for memory leaks

---

## Rollback Plan

If migration fails:
1. Revert to Hilt branch
2. Keep Metro experimental in separate branch
3. Document blockers encountered
4. Wait for Metro updates or find workarounds

---

## Related Resources

- [Metro DI Documentation](https://github.com/AdevintaSpain/Metro)
- `.claude/skills/metro-expert/SKILL.md` - Metro expert skill
- Convention plugin: `build-logic/convention/`

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-15 | Initial migration plan |
