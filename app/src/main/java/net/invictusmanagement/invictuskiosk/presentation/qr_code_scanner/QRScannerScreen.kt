package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import android.Manifest
import androidx.camera.lifecycle.ProcessCameraProvider
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.ErrorScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components.QRScannerUI
import net.invictusmanagement.invictuskiosk.util.UiEvent
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

        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    navController.navigate(
                        ErrorScreenRoute(
                            errorMessage = event.errorMessage
                        )
                    ) { popUpTo(HomeScreen) }
                }
            }
        }
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
                currentAccessPointId = currentAccessPoint?.id ?: 0
            )
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            viewModel.scanner.close()
            executor.shutdown()
            viewModel.releaseCamera(context)
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
