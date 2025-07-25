package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

@androidx.camera.core.ExperimentalGetImage
class BarcodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val isScanning: () -> Boolean,
    private val onSuccess: (String) -> Unit,
    private val onFailure: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val width = image.width
        val height = image.height
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
                        barcode.rawValue?.let {
                            onSuccess(it)
                            imageProxy.close()
                            return@addOnSuccessListener
                        }
                    }
                }
                imageProxy.close()
            }
            .addOnFailureListener {
                onFailure(it)
                imageProxy.close()
            }
    }
}
