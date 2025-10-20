package net.invictusmanagement.invictuskiosk.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Promotion(
    val advertise: Boolean = false,
    val approvedUtc: String = "",
    val businessId: Int = 0,
    val createdUtc: String = "",
    val deleted: Boolean = false,
    val description: String = "",
    val fromUtc: String = "",
    val id: Int = 0,
    val isAnytimeCoupon: Boolean = false,
    val isApproved: Boolean = false,
    val isShowOnHome: Boolean = false,
    val name: String = "",
    val numberOfUse: Int = 0,
    val planFromUtc: String = "",
    val planToUtc: String = "",
    val revenueTotal: Int = 0,
    val selectedBusinessId: Int = 0,
    val toUtc: String = ""
)
