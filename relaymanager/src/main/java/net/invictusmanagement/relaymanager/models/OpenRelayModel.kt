package net.invictusmanagement.relaymanager.models

import androidx.annotation.IntRange

data class OpenRelayModel(
    val relayId: String,
    @IntRange(from = 0, to = 3)
    val relayNumber: Int,
    @IntRange(from = 0, to = Int.MAX_VALUE.toLong())
    val relayDelayTimer: Int,
    @IntRange(from = 0, to = Int.MAX_VALUE.toLong())
    val relayOpenTimer: Int
)
