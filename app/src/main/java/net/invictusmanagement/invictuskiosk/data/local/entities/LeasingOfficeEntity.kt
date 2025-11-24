package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.data.remote.dto.LeasingOfficer
import net.invictusmanagement.invictuskiosk.domain.model.LeasingOffice

@Entity(tableName = "leasing_office")
data class LeasingOfficeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val allowSinglePushCallToLeasingOffice: Boolean,
    val leasingOfficer: LeasingOfficer?
)


fun LeasingOfficeEntity.toLeasingOffice(): LeasingOffice {
    return LeasingOffice(
        allowSinglePushCallToLeasingOffice = allowSinglePushCallToLeasingOffice,
        leasingOfficer = leasingOfficer
    )
}
