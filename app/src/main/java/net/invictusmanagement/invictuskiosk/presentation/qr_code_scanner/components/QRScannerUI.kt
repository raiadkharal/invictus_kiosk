package net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.components

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import net.invictusmanagement.invictuskiosk.R

@Composable
fun QRScannerUI(
    modifier: Modifier = Modifier,
    previewView: PreviewView,
    isLoading: Boolean,
    errorMessage: String?,
    previewVisible: Boolean
) {

    // Scan box dimensions
    val scanBoxSize = 500.dp
    val scanBoxCornerRadius = 16.dp

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Show your QRCode",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
        )
        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Validating...",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.green))
            )
            Spacer(Modifier.height(16.dp))
        } else if (errorMessage != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = errorMessage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.red))
            )
            Spacer(Modifier.height(16.dp))
        }

        Box(
            Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            if (previewVisible) {
                AndroidView(
                    factory = {
                        previewView.apply {
                            scaleType = PreviewView.ScaleType.FIT_CENTER // Changed to FIT_CENTER
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.background))
                        .clip(RoundedCornerShape(12.dp)),
                    update = { view ->
                        // Ensure the view respects parent bounds
                        view.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
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

            //Add scan box border for better visibility
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val boxWidth = scanBoxSize.toPx()
                val boxHeight = scanBoxSize.toPx()
                val left = (size.width - boxWidth) / 2
                val top = (size.height - boxHeight) / 2

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(scanBoxCornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Bottom UI Section
            // You can add additional UI elements here like buttons, instructions, etc.
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                contentAlignment = Alignment.BottomCenter
//            ) {
//                Text(
//                    text = "Position the QR code within the frame",
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        color = colorResource(R.color.btn_text).copy(alpha = 0.7f)
//                    ),
//                    textAlign = TextAlign.Center
//                )
//            }
        }
    }
}
