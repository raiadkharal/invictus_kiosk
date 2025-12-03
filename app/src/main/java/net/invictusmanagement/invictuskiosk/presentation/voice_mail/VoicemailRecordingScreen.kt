package net.invictusmanagement.invictuskiosk.presentation.voice_mail

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.ErrorScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun VoicemailRecordingScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    residentId: Int,
    residentDisplayName: String,
    mainViewModel: MainViewModel = hiltViewModel(),
    viewModel: VoicemailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()

    val countdown by viewModel.countdown
    val isRecordingStarted by viewModel.isRecordingStarted
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        if (!viewModel.isRecordingStarted.value) {
            viewModel.startCountdown()
        }
    }

    LaunchedEffect(previewView) {
        viewModel.setupCamera(previewView, context, lifecycleOwner)
    }

    LaunchedEffect(uploadState) {
        //on upload success navigate to home screen
        if (uploadState.data > 0) {
            navController.navigate(HomeScreen) {
                popUpTo(HomeScreen)
            }
        } else if (uploadState.error.isNotEmpty()) {
            //on upload error navigate to error screen
            navController.navigate(ErrorScreenRoute(errorMessage = uploadState.error)) {
                popUpTo(HomeScreen)
            }
        }
    }

    if (isRecordingStarted) {
        LaunchedEffect(Unit) {
            viewModel.startRecording(context) { file ->
                val fileSizeInMB = file.length() / (1024 * 1024)
                Log.d("FileSize", "Size: $fileSizeInMB MB")
                viewModel.uploadVoicemail(file, residentId.toLong())
                viewModel.resetState()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = residentDisplayName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(color = colorResource(R.color.btn_text))
            )

            Spacer(Modifier.height(8.dp))

            when {
                uploadState.isLoading -> UploadingMessage(modifier = Modifier.weight(1f))
                else -> RecordingContent(
                    modifier = Modifier.weight(1f),
                    countdown = countdown,
                    viewModel = viewModel,
                    previewView = previewView
                )
            }
        }
    }
}


@Composable
private fun UploadingMessage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Uploading voicemail...",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(color = colorResource(R.color.btn_text))
        )
    }
}

@Composable
private fun RecordingContent(
    modifier: Modifier = Modifier,
    countdown: Int,
    viewModel: VoicemailViewModel,
    previewView: PreviewView
) {
    if (countdown > 0) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Recording start in $countdown seconds...",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
                .copy(
                    color = colorResource(R.color.btn_text)
                )
        )
    }

    CameraPreview(modifier, previewView)

    CustomTextButton(
        modifier = Modifier
            .width(300.dp)
            .padding(16.dp),
        text = stringResource(R.string.finish_recording).uppercase(),
        isGradient = true,
        enabled = countdown == 0,
        onClick = {
            viewModel.stopRecording()
        },
    )
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    previewView: PreviewView
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .padding(vertical = 16.dp, horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Local video small preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
        )
    }
}


