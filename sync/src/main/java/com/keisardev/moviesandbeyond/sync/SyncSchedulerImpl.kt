package com.keisardev.moviesandbeyond.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.library.LibraryTask
import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import com.keisardev.moviesandbeyond.sync.util.FAVORITES_TAG
import com.keisardev.moviesandbeyond.sync.util.WATCHLIST_TAG
import com.keisardev.moviesandbeyond.sync.util.putEnum
import com.keisardev.moviesandbeyond.sync.workers.LibrarySyncWorker
import com.keisardev.moviesandbeyond.sync.workers.LibraryTaskWorker
import com.keisardev.moviesandbeyond.sync.workers.LibraryTaskWorker.Companion.ITEM_EXISTS_KEY
import com.keisardev.moviesandbeyond.sync.workers.LibraryTaskWorker.Companion.ITEM_TYPE_KEY
import com.keisardev.moviesandbeyond.sync.workers.LibraryTaskWorker.Companion.MEDIA_TYPE_KEY
import com.keisardev.moviesandbeyond.sync.workers.LibraryTaskWorker.Companion.TASK_KEY
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class SyncSchedulerImpl @Inject constructor(private val workManager: WorkManager) :
    SyncScheduler {
    override fun scheduleLibraryTaskWork(libraryTask: LibraryTask) {
        val libraryTaskWorkRequest =
            OneTimeWorkRequestBuilder<LibraryTaskWorker>()
                .setConstraints(getWorkConstraints())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10L, TimeUnit.SECONDS)
                .setInputData(generateInputData(libraryTask))
                .build()

        workManager.enqueueUniqueWork(
            generateWorkerName(
                mediaId = libraryTask.mediaId,
                mediaType = libraryTask.mediaType,
                itemType = libraryTask.itemType),
            ExistingWorkPolicy.REPLACE,
            libraryTaskWorkRequest)
    }

    override fun scheduleLibrarySyncWork() {
        val librarySyncWorkRequest =
            OneTimeWorkRequestBuilder<LibrarySyncWorker>()
                .setConstraints(getWorkConstraints())
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        workManager.enqueueUniqueWork(
            LibrarySyncWorker.SYNC_LIBRARY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            librarySyncWorkRequest)
    }

    override fun isWorkNotScheduled(
        mediaId: Int,
        mediaType: MediaType,
        itemType: LibraryItemType
    ): Boolean {
        return workManager
            .getWorkInfosForUniqueWork(
                generateWorkerName(mediaId = mediaId, mediaType = mediaType, itemType = itemType))
            .get()
            .any {
                it.state == WorkInfo.State.ENQUEUED ||
                    it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.BLOCKED
            }
            .not()
    }

    private fun generateWorkerName(
        mediaId: Int,
        mediaType: MediaType,
        itemType: LibraryItemType
    ): String {
        return when (itemType) {
            LibraryItemType.FAVORITE -> "${FAVORITES_TAG}-${mediaId}-${mediaType.name}"
            LibraryItemType.WATCHLIST -> "${WATCHLIST_TAG}-${mediaId}-${mediaType.name}"
        }
    }

    private fun generateInputData(libraryTask: LibraryTask) =
        Data.Builder()
            .putInt(TASK_KEY, libraryTask.mediaId)
            .putEnum(MEDIA_TYPE_KEY, libraryTask.mediaType)
            .putEnum(ITEM_TYPE_KEY, libraryTask.itemType)
            .putBoolean(ITEM_EXISTS_KEY, libraryTask.itemExistLocally)
            .build()

    private fun getWorkConstraints() =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
}
