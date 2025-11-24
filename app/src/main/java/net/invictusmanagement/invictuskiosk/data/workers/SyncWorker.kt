package net.invictusmanagement.invictuskiosk.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.invictusmanagement.invictuskiosk.domain.repository.CouponsRepository
import net.invictusmanagement.invictuskiosk.domain.repository.DirectoryRepository
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.VacancyRepository

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val couponsRepository: CouponsRepository,
    private val directoryRepository: DirectoryRepository,
    private val homeRepository: HomeRepository,
    private val vacancyRepository: VacancyRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        // Sync everything independently
        couponsRepository.sync()
        directoryRepository.sync()
        homeRepository.sync()
        vacancyRepository.sync()

        return Result.success()
    }

}
