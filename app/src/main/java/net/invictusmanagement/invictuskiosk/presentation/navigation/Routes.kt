package net.invictusmanagement.invictuskiosk.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Serializable
object HomeScreen

@Serializable
object DirectoryScreen

@Serializable
data class ResidentsScreen(
    val isUnitSelected: Boolean,
    val unitNumber: String,
    val filter: String,
    val byName: String,
    val isLeasingOffice: Boolean = false
)

@Serializable
object ServiceKeyScreen

@Serializable
object CouponsScreen

@Serializable
data class CouponsBusinessListScreen(val selectedCouponId: String)

@Serializable
data class CouponListScreen(val selectedCouponId: String,val businessPromotionJson: String )

@Serializable
data class CouponDetailsScreen(val promotionId: Int,val businessPromotionJson: String)

@Serializable
data class LeasingOfficeScreenRoute(
    val residentId: Int,
    val residentDisplayName: String,
    val residentActivationCode: String
)

@Serializable
object SelfGuidedTourScreen

@Serializable
object VacancyScreen

@Serializable
object QRScannerScreen

@Serializable
data class UnlockedScreenRoute(val unitId: Int, val mapId: Int, val toPackageCenter: Boolean = false)

@Serializable
data class VideoCallScreenRoute(
    val residentId: Int,
    val residentDisplayName: String,
    val residentActivationCode: String
)

@Serializable
data class ResponseMessageScreenRoute(val errorMessage: String)

@Serializable
data class VoiceMailRecordingScreenRoute(val residentId: Int, val residentDisplayName: String)

