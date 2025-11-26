package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.commons.Resource
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint
import net.invictusmanagement.invictuskiosk.domain.model.DigitalKeyState
import net.invictusmanagement.invictuskiosk.domain.repository.HomeRepository
import net.invictusmanagement.invictuskiosk.domain.repository.RelayManagerRepository
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.BarcodeAnalyzer
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.QRScannerUiState
import net.invictusmanagement.invictuskiosk.util.DataStoreManager
import net.invictusmanagement.invictuskiosk.util.GlobalLogger
import net.invictusmanagement.invictuskiosk.util.UiEvent
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val dataStoreManager: DataStoreManager,
    private val relayRepository: RelayManagerRepository,
    private val logger: GlobalLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScannerUiState())
    val uiState: StateFlow<QRScannerUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
                    if (result.data?.isValid == true) {
                        //send open AccessPoint request to the relay manager if the digital key is valid
                        relayRepository.openAccessPoint(
                            accessPoint.value?.relayPort,
                            accessPoint.value?.relayOpenTimer,
                            accessPoint.value?.relayDelayTimer
                        )
                    }
                    _digitalKeyValidationState.value = DigitalKeyState(digitalKey = result.data)
                    setLoading(false)
                }

                is Resource.Error -> {
                    _eventFlow.emit(
                        UiEvent.ShowError(
                            Constants.QR_CODE_GENERIC_ERROR
                        )
                    )
                    setLoading(false)
                }

                is Resource.Loading -> {
                    _digitalKeyValidationState.value = DigitalKeyState(isLoading = true)
                    setLoading(true)
                }

            }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalGetImage::class)
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        executor: Executor,
        currentAccessPointId: Int,
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                val analyzer = BarcodeAnalyzer(
                    scanner = scanner,
                    isScanning = { uiState.value.isScanning },
                    onSuccess = { result ->
                        stopScanning()
                        validateDigitalKey(
                            DigitalKeyDto(
                                accessPointId = currentAccessPointId,
                                key = result
                            )
                        )
                    },
                    onFailure = { reportError("Scan failed: ${it.localizedMessage}") }
                )

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }

                cameraProvider.unbindAll()

                val frontCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val backCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val cameraSelector = if (cameraProvider.hasCamera(frontCameraSelector)) {
                    frontCameraSelector
                } else {
                    backCameraSelector
                }

                // Must be on main thread
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                logger.logError("QrCodeScanner/StartCamera", "Error binding camera ${e.localizedMessage}", e)
                reportError(Constants.getFriendlyCameraError(e))
            }
        }, ContextCompat.getMainExecutor(context))
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

    fun releaseCamera(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(150)
                val provider = ProcessCameraProvider.getInstance(context).get()
                provider.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        scanner.close()
        super.onCleared()
    }
}