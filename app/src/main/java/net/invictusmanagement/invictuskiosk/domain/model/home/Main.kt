package net.invictusmanagement.invictuskiosk.domain.model.home

import kotlinx.serialization.Serializable
import net.invictusmanagement.invictuskiosk.data.remote.dto.home.Kiosk

@Serializable
data class Main(
    val kiosk: Kiosk,
    val ssUrl: String
)