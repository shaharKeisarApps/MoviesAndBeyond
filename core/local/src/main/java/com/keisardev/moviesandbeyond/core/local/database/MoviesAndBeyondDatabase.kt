package com.keisardev.moviesandbeyond.core.local.database

import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedMovieDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedTvDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import com.keisardev.moviesandbeyond.core.local.database.entity.AccountDetailsEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedMovieDetailsEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.CachedTvDetailsEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.FavoriteContentEntity
import com.keisardev.moviesandbeyond.core.local.database.entity.WatchlistContentEntity

@Database(
    entities =
        [
            FavoriteContentEntity::class,
            WatchlistContentEntity::class,
            AccountDetailsEntity::class,
            CachedContentEntity::class,
            CachedMovieDetailsEntity::class,
            CachedTvDetailsEntity::class],
    version = 3,
    /*autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = MoviesAndBeyondDatabase.Companion.Migration5to6::class)
    ],
    exportSchema = true*/
)
abstract class MoviesAndBeyondDatabase : RoomDatabase() {
    abstract fun favoriteContentDao(): FavoriteContentDao

    abstract fun watchlistContentDao(): WatchlistContentDao

    abstract fun accountDetailsDao(): AccountDetailsDao

    abstract fun cachedContentDao(): CachedContentDao

    abstract fun cachedMovieDetailsDao(): CachedMovieDetailsDao

    abstract fun cachedTvDetailsDao(): CachedTvDetailsDao

