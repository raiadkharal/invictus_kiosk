package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKeyState
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.QRScannerUiState
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.UiEvent
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScannerUiState())
    val uiState: StateFlow<QRScannerUiState> = _uiState

    private val _digitalKeyValidationState = MutableStateFlow(DigitalKeyState())
    val digitalKeyValidationState: StateFlow<DigitalKeyState> = _digitalKeyValidationState

    private val _accessPoint = MutableStateFlow<AccessPoint?>(null)
    val accessPoint: StateFlow<AccessPoint?> = _accessPoint

    val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    fun loadInitialData() {
        viewModelScope.launch {
            dataStoreManager.accessPointFlow.collect {
                _accessPoint.value = it
            }
        }
    }
    fun validateDigitalKey(digitalKeyDto: DigitalKeyDto) {
        resetError()
        homeRepository.validateDigitalKey(digitalKeyDto).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _digitalKeyValidationState.value = DigitalKeyState(digitalKey = result.data)
                    setLoading(false)
                }

                is Resource.Error -> {
                    _digitalKeyValidationState.value = DigitalKeyState(error = result.message?: "An unexpected error occurred")
                    reportError(result.message ?: "An unexpected error occurred")
                    setLoading(false)
                }

                is Resource.Loading -> {
                    _digitalKeyValidationState.value = DigitalKeyState(isLoading = true)
                   setLoading(true)
                }

            }
        }.launchIn(viewModelScope)
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _uiState.update { it.copy(hasCameraPermission = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(errorMessage = "Camera permission required") }
        }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun reportError(msg: String) {
        _uiState.update { it.copy(errorMessage = msg) }
    }

    fun stopScanning() {
        _uiState.update { it.copy(isScanning = false) }
    }

    fun startScanning() {
        _uiState.update { it.copy(isScanning = true) }
    }

    fun resetError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        scanner.close()
        super.onCleared()
    }
}