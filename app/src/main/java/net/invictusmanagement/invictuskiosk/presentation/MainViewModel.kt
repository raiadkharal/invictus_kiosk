package net.invictusmanagement.invictuskiosk.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.repository.UnitMapRepository
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.NetworkMonitor
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val unitMapRepository: UnitMapRepository,
    private val networkMonitor: NetworkMonitor
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

    private val _mapImage = MutableStateFlow<ByteArray?>(null)
    val mapImage: StateFlow<ByteArray?> = _mapImage

    var unitImages by mutableStateOf<List<ByteArray>>(emptyList())
        private set

    var currentImageIndex by mutableIntStateOf(0)
        private set

    init {
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

    fun loadImages(unitId: Long, imageIds: List<Long>) {
        viewModelScope.launch {
            try {
                val byteArrays = mutableListOf<ByteArray>()

                for (id in imageIds) {
                    unitMapRepository.getUnitImage(unitId, id)
                        .collect { result ->
                            when (result) {
                                is Resource.Success -> result.data?.let { byteArrays.add(it) }
                                is Resource.Error -> {
                                    // Handle error if needed
                                }
                                is Resource.Loading -> {
                                    // Optional loading logic
                                }
                            }
                        }
                }

                // When all images are collected
                unitImages = byteArrays
                currentImageIndex = 0

            } catch (e: Exception) {
                e.printStackTrace()
                unitImages = emptyList()
            }
        }
    }


    fun showNextImage() {
        if (unitImages.isNotEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % unitImages.size
        }
    }

    fun updateImageIndex(newIndex: Int) {
        if (unitImages.isNotEmpty()) {
            currentImageIndex = newIndex.coerceIn(0, unitImages.lastIndex)
        }
    }

    fun showPreviousImage() {
        if (unitImages.isNotEmpty()) {
            currentImageIndex =
                if (currentImageIndex == 0) unitImages.lastIndex else currentImageIndex - 1
        }
    }

    fun clearImages(){
        unitImages = emptyList()
    }
}