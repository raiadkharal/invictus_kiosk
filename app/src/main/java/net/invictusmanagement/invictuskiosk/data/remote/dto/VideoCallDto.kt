package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.VideoCall

data class VideoCallDto(
    val accessPointId: Int,
    val residentActivationCode: String
)


fun VideoCallDto.toVideoCall(): VideoCall {
    return VideoCall(
        accessPointId = accessPointId,
        residentActivationCode = residentActivationCode
    )
}