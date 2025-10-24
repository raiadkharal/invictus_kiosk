package net.invictusmanagement.invictuskiosk.presentation.video_call

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.twilio.video.VideoView
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.LeasingOfficeScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.VoiceMailRecordingScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.video_call.components.VoiceMailConfirmationDialog
import net.invictusmanagement.invictuskiosk.util.ConnectionState

@Composable
fun VideoCallScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    residentId: Int,
    residentDisplayName: String,
    residentActivationCode: String,
    videoCallViewModel: VideoCallViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    val localVideoView = remember { VideoView(context) }
    val remoteVideoView = remember { VideoView(context) }
    var hasAllPermissions by remember { mutableStateOf(false) }
    var showVoiceMailDialog by remember { mutableStateOf(false) }

    val connectionState by remember { derivedStateOf { videoCallViewModel.connectionState } }
    val remoteVideoTrack = videoCallViewModel.remoteVideoTrack
    val token = videoCallViewModel.token
    val remainingSeconds = videoCallViewModel.remainingSeconds
    val sendToVoiceMail = videoCallViewModel.sendToVoiceMail

    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()
    val kioskId by mainViewModel.kioskId.collectAsStateWithLifecycle()
    val currentAccessPoint by mainViewModel.accessPoint.collectAsStateWithLifecycle()
    val kioskActivationCode by mainViewModel.activationCode.collectAsStateWithLifecycle()

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (audioGranted && cameraGranted) {
            hasAllPermissions = true
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            currentAccessPoint?.let {
                videoCallViewModel.connectToVideoCall(it.id, residentActivationCode)
            }
        }
    }

    LaunchedEffect(sendToVoiceMail) {
        if (sendToVoiceMail) {
            showVoiceMailDialog = true
        }
    }
    LaunchedEffect(token) {
        if (hasAllPermissions) {
            if (token.token.isNotEmpty()) {
                videoCallViewModel.connectToRoom(
                    context,
                    token.token,
                    kioskActivationCode,
                    onConnected = {
                        videoCallViewModel.videoTrack?.addSink(localVideoView)
                    },
                    onDisconnected = {
                        navController.navigate(
                            LeasingOfficeScreenRoute(
                                residentId = residentId,
                                residentDisplayName = residentDisplayName,
                                residentActivationCode = residentActivationCode
                            )
                        ) {
                           popUpTo(HomeScreen)
                        }
                    },
                    onMissedCall = {
                        videoCallViewModel.postMissedCall(kioskName ?: "", residentActivationCode)
                        showVoiceMailDialog = true
                    }
                )
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        }

    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        )
    }

    LaunchedEffect(hasAllPermissions) {
        videoCallViewModel.getVideoCallToken(kioskActivationCode)
    }

    // Attach remote video when available
    LaunchedEffect(remoteVideoTrack) {
        remoteVideoTrack?.addSink(remoteVideoView)
    }

    LaunchedEffect(currentAccessPoint) {
        currentAccessPoint?.let { accessPoint ->
            videoCallViewModel.initializeSignalR(kioskId)
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            videoCallViewModel.disconnect()
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
            showBackArrow = false,
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

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = when (connectionState) {
                    ConnectionState.CONNECTING -> ConnectionState.CONNECTING.displayName
                    ConnectionState.CONNECTED -> "Remaining: $remainingSeconds seconds"
                    ConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED.displayName
                    ConnectionState.FAILED -> ConnectionState.FAILED.displayName
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
                    .copy(
                        color = when (connectionState) {
                            ConnectionState.CONNECTING -> colorResource(R.color.btn_text)
                            ConnectionState.CONNECTED -> colorResource(R.color.btn_text)
                            ConnectionState.DISCONNECTED -> colorResource(R.color.red)
                            ConnectionState.FAILED -> colorResource(R.color.red)
                        }
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Local video preview
                AndroidView(
                    factory = { localVideoView },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            horizontal = 16.dp
                        ),
                    thickness = 2.dp,
                    color = colorResource(R.color.divider_color)
                )
                // Remote video view
                AndroidView(
                    factory = { remoteVideoView },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = stringResource(R.string.video_call_message),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall.copy(color = colorResource(R.color.btn_text))
                )

                CustomTextButton(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(16.dp),
                    text = stringResource(R.string.end_call),
                    isGradient = true,
                    onClick = {
                        videoCallViewModel.disconnect()
                    },
                )
            }
        }

    }

    if (showVoiceMailDialog) {
        VoiceMailConfirmationDialog(
            navController = navController,
            onYesClick = {
                showVoiceMailDialog = false
                navController.navigate(
                    VoiceMailRecordingScreenRoute(
                        residentId,
                        residentDisplayName
                    )
                ) {
                    popUpTo(HomeScreen)
                }
            },
            onNoClick = {
                showVoiceMailDialog = false
                navController.navigate(HomeScreen) {
                    popUpTo(HomeScreen)
                }
            }
        )
    }
}