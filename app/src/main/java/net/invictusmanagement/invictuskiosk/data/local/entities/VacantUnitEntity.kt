package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.Unit

@Entity(tableName = "vacant_units")
data class VacantUnitEntity(
    @PrimaryKey val id: Int,
    val area: Float?,
    val availableDateUtc: String?,
    val bathrooms: Float?,
    val bedrooms: Float?,
    val floor: Int?,
    val imageIds: List<Long>?,   // Requires TypeConverter
    val isRentHide: Boolean?,
    val isUnitsForSale: Boolean?,
    val rent: Float?,
    val unitNbr: String?
)

fun VacantUnitEntity.toUnit(): Unit {
    return Unit(
        id = id,
        area = area ?: 0.0f,
        availableDateUtc = availableDateUtc ?: "",
        bathrooms = bathrooms ?: 0.0f,
        bedrooms = bedrooms ?: 0.0f,
        floor = floor ?: 0,
        imageIds = imageIds ?: emptyList(),
        isRentHide = isRentHide ?: false,
        isUnitsForSale = isUnitsForSale ?: false,
        rent = rent ?: 0.0f,
        unitNbr = unitNbr ?: ""
    )
}
