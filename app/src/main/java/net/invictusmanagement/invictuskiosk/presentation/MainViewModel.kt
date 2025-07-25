package net.invictusmanagement.invictuskiosk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKeyState
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val unitMapRepository: UnitMapRepository
) : ViewModel() {

    private val _locationName = MutableStateFlow<String?>("")
    val locationName: StateFlow<String?> = _locationName

    private val _kioskName = MutableStateFlow<String?>("")
    val kioskName: StateFlow<String?> = _kioskName

    private val _isUnitFilterEnabled = MutableStateFlow<Boolean>(false)
    val isUnitFilterEnabled: StateFlow<Boolean> = _isUnitFilterEnabled

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _activationCode = MutableStateFlow("")
    val activationCode: StateFlow<String> = _activationCode

    private val _mapImage = MutableStateFlow<ByteArray?>(null)
    val mapImage: StateFlow<ByteArray?> = _mapImage

    init {
        viewModelScope.launch {
            dataStoreManager.kioskDataFlow.collect {
                _locationName.value = it?.kiosk?.location?.name ?: ""
                _kioskName.value = it?.kiosk?.name ?: ""
                _isUnitFilterEnabled.value = it?.kiosk?.isUnitFilterEnable ?: false
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

    fun fetchMapImage(unitId: Long, unitMapId: Long, toPackageCenter: Boolean) {
        viewModelScope.launch {
            unitMapRepository.getMapImage(unitId, unitMapId, toPackageCenter).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _mapImage.value = result.data
                    }

                    is Resource.Error -> {
                        _mapImage.value = null
                    }

                    is Resource.Loading -> {
                        _mapImage.value = null
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}