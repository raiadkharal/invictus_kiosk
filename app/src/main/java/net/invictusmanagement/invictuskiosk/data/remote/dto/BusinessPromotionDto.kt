package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion

data class BusinessPromotionDto(
    val address1: String?,
    val address2: String?,
    val city: String?,
    val name: String?,
    val phone: String?,
    val promotions: List<PromotionDto>?,
    val state: String?,
    val zip: String?
)

fun BusinessPromotionDto.toBusinessPromotion(): BusinessPromotion {
    return BusinessPromotion(
        address1 = address1 ?: "",
        address2 = address2 ?: "",
        city = city ?: "",
        name = name ?: "",
        phone = phone ?: "",
        promotions = promotions?.map { it.toPromotion() } ?: emptyList(),
        state = state ?: "",
        zip = zip ?: ""
    )
}
