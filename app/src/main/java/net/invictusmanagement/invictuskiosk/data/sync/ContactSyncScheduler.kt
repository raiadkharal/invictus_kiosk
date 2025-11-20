package net.invictusmanagement.invictuskiosk.data.sync

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jakarta.inject.Inject
import net.invictusmanagement.invictuskiosk.data.workers.ContactRequestSyncWorker

class ContactSyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun enqueueContactSyncWork() {
        val request = OneTimeWorkRequestBuilder<ContactRequestSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "contact_request_sync",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
