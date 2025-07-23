package net.invictusmanagement.invictuskiosk.presentation.video_call

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.twilio.video.VideoView

@Composable
fun VideoCallScreen1(
    modifier: Modifier = Modifier,
    roomName: String,
    viewModel: VideoCallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit
) {
    val context = LocalContext.current
    val localVideoView = remember { VideoView(context) }
    val remoteVideoView = remember { VideoView(context) }

    val connectionState by remember { derivedStateOf { viewModel.connectionState } }
    val remoteVideoTrack = viewModel.remoteVideoTrack
    val token = viewModel.token

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.connectToRoom(
                context,
                token.token,
                roomName,
                onConnected = {
                    viewModel.videoTrack?.addSink(localVideoView)
                },
                onDisconnected = onCallEnded,
                onMissedCall = onCallEnded
            )
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getVideoCallToken(roomName)
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Attach remote video when available
    LaunchedEffect(remoteVideoTrack) {
        remoteVideoTrack?.addSink(remoteVideoView)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Remote video full screen
        AndroidView(
            factory = { remoteVideoView },
            modifier = Modifier.fillMaxSize()
        )

        // Local video small preview
        AndroidView(
            factory = { localVideoView },
            modifier = Modifier
                .size(120.dp, 160.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Status: $connectionState")
        }

        Button(
            onClick = {
                viewModel.disconnect()
                onCallEnded()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text("End Call")
        }
    }
}

