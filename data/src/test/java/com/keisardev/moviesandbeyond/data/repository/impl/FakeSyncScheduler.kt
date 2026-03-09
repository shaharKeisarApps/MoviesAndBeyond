package com.keisardev.moviesandbeyond.data.repository.impl

import com.keisardev.moviesandbeyond.core.model.MediaType
import com.keisardev.moviesandbeyond.core.model.library.LibraryItemType
import com.keisardev.moviesandbeyond.core.model.library.LibraryTask
import com.keisardev.moviesandbeyond.data.util.SyncScheduler

class FakeSyncScheduler : SyncScheduler {
    val scheduledTasks = mutableListOf<LibraryTask>()
    var librarySyncScheduled = false
    var workNotScheduled = true

    override fun scheduleLibraryTaskWork(libraryTask: LibraryTask) {
        scheduledTasks.add(libraryTask)
    }

    override fun scheduleLibrarySyncWork() {
        librarySyncScheduled = true
    }

    override fun isWorkNotScheduled(
        mediaId: Int,
        mediaType: MediaType,
        itemType: LibraryItemType,
    ): Boolean = workNotScheduled
}
