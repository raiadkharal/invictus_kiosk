package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.Resident

@Entity(tableName = "residents")
data class ResidentEntity(
    @PrimaryKey val id: Int,
    val activationCode: String?,
    val displayName: String,
    val isDoNotDisturb: Boolean?,
    val isSmartPhone: Boolean?,
    val mapId: Int?,
    val phoneNumber: String?,
    val role: String?,
    val unitId: Int?,
    val unitNbr: String?
)


fun ResidentEntity.toResident(): Resident {
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
