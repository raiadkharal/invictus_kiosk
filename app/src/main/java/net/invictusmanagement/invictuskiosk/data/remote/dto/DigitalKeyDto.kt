package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.DigitalKey

data class DigitalKeyDto(
    val accessLogId: Long = 0,
    val accessPointId: Long = 0,
    val activationCode: String? = null,
    val attemptCount: Long = 0,
    val isValid: Boolean = false,
    val key: String = "",
    val mapId: Long = 0,
    val recipient: String = "",
    val snapShotImageId: Long = 0,
    val toPackageCenter: Boolean = false,
    val unitId: Long = 0
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