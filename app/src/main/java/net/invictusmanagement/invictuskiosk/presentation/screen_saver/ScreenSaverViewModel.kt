package net.invictusmanagement.invictuskiosk.presentation.screen_saver

import android.app.usage.NetworkStatsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.domain.repository.ScreenSaverRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import javax.inject.Inject

@HiltViewModel
class ScreenSaverViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val repository: ScreenSaverRepository
) : ViewModel() {

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken

    private val _locationName = MutableStateFlow<String?>("")
    val locationName: StateFlow<String?> = _locationName

    private val _kioskName = MutableStateFlow<String?>("")
    val kioskName: StateFlow<String?> = _kioskName

    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl: StateFlow<String?> = _videoUrl

    val isPaused = repository.isPaused

    init {
        viewModelScope.launch {
            dataStoreManager.accessTokenFlow.collect {
                _accessToken.value = it
            }
        }
    }

    suspend fun loadKioskData() {
        dataStoreManager.kioskDataFlow.collect {
            _videoUrl.value = it?.ssUrl
            _locationName.value = it?.kiosk?.location?.name ?: ""
            _kioskName.value = it?.kiosk?.name ?: ""
        }
    }
}