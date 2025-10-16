package net.invictusmanagement.invictuskiosk.data.remote.dto

data class PromotionDto(
    val advertise: Boolean?,
    val advertiseFile: Any?,
    val approvedUtc: String?,
    val business: Any?,
    val businessId: Int?,
    val businessList: List<Any?>?,
    val createdUtc: String?,
    val `data`: Any?,
    val deleted: Boolean?,
    val description: String?,
    val fromUtc: String?,
    val id: Int?,
    val isAnytimeCoupon: Boolean?,
    val isApproved: Boolean?,
    val isShowOnHome: Boolean?,
    val name: String?,
    val numberOfUse: Int?,
    val planFromUtc: String?,
    val planToUtc: String?,
    val promotionAdvertiseDetail: Any?,
    val promotionAdvertises: List<Any?>?,
    val revenueTotal: Int?,
    val selectedBusinessId: Int?,
    val toUtc: String?
)