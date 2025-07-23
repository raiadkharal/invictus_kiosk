package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QRScannerScreen(
    navController: NavController? = null,
    onResult: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(true) }

    // Camera permission handling
    CameraPermission(
        onGranted = { hasCameraPermission = true },
        onDenied = { errorMessage = "Camera permission required" }
    )

    // Camera preview setup
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    // Barcode scanner setup
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    // Scan box dimensions
    val scanBoxSize = 350.dp
    val scanBoxCornerRadius = 16.dp

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect

        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor, { imageProxy ->
                        if (!isScanning) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        processImage(
                            imageProxy = imageProxy,
                            scanner = barcodeScanner,
                            onSuccess = { result ->
                                // Handle result
                                navController?.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("scan_result", result)
                                onResult(result)
                                isScanning = false
                                navController?.popBackStack()
                            },
                            onFailure = { e ->
                                errorMessage = "Scan failed: ${e.localizedMessage}"
                            }
                        )
                    })
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Camera setup failed: ${e.localizedMessage}"
            isLoading = false
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            executor.shutdown()
            cameraProviderFuture.get().unbindAll()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Blurred overlay with scan box cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    // Create scan box area
                    val boxWidth = scanBoxSize.toPx()
                    val boxHeight = scanBoxSize.toPx()
                    val left = (size.width - boxWidth) / 2
                    val top = (size.height - boxHeight) / 2

                    // Create path for the scan box cutout
                    val holePath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(left, top, left + boxWidth, top + boxHeight),
                                cornerRadius = CornerRadius(scanBoxCornerRadius.toPx())
                            )
                        )
                    }

                    // Draw semi-transparent overlay
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f)
                    )

                    // Cut out the scan box area
                    drawPath(
                        path = holePath,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
        )

        // Scan box border
        Box(
            modifier = Modifier
                .size(scanBoxSize)
                .align(Alignment.Center)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(scanBoxCornerRadius)
                )
        )

        // Corner indicators
//        Box(
//            modifier = Modifier
//                .size(scanBoxSize)
//                .align(Alignment.Center)
//                .clip(RoundedCornerShape(scanBoxCornerRadius))
//        ) {
//            // Top-left corner
//            Box(
//                modifier = Modifier
//                    .size(30.dp, 4.dp)
//                    .align(Alignment.TopStart)
//                    .background(Color.Green)
//            )
//            Box(
//                modifier = Modifier
//                    .size(4.dp, 30.dp)
//                    .align(Alignment.TopStart)
//                    .background(Color.Green)
//            )
//
//            // Top-right corner
//            Box(
//                modifier = Modifier
//                    .size(30.dp, 4.dp)
//                    .align(Alignment.TopEnd)
//                    .background(Color.Green)
//            )
//            Box(
//                modifier = Modifier
//                    .size(4.dp, 30.dp)
//                    .align(Alignment.TopEnd)
//                    .background(Color.Green)
//            )
//
//            // Bottom-left corner
//            Box(
//                modifier = Modifier
//                    .size(30.dp, 4.dp)
//                    .align(Alignment.BottomStart)
//                    .background(Color.Green)
//            )
//            Box(
//                modifier = Modifier
//                    .size(4.dp, 30.dp)
//                    .align(Alignment.BottomStart)
//                    .background(Color.Green)
//            )
//
//            // Bottom-right corner
//            Box(
//                modifier = Modifier
//                    .size(30.dp, 4.dp)
//                    .align(Alignment.BottomEnd)
//                    .background(Color.Green)
//            )
//            Box(
//                modifier = Modifier
//                    .size(4.dp, 30.dp)
//                    .align(Alignment.BottomEnd)
//                    .background(Color.Green)
//            )
//        }

        // Instructions
        Text(
            text = "Align QR code within the frame",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        // Close button
        IconButton(
            onClick = {
                navController?.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close scanner",
                tint = Color.White
            )
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error message
        errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        // Retry initialization
                        hasCameraPermission = false
                    }
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(permissionState) {
        when {
            permissionState.status.isGranted -> onGranted()
            permissionState.status.shouldShowRationale -> onDenied()
            else -> permissionState.launchPermissionRequest()
        }
    }
}

//@androidx.camera.core.ExperimentalGetImage
//private fun processImage(
//    imageProxy: ImageProxy,
//    scanner: BarcodeScanner,
//    onSuccess: (String) -> Unit,
//    onFailure: (Exception) -> Unit
//) {
//    val mediaImage = imageProxy.image
//    if (mediaImage == null) {
//        imageProxy.close()
//        return
//    }
//
//    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//
//    scanner.process(image)
//        .addOnSuccessListener { barcodes ->
//            for (barcode in barcodes) {
//                barcode.rawValue?.let { value ->
//                    onSuccess(value)
//                    imageProxy.close()
//                    return@addOnSuccessListener
//                }
//            }
//            imageProxy.close()
//        }
//        .addOnFailureListener { e ->
//            onFailure(e as Exception)
//            imageProxy.close()
//        }
//}

@androidx.camera.core.ExperimentalGetImage
private fun processImage(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

    val width = image.width
    val height = image.height

    // Define scan box in image coordinates (center 50% area as example)
    val scanRegion = android.graphics.Rect(
        (width * 0.25).toInt(),
        (height * 0.25).toInt(),
        (width * 0.75).toInt(),
        (height * 0.75).toInt()
    )

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                val box = barcode.boundingBox
                if (box != null && scanRegion.contains(box)) {
                    barcode.rawValue?.let { value ->
                        onSuccess(value)
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                }
            }
            imageProxy.close()
        }
        .addOnFailureListener { e ->
            onFailure(e as Exception)
            imageProxy.close()
        }
}

