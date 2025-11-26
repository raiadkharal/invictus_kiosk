package net.invictusmanagement.invictuskiosk.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.sync.PushToServerScheduler
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val unitMapRepository: UnitMapRepository,
    private val networkMonitor: NetworkMonitor,
    private val contactScheduler: PushToServerScheduler
) : ViewModel() {

    val isConnected = networkMonitor.isConnected

    private val _locationName = MutableStateFlow<String?>("")
    val locationName: StateFlow<String?> = _locationName

    private val _kioskName = MutableStateFlow<String?>("")
    val kioskName: StateFlow<String?> = _kioskName

    private val _kioskId = MutableStateFlow<Int>(0)
    val kioskId: StateFlow<Int> = _kioskId

    private val _isUnitFilterEnabled = MutableStateFlow<Boolean>(false)
    val isUnitFilterEnabled: StateFlow<Boolean> = _isUnitFilterEnabled

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    private val _activationCode = MutableStateFlow("")
    val activationCode: StateFlow<String> = _activationCode

    private val _mapImagePath = MutableStateFlow<String?>(null)
    val mapImagePath: StateFlow<String?> = _mapImagePath

    var unitImagePaths by mutableStateOf<List<String>>(emptyList())
        private set

    var currentImageIndex by mutableIntStateOf(0)
        private set

    init {
        observeNetwork()
        viewModelScope.launch {
            dataStoreManager.kioskDataFlow.collect {
                _locationName.value = it?.kiosk?.location?.name ?: ""
                _kioskName.value = it?.kiosk?.name ?: ""
                _kioskId.value = it?.kiosk?.id ?: 0
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
                        _mapImagePath.value = result.data
                    }

                    is Resource.Error -> {
                        _mapImagePath.value = null
                    }

                    is Resource.Loading -> {
                        _mapImagePath.value = null
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun loadImages(unitId: Long, imageIds: List<Long>) {
        viewModelScope.launch {
            try {
                val paths = mutableListOf<String>()

                for (id in imageIds) {
                    unitMapRepository.getUnitImage(unitId, id)
                        .collect { result ->
                            when (result) {
                                is Resource.Success -> result.data?.let { paths.add(it) }
                                is Resource.Error -> {
                                    result.data?.let { paths.add(it) }
                                }
                                is Resource.Loading -> {
                                    // Optional loading logic
                                }
                            }
                        }
                }

                // When all images are collected
                unitImagePaths = paths
                currentImageIndex = 0

            } catch (e: Exception) {
                e.printStackTrace()
                unitImagePaths = emptyList()
            }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isOnline ->
                if (isOnline) {
                    contactScheduler.enqueuePushToServerWork()
                }
            }
        }
    }

    fun updateImageIndex(newIndex: Int) {
        if (unitImagePaths.isNotEmpty()) {
            currentImageIndex = newIndex.coerceIn(0, unitImagePaths.lastIndex)
        }
    }

    fun clearImages(){
        unitImagePaths = emptyList()
    }
}