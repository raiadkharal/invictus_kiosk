package net.invictusmanagement.invictuskiosk.presentation.screen_saver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.home.components.UrlVideoPlayer

@Composable
fun ScreenSaver(
    viewModel: ScreenSaverViewModel = hiltViewModel()
) {

    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle()

    LaunchedEffect (Unit){
        viewModel.loadKioskData()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        videoUrl?.let {
            UrlVideoPlayer(
                url = it,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}