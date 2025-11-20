package net.invictusmanagement.invictuskiosk.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository

class ContactRequestSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: VacancyRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncPendingRequests()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
