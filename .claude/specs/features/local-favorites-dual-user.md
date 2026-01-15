---
spec_version: 1.0
feature_name: Local Favorites with Dual-User Support
description: Allow users to use favorites/watchlist features regardless of TMDB authentication status
status: PLANNING
priority: P1
owner: feature-team
created: 2026-01-15
updated: 2026-01-15
---

# Feature: Local Favorites with Dual-User Support

## User Story

As a **MoviesAndBeyond user**, I want to **save movies and TV shows to my favorites and watchlist even without logging into TMDB**, so that **I can start using the app immediately and sync my data when I decide to create an account**.

---

## Problem Statement

Currently, the app requires TMDB authentication to use favorites/watchlist features. This creates friction for new users who want to explore the app before committing to account creation.

---

## Solution Overview

Implement a dual-mode system where:
1. **Guest users** can save favorites locally in Room database
2. **Authenticated users** sync favorites with TMDB API
3. **Login transition** merges local data with remote data

---

## User Flows

### Flow 1: Guest User (Skipped Login)

```
1. User launches app
2. User presses "Skip" on login page
3. User browses movies/TV shows
4. User taps "Add to Favorites" on a movie
5. Movie saved to local Room database
6. User sees movie in their Favorites list
7. Data persists across app restarts
```

### Flow 2: Authenticated User

```
1. User logs in with TMDB account
2. User browses movies/TV shows
3. User taps "Add to Favorites" on a movie
4. Movie saved to both:
   - Local Room database (immediate feedback)
   - TMDB API (background sync)
5. User sees movie in their Favorites list
6. Data syncs across devices via TMDB
```

### Flow 3: Login After Guest Usage

```
1. User was guest, had local favorites
2. User decides to log in with TMDB
3. Login successful
4. Sync process begins:
   a. Pull existing TMDB favorites
   b. Compare with local favorites
   c. Push local-only items to TMDB
   d. Merge results (remote wins on conflicts)
5. User sees unified favorites list
6. Notification: "Synced X favorites from your device"
```

### Flow 4: Logout After Authenticated Usage

```
1. Authenticated user has synced favorites
2. User logs out
3. Local favorites remain in Room database
4. User can continue using favorites locally
5. Changes no longer sync to TMDB
```

---

## Technical Requirements

### Data Layer Changes

#### 1. Add Guest Mode Flag to UserPreferences

```kotlin
// core/local/datastore/UserPreferences.kt

data class UserPreferences(
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,      // NEW
    val sessionId: String? = null,
    val accountId: String? = null,
    val lastSyncTimestamp: Long = 0L       // NEW
)
```

#### 2. Extend FavoritesEntity

```kotlin
// core/local/database/entity/FavoritesEntity.kt

@Entity(tableName = "favorites")
data class FavoritesEntity(
    @PrimaryKey val id: Int,
    val mediaType: String,
    val title: String,
    val posterPath: String?,
    val addedAt: Long,
    val syncStatus: SyncStatus,            // NEW
    val tmdbAddedAt: Long?                 // NEW: For conflict resolution
)

enum class SyncStatus {
    LOCAL_ONLY,      // Not synced to TMDB
    SYNCED,          // In sync with TMDB
    PENDING_PUSH,    // Needs to be pushed
    PENDING_DELETE   // Marked for remote deletion
}
```

#### 3. Modify FavoritesRepository Interface

```kotlin
// data/repository/FavoritesRepository.kt

interface FavoritesRepository {
    // Existing
    fun getFavorites(): Flow<List<MediaItem>>
    suspend fun addToFavorites(item: MediaItem): Result<Unit>
    suspend fun removeFromFavorites(id: Int): Result<Unit>
    suspend fun isFavorite(id: Int): Boolean

    // NEW
    suspend fun syncWithTmdb(): SyncResult
    suspend fun getPendingSyncItems(): List<FavoritesEntity>
    suspend fun markAsSynced(ids: List<Int>)
}

data class SyncResult(
    val pushed: Int,
    val pulled: Int,
    val conflicts: Int,
    val errors: List<SyncError>
)
```

#### 4. Implement Dual-Mode Logic

```kotlin
// data/repository/impl/FavoritesRepositoryImpl.kt

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoritesDao: FavoritesDao,
    private val tmdbApi: TmdbApi,
    private val userPreferences: UserPreferencesDataStore
) : FavoritesRepository {

    override suspend fun addToFavorites(item: MediaItem): Result<Unit> {
        return runCatching {
            val isAuthenticated = userPreferences.isLoggedIn.first()

            // Always save locally first (immediate feedback)
            val entity = item.toEntity(
                syncStatus = if (isAuthenticated) SyncStatus.PENDING_PUSH else SyncStatus.LOCAL_ONLY
            )
            favoritesDao.insert(entity)

            // If authenticated, also push to TMDB
            if (isAuthenticated) {
                try {
                    tmdbApi.addToFavorites(item.id, item.mediaType)
                    favoritesDao.updateSyncStatus(item.id, SyncStatus.SYNCED)
                } catch (e: Exception) {
                    // Keep as PENDING_PUSH for later retry
                    Log.w("Favorites", "Failed to sync to TMDB, will retry", e)
                }
            }
        }
    }

    override suspend fun syncWithTmdb(): SyncResult {
        // Implementation for full sync after login
        // See detailed algorithm below
    }
}
```

### Sync Algorithm

