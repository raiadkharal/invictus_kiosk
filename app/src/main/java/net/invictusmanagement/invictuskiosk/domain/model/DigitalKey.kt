package net.invictusmanagement.invictuskiosk.domain.model

data class DigitalKey(
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