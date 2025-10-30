package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import android.Manifest
import android.view.ViewGroup
import android.widget.Space
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.BarcodeAnalyzer
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.QRScannerUI
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: QRScannerViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    navController: NavController,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val keyValidationState by viewModel.digitalKeyValidationState.collectAsStateWithLifecycle()
    val currentAccessPoint by viewModel.accessPoint.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    CameraPermission(
        onGranted = { viewModel.onPermissionResult(true) },
        onDenied = { viewModel.onPermissionResult(false) }
    )

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            viewModel.stopScanning()
            viewModel.resetError()
            navController.navigate(
                UnlockedScreenRoute(
                    unitId = keyValidationState.digitalKey?.unitId ?: 0,
                    mapId = keyValidationState.digitalKey?.mapId ?: 0,
                    toPackageCenter = keyValidationState.digitalKey?.toPackageCenter ?: false
                )
            ){
                popUpTo(HomeScreen)
            }
        } else if (keyValidationState.digitalKey?.isValid == false) {
            viewModel.reportError("Invalid QRCode! Please show valid QRCode.")
            delay(2000)
            viewModel.startScanning()
        }
    }

    LaunchedEffect(uiState.hasCameraPermission) {
        if (!uiState.hasCameraPermission) return@LaunchedEffect

        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val analyzer = BarcodeAnalyzer(
                scanner = viewModel.scanner,
                isScanning = { uiState.isScanning },
                onSuccess = { result ->
                    viewModel.stopScanning()
                    viewModel.validateDigitalKey(
                        DigitalKeyDto(
                            accessPointId = currentAccessPoint?.id ?: 0,
                            key = result
                        )
                    )
                },
                onFailure = { viewModel.reportError("Scan failed: ${it.localizedMessage}") }
            )

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            viewModel.reportError("Camera setup failed: ${e.localizedMessage}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.scanner.close()
            executor.shutdown()
            cameraProviderFuture.get().unbindAll()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        // Top UI Section
        CustomToolbar(
            title = "$locationName - $kioskName",
            showBackArrow = true,
            navController = navController
        )
        Spacer(Modifier.height(8.dp))
        QRScannerUI(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            previewView = previewView,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Ask permission when first launched
    LaunchedEffect(Unit) {
        when {
            cameraPermissionState.status.isGranted -> onGranted()
            cameraPermissionState.status.shouldShowRationale -> onDenied()
            else -> cameraPermissionState.launchPermissionRequest()
        }
    }

    // React to permission state changes
    LaunchedEffect(cameraPermissionState.status) {
        when {
            cameraPermissionState.status.isGranted -> onGranted()
            cameraPermissionState.status.shouldShowRationale -> onDenied()
        }
    }
}
