package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CameraAndAudioPermission
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.QRScannerUI
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
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
    val keyValidationState by viewModel.digitalKeyValidationState.collectAsStateWithLifecycle()
    val currentAccessPoint by viewModel.accessPoint.collectAsStateWithLifecycle()
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()

    CameraAndAudioPermission(
        onGranted = { viewModel.onPermissionResult(true) },
//        onDenied = { viewModel.onPermissionResult(false) }
    )

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            val digitalKey = keyValidationState.digitalKey
            viewModel.snapshotManager.stopStampRecordingAndSend(
                recipient = digitalKey?.recipient,
                isValid = true,
                accessLogId = digitalKey?.accessLogId
            )
            viewModel.stopScanning()
            viewModel.resetError()
            navController.navigate(
                UnlockedScreenRoute(
                    unitId = keyValidationState.digitalKey?.unitId ?: 0,
                    mapId = keyValidationState.digitalKey?.mapId ?: 0,
                    toPackageCenter = keyValidationState.digitalKey?.toPackageCenter ?: false
                )
            ) {
                popUpTo(HomeScreen)
            }
        } else if (keyValidationState.digitalKey?.isValid == false) {
            viewModel.reportError("Invalid QRCode! Please show valid QRCode.")
            delay(2000)
            viewModel.startScanning()
        }
    }

    LaunchedEffect(uiState.hasCameraPermission) {
        if (uiState.hasCameraPermission) {
            viewModel.startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                executor = executor,
                onScanSuccess = { result ->
                    viewModel.stopScanning()
                    CoroutineScope(Dispatchers.IO).launch {
                        //wait for screenshot
                        while (!viewModel.snapshotManager.isScreenShotTaken)
                            delay(500)
                        viewModel.validateDigitalKey(
                            DigitalKeyDto(
                                accessPointId = currentAccessPoint?.id?.toLong() ?: 0L,
                                key = result
                            )
                        )
                    }
                }
            )

            delay(2000)  // wait for the camera to initialize
            viewModel.snapshotManager.recordStampVideoAndUpload(0L)
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            viewModel.scanner.close()
            viewModel.snapshotManager.cleanupCameraSession()
            viewModel.releaseCamera()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
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
