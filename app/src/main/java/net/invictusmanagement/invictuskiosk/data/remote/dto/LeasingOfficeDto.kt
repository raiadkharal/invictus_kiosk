package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.data.local.entities.LeasingOfficeEntity
import net.invictusmanagement.invictuskiosk.domain.model.LeasingOffice

data class LeasingOfficeDto(
    val allowSinglePushCallToLeasingOffice: Boolean?,
    val leasingOfficer: LeasingOfficer?
)

fun LeasingOfficeDto.toLeasingOffice(): LeasingOffice{
    return LeasingOffice(
        allowSinglePushCallToLeasingOffice = allowSinglePushCallToLeasingOffice?:false,
        leasingOfficer = leasingOfficer
    )
}

fun LeasingOfficeDto.toEntity(): LeasingOfficeEntity {
    return LeasingOfficeEntity(
        allowSinglePushCallToLeasingOffice = allowSinglePushCallToLeasingOffice?:false,
        leasingOfficer = leasingOfficer
    )
}

