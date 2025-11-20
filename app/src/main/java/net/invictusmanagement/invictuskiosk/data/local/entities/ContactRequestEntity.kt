package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.ContactRequest

@Entity(tableName = "contact_requests")
data class ContactRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val email: String?,
    val inquirerImageBytes: String,
    val name: String,
    val phone: String?,
    val unitId: Int,
    val unitNbr: String
)


fun ContactRequestEntity.toContactRequest(): ContactRequest {
    return ContactRequest(
        email = email,
        inquirerImageBytes = inquirerImageBytes,
        name = name,
        phone = phone,
        unitId = unitId,
        unitNbr = unitNbr
    )
}
