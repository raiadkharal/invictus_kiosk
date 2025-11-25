package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.ResidentEntity
import net.invictusmanagement.invictuskiosk.domain.model.Resident

data class ResidentDto(
    val activationCode: String?,
    val displayName: String,
    val id: Int,
    val isDoNotDisturb: Boolean?,
    val isSmartPhone: Boolean?,
    val mapId: Int?,
    val phoneNumber: String?,
    val role: String?,
    val unitId: Int?,
    val unitNbr: String?
)

fun ResidentDto.toResident(): Resident {
    return Resident(
        activationCode = activationCode,
        displayName = displayName,
        id = id,
        isDoNotDisturb = isDoNotDisturb,
        isSmartPhone = isSmartPhone,
        mapId = mapId,
        phoneNumber = phoneNumber,
        role = role,
        unitId = unitId,
        unitNbr = unitNbr
    )
}

fun ResidentDto.toResidentEntity(): ResidentEntity {
    return ResidentEntity(
        id = id,
        activationCode = activationCode,
        displayName = displayName,
        isDoNotDisturb = isDoNotDisturb,
        isSmartPhone = isSmartPhone,
        mapId = mapId,
        phoneNumber = phoneNumber,
        role = role,
        unitId = unitId,
        unitNbr = unitNbr
    )
}
