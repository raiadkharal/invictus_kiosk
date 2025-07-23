package net.invictusmanagement.invictuskiosk.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Serializable
object HomeScreen

@Serializable
object DirectoryScreen

@Serializable
data class ResidentsScreen(val isUnitSelected: Boolean,val unitNumber: String,val filter: String, val byName: String,val isLeasingOffice: Boolean = false)

@Serializable
object ServiceKeyScreen

@Serializable
object CouponsScreen

@Serializable
data class CouponsDetailScreen(val selectedCouponId: String)

@Serializable
object LeasingOfficeCallingScreen

@Serializable
object LeasingOfficeScreen

@Serializable
object SelfGuidedTourScreen

@Serializable
object VacancyScreen

@Serializable
object QRScannerScreen

@Serializable
object UnlockedScreen

@Serializable
data class VideoCallScreenRoute(val residentId : Int,val residentDisplayName : String,val residentActivationCode : String)

@Serializable
data class ErrorScreenRoute(val errorMessage: String)

@Serializable
data class VoiceMailRecordingScreenRoute(val residentId : Int,val residentDisplayName : String)

