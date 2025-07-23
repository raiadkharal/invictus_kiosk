package net.invictusmanagement.invictuskiosk.presentation.home

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.invictusmanagement.invictuskiosk.R
import net.invictusmanagement.invictuskiosk.commons.Constants
import net.invictusmanagement.invictuskiosk.data.remote.dto.DigitalKeyDto
import net.invictusmanagement.invictuskiosk.presentation.components.CustomIconButton
import net.invictusmanagement.invictuskiosk.presentation.components.CustomTextButton
import net.invictusmanagement.invictuskiosk.presentation.components.PinInputPanel
import net.invictusmanagement.invictuskiosk.presentation.components.QRCodePanel
import net.invictusmanagement.invictuskiosk.presentation.home.components.CustomBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.HomeBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.PinCodeBottomSheet
import net.invictusmanagement.invictuskiosk.presentation.home.components.UrlVideoPlayer
import net.invictusmanagement.invictuskiosk.presentation.navigation.CouponsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.QRScannerScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.SelfGuidedTourScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ServiceKeyScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.UnlockedScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.VacancyScreen
import net.invictusmanagement.invictuskiosk.ui.theme.InvictusKioskTheme
import net.invictusmanagement.invictuskiosk.util.UiEvent
import net.invictusmanagement.invictuskiosk.util.locale.LocaleHelper
import net.invictusmanagement.invictuskiosk.presentation.MainViewModel
import net.invictusmanagement.invictuskiosk.presentation.components.CustomToolbar
import net.invictusmanagement.invictuskiosk.presentation.navigation.ErrorScreenRoute
import net.invictusmanagement.invictuskiosk.presentation.navigation.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.navigation.VideoCallScreenRoute
import net.invictusmanagement.invictuskiosk.util.IntroButtons


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val keyValidationState by viewModel.digitalKeyValidationState.collectAsState()
    var currentLocale by remember { mutableStateOf(LocaleHelper.getCurrentLocale(context)) }
    var isPinSelected by remember { mutableStateOf(false) }
    var showHomeBottomSheet by remember { mutableStateOf(false) }
    var showPinCodeBottomSheet by remember { mutableStateOf(false) }
    val currentAccessPoint by viewModel.accessPoint.collectAsState()
    val activationCode by viewModel.activationCode.collectAsState()
    var isError by remember { mutableStateOf(false) }
    val screenSaverUrl by viewModel.videoUrl.collectAsState()
    val locationName by mainViewModel.locationName.collectAsState()
    val kioskName by mainViewModel.kioskName.collectAsState()
    val leasingOfficeDetails by viewModel.leasingOfficeDetails.collectAsState()
    val introButtons by viewModel.introButtons.collectAsState()

    // Get result from qrcode screen using SavedStateHandle
    val scanResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("scan_result", null)
        ?.collectAsState()

    // Process qr code result when available
    LaunchedEffect(scanResult?.value) {
        scanResult?.value?.let { result ->
            // validate digital key from qrcode
            viewModel.validateDigitalKey(
                DigitalKeyDto(
                    accessPointId = currentAccessPoint?.id ?: 0,
                    key = result
                )
            )

            // Clear result to avoid reprocessing
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scan_result")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAccessPoints()
    }
    LaunchedEffect(keyValidationState) {
        if (keyValidationState.digitalKey?.isValid == true) {
            isError = false
            delay(2000)
            navController.navigate(UnlockedScreen)
        } else if (keyValidationState.digitalKey?.isValid == false) {
            isError = true
            delay(3000)
            isError = false
        }
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    navController.navigate(
                        ErrorScreenRoute(
                            errorMessage = event.errorMessage
                        )
                    ) { popUpTo(HomeScreen) }
                }
            }
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
            showBackArrow = false,
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
                UrlVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    url = screenSaverUrl
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (introButtons.contains(IntroButtons.RESIDENTS.value))
                        CustomIconButton(
                            modifier = Modifier.weight(1f),
                            icon = R.drawable.ic_directory,
                            text = stringResource(R.string.directory),
                            onClick = {
                                navController.navigate(DirectoryScreen)
                            })

                    Spacer(Modifier.width(20.dp))

                    if (introButtons.contains(IntroButtons.KEYS.value))
                        CustomIconButton(
                            modifier = Modifier.weight(1f),
                            icon = R.drawable.ic_service_key,
                            text = stringResource(R.string.service_key_all_caps),
                            onClick = {
                                navController.navigate(ServiceKeyScreen)
                            })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (introButtons.contains(IntroButtons.SELF_TOUR.value))
                        CustomIconButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp),
                            icon = R.drawable.ic_check_in,
                            text = stringResource(R.string.check_in),
                            onClick = { navController.navigate(SelfGuidedTourScreen) })

                    Spacer(Modifier.width(20.dp))

                    if (introButtons.contains(IntroButtons.LEASING_OFFICE.value))
                        CustomIconButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp),
                            icon = R.drawable.ic_leasing_office,
                            text = stringResource(R.string.leasing_office),
                            onClick = {
                                if (leasingOfficeDetails?.leasingOfficer != null && leasingOfficeDetails?.allowSinglePushCallToLeasingOffice == true) {
                                    navController.navigate(
                                        VideoCallScreenRoute(
                                            residentId = leasingOfficeDetails?.leasingOfficer?.id
                                                ?: 0,
                                            residentDisplayName = leasingOfficeDetails?.leasingOfficer?.displayName
                                                ?: "",
                                            residentActivationCode = leasingOfficeDetails?.leasingOfficer?.activationCode
                                                ?: ""
                                        )
                                    )
                                } else {
                                    //navigate to residents screen
                                    navController.navigate(
                                        ResidentsScreen(
                                            isLeasingOffice = false,
                                            isUnitSelected = true,
                                            unitNumber = "*",
                                            filter = "",
                                            byName = "f"
                                        )
                                    )
                                }
                            })

                    Spacer(Modifier.width(20.dp))

                    if (introButtons.contains(IntroButtons.PROMOTIONS.value))
                        CustomIconButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp),
                            icon = R.drawable.ic_coupons,
                            text = stringResource(R.string.local_coupons),
                            onClick = { navController.navigate(CouponsScreen) })

                    Spacer(Modifier.width(20.dp))

                    if (introButtons.contains(IntroButtons.VACANCIES.value))
                        CustomIconButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp),
                            icon = R.drawable.ic_vacancies,
                            text = stringResource(R.string.vacancies),
                            onClick = { navController.navigate(VacancyScreen) })
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CustomTextButton(
                        modifier = Modifier.weight(1f),
                        isGradient = isPinSelected,
                        text = stringResource(R.string.pin),
                        onClick = { isPinSelected = true })
                    Spacer(Modifier.width(16.dp))
                    CustomTextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.qr_code),
                        isGradient = !isPinSelected,
                        onClick = { isPinSelected = false })
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (isError) {
                        stringResource(R.string.invalid_key)
                    } else {
                        if (isPinSelected) stringResource(R.string.pin_title_text) else stringResource(
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
                    visible = isPinSelected
                ) {
                    PinInputPanel(
                        modifier = Modifier.fillMaxSize(),
                        isError = isError,
                        onCompleted = { pinCode ->
                            viewModel.validateDigitalKey(
                                DigitalKeyDto(
                                    accessPointId = currentAccessPoint?.id ?: 0,
                                    key = pinCode,
                                    activationCode = activationCode ?: ""
                                )
                            )
                        }
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                    visible = !isPinSelected
                ) {
                    QRCodePanel(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageWidth = 250.dp,
                        imageHeight = 250.dp,
                        onScanClick = { navController.navigate(QRScannerScreen) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .clickable(onClick = {
                                // Update to Spanish (Mexico)
                                CoroutineScope(Dispatchers.IO).launch {
                                    LocaleHelper.toggleLocale(context)
                                    currentLocale = LocaleHelper.getCurrentLocale(context)

                                    withContext(Dispatchers.Main) {
                                        (context as Activity).recreate()
                                    }
                                }
                            }),
                        painter = painterResource(R.drawable.ic_language),
                        contentDescription = "Language icon"
                    )
                    Spacer(Modifier.width(12.dp))
                    Image(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .clickable(onClick = { showHomeBottomSheet = true }),
                        painter = painterResource(R.drawable.ic_wheel_chair),
                        contentDescription = "Wheel chair icon"
                    )
                }
            }
        }
    }



    CustomBottomSheet(
        isVisible = showHomeBottomSheet,
        onDismiss = { showHomeBottomSheet = false }
    ) {
        // Place the bottom sheet content here
        HomeBottomSheet(
            onPinCodeClick = {
                showHomeBottomSheet = false
                showPinCodeBottomSheet = true
            },
            onQrCodeClick = {
                //navigate to qr code
                navController.navigate(QRScannerScreen)
            },
            onBackClick = {
                showHomeBottomSheet = false
            },
            onHomeClick = {
                showHomeBottomSheet = false
            },
            onCallBtnClick = { resident ->
                navController.navigate(
                    VideoCallScreenRoute(
                        residentId = resident.id,
                        residentDisplayName = resident.displayName,
                        residentActivationCode = resident.activationCode ?: ""
                    )
                )
            }
        )
    }

    CustomBottomSheet(
        isVisible = showPinCodeBottomSheet,
        onDismiss = { showPinCodeBottomSheet = false }
    ) {
        // Place the bottom sheet content here
        PinCodeBottomSheet(
            navController = navController,
            onHomeClick = {
                showPinCodeBottomSheet = false
                showHomeBottomSheet = false
            },
            onBackClick = {
                showPinCodeBottomSheet = false
                showHomeBottomSheet = true
            }
        )
    }
}


@Preview(widthDp = 1400, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    InvictusKioskTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}