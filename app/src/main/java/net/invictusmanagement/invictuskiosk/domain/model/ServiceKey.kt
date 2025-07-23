package net.invictusmanagement.invictuskiosk.domain.model

data class ServiceKey(
    val accessLogId: Int=0,
    val accessPointId: Int,
    val isValid: Boolean = false,
    val key: String,
    val keyId: Int = 0,
    val serviceKeyUsageId: Int =0,
    val snapShotImageId: Int=0
)