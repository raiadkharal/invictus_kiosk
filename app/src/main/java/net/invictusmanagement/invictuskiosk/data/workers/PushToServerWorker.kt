package net.invictusmanagement.invictuskiosk.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.invictusmanagement.invictuskiosk.domain.repository.LogRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository

@HiltWorker
class PushToServerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: VacancyRepository,
    private val logRepository: LogRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncPendingRequests()
            logRepository.syncPendingLogs()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
