package net.invictusmanagement.invictuskiosk

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.data.sync.FetchFromServerScheduler

@HiltAndroidApp
class KioskApplication:Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var fetchFromServerScheduler: FetchFromServerScheduler

    override val workManagerConfiguration: Configuration
        get() =  Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    override fun onCreate() {
        super.onCreate()
//        WorkManager.initialize(this, workManagerConfiguration) // Not needed, Hilt does this automatically
        CoroutineScope(Dispatchers.Default).launch {
            fetchFromServerScheduler.schedulePeriodicDataFetch()
            fetchFromServerScheduler.runImmediateSyncIfAppUpdated()
        }
    }


}
