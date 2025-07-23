package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.ServiceKey

data class ServiceKeyDto(
    val accessLogId: Int=0,
    val accessPointId: Int,
    val isValid: Boolean = false,
    val key: String,
    val keyId: Int = 0,
    val serviceKeyUsageId: Int =0,
    val snapShotImageId: Int=0
)

fun ServiceKeyDto.toServiceKey():ServiceKey{
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