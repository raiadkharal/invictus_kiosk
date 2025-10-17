package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.Unit

data class UnitDto(
    val area: Float?,
    val availableDateUtc: String?,
    val bathrooms: Float?,
    val bedrooms: Float?,
    val floor: Int?,
    val id: Int,
    val imageIds: List<Long>?,
    val isRentHide: Boolean?,
    val isUnitsForSale: Boolean?,
    val rent: Float?,
    val unitNbr: String?
)

fun UnitDto.toUnit(): Unit {
    return Unit(
        area = area ?: 0.0F,
        availableDateUtc = availableDateUtc ?: "",
        bathrooms = bathrooms ?: 0.0F,
        bedrooms = bedrooms ?: 0.0F,
        floor = floor ?: 0,
        id = id,
        imageIds = imageIds ?: emptyList(),
        isRentHide = isRentHide ?: false,
        isUnitsForSale = isUnitsForSale ?: false,
        rent = rent ?: 0.0F,
        unitNbr = unitNbr ?: ""
    )
}