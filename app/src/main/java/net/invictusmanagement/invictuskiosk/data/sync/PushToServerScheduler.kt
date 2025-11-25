package net.invictusmanagement.invictuskiosk.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import net.invictusmanagement.invictuskiosk.data.workers.PushToServerWorker

class PushToServerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun enqueuePushToServerWork() {
        val request = OneTimeWorkRequestBuilder<PushToServerWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "PushToServerWorker",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
