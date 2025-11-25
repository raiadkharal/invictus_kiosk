package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint

@Entity(tableName = "access_points")
data class AccessPointEntity(

    @PrimaryKey(autoGenerate = false)
    val id: Int,

    val ledLightOnFrom: String? = null,
    val ledLightOnTo: String? = null,
    val ledLightPort: Int? = null,

    val lumiLightOnFrom: String? = null,
    val lumiLightOnTo: String? = null,
    val lumiLightPort: Int? = null,

    val name: String = "",

    val relayDelayTimer: Int? = null,
    val relayOpenTimer: Int? = null,
    val relayPort: Int? = null,

    val type: Int? = null
)

fun AccessPointEntity.toAccessPoint(): AccessPoint =
    AccessPoint(
        id = id,
        ledLightOnFrom = ledLightOnFrom,
        ledLightOnTo = ledLightOnTo,
        ledLightPort = ledLightPort,
        lumiLightOnFrom = lumiLightOnFrom,
        lumiLightOnTo = lumiLightOnTo,
        lumiLightPort = lumiLightPort,
        name = name,
        relayDelayTimer = relayDelayTimer,
        relayOpenTimer = relayOpenTimer,
        relayPort = relayPort,
        type = type
    )