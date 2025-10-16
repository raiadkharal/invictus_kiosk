package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.Unit

data class UnitDto(
    val area: Int?,
    val availableDateUtc: String?,
    val bathrooms: Float?,
    val bedrooms: Int?,
    val floor: Int?,
    val id: Int,
    val imageIds: List<Any>?,
    val isRentHide: Boolean?,
    val isUnitsForSale: Boolean?,
    val rent: Int?,
    val unitNbr: String?
)

fun UnitDto.toUnit(): Unit {
    return Unit(
        area = area ?: 0,
        availableDateUtc = availableDateUtc ?: "",
        bathrooms = bathrooms ?: 0.0F,
        bedrooms = bedrooms ?: 0,
        floor = floor ?: 0,
        id = id,
        imageIds = imageIds ?: emptyList(),
        isRentHide = isRentHide ?: false,
        isUnitsForSale = isUnitsForSale ?: false,
        rent = rent ?: 0,
        unitNbr = unitNbr ?: ""
    )
}