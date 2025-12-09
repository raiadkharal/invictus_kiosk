package net.invictusmanagement.invictuskiosk.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.colorResource
import net.invictusmanagement.invictuskiosk.R

/**
 * A customizable bottom sheet that slides up from the bottom.
 *
 * @param isVisible Controls visibility; when true, the sheet is shown.
 * @param onDismiss Called when the backdrop is tapped to hide.
 * @param heightFraction Fraction of screen height the sheet should occupy (0f–1f).
 * @param content Composable content displayed inside the sheet.
 */
@Composable
fun CustomBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    heightFraction: Float = 0.5f,
    content: @Composable () -> Unit
) {
    if (!isVisible) return

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onDismiss)
        )
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightFraction),
                color = colorResource(id = R.color.background).copy(alpha = 0.98f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}


