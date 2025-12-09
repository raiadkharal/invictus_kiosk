package net.invictusmanagement.invictuskiosk.presentation.leasing_office

import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.navigation.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResponseMessageScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.VideoCallScreenRoute
import net.invictusmanagement.invictuskiosk.util.UiEvent
import net.invictusmanagement.invictuskiosk.util.locale.localizedString

@RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
@Composable
fun LeasingOfficeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    residentId: Int,
    residentDisplayName: String,
    residentActivationCode: String,
    mainViewModel: MainViewModel = hiltViewModel(),
    viewModel: LeasingOfficeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()
    val keyValidationState = viewModel.keyValidationState.value
    var isError by remember { mutableStateOf(false) }
    val currentAccessPoint by viewModel.accessPoint.collectAsStateWithLifecycle()

    val buttons: List<List<String>> = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0", "X", "clear")
    )

    val previewView = remember { PreviewView(context) }
    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .size(1.dp)
            .alpha(0f)
    )

    LaunchedEffect(Unit) {
        mainViewModel.snapshotManager.startCamera(
            previewView,
            context,
            lifecycleOwner
        )
        delay(2000) // wait for the camera to initialize
        mainViewModel.snapshotManager.recordStampVideoAndUpload(residentId.toLong())
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()

        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    navController.navigate(
                        ResponseMessageScreenRoute(
                            errorMessage = event.errorMessage
                        )
                    ) { popUpTo(HomeScreen) }
                }
            }
        }
    }
    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            val digitalKey = keyValidationState.digitalKey
            mainViewModel.snapshotManager.stopStampRecordingAndSend(
                recipient = digitalKey.recipient,
                isValid = true,
                accessLogId = digitalKey.accessLogId
            )
            isError = false
            navController.navigate(
                UnlockedScreenRoute(
                    unitId = keyValidationState.digitalKey.unitId,
                    mapId = keyValidationState.digitalKey.mapId,
                    toPackageCenter = keyValidationState.digitalKey.toPackageCenter
                )
            ) {
                popUpTo(HomeScreen)
            }
        } else if (keyValidationState.digitalKey?.isValid == false) {
            isError = true
            delay(3000)
            isError = false
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.snapshotManager.cleanupCameraSession()
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
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                text = residentDisplayName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(color = colorResource(R.color.btn_text))
            )

            Row(
                modifier = Modifier
                    .width(720.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CustomTextButton(
                    modifier = Modifier
                        .weight(1f),
                    text = localizedString(R.string.directory),
                    onClick = {
                        navController.navigate(DirectoryScreen)
                    }
                )
                Spacer(Modifier.width(16.dp))
                CustomTextButton(
                    modifier = Modifier
                        .weight(1f),
                    isGradient = true,
                    text = localizedString(R.string.video_call),
                    onClick = {
                        navController.navigate(
                            VideoCallScreenRoute(
                                residentId = residentId,
                                residentDisplayName = residentDisplayName,
                                residentActivationCode = residentActivationCode
                            )
                        ) {
                            popUpTo(HomeScreen)
                        }
                    }
                )
            }

            PinInputPanel(
                modifier = Modifier
                    .weight(1f)
                    .width(720.dp),
                buttons = buttons,
                isError = isError,
                onCompleted = { pinCode ->
                    CoroutineScope(Dispatchers.IO).launch {
                        //wait for screenshot
                        while (!mainViewModel.snapshotManager.isScreenShotTaken)
                            delay(500)
                        viewModel.validateDigitalKey(
                            DigitalKeyDto(
                                accessPointId = currentAccessPoint?.id?.toLong() ?: 0L,
                                key = pinCode,
                                activationCode = residentActivationCode
                            )
                        )
                    }
                }
            )

        }

    }

}