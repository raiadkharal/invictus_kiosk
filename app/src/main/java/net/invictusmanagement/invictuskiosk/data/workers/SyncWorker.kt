package net.invictusmanagement.invictuskiosk.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val couponsRepository: CouponsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        // Sync everything independently
        couponsRepository.syncAllCoupons()

        return Result.success()
    }

}
