package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.UnitEntity
import net.invictusmanagement.invictuskiosk.domain.model.UnitList

data class UnitListDto(
    val id: Int,
    val occupants: List<Occupant>?,
    val unitNbr: String
)

fun UnitListDto.toUnitList(): UnitList {
    return UnitList(
        id = id,
        occupants = occupants,
        unitNbr = unitNbr
    )
}

fun UnitListDto.toUnitEntity(): UnitEntity {
    return UnitEntity(
        id = id,
        unitNbr = unitNbr
    )
}
