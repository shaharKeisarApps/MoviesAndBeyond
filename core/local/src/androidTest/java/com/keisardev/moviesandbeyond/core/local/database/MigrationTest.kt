package com.keisardev.moviesandbeyond.core.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests Room database migrations to ensure data integrity is preserved across schema upgrades.
 *
 * Each test creates a database at the starting version, inserts test data, runs the migration, and
 * verifies the data survives the migration intact.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MoviesAndBeyondDatabase::class.java,
        )

    /**
     * Migration 1 -> 2: Adds Store5 cache tables (cached_content, cached_movie_details,
     * cached_tv_details).
     *
     * Existing data in favorite_content and watchlist_content must be preserved.
     */
    @Test
    fun migrate1to2_preservesExistingData() {
        // Create database at version 1
        helper.createDatabase(TEST_DB_NAME, 1).use { db ->
            // Insert test data into favorite_content (version 1 schema)
            db.execSQL(
                """
                INSERT INTO favorite_content (media_id, media_type, image_path, name)
                VALUES (123, 'movie', '/poster.jpg', 'Test Movie')
                """
                    .trimIndent()
            )

            // Insert test data into watchlist_content (version 1 schema)
            db.execSQL(
                """
                INSERT INTO watchlist_content (media_id, media_type, image_path, name)
                VALUES (456, 'tv', '/tv_poster.jpg', 'Test TV Show')
                """
                    .trimIndent()
            )
        }

        // Run migration and validate
        helper
            .runMigrationsAndValidate(TEST_DB_NAME, 2, true, MoviesAndBeyondDatabase.MIGRATION_1_2)
            .use { db ->
                // Verify favorite_content data survived
                db.query("SELECT * FROM favorite_content WHERE media_id = 123").use { cursor ->
                    assert(cursor.moveToFirst()) {
                        "favorite_content row should exist after migration"
                    }
                    val nameIdx = cursor.getColumnIndex("name")
                    assert(cursor.getString(nameIdx) == "Test Movie")
                }

                // Verify watchlist_content data survived
                db.query("SELECT * FROM watchlist_content WHERE media_id = 456").use { cursor ->
                    assert(cursor.moveToFirst()) {
                        "watchlist_content row should exist after migration"
                    }
                    val nameIdx = cursor.getColumnIndex("name")
                    assert(cursor.getString(nameIdx) == "Test TV Show")
                }

                // Verify new cache tables were created
                db.query("SELECT * FROM cached_content").use { cursor ->
                    assert(cursor.count == 0) { "cached_content table should exist and be empty" }
                }
                db.query("SELECT * FROM cached_movie_details").use { cursor ->
                    assert(cursor.count == 0) {
                        "cached_movie_details table should exist and be empty"
                    }
                }
                db.query("SELECT * FROM cached_tv_details").use { cursor ->
                    assert(cursor.count == 0) {
                        "cached_tv_details table should exist and be empty"
                    }
                }
            }
    }

    /**
     * Migration 2 -> 3: Adds sync_status and added_at columns to favorite_content and
     * watchlist_content for offline-first dual-user support.
     *
     * Existing rows must get default values: sync_status='SYNCED', added_at=0.
     */
    @Test
    fun migrate2to3_addsColumnsWithDefaults() {
        // Create database at version 2 (through migration 1->2)
        helper.createDatabase(TEST_DB_NAME, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO favorite_content (media_id, media_type, image_path, name)
                VALUES (789, 'movie', '/poster2.jpg', 'Migration Test Movie')
                """
                    .trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO watchlist_content (media_id, media_type, image_path, name)
                VALUES (101, 'tv', '/tv2.jpg', 'Migration Test Show')
                """
                    .trimIndent()
            )
        }

        // Migrate 1->2 first, then 2->3
        helper
            .runMigrationsAndValidate(
                TEST_DB_NAME,
                3,
                true,
                MoviesAndBeyondDatabase.MIGRATION_1_2,
                MoviesAndBeyondDatabase.MIGRATION_2_3,
            )
            .use { db ->
                // Verify favorite_content has new columns with correct defaults
                db.query("SELECT * FROM favorite_content WHERE media_id = 789").use { cursor ->
                    assert(cursor.moveToFirst()) { "favorite_content row should exist" }

                    val syncStatusIdx = cursor.getColumnIndex("sync_status")
                    assert(syncStatusIdx >= 0) { "sync_status column should exist" }
                    assert(cursor.getString(syncStatusIdx) == "SYNCED") {
                        "sync_status should default to SYNCED"
                    }

                    val addedAtIdx = cursor.getColumnIndex("added_at")
                    assert(addedAtIdx >= 0) { "added_at column should exist" }
                    assert(cursor.getLong(addedAtIdx) == 0L) { "added_at should default to 0" }
                }

                // Verify watchlist_content has new columns with correct defaults
                db.query("SELECT * FROM watchlist_content WHERE media_id = 101").use { cursor ->
                    assert(cursor.moveToFirst()) { "watchlist_content row should exist" }

                    val syncStatusIdx = cursor.getColumnIndex("sync_status")
                    assert(cursor.getString(syncStatusIdx) == "SYNCED") {
                        "sync_status should default to SYNCED"
                    }

                    val addedAtIdx = cursor.getColumnIndex("added_at")
                    assert(cursor.getLong(addedAtIdx) == 0L) { "added_at should default to 0" }
                }
            }
    }

    /**
     * Full migration chain: Version 1 -> 3. Verifies the complete migration path works end to end.
     */
    @Test
    fun migrateAll_version1to3_succeeds() {
        helper.createDatabase(TEST_DB_NAME, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO favorite_content (media_id, media_type, image_path, name)
                VALUES (999, 'movie', '/full_test.jpg', 'Full Migration Test')
                """
                    .trimIndent()
            )
        }

        helper
            .runMigrationsAndValidate(
                TEST_DB_NAME,
                3,
                true,
                MoviesAndBeyondDatabase.MIGRATION_1_2,
                MoviesAndBeyondDatabase.MIGRATION_2_3,
            )
            .use { db ->
                // Verify data and all new columns/tables exist
                db.query("SELECT * FROM favorite_content WHERE media_id = 999").use { cursor ->
                    assert(cursor.moveToFirst()) { "Data should survive full migration chain" }
                    assert(cursor.getColumnIndex("sync_status") >= 0)
                    assert(cursor.getColumnIndex("added_at") >= 0)
                }
                db.query("SELECT * FROM cached_content").use { cursor ->
                    assert(cursor.count == 0) { "Cache tables should exist after full migration" }
                }
            }
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }
}
