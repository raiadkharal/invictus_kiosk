package net.invictusmanagement.invictuskiosk.domain.model

import net.invictusmanagement.invictuskiosk.data.remote.dto.Occupant

data class UnitList(
    val id: Int,
    val occupants: List<Occupant>?,
    val unitNbr: String
)