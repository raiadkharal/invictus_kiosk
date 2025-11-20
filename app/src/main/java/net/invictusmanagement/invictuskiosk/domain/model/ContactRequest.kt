package net.invictusmanagement.invictuskiosk.domain.model

import net.invictusmanagement.invictuskiosk.data.local.entities.ContactRequestEntity

data class ContactRequest(
    val email: String? = null,
    val inquirerImageBytes: String = "",
    val name: String,
    val phone: String? = null,
    val unitId: Int,
    val unitNbr: String
)

fun ContactRequest.toEntity(): ContactRequestEntity {
    return ContactRequestEntity(
        email = email,
        inquirerImageBytes = inquirerImageBytes,
        name = name,
        phone = phone,
        unitId = unitId,
        unitNbr = unitNbr
    )
}
