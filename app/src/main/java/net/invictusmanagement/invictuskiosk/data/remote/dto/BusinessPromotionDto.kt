package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.BusinessPromotionEntity
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion

data class BusinessPromotionDto(
    val type: Int?,
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
        type = type ?: 0,
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

fun BusinessPromotionDto.toEntity(): BusinessPromotionEntity =
    BusinessPromotionEntity(
        type = type ?: 0,
        name = name ?: "",
        address1 = address1 ?: "",
        address2 = address2 ?: "",
        city = city ?: "",
        phone = phone ?: "",
        promotions = promotions?.map { it.toPromotion() } ?: emptyList(),
        state = state ?: "",
        zip = zip ?: ""
    )