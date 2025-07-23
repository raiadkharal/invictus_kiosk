package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey

data class DigitalKeyDto(
    val accessLogId: Int = 0,
    val accessPointId: Int = 0,
    val activationCode: String? = null,
    val attemptCount: Int = 0,
    val isValid: Boolean = false,
    val key: String = "",
    val mapId: Int = 0,
    val recipient: String = "",
    val snapShotImageId: Int = 0,
    val toPackageCenter: Boolean = false,
    val unitId: Int = 0
)

fun DigitalKeyDto.toDigitalKey(): DigitalKey {
    return DigitalKey(
        accessLogId = accessLogId,
        accessPointId = accessPointId,
        activationCode = activationCode,
        attemptCount = attemptCount,
        isValid = isValid,
        key = key,
        mapId = mapId,
        recipient = recipient,
        snapShotImageId = snapShotImageId,
        toPackageCenter = toPackageCenter,
        unitId = unitId
    )
}