package net.invictusmanagement.invictuskiosk.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import net.invictusmanagement.invictuskiosk.BuildConfig
import net.invictusmanagement.invictuskiosk.data.workers.FetchFromServerWorker
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FetchFromServerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreManager: DataStoreManager
) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val UNIQUE_WORK_NAME = "hourly_data_sync"
        private const val UNIQUE_IMMEDIATE_WORK_NAME = "immediate_data_sync"
    }

    /**
     * Schedules the hourly sync.
     * Called **after login**, which ensures token exists.
     */
    fun schedulePeriodicDataFetch() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodic = PeriodicWorkRequestBuilder<FetchFromServerWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("periodic_sync")
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

    suspend fun runImmediateSyncIfAppUpdated() {
        val currentVersion = BuildConfig.VERSION_CODE
        val lastVersion = dataStoreManager.lastSyncedVersion.first()

        if (lastVersion < currentVersion) {
            // App has updated ⟶ run immediate sync once
            initializeDataSync()

            // Save new version so this does not repeat
            dataStoreManager.saveLastSyncedVersion(currentVersion)
        }
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
            .addTag("immediate_sync")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTime
        )
    }

    /**
     * Useful on logout or if kiosk app is deactivated.
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
