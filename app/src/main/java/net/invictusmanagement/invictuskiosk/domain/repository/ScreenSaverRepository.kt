package net.invictusmanagement.invictuskiosk.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ScreenSaverRepository {
    val isPaused: StateFlow<Boolean>
    fun pauseScreenSaver()
    fun resumeScreenSaver()
}
