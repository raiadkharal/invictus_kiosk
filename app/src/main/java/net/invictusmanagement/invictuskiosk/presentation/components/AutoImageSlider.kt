package net.invictusmanagement.invictuskiosk.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel

@Composable
fun AutoImageSlider(
    modifier: Modifier = Modifier,
    unitImages: List<ByteArray>,
    currentImageIndex: Int,
    mainViewModel: MainViewModel,
    resetSleepTimer: (() -> Unit)? = null
) {
    var currentIndex by remember { mutableIntStateOf(currentImageIndex) }
    var resetKey by remember { mutableIntStateOf(0) }

    // Automatically switch images every 5 seconds
    LaunchedEffect(unitImages,resetKey) {
        if (unitImages.isNotEmpty()) {
            while (true) {
                delay(5000) // 5 seconds
                currentIndex = (currentIndex + 1) % unitImages.size
                mainViewModel.updateImageIndex(currentIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(6f)
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            if (unitImages.isNotEmpty()) {
                AsyncImage(
                    model = unitImages[currentIndex],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            resetSleepTimer?.invoke()
                            currentIndex = if (currentIndex - 1 < 0) unitImages.lastIndex else currentIndex - 1
                            mainViewModel.updateImageIndex(currentIndex)

                            resetKey++
                        },
                    tint = Color.White
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            resetSleepTimer?.invoke()
                            currentIndex = (currentIndex + 1) % unitImages.size
                            mainViewModel.updateImageIndex(currentIndex)

                            resetKey++
                        },
                    tint = Color.White
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.placeholder_image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Dots indicator
        if (unitImages.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                repeat(unitImages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == currentIndex) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) Color.White
                                else Color.LightGray.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }
    }
}
