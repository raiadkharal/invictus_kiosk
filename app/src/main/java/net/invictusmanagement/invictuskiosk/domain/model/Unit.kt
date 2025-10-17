package net.invictusmanagement.invictuskiosk.domain.model

data class Unit(
    val area: Float,
    val availableDateUtc: String,
    val bathrooms: Float,
    val bedrooms: Float,
    val floor: Int,
    val id: Int,
    val imageIds: List<Any>,
    val isRentHide: Boolean,
    val isUnitsForSale: Boolean,
    val rent: Float,
    val unitNbr: String
)