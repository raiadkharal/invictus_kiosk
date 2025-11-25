package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.UnitList

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: Int,
    val unitNbr: String
)

fun UnitEntity.toUnitList(): UnitList {
    return UnitList(
        id = id,
        occupants = emptyList(),
        unitNbr = unitNbr
    )
}
