package net.invictusmanagement.invictuskiosk.domain.model

data class Unit(
    val area: Int,
    val availableDateUtc: String,
    val bathrooms: Int,
    val bedrooms: Int,
    val floor: Int,
    val id: Int,
    val imageIds: List<Any>,
    val isRentHide: Boolean,
    val isUnitsForSale: Boolean,
    val rent: Int,
    val unitNbr: String
)