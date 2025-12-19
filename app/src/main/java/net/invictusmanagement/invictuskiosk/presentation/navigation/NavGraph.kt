package net.invictusmanagement.invictuskiosk.presentation.navigation

import android.Manifest
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
import kotlinx.serialization.json.Json
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.presentation.coupon_detail.CouponsDetailsScreen
import net.invictusmanagement.invictuskiosk.presentation.coupon_list.CouponListScreen
import net.invictusmanagement.invictuskiosk.presentation.coupons.CouponsScreen
import net.invictusmanagement.invictuskiosk.presentation.coupons_business_list.CouponsBusinessListScreen
import net.invictusmanagement.invictuskiosk.presentation.directory.DirectoryScreen
import net.invictusmanagement.invictuskiosk.presentation.response_message.ResponseMessageScreen
import net.invictusmanagement.invictuskiosk.presentation.home.HomeScreen
import net.invictusmanagement.invictuskiosk.presentation.keyboard.KeyboardViewModel
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun NavGraph(
    startDestination: Any,
    innerPadding: PaddingValues,
    keyboardVM: KeyboardViewModel
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<LoginScreen> {
            LoginScreen(modifier = Modifier.padding(innerPadding), navController = navController,keyboardVM = keyboardVM)
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
                byName = args.byName,
                keyboardVM = keyboardVM
            )
        }
        composable<DirectoryScreen> {
            DirectoryScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                keyboardVM = keyboardVM
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
        composable<CouponsBusinessListScreen> {
            val args = it.toRoute<CouponsBusinessListScreen>()
            CouponsBusinessListScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                selectedCouponId = args.selectedCouponId
            )
        }
        composable<CouponListScreen> {
            val args = it.toRoute<CouponListScreen>()

            val businessPromotion = Json.decodeFromString<BusinessPromotion>(
                URLDecoder.decode(args.businessPromotionJson, StandardCharsets.UTF_8.toString())
            )

            CouponListScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                businessPromotion = businessPromotion,
                selectedCouponId = args.selectedCouponId,
            )
        }

        composable<CouponDetailsScreen> {
            val args = it.toRoute<CouponDetailsScreen>()

            val businessPromotion = Json.decodeFromString<BusinessPromotion>(
                URLDecoder.decode(args.businessPromotionJson, StandardCharsets.UTF_8.toString())
            )

            CouponsDetailsScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                promotionId = args.promotionId,
                businessPromotion = businessPromotion
            )
        }
        composable<LeasingOfficeScreenRoute> {
            val args = it.toRoute<LeasingOfficeScreenRoute>()
            LeasingOfficeScreen(
                modifier = Modifier.padding(innerPadding),
                residentId = args.residentId,
                residentDisplayName = args.residentDisplayName,
                residentActivationCode = args.residentActivationCode,
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
            VacancyScreen(modifier = Modifier.padding(innerPadding), navController = navController, keyboardVM = keyboardVM)
        }
        composable<QRScannerScreen> {
            QRScannerScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable<UnlockedScreenRoute> {
            val args = it.toRoute<UnlockedScreenRoute>()
            UnlockScreen(
                modifier = Modifier.padding(innerPadding),
                unitId = args.unitId,
                mapId = args.mapId,
                toPackageCenter = args.toPackageCenter,
                navController = navController
            )
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
        composable<ResponseMessageScreenRoute> {
            val args = it.toRoute<ResponseMessageScreenRoute>()
            ResponseMessageScreen(
                modifier = Modifier.padding(innerPadding),
                message = args.errorMessage,
                navController = navController
            )
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