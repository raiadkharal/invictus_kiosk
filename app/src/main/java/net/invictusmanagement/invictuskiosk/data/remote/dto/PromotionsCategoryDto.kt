package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

data class PromotionsCategoryDto(
    val id: String,
    val name: String
)

fun PromotionsCategoryDto.toPromotionsCategory(): PromotionsCategory{
    return PromotionsCategory(
        id = id,
        name = name
    )
}