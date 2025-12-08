package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.ServiceKey

data class ServiceKeyDto(
    val accessLogId: Long = 0,
    val accessPointId: Long,
    val isValid: Boolean = false,
    val key: String,
    val keyId: Long = 0,
    val serviceKeyUsageId: Long = 0,
    val snapShotImageId: Long = 0
)

fun ServiceKeyDto.toServiceKey(): ServiceKey {
    return ServiceKey(
        accessLogId = accessLogId,
        accessPointId = accessPointId,
        isValid = isValid,
        key = key,
        keyId = keyId,
        serviceKeyUsageId = serviceKeyUsageId,
        snapShotImageId = snapShotImageId
    )
}