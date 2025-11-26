package net.invictusmanagement.invictuskiosk.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.invictusmanagement.invictuskiosk.data.workers.FetchFromServerWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FetchFromServerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val UNIQUE_WORK_NAME = "hourly_data_sync"
    }

    /**
     * Schedules the hourly sync.
     * Called **after login**, which ensures token exists.
     */
    private fun schedulePeriodicDataFetch() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<FetchFromServerWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        // REPLACE is important → replaces unauthenticated worker
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodic
        )
    }

    /**
     * The ONLY method you call after login.
     * It performs:
     * 1. Immediate sync (OneTimeWorker)
     * 2. Schedules hourly repeating sync
     */
    fun initializeDataSync() {
        performOneTimeSync()
        schedulePeriodicDataFetch()
    }


    /**
     * Immediately perform a manual one-time sync.
     */
    private fun performOneTimeSync() {
        val constraints =  Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTime = OneTimeWorkRequestBuilder<FetchFromServerWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        workManager.enqueue(oneTime)
    }

    /**
     * Useful on logout or if kiosk app is deactivated.
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
