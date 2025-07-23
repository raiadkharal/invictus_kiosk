package net.invictusmanagement.invictuskiosk.data.remote.dto

data class Occupant(
    val activationCode: String?,
    val displayName: String?,
    val id: Int,
    val isDoNotDisturb: Boolean?,
    val isSmartPhone: Boolean?,
    val phoneNumber: String?,
    val role: String?,
    val unitId: Int?,
    val unitNbr: String?
)