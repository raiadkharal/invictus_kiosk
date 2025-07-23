package net.invictusmanagement.invictuskiosk.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import net.invictusmanagement.invictuskiosk.presentation.coupons.CouponsScreen
import net.invictusmanagement.invictuskiosk.presentation.coupons_detail.CouponsDetailScreen
import net.invictusmanagement.invictuskiosk.presentation.directory.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.error.ErrorScreen
import net.invictusmanagement.invictuskiosk.presentation.home.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.leasing_office.LeasingOfficeScreen
import net.invictusmanagement.invictuskiosk.presentation.login.LoginScreen
import net.invictusmanagement.invictuskiosk.presentation.qr_code_scanner.QRScannerScreen
import net.invictusmanagement.invictuskiosk.presentation.residents.ResidentsScreen
import net.invictusmanagement.invictuskiosk.presentation.self_guided_tour.SelfGuidedTourScreen
import net.invictusmanagement.invictuskiosk.presentation.service_key.ServiceKeyScreen
import net.invictusmanagement.invictuskiosk.presentation.unlock.UnlockScreen
import net.invictusmanagement.invictuskiosk.presentation.vacancy.VacancyScreen
import net.invictusmanagement.invictuskiosk.presentation.video_call.VideoCallScreen
import net.invictusmanagement.invictuskiosk.presentation.voice_mail.VoicemailRecordingScreen

@RequiresApi(Build.VERSION_CODES.O)
@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun NavGraph(
    startDestination: Any,
    innerPadding: PaddingValues,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<LoginScreen> {
            LoginScreen(modifier = Modifier.padding(innerPadding), navController = navController)
        }
        composable<HomeScreen>(
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) },
        ) {
            HomeScreen(modifier = Modifier.padding(innerPadding), navController = navController)
        }
        composable<ResidentsScreen> {
            val args = it.toRoute<ResidentsScreen>()
            ResidentsScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                isUnitNumberSelected = args.isUnitSelected,
                isLeasingOffice = args.isLeasingOffice,
                unitNumber = args.unitNumber,
                filter = args.filter,
                byName = args.byName
            )
        }
        composable<DirectoryScreen> {
            DirectoryScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable<ServiceKeyScreen> {
            ServiceKeyScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable<CouponsScreen> {
            CouponsScreen(modifier = Modifier.padding(innerPadding), navController = navController)
        }
        composable<CouponsDetailScreen> {
            val args = it.toRoute<CouponsDetailScreen>()
            CouponsDetailScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                selectedCouponId = args.selectedCouponId
            )
        }
        composable<LeasingOfficeScreen> {
            LeasingOfficeScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable<SelfGuidedTourScreen> {
            SelfGuidedTourScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable<VacancyScreen> {
            VacancyScreen(modifier = Modifier.padding(innerPadding), navController = navController)
        }
        composable<QRScannerScreen> {
            QRScannerScreen(navController = navController)
        }
        composable<UnlockedScreen> {
            UnlockScreen(modifier = Modifier.padding(innerPadding), navController = navController)
        }
        composable<VideoCallScreenRoute> {
            val args = it.toRoute<VideoCallScreenRoute>()
            VideoCallScreen(
                modifier = Modifier.padding(innerPadding),
                residentId = args.residentId,
                residentDisplayName = args.residentDisplayName,
                residentActivationCode = args.residentActivationCode,
                navController = navController
            )
        }
        composable<ErrorScreenRoute> {
            val args = it.toRoute<ErrorScreenRoute>()
            ErrorScreen(modifier = Modifier.padding(innerPadding), errorMessage = args.errorMessage, navController = navController)
        }

        composable<VoiceMailRecordingScreenRoute> {
            val args = it.toRoute<VoiceMailRecordingScreenRoute>()
            VoicemailRecordingScreen(
                modifier = Modifier.padding(innerPadding),
                residentId = args.residentId,
                residentDisplayName = args.residentDisplayName,
                navController = navController
            )
        }

    }
}