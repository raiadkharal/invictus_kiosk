package net.invictusmanagement.invictuskiosk.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import javax.inject.Inject

class ScreenSaverRepositoryImpl @Inject constructor() : ScreenSaverRepository {

    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused

    override fun pauseScreenSaver() {
        _isPaused.value = true
    }

    override fun resumeScreenSaver() {
        _isPaused.value = false
    }
}