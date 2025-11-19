package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NetworkStatusBar(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    isInternetStable: Boolean,
) {
    // Determine visibility + message + color
    val (show, message, color) = when {
        !isConnected -> Triple(
            true,
            "No Internet Connection",
            Color(0xFFFF3B30) // Red
        )

        isConnected && !isInternetStable -> Triple(
            true,
            "Unstable Internet Connection",
            Color(0xFFFF3B30) // Red
        )

        else -> Triple(false, "", Color.Transparent)
    }

    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(color)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
