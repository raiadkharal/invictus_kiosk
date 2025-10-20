package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.Promotion

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

fun PromotionDto.toPromotion(): Promotion {
    return Promotion(
        advertise = advertise ?: false,
        approvedUtc = approvedUtc ?: "",
        businessId = businessId ?: 0,
        createdUtc = createdUtc ?: "",
        deleted = deleted ?: false,
        description = description ?: "",
        fromUtc = fromUtc ?: "",
        id = id ?: 0,
        isAnytimeCoupon = isAnytimeCoupon ?: false,
        isApproved = isApproved ?: false,
        isShowOnHome = isShowOnHome ?: false,
        name = name ?: "",
        numberOfUse = numberOfUse ?: 0,
        planFromUtc = planFromUtc ?: "",
        planToUtc = planToUtc ?: "",
        revenueTotal = revenueTotal ?: 0,
        selectedBusinessId = selectedBusinessId ?: 0,
        toUtc = toUtc ?: ""
    )
}
