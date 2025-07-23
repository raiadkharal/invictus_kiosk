package net.invictusmanagement.invictuskiosk.domain.model

import net.invictusmanagement.invictuskiosk.data.remote.dto.LeasingOfficer

data class LeasingOffice(
    val allowSinglePushCallToLeasingOffice: Boolean,
    val leasingOfficer: LeasingOfficer?
)