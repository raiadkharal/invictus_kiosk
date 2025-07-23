package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.AccessPoint

data class AccessPointDto(
    val id: Int = 0,
    val ledLightOnFrom: String? = null,
    val ledLightOnTo: String? = null,
    val ledLightPort: Int? = null,
    val lumiLightOnFrom: String? = null,
    val lumiLightOnTo: String? = null,
    val lumiLightPort: Int?=null,
    val name: String = "",
    val relayDelayTimer: Int? = null,
    val relayOpenTimer: Int? =null,
    val relayPort: Int? = null,
    val type: Int? = null
)

fun AccessPointDto.toAccessPoint(): AccessPoint {
    return AccessPoint(
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
}