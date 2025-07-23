package net.invictusmanagement.invictuskiosk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KioskApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
