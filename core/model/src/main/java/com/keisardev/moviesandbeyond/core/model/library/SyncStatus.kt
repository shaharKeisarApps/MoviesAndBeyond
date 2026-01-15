package com.keisardev.moviesandbeyond.core.model.library

/**
 * Represents the synchronization status of a library item (favorite/watchlist).
 *
 * Used to track whether items have been synced with TMDB API, enabling offline-first functionality
 * where guest users can use favorites locally and sync when they authenticate.
 */
enum class SyncStatus {
    /** Item exists only locally, not synced to TMDB. Used for guest users or when offline. */
    LOCAL_ONLY,

    /** Item is in sync with TMDB. Both local and remote have the same state. */
    SYNCED,

    /**
     * Item needs to be pushed to TMDB. Added locally while offline or as authenticated user with
     * pending sync.
     */
    PENDING_PUSH,

    /** Item is marked for deletion on TMDB. Removed locally but remote deletion pending. */
    PENDING_DELETE
}
