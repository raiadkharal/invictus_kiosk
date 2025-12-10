package net.invictusmanagement.invictuskiosk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor

@HiltAndroidApp
class KioskApplication:Application() {


    override fun onCreate() {
        super.onCreate()
    }
}
