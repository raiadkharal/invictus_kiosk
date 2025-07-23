package net.invictusmanagement.invictuskiosk.domain.model

data class ContactRequest(
    val email: String? = null,
    val inquirerImageBytes: String = "",
    val name: String,
    val phone: String? = null,
    val unitId: Int,
    val unitNbr: String
)