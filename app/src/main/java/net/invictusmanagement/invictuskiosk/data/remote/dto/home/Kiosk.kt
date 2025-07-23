package net.invictusmanagement.invictuskiosk.data.remote.dto.home

import kotlinx.serialization.Serializable

@Serializable
data class Kiosk(
    val activationCode: String?,
    val buildingId: Int?,
    val createdUtc: String?,
    val deleted: Boolean?,
    val host: Int?,
    val id: Int?,
    val isActivated: Boolean?,
    val isActive: Boolean?,
    val isAutoUpdateServiceInstalled: Boolean?,
    val isGuestKeyEnable: Boolean?,
    val isHostEnable: Boolean?,
    val isKioskFontSizeLarge: Boolean?,
    val isUnitFilterEnable: Boolean?,
    val kioskVolume: Int?,
    val location: Location?,
    val locationId: Int?,
    val name: String?,
    val relayModel: String?
)