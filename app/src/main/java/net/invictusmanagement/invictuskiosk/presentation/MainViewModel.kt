package net.invictusmanagement.invictuskiosk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _locationName = MutableStateFlow<String?>("")
    val locationName: StateFlow<String?> = _locationName

    private val _kioskName = MutableStateFlow<String?>("")
    val kioskName: StateFlow<String?> = _kioskName

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _activationCode = MutableStateFlow<String>("")
    val activationCode: StateFlow<String> = _activationCode

    init {
        viewModelScope.launch {
            dataStoreManager.kioskDataFlow.collect {
                _locationName.value = it?.kiosk?.location?.name ?: ""
                _kioskName.value = it?.kiosk?.name ?: ""
            }
        }
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.activationCodeFlow.collect {
                _activationCode.value = it ?: ""
            }
        }
    }
}