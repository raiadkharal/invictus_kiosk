package net.invictusmanagement.invictuskiosk.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PromotionsCategory(
    val id: String,
    val name: String
)