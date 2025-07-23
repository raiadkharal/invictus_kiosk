package net.invictusmanagement.invictuskiosk.domain.model

data class Resident(
    val activationCode: String?,
    val displayName: String,
    val id: Int,
    val isDoNotDisturb: Boolean?,
    val isSmartPhone: Boolean?,
    val mapId: Int?,
    val phoneNumber: String?,
    val role: String?,
    val unitId: Int?,
    val unitNbr: String?
)