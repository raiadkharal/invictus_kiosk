package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.MissedCall

data class MissedCallDto(
    val kioskName: String?,
    val residentActivationCode: String?
)


fun MissedCallDto.toMissedCall():MissedCall{
    return MissedCall(
        kioskName = kioskName,
        residentActivationCode = residentActivationCode
    )
}