package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest

data class ContactRequestDto(
    val email: String? = null,
    val inquirerImageBytes: String = "",
    val name: String,
    val phone: String? = null,
    val unitId: Int,
    val unitNbr: String
)


fun ContactRequestDto.toContactRequest(): ContactRequest{
    return ContactRequest(
        email = email,
        inquirerImageBytes = inquirerImageBytes,
        name = name,
        phone = phone,
        unitId = unitId,
        unitNbr = unitNbr
    )
}