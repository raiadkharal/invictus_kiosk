package net.invictusmanagement.invictuskiosk.presentation.residents

import androidx.annotation.RequiresPermission
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.domain.model.Resident
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.components.QRCodePanel
import net.invictusmanagement.invictuskiosk.presentation.components.SearchTextField
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResponseMessageScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.QRScannerScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.VideoCallScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.residents.components.ResidentListItem
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme
import net.invictusmanagement.invictuskiosk.util.UiEvent
import net.invictusmanagement.invictuskiosk.util.locale.localizedString

@RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
@Composable
fun ResidentsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    isLeasingOffice: Boolean,
    isUnitNumberSelected: Boolean,
    unitNumber: String,
    filter: String,
    byName: String,
    viewModel: ResidentsViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isConnected by mainViewModel.isConnected.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val residentState by viewModel.residentsState.collectAsStateWithLifecycle()
    val currentAccessPoint by viewModel.accessPoint.collectAsStateWithLifecycle()
    val keyValidationState = viewModel.keyValidationState.value
    var residentList by remember { mutableStateOf(residentState.residents ?: emptyList()) }
    val locationName by mainViewModel.locationName.collectAsStateWithLifecycle()
    val kioskName by mainViewModel.kioskName.collectAsStateWithLifecycle()
    var isError by remember { mutableStateOf(false) }
    var selectedResident by remember { mutableStateOf<Resident?>(null) }

    val filteredResidents =
        residentList.filter { it.displayName.contains(searchQuery.trim(), ignoreCase = true) }

    val previewView = remember { PreviewView(context) }
    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .size(1.dp)
            .alpha(0f)
    )

    LaunchedEffect(selectedResident) {
        if (selectedResident != null) {
            mainViewModel.snapshotManager.startCamera(
                previewView,
                context,
                lifecycleOwner
            )
            delay(2000) // wait for the camera to initialize
            mainViewModel.snapshotManager.recordStampVideoAndUpload(selectedResident!!.id.toLong())
        }
    }

    LaunchedEffect(Unit, isConnected) {
        viewModel.loadInitialData()

        when {
            isUnitNumberSelected -> {
                viewModel.getResidentByUnitNumber(unitNumber)
            }

            isLeasingOffice -> {
                viewModel.getAllLeasingAgents(byName)
            }

            else -> {
                viewModel.getResidentsByName(filter, byName)
            }
        }



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
            delay(2000)
            isError = false
        }
    }
    LaunchedEffect(residentState) {
        residentList = residentState.residents ?: emptyList()
    }

    DisposableEffect(Unit) {
        onDispose {
            isError = false
            viewModel.resetDigitalKeyState()
            mainViewModel.snapshotManager.cleanupCameraSession()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomToolbar(
            title = "$locationName - $kioskName",
            navController = navController
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(6f)
                    .fillMaxSize()
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchTextField(
                        modifier = Modifier.weight(7f),
                        searchQuery = searchQuery,
                        placeholder = if (isLeasingOffice) localizedString(R.string.search_leasing_officer) else localizedString(R.string.search_resident),
                        onValueChange = { searchQuery = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (residentList.isEmpty() || filteredResidents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (residentState.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                if (isLeasingOffice) localizedString(R.string.no_leasing_officers_found) else localizedString(
                                    R.string.no_residents_found
                                ),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.btn_text)
                                )
                            )
                        }
                    }
                } else {
                    // Residents List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredResidents) { resident ->
                            ResidentListItem(
                                residentName = resident.displayName,
                                isSelected = resident.id == selectedResident?.id,
                                onItemClick = {
                                    selectedResident = resident
                                },
                                onCallClick = {
                                    navController.navigate(
                                        VideoCallScreenRoute(
                                            residentId = resident.id,
                                            residentDisplayName = resident.displayName,
                                            residentActivationCode = resident.activationCode ?: "",
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

            }

            Spacer(Modifier.width(16.dp))
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 2.dp,
                color = colorResource(R.color.divider_color)
            )
            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isError) {
                        localizedString(R.string.invalid_key)
                    } else {
                        if (selectedResident != null) localizedString(R.string.pin_title_text) else localizedString(
                            R.string.qr_code_title_text
                        )
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = if (isError) Color.Red else colorResource(
                            R.color.btn_text
                        )
                    )
                )

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    visible = selectedResident != null
                ) {
                    PinInputPanel(
                        modifier = Modifier
                            .fillMaxSize(),
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
                                        activationCode = selectedResident?.activationCode ?: ""
                                    )
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    visible = selectedResident == null
                ) {
                    QRCodePanel(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageWidth = 300.dp,
                        imageHeight = 300.dp,
                        onScanClick = { navController.navigate(QRScannerScreen) }
                    )
                }
            }
        }
    }
}