package net.invictusmanagement.invictuskiosk.domain.model

data class ServiceKey(
    val accessLogId: Long = 0,
    val accessPointId: Long,
    val isValid: Boolean = false,
    val key: String,
    val keyId: Long = 0L,
    val serviceKeyUsageId: Long = 0,
    val snapShotImageId: Long = 0
)