    companion object {
        /**
         * Migration from version 2 to 3: Add sync_status and added_at columns to favorite_content
         * and watchlist_content tables for offline-first dual-user support.
         */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Add sync_status column to favorite_content (default SYNCED for existing rows)
                    db.execSQL(
                        """
                        ALTER TABLE favorite_content ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SYNCED'
                        """
                            .trimIndent())
                    // Add added_at column to favorite_content
                    db.execSQL(
                        """
                        ALTER TABLE favorite_content ADD COLUMN added_at INTEGER NOT NULL DEFAULT 0
                        """
                            .trimIndent())

                    // Add sync_status column to watchlist_content (default SYNCED for existing
                    // rows)
                    db.execSQL(
                        """
                        ALTER TABLE watchlist_content ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SYNCED'
                        """
                            .trimIndent())
                    // Add added_at column to watchlist_content
                    db.execSQL(
                        """
                        ALTER TABLE watchlist_content ADD COLUMN added_at INTEGER NOT NULL DEFAULT 0
                        """
                            .trimIndent())
                }
            }

        /** Migration from version 1 to 2: Add Store5 cache tables for offline-first support. */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // Create cached_content table for movie/TV show lists
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS cached_content (
                            content_id INTEGER NOT NULL,
                            category TEXT NOT NULL,
                            page INTEGER NOT NULL,
                            position INTEGER NOT NULL,
                            image_path TEXT NOT NULL,
                            name TEXT NOT NULL,
                            backdrop_path TEXT,
                            rating REAL,
                            release_date TEXT,
                            overview TEXT,
                            fetched_at INTEGER NOT NULL,
                            PRIMARY KEY(content_id, category)
                        )
                        """
                            .trimIndent())
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_cached_content_category ON cached_content (category)")
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_cached_content_fetched_at ON cached_content (fetched_at)")

                    // Create cached_movie_details table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS cached_movie_details (
                            id INTEGER PRIMARY KEY NOT NULL,
                            details_json TEXT NOT NULL,
                            fetched_at INTEGER NOT NULL
                        )
                        """
                            .trimIndent())

                    // Create cached_tv_details table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS cached_tv_details (
                            id INTEGER PRIMARY KEY NOT NULL,
                            details_json TEXT NOT NULL,
                            fetched_at INTEGER NOT NULL
                        )
                        """
                            .trimIndent())
                }
            }

        // Legacy migrations below (kept for reference, database was reset)
        @Deprecated("Legacy migration - database was reset to version 1")
        val LEGACY_MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    ALTER TABLE trending_content ADD COLUMN remote_id INTEGER NOT NULL
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    ALTER TABLE trending_content
                    MODIFY COLUMN id INTEGER AUTOINCREMENT NOT NULL
                    """
                            .trimIndent())
                }
            }

        @Deprecated("Legacy migration - database was reset, superseded by new MIGRATION_2_3")
        val LEGACY_MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE temp_fc (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    remote_id INTEGER NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    overview TEXT NOT NULL)
                    """
                            .trimIndent())

                    db.execSQL(
                        """
                   INSERT INTO temp_fc (remote_id, image_path, name, overview)
                   SELECT id, image_path, name, overview FROM free_content
                """
                            .trimIndent())

                    db.execSQL("DROP TABLE free_content")
                    db.execSQL("ALTER TABLE temp_fc RENAME TO free_content")

                    db.execSQL(
                        """
                    CREATE TABLE temp_pc (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    remote_id INTEGER NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    overview TEXT NOT NULL)
                    """
                            .trimIndent())

                    db.execSQL(
                        """
                   INSERT INTO temp_pc (remote_id, image_path, name, overview)
                   SELECT id, image_path, name, overview FROM popular_content
                   """
                            .trimIndent())

                    db.execSQL("DROP TABLE popular_content")
                    db.execSQL("ALTER TABLE temp_pc RENAME TO popular_content")
                }
            }

        @Deprecated("Legacy migration - database was reset")
        val LEGACY_MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS free_content_remote_key (
                    id INTEGER NOT NULL,
                    nextKey INTEGER NULL,
                    PRIMARY KEY(id))
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS popular_content_remote_key (
                    id INTEGER NOT NULL,
                    nextKey INTEGER NULL,
                    PRIMARY KEY(id))
                    """
                            .trimIndent())
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS entity_last_modified (
                    name TEXT PRIMARY KEY NOT NULL,
                    last_modified INTEGER NOT NULL)
                    """
                            .trimIndent())

                    db.execSQL(
                        """
                    INSERT INTO entity_last_modified VALUES
                    ('trending_content', 0),
                    ('free_content', 0),
                    ('popular_content', 0)
                """
                            .trimIndent())
                }
            }

        @DeleteColumn.Entries(
            DeleteColumn("trending_content", "overview"),
            DeleteColumn("free_content", "overview"),
            DeleteColumn("popular_content", "overview"))
        class Migration5to6 : AutoMigrationSpec

        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE favorite_content (
                    id INTEGER PRIMARY KEY NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    created_at INTEGER NOT NULL)
                    """
                            .trimIndent())
                }
            }

        val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE watchlist_content (
                    id INTEGER PRIMARY KEY NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    created_at INTEGER NOT NULL)
                    """
                            .trimIndent())
                }
            }

        val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP TABLE entity_last_modified")
                    db.execSQL("DROP TABLE free_content")
                    db.execSQL("DROP TABLE free_content_remote_key")
                    db.execSQL("DROP TABLE popular_content")
                    db.execSQL("DROP TABLE popular_content_remote_key")
                    db.execSQL("DROP TABLE trending_content")
                    db.execSQL("DROP TABLE trending_content_remote_key")
                }
            }

        val MIGRATION_9_10 =
            object : Migration(9, 10) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE account_details (
                    id INTEGER PRIMARY KEY NOT NULL,
                    gravatar_hash TEXT NOT NULL,
                    include_adult INTEGER NOT NULL,
                    iso_639_1 TEXT NOT NULL,
                    iso_3166_1 TEXT NOT NULL,
                    name TEXT NOT NULL,
                    tmdb_avatar_path TEXT NULL,
                    username TEXT NOT NULL)
                    """
                            .trimIndent())
                }
            }

        val MIGRATION_10_11 =
            object : Migration(10, 11) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE fc (
                    id INTEGER NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    PRIMARY KEY(id,media_type))
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    INSERT INTO fc SELECT * FROM favorite_content
                    """
                            .trimIndent())
                    db.execSQL("DROP TABLE favorite_content")
                    db.execSQL("ALTER TABLE fc RENAME TO favorite_content")

                    db.execSQL(
                        """
                    CREATE TABLE wc (
                    id INTEGER NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    PRIMARY KEY(id,media_type))
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    INSERT INTO wc SELECT * FROM watchlist_content
                    """
                            .trimIndent())
                    db.execSQL("DROP TABLE watchlist_content")
                    db.execSQL("ALTER TABLE wc RENAME TO watchlist_content")
                }
            }

        val MIGRATION_11_12 =
            object : Migration(11, 12) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                    CREATE TABLE favorite_content_temp (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    media_id INTEGER NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL
                    )
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    INSERT INTO favorite_content_temp (media_id, media_type, image_path, name)
                    SELECT id, media_type, image_path, name FROM favorite_content
                    """
                            .trimIndent())
                    db.execSQL("DROP TABLE favorite_content")
                    db.execSQL("ALTER TABLE favorite_content_temp RENAME TO favorite_content")
                    db.execSQL(
                        """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_favorite_content_media_id_media_type
                    ON favorite_content (media_id, media_type)
                    """
                            .trimIndent())

                    db.execSQL(
                        """
                    CREATE TABLE watchlist_content_temp (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    media_id INTEGER NOT NULL,
                    media_type TEXT NOT NULL,
                    image_path TEXT NOT NULL,
                    name TEXT NOT NULL
                    )
                    """
                            .trimIndent())
                    db.execSQL(
                        """
                    INSERT INTO watchlist_content_temp (media_id, media_type, image_path, name)
                    SELECT id, media_type, image_path, name FROM watchlist_content
                    """
                            .trimIndent())
                    db.execSQL("DROP TABLE watchlist_content")
                    db.execSQL("ALTER TABLE watchlist_content_temp RENAME TO watchlist_content")
                    db.execSQL(
                        """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_watchlist_content_media_id_media_type
                    ON watchlist_content (media_id, media_type)
                    """
                            .trimIndent())
                }
            }
    }
}
