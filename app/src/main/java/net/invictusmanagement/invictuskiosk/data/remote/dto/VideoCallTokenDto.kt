package net.invictusmanagement.invictuskiosk.data.remote.dto

import net.invictusmanagement.invictuskiosk.domain.model.VideoCallToken

data class VideoCallTokenDto(
    val token: String
)

fun VideoCallTokenDto.toVideoCallToken(): VideoCallToken {
    return VideoCallToken(
        token = token
    )
}