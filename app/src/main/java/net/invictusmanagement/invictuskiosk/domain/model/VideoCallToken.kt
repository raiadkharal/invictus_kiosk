package net.invictusmanagement.invictuskiosk.domain.model

data class VideoCallToken(
    val token: String,
    val videoCallTimeout: Int = 45
)