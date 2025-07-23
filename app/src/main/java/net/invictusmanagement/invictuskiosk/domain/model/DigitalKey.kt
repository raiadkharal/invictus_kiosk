package net.invictusmanagement.invictuskiosk.domain.model

data class DigitalKey(
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