package net.invictusmanagement.invictuskiosk.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AccessPoint(
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