```kotlin
suspend fun syncWithTmdb(): SyncResult {
    val localFavorites = favoritesDao.getAll()
    val remoteFavorites = tmdbApi.getFavorites()

    val pushed = mutableListOf<Int>()
    val pulled = mutableListOf<Int>()
    val conflicts = mutableListOf<Int>()

    // 1. Push local-only items to TMDB
    localFavorites
        .filter { it.syncStatus == SyncStatus.LOCAL_ONLY }
        .forEach { local ->
            try {
                tmdbApi.addToFavorites(local.id, local.mediaType)
                favoritesDao.updateSyncStatus(local.id, SyncStatus.SYNCED)
                pushed.add(local.id)
            } catch (e: Exception) {
                // Keep as LOCAL_ONLY, try again later
            }
        }

    // 2. Pull remote items not in local
    remoteFavorites
        .filter { remote -> localFavorites.none { it.id == remote.id } }
        .forEach { remote ->
            favoritesDao.insert(remote.toEntity(SyncStatus.SYNCED))
            pulled.add(remote.id)
        }

    // 3. Handle conflicts (item in both, different states)
    localFavorites
        .filter { it.syncStatus == SyncStatus.PENDING_DELETE }
        .forEach { local ->
            val inRemote = remoteFavorites.any { it.id == local.id }
            if (inRemote) {
                // Conflict: Local wants delete, remote still has it
                // Resolution: Remote wins (keep the item)
                favoritesDao.updateSyncStatus(local.id, SyncStatus.SYNCED)
                conflicts.add(local.id)
            } else {
                // No conflict, remove locally
                favoritesDao.delete(local.id)
            }
        }

    // 4. Update last sync timestamp
    userPreferences.updateLastSyncTimestamp(System.currentTimeMillis())

    return SyncResult(
        pushed = pushed.size,
        pulled = pulled.size,
        conflicts = conflicts.size,
        errors = emptyList()
    )
}
```

### UI Changes

#### 1. Auth Screen - Add Skip Option

```kotlin
// feature/auth/ui/AuthScreen.kt

@Composable
fun AuthScreen(
    onLoginClick: () -> Unit,
    onSkipClick: () -> Unit,   // NEW
    modifier: Modifier = Modifier
) {
    // ... existing login UI ...

    TextButton(
        onClick = onSkipClick,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = "Skip for now",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

#### 2. Favorites UI - Show Sync Status

```kotlin
// feature/you/ui/FavoritesScreen.kt

@Composable
fun FavoriteItem(
    item: MediaItem,
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        // ... existing content ...

        // Show sync indicator for authenticated users
        if (syncStatus != SyncStatus.SYNCED) {
            Icon(
                imageVector = when (syncStatus) {
                    SyncStatus.LOCAL_ONLY -> Icons.Outlined.CloudOff
                    SyncStatus.PENDING_PUSH -> Icons.Outlined.CloudUpload
                    SyncStatus.PENDING_DELETE -> Icons.Outlined.DeleteSweep
                    else -> null
                },
                contentDescription = "Sync status",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
```

---

## Dependencies

### Module Dependencies

| Module | Dependency | Reason |
|--------|------------|--------|
| feature:auth | core:local | Guest mode flag |
| feature:details | data | Repository changes |
| feature:you | data | Display sync status |
| data | core:local | Room entities |
| data | core:network | TMDB sync |
| sync | data | Background sync |

### External Dependencies

- Room (existing): Local database
- DataStore (existing): User preferences
- WorkManager (existing): Background sync

---

## Implementation Tasks

### Phase 1: Data Layer Foundation
- [ ] Add `SyncStatus` enum to favorites entity
- [ ] Update `FavoritesEntity` with sync fields
- [ ] Create database migration
- [ ] Update `FavoritesDao` with sync queries

### Phase 2: Repository Changes
- [ ] Add `isGuestMode` to UserPreferences
- [ ] Implement dual-mode `addToFavorites`
- [ ] Implement dual-mode `removeFromFavorites`
- [ ] Implement `syncWithTmdb` algorithm

### Phase 3: Sync Logic
- [ ] Implement post-login sync trigger
- [ ] Handle conflict resolution
- [ ] Add retry logic for failed syncs
- [ ] Implement background sync with WorkManager

### Phase 4: UI Updates
- [ ] Add "Skip" button to auth screen
- [ ] Show sync status indicators
- [ ] Add sync progress feedback
- [ ] Handle offline state gracefully

### Phase 5: Testing
- [ ] Unit tests for sync algorithm
- [ ] Unit tests for repository dual-mode
- [ ] Integration tests for login transition
- [ ] UI tests for guest flow

---

## Success Criteria

- [ ] Guest users can add/remove favorites without login
- [ ] Favorites persist across app restarts for guests
- [ ] Login syncs local favorites to TMDB
- [ ] No data loss during login transition
- [ ] Clear UI feedback for sync status
- [ ] Offline mode works correctly

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data loss during sync | High | Local-first approach, never delete local until confirmed synced |
| Sync conflicts | Medium | Remote wins, user can re-add if needed |
| Large sync on first login | Medium | Paginate sync, show progress |
| API rate limiting | Low | Implement backoff, batch requests |

---

## Out of Scope

- Watchlist feature (separate spec)
- Multi-device offline sync
- Conflict resolution UI (beyond automatic resolution)
- Guest-to-guest data transfer

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-15 | Initial spec